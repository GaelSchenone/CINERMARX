/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CINEMARX.M2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para la entidad Pelicula
 * Gestiona todas las operaciones CRUD con la tabla Pelicula
 */
public class PeliculaDAO {
    
    // (Tu código de INSERT, LEER (todos), ACTUALIZAR, ELIMINAR se mantiene igual...)
    // ...
    // (Omitido por brevedad, no necesita cambios)
    // ...

    // ==========================================
    // CREAR (INSERT)
    // ==========================================
    
    /**
     * Inserta una nueva película en la base de datos
     * @param pelicula Objeto Pelicula con los datos (sin ID)
     * @return true si se insertó correctamente, false si hubo error
     */
    public boolean insertar(Pelicula pelicula) {
        String sql = "INSERT INTO Pelicula (Genero, Titulo, ClasificacionEdad, Estado, Imagen) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, pelicula.getGenero());
            pstmt.setString(2, pelicula.getTitulo());
            pstmt.setString(3, pelicula.getClasificacionEdad());
            pstmt.setString(4, pelicula.getEstado());
            pstmt.setString(5, pelicula.getImagen());
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                // Obtener el ID generado automáticamente
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        pelicula.setIdPelicula(rs.getInt(1));
                    }
                }
                System.out.println("✅ Película insertada: " + pelicula.getTitulo());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al insertar película: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // ==========================================
    // LEER (SELECT)
    // ==========================================
    
    /**
     * Obtiene todas las películas de la base de datos
     * @return Lista de películas
     */
    public List<Pelicula> obtenerTodas() {
        List<Pelicula> peliculas = new ArrayList<>();
        String sql = "SELECT ID_Pelicula, Genero, Titulo, ClasificacionEdad, Estado, Imagen " +
                     "FROM Pelicula ORDER BY Titulo";
        
        try (Connection conn = M2.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Pelicula pelicula = new Pelicula(
                    rs.getInt("ID_Pelicula"),
                    rs.getString("Titulo"),
                    rs.getString("Genero"),
                    rs.getString("ClasificacionEdad"),
                    rs.getString("Estado"),
                    rs.getString("Imagen")
                );
                peliculas.add(pelicula);
            }
            
            System.out.println("✅ Se obtuvieron " + peliculas.size() + " películas");
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener películas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return peliculas;
    }
    
    /**
     * Obtiene una película por su ID
     * @param idPelicula ID de la película
     * @return Objeto Pelicula o null si no existe
     */
    public Pelicula obtenerPorId(int idPelicula) {
        String sql = "SELECT ID_Pelicula, Genero, Titulo, ClasificacionEdad, Estado, Imagen " +
                     "FROM Pelicula WHERE ID_Pelicula = ?";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idPelicula);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Pelicula(
                        rs.getInt("ID_Pelicula"),
                        rs.getString("Titulo"),
                        rs.getString("Genero"),
                        rs.getString("ClasificacionEdad"),
                        rs.getString("Estado"),
                        rs.getString("Imagen")
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener película por ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Obtiene películas por estado
     * @param estado Estado de la película ("En Cartelera", "Próximamente", etc.)
     * @return Lista de películas con ese estado
     */
    public List<Pelicula> obtenerPorEstado(String estado) {
        List<Pelicula> peliculas = new ArrayList<>();
        String sql = "SELECT ID_Pelicula, Genero, Titulo, ClasificacionEdad, Estado, Imagen " +
                     "FROM Pelicula WHERE Estado = ? ORDER BY Titulo";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Pelicula pelicula = new Pelicula(
                        rs.getInt("ID_Pelicula"),
                        rs.getString("Titulo"),
                        rs.getString("Genero"),
                        rs.getString("ClasificacionEdad"),
                        rs.getString("Estado"),
                        rs.getString("Imagen")
                    );
                    peliculas.add(pelicula);
                }
            }
            
            System.out.println("✅ Se obtuvieron " + peliculas.size() + " películas con estado: " + estado);
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener películas por estado: " + e.getMessage());
            e.printStackTrace();
        }
        
        return peliculas;
    }
    
    /**
     * Busca películas por título (búsqueda parcial)
     * @param termino Término de búsqueda
     * @return Lista de películas que contienen el término en el título
     */
    public List<Pelicula> buscarPorTitulo(String termino) {
        List<Pelicula> peliculas = new ArrayList<>();
        String sql = "SELECT ID_Pelicula, Genero, Titulo, ClasificacionEdad, Estado, Imagen " +
                     "FROM Pelicula WHERE Titulo LIKE ? ORDER BY Titulo";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + termino + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Pelicula pelicula = new Pelicula(
                        rs.getInt("ID_Pelicula"),
                        rs.getString("Titulo"),
                        rs.getString("Genero"),
                        rs.getString("ClasificacionEdad"),
                        rs.getString("Estado"),
                        rs.getString("Imagen")
                    );
                    peliculas.add(pelicula);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar películas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return peliculas;
    }
    
    /**
     * Obtiene películas por género
     * @param genero Género de la película
     * @return Lista de películas de ese género
     */
    public List<Pelicula> obtenerPorGenero(String genero) {
        List<Pelicula> peliculas = new ArrayList<>();
        String sql = "SELECT ID_Pelicula, Genero, Titulo, ClasificacionEdad, Estado, Imagen " +
                     "FROM Pelicula WHERE Genero = ? ORDER BY Titulo";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, genero);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Pelicula pelicula = new Pelicula(
                        rs.getInt("ID_Pelicula"),
                        rs.getString("Titulo"),
                        rs.getString("Genero"),
                        rs.getString("ClasificacionEdad"),
                        rs.getString("Estado"),
                        rs.getString("Imagen")
                    );
                    peliculas.add(pelicula);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener películas por género: " + e.getMessage());
            e.printStackTrace();
        }
        
        return peliculas;
    }

    // ==========================================
    // ACTUALIZAR (UPDATE)
    // ==========================================
    
    public boolean actualizar(Pelicula pelicula) {
        String sql = "UPDATE Pelicula SET Genero = ?, Titulo = ?, ClasificacionEdad = ?, " +
                     "Estado = ?, Imagen = ? WHERE ID_Pelicula = ?";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pelicula.getGenero());
            pstmt.setString(2, pelicula.getTitulo());
            pstmt.setString(3, pelicula.getClasificacionEdad());
            pstmt.setString(4, pelicula.getEstado());
            pstmt.setString(5, pelicula.getImagen());
            pstmt.setInt(6, pelicula.getIdPelicula());
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✅ Película actualizada: " + pelicula.getTitulo());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar película: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean actualizarEstado(int idPelicula, String nuevoEstado) {
        String sql = "UPDATE Pelicula SET Estado = ? WHERE ID_Pelicula = ?";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idPelicula);
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✅ Estado actualizado a: " + nuevoEstado);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar estado: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // ==========================================
    // ELIMINAR (DELETE)
    // ==========================================
    
    public boolean eliminar(int idPelicula) {
        String sql = "DELETE FROM Pelicula WHERE ID_Pelicula = ?";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idPelicula);
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✅ Película eliminada (ID: " + idPelicula + ")");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar película: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // ==========================================
    // MÉTODO PARA SECCIONES (de la Funcionalidad 3)
    // ==========================================
    
    public List<Pelicula> obtenerPeliculasConFiltro(String genero, String estado, int limit) {
        List<Pelicula> peliculas = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT ID_Pelicula, Genero, Titulo, ClasificacionEdad, Estado, Imagen FROM Pelicula WHERE 1=1"
        );
        
        if (genero != null && !genero.trim().isEmpty()) {
            sql.append(" AND Genero = ?");
        }
        if (estado != null && !estado.trim().isEmpty()) {
            sql.append(" AND Estado = ?");
        }
        sql.append(" ORDER BY Titulo"); 
        if (limit > 0) {
            sql.append(" LIMIT ?");
        }

        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (genero != null && !genero.trim().isEmpty()) {
                pstmt.setString(paramIndex++, genero);
            }
            if (estado != null && !estado.trim().isEmpty()) {
                pstmt.setString(paramIndex++, estado);
            }
            if (limit > 0) {
                pstmt.setInt(paramIndex, limit);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Pelicula pelicula = new Pelicula(
                        rs.getInt("ID_Pelicula"),
                        rs.getString("Titulo"),
                        rs.getString("Genero"),
                        rs.getString("ClasificacionEdad"),
                        rs.getString("Estado"),
                        rs.getString("Imagen")
                    );
                    peliculas.add(pelicula);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener películas con filtro: " + e.getMessage());
            e.printStackTrace();
        }
        return peliculas;
    }

    // ==========================================
    // *** MÉTODO PARA SUGERENCIAS ***
    // ==========================================
    
    /**
     * Obtiene una lista de TÍTULOS de películas que coinciden con un término.
     * Es ligero y rápido, ideal para un popup de sugerencias.
     * @param termino Término de búsqueda (ej: "Spi")
     * @param limit Número máximo de sugerencias
     * @return Lista de Strings (títulos)
     */
    public List<String> obtenerTitulosQueCoinciden(String termino, int limit) {
        List<String> titulos = new ArrayList<>();
        String sql = "SELECT Titulo FROM Pelicula WHERE Titulo LIKE ? ORDER BY Titulo LIMIT ?";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + termino + "%");
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    titulos.add(rs.getString("Titulo"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener sugerencias de títulos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return titulos;
    }
    // ==========================================
    // *** NUEVO MÉTODO PARA "MÁS TAQUILLERAS" ***
    // ==========================================
    
    /**
     * Obtiene una lista de películas ordenadas por la cantidad total de boletos vendidos.
     * @param limit El número máximo de películas a devolver (ej: 10 para "Top 10")
     * @return Una lista de objetos Pelicula.
     */
    public List<Pelicula> obtenerPeliculasMasTaquilleras(int limit) {
        List<Pelicula> peliculas = new ArrayList<>();
        
        // Esta consulta une 4 tablas para sumar las cantidades de boletos por película
        String sql = "SELECT p.ID_Pelicula, p.Genero, p.Titulo, p.ClasificacionEdad, p.Estado, p.Imagen, SUM(cb.Cantidad) AS TotalVentas "
                   + "FROM Pelicula p "
                   + "JOIN Funcion f ON p.ID_Pelicula = f.ID_Pelicula "
                   + "JOIN Boleto b ON f.ID_Funcion = b.ID_Funcion "
                   + "JOIN Comprobante_Boleto cb ON b.ID_Boleto = cb.ID_Boleto "
                   + "GROUP BY p.ID_Pelicula, p.Genero, p.Titulo, p.ClasificacionEdad, p.Estado, p.Imagen " // Agrupar por todas las columnas seleccionadas
                   + "ORDER BY TotalVentas DESC " // Ordenar por la suma
                   + "LIMIT ?";
        
        try (Connection conn = M2.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Pelicula pelicula = new Pelicula(
                        rs.getInt("ID_Pelicula"),
                        rs.getString("Titulo"),
                        rs.getString("Genero"),
                        rs.getString("ClasificacionEdad"),
                        rs.getString("Estado"),
                        rs.getString("Imagen")
                    );
                    peliculas.add(pelicula);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener películas más taquilleras: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("✅ Se obtuvieron " + peliculas.size() + " películas taquilleras");
        return peliculas;
    }
    
    // ==========================================
    // MÉTODOS DE PRUEBA
    // ==========================================
    
    public static void main(String[] args) {
        // ... (tu main de prueba) ...
        
        PeliculaDAO dao = new PeliculaDAO();
        
        System.out.println("\n--- Prueba de Sugerencias (buscando 'man') ---");
        List<String> sugerencias = dao.obtenerTitulosQueCoinciden("man", 5);
        for (String s : sugerencias) {
            System.out.println(s);
        }
    }
}