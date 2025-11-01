/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CINEMARX.M4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gaels
 */
public class PantallaSeleccionButacas extends JPanel {
    private DatabaseService dbService;
    private Pelicula pelicula;
    private Funcion funcion;
    private List<Butaca> butacas;
    private List<Butaca> butacasSeleccionadas = new ArrayList<>();
    private JPanel panelButacas;
    
    private static final Color COLOR_DISPONIBLE = Color.WHITE;
    private static final Color COLOR_OCUPADA = new Color(100, 100, 100);
    private static final Color COLOR_SELECCIONADA = new Color(220, 60, 60);
    
    public PantallaSeleccionButacas(DatabaseService dbService, Pelicula pelicula, Funcion funcion) {
        this.dbService = dbService;
        this.pelicula = pelicula;
        this.funcion = funcion;
        this.butacas = dbService.obtenerButacasSala(funcion.getSalaId(), funcion.getId());
        
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));
        
        add(crearHeader(), BorderLayout.NORTH);
        add(crearContenido(), BorderLayout.CENTER);
    }
    
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45, 45, 45));
        header.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        JLabel logo = new JLabel("CINEMAR X");
        logo.setFont(new Font("Arial", Font.BOLD, 32));
        logo.setForeground(Color.WHITE);
        logo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 0));
        menu.setBackground(new Color(45, 45, 45));
        
        String[] opciones = {"PELICULAS", "BUFFET", "MEMBRESIA"};
        for (String opcion : opciones) {
            JLabel item = new JLabel(opcion);
            item.setFont(new Font("Arial", Font.PLAIN, 16));
            item.setForeground(Color.WHITE);
            menu.add(item);
        }
        
        JLabel iconoUsuario = new JLabel("👤");
        iconoUsuario.setFont(new Font("Arial", Font.PLAIN, 24));
        iconoUsuario.setForeground(Color.WHITE);
        menu.add(iconoUsuario);
        
        header.add(logo, BorderLayout.WEST);
        header.add(menu, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel crearContenido() {
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(new Color(45, 45, 45));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        
        // Título
        JLabel titulo = new JLabel("Selecciona tu butaca");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Pantalla
        JPanel pantalla = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Dibujar arco de la pantalla
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(220, 60, 60),
                    0, height, new Color(180, 40, 40)
                );
                g2d.setPaint(gradient);
                
                int[] xPoints = {0, width, width - 50, 50};
                int[] yPoints = {height, height, 0, 0};
                g2d.fillPolygon(xPoints, yPoints, 4);
            }
        };
        pantalla.setPreferredSize(new Dimension(600, 50));
        pantalla.setBackground(new Color(45, 45, 45));
        
        JLabel lblPantalla = new JLabel("PANTALLA", SwingConstants.CENTER);
        lblPantalla.setFont(new Font("Arial", Font.BOLD, 14));
        lblPantalla.setForeground(Color.WHITE);
        pantalla.add(lblPantalla);
        
        // Panel de butacas
        panelButacas = new JPanel(new GridBagLayout());
        panelButacas.setBackground(new Color(45, 45, 45));
        crearButacas();
        
        // Leyenda
        JPanel leyenda = crearLeyenda();
        
        // Botón siguiente
        JButton btnSiguiente = new JButton("SIGUIENTE");
        btnSiguiente.setFont(new Font("Arial", Font.BOLD, 16));
        btnSiguiente.setForeground(Color.WHITE);
        btnSiguiente.setBackground(new Color(220, 60, 60));
        btnSiguiente.setBorder(BorderFactory.createEmptyBorder(12, 35, 12, 35));
        btnSiguiente.setFocusPainted(false);
        btnSiguiente.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSiguiente.addActionListener(e -> {
            if (butacasSeleccionadas.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Por favor seleccione al menos una butaca",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Butacas seleccionadas: " + butacasSeleccionadas.size(),
                    "Confirmación",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setBackground(new Color(45, 45, 45));
        panelBoton.add(btnSiguiente);
        
        // Ensamblar todo
        JPanel centro = new JPanel(new BorderLayout(0, 30));
        centro.setBackground(new Color(45, 45, 45));
        centro.add(pantalla, BorderLayout.NORTH);
        centro.add(panelButacas, BorderLayout.CENTER);
        
        JPanel inferior = new JPanel(new BorderLayout(0, 20));
        inferior.setBackground(new Color(45, 45, 45));
        inferior.add(leyenda, BorderLayout.CENTER);
        inferior.add(panelBoton, BorderLayout.SOUTH);
        
        contenido.add(titulo, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(inferior, BorderLayout.SOUTH);
        
        return contenido;
    }
    
    private void crearButacas() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        
        // Determinar dimensiones de la sala (asumiendo una sala típica)
        int filas = 12;
        int columnasLaterales = 3;
        int columnasCentro = 14;
        
        for (int fila = 0; fila < filas; fila++) {
            int colActual = 0;
            
            // Butacas laterales izquierdas
            for (int col = 0; col < columnasLaterales; col++) {
                Butaca butaca = obtenerButaca(fila, colActual);
                if (butaca != null) {
                    JButton btnButaca = crearBotonButaca(butaca);
                    gbc.gridx = colActual;
                    gbc.gridy = fila;
                    panelButacas.add(btnButaca, gbc);
                }
                colActual++;
            }
            
            // Espacio central
            gbc.gridx = colActual;
            gbc.gridy = fila;
            panelButacas.add(Box.createHorizontalStrut(20), gbc);
            colActual++;
            
            // Butacas centrales
            for (int col = 0; col < columnasCentro; col++) {
                Butaca butaca = obtenerButaca(fila, colActual);
                if (butaca != null) {
                    JButton btnButaca = crearBotonButaca(butaca);
                    gbc.gridx = colActual;
                    gbc.gridy = fila;
                    panelButacas.add(btnButaca, gbc);
                }
                colActual++;
            }
            
            // Espacio central
            gbc.gridx = colActual;
            gbc.gridy = fila;
            panelButacas.add(Box.createHorizontalStrut(20), gbc);
            colActual++;
            
            // Butacas laterales derechas
            for (int col = 0; col < columnasLaterales; col++) {
                Butaca butaca = obtenerButaca(fila, colActual);
                if (butaca != null) {
                    JButton btnButaca = crearBotonButaca(butaca);
                    gbc.gridx = colActual;
                    gbc.gridy = fila;
                    panelButacas.add(btnButaca, gbc);
                }
                colActual++;
            }
        }
    }
    
    private Butaca obtenerButaca(int fila, int columna) {
        for (Butaca butaca : butacas) {
            if (butaca.getFila() == fila && butaca.getColumna() == columna) {
                return butaca;
            }
        }
        return new Butaca(fila, columna, false);
    }
    
    private JButton crearBotonButaca(Butaca butaca) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (butaca.isOcupada()) {
            btn.setBackground(COLOR_OCUPADA);
            btn.setBorder(BorderFactory.createLineBorder(COLOR_OCUPADA.darker(), 1));
            btn.setEnabled(false);
        } else {
            btn.setBackground(COLOR_DISPONIBLE);
            btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            
            btn.addActionListener(e -> {
                if (butaca.isSeleccionada()) {
                    butaca.setSeleccionada(false);
                    btn.setBackground(COLOR_DISPONIBLE);
                    btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                    butacasSeleccionadas.remove(butaca);
                } else {
                    butaca.setSeleccionada(true);
                    btn.setBackground(COLOR_SELECCIONADA);
                    btn.setBorder(BorderFactory.createLineBorder(COLOR_SELECCIONADA.darker(), 2));
                    butacasSeleccionadas.add(butaca);
                }
            });
        }
        
        return btn;
    }
    
    private JPanel crearLeyenda() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        leyenda.setBackground(new Color(45, 45, 45));
        leyenda.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        leyenda.add(crearItemLeyenda("Disponible", COLOR_DISPONIBLE, Color.LIGHT_GRAY));
        leyenda.add(crearItemLeyenda("No disponible", COLOR_OCUPADA, COLOR_OCUPADA.darker()));
        leyenda.add(crearItemLeyenda("Seleccionado", COLOR_SELECCIONADA, COLOR_SELECCIONADA.darker()));
        
        return leyenda;
    }
    
    private JPanel crearItemLeyenda(String texto, Color colorFondo, Color colorBorde) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        item.setBackground(new Color(45, 45, 45));
        
        JPanel cuadro = new JPanel();
        cuadro.setPreferredSize(new Dimension(25, 25));
        cuadro.setBackground(colorFondo);
        cuadro.setBorder(BorderFactory.createLineBorder(colorBorde, 1));
        
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(Color.WHITE);
        
        item.add(cuadro);
        item.add(label);
        
        return item;
    }
}