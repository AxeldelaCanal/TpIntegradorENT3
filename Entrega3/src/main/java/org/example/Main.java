package org.example;

import org.example.Objetos.Pronostico;
import org.example.Objetos.Resultado;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;




public class Main {
    private static final int PUNTOS_POR_RESULTADO = 1;

    public static void main(String[] args) {

        int PUNTOS_POR_RESULTADO = 1; // Valor por defecto

        // Leer los archivos de partidos y resultados
        String[] partidos = leerArchivo("C:\\Users\\Gc\\OneDrive\\Escritorio\\TP3Java\\TpIntegradorENT3\\Entrega3\\src\\main\\java\\org\\example\\Archivos\\partidos.txt");
        String[] resultados = leerArchivo("C:\\Users\\Gc\\OneDrive\\Escritorio\\TP3Java\\TpIntegradorENT3\\Entrega3\\src\\main\\java\\org\\example\\Archivos\\resultados.txt");

        // Crear objeto Resultado y objeto Pronostico para cada persona
        Resultado resultado = new Resultado(Arrays.asList(resultados));
        Pronostico[] pronosticos = new Pronostico[] {
                new Pronostico(1, Arrays.asList(leerPronosticosBD(1))),
                new Pronostico(2, Arrays.asList(leerPronosticosBD(2))),
                new Pronostico(3, Arrays.asList(leerPronosticosBD(3))),
                new Pronostico(4, Arrays.asList(leerPronosticosBD(4))),
                new Pronostico(5, Arrays.asList(leerPronosticosBD(5))),
                new Pronostico(6, Arrays.asList(leerPronosticosBD(6))),
                new Pronostico(7, Arrays.asList(leerPronosticosBD(7)))
        };


        // Personas y sus IDs en la base de datos
        int[] idsPersonas = {1, 2, 3, 4, 5, 6, 7};
        String[] nombresPersonas = {
                "Facundo Reinoso:",
                "Carlos de la Canal:",
                "Maria Rodriguez:",
                "Juan Perez:",
                "Axel de la Canal:",
                "Alicia Cebrian:",
                "Nicolas Amado:"
        };

        // Calcular y mostrar puntuaciones de todas las personas
        for (int i = 0; i < idsPersonas.length; i++) {
            Map<String, Integer> aciertosPorFase = new HashMap<>();
            int puntos = calcularPuntos(pronosticos[i], resultado, aciertosPorFase);
            int puntosExtraRonda = calcularPuntosExtraRonda(pronosticos[i], resultado);
            int puntosExtraFase = calcularPuntosExtraFase(aciertosPorFase);
            puntos += puntosExtraRonda + puntosExtraFase;
            System.out.println(nombresPersonas[i] + " ha obtenido " + puntos + " puntos.");
        }
    }

    private static String[] leerArchivo(String nombreArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            return br.lines().toArray(String[]::new);
        } catch (IOException e) {
            System.err.format("Error al leer el archivo %s: %s%n", nombreArchivo, e);
            return null;
        }
    }

        private static int calcularPuntos(Pronostico pronostico, Resultado resultado, Map<String, Integer> aciertosPorFase) {
            int puntos = 0;

            // Verificar si cada pronóstico coincide con el resultado real
            List<String> resultados = resultado.getResultados();
            List<String> pronosticosList = pronostico.getPronosticos();

            for (int i = 0; i < resultados.size(); i++) {
                String[] resultadoArray = resultados.get(i).split("-");
                String[] pronosticoArray = pronosticosList.get(i).split("-");

                int golesReal = Integer.parseInt(resultadoArray[0]) + Integer.parseInt(resultadoArray[1]);
                int golesPronosticados = Integer.parseInt(pronosticoArray[0]) + Integer.parseInt(pronosticoArray[1]);

                if (golesReal == golesPronosticados) {
                    puntos += PUNTOS_POR_RESULTADO;
                }
            }

            return puntos;
        }

    private static int calcularPuntosExtraRonda(Pronostico pronostico, Resultado resultado) {
        int puntosExtra = 0;

        List<String> pronosticoList = pronostico.getPronosticos();

        // Calcular puntos extras por ronda
        int rondaActual = 1;
        int aciertosRonda = 0;
        for (int i = 0; i < resultado.size(); i++) {
            String[] resultadoArray = resultado.get(i).split("-");
            String[] pronosticoArray = pronosticoList.get(i).split("-");

            int golesReal = Integer.parseInt(resultadoArray[0]) + Integer.parseInt(resultadoArray[1]);
            int golesPronosticados = Integer.parseInt(pronosticoArray[0]) + Integer.parseInt(pronosticoArray[1]);

            // Verificar si el pronóstico coincide con el resultado real
            if (golesReal == golesPronosticados) {
                aciertosRonda++;
            }

            // Verificar si se ha terminado la ronda actual
            if ((i + 1) % 3 == 0) {
                if (aciertosRonda == 3) {
                    puntosExtra += 3;
                }
                aciertosRonda = 0;
                rondaActual++;
            }
        }
        return puntosExtra;
    }



        private static int calcularPuntosExtraFase(Map<String, Integer> aciertosPorFase) {
            int puntosExtra = 0;

            // Calcular puntos extras por fase
            for (Map.Entry<String, Integer> entry : aciertosPorFase.entrySet()) {
                if (entry.getValue() == 3) { // Si se acertaron todos los partidos de la fase
                    puntosExtra += 10; // Se suman 10 puntos extras
                }
            }
            return puntosExtra;
        }

    private static String[] leerPronosticosBD(int personaID) {
        String[] pronosticos = null;
        Connection conn = conectarBD();

        if (conn != null) {
            try {
                Statement stmt = conn.createStatement();
                String sql = "SELECT pronostico FROM pronosticos WHERE persona_id = " + personaID + " ORDER BY partido_id ASC";
                ResultSet rs = stmt.executeQuery(sql);

                List<String> pronosticosList = new ArrayList<>();
                while (rs.next()) {
                    pronosticosList.add(rs.getString("pronostico"));
                }

                pronosticos = pronosticosList.toArray(new String[0]);

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.err.format("Error al leer los pronósticos de la persona con ID %d: %s%n", personaID, e);
            }
        }

        return pronosticos;
    }


        private static Connection conectarBD() {
        String url = "jdbc:mysql://localhost:3306/mydatabase?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "root";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (SQLException e) {
            System.err.format("Error al conectar con la base de datos: %s%n", e);
            return null;
        }
    }
}