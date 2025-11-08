package CINEMARX.M7;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuffetFrame extends JFrame {
    private JPanel panelContenido;

    public BuffetFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Cinemar X - Buffet");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal con fondo oscuro
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(45, 45, 45));

        // Header con navegación
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // Panel de contenido con scroll
        panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(new Color(45, 45, 45));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Título principal "Buffet:"
        JLabel lblTitulo = new JLabel("Buffet:");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 36));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panelContenido.add(lblTitulo);

        JScrollPane scroll = new JScrollPane(panelContenido);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        scroll.setBackground(new Color(45, 45, 45));
        scroll.getViewport().setBackground(new Color(45, 45, 45));

        mainPanel.add(scroll, BorderLayout.CENTER);
        add(mainPanel);

        cargarProductosPorCategoria();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45, 45, 45));
        header.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50));

        // Logo
        JLabel lblLogo = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/resources/02.png"));
            Image logoImg = logoIcon.getImage().getScaledInstance(300, 75, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(logoImg));
        } catch (Exception e) {
            lblLogo.setText("CINEMAR X");
            lblLogo.setFont(new Font("Arial", Font.BOLD, 28));
            lblLogo.setForeground(Color.WHITE);
        }

        // Panel de navegación
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        navPanel.setBackground(new Color(45, 45, 45));

        JLabel lblPeliculas = new JLabel("PELICULAS");
        lblPeliculas.setFont(new Font("Arial", Font.PLAIN, 16));
        lblPeliculas.setForeground(Color.LIGHT_GRAY);
        lblPeliculas.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblBuffet = new JLabel("BUFFET");
        lblBuffet.setFont(new Font("Arial", Font.PLAIN, 16));
        lblBuffet.setForeground(Color.WHITE);
        lblBuffet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBuffet.setText("<html><u>BUFFET</u></html>");

        JLabel lblMembresia = new JLabel("MEMBRESIA");
        lblMembresia.setFont(new Font("Arial", Font.PLAIN, 16));
        lblMembresia.setForeground(Color.LIGHT_GRAY);
        lblMembresia.setCursor(new Cursor(Cursor.HAND_CURSOR));

        navPanel.add(lblPeliculas);
        navPanel.add(lblBuffet);
        navPanel.add(lblMembresia);

        // Icono de usuario
        JLabel lblUser = new JLabel("👤");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 24));
        lblUser.setForeground(Color.WHITE);
        lblUser.setCursor(new Cursor(Cursor.HAND_CURSOR));

        header.add(lblLogo, BorderLayout.WEST);
        header.add(navPanel, BorderLayout.CENTER);
        header.add(lblUser, BorderLayout.EAST);

        return header;
    }

    private void cargarProductosPorCategoria() {
        Map<String, List<Producto>> productosPorCategoria = new HashMap<>();

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                throw new Exception("No se pudo conectar a la base de datos.");
            }

            // Query adaptada a la nueva estructura (tabla Producto solo tiene ID_Prod, Nombre, Precio)
            String query = "SELECT ID_Prod, Nombre, Precio FROM Producto ORDER BY Nombre";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                // Crear producto con los datos disponibles
                Producto p = new Producto(
                    rs.getInt("ID_Prod"),
                    rs.getString("Nombre"),
                    rs.getDouble("Precio"),
                    determinarCategoria(rs.getString("Nombre")), // Determinar categoría por nombre
                    "" // Sin imagen por ahora
                );

                String categoria = p.getCategoria();
                if (!productosPorCategoria.containsKey(categoria)) {
                    productosPorCategoria.put(categoria, new ArrayList<>());
                }
                productosPorCategoria.get(categoria).add(p);
            }

        } catch (Exception e) {
            System.err.println("❌ Error al cargar productos: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al cargar productos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }

        mostrarProductosPorCategoria(productosPorCategoria);
    }

    // Método auxiliar para determinar categoría basándose en el nombre del producto
    private String determinarCategoria(String nombreProducto) {
        String nombre = nombreProducto.toLowerCase();
        
        if (nombre.contains("combo") || nombre.contains("pack")) {
            return "Combos";
        } else if (nombre.contains("coca") || nombre.contains("pepsi") || 
                   nombre.contains("sprite") || nombre.contains("agua") || 
                   nombre.contains("jugo") || nombre.contains("gaseosa")) {
            return "Bebidas";
        } else if (nombre.contains("pochoclo") || nombre.contains("nachos") || 
                   nombre.contains("palomitas")) {
            return "Snacks";
        } else if (nombre.contains("chocolate") || nombre.contains("caramelo") || 
                   nombre.contains("gomita") || nombre.contains("dulce")) {
            return "Golosinas";
        }
        
        return "Otros";
    }

    private void mostrarProductosPorCategoria(Map<String, List<Producto>> productosPorCategoria) {
        String[] ordenCategorias = {"Combos", "Bebidas", "Snacks", "Golosinas", "Otros"};

        for (String categoria : ordenCategorias) {
            if (productosPorCategoria.containsKey(categoria)) {
                List<Producto> productos = productosPorCategoria.get(categoria);

                // Título de la categoría
                JLabel lblCategoria = new JLabel(categoria + ":");
                lblCategoria.setFont(new Font("Arial", Font.BOLD, 24));
                lblCategoria.setForeground(Color.WHITE);
                lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblCategoria.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));
                panelContenido.add(lblCategoria);

                // Panel para productos de esta categoría
                JPanel panelCategoria = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 25));
                panelCategoria.setBackground(new Color(45, 45, 45));
                panelCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

                for (Producto p : productos) {
                    JPanel card = crearTarjetaProducto(p);
                    panelCategoria.add(card);
                }

                panelContenido.add(panelCategoria);
            }
        }

        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private JPanel crearTarjetaProducto(Producto p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(280, 380));
        card.setBackground(new Color(45, 45, 45));
        card.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Imagen con fondo beige
        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBackground(new Color(235, 225, 210));
        imgPanel.setPreferredSize(new Dimension(280, 280));

        // Emoji según categoría
        String emoji = obtenerEmojiPorCategoria(p.getCategoria());
        JLabel lbl = new JLabel(emoji, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 80));
        imgPanel.add(lbl, BorderLayout.CENTER);

        card.add(imgPanel, BorderLayout.CENTER);

        // Info inferior (nombre y precio)
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(new Color(45, 45, 45));
        info.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        JLabel lblNombre = new JLabel(p.getNombre().toUpperCase());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrecio = new JLabel("$" + String.format("%.0f", p.getPrecio()));
        lblPrecio.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPrecio.setForeground(Color.WHITE);
        lblPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(lblNombre);
        info.add(Box.createVerticalStrut(5));
        info.add(lblPrecio);

        card.add(info, BorderLayout.SOUTH);

        return card;
    }

    private String obtenerEmojiPorCategoria(String categoria) {
        switch (categoria) {
            case "Combos":
                return "🍿";
            case "Bebidas":
                return "🥤";
            case "Snacks":
                return "🍕";
            case "Golosinas":
                return "🍬";
            default:
                return "🎬";
        }
    }
}