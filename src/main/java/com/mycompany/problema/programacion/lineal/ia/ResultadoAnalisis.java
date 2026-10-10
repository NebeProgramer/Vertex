package com.mycompany.problema.programacion.lineal.ia;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

/**
 * Resultado de {@link AnalizadorProblema#analizar(String)}.
 *
 * {@code problema} tiene la misma forma que un elemento de history.json
 * (z, Tipo, R, Metodos) sin id, así que se inyecta con el mismo código del
 * historial. Puede ser null si el modelo nunca devolvió un JSON legible.
 */
public final class ResultadoAnalisis {

    private final JSONObject problema;
    private final boolean valido;
    private final String error;
    private final List<String> advertencias;

    ResultadoAnalisis(JSONObject problema, boolean valido, String error, List<String> advertencias) {
        this.problema = problema;
        this.valido = valido;
        this.error = error;
        this.advertencias = Collections.unmodifiableList(advertencias);
    }

    public JSONObject getProblema() {
        return problema;
    }

    /** true si Vertex aceptó el modelo tal cual (validateAndBuild no se quejó). */
    public boolean isValido() {
        return valido;
    }

    /** Mensaje del parser de Vertex cuando {@link #isValido()} es false. */
    public String getError() {
        return error;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }
}
