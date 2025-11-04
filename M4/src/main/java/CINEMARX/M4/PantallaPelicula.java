package CINEMARX.M4;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.InputStream;

public class PantallaPelicula extends JFrame {
    private Connection conn;
    private int idPelicula;
    private JPanel panelFechas;
    private JPanel panelHorarios;
    private JComboBox<String> cbIdioma;
    private JComboBox<String> cbFormato;
    private ButtonGroup grupoBotones;
    private Map<JButton, FuncionInfo> mapaFunciones;
    private JLabel titulo;
    private JTextArea descripcionArea;
    private JLabel duracionLabel;
    private JLabel posterLabel;

    // Filter state
    private java.util.Date selectedDate;
    private String selectedIdioma;
    private String selectedFormato;
    private JPanel panelTarjetasPeliculas;
    
    class FuncionInfo {
        int idFuncion;
        int idSala;
        String hora;
        
        FuncionInfo(int idFuncion, int idSala, String hora) {
            this.idFuncion = idFuncion;
            this.idSala = idSala;
            this.hora = hora;
        }
    }

    class PeliculaCard extends JPanel {
        public PeliculaCard(int peliculaId, String titulo, String urlImagen, String clasificacionEdad) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(150, 270));

            JLabel posterLabel = new JLabel();
            posterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            posterLabel.setPreferredSize(new Dimension(130, 195));

            try {
                URL imageUrl = new URL(urlImagen);
                InputStream in = imageUrl.openStream();
                BufferedImage originalImage = ImageIO.read(in);
                in.close();
                if (originalImage != null) {
                    Image scaledImage = originalImage.getScaledInstance(130, 195, Image.SCALE_SMOOTH);
                    posterLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    posterLabel.setText("No Poster");
                }
            } catch (Exception e) {
                posterLabel.setText("No Poster");
                e.printStackTrace();
            }

            JLabel tituloLabel = new JLabel("<html><body style='width: 120px;'>" + titulo + "</body></html>");
            tituloLabel.setForeground(Color.WHITE);
            tituloLabel.setFont(new Font("Arial", Font.BOLD, 12));
            tituloLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel clasificacionLabel = new JLabel(clasificacionEdad);
            clasificacionLabel.setForeground(Color.LIGHT_GRAY);
            clasificacionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            clasificacionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(posterLabel);
            add(Box.createRigidArea(new Dimension(0, 10)));
            add(tituloLabel);
            add(Box.createRigidArea(new Dimension(0, 5)));
            add(clasificacionLabel);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    M4.mostrarPantallaPelicula(peliculaId);
                    dispose();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(40, 40, 40));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();
        }
    }
    
    public PantallaPelicula(Connection conn, int idPelicula) {
        this.conn = conn;
        this.idPelicula = idPelicula;
        this.mapaFunciones = new HashMap<>();
        
        setTitle("CINEMAR X - Selección de Función");
        setSize(1000, 850); // Increased height for the new section
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        inicializarUI();
        cargarDatosPelicula();
    }
    
    private void inicializarUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 30));
        
        // Header
        JPanel header = crearHeader();
        mainPanel.add(header, BorderLayout.NORTH);
        
        // Main scrollable content container
        JPanel mainScrollableContent = new JPanel();
        mainScrollableContent.setLayout(new BoxLayout(mainScrollableContent, BoxLayout.Y_AXIS));
        mainScrollableContent.setBackground(new Color(30, 30, 30));
        mainScrollableContent.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Two-column section (poster/synopsis + functions/filters)
        JPanel twoColumnPanel = new JPanel(new BorderLayout(20, 20));
        twoColumnPanel.setBackground(new Color(30, 30, 30));

        // --- Left Column --- 
        JPanel leftPanel = crearPanelIzquierdo();
        JScrollPane leftScrollPane = new JScrollPane(leftPanel);
        leftScrollPane.setBorder(null);
        leftScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        twoColumnPanel.add(leftScrollPane, BorderLayout.WEST);

        // --- Right Column --- 
        JPanel rightPanel = crearPanelDerecho();
        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setBorder(null);
        rightScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        twoColumnPanel.add(rightScrollPane, BorderLayout.CENTER);

        mainScrollableContent.add(twoColumnPanel);
        mainScrollableContent.add(Box.createRigidArea(new Dimension(0, 30))); // Space before separator

        // Separator
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(70, 70, 70));
        separator.setBackground(new Color(50, 50, 50));
        mainScrollableContent.add(separator);
        mainScrollableContent.add(Box.createRigidArea(new Dimension(0, 30))); // Space after separator

        // New invisible container with two red transparent halves
        JPanel transparentContainer = new JPanel(new GridLayout(1, 2));
        transparentContainer.setOpaque(false); // Make the container itself invisible
        transparentContainer.setPreferredSize(new Dimension(0, 100)); // Give it some height
        transparentContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel redPanel1 = new JPanel();
        redPanel1.setBackground(new Color(255, 0, 0, 100)); // Transparent red
        redPanel1.setOpaque(true); // Ensure background is painted
        transparentContainer.add(redPanel1);

        JPanel redPanel2 = new JPanel();
        redPanel2.setBackground(new Color(255, 0, 0, 100)); // Transparent red
        redPanel2.setOpaque(true); // Ensure background is painted
        transparentContainer.add(redPanel2);

        mainScrollableContent.add(transparentContainer);
        mainScrollableContent.add(Box.createRigidArea(new Dimension(0, 30))); // Space after transparent container

        // New separator below the transparent container
        JSeparator separator2 = new JSeparator(SwingConstants.HORIZONTAL);
        separator2.setForeground(new Color(70, 70, 70));
        separator2.setBackground(new Color(50, 50, 50));
        mainScrollableContent.add(separator2);
        mainScrollableContent.add(Box.createRigidArea(new Dimension(0, 30))); // Space after separator

        // "Otras Peliculas" Section
        JPanel otrasPeliculasSection = crearPanelOtrasPeliculas();
        mainScrollableContent.add(otrasPeliculasSection);

        // Wrap the entire mainScrollableContent in a JScrollPane
        JScrollPane masterScrollPane = new JScrollPane(mainScrollableContent);
        masterScrollPane.setBorder(null);
        masterScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        masterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(masterScrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel crearPanelOtrasPeliculas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30)); // General background color
        panel.setBorder(new EmptyBorder(15, 0, 15, 0)); // Remove left/right padding here, let mainScrollableContent handle it

        JLabel titulo = new JLabel("OTRAS PEL\u00CDCULAS EN CARTELERA");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setForeground(Color.WHITE);
        titulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT); // Ensure title is left-aligned
        panel.add(titulo, BorderLayout.NORTH);

        panelTarjetasPeliculas = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panelTarjetasPeliculas.setBackground(new Color(30, 30, 30)); // General background color

        JScrollPane scrollPane = new JScrollPane(panelTarjetasPeliculas);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(40, 40, 40));
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));

        // Logo with correct aspect ratio
        try {
            URL imageUrl = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=logos%2FCINEMARX%20logotipo.png");
            InputStream in = imageUrl.openStream();
            BufferedImage originalImage = ImageIO.read(in);
            in.close();
            
            if (originalImage != null) {
                int newHeight = 40; // Set a fixed height
                int newWidth = (originalImage.getWidth() * newHeight) / originalImage.getHeight(); // Calculate width to maintain aspect ratio (fixed height)

                Image logo = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(logo));
                header.add(logoLabel, BorderLayout.WEST);
            } else {
                throw new Exception("Logo image could not be read.");
            }
        } catch (Exception e) {
            JLabel logoLabel = new JLabel("CINEMAR X");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 32));
            logoLabel.setForeground(Color.WHITE);
            header.add(logoLabel, BorderLayout.WEST);
        }
        
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 0));
        menu.setBackground(new Color(40, 40, 40));
        
        String[] items = {"PELICULAS", "BUFFET", "MEMBRESIA"};
        for (String item : items) {
            JLabel label = new JLabel(item);
            label.setFont(new Font("Arial", Font.PLAIN, 16));
            label.setForeground(Color.WHITE);
            label.setCursor(new Cursor(Cursor.HAND_CURSOR));
            menu.add(label);
        }
        
        header.add(menu, BorderLayout.EAST);
        return header;
    }
    
    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 30));
        panel.setPreferredSize(new Dimension(300, 0));
        
        // Espacio para poster (300x450)
        posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(300, 450));
        posterLabel.setMaximumSize(new Dimension(300, 450));
        posterLabel.setBackground(new Color(50, 50, 50));
        posterLabel.setOpaque(true);
        posterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(posterLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Descripción
        descripcionArea = new JTextArea();
        descripcionArea.setLineWrap(true);
        descripcionArea.setWrapStyleWord(true);
        descripcionArea.setEditable(false);
        descripcionArea.setBackground(new Color(30, 30, 30));
        descripcionArea.setForeground(Color.LIGHT_GRAY);
        descripcionArea.setFont(new Font("Arial", Font.PLAIN, 13));
        descripcionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        descripcionArea.setPreferredSize(new Dimension(280, 100)); // Give it a size hint
        panel.add(descripcionArea);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Panel para la duración
        JPanel duracionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        duracionPanel.setBackground(new Color(30, 30, 30));
        duracionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel duracionTitulo = new JLabel("Duración: ");
        duracionTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        duracionTitulo.setForeground(Color.WHITE);
        duracionPanel.add(duracionTitulo);

        duracionLabel = new JLabel();
        duracionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        duracionLabel.setForeground(Color.LIGHT_GRAY);
        duracionPanel.add(duracionLabel);

        panel.add(duracionPanel);
        
        return panel;
    }
    
    private JPanel crearPanelDerecho() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 30));
        
        // Título película
        titulo = new JLabel("CARGANDO...");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titulo);
        
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Fechas
        JLabel labelFechas = new JLabel("Fechas:");
        labelFechas.setFont(new Font("Arial", Font.BOLD, 18));
        labelFechas.setForeground(Color.WHITE);
        labelFechas.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(labelFechas);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panelFechas = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFechas.setBackground(new Color(30, 30, 30));
        panelFechas.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(panelFechas);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Idioma y Formato
        JPanel filtrosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0)); // Main horizontal panel for filters
        filtrosPanel.setBackground(new Color(30, 30, 30));
        filtrosPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbIdioma = crearComboBox("Idioma");
        cbFormato = crearComboBox("Formato");

        filtrosPanel.add(cbIdioma);
        filtrosPanel.add(Box.createRigidArea(new Dimension(20, 0))); // Space between filter panels
        filtrosPanel.add(cbFormato);
        panel.add(filtrosPanel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        cbIdioma.addActionListener(e -> {
            selectedIdioma = (String) cbIdioma.getSelectedItem();
            actualizarHorarios();
        });

        cbFormato.addActionListener(e -> {
            selectedFormato = (String) cbFormato.getSelectedItem();
            actualizarHorarios();
        });
        
        filtrosPanel.add(cbIdioma);
        filtrosPanel.add(cbFormato);
        panel.add(filtrosPanel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Horarios
        JLabel labelHorarios = new JLabel("Horarios:");
        labelHorarios.setFont(new Font("Arial", Font.BOLD, 18));
        labelHorarios.setForeground(Color.WHITE);
        labelHorarios.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(labelHorarios);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panelHorarios = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panelHorarios.setBackground(new Color(30, 30, 30));
        panelHorarios.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(panelHorarios);
        
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Botón comprar
        JButton btnComprar = new JButton("COMPRAR ENTRADAS");
        btnComprar.setFont(new Font("Arial", Font.BOLD, 16));
        btnComprar.setForeground(Color.WHITE);
        btnComprar.setBackground(new Color(220, 50, 50));
        btnComprar.setPreferredSize(new Dimension(400, 50));
        btnComprar.setMaximumSize(new Dimension(400, 50));
        btnComprar.setFocusPainted(false);
        btnComprar.setBorderPainted(false);
        btnComprar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnComprar.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnComprar.addActionListener(e -> {
            FuncionInfo seleccionada = obtenerFuncionSeleccionada();
            if (seleccionada != null) {
                abrirPantallaButacas(seleccionada);
            } else {
                JOptionPane.showMessageDialog(this, "Por favor seleccione una función", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        panel.add(btnComprar);
        
        return panel;
    }
    
    private JComboBox<String> crearComboBox(String nombre) {
        JComboBox<String> cb = new JComboBox<String>() {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(80, 80, 80)); // Border color
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        cb.setOpaque(false);
        cb.setPreferredSize(new Dimension(200, 40));
        cb.setBackground(new Color(50, 50, 50));
        cb.setForeground(Color.WHITE); // Set text to white
        cb.setFont(new Font("Arial", Font.PLAIN, 14));

        // Style the arrow button
        for (Component comp : cb.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).setBackground(new Color(50, 50, 50));
                ((JButton) comp).setBorderPainted(false);
                ((JButton) comp).setContentAreaFilled(false);
            }
        }

        cb.addItem(nombre);
        return cb;
    }
    
    private void cargarDatosPelicula() {
        try {
            // Cargar datos de la película
            String sql = "SELECT Titulo, Sinopsis, Duracion, Imagen FROM Pelicula WHERE ID_Pelicula = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idPelicula);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                titulo.setText(rs.getString("Titulo").toUpperCase());
                descripcionArea.setText(rs.getString("Sinopsis"));
                
                int duracionMinutos = rs.getInt("Duracion");
                int horas = duracionMinutos / 60;
                int minutos = duracionMinutos % 60;
                duracionLabel.setText(String.format("%dh %dm", horas, minutos));

                // Load poster image
                String imageUrlString = rs.getString("Imagen");
                System.out.println("Intentando cargar poster desde URL: " + imageUrlString); // Log the URL

                if (imageUrlString != null && !imageUrlString.isEmpty()) {
                    try {
                        URL imageUrl = new URL(imageUrlString);
                        InputStream in = imageUrl.openStream();
                        BufferedImage originalImage = ImageIO.read(in);
                        in.close();
                        
                        if (originalImage != null) {
                            // Scale image to fit 300 width, maintaining aspect ratio
                            int newWidth = 300;
                            int newHeight = (originalImage.getHeight() * newWidth) / originalImage.getWidth();
                            
                            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                            posterLabel.setIcon(new ImageIcon(scaledImage));
                        } else {
                            System.err.println("Error: ImageIO.read devolvió null para la URL: " + imageUrlString);
                            posterLabel.setText("Error: no se pudo leer la imagen del póster.");
                        }
                    } catch (Exception e) {
                        System.err.println("Excepción al cargar la imagen del póster desde la URL: " + imageUrlString);
                        e.printStackTrace();
                        posterLabel.setText("Error al cargar poster");
                    }
                } else {
                    System.out.println("No se encontró URL de imagen en la base de datos para esta película.");
                    posterLabel.setText("Póster no disponible.");
                }
            }
            
            cargarFunciones();
            cargarOtrasPeliculas();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage());
        }
    }
    
    private void cargarFunciones() {
        try {
            String sql = "SELECT DISTINCT FechaFuncion FROM Funcion " +
                        "WHERE ID_Pelicula = ? ORDER BY FechaFuncion";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idPelicula);
            ResultSet rs = ps.executeQuery();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
            grupoBotones = new ButtonGroup();
            
            while (rs.next()) {
                java.util.Date fecha = rs.getDate("FechaFuncion");
                JButton btnFecha = crearBotonFecha(sdf.format(fecha));
                btnFecha.addActionListener(e -> {
                    selectedDate = fecha;
                    actualizarFiltros(fecha);
                    actualizarHorarios();
                });
                panelFechas.add(btnFecha);
                grupoBotones.add(btnFecha);
            }

            // Auto-select the first date
            if (panelFechas.getComponentCount() > 0) {
                Component firstButton = panelFechas.getComponent(0);
                if (firstButton instanceof JButton) {
                    ((JButton) firstButton).doClick();
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void actualizarHorarios() {
        panelHorarios.removeAll();
        mapaFunciones.clear();

        try {
            StringBuilder sqlBuilder = new StringBuilder(
                "SELECT DISTINCT f.ID_Funcion, f.ID_Sala, f.HoraFuncion FROM Funcion f JOIN Sala s ON f.ID_Sala = s.ID_Sala WHERE f.ID_Pelicula = ?");
            
            java.util.List<Object> params = new ArrayList<>();
            params.add(idPelicula);

            if (selectedDate != null) {
                sqlBuilder.append(" AND f.FechaFuncion = ?");
                params.add(new java.sql.Date(selectedDate.getTime()));
            }
            if (selectedIdioma != null && !"Todos".equals(selectedIdioma)) {
                sqlBuilder.append(" AND f.Idioma = ?");
                params.add(selectedIdioma);
            }
            if (selectedFormato != null && !"Todos".equals(selectedFormato)) {
                sqlBuilder.append(" AND s.TipoDeSala = ?");
                params.add(selectedFormato);
            }

            sqlBuilder.append(" ORDER BY f.HoraFuncion");

            PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String hora = rs.getString("HoraFuncion");
                JButton btnHora = crearBotonHorario(hora);
                
                FuncionInfo info = new FuncionInfo(
                    rs.getInt("ID_Funcion"),
                    rs.getInt("ID_Sala"),
                    hora
                );
                mapaFunciones.put(btnHora, info);
                
                panelHorarios.add(btnHora);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        panelHorarios.revalidate();
        panelHorarios.repaint();

        // Auto-select the first time slot if available
        if (panelHorarios.getComponentCount() > 0) {
            Component firstButton = panelHorarios.getComponent(0);
            if (firstButton instanceof JButton) {
                ((JButton) firstButton).doClick();
            }
        }
    }

    private void actualizarFiltros(java.util.Date fecha) {
        // Temporarily remove listeners to prevent them from firing during update
        ActionListener idiomaListener = cbIdioma.getActionListeners().length > 0 ? cbIdioma.getActionListeners()[0] : null;
        ActionListener formatoListener = cbFormato.getActionListeners().length > 0 ? cbFormato.getActionListeners()[0] : null;
        if (idiomaListener != null) cbIdioma.removeActionListener(idiomaListener);
        if (formatoListener != null) cbFormato.removeActionListener(formatoListener);

        cbIdioma.removeAllItems();
        cbFormato.removeAllItems();

        try {
            // Load languages for the selected date
            String sqlLang = "SELECT DISTINCT Idioma FROM Funcion WHERE ID_Pelicula = ? AND FechaFuncion = ?";
            PreparedStatement psLang = conn.prepareStatement(sqlLang);
            psLang.setInt(1, idPelicula);
            psLang.setDate(2, new java.sql.Date(fecha.getTime()));
            ResultSet rsLang = psLang.executeQuery();
            while (rsLang.next()) {
                cbIdioma.addItem(rsLang.getString("Idioma"));
            }

            // Load formats for the selected date
            String sqlFormat = "SELECT DISTINCT s.TipoDeSala FROM Sala s INNER JOIN Funcion f ON s.ID_Sala = f.ID_Sala WHERE f.ID_Pelicula = ? AND f.FechaFuncion = ?";
            PreparedStatement psFormat = conn.prepareStatement(sqlFormat);
            psFormat.setInt(1, idPelicula);
            psFormat.setDate(2, new java.sql.Date(fecha.getTime()));
            ResultSet rsFormat = psFormat.executeQuery();
            while (rsFormat.next()) {
                cbFormato.addItem(rsFormat.getString("TipoDeSala"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set default selections to the first item or null if no items
        if (cbIdioma.getItemCount() > 0) {
            cbIdioma.setSelectedIndex(0);
            selectedIdioma = (String) cbIdioma.getSelectedItem();
        } else {
            selectedIdioma = null;
        }

        if (cbFormato.getItemCount() > 0) {
            cbFormato.setSelectedIndex(0);
            selectedFormato = (String) cbFormato.getSelectedItem();
        } else {
            selectedFormato = null;
        }

        // Re-add listeners
        if (idiomaListener != null) cbIdioma.addActionListener(idiomaListener);
        if (formatoListener != null) cbFormato.addActionListener(formatoListener);
    }

    private void cargarOtrasPeliculas() {
        try {
            String sql = "SELECT ID_Pelicula, Titulo, Imagen, ClasificacionEdad FROM Pelicula WHERE ID_Pelicula != ? LIMIT 7";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, this.idPelicula);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PeliculaCard card = new PeliculaCard(
                    rs.getInt("ID_Pelicula"),
                    rs.getString("Titulo"),
                    rs.getString("Imagen"),
                    rs.getString("ClasificacionEdad")
                );
                panelTarjetasPeliculas.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private JButton crearBotonFecha(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(getBackground().darker());
                } else {
                    g.setColor(getBackground());
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(80, 40));
        btn.setBackground(new Color(50, 50, 50));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            // Resaltar botón seleccionado
            for (Component c : panelFechas.getComponents()) {
                if (c instanceof JButton) {
                    c.setBackground(new Color(50, 50, 50));
                }
            }
            btn.setBackground(new Color(80, 80, 80));
        });
        
        return btn;
    }
    
    private JButton crearBotonHorario(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(getBackground().darker());
                } else {
                    g.setColor(getBackground());
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(90, 45));
        btn.setBackground(new Color(50, 50, 50));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            // Resaltar botón seleccionado
            for (Component c : panelHorarios.getComponents()) {
                if (c instanceof JButton) {
                    c.setBackground(new Color(50, 50, 50));
                }
            }
            btn.setBackground(new Color(220, 50, 50));
        });
        
        return btn;
    }
    
    private FuncionInfo obtenerFuncionSeleccionada() {
        for (Map.Entry<JButton, FuncionInfo> entry : mapaFunciones.entrySet()) {
            if (entry.getKey().getBackground().equals(new Color(220, 50, 50))) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    private void abrirPantallaButacas(FuncionInfo funcion) {
        M4.mostrarPantallaButacas(funcion.idFuncion, funcion.idSala, this.idPelicula);
        M4.cerrarVentana(this);
    }
}