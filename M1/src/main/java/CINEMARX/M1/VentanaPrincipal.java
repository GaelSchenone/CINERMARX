package CINEMARX.M1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class VentanaPrincipal extends JFrame {
    private Connection connection;
    private Logear sistemaLogin;
    private UsuarioCliente usuarioActual;

    private JPanel panelLateral;
    private JPanel panelContenido;
    
    // Sistema de puntos
    private int puntosUsuario = 0;

    public VentanaPrincipal(Connection connection) {
        this.connection = connection;

        setTitle("CINEMARX");
        setSize(1400, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 30));
        this.sistemaLogin = new Logear(connection);

        setVisible(false);
        mostrarLogin();
    }
    
    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(
            this, 
            "¿Estás seguro de que deseas cerrar sesión?", 
            "Cerrar Sesión", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            usuarioActual = null;
            setVisible(false);
            mostrarLogin();
        }
    }
    
    private void crearInterfazPrincipal() {
        getContentPane().removeAll();

        // CARGAR PUNTOS DESDE LA BASE DE DATOS
        puntosUsuario = sistemaLogin.getDbHelper().obtenerPuntosCliente(usuarioActual.getCorreo());

        // BARRA SUPERIOR
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BorderLayout());
        panelSuperior.setBackground(new Color(45, 45, 45));
        panelSuperior.setPreferredSize(new Dimension(getWidth(), 50));
        panelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));

        // Logo CINEMARX (izquierda) - Solo texto
        JLabel lblLogo = new JLabel("CINEMARX");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(new Color(220, 20, 60));
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Usuario (derecha)
        JPanel panelUsuarioDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        panelUsuarioDer.setBackground(new Color(45, 45, 45));

        JButton btnUsuario = new JButton(usuarioActual.getNombreCompleto());
        btnUsuario.setIcon(createUserIcon());
        btnUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnUsuario.setForeground(Color.WHITE);
        btnUsuario.setBackground(new Color(45, 45, 45));
        btnUsuario.setFocusPainted(false);
        btnUsuario.setBorderPainted(false);
        btnUsuario.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUsuario.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        // Menú desplegable del usuario
        JPopupMenu menuUsuario = new JPopupMenu();
        menuUsuario.setBackground(new Color(30, 30, 30));
        menuUsuario.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        
        JMenuItem itemPerfil = crearItemMenu("Perfil");
        JMenuItem itemCompras = crearItemMenu("Mis compras");
        JMenuItem itemCanjear = crearItemMenu("Canjear puntos");
        JMenuItem itemHistorialCanjes = crearItemMenu("Historial de canjes");
        menuUsuario.addSeparator();
        JMenuItem itemCerrarSesion = crearItemMenu("Cerrar sesión");
        
        itemPerfil.addActionListener(e -> mostrarPerfil());
        itemCompras.addActionListener(e -> mostrarHistorial());
        itemCanjear.addActionListener(e -> mostrarCanjePuntos());
        itemHistorialCanjes.addActionListener(e -> mostrarHistorialCanjes());
        itemCerrarSesion.addActionListener(e -> cerrarSesion());
        
        menuUsuario.add(itemPerfil);
        menuUsuario.add(itemCompras);
        menuUsuario.add(itemCanjear);
        menuUsuario.add(itemHistorialCanjes);
        menuUsuario.add(itemCerrarSesion);
        
        btnUsuario.addActionListener(e -> {
            menuUsuario.show(btnUsuario, 0, btnUsuario.getHeight());
        });

        panelUsuarioDer.add(btnUsuario);

        panelSuperior.add(lblLogo, BorderLayout.WEST);
        panelSuperior.add(panelUsuarioDer, BorderLayout.EAST);

        // PANEL LATERAL IZQUIERDO
        panelLateral = new JPanel();
        panelLateral.setLayout(new BoxLayout(panelLateral, BoxLayout.Y_AXIS));
        panelLateral.setBackground(new Color(20, 20, 20));
        panelLateral.setPreferredSize(new Dimension(250, getHeight()));
        panelLateral.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));

        // Cuenta Section
        JLabel lblCuentaTitulo = new JLabel("Cuenta");
        lblCuentaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCuentaTitulo.setForeground(new Color(150, 150, 150));
        lblCuentaTitulo.setBorder(BorderFactory.createEmptyBorder(0, 25, 10, 0));

        JButton btnPerfil = crearBotonLateral("• Perfil");
        btnPerfil.addActionListener(e -> mostrarPerfil());

        // Pagos y recompensas Section
        JLabel lblPagosTitulo = new JLabel("Pagos y recompensas");
        lblPagosTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPagosTitulo.setForeground(new Color(150, 150, 150));
        lblPagosTitulo.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 0));

        JButton btnMisCompras = crearBotonLateral("• Mis compras");
        btnMisCompras.addActionListener(e -> mostrarHistorial());

        JButton btnCanjearPuntos = crearBotonLateral("• Canjear puntos");
        btnCanjearPuntos.addActionListener(e -> mostrarCanjePuntos());

        JButton btnHistorialCanjes = crearBotonLateral("• Historial de canjes");
        btnHistorialCanjes.addActionListener(e -> mostrarHistorialCanjes());

        // Separador
        JSeparator separador = new JSeparator();
        separador.setForeground(new Color(60, 60, 60));
        separador.setMaximumSize(new Dimension(200, 1));
        separador.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCerrarSesion = crearBotonLateral("• Cerrar sesión");
        btnCerrarSesion.setForeground(new Color(220, 20, 60));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        panelLateral.add(lblCuentaTitulo);
        panelLateral.add(btnPerfil);
        panelLateral.add(lblPagosTitulo);
        panelLateral.add(btnMisCompras);
        panelLateral.add(btnCanjearPuntos);
        panelLateral.add(btnHistorialCanjes);
        panelLateral.add(Box.createVerticalStrut(20));
        panelLateral.add(separador);
        panelLateral.add(Box.createVerticalStrut(10));
        panelLateral.add(btnCerrarSesion);
        panelLateral.add(Box.createVerticalGlue());

        // PANEL DE CONTENIDO
        panelContenido = new JPanel();
        panelContenido.setBackground(new Color(30, 30, 30));
        panelContenido.setLayout(new BorderLayout());

        mostrarBienvenida();

        add(panelSuperior, BorderLayout.NORTH);
        add(panelLateral, BorderLayout.WEST);
        add(panelContenido, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JButton crearBotonLateral(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(20, 20, 20));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setContentAreaFilled(false);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        boton.setMaximumSize(new Dimension(250, 40));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(new Color(35, 35, 35));
                boton.setOpaque(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(new Color(20, 20, 20));
                boton.setOpaque(false);
            }
        });
        
        return boton;
    }

    private ImageIcon createUserIcon() {
        int size = 20;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillOval(0, 0, size, size);
        g2.dispose();
        return new ImageIcon(img);
    }
    
    private JMenuItem crearItemMenu(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setForeground(Color.WHITE);
        item.setBackground(new Color(30, 30, 30));
        item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        item.setOpaque(true);
        
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(45, 45, 45));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(new Color(30, 30, 30));
            }
        });
        
        return item;
    }

    private void mostrarBienvenida() {
        panelContenido.removeAll();
        panelContenido.setLayout(new GridBagLayout());

        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(new Color(30, 30, 30));
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));

        JLabel lblBienvenida = new JLabel("Bienvenido, " + usuarioActual.getNombreCompleto() + "!");
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblBienvenida.setForeground(Color.WHITE);
        lblBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMensaje = new JLabel("Selecciona una opción del menú para comenzar");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMensaje.setForeground(new Color(150, 150, 150));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelCentral.add(lblBienvenida);
        panelCentral.add(Box.createVerticalStrut(15));
        panelCentral.add(lblMensaje);

        panelContenido.add(panelCentral);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarCanjePuntos() {
        // Recargar puntos desde la BD
        puntosUsuario = sistemaLogin.getDbHelper().obtenerPuntosCliente(usuarioActual.getCorreo());
        
        panelContenido.removeAll();
        panelContenido.setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout());
        panelPrincipal.setBackground(new Color(30, 30, 30));

        // Panel superior con título y puntos
        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(30, 30, 30));
        panelSuperior.setLayout(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBackground(new Color(30, 30, 30));

        JLabel lblTitulo = new JLabel("Canjea tus puntos:");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelIzquierdo.add(lblTitulo);

        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelDerecho.setBackground(new Color(30, 30, 30));

        JLabel lblPuntosDisponibles = new JLabel("Tus puntos Cinemax: " + puntosUsuario);
        lblPuntosDisponibles.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPuntosDisponibles.setForeground(Color.WHITE);
        lblPuntosDisponibles.setBackground(new Color(220, 20, 60));
        lblPuntosDisponibles.setOpaque(true);
        lblPuntosDisponibles.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        panelDerecho.add(lblPuntosDisponibles);

        panelSuperior.add(panelIzquierdo, BorderLayout.WEST);
        panelSuperior.add(panelDerecho, BorderLayout.EAST);

        // Panel central con productos
        JPanel panelProductos = new JPanel();
        panelProductos.setLayout(new GridLayout(2, 2, 20, 20));
        panelProductos.setBackground(new Color(30, 30, 30));
        panelProductos.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        // Productos disponibles con sus precios
        String[][] productos = {
            {"COMBO PANCHO", "500"},
            {"COMBO CHIPA", "500"},
            {"AMERICAN PSYCHO", "1500"},
            {"TAXI DRIVER", "1500"}
        };

        ButtonGroup grupo = new ButtonGroup();
        JRadioButton[] radios = new JRadioButton[4];

        for (int i = 0; i < productos.length; i++) {
            JPanel panelProducto = crearPanelProducto(productos[i][0], productos[i][1], true);
            radios[i] = (JRadioButton) panelProducto.getComponent(0);
            grupo.add(radios[i]);
            panelProductos.add(panelProducto);
        }

        // Panel inferior con botón de canje
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.setBackground(new Color(30, 30, 30));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JButton btnCanjear = new JButton("CANJEAR PUNTOS");
        btnCanjear.setPreferredSize(new Dimension(250, 50));
        btnCanjear.setBackground(new Color(220, 20, 60));
        btnCanjear.setForeground(Color.WHITE);
        btnCanjear.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCanjear.setFocusPainted(false);
        btnCanjear.setBorderPainted(false);
        btnCanjear.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Panel para mensajes
        JPanel panelMensajes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelMensajes.setBackground(new Color(30, 30, 30));
        panelMensajes.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panelMensajes.setVisible(false);

        JLabel lblMensaje = new JLabel();
        lblMensaje.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMensaje.setForeground(new Color(255, 87, 51));
        panelMensajes.add(lblMensaje);

        btnCanjear.addActionListener(e -> {
            int seleccionado = -1;
            for (int i = 0; i < radios.length; i++) {
                if (radios[i].isSelected()) {
                    seleccionado = i;
                    break;
                }
            }

            if (seleccionado == -1) {
                return;
            }

            String productoNombre = productos[seleccionado][0];
            int puntosRequeridos = Integer.parseInt(productos[seleccionado][1]);

            if (puntosUsuario >= puntosRequeridos) {
                // REGISTRAR CANJE EN LA BASE DE DATOS
                if (sistemaLogin.getDbHelper().registrarCanje(usuarioActual.getCorreo(), productoNombre, puntosRequeridos)) {
                    puntosUsuario -= puntosRequeridos;

                    // Mostrar pantalla de éxito
                    mostrarPantallaCanjeoExitoso(productoNombre, puntosRequeridos);
                    grupo.clearSelection();
                } else {
                    lblMensaje.setText("⚠ Error al procesar el canje. Intenta de nuevo.");
                    panelMensajes.setVisible(true);
                }
            } else {
                lblMensaje.setText("⚠ No tienes suficientes puntos para canjear este producto");
                lblMensaje.setForeground(new Color(255, 152, 0));
                panelMensajes.setVisible(true);

                // Ocultar mensaje después de 4 segundos
                Timer timer = new Timer(4000, evt -> panelMensajes.setVisible(false));
                timer.setRepeats(false);
                timer.start();
            }
        });

        panelInferior.add(panelMensajes);
        panelInferior.add(btnCanjear);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelProductos, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        panelContenido.add(panelPrincipal);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private JPanel crearPanelProducto(String nombre, String puntos, boolean mostrarPuntos) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70), 2));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JRadioButton radio = new JRadioButton();
        radio.setBackground(new Color(45, 45, 45));
        radio.setFocusPainted(false);
        radio.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

        // Panel para la imagen (placeholder con color de fondo)
        JPanel panelImagen = new JPanel();
        panelImagen.setPreferredSize(new Dimension(200, 180));
        panelImagen.setBackground(new Color(60, 60, 60));
        panelImagen.setLayout(new GridBagLayout());
        
        JLabel lblIcono = new JLabel("🍿");
        lblIcono.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        panelImagen.add(lblIcono);

        // Panel de información
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(new Color(45, 45, 45));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelInfo.add(lblNombre);

        if (mostrarPuntos) {
            panelInfo.add(Box.createVerticalStrut(10));
            JLabel lblPuntos = new JLabel(puntos);
            lblPuntos.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblPuntos.setForeground(new Color(220, 20, 60));
            lblPuntos.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelInfo.add(lblPuntos);
        }

        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(new Color(45, 45, 45));
        panelCentral.add(panelImagen, BorderLayout.CENTER);
        panelCentral.add(panelInfo, BorderLayout.SOUTH);

        panel.add(radio, BorderLayout.NORTH);
        panel.add(panelCentral, BorderLayout.CENTER);

        // Hacer que TODO el panel sea clickeable
        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radio.setSelected(true);
            }
        };
        
        panel.addMouseListener(clickListener);
        panelImagen.addMouseListener(clickListener);
        panelInfo.addMouseListener(clickListener);
        panelCentral.addMouseListener(clickListener);

        return panel;
    }

    private void mostrarHistorialCanjes() {
        // Recargar puntos desde la BD
        puntosUsuario = sistemaLogin.getDbHelper().obtenerPuntosCliente(usuarioActual.getCorreo());
        
        panelContenido.removeAll();
        panelContenido.setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout());
        panelPrincipal.setBackground(new Color(30, 30, 30));

        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(30, 30, 30));
        panelSuperior.setLayout(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBackground(new Color(30, 30, 30));

        JLabel lblTitulo = new JLabel("Historial de canjes");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Productos canjeados con tus puntos Cinemax.");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(new Color(180, 180, 180));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelIzquierdo.add(lblTitulo);
        panelIzquierdo.add(Box.createVerticalStrut(5));
        panelIzquierdo.add(lblSubtitulo);

        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        panelDerecho.setBackground(new Color(30, 30, 30));

        // OBTENER PUNTOS GASTADOS DESDE LA BD
        int totalGastado = 0;
        String sqlPuntosGastados = "SELECT PuntosGastados FROM Cliente WHERE Mail = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sqlPuntosGastados)) {
            pstmt.setString(1, usuarioActual.getCorreo());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalGastado = rs.getInt("PuntosGastados");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JLabel lblPuntosGastados = new JLabel("Gastados: " + totalGastado);
        lblPuntosGastados.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPuntosGastados.setForeground(new Color(255, 140, 0));
        panelDerecho.add(lblPuntosGastados);

        JLabel lblPuntosActuales = new JLabel("Actuales: " + puntosUsuario);
        lblPuntosActuales.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPuntosActuales.setForeground(Color.WHITE);
        lblPuntosActuales.setBackground(new Color(220, 20, 60));
        lblPuntosActuales.setOpaque(true);
        lblPuntosActuales.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        panelDerecho.add(lblPuntosActuales);

        panelSuperior.add(panelIzquierdo, BorderLayout.WEST);
        panelSuperior.add(panelDerecho, BorderLayout.EAST);

        JPanel panelTabla = new JPanel();
        panelTabla.setBackground(new Color(40, 40, 40));
        panelTabla.setLayout(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // OBTENER HISTORIAL DESDE LA BASE DE DATOS
        ResultSet rs = sistemaLogin.getDbHelper().obtenerHistorialCanjes(usuarioActual.getCorreo());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy");
        boolean hayCanjes = false;

        try {
            if (rs != null && rs.isBeforeFirst()) {
                hayCanjes = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!hayCanjes) {
            JLabel lblSinCanjes = new JLabel("No has canjeado ningún producto aún.");
            lblSinCanjes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblSinCanjes.setForeground(new Color(150, 150, 150));
            lblSinCanjes.setHorizontalAlignment(SwingConstants.CENTER);
            panelTabla.add(lblSinCanjes, BorderLayout.CENTER);
        } else {
            JPanel panelEncabezados = new JPanel(new GridLayout(1, 3));
            panelEncabezados.setBackground(new Color(40, 40, 40));
            panelEncabezados.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

            JLabel lblFecha = new JLabel("Fecha");
            lblFecha.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblFecha.setForeground(new Color(200, 200, 200));

            JLabel lblProducto = new JLabel("Producto");
            lblProducto.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblProducto.setForeground(new Color(200, 200, 200));

            JLabel lblPuntos = new JLabel("Puntos", SwingConstants.RIGHT);
            lblPuntos.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblPuntos.setForeground(new Color(200, 200, 200));

            panelEncabezados.add(lblFecha);
            panelEncabezados.add(lblProducto);
            panelEncabezados.add(lblPuntos);

            JPanel panelCanjes = new JPanel();
            panelCanjes.setLayout(new BoxLayout(panelCanjes, BoxLayout.Y_AXIS));
            panelCanjes.setBackground(new Color(40, 40, 40));

            try {
                int contador = 0;
                while (rs.next()) {
                    Timestamp fecha = rs.getTimestamp("FechaCanje");
                    String producto = rs.getString("Producto");
                    int puntos = rs.getInt("Puntos");
                    
                    JPanel panelFila = new JPanel(new GridLayout(1, 3));
                    panelFila.setBackground(new Color(40, 40, 40));
                    panelFila.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
                    panelFila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                    JLabel fechaLabel = new JLabel(sdf.format(fecha));
                    fechaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    fechaLabel.setForeground(Color.WHITE);

                    JLabel productoLabel = new JLabel(producto);
                    productoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    productoLabel.setForeground(Color.WHITE);

                    JLabel puntosLabel = new JLabel(String.valueOf(puntos), SwingConstants.RIGHT);
                    puntosLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    puntosLabel.setForeground(new Color(220, 20, 60));

                    panelFila.add(fechaLabel);
                    panelFila.add(productoLabel);
                    panelFila.add(puntosLabel);

                    panelCanjes.add(panelFila);

                    contador++;
                    JSeparator separador = new JSeparator();
                    separador.setForeground(new Color(60, 60, 60));
                    separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    panelCanjes.add(separador);
                }

                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            JScrollPane scrollCanjes = new JScrollPane(panelCanjes);
            scrollCanjes.setBorder(null);
            scrollCanjes.setBackground(new Color(40, 40, 40));
            scrollCanjes.getViewport().setBackground(new Color(40, 40, 40));

            panelTabla.add(panelEncabezados, BorderLayout.NORTH);
            panelTabla.add(scrollCanjes, BorderLayout.CENTER);
        }

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelTabla, BorderLayout.CENTER);

        panelContenido.add(panelPrincipal, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarHistorial() {
        // Recargar puntos desde la BD
        puntosUsuario = sistemaLogin.getDbHelper().obtenerPuntosCliente(usuarioActual.getCorreo());
        
        panelContenido.removeAll();
        panelContenido.setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout());
        panelPrincipal.setBackground(new Color(30, 30, 30));

        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(30, 30, 30));
        panelSuperior.setLayout(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBackground(new Color(30, 30, 30));

        JLabel lblTitulo = new JLabel("Mis compras");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Consulta el historial de compras realizadas en tu cuenta.");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(new Color(180, 180, 180));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelIzquierdo.add(lblTitulo);
        panelIzquierdo.add(Box.createVerticalStrut(5));
        panelIzquierdo.add(lblSubtitulo);

        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelDerecho.setBackground(new Color(30, 30, 30));

        JLabel lblPuntos = new JLabel("Puntos: " + puntosUsuario);
        lblPuntos.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPuntos.setForeground(new Color(220, 20, 60));
        panelDerecho.add(lblPuntos);

        panelSuperior.add(panelIzquierdo, BorderLayout.CENTER);
        panelSuperior.add(panelDerecho, BorderLayout.EAST);

        JPanel panelTabla = new JPanel();
        panelTabla.setBackground(new Color(40, 40, 40));
        panelTabla.setLayout(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel panelEncabezados = new JPanel(new GridLayout(1, 3));
        panelEncabezados.setBackground(new Color(40, 40, 40));
        panelEncabezados.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel lblFecha = new JLabel("Fecha");
        lblFecha.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFecha.setForeground(new Color(200, 200, 200));

        JLabel lblDescripcion = new JLabel("Descripción");
        lblDescripcion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDescripcion.setForeground(new Color(200, 200, 200));

        JLabel lblPrecio = new JLabel("Precio", SwingConstants.RIGHT);
        lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPrecio.setForeground(new Color(200, 200, 200));

        panelEncabezados.add(lblFecha);
        panelEncabezados.add(lblDescripcion);
        panelEncabezados.add(lblPrecio);

        JPanel panelCompras = new JPanel();
        panelCompras.setLayout(new BoxLayout(panelCompras, BoxLayout.Y_AXIS));
        panelCompras.setBackground(new Color(40, 40, 40));

        ResultSet rs = sistemaLogin.getDbHelper().obtenerHistorialCompras(usuarioActual.getCorreo());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy");
        double totalGastado = 0;
        boolean hayCompras = false;

        try {
            while (rs != null && rs.next()) {
                hayCompras = true;

                Timestamp fecha = rs.getTimestamp("Fecha");
                String descripcion = rs.getString("Descripcion");
                double precio = rs.getDouble("Precio");
                totalGastado += precio;

                JPanel panelFila = new JPanel(new GridLayout(1, 3));
                panelFila.setBackground(new Color(40, 40, 40));
                panelFila.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
                panelFila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                JLabel fechaLabel = new JLabel(sdf.format(fecha));
                fechaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                fechaLabel.setForeground(Color.WHITE);

                JLabel descripcionLabel = new JLabel(descripcion);
                descripcionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                descripcionLabel.setForeground(Color.WHITE);

                JLabel precioLabel = new JLabel(String.format("$%.2f", precio), SwingConstants.RIGHT);
                precioLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                precioLabel.setForeground(Color.WHITE);

                panelFila.add(fechaLabel);
                panelFila.add(descripcionLabel);
                panelFila.add(precioLabel);

                panelCompras.add(panelFila);

                JSeparator separador = new JSeparator();
                separador.setForeground(new Color(60, 60, 60));
                separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                panelCompras.add(separador);
            }

            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el historial de compras.");
        }

        if (!hayCompras) {
            JLabel lblSinCompras = new JLabel("No tienes compras registradas aún.");
            lblSinCompras.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblSinCompras.setForeground(new Color(150, 150, 150));
            lblSinCompras.setHorizontalAlignment(SwingConstants.CENTER);
            panelTabla.add(lblSinCompras, BorderLayout.CENTER);
        } else {
            JScrollPane scrollCompras = new JScrollPane(panelCompras);
            scrollCompras.setBorder(null);
            scrollCompras.setBackground(new Color(40, 40, 40));
            scrollCompras.getViewport().setBackground(new Color(40, 40, 40));

            panelTabla.add(panelEncabezados, BorderLayout.NORTH);
            panelTabla.add(scrollCompras, BorderLayout.CENTER);

            JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panelInferior.setBackground(new Color(40, 40, 40));
            panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

            JLabel lblTotal = new JLabel(String.format("TOTAL: $%.2f", totalGastado));
            lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTotal.setForeground(Color.WHITE);
            panelInferior.add(lblTotal);

            panelTabla.add(panelInferior, BorderLayout.SOUTH);
        }

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelTabla, BorderLayout.CENTER);

        panelContenido.add(panelPrincipal, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarPerfil() {
        panelContenido.removeAll();
        panelContenido.setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBackground(new Color(30, 30, 30));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblTitulo = new JLabel("Mi Perfil");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Gestiona los datos de tu cuenta.");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(180, 180, 180));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSeccion1 = new JLabel("Información personal");
        lblSeccion1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSeccion1.setForeground(Color.WHITE);
        lblSeccion1.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSeccion1.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 0));

        JPanel panelCampos = new JPanel();
        panelCampos.setLayout(new BoxLayout(panelCampos, BoxLayout.Y_AXIS));
        panelCampos.setBackground(new Color(30, 30, 30));
        panelCampos.setAlignmentX(Component.LEFT_ALIGNMENT);

        // NOMBRE
        JLabel lblNombreLabel = new JLabel("Nombre");
        lblNombreLabel.setForeground(new Color(180, 180, 180));
        lblNombreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNombreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtNombre = new JTextField(usuarioActual.getNombre());
        txtNombre.setMaximumSize(new Dimension(500, 45));
        txtNombre.setBackground(new Color(50, 50, 50));
        txtNombre.setForeground(Color.WHITE);
        txtNombre.setCaretColor(Color.WHITE);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNombre.setEditable(false);

        panelCampos.add(lblNombreLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtNombre);
        panelCampos.add(Box.createVerticalStrut(15));

        // APELLIDO
        JLabel lblApellidoLabel = new JLabel("Apellido");
        lblApellidoLabel.setForeground(new Color(180, 180, 180));
        lblApellidoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblApellidoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String apellidoActual = usuarioActual.getApellido() != null ? usuarioActual.getApellido() : "";
        JTextField txtApellido = new JTextField(apellidoActual);
        txtApellido.setMaximumSize(new Dimension(500, 45));
        txtApellido.setBackground(new Color(50, 50, 50));
        txtApellido.setForeground(Color.WHITE);
        txtApellido.setCaretColor(Color.WHITE);
        txtApellido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtApellido.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtApellido.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtApellido.setEditable(false);

        panelCampos.add(lblApellidoLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtApellido);
        panelCampos.add(Box.createVerticalStrut(15));

        // DNI
        JLabel lblDNILabel = new JLabel("DNI");
        lblDNILabel.setForeground(new Color(180, 180, 180));
        lblDNILabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDNILabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtDNI = new JTextField(String.valueOf(usuarioActual.getDNI()));
        txtDNI.setMaximumSize(new Dimension(500, 45));
        txtDNI.setBackground(new Color(50, 50, 50));
        txtDNI.setForeground(Color.WHITE);
        txtDNI.setCaretColor(Color.WHITE);
        txtDNI.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDNI.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtDNI.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDNI.setEditable(false);

        panelCampos.add(lblDNILabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtDNI);
        panelCampos.add(Box.createVerticalStrut(15));

        // FECHA DE NACIMIENTO
        JLabel lblFechaNacLabel = new JLabel("Fecha de Nacimiento");
        lblFechaNacLabel.setForeground(new Color(180, 180, 180));
        lblFechaNacLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFechaNacLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        java.sql.Date fechaNac = usuarioActual.getFechaNacimiento();
        String fechaNacStr = "";
        if (fechaNac != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(fechaNac);
            fechaNacStr = String.format("%02d/%02d/%04d", 
                cal.get(java.util.Calendar.DAY_OF_MONTH),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.YEAR));
        }

        JTextField txtFechaNac = new JTextField(fechaNacStr);
        txtFechaNac.setMaximumSize(new Dimension(500, 45));
        txtFechaNac.setBackground(new Color(50, 50, 50));
        txtFechaNac.setForeground(Color.WHITE);
        txtFechaNac.setCaretColor(Color.WHITE);
        txtFechaNac.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtFechaNac.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtFechaNac.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtFechaNac.setEditable(false);

        panelCampos.add(lblFechaNacLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtFechaNac);
        panelCampos.add(Box.createVerticalStrut(15));

        JLabel lblSeccion2 = new JLabel("Información de la cuenta");
        lblSeccion2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSeccion2.setForeground(Color.WHITE);
        lblSeccion2.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSeccion2.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        panelCampos.add(lblSeccion2);

        // CORREO
        JLabel lblCorreoLabel = new JLabel("Dirección de correo electrónico");
        lblCorreoLabel.setForeground(new Color(180, 180, 180));
        lblCorreoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCorreoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtCorreo = new JTextField(usuarioActual.getCorreo());
        txtCorreo.setMaximumSize(new Dimension(500, 45));
        txtCorreo.setBackground(new Color(50, 50, 50));
        txtCorreo.setForeground(Color.WHITE);
        txtCorreo.setCaretColor(Color.WHITE);
        txtCorreo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCorreo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtCorreo.setEditable(false);

        panelCampos.add(lblCorreoLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtCorreo);
        panelCampos.add(Box.createVerticalStrut(15));

        // CONTRASEÑA
        JLabel lblPassInfoLabel = new JLabel("Contraseña");
        lblPassInfoLabel.setForeground(new Color(180, 180, 180));
        lblPassInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPassInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField txtPassInfo = new JPasswordField(usuarioActual.getContrasena());
        txtPassInfo.setMaximumSize(new Dimension(500, 45));
        txtPassInfo.setBackground(new Color(50, 50, 50));
        txtPassInfo.setForeground(Color.WHITE);
        txtPassInfo.setCaretColor(Color.WHITE);
        txtPassInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtPassInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassInfo.setEditable(false);

        panelCampos.add(lblPassInfoLabel);
        panelCampos.add(Box.createVerticalStrut(5));
        panelCampos.add(txtPassInfo);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
        panelBotones.setBackground(new Color(30, 30, 30));
        panelBotones.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEditar = new JButton("Editar Datos");
        btnEditar.setPreferredSize(new Dimension(150, 40));
        btnEditar.setBackground(new Color(220, 20, 60));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEditar.setFocusPainted(false);
        btnEditar.setBorderPainted(false);
        btnEditar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setPreferredSize(new Dimension(150, 40));
        btnGuardar.setBackground(new Color(220, 20, 60));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setVisible(false);

        panelBotones.add(btnEditar);
        panelBotones.add(btnGuardar);

        btnEditar.addActionListener(e -> {
            txtNombre.setEditable(true);
            txtApellido.setEditable(true);
            txtDNI.setEditable(true);
            txtFechaNac.setEditable(true);
            txtCorreo.setEditable(true);
            txtPassInfo.setEditable(true);
            txtPassInfo.setEchoChar((char) 0);
            btnEditar.setVisible(false);
            btnGuardar.setVisible(true);
        });

        // Variable para el mensaje de confirmación
        final JLabel[] lblMensajeExito = new JLabel[1];
        final JLabel[] lblMensajeError = new JLabel[1];

        btnGuardar.addActionListener(e -> {
            String nuevoNombre = txtNombre.getText().trim();
            String nuevoApellido = txtApellido.getText().trim();
            String nuevoDNIStr = txtDNI.getText().trim();
            String nuevaFechaStr = txtFechaNac.getText().trim();
            String nuevoCorreo = txtCorreo.getText().trim();
            String nuevaPass = new String(txtPassInfo.getPassword()).trim();

            // Ocultar mensajes previos
            if (lblMensajeExito[0] != null) lblMensajeExito[0].setVisible(false);
            if (lblMensajeError[0] != null) lblMensajeError[0].setVisible(false);

            if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty() || nuevoDNIStr.isEmpty() || 
                nuevaFechaStr.isEmpty() || nuevoCorreo.isEmpty() || nuevaPass.isEmpty()) {
                mostrarMensajeError("Todos los campos son obligatorios", lblMensajeError, panelBotones);
                return;
            }

            int nuevoDNI;
            try {
                nuevoDNI = Integer.parseInt(nuevoDNIStr);
                if (nuevoDNIStr.length() != 8) {
                    mostrarMensajeError("El DNI debe tener exactamente 8 dígitos", lblMensajeError, panelBotones);
                    return;
                }
            } catch (NumberFormatException ex) {
                mostrarMensajeError("El DNI debe contener solo números", lblMensajeError, panelBotones);
                return;
            }

            java.sql.Date nuevaFechaNac;
            try {
                String[] partes = nuevaFechaStr.split("/");
                if (partes.length != 3) {
                    mostrarMensajeError("La fecha debe tener el formato DD/MM/AAAA", lblMensajeError, panelBotones);
                    return;
                }
                int dia = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                int anio = Integer.parseInt(partes[2]);

                // Validar rangos básicos
                if (dia < 1 || dia > 31) {
                    mostrarMensajeError("El día debe estar entre 1 y 31", lblMensajeError, panelBotones);
                    return;
                }
                if (mes < 1 || mes > 12) {
                    mostrarMensajeError("El mes debe estar entre 1 y 12", lblMensajeError, panelBotones);
                    return;
                }
                if (anio < 1920 || anio > 2024) {
                    mostrarMensajeError("El año debe estar entre 1920 y 2024", lblMensajeError, panelBotones);
                    return;
                }
                
                // Validar días según el mes
                if (mes == 2) { // Febrero
                    boolean esBisiesto = (anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0);
                    if (dia > (esBisiesto ? 29 : 28)) {
                        mostrarMensajeError("Febrero no puede tener más de " + (esBisiesto ? "29" : "28") + " días", lblMensajeError, panelBotones);
                        return;
                    }
                } else if (mes == 4 || mes == 6 || mes == 9 || mes == 11) { // Meses de 30 días
                    if (dia > 30) {
                        mostrarMensajeError("Este mes no puede tener más de 30 días", lblMensajeError, panelBotones);
                        return;
                    }
                }

                String fechaSQL = String.format("%04d-%02d-%02d", anio, mes, dia);
                nuevaFechaNac = java.sql.Date.valueOf(fechaSQL);
            } catch (NumberFormatException ex) {
                mostrarMensajeError("La fecha debe contener solo números", lblMensajeError, panelBotones);
                return;
            } catch (Exception ex) {
                mostrarMensajeError("La fecha ingresada no es válida", lblMensajeError, panelBotones);
                return;
            }

            if (!nuevoCorreo.toLowerCase().endsWith("@gmail.com")) {
                mostrarMensajeError("El correo debe terminar en @gmail.com", lblMensajeError, panelBotones);
                return;
            }

            DatabaseHelper dbHelper = sistemaLogin.getDbHelper();

            if (dbHelper.actualizarDatosCliente(usuarioActual.getCorreo(), nuevoNombre, nuevoApellido,
                                                nuevoCorreo, nuevaPass, nuevoDNI, nuevaFechaNac)) {
                usuarioActual.setNombre(nuevoNombre);
                usuarioActual.setApellido(nuevoApellido);
                usuarioActual.setCorreo(nuevoCorreo);
                usuarioActual.setContrasena(nuevaPass);
                usuarioActual.setDNI(nuevoDNI);
                usuarioActual.setFechaNacimiento(nuevaFechaNac);

                // Mostrar mensaje de éxito elegante
                if (lblMensajeExito[0] == null) {
                    lblMensajeExito[0] = new JLabel("✓ Datos guardados correctamente");
                    lblMensajeExito[0].setFont(new Font("Segoe UI", Font.BOLD, 14));
                    lblMensajeExito[0].setForeground(new Color(76, 175, 80));
                    lblMensajeExito[0].setAlignmentX(Component.LEFT_ALIGNMENT);
                    lblMensajeExito[0].setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
                    panelBotones.add(lblMensajeExito[0]);
                }
                lblMensajeExito[0].setVisible(true);
                panelBotones.revalidate();
                panelBotones.repaint();

                // Deshabilitar edición
                txtNombre.setEditable(false);
                txtApellido.setEditable(false);
                txtDNI.setEditable(false);
                txtFechaNac.setEditable(false);
                txtCorreo.setEditable(false);
                txtPassInfo.setEditable(false);
                txtPassInfo.setEchoChar('•');
                btnEditar.setVisible(true);
                btnGuardar.setVisible(false);

                // Ocultar mensaje después de 3 segundos
                Timer timer = new Timer(3000, evt -> {
                    if (lblMensajeExito[0] != null) {
                        lblMensajeExito[0].setVisible(false);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                mostrarMensajeError("Error: El correo o DNI ya están registrados", lblMensajeError, panelBotones);
            }
        });

        panelPrincipal.add(lblTitulo);
        panelPrincipal.add(Box.createVerticalStrut(5));
        panelPrincipal.add(lblSubtitulo);
        panelPrincipal.add(lblSeccion1);
        panelPrincipal.add(Box.createVerticalStrut(10));
        panelPrincipal.add(panelCampos);
        panelPrincipal.add(panelBotones);

        JScrollPane scrollPane = new JScrollPane(panelPrincipal);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panelContenido.add(scrollPane, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarPantallaCanjeoExitoso(String producto, int puntos) {
        panelContenido.removeAll();
        panelContenido.setLayout(new GridBagLayout());

        JPanel panelExito = new JPanel();
        panelExito.setLayout(new BoxLayout(panelExito, BoxLayout.Y_AXIS));
        panelExito.setBackground(new Color(30, 30, 30));
        panelExito.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Ícono de éxito
        JLabel lblIcono = new JLabel("✓");
        lblIcono.setFont(new Font("Segoe UI", Font.BOLD, 80));
        lblIcono.setForeground(new Color(76, 175, 80));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("¡Canje exitoso!");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblProducto = new JLabel(producto);
        lblProducto.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblProducto.setForeground(new Color(180, 180, 180));
        lblProducto.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPuntos = new JLabel(puntos + " puntos utilizados");
        lblPuntos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPuntos.setForeground(new Color(220, 20, 60));
        lblPuntos.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRestantes = new JLabel("Puntos restantes: " + puntosUsuario);
        lblRestantes.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblRestantes.setForeground(new Color(150, 150, 150));
        lblRestantes.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnVolver = new JButton("Volver a canjes");
        btnVolver.setPreferredSize(new Dimension(200, 45));
        btnVolver.setMaximumSize(new Dimension(200, 45));
        btnVolver.setBackground(new Color(220, 20, 60));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> mostrarCanjePuntos());

        panelExito.add(lblIcono);
        panelExito.add(Box.createVerticalStrut(20));
        panelExito.add(lblTitulo);
        panelExito.add(Box.createVerticalStrut(15));
        panelExito.add(lblProducto);
        panelExito.add(Box.createVerticalStrut(10));
        panelExito.add(lblPuntos);
        panelExito.add(Box.createVerticalStrut(10));
        panelExito.add(lblRestantes);
        panelExito.add(Box.createVerticalStrut(30));
        panelExito.add(btnVolver);

        panelContenido.add(panelExito);
        panelContenido.revalidate();
        panelContenido.repaint();
    }
    
    private void mostrarLogin() {
        mostrarPantallaLogin();
    }

    private void mostrarPantallaLogin() {
        JDialog dialog = new JDialog((Frame)null, "Iniciar Sesión", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.getContentPane().setBackground(new Color(50, 50, 50));
        dialog.setLayout(new BorderLayout());

        JPanel panelContenedor = new JPanel(new CardLayout());
        panelContenedor.setBackground(new Color(50, 50, 50));

        JPanel panelLogin = crearPanelLogin(dialog, panelContenedor);
        JPanel panelRegistro = crearPanelRegistro(dialog, panelContenedor);

        panelContenedor.add(panelLogin, "LOGIN");
        panelContenedor.add(panelRegistro, "REGISTRO");

        dialog.add(panelContenedor);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        dialog.setVisible(true);
    }

    private JPanel crearPanelLogin(JDialog dialog, JPanel panelContenedor) {
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBackground(new Color(50, 50, 50));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        JLabel lblLogo = new JLabel("CINEMARX", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelNegro = new JPanel();
        panelNegro.setBackground(new Color(20, 20, 20));
        panelNegro.setLayout(null);
        panelNegro.setPreferredSize(new Dimension(500, 400));
        panelNegro.setMaximumSize(new Dimension(500, 400));

        JLabel lblTituloLogin = new JLabel("Hola! Que bueno verte por aca.", SwingConstants.CENTER);
        lblTituloLogin.setForeground(Color.WHITE);
        lblTituloLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTituloLogin.setBounds(0, 40, 500, 30);
        panelNegro.add(lblTituloLogin);

        JLabel lblCorreoLabelLogin = new JLabel("Correo electrónico");
        lblCorreoLabelLogin.setForeground(new Color(180, 180, 180));
        lblCorreoLabelLogin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCorreoLabelLogin.setBounds(100, 100, 300, 15);
        panelNegro.add(lblCorreoLabelLogin);

        JTextField txtCorreoLogin = new JTextField();
        txtCorreoLogin.setBounds(100, 115, 300, 45);
        txtCorreoLogin.setBackground(new Color(35, 35, 35));
        txtCorreoLogin.setForeground(Color.WHITE);
        txtCorreoLogin.setCaretColor(Color.WHITE);
        txtCorreoLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCorreoLogin.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        panelNegro.add(txtCorreoLogin);

        JLabel lblPassLabelLogin = new JLabel("Contraseña");
        lblPassLabelLogin.setForeground(new Color(180, 180, 180));
        lblPassLabelLogin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPassLabelLogin.setBounds(100, 175, 300, 15);
        panelNegro.add(lblPassLabelLogin);

        JPasswordField txtPassLogin = new JPasswordField();
        txtPassLogin.setBounds(100, 190, 300, 45);
        txtPassLogin.setBackground(new Color(35, 35, 35));
        txtPassLogin.setForeground(Color.WHITE);
        txtPassLogin.setCaretColor(Color.WHITE);
        txtPassLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassLogin.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        panelNegro.add(txtPassLogin);

        JButton btnIniciar = new JButton("Iniciar Sesión");
        btnIniciar.setBounds(100, 260, 300, 45);
        btnIniciar.setBackground(new Color(220, 20, 60));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIniciar.setFocusPainted(false);
        btnIniciar.setBorderPainted(false);
        btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelNegro.add(btnIniciar);

        JButton btnRegistrarse = new JButton("Registrarse");
        btnRegistrarse.setBounds(100, 320, 300, 45);
        btnRegistrarse.setBackground(new Color(60, 60, 60));
        btnRegistrarse.setForeground(Color.WHITE);
        btnRegistrarse.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegistrarse.setFocusPainted(false);
        btnRegistrarse.setBorderPainted(false);
        btnRegistrarse.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelNegro.add(btnRegistrarse);

        panelPrincipal.add(lblLogo);
        panelPrincipal.add(Box.createVerticalStrut(20));
        panelPrincipal.add(panelNegro);

        btnRegistrarse.addActionListener(e -> {
            CardLayout cl = (CardLayout) panelContenedor.getLayout();
            cl.show(panelContenedor, "REGISTRO");
        });

        btnIniciar.addActionListener(e -> {
            String correo = txtCorreoLogin.getText().trim();
            String pass = new String(txtPassLogin.getPassword()).trim();

            if (correo.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete ambos campos.");
                return;
            }

            if (sistemaLogin.iniciarSesion(correo, pass)) {
                usuarioActual = sistemaLogin.getUsuarioActual();
                dialog.dispose();
                crearInterfazPrincipal();
                setVisible(true);
                mostrarPerfil();
            } else {
                JOptionPane.showMessageDialog(dialog, "Correo o contraseña incorrectos.");
            }
        });

        return panelPrincipal;
    }

    private JPanel crearPanelRegistro(JDialog dialog, JPanel panelContenedor) {
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBackground(new Color(50, 50, 50));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel lblLogo = new JLabel("CINEMARX", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelContenido = new JPanel();
        panelContenido.setBackground(new Color(20, 20, 20));
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel lblTitulo = new JLabel("Regístrate:", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Completa todos los campos para crear tu cuenta");
        lblSubtitulo.setForeground(new Color(180, 180, 180));
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelContenido.add(lblTitulo);
        panelContenido.add(Box.createVerticalStrut(5));
        panelContenido.add(lblSubtitulo);
        panelContenido.add(Box.createVerticalStrut(25));

        // NOMBRE
        JLabel lblNombreLabel = new JLabel("Nombre *");
        lblNombreLabel.setForeground(new Color(180, 180, 180));
        lblNombreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNombreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtNombre = new JTextField();
        txtNombre.setMaximumSize(new Dimension(400, 45));
        txtNombre.setBackground(new Color(35, 35, 35));
        txtNombre.setForeground(Color.WHITE);
        txtNombre.setCaretColor(Color.WHITE);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelContenido.add(lblNombreLabel);
        panelContenido.add(Box.createVerticalStrut(5));
        panelContenido.add(txtNombre);
        panelContenido.add(Box.createVerticalStrut(15));

        // APELLIDO
        JLabel lblApellidoLabel = new JLabel("Apellido *");
        lblApellidoLabel.setForeground(new Color(180, 180, 180));
        lblApellidoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblApellidoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtApellido = new JTextField();
        txtApellido.setMaximumSize(new Dimension(400, 45));
        txtApellido.setBackground(new Color(35, 35, 35));
        txtApellido.setForeground(Color.WHITE);
        txtApellido.setCaretColor(Color.WHITE);
        txtApellido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtApellido.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtApellido.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelContenido.add(lblApellidoLabel);
        panelContenido.add(Box.createVerticalStrut(5));
        panelContenido.add(txtApellido);
        panelContenido.add(Box.createVerticalStrut(15));

        // DNI
        JLabel lblDNILabel = new JLabel("DNI (solo números) *");
        lblDNILabel.setForeground(new Color(180, 180, 180));
        lblDNILabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDNILabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtDNI = new JTextField();
        txtDNI.setMaximumSize(new Dimension(400, 45));
        txtDNI.setBackground(new Color(35, 35, 35));
        txtDNI.setForeground(Color.WHITE);
        txtDNI.setCaretColor(Color.WHITE);
        txtDNI.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDNI.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtDNI.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelContenido.add(lblDNILabel);
        panelContenido.add(Box.createVerticalStrut(5));
        panelContenido.add(txtDNI);
        panelContenido.add(Box.createVerticalStrut(15));

        // FECHA DE NACIMIENTO
        JLabel lblFechaLabel = new JLabel("Fecha de Nacimiento (DD/MM/AAAA) *");
        lblFechaLabel.setForeground(new Color(180, 180, 180));
        lblFechaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFechaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelFechaContainer = new JPanel();
        panelFechaContainer.setLayout(new BoxLayout(panelFechaContainer, BoxLayout.X_AXIS));
        panelFechaContainer.setBackground(new Color(20, 20, 20));
        panelFechaContainer.setMaximumSize(new Dimension(400, 45));
        panelFechaContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtDia = new JTextField();
        txtDia.setPreferredSize(new Dimension(70, 45));
        txtDia.setMaximumSize(new Dimension(70, 45));
        txtDia.setBackground(new Color(35, 35, 35));
        txtDia.setForeground(Color.WHITE);
        txtDia.setCaretColor(Color.WHITE);
        txtDia.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDia.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtDia.setHorizontalAlignment(JTextField.CENTER);

        JLabel lblBarra1 = new JLabel(" / ");
        lblBarra1.setForeground(Color.WHITE);
        lblBarra1.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtMes = new JTextField();
        txtMes.setPreferredSize(new Dimension(70, 45));
        txtMes.setMaximumSize(new Dimension(70, 45));
        txtMes.setBackground(new Color(35, 35, 35));
        txtMes.setForeground(Color.WHITE);
        txtMes.setCaretColor(Color.WHITE);
        txtMes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtMes.setHorizontalAlignment(JTextField.CENTER);

        JLabel lblBarra2 = new JLabel(" / ");
        lblBarra2.setForeground(Color.WHITE);
        lblBarra2.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextField txtAnio = new JTextField();
        txtAnio.setPreferredSize(new Dimension(100, 45));
        txtAnio.setMaximumSize(new Dimension(100, 45));
        txtAnio.setBackground(new Color(35, 35, 35));
        txtAnio.setForeground(Color.WHITE);
        txtAnio.setCaretColor(Color.WHITE);
        txtAnio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtAnio.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtAnio.setHorizontalAlignment(JTextField.CENTER);

        panelFechaContainer.add(txtDia);
        panelFechaContainer.add(lblBarra1);
        panelFechaContainer.add(txtMes);
        panelFechaContainer.add(lblBarra2);
        panelFechaContainer.add(txtAnio);
        panelFechaContainer.add(Box.createHorizontalGlue());

        panelContenido.add(lblFechaLabel);
        panelContenido.add(Box.createVerticalStrut(5));
        panelContenido.add(panelFechaContainer);
        panelContenido.add(Box.createVerticalStrut(15));

        // CORREO
        JLabel lblCorreoLabel = new JLabel("Correo electrónico (@gmail.com) *");
        lblCorreoLabel.setForeground(new Color(180, 180, 180));
        lblCorreoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCorreoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtCorreo = new JTextField();
        txtCorreo.setMaximumSize(new Dimension(400, 45));
        txtCorreo.setBackground(new Color(35, 35, 35));
        txtCorreo.setForeground(Color.WHITE);
        txtCorreo.setCaretColor(Color.WHITE);
        txtCorreo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCorreo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelContenido.add(lblCorreoLabel);
        panelContenido.add(Box.createVerticalStrut(5));
        panelContenido.add(txtCorreo);
        panelContenido.add(Box.createVerticalStrut(15));

        // CONTRASEÑA
        JLabel lblPassLabel = new JLabel("Contraseña *");
        lblPassLabel.setForeground(new Color(180, 180, 180));
        lblPassLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPassLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField txtPass = new JPasswordField();
        txtPass.setMaximumSize(new Dimension(400, 45));
        txtPass.setBackground(new Color(35, 35, 35));
        txtPass.setForeground(Color.WHITE);
        txtPass.setCaretColor(Color.WHITE);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelContenido.add(lblPassLabel);
        panelContenido.add(Box.createVerticalStrut(5));
        panelContenido.add(txtPass);
        panelContenido.add(Box.createVerticalStrut(15));

        // CONFIRMAR CONTRASEÑA
        JLabel lblConfirmarLabel = new JLabel("Confirmar Contraseña *");
        lblConfirmarLabel.setForeground(new Color(180, 180, 180));
        lblConfirmarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblConfirmarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField txtConfirmar = new JPasswordField();
        txtConfirmar.setMaximumSize(new Dimension(400, 45));
        txtConfirmar.setBackground(new Color(35, 35, 35));
        txtConfirmar.setForeground(Color.WHITE);
        txtConfirmar.setCaretColor(Color.WHITE);
        txtConfirmar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtConfirmar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtConfirmar.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelContenido.add(lblConfirmarLabel);
        panelContenido.add(Box.createVerticalStrut(5));
        panelContenido.add(txtConfirmar);
        panelContenido.add(Box.createVerticalStrut(25));

        // BOTONES
        JButton btnCrear = new JButton("Crear cuenta");
        btnCrear.setMaximumSize(new Dimension(400, 50));
        btnCrear.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCrear.setBackground(new Color(220, 20, 60));
        btnCrear.setForeground(Color.WHITE);
        btnCrear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCrear.setFocusPainted(false);
        btnCrear.setBorderPainted(false);
        btnCrear.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnVolverReg = new JButton("Volver");
        btnVolverReg.setMaximumSize(new Dimension(400, 50));
        btnVolverReg.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVolverReg.setBackground(new Color(60, 60, 60));
        btnVolverReg.setForeground(Color.WHITE);
        btnVolverReg.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolverReg.setFocusPainted(false);
        btnVolverReg.setBorderPainted(false);
        btnVolverReg.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelContenido.add(btnCrear);
        panelContenido.add(Box.createVerticalStrut(10));
        panelContenido.add(btnVolverReg);

        JScrollPane scrollPane = new JScrollPane(panelContenido);
        scrollPane.setPreferredSize(new Dimension(500, 600));
        scrollPane.setMaximumSize(new Dimension(500, 600));
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(20, 20, 20));
        scrollPane.getViewport().setBackground(new Color(20, 20, 20));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panelPrincipal.add(lblLogo);
        panelPrincipal.add(Box.createVerticalStrut(15));
        panelPrincipal.add(scrollPane);

        btnVolverReg.addActionListener(e -> {
            CardLayout cl = (CardLayout) panelContenedor.getLayout();
            cl.show(panelContenedor, "LOGIN");
        });

        btnCrear.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String dniStr = txtDNI.getText().trim();
            String dia = txtDia.getText().trim();
            String mes = txtMes.getText().trim();
            String anio = txtAnio.getText().trim();
            String correo = txtCorreo.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            String confirmar = new String(txtConfirmar.getPassword()).trim();

            if (nombre.isEmpty() || apellido.isEmpty() || dniStr.isEmpty() || 
                dia.isEmpty() || mes.isEmpty() || anio.isEmpty() || 
                correo.isEmpty() || pass.isEmpty() || confirmar.isEmpty()) {
                return;
            }

            int DNI;
            try {
                DNI = Integer.parseInt(dniStr);
                if (DNI <= 0 || dniStr.length() != 8) {
                    return;
                }
            } catch (NumberFormatException ex) {
                return;
            }

            int diaInt, mesInt, anioInt;
            try {
                diaInt = Integer.parseInt(dia);
                mesInt = Integer.parseInt(mes);
                anioInt = Integer.parseInt(anio);

                if (diaInt < 1 || diaInt > 31) {
                    return;
                }
                if (mesInt < 1 || mesInt > 12) {
                    return;
                }
                if (anioInt < 1920 || anioInt > 2024) {
                    return;
                }

                if (mesInt == 2) {
                    boolean esBisiesto = (anioInt % 4 == 0 && anioInt % 100 != 0) || (anioInt % 400 == 0);
                    if (diaInt > (esBisiesto ? 29 : 28)) {
                        return;
                    }
                } else if (mesInt == 4 || mesInt == 6 || mesInt == 9 || mesInt == 11) {
                    if (diaInt > 30) {
                        return;
                    }
                }
            } catch (NumberFormatException ex) {
                return;
            }

            java.sql.Date fechaNacimiento;
            try {
                String fechaStr = String.format("%04d-%02d-%02d", anioInt, mesInt, diaInt);
                fechaNacimiento = java.sql.Date.valueOf(fechaStr);
            } catch (IllegalArgumentException ex) {
                return;
            }

            if (!pass.equals(confirmar)) {
                return;
            }

            if (!correo.toLowerCase().endsWith("@gmail.com")) {
                return;
            }

            if (sistemaLogin.registrarUsuario(nombre, apellido, correo, pass, DNI, fechaNacimiento)) {
                txtNombre.setText("");
                txtApellido.setText("");
                txtDNI.setText("");
                txtDia.setText("");
                txtMes.setText("");
                txtAnio.setText("");
                txtCorreo.setText("");
                txtPass.setText("");
                txtConfirmar.setText("");

                CardLayout cl = (CardLayout) panelContenedor.getLayout();
                cl.show(panelContenedor, "LOGIN");
            }
        });

        return panelPrincipal;
    }

    private void mostrarMensajeError(String mensaje, JLabel[] lblMensajeError, JPanel panelBotones) {
        if (lblMensajeError[0] == null) {
            lblMensajeError[0] = new JLabel();
            lblMensajeError[0].setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblMensajeError[0].setForeground(new Color(244, 67, 54));
            lblMensajeError[0].setAlignmentX(Component.LEFT_ALIGNMENT);
            lblMensajeError[0].setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            panelBotones.add(lblMensajeError[0]);
        }
        lblMensajeError[0].setText("⚠ " + mensaje);
        lblMensajeError[0].setVisible(true);
        panelBotones.revalidate();
        panelBotones.repaint();

        Timer timer = new Timer(5000, evt -> {
            if (lblMensajeError[0] != null) {
                lblMensajeError[0].setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}