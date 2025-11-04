package CINEMARX.M6;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ModificacionPelicula {
    
    private M6 mainFrame;
    private JPanel panel;
    
    public ModificacionPelicula(M6 mainFrame, JPanel panel) {
        this.mainFrame = mainFrame;
        this.panel = panel;
    }
    
    public void mostrar() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(new Color(45, 45, 45));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setMaximumSize(new Dimension(700, 450));
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoLabel = UIHelpers.createLabel("Seleccione la película a modificar:");
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(infoLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JComboBox<PeliculaItem> peliculaCombo = new JComboBox<>();
        UIHelpers.styleComboBox(peliculaCombo);
        peliculaCombo.setMaximumSize(new Dimension(650, 35));
        peliculaCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        DatabaseHelper.cargarPeliculas(mainFrame, peliculaCombo);
        formPanel.add(peliculaCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Panel de formulario con campos
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(new Color(45, 45, 45));
        fieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(UIHelpers.createLabel("Título:"), gbc);
        gbc.gridx = 1;
        JTextField tituloField = UIHelpers.createTextField();
        fieldsPanel.add(tituloField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        fieldsPanel.add(UIHelpers.createLabel("Género:"), gbc);
        gbc.gridx = 1;
        JTextField generoField = UIHelpers.createTextField();
        fieldsPanel.add(generoField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        fieldsPanel.add(UIHelpers.createLabel("Clasificación:"), gbc);
        gbc.gridx = 1;
        JTextField clasificacionField = UIHelpers.createTextField();
        fieldsPanel.add(clasificacionField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        fieldsPanel.add(UIHelpers.createLabel("Estado:"), gbc);
        gbc.gridx = 1;
        JTextField estadoField = UIHelpers.createTextField();
        fieldsPanel.add(estadoField, gbc);

        formPanel.add(fieldsPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton actualizarBtn = UIHelpers.createButton("Actualizar Película");
        actualizarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(actualizarBtn);

        // Listener para cargar datos al seleccionar película
        peliculaCombo.addActionListener(e -> {
            PeliculaItem pelicula = (PeliculaItem) peliculaCombo.getSelectedItem();
            if (pelicula != null) {
                cargarDatosPelicula(pelicula.getId(), tituloField, generoField, clasificacionField, estadoField);
            }
        });

        // Cargar datos iniciales
        if (peliculaCombo.getItemCount() > 0) {
            PeliculaItem firstPelicula = peliculaCombo.getItemAt(0);
            cargarDatosPelicula(firstPelicula.getId(), tituloField, generoField, clasificacionField, estadoField);
        }

        actualizarBtn.addActionListener(e -> {
            PeliculaItem pelicula = (PeliculaItem) peliculaCombo.getSelectedItem();
            if (pelicula == null) {
                JOptionPane.showMessageDialog(mainFrame, "Seleccione una película", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String nuevoTitulo = tituloField.getText().trim();

                if (nuevoTitulo.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame, "El título es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Generar nuevo nombre de imagen basado en el nuevo título
                String nuevaImagen = UIHelpers.generarNombreImagen(nuevoTitulo);

                String query = "UPDATE Pelicula SET Titulo = ?, Genero = ?, ClasificacionEdad = ?, Estado = ?, Imagen = ? WHERE ID_Pelicula = ?";

                try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(query)) {
                    pstmt.setString(1, nuevoTitulo);
                    pstmt.setString(2, generoField.getText().trim());
                    pstmt.setString(3, clasificacionField.getText().trim());
                    pstmt.setString(4, estadoField.getText().trim());
                    pstmt.setString(5, nuevaImagen);
                    pstmt.setInt(6, pelicula.getId());

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(mainFrame, "Película actualizada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                    // Recargar ComboBox
                    peliculaCombo.removeAllItems();
                    DatabaseHelper.cargarPeliculas(mainFrame, peliculaCombo);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Error al actualizar película: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        panel.add(formPanel);
    }
    
    private void cargarDatosPelicula(int idPelicula, JTextField tituloField, JTextField generoField, 
                                     JTextField clasificacionField, JTextField estadoField) {
        try {
            String query = "SELECT Titulo, Genero, ClasificacionEdad, Estado FROM Pelicula WHERE ID_Pelicula = ?";

            try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(query)) {
                pstmt.setInt(1, idPelicula);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    tituloField.setText(rs.getString("Titulo"));
                    generoField.setText(rs.getString("Genero") != null ? rs.getString("Genero") : "");
                    clasificacionField.setText(rs.getString("ClasificacionEdad") != null ? rs.getString("ClasificacionEdad") : "");
                    estadoField.setText(rs.getString("Estado") != null ? rs.getString("Estado") : "");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error al cargar datos de película: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}