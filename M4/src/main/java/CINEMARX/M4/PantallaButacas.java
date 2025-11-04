package CINEMARX.M4;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class PantallaButacas extends JFrame {
    private Connection conn;
    private int idFuncion;
    private int idSala;
    private int idPelicula; // Add field for movie ID
    private Set<String> butacasOcupadas;
    private Set<ButacaBoton> butacasSeleccionadas;
    private JPanel panelButacas;
    
    class ButacaBoton extends JButton {
        String id;
        boolean ocupada;
        boolean isSelected; // New field
        private Color currentBackgroundColor;

        ButacaBoton(String id) {
            this.id = id;
            this.ocupada = false;
            this.isSelected = false; // Initialize
            configurarEstilo();
        }

        void configurarEstilo() {
            setPreferredSize(new Dimension(18, 18)); // Reduced size
            setFont(new Font("Arial", Font.BOLD, 9));
            setFocusPainted(false);
            setBorderPainted(false); // We paint our own border
            setContentAreaFilled(false); // We handle the background filling
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (ocupada) {
                currentBackgroundColor = new Color(100, 100, 100);
                setForeground(new Color(60, 60, 60));
                setEnabled(false);
            } else {
                currentBackgroundColor = Color.WHITE;
                setForeground(new Color(50, 50, 50));
            }
        }

        void seleccionar() {
            currentBackgroundColor = new Color(220, 50, 50);
            setForeground(Color.WHITE);
            isSelected = true; // Set selected state
            repaint();
        }

        void deseleccionar() {
            if (!ocupada) {
                currentBackgroundColor = Color.WHITE;
                setForeground(new Color(50, 50, 50));
                isSelected = false; // Unset selected state
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE); // For sharper lines

            // Paint background
            g2.setColor(currentBackgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Rounded corners

            // Paint text
            super.paintComponent(g);

            // Draw tick if selected
            if (isSelected) {
                g2.setColor(new Color(0x6C0002)); // Tick color
                g2.setStroke(new BasicStroke(2)); // Thicker line for the tick

                int w = getWidth();
                int h = getHeight();

                // Coordinates for a simple tick (adjust as needed for appearance)
                // This is a basic V shape
                g2.drawLine(w / 4, h / 2, w / 2, h * 3 / 4);
                g2.drawLine(w / 2, h * 3 / 4, w * 3 / 4, h / 4);
            }
            
            g2.dispose();
        }
    }
    
    public PantallaButacas(Connection conn, int idFuncion, int idSala, int idPelicula) {
        this.conn = conn;
        this.idFuncion = idFuncion;
        this.idSala = idSala;
        this.idPelicula = idPelicula; // Store movie ID
        this.butacasOcupadas = new HashSet<>();
        this.butacasSeleccionadas = new HashSet<>();
        
        setTitle("CINEMAR X - Selecciona tu butaca");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cargarButacasOcupadas();
        inicializarUI();
    }
    
    private void cargarButacasOcupadas() {
        try {
            // Buscar todos los boletos de esta función
            String sql = "SELECT b.NumeroButaca FROM Boleto b " +
                        "INNER JOIN Empleado_Sala es ON b.ID_Empleado = es.ID_Empleado " +
                        "WHERE es.ID_Sala = ? AND b.ID_Boleto IN (" +
                        "  SELECT ID_Boleto FROM Comprobante_Boleto cb " +
                        "  INNER JOIN Comprobante c ON cb.ID_Comprobante = c.ID_Comprobante " +
                        "  WHERE c.ID_Funcion = ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idSala);
            ps.setInt(2, idFuncion);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                butacasOcupadas.add(rs.getString("NumeroButaca"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void inicializarUI() {
        // Main container for the whole window
        JPanel windowPanel = new JPanel(new BorderLayout());
        windowPanel.setBackground(new Color(30, 30, 30));

        // 1. Header (Top Bar) - This will be fixed at the top
        JPanel header = crearHeader();
        windowPanel.add(header, BorderLayout.NORTH);

        // 2. Scrollable Content Panel - This will contain everything else
        JPanel scrollableContent = new JPanel(new BorderLayout(20, 20));
        scrollableContent.setBackground(new Color(30, 30, 30));
        scrollableContent.setBorder(new EmptyBorder(30, 50, 30, 50));

        // Título
        JLabel titulo = new JLabel("Selecciona tu butaca", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        scrollableContent.add(titulo, BorderLayout.NORTH);

        // Panel central con butacas
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(30, 30, 30));

        // Pantalla
        JPanel panelPantalla = crearPanelPantalla();
        centerPanel.add(panelPantalla, BorderLayout.NORTH);

        // Butacas (NO MORE SCROLLPANE HERE)
        panelButacas = crearPanelButacas();
        centerPanel.add(panelButacas, BorderLayout.CENTER); // Add directly

        scrollableContent.add(centerPanel, BorderLayout.CENTER);

        // Panel inferior - Leyenda y botón
        JPanel bottomPanel = crearPanelInferior();
        scrollableContent.add(bottomPanel, BorderLayout.SOUTH);

        // 3. Create the main scroll pane that holds all the scrollable content
        JScrollPane mainScrollPane = new JScrollPane(scrollableContent);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Improve scroll speed

        // 4. Add the scroll pane to the main window panel
        windowPanel.add(mainScrollPane, BorderLayout.CENTER);

        // 5. Add the final panel to the JFrame
        add(windowPanel);
    }
    
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(40, 40, 40));
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));

        // Back button with image
        try {
            URL backIconUrl = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=botones%2Farrow-left.png");
            InputStream backIn = backIconUrl.openStream();
            BufferedImage backIconOriginal = ImageIO.read(backIn);
            backIn.close();

            if (backIconOriginal != null) {
                Image backIconScaled = backIconOriginal.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                JLabel backButtonLabel = new JLabel(new ImageIcon(backIconScaled));
                backButtonLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                backButtonLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        M4.mostrarPantallaPelicula(idPelicula);
                        dispose();
                    }
                });
                header.add(backButtonLabel, BorderLayout.WEST);
            } else {
                throw new Exception("Back icon image could not be read.");
            }
        } catch (Exception e) {
            // Fallback to text button if image fails
            JButton backButton = new JButton("<");
            backButton.addActionListener(ev -> {
                M4.mostrarPantallaPelicula(this.idPelicula);
                dispose();
            });
            header.add(backButton, BorderLayout.WEST);
        }

        // Logo with correct aspect ratio
        try {
            URL imageUrl = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=logos%2FCINEMARX%20logotipo.png");
            InputStream logoIn = imageUrl.openStream();
            BufferedImage originalImage = ImageIO.read(logoIn);
            logoIn.close();

            if (originalImage != null) {
                int newHeight = 40; // Set a fixed height
                int newWidth = (originalImage.getWidth() * newHeight) / originalImage.getHeight(); // Calculate width to maintain aspect ratio
                Image logo = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(logo));
                logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
                header.add(logoLabel, BorderLayout.CENTER);
            } else {
                 throw new Exception("Logo image could not be read.");
            }
        } catch (Exception e) {
            JLabel logoLabel = new JLabel("CINEMAR X");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 32));
            logoLabel.setForeground(Color.WHITE);
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            header.add(logoLabel, BorderLayout.CENTER);
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
    
    private JPanel crearPanelPantalla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(20, 0, 30, 0));

        try {
            URL imageUrl = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=M4%2FPantallaSala.png");
            InputStream in = imageUrl.openStream();
            BufferedImage originalImage = ImageIO.read(in);
            in.close();

            if (originalImage != null) {
                // --- Image Scaling Logic ---
                int newWidth = 600; // Define the new width
                int newHeight = (originalImage.getHeight() * newWidth) / originalImage.getWidth(); // Maintain aspect ratio

                Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                // --- End of Scaling Logic ---

                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                panel.add(imageLabel, BorderLayout.CENTER);
            } else {
                throw new Exception("Screen image could not be read.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback in case the image fails to load
            JLabel lblPantalla = new JLabel("PANTALLA (Error al cargar imagen)", JLabel.CENTER);
            lblPantalla.setFont(new Font("Arial", Font.BOLD, 14));
            lblPantalla.setForeground(Color.RED);
            panel.add(lblPantalla, BorderLayout.CENTER);
        }

        return panel;
    }
    
    private JPanel crearPanelButacas() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 30));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        
        // Configuración de la sala (similar a la imagen)
        int[] filasPorSeccion = {1, 4, 9, 3}; // Fila superior, filas superiores medias, filas centrales, pasillos laterales
        char letraInicial = 'A';
        
        // Fila superior (12 butacas)
        gbc.gridy = 0;
        for (int col = 0; col < 12; col++) {
            gbc.gridx = col + 4; // Centrado
            String id = "A" + (col + 1);
            agregarButaca(panel, id, gbc);
        }
        
        // Separador
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 20;
        panel.add(Box.createVerticalStrut(15), gbc);
        gbc.gridwidth = 1;
        
        // Filas superiores (B-E): 3 + 7 + pasillo + 7 + 3
        for (int fila = 0; fila < 4; fila++) {
            gbc.gridy++;
            char letra = (char) ('B' + fila);
            
            // Lado izquierdo (3 butacas)
            for (int col = 0; col < 3; col++) {
                gbc.gridx = col;
                String id = letra + "" + (col + 1);
                agregarButaca(panel, id, gbc);
            }
            
            // Centro-izquierda (7 butacas)
            for (int col = 0; col < 7; col++) {
                gbc.gridx = col + 4;
                String id = letra + "" + (col + 4);
                agregarButaca(panel, id, gbc);
            }
            
            // Centro-derecha (7 butacas)
            for (int col = 0; col < 7; col++) {
                gbc.gridx = col + 13;
                String id = letra + "" + (col + 11);
                agregarButaca(panel, id, gbc);
            }
            
            // Lado derecho (3 butacas)
            for (int col = 0; col < 3; col++) {
                gbc.gridx = col + 21;
                String id = letra + "" + (col + 18);
                agregarButaca(panel, id, gbc);
            }
        }
        
        // Separador
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 24;
        panel.add(Box.createVerticalStrut(15), gbc);
        gbc.gridwidth = 1;
        
        // Filas centrales (F-N): 3 + 7 + pasillo + 7 + 3
        for (int fila = 0; fila < 9; fila++) {
            gbc.gridy++;
            char letra = (char) ('F' + fila);
            
            // Lado izquierdo (3 butacas)
            for (int col = 0; col < 3; col++) {
                gbc.gridx = col;
                String id = letra + "" + (col + 1);
                agregarButaca(panel, id, gbc);
            }
            
            // Centro-izquierda (7 butacas)
            for (int col = 0; col < 7; col++) {
                gbc.gridx = col + 4;
                String id = letra + "" + (col + 4);
                agregarButaca(panel, id, gbc);
            }
            
            // Centro-derecha (7 butacas)
            for (int col = 0; col < 7; col++) {
                gbc.gridx = col + 13;
                String id = letra + "" + (col + 11);
                agregarButaca(panel, id, gbc);
            }
            
            // Lado derecho (3 butacas)
            for (int col = 0; col < 3; col++) {
                gbc.gridx = col + 21;
                String id = letra + "" + (col + 18);
                agregarButaca(panel, id, gbc);
            }
        }
        
        return panel;
    }
    
    private void agregarButaca(JPanel panel, String id, GridBagConstraints gbc) {
        ButacaBoton btn = new ButacaBoton(id);
        
        if (butacasOcupadas.contains(id)) {
            btn.ocupada = true;
            btn.configurarEstilo();
        } else {
            btn.addActionListener(e -> {
                if (butacasSeleccionadas.contains(btn)) {
                    butacasSeleccionadas.remove(btn);
                    btn.deseleccionar();
                } else {
                    butacasSeleccionadas.add(btn);
                    btn.seleccionar();
                }
            });
        }
        
        panel.add(btn, gbc);
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Leyenda
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        leyenda.setBackground(new Color(30, 30, 30));
        
        leyenda.add(crearItemLeyenda("Disponible", Color.WHITE));
        leyenda.add(crearItemLeyenda("No disponible", new Color(100, 100, 100)));
        leyenda.add(crearItemLeyenda("Seleccionado", new Color(220, 50, 50)));
        
        panel.add(leyenda, BorderLayout.NORTH);
        
        // Panel de compra
        JPanel panelCompra = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        panelCompra.setBackground(new Color(30, 30, 30));
        
        JButton btnSiguiente = new JButton("SIGUIENTE") {
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
        btnSiguiente.setFont(new Font("Arial", Font.BOLD, 16));
        btnSiguiente.setForeground(Color.WHITE);
        btnSiguiente.setBackground(new Color(220, 50, 50));
        btnSiguiente.setPreferredSize(new Dimension(200, 50));
        btnSiguiente.setFocusPainted(false);
        btnSiguiente.setBorderPainted(false);
        btnSiguiente.setContentAreaFilled(false);
        btnSiguiente.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSiguiente.addActionListener(e -> procesarCompra());
        panelCompra.add(btnSiguiente);
        
        panel.add(panelCompra, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearItemLeyenda(String texto, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        item.setBackground(new Color(30, 30, 30));
        
        JPanel circulo = new JPanel();
        circulo.setPreferredSize(new Dimension(25, 25));
        circulo.setBackground(color);
        circulo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(Color.WHITE);
        
        item.add(circulo);
        item.add(label);
        
        return item;
    }
    
    private void procesarCompra() {
        if (butacasSeleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione al menos una butaca", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Aquí iría la lógica de crear el boleto en la BD
        StringBuilder butacas = new StringBuilder();
        for (ButacaBoton btn : butacasSeleccionadas) {
            butacas.append(btn.id).append(", ");
        }
        
        int respuesta = JOptionPane.showConfirmDialog(this,
            "Butacas seleccionadas: " + butacas.toString() + "\n\n" +
            "¿Confirmar compra?",
            "Confirmar Compra",
            JOptionPane.YES_NO_OPTION);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            guardarCompra();
        }
    }
    
    private void guardarCompra() {
        try {
            conn.setAutoCommit(false);
            
            // Aquí implementarías la lógica completa de guardado en la BD
            // 1. Crear Comprobante
            // 2. Crear Boletos con NumeroButaca
            // 3. Relacionar en Comprobante_Boleto
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this, 
                "¡Compra realizada con éxito!", 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Volver a pantalla principal
            M4.cerrarVentana(this);
            // Aquí podrías mostrar un comprobante o volver al inicio
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al procesar la compra: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}