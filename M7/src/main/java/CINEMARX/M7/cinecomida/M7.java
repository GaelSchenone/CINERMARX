package CINEMARX.M7;

import javax.swing.*;

public class M7 extends JFrame {

    public M7() {
        initComponents();
    }

    private void initComponents() {
        setTitle("CinemarX");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    private void abrirModuloBuffet() {
        BuffetFrame buffetFrame = new BuffetFrame();
        buffetFrame.setVisible(true);
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        // Probar conexión al iniciar
        Connection testCon = ConexionBD.getConexion();
        if (testCon != null) {
            System.out.println("✅ Aplicación iniciada correctamente");
        } else {
            System.err.println("❌ No se pudo conectar a la base de datos");
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar a la base de datos.\nVerifique la configuración de conexión.",
                "Error de Conexión",
                JOptionPane.ERROR_MESSAGE);
        }

        // Iniciar aplicación
        java.awt.EventQueue.invokeLater(() -> {
            // Puedes pasar el ID del cliente si lo tienes
            // BuffetFrame buffet = new BuffetFrame(idCliente);
            BuffetFrame buffet = new BuffetFrame();
            buffet.setVisible(true);
        });
    }
}