package CINEMARX.M6;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.*;
import javax.imageio.ImageIO;

public class PersonalDialogs {
    
    private M6 mainFrame;
    
    public PersonalDialogs(M6 mainFrame) {
        this.mainFrame = mainFrame;
    }
    
    public void abrirVentanaAltaPersonal(DefaultTableModel tableModel) {
        // Crear ventana emergente
        JDialog dialog = new JDialog(mainFrame, "Alta de Personal", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(M6.BACKGROUND_COLOR);

        // Panel superior con imagen TOPBAR
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(M6.TOPBAR_COLOR);
        topPanel.setPreferredSize(new Dimension(500, 80));

        try {
            URL imgURL = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=logos%2FCINEMARX%20logotipo.png");
            BufferedImage image = ImageIO.read(imgURL);

            if (image != null) {
                Image scaledImg = image.getScaledInstance(500, 80, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                topPanel.add(imageLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.add(topPanel, BorderLayout.NORTH);

        // Panel central con formulario
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(M6.BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // DNI
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(UIHelpers.createLabel("DNI:"), gbc);
        gbc.gridx = 1;
        JTextField dniField = UIHelpers.createTextField();
        formPanel.add(dniField, gbc);

        // Nombre
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(UIHelpers.createLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        JTextField nombreField = UIHelpers.createTextField();
        formPanel.add(nombreField, gbc);

        // Apellido
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(UIHelpers.createLabel("Apellido:"), gbc);
        gbc.gridx = 1;
        JTextField apellidoField = UIHelpers.createTextField();
        formPanel.add(apellidoField, gbc);

        // Fecha de Nacimiento
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(UIHelpers.createLabel("Fecha Nac. (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        JTextField fechaNacField = UIHelpers.createTextField();
        formPanel.add(fechaNacField, gbc);

        // Rol - Botones de selección exclusiva
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(UIHelpers.createLabel("Rol:"), gbc);

        gbc.gridx = 1;
        JPanel rolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolPanel.setBackground(M6.BACKGROUND_COLOR);

        ButtonGroup rolGroup = new ButtonGroup();
        JRadioButton empleadoRadio = new JRadioButton("Empleado");
        JRadioButton adminRadio = new JRadioButton("Administrador");

        empleadoRadio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        empleadoRadio.setForeground(M6.TEXT_COLOR);
        empleadoRadio.setBackground(M6.BACKGROUND_COLOR);
        empleadoRadio.setSelected(true);

        adminRadio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        adminRadio.setForeground(M6.TEXT_COLOR);
        adminRadio.setBackground(M6.BACKGROUND_COLOR);

        rolGroup.add(empleadoRadio);
        rolGroup.add(adminRadio);
        rolPanel.add(empleadoRadio);
        rolPanel.add(adminRadio);

        formPanel.add(rolPanel, gbc);

        // Panel adicional para administrador (ID_Cine)
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel cineLabel = UIHelpers.createLabel("Cine:");
        cineLabel.setVisible(false);
        formPanel.add(cineLabel, gbc);

        gbc.gridx = 1;
        JComboBox<CineItem> cineCombo = new JComboBox<>();
        UIHelpers.styleComboBox(cineCombo);
        cineCombo.setVisible(false);
        DatabaseHelper.cargarCines(mainFrame, cineCombo);
        formPanel.add(cineCombo, gbc);

        // Listener para mostrar/ocultar campo de cine
        adminRadio.addActionListener(e -> {
            cineLabel.setVisible(true);
            cineCombo.setVisible(true);
            dialog.revalidate();
        });

        empleadoRadio.addActionListener(e -> {
            cineLabel.setVisible(false);
            cineCombo.setVisible(false);
            dialog.revalidate();
        });

        dialog.add(formPanel, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(M6.BACKGROUND_COLOR);

        JButton guardarBtn = UIHelpers.createButton("Guardar");
        JButton cancelarBtn = UIHelpers.createButton("Cancelar");
        cancelarBtn.setBackground(new Color(100, 100, 100));

        buttonPanel.add(guardarBtn);
        buttonPanel.add(cancelarBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Listener del botón Guardar
        guardarBtn.addActionListener(e -> {
            try {
                String dniStr = dniField.getText().trim();
                String nombre = nombreField.getText().trim();
                String apellido = apellidoField.getText().trim();
                String fechaNac = fechaNacField.getText().trim();

                if (dniStr.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || fechaNac.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Complete todos los campos", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int dni = Integer.parseInt(dniStr);
                boolean esAdmin = adminRadio.isSelected();

                if (esAdmin && cineCombo.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(dialog, "Seleccione un cine para el administrador", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Insertar usuario primero
                String queryUsuario = "INSERT INTO Usuario (DNI, FechaNac, Nombre, Apellido) VALUES (?, ?, ?, ?)";

                try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(queryUsuario)) {
                    pstmt.setInt(1, dni);
                    pstmt.setString(2, fechaNac);
                    pstmt.setString(3, nombre);
                    pstmt.setString(4, apellido);
                    pstmt.executeUpdate();
                }

                // Insertar empleado o administrador
                if (esAdmin) {
                    CineItem cine = (CineItem) cineCombo.getSelectedItem();
                    String queryAdmin = "INSERT INTO Administrador (DNI, ID_Cine) VALUES (?, ?)";
                    try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(queryAdmin)) {
                        pstmt.setInt(1, dni);
                        pstmt.setInt(2, cine.getId());
                        pstmt.executeUpdate();
                    }
                } else {
                    String queryEmp = "INSERT INTO Empleado (DNI) VALUES (?)";
                    try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(queryEmp)) {
                        pstmt.setInt(1, dni);
                        pstmt.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(dialog, 
                    (esAdmin ? "Administrador" : "Empleado") + " creado exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

                new Personal(mainFrame, null).cargarPersonal(tableModel);
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "El DNI debe ser un número válido", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error al crear registro: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
// CONTINUACIÓN DE PersonalDialogs.java - Agregar este método a la clase

    public void abrirVentanaModificacionPersonal(JTable table, DefaultTableModel tableModel, int selectedRow) {
        // Obtener datos de la fila seleccionada
        int dni = (int) tableModel.getValueAt(selectedRow, 0);
        String nombreActual = (String) tableModel.getValueAt(selectedRow, 1);
        String apellidoActual = (String) tableModel.getValueAt(selectedRow, 2);
        String rolActual = (String) tableModel.getValueAt(selectedRow, 3);

        // Crear ventana emergente
        JDialog dialog = new JDialog(mainFrame, "Modificación de Personal", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(M6.BACKGROUND_COLOR);

        // Panel superior con imagen TOPBAR
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(M6.TOPBAR_COLOR);
        topPanel.setPreferredSize(new Dimension(700, 80));

        try {
            URL imgURL = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=logos%2FCINEMARX%20logotipo.png");
            BufferedImage image = ImageIO.read(imgURL);

            if (image != null) {
                Image scaledImg = image.getScaledInstance(500, 80, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                topPanel.add(imageLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.add(topPanel, BorderLayout.NORTH);

        // Panel central con formulario
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(M6.BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mostrar rol (no editable)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(UIHelpers.createLabel("Rol:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel rolLabel = UIHelpers.createLabel(rolActual);
        rolLabel.setForeground(M6.ACCENT_COLOR);
        formPanel.add(rolLabel, gbc);

        // DNI (EDITABLE)
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(UIHelpers.createLabel("DNI:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField dniField = UIHelpers.createTextFieldWide();
        dniField.setText(String.valueOf(dni));
        formPanel.add(dniField, gbc);

        // Nombre
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(UIHelpers.createLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField nombreField = UIHelpers.createTextFieldWide();
        nombreField.setText(nombreActual);
        formPanel.add(nombreField, gbc);

        // Apellido
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.3;
        formPanel.add(UIHelpers.createLabel("Apellido:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField apellidoField = UIHelpers.createTextFieldWide();
        apellidoField.setText(apellidoActual);
        formPanel.add(apellidoField, gbc);

        // Fecha de Nacimiento
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0.3;
        formPanel.add(UIHelpers.createLabel("Fecha Nac. (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField fechaNacField = UIHelpers.createTextFieldWide();
        formPanel.add(fechaNacField, gbc);

        // Cargar fecha de nacimiento actual
        try {
            String queryFecha = "SELECT FechaNac FROM Usuario WHERE DNI = ?";
            try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(queryFecha)) {
                pstmt.setInt(1, dni);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    fechaNacField.setText(rs.getString("FechaNac"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(M6.BACKGROUND_COLOR);

        JButton actualizarBtn = UIHelpers.createButton("Actualizar");
        JButton cancelarBtn = UIHelpers.createButton("Cancelar");
        cancelarBtn.setBackground(new Color(100, 100, 100));

        buttonPanel.add(actualizarBtn);
        buttonPanel.add(cancelarBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Listener del botón Actualizar
        actualizarBtn.addActionListener(e -> {
            try {
                String nuevoDniStr = dniField.getText().trim();
                String nuevoNombre = nombreField.getText().trim();
                String nuevoApellido = apellidoField.getText().trim();
                String nuevaFechaNac = fechaNacField.getText().trim();

                if (nuevoDniStr.isEmpty() || nuevoNombre.isEmpty() || nuevoApellido.isEmpty() || nuevaFechaNac.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Complete todos los campos", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int nuevoDni = Integer.parseInt(nuevoDniStr);

                // Si el DNI cambió, verificar que no exista
                if (nuevoDni != dni) {
                    String queryCheck = "SELECT COUNT(*) as count FROM Usuario WHERE DNI = ?";
                    try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(queryCheck)) {
                        pstmt.setInt(1, nuevoDni);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next() && rs.getInt("count") > 0) {
                            JOptionPane.showMessageDialog(dialog, "El DNI " + nuevoDni + " ya existe", 
                                "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Actualizar DNI en tabla específica primero
                    if (rolActual.equals("Administrador")) {
                        String queryUpdateAdmin = "UPDATE Administrador SET DNI = ? WHERE DNI = ?";
                        try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(queryUpdateAdmin)) {
                            pstmt.setInt(1, nuevoDni);
                            pstmt.setInt(2, dni);
                            pstmt.executeUpdate();
                        }
                    } else {
                        String queryUpdateEmp = "UPDATE Empleado SET DNI = ? WHERE DNI = ?";
                        try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(queryUpdateEmp)) {
                            pstmt.setInt(1, nuevoDni);
                            pstmt.setInt(2, dni);
                            pstmt.executeUpdate();
                        }
                    }
                }

                // Actualizar usuario
                String queryUpdate = "UPDATE Usuario SET DNI = ?, Nombre = ?, Apellido = ?, FechaNac = ? WHERE DNI = ?";

                try (PreparedStatement pstmt = mainFrame.getConnection().prepareStatement(queryUpdate)) {
                    pstmt.setInt(1, nuevoDni);
                    pstmt.setString(2, nuevoNombre);
                    pstmt.setString(3, nuevoApellido);
                    pstmt.setString(4, nuevaFechaNac);
                    pstmt.setInt(5, dni);
                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(dialog, "Datos actualizados exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

                new Personal(mainFrame, null).cargarPersonal(tableModel);
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "El DNI debe ser un número válido", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelarBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}

// FIN DE LA CLASE PersonalDialogs.java