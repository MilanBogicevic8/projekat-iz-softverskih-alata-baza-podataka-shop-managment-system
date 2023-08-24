package rs.etf.sab.student;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private Connection connection;

    public Graph(Connection connection) {
        this.connection = connection;
    }

    public List<Transport> findShortestPath(int source, int destination) {
        List<Transport> transports = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Linija");

            // Inicijalizacija grafa
            int[][] graph = new int[100][100];
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    graph[i][j] = Integer.MAX_VALUE;
                }
            }

            // Popunjavanje grafa na osnovu podataka iz tabele
            while (resultSet.next()) {
                int distance = resultSet.getInt("Rastojanje");
                int idG1 = resultSet.getInt("IdG1");
                int idG2 = resultSet.getInt("IdG2");
                graph[idG1][idG2] = distance;
                graph[idG2][idG1] = distance;
            }

            // Primena algoritma za pronalaženje najkraćeg puta (npr. Dijkstrin algoritam)
            int[] distances = new int[100];
            boolean[] visited = new boolean[100];
            int[] previous = new int[100];

            for (int i = 0; i < 100; i++) {
                distances[i] = Integer.MAX_VALUE;
                visited[i] = false;
                previous[i] = -1;
            }

            distances[source] = 0;

            for (int i = 0; i < 100; i++) {
                int minDistance = Integer.MAX_VALUE;
                int minIndex = -1;

                for (int j = 0; j < 100; j++) {
                    if (!visited[j] && distances[j] < minDistance) {
                        minDistance = distances[j];
                        minIndex = j;
                    }
                }

                if (minIndex == -1) {
                    break;
                }

                visited[minIndex] = true;

                for (int j = 0; j < 100; j++) {
                    if (!visited[j] && graph[minIndex][j] != Integer.MAX_VALUE) {
                        int newDistance = distances[minIndex] + graph[minIndex][j];
                        if (newDistance < distances[j]) {
                            distances[j] = newDistance;
                            previous[j] = minIndex;
                        }
                    }
                }
            }

            // Dodavanje Transport objekata za svaki par gradova na putu
            int current = destination;
            while (current != -1 && current != source) {
                int previousCity = previous[current];
                if (previousCity != -1) {
                    transports.add(new Transport(previousCity, current, (int) graph[previousCity][current]));
                }
                current = previousCity;
            }

            Collections.reverse(transports); // Obrni redosled transportnih objekata

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transports;
    }
    
    public int rastojanje(int source, int destination){
        List<Transport> lista=findShortestPath(source, destination);
        int rastojanje=0;
        for(Transport t:lista){
            //System.out.println(t.getCity1()+" "+t.getCity2()+" "+t.getDistance());
            rastojanje=rastojanje+t.getDistance();
        }
        return rastojanje;
    }
    
     public static class Transport {
                private int city1;
                private int city2;
                private int distance;

                public Transport(int city1, int city2, int distance) {
                    this.city1 = city1;
                    this.city2 = city2;
                    this.distance = distance;
                }

                public int getCity1() {
                    return city1;
                }

                public int getCity2() {
                    return city2;
                }

                public int getDistance() {
                    return distance;
                }

        @Override
        public String toString() {
            return "Od: "+city1+"Do: "+city2+"Rastojanje: "+distance;
        }
                
                
    }
     
     
    public static void main(String[] args) {
       List<Transport> lista=(new Graph(DB.getInstance().getConnection())).findShortestPath(6, 1);
       for(Transport t:lista){
           System.out.println(t);
       }
    }
}