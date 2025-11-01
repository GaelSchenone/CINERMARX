/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package CINEMARX.M4;

import javax.swing.*;
import java.sql.SQLException;

/**
 *
 * @author gaels
 */
public class M4 {
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Configurar Look and Feel del sistema
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Crear conexión a base de datos
                // IMPORTANTE: Ajusta estos valores según tu configuración
                String url = "jdbc:mariadb://br1.aguilucho.ar:25584/mod4_reservas_boletos";
                String user = "mod4_reservas_boletos";
                String password = "Cnx!M4";
                
                DatabaseService dbService = new DatabaseService(url, user, password);
                
                // Crear una película de ejemplo para probar (reemplaza con datos reales)
                Pelicula peliculaEjemplo = new Pelicula(
                    1,
                    "American Psycho",
                    "",
                    "+17",
                    "2D",
                    "1h 42m",
                    "24 Agosto, 2000",
                    "Patrick Bateman, un rico ejecutivo de banca de inversión de Nueva York, esconde su ego psicopático alternativo de sus compañeros de trabajo y amigos al tiempo que profundiza en sus violentas fantasías hedonistas."
                );
                
                // Crear ventana principal con resolución 1366x768 (16:9)
                JFrame frame = new JFrame("CineMarX");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1366, 768);
                
                frame.setLocationRelativeTo(null); // Centrar en pantalla
                frame.setResizable(false); // No permitir redimensionar
                
                // Agregar pantalla inicial (Detalle de Película)
                frame.add(new PantallaDetallePelicula(dbService, peliculaEjemplo));
                
                // Hacer visible la ventana
                frame.setVisible(true);
                
                // Cerrar conexión a base de datos al cerrar la aplicación
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        try {
                            dbService.close();
                            System.out.println("Conexión a base de datos cerrada correctamente");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error al iniciar la aplicación:\n" + e.getMessage(),
                    "Error Fatal",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}