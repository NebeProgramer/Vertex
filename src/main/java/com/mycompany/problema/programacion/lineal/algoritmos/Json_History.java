/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.problema.programacion.lineal.algoritmos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Persiste el historial de cálculos (Z, restricciones, tipo y algoritmo(s)
 * usados) en history.json, en la carpeta de trabajo de la app.
 *
 * El array siempre queda ordenado con el elemento MÁS RECIENTE en la
 * posición 0 (índice 0 = arriba de la lista visual).
 *
 * "Mismo problema" = comparación simple y exacta: mismo tipo (Max/Min),
 * misma Z (trim) y las MISMAS restricciones en el MISMO orden (trim a
 * trim). Si el usuario reordena o cambia una restricción, se trata como
 * un problema distinto — decisión tomada a propósito para evitar casos
 * raros de "equivalencia" ambigua.
 *
 * @author andor
 */
public class Json_History {

    private static final Path HISTORY_PATH = Paths.get("history.json");

    public static String generarIdHex(int longitud) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        while (sb.length() < longitud) {
            // Genera un número entero aleatorio y lo pasa a hexadecimal
            sb.append(Integer.toHexString(random.nextInt()));
        }

        // Recorta la cadena exactamente a la longitud deseada
        return sb.substring(0, longitud);
    }

    // ---------------------------------------------------------------
    // Lectura / escritura de history.json
    // ---------------------------------------------------------------

    private JSONArray leerArray() {
        try {
            if (!Files.exists(HISTORY_PATH)) {
                return new JSONArray();
            }
            String content = new String(Files.readAllBytes(HISTORY_PATH), StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return new JSONArray();
            }
            if (content.startsWith("[")) {
                return new JSONArray(content);
            }
            // admitir archivo NDJSON viejo: cada línea es un JSONObject
            JSONArray arr = new JSONArray();
            for (String line : content.split("\\R")) {
                if (!line.trim().isEmpty()) {
                    arr.put(new JSONObject(line));
                }
            }
            return arr;
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    private void escribirArray(JSONArray arr) {
        try {
            Path carpeta = HISTORY_PATH.toAbsolutePath().getParent();
            // Escritura atómica: escribir a temp y mover, para no dejar el
            // archivo a medias si la app se cierra en mitad de la escritura.
            Path tmp = Files.createTempFile(carpeta, "history", ".json");
            try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                w.write(arr.toString(2));
            }
            Files.move(tmp, HISTORY_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject construirObjeto(String id, String z, String[] R, String tipo, String[] metodos) {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("z", z);
        obj.put("Tipo", tipo);
        obj.put("R", R != null ? Arrays.asList(R) : new ArrayList<>());
        obj.put("Metodos", metodos != null ? Arrays.asList(metodos) : new ArrayList<>());
        return obj;
    }

    // ---------------------------------------------------------------
    // Comparación de "mismo problema"
    // ---------------------------------------------------------------

    private static boolean mismoProblema(JSONObject obj, String z, String[] R, String tipo) {
        if (!obj.optString("Tipo", "").equals(tipo)) {
            return false;
        }
        if (!obj.optString("z", "").trim().equals(z == null ? "" : z.trim())) {
            return false;
        }
        JSONArray rGuardado = obj.optJSONArray("R");
        int nGuardado = rGuardado != null ? rGuardado.length() : 0;
        int nNuevo = R != null ? R.length : 0;
        if (nGuardado != nNuevo) {
            return false;
        }
        for (int i = 0; i < nNuevo; i++) {
            String guardado = rGuardado.optString(i, "").trim();
            String nuevo = R[i] == null ? "" : R[i].trim();
            if (!guardado.equals(nuevo)) {
                return false;
            }
        }
        return true;
    }

    private static boolean mismosMetodos(JSONObject obj, String[] metodos) {
        JSONArray guardados = obj.optJSONArray("Metodos");
        int nGuardado = guardados != null ? guardados.length() : 0;
        int nNuevo = metodos != null ? metodos.length : 0;
        if (nGuardado != nNuevo) {
            return false;
        }
        for (int i = 0; i < nNuevo; i++) {
            if (!guardados.optString(i, "").equals(metodos[i])) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------
    // API principal: se llama cada vez que el usuario presiona "Calcular"
    // ---------------------------------------------------------------

    /**
     * Guarda el cálculo en el historial, o actualiza uno existente si el
     * problema (Z + restricciones + tipo) ya estaba guardado:
     *  - Si ya existe un elemento con el MISMO problema: se mueve a la
     *    posición 0 (más reciente) y, si la lista de métodos cambió, se
     *    reemplaza por la nueva.
     *  - Si no existe: se agrega un elemento nuevo en la posición 0.
     *
     * @return el id del elemento guardado/actualizado.
     */
    public String guardarCalculo(String z, String[] R, String tipo, String[] metodos) {
        JSONArray arr = leerArray();

        int indiceExistente = -1;
        for (int i = 0; i < arr.length(); i++) {
            if (mismoProblema(arr.getJSONObject(i), z, R, tipo)) {
                indiceExistente = i;
                break;
            }
        }

        String id;
        if (indiceExistente >= 0) {
            JSONObject existente = arr.getJSONObject(indiceExistente);
            id = existente.getString("id");
            if (!mismosMetodos(existente, metodos)) {
                existente.put("Metodos", metodos != null ? Arrays.asList(metodos) : new ArrayList<>());
            }
            // sacarlo de su posición actual y ponerlo de primero
            arr.remove(indiceExistente);
            JSONArray nuevo = new JSONArray();
            nuevo.put(existente);
            for (int i = 0; i < arr.length(); i++) {
                nuevo.put(arr.get(i));
            }
            arr = nuevo;
        } else {
            id = generarIdHex(15);
            JSONObject nuevoObj = construirObjeto(id, z, R, tipo, metodos);
            JSONArray nuevo = new JSONArray();
            nuevo.put(nuevoObj);
            for (int i = 0; i < arr.length(); i++) {
                nuevo.put(arr.get(i));
            }
            arr = nuevo;
        }

        escribirArray(arr);
        return id;
    }

    // ---------------------------------------------------------------
    // Consulta / edición manual / borrado
    // ---------------------------------------------------------------

    /** Todos los elementos, más reciente primero (mismo orden en que se guardan). */
    public List<JSONObject> listarElementos() {
        JSONArray arr = leerArray();
        List<JSONObject> lista = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            lista.add(arr.getJSONObject(i));
        }
        return lista;
    }

    public JSONObject obtenerElemento(String id) {
        JSONArray arr = leerArray();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if (obj.getString("id").equals(id)) {
                return obj;
            }
        }
        return null;
    }

    public void editElement(String id, String z, String[] R, String Tipo, String[] Metodos) {
        JSONArray arr = leerArray();
        boolean found = false;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if (obj.getString("id").equals(id)) {
                obj.put("z", z);
                obj.put("Tipo", Tipo);
                obj.put("R", R != null ? Arrays.asList(R) : new ArrayList<>());
                obj.put("Metodos", Metodos != null ? Arrays.asList(Metodos) : new ArrayList<>());
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Elemento con id " + id + " no encontrado.");
            return;
        }
        escribirArray(arr);
    }

    public void deleteElement(String id) {
        JSONArray arr = leerArray();
        JSONArray nuevo = new JSONArray();
        boolean found = false;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if (obj.getString("id").equals(id)) {
                found = true; // no se copia al nuevo array -> queda eliminado
                continue;
            }
            nuevo.put(obj);
        }
        if (!found) {
            System.out.println("Elemento con id " + id + " no encontrado.");
            return;
        }
        escribirArray(nuevo);
    }
}
