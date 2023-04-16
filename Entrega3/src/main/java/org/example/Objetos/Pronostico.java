package org.example.Objetos;

import java.util.List;

public class Pronostico {
    private int personaID;
    private List<String> pronosticos;

    public Pronostico(int personaID, List<String> pronosticos) {
        this.personaID = personaID;
        this.pronosticos = pronosticos;
    }

    public int getPersonaID() {
        return personaID;
    }

    public List<String> getPronosticos() {
        return pronosticos;
    }
}

