package CINEMARX.M4;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Clase principal del sistema de Cine Cinemar X
 * Gestiona la conexión a la base de datos y la navegación entre pantallas
 */
public class M4 {
    private static Connection conexion;
    private static final String URL = "jdbc:mariadb://br1.aguilucho.ar:25584/Cinemarx";
    private static final String USER = "cnx_admin";
    private static final String PASSWORD = "CnxAdmin!620";
    
    public static void main(String[] args) {
        try {
            // Configurar Look and Feel del sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Establecer conexión a la base de datos
            conectarBaseDatos();
            
            // Iniciar la aplicación
            SwingUtilities.invokeLater(() -> {
                mostrarPantallaInicio();
            });
            
        } catch (Exception e) {
            mostrarError("Error al iniciar la aplicación", e);
        }
    }
    
    /**
     * Establece la conexión con la base de datos
     */
    private static void conectarBaseDatos() {
        try {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ Conexión exitosa a la base de datos");
        } catch (SQLException e) {
            mostrarError("Error al conectar con la base de datos", e);
            System.exit(1);
        }
    }
    
    /**
     * Obtiene la conexión a la base de datos
     */
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conectarBaseDatos();
            }
        } catch (SQLException e) {
            mostrarError("Error al verificar la conexión", e);
        }
        return conexion;
    }
    
    /**
     * Muestra la pantalla de inicio/selección de película
     */
    private static void mostrarPantallaInicio() {
        // Aquí puedes crear una pantalla de inicio que liste las películas
        // Por ahora, abrimos directamente una película de ejemplo
        mostrarPantallaPelicula(1); // ID de película de ejemplo
    }
    
    /**
     * Muestra la pantalla de información de película
     * @param idPelicula ID de la película a mostrar
     */
    
    /**
     * Muestra la pantalla de información de película
     * @param idPelicula ID de la película a mostrar
     */
    public static void mostrarPantallaPelicula(int idPelicula) {
        SwingUtilities.invokeLater(() -> {
            PantallaPelicula pantalla = new PantallaPelicula(getConexion(), idPelicula);
            pantalla.setVisible(true);
        });
    }
    
    /**
     * Muestra la pantalla de selección de butacas
     * @param idFuncion ID de la función seleccionada
     * @param idSala ID de la sala
     */
    public static void mostrarPantallaButacas(int idFuncion, int idSala, int idPelicula) {
        SwingUtilities.invokeLater(() -> {
            PantallaButacas pantalla = new PantallaButacas(getConexion(), idFuncion, idSala, idPelicula);
            pantalla.setVisible(true);
        });
    }
    
    /**
     * Cierra la ventana actual
     * @param frame Ventana a cerrar
     */
    public static void cerrarVentana(JFrame frame) {
        if (frame != null) {
            frame.dispose();
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private static void mostrarError(String mensaje, Exception e) {
        System.err.println("ERROR: " + mensaje);
        e.printStackTrace();
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                mensaje + "\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        });
    }
    
    /**
     * Cierra la conexión a la base de datos
     */
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("✓ Conexión cerrada");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Hook para cerrar la conexión al salir
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cerrarConexion();
        }));
    }
}