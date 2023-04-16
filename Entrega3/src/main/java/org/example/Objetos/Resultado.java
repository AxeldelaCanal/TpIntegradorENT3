package org.example.Objetos;

import java.util.List;

public class Resultado {
    private List<String> resultados;

    public Resultado(List<String> resultados) {
        this.resultados = resultados;
    }

    public int size() {
        return resultados.size();
    }

    public String get(int i) {
        return resultados.get(i);
    }

    public List<String> getResultados() {
        return resultados;
    }
}