package CINEMARX.M6;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AltaPelicula {
    
    private M6 mainFrame;
    private JPanel panel;
    
    public AltaPelicula(M6 mainFrame, JPanel panel) {
        this.mainFrame = mainFrame;
        this.panel = panel;
    }
    
    public void mostrar() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(new Color(45, 45, 45));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setMaximumSize(new Dimension(700, 300));
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(UIHelpers.createLabel("Título:"), gbc);
        gbc.gridx = 1;
        JTextField tituloField = UIHelpers.createTextField();
        formPanel.add(tituloField, gbc);

        // Género
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(UIHelpers.createLabel("Género:"), gbc);
        gbc.gridx = 1;
        JTextField generoField = UIHelpers.createTextField();
        formPanel.add(generoField, gbc);

        // Clasificación
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(UIHelpers.createLabel("Clasificación de Edad:"), gbc);
        gbc.gridx = 1;
        JTextField clasificacionField = UIHelpers.createTextField();
        formPanel.add(clasificacionField, gbc);

        // Estado
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(UIHelpers.createLabel("Estado:"), gbc);
        gbc.gridx = 1;
        JTextField estadoField = UIHelpers.createTextField();
        formPanel.add(estadoField, gbc);

        // Botón
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton guardarBtn = UIHelpers.createButton("Guardar Película");
        formPanel.add(guardarBtn, gbc);

        guardarBtn.addActionListener(e -> {
            try {
                String titulo = tituloField.getText().trim();
                String genero = generoField.getText().trim();
                String clasificacion = clasificacionField.getText().trim();
                String estado = estadoField.getText().trim();

                if (titulo.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame, "El título es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Generar nombre de imagen automáticamente
                String imagenNombre = UIHelpers.generarNombreImagen(titulo);

                String query = "INSERT INTO Pelicula (Titulo, Genero, ClasificacionEdad, Estado, Imagen) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(query)) {
                    pstmt.setString(1, titulo);
                    pstmt.setString(2, genero.isEmpty() ? null : genero);
                    pstmt.setString(3, clasificacion.isEmpty() ? null : clasificacion);
                    pstmt.setString(4, estado.isEmpty() ? null : estado);
                    pstmt.setString(5, imagenNombre);

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(mainFrame, "Película creada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                    // Limpiar campos
                    tituloField.setText("");
                    generoField.setText("");
                    clasificacionField.setText("");
                    estadoField.setText("");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Error al crear película: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        panel.add(formPanel);
    }
}