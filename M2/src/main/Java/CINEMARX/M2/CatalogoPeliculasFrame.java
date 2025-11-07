/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CINEMARX.M2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

// Imports de Modelo y DAO
import CINEMARX.M2.Pelicula;
import CINEMARX.M2.PeliculaDAO;

// Imports para imágenes
import javax.imageio.ImageIO;
import java.net.URL;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.IOException;

// --- NUEVO: Imports para CardLayout, Sugerencias y Eventos ---
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
// -----------------------------------------------------------


public class CatalogoPeliculasFrame extends JFrame {
    
    // Colores del tema Cinemarx
    private static final Color COLOR_FONDO = new Color(24, 24, 24);
    private static final Color COLOR_HEADER = new Color(9, 9, 9);
    private static final Color COLOR_CARD = new Color(40, 40, 40);
    private static final Color COLOR_ROJO = new Color(229, 9, 20);
    private static final Color COLOR_TEXTO = new Color(230, 230, 230);
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(160, 160, 160);
    private static final Color COLOR_INPUT = new Color(50, 50, 50);
    private static final Color COLOR_BORDER = new Color(80, 80, 80);
    
    // --- NUEVOS COMPONENTES ---
    // Componentes de Búsqueda
    private JTextField txtBusqueda;
    private JButton btnLimpiarBusqueda;
    private JPopupMenu sugerenciasPopup;
    private boolean actualizandoPorSugerencia = false;
    
    // Componentes de Layout
    private CardLayout cardLayout;
    private JPanel panelContenedor; // Panel principal con CardLayout
    private JPanel panelPeliculasSecciones; // Panel vertical para las filas
    private JPanel panelResultadosBusqueda; // Panel grid para resultados
    
    // Conexión a la BD
    private PeliculaDAO peliculaDAO; 
    
    
    public CatalogoPeliculasFrame() {
        inicializarDatos();
        configurarVentana();
        crearComponentes(); // Esto crea la UI base
        
        // Cargamos las secciones después de que la ventana es visible
        SwingUtilities.invokeLater(() -> cargarSecciones());
    }
    
    private void inicializarDatos() {
        this.peliculaDAO = new PeliculaDAO();
        this.sugerenciasPopup = new JPopupMenu();
        // Estilo del Popup
        this.sugerenciasPopup.setBackground(COLOR_CARD);
        this.sugerenciasPopup.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
    }
    
    private void configurarVentana() {
        setTitle("CinemarX - Catálogo de Películas");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);
    }
    
    private void crearComponentes() {
        add(crearHeader(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
    }
    
    /**
     * --- MODIFICADO ---
     * El Header ahora contiene solo el logo y el menú.
     * La búsqueda la pondremos dentro del panel central.
     */
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        JPanel panelLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelLogo.setBackground(COLOR_HEADER);
        
        JLabel lblCinemar = new JLabel("CINEMAR");
        lblCinemar.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblCinemar.setForeground(COLOR_TEXTO);
        
        JLabel lblX = new JLabel("X");
        lblX.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblX.setForeground(COLOR_ROJO);
        lblX.setBorder(new EmptyBorder(0, 2, 0, 0));
        
        panelLogo.add(lblCinemar);
        panelLogo.add(lblX);
        
        JPanel panelMenu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        panelMenu.setBackground(COLOR_HEADER);
        
        String[] opciones = {"PELÍCULAS", "BUFFET", "MEMBRESÍA"};
        for (String opcion : opciones) {
            JLabel lblMenu = new JLabel(opcion);
            lblMenu.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblMenu.setForeground(COLOR_TEXTO_SECUNDARIO);
            lblMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblMenu.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { lblMenu.setForeground(COLOR_ROJO); }
                @Override public void mouseExited(MouseEvent e) { lblMenu.setForeground(COLOR_TEXTO_SECUNDARIO); }
            });
            panelMenu.add(lblMenu);
        }
        
        JLabel iconoUsuario = new JLabel("👤");
        iconoUsuario.setFont(new Font("SansSerif", Font.PLAIN, 24));
        iconoUsuario.setOpaque(true);
        iconoUsuario.setBackground(Color.WHITE);
        iconoUsuario.setPreferredSize(new Dimension(40, 40));
        iconoUsuario.setHorizontalAlignment(SwingConstants.CENTER);
        panelMenu.add(iconoUsuario);
        
        header.add(panelLogo, BorderLayout.WEST);
        header.add(panelMenu, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * --- MODIFICADO ---
     * Crea el panel central que contiene la barra de búsqueda y el CardLayout
     */
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. Panel de Búsqueda (Arriba)
        panel.add(crearPanelBusqueda(), BorderLayout.NORTH);
        
        // 2. Panel Contenedor (Centro) con CardLayout
        cardLayout = new CardLayout();
        panelContenedor = new JPanel(cardLayout);
        panelContenedor.setBackground(COLOR_FONDO);

        // Panel 1: "SECCIONES" (Vertical con filas horizontales)
        panelPeliculasSecciones = new JPanel();
        panelPeliculasSecciones.setLayout(new BoxLayout(panelPeliculasSecciones, BoxLayout.Y_AXIS));
        panelPeliculasSecciones.setBackground(COLOR_FONDO);
        
        JScrollPane scrollSecciones = new JScrollPane(panelPeliculasSecciones);
        scrollSecciones.setBorder(null);
        scrollSecciones.getVerticalScrollBar().setUnitIncrement(16);
        scrollSecciones.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Panel 2: "BUSQUEDA" (Grid/Flow con resultados)
        panelResultadosBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panelResultadosBusqueda.setBackground(COLOR_FONDO);
        
        JScrollPane scrollBusqueda = new JScrollPane(panelResultadosBusqueda);
        scrollBusqueda.setBorder(null);
        scrollBusqueda.getVerticalScrollBar().setUnitIncrement(16);

        // Añadir vistas al CardLayout
        panelContenedor.add(scrollSecciones, "SECCIONES");
        panelContenedor.add(scrollBusqueda, "BUSQUEDA");
        
        panel.add(panelContenedor, BorderLayout.CENTER);
        
        // Mostrar "SECCIONES" por defecto
        cardLayout.show(panelContenedor, "SECCIONES");
        
        return panel;
    }

    /**
     * --- NUEVO MÉTODO ---
     * Crea el panel con el JTextField de búsqueda y el botón de limpiar.
     */
    private JPanel crearPanelBusqueda() {
        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBusqueda.setBackground(COLOR_FONDO);
        
        txtBusqueda = new JTextField("🔍 Buscar película...");
        CinemarXEstilos.aplicarEstiloTextField(txtBusqueda);
        
        // Efecto "Placeholder"
        txtBusqueda.setForeground(COLOR_TEXTO_SECUNDARIO);
        txtBusqueda.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtBusqueda.getText().equals("🔍 Buscar película...")) {
                    txtBusqueda.setText("");
                    txtBusqueda.setForeground(COLOR_TEXTO);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtBusqueda.getText().isEmpty()) {
                    txtBusqueda.setText("🔍 Buscar película...");
                    txtBusqueda.setForeground(COLOR_TEXTO_SECUNDARIO);
                }
            }
        });

        // Evento al presionar "Enter"
        txtBusqueda.addActionListener(e -> {
            sugerenciasPopup.setVisible(false);
            ejecutarBusqueda(txtBusqueda.getText());
        });
        
        // Evento al escribir (para sugerencias)
        txtBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { manejarSugerencias(); }
            @Override public void removeUpdate(DocumentEvent e) { manejarSugerencias(); }
            @Override public void changedUpdate(DocumentEvent e) { manejarSugerencias(); }
        });
        
        // Botón Limpiar ("X")
        btnLimpiarBusqueda = new JButton("X");
        CinemarXEstilos.aplicarEstiloBoton(btnLimpiarBusqueda, COLOR_ROJO);
        btnLimpiarBusqueda.setPreferredSize(new Dimension(45, 45));
        btnLimpiarBusqueda.setVisible(false); // Oculto por defecto
        btnLimpiarBusqueda.addActionListener(e -> limpiarBusqueda());
        
        panelBusqueda.add(txtBusqueda, BorderLayout.CENTER);
        panelBusqueda.add(btnLimpiarBusqueda, BorderLayout.EAST);
        
        return panelBusqueda;
    }

    
    /**
     * --- NUEVA LÓGICA DE BÚSQUEDA ---
     */
     
    private void manejarSugerencias() {
        if (actualizandoPorSugerencia) {
            return; // Evitar bucle infinito al hacer clic en sugerencia
        }
        
        String termino = txtBusqueda.getText();
        
        if (termino.isEmpty() || termino.equals("🔍 Buscar película...") || termino.length() < 2) {
            sugerenciasPopup.setVisible(false);
            return;
        }
        
        // Llamada al DAO en un hilo separado para no bloquear la UI
        SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return peliculaDAO.obtenerTitulosQueCoinciden(termino, 5); // Límite de 5 sugerencias
            }
            
            @Override
            protected void done() {
                try {
                    List<String> titulos = get();
                    sugerenciasPopup.removeAll();
                    
                    if (titulos.isEmpty()) {
                        sugerenciasPopup.setVisible(false);
                        return;
                    }
                    
                    for (String titulo : titulos) {
                        JMenuItem item = new JMenuItem(titulo);
                        item.setBackground(COLOR_CARD);
                        item.setForeground(COLOR_TEXTO);
                        item.setFont(new Font("SansSerif", Font.PLAIN, 14));
                        item.setBorder(new EmptyBorder(8, 10, 8, 10));
                        
                        item.addActionListener(e -> {
                            actualizandoPorSugerencia = true; // Marcar
                            txtBusqueda.setText(titulo);
                            sugerenciasPopup.setVisible(false);
                            ejecutarBusqueda(titulo);
                            actualizandoPorSugerencia = false; // Desmarcar
                        });
                        sugerenciasPopup.add(item);
                    }
                    
                    sugerenciasPopup.show(txtBusqueda, 0, txtBusqueda.getHeight());
                    txtBusqueda.requestFocusInWindow();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void ejecutarBusqueda(String termino) {
        if (termino.isEmpty() || termino.equals("🔍 Buscar película...")) {
            limpiarBusqueda();
            return;
        }
        
        // Llamada al DAO
        List<Pelicula> resultados = peliculaDAO.buscarPorTitulo(termino);
        
        // Mostrar resultados en el panel de búsqueda
        panelResultadosBusqueda.removeAll();
        
        if (resultados.isEmpty()) {
            JLabel noResultados = new JLabel("No se encontraron resultados para '" + termino + "'");
            noResultados.setFont(new Font("SansSerif", Font.BOLD, 18));
            noResultados.setForeground(COLOR_TEXTO_SECUNDARIO);
            panelResultadosBusqueda.add(noResultados);
        } else {
            for (Pelicula peli : resultados) {
                panelResultadosBusqueda.add(crearCardPelicula(peli));
            }
        }
        
        panelResultadosBusqueda.revalidate();
        panelResultadosBusqueda.repaint();
        
        // Mostrar botón "X" y cambiar de vista
        btnLimpiarBusqueda.setVisible(true);
        cardLayout.show(panelContenedor, "BUSQUEDA");
    }
    
    private void limpiarBusqueda() {
        txtBusqueda.setText("🔍 Buscar película...");
        txtBusqueda.setForeground(COLOR_TEXTO_SECUNDARIO);
        btnLimpiarBusqueda.setVisible(false);
        sugerenciasPopup.setVisible(false);
        
        // Volver a la vista de secciones
        cardLayout.show(panelContenedor, "SECCIONES");
    }

    
    /**
     * --- LÓGICA DE SECCIONES (Funcionalidad 3) ---
     * (Sin cambios respecto al paso anterior)
     */
    
    private void cargarSecciones() {
        panelPeliculasSecciones.removeAll(); // Limpia
        
        int LIMITE_POR_FILA = 10;
        
        // --- NUEVA FILA/FUNCIONALIDAD: Películas más taquilleras 
        // Se llama al método del DAO
        List<Pelicula> masTaquilleras = peliculaDAO.obtenerPeliculasMasTaquilleras(LIMITE_POR_FILA);
        if (!masTaquilleras.isEmpty()) {
            panelPeliculasSecciones.add(crearFilaSeccion("🏆 Películas más taquilleras", masTaquilleras));
            panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30))); // Espacio vertical
        }
        // ------------------------------------------
        
        // Fila 1: En Cartelera
        List<Pelicula> enCartelera = peliculaDAO.obtenerPeliculasConFiltro(null, "En Cartelera", LIMITE_POR_FILA);
        if (!enCartelera.isEmpty()) {
            panelPeliculasSecciones.add(crearFilaSeccion("🎬 En Cartelera", enCartelera));
            panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30)));
        }
        
        // Fila 2: Próximamente
        List<Pelicula> proximamente = peliculaDAO.obtenerPeliculasConFiltro(null, "Próximamente", LIMITE_POR_FILA);
        if (!proximamente.isEmpty()) {
            panelPeliculasSecciones.add(crearFilaSeccion("🍿 Próximamente", proximamente));
            panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30)));
        }
        
        // Fila 3: Acción
        List<Pelicula> accion = peliculaDAO.obtenerPeliculasConFiltro("Acción", "En Cartelera", LIMITE_POR_FILA);
        if (!accion.isEmpty()) {
            panelPeliculasSecciones.add(crearFilaSeccion("💥 Acción", accion));
            panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30)));
        }
        
        // Fila 4: Terror
        List<Pelicula> terror = peliculaDAO.obtenerPeliculasConFiltro("Terror", "En Cartelera", LIMITE_POR_FILA);
        if (!terror.isEmpty()) {
            panelPeliculasSecciones.add(crearFilaSeccion("👻 Terror", terror));
            panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30)));
        }
        
        // Fila 5: Comedia
        List<Pelicula> comedia = peliculaDAO.obtenerPeliculasConFiltro("Comedia", "En Cartelera", LIMITE_POR_FILA);
        if (!comedia.isEmpty()) {
            panelPeliculasSecciones.add(crearFilaSeccion("😂 Comedia", comedia));
            panelPeliculasSecciones.add(Box.createRigidArea(new Dimension(0, 30)));
        }
        
        panelPeliculasSecciones.revalidate();
        panelPeliculasSecciones.repaint();
    }
    
    private JPanel crearFilaSeccion(String titulo, List<Pelicula> peliculas) {
        JPanel panelFila = new JPanel(new BorderLayout(0, 10));
        panelFila.setBackground(COLOR_FONDO);
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(COLOR_TEXTO);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 5, 0));
        panelFila.add(lblTitulo, BorderLayout.NORTH);
        
        JPanel panelTarjetas = new JPanel();
        panelTarjetas.setLayout(new BoxLayout(panelTarjetas, BoxLayout.X_AXIS));
        panelTarjetas.setBackground(COLOR_FONDO);
        
        for (Pelicula peli : peliculas) {
            panelTarjetas.add(crearCardPelicula(peli));
            panelTarjetas.add(Box.createRigidArea(new Dimension(15, 0)));
        }
        
        JScrollPane scrollTarjetas = new JScrollPane(panelTarjetas);
        scrollTarjetas.setBorder(null);
        scrollTarjetas.setBackground(COLOR_FONDO);
        scrollTarjetas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollTarjetas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTarjetas.getHorizontalScrollBar().setUnitIncrement(16);
        
        scrollTarjetas.setMinimumSize(new Dimension(500, 370));
        scrollTarjetas.setPreferredSize(new Dimension(1100, 370));
        scrollTarjetas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 370));
        
        panelFila.add(scrollTarjetas, BorderLayout.CENTER);
        
        return panelFila;
    }
    
    
    /**
     * --- SIN CAMBIOS ---
     * Este método es perfectamente reutilizable tanto para las filas 
     * como para la cuadrícula de resultados de búsqueda.
     */
    private JPanel crearCardPelicula(Pelicula peli) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        card.setPreferredSize(new Dimension(200, 350));
        card.setMinimumSize(new Dimension(200, 350));
        card.setMaximumSize(new Dimension(200, 350));
        
        final int POSTER_WIDTH = 200;
        final int POSTER_HEIGHT = 280;
        
        JLabel lblPoster = new JLabel();
        lblPoster.setPreferredSize(new Dimension(POSTER_WIDTH, POSTER_HEIGHT));
        lblPoster.setBackground(new Color(60, 60, 60));
        lblPoster.setOpaque(true);
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblPoster.setText("Cargando...");
        
        String urlImagen = peli.getImagen();
        String tituloPeli = peli.getTitulo();

        if (urlImagen != null && !urlImagen.trim().isEmpty()) {
            SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    try {
                        URL url = new URL(urlImagen);
                        Image image = ImageIO.read(url);
                        if (image != null) {
                            Image scaledImage = image.getScaledInstance(POSTER_WIDTH, POSTER_HEIGHT, Image.SCALE_SMOOTH);
                            return new ImageIcon(scaledImage);
                        }
                    } catch (IOException e) {
                        System.err.println("Error al cargar imagen: " + urlImagen + " | " + e.getMessage());
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        ImageIcon imageIcon = get();
                        if (imageIcon != null) {
                            lblPoster.setIcon(imageIcon);
lblPoster.setText(null);
                        } else {
                            lblPoster.setText(String.valueOf(tituloPeli.charAt(0)));
                            lblPoster.setFont(new Font("SansSerif", Font.BOLD, 72));
                        }
                    } catch (Exception e) {
                        lblPoster.setText(String.valueOf(tituloPeli.charAt(0)));
                        lblPoster.setFont(new Font("SansSerif", Font.BOLD, 72));
                    }
                }
            };
            worker.execute();
        } else {
            lblPoster.setText(String.valueOf(tituloPeli.charAt(0)));
            lblPoster.setFont(new Font("SansSerif", Font.BOLD, 72));
        }
        
        JLabel badge = new JLabel(peli.estaEnCartelera() ? "EN CARTELERA" : "PRÓXIMAMENTE");
        badge.setFont(new Font("SansSerif", Font.BOLD, 9));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(peli.estaEnCartelera() ? COLOR_ROJO : new Color(234, 179, 8));
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        topPanel.add(badge);
        
        JPanel posterWrapper = new JPanel(new BorderLayout());
        posterWrapper.add(lblPoster, BorderLayout.CENTER);
        posterWrapper.add(topPanel, BorderLayout.NORTH);
        
        JPanel info = new JPanel(new BorderLayout());
        info.setBackground(COLOR_CARD);
        info.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitulo = new JLabel("<html>" + peli.getTitulo() + "</html>");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitulo.setForeground(COLOR_TEXTO);
        
        JLabel lblFormato = new JLabel(peli.getClasificacionEdad());
        lblFormato.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblFormato.setForeground(COLOR_TEXTO_SECUNDARIO);
        
        info.add(lblTitulo, BorderLayout.NORTH);
        info.add(lblFormato, BorderLayout.SOUTH);
        
        card.add(posterWrapper, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);
        
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBorder(BorderFactory.createLineBorder(COLOR_ROJO, 2)); }
            @Override public void mouseExited(MouseEvent e) { card.setBorder(BorderFactory.createLineBorder(COLOR_BORDER)); }
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(card, 
                    "Película: " + peli.getTitulo() + "\n" +
                    "Género: " + peli.getGenero() + "\n" +
                    "Clasificación: " + peli.getClasificacionEdad(),
                    "Detalle de Película",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        
        return card;
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CatalogoPeliculasFrame().setVisible(true);
        });
    }
}