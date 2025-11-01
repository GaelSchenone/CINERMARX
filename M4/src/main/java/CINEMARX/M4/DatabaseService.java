/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CINEMARX.M4;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gaels
 */
public class DatabaseService {
    private Connection connection;
    
    public DatabaseService(String url, String user, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, user, password);
    }
    
    public List<Pelicula> obtenerPeliculasEnCartelera() {
        List<Pelicula> peliculas = new ArrayList<>();
        String query = "SELECT * FROM peliculas WHERE en_cartelera = true";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                peliculas.add(new Pelicula(
                    rs.getInt("id"),
                    rs.getString("titulo"),
                    rs.getString("imagen"),
                    rs.getString("clasificacion"),
                    rs.getString("formato"),
                    rs.getString("duracion"),
                    rs.getString("fecha_estreno"),
                    rs.getString("descripcion")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return peliculas;
    }
    
    public Pelicula obtenerPeliculaPorId(int id) {
        String query = "SELECT * FROM peliculas WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Pelicula(
                    rs.getInt("id"),
                    rs.getString("titulo"),
                    rs.getString("imagen"),
                    rs.getString("clasificacion"),
                    rs.getString("formato"),
                    rs.getString("duracion"),
                    rs.getString("fecha_estreno"),
                    rs.getString("descripcion")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<Funcion> obtenerFuncionesPorPelicula(int peliculaId) {
        List<Funcion> funciones = new ArrayList<>();
        String query = "SELECT * FROM funciones WHERE pelicula_id = ? ORDER BY fecha, hora";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, peliculaId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                funciones.add(new Funcion(
                    rs.getInt("id"),
                    rs.getInt("pelicula_id"),
                    rs.getString("fecha"),
                    rs.getString("hora"),
                    rs.getString("idioma"),
                    rs.getString("formato"),
                    rs.getInt("sala_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return funciones;
    }
    
    public List<Butaca> obtenerButacasSala(int salaId, int funcionId) {
        List<Butaca> butacas = new ArrayList<>();
        
        // Primero obtenemos la configuración de la sala
        String querySala = "SELECT filas, columnas FROM salas WHERE id = ?";
        int filas = 0, columnas = 0;
        
        try (PreparedStatement pstmt = connection.prepareStatement(querySala)) {
            pstmt.setInt(1, salaId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                filas = rs.getInt("filas");
                columnas = rs.getInt("columnas");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Luego obtenemos las butacas ocupadas para esta función
        String queryOcupadas = "SELECT fila, columna FROM reservas " +
                               "WHERE funcion_id = ? AND estado = 'ocupada'";
        List<String> ocupadas = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(queryOcupadas)) {
            pstmt.setInt(1, funcionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ocupadas.add(rs.getInt("fila") + "-" + rs.getInt("columna"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Creamos todas las butacas
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                boolean ocupada = ocupadas.contains(i + "-" + j);
                butacas.add(new Butaca(i, j, ocupada));
            }
        }
        
        return butacas;
    }
    
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}