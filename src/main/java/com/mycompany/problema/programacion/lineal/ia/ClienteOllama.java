package com.mycompany.problema.programacion.lineal.ia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Cliente HTTP mínimo para Ollama (https://ollama.com), el motor de IA
 * gratuito que corre en la máquina del usuario. Solo usa el HttpClient de
 * Java 17 y org.json, así que no agrega dependencias al pom.
 *
 * La URL y el modelo se guardan en Preferences (mismo patrón que la
 * posición de la ventana principal), por si el usuario quiere cambiarlos.
 */
public class ClienteOllama implements MotorIA {

    public static final String URL_POR_DEFECTO = "http://localhost:11434";
    public static final String MODELO_POR_DEFECTO = "qwen2.5:3b";
    public static final String LINK_DESCARGA = "https://ollama.com/download";

    private static final String PREF_URL = "iaUrl";
    private static final String PREF_MODELO = "iaModelo";
    /** En CPU un modelo de 3B puede tardar 10-40 s; se deja margen. */
    private static final Duration TIMEOUT_GENERAR = Duration.ofSeconds(180);
    /** Alcanza para prompt de sistema + enunciado + un reintento con su historial. */
    private static final int CONTEXTO_TOKENS = 8192;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private final String url;
    private final String modelo;

    public ClienteOllama() {
        Preferences prefs = Preferences.userNodeForPackage(ClienteOllama.class);
        this.url = quitarBarraFinal(prefs.get(PREF_URL, URL_POR_DEFECTO));
        this.modelo = prefs.get(PREF_MODELO, MODELO_POR_DEFECTO);
    }

    public String getModelo() {
        return modelo;
    }

    /** true si hay un servidor de Ollama respondiendo en la URL configurada. */
    public boolean disponible() {
        try {
            return enviar(get("/api/tags", Duration.ofSeconds(2))).statusCode() == 200;
        } catch (IOException | InterruptedException ex) {
            return false;
        }
    }

    /** true si el modelo configurado ya está descargado en Ollama. */
    public boolean modeloInstalado() {
        try {
            HttpResponse<String> resp = enviar(get("/api/tags", Duration.ofSeconds(2)));
            if (resp.statusCode() != 200) {
                return false;
            }
            JSONArray modelos = new JSONObject(resp.body()).optJSONArray("models");
            if (modelos == null) {
                return false;
            }
            for (int i = 0; i < modelos.length(); i++) {
                String nombre = modelos.getJSONObject(i).optString("name", "");
                // "qwen2.5:3b" también puede aparecer como "qwen2.5:3b-instruct..." o sin tag ":latest"
                if (nombre.equals(modelo) || nombre.equals(modelo + ":latest") || nombre.startsWith(modelo + "-")) {
                    return true;
                }
            }
            return false;
        } catch (IOException | InterruptedException | org.json.JSONException ex) {
            return false;
        }
    }

    /**
     * Descarga el modelo configurado (POST /api/pull en streaming). Ollama
     * responde una línea JSON por avance; {@code progreso} recibe 0..1, o
     * -1 mientras no se conoce el tamaño total.
     */
    public void descargarModelo(Consumer<Double> progreso) throws IOException, InterruptedException {
        JSONObject cuerpo = new JSONObject().put("model", modelo).put("stream", true);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url + "/api/pull"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() != 200) {
            throw new IOException("Ollama respondió " + resp.statusCode() + " al descargar el modelo.");
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resp.body(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                if (linea.isBlank()) {
                    continue;
                }
                JSONObject evento = new JSONObject(linea);
                if (evento.has("error")) {
                    throw new IOException(evento.getString("error"));
                }
                long total = evento.optLong("total", 0);
                long completado = evento.optLong("completed", 0);
                progreso.accept(total > 0 ? (double) completado / total : -1.0);
            }
        }
    }

    @Override
    public String chat(List<JSONObject> mensajes, String esquemaJson) throws IOException, InterruptedException {
        JSONObject cuerpo = new JSONObject()
                .put("model", modelo)
                .put("stream", false)
                .put("messages", new JSONArray(mensajes))
                // num_ctx explícito: el valor por defecto de Ollama (2048 o 4096
                // según la versión) recorta el prompt de sistema y el modelo
                // termina ignorando las reglas de formato
                .put("options", new JSONObject().put("temperature", 0).put("num_ctx", CONTEXTO_TOKENS));
        // el schema se inserta como texto para no perder el orden de sus propiedades
        String texto = cuerpo.toString();
        texto = texto.substring(0, texto.length() - 1) + ",\"format\":" + esquemaJson + "}";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url + "/api/chat"))
                .timeout(TIMEOUT_GENERAR)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(texto, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = enviar(req);
        if (resp.statusCode() != 200) {
            String detalle = resp.body();
            try {
                detalle = new JSONObject(resp.body()).optString("error", detalle);
            } catch (org.json.JSONException ex) {
                // cuerpo no-JSON: se muestra tal cual
            }
            throw new IOException("Ollama respondió " + resp.statusCode() + ": " + detalle);
        }
        return new JSONObject(resp.body()).getJSONObject("message").getString("content");
    }

    private HttpRequest get(String ruta, Duration timeout) {
        return HttpRequest.newBuilder(URI.create(url + ruta)).timeout(timeout).GET().build();
    }

    /**
     * Envía de forma asíncrona y espera, para que un cancel(true) del
     * SwingWorker (interrupción del hilo) corte también la petición.
     */
    private HttpResponse<String> enviar(HttpRequest req) throws IOException, InterruptedException {
        CompletableFuture<HttpResponse<String>> futuro
                = http.sendAsync(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        try {
            return futuro.get();
        } catch (InterruptedException ex) {
            futuro.cancel(true);
            throw ex;
        } catch (ExecutionException ex) {
            Throwable causa = ex.getCause();
            if (causa instanceof IOException) {
                throw (IOException) causa;
            }
            throw new IOException(causa);
        }
    }

    private static String quitarBarraFinal(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
