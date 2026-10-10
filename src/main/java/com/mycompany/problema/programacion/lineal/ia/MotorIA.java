package com.mycompany.problema.programacion.lineal.ia;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

/**
 * Motor de lenguaje que convierte un enunciado en el modelo de Vertex.
 * Separado de {@link ClienteOllama} para poder probar
 * {@link AnalizadorProblema} con un motor falso y, más adelante, enchufar
 * otros proveedores (LM Studio, llama.cpp server...).
 */
public interface MotorIA {

    /**
     * Envía una conversación (mensajes {"role", "content"}) y devuelve el
     * texto de la respuesta, que debe respetar {@code esquemaJson} (JSON
     * schema como texto: el orden de las propiedades importa, porque el
     * modelo las genera en ese orden, y JSONObject no lo conserva).
     */
    String chat(List<JSONObject> mensajes, String esquemaJson) throws IOException, InterruptedException;
}
