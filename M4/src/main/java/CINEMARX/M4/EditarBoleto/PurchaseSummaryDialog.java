package CINEMARX.M4.EditarBoleto;

import CINEMARX.M4.CustomDialog;
import CINEMARX.M4.StyledButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseSummaryDialog extends JDialog {
    private int idComprobante;
    private JPanel ticketsPanel;
    private JPanel productsPanel;
    private JLabel totalLabel;
    private List<TicketInfo> tickets;

    public PurchaseSummaryDialog(Frame owner, int idComprobante) {
        super(owner, true);
        this.idComprobante = idComprobante;
        this.tickets = new ArrayList<>();

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(800, 600);
        setLocationRelativeTo(owner);

        initComponents();
        loadTicketData();
        loadProductData();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(31, 31, 31));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Resumen de tu compra");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content Panel that will be scrollable
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        ticketsPanel = new JPanel();
        ticketsPanel.setOpaque(false);
        ticketsPanel.setLayout(new BoxLayout(ticketsPanel, BoxLayout.Y_AXIS));
        contentPanel.add(ticketsPanel);

        contentPanel.add(Box.createVerticalStrut(20));

        productsPanel = new JPanel();
        productsPanel.setOpaque(false);
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        contentPanel.add(productsPanel);

        contentPanel.add(Box.createVerticalStrut(40));

        // Separator
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(64, 64, 64));
        separator.setBackground(new Color(64, 64, 64));
        contentPanel.add(separator);

        contentPanel.add(Box.createVerticalStrut(40));

        // Total
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        
        JLabel totalTextLabel = new JLabel("Total");
        totalTextLabel.setForeground(Color.WHITE);
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalPanel.add(totalTextLabel, BorderLayout.WEST);

        totalLabel = new JLabel("$0.00");
        totalLabel.setForeground(Color.WHITE);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        totalPanel.add(totalLabel, BorderLayout.EAST);
        
        contentPanel.add(totalPanel);

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        personalizarScrollBar(scrollPane.getVerticalScrollBar());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);

        StyledButton continueButton = new StyledButton("Continuar", StyledButton.ButtonStyle.GRADIENT);
        continueButton.addActionListener(e -> dispose());
        bottomPanel.add(continueButton);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void personalizarScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(60, 60, 60);
                this.trackColor = new Color(31, 31, 31);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
                g2.dispose();
            }
            
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });
    }

    private void loadTicketData() {
        String query = "SELECT b.ID_Boleto, b.NumeroButaca, b.ID_Funcion, " +
                      "p.Titulo, f.FechaFuncion, f.HoraFuncion, f.Idioma, f.Precio " +
                      "FROM Comprobante_Boleto cb " +
                      "INNER JOIN Boleto b ON cb.ID_Boleto = b.ID_Boleto " +
                      "INNER JOIN Funcion f ON b.ID_Funcion = f.ID_Funcion " +
                      "INNER JOIN Pelicula p ON f.ID_Pelicula = p.ID_Pelicula " +
                      "WHERE cb.ID_Comprobante = ?";

        try (PreparedStatement pstmt = CINEMARX.M4.M4.getConexion().prepareStatement(query)) {

            pstmt.setInt(1, idComprobante);
            ResultSet rs = pstmt.executeQuery();

            double total = 0;
            while (rs.next()) {
                TicketInfo ticket = new TicketInfo(
                    rs.getInt("ID_Boleto"),
                    rs.getString("NumeroButaca"),
                    rs.getInt("ID_Funcion"),
                    rs.getString("Titulo"),
                    rs.getString("FechaFuncion"),
                    rs.getString("HoraFuncion"),
                    rs.getString("Idioma"),
                    rs.getDouble("Precio")
                );
                tickets.add(ticket);
                total += ticket.precio;
                addTicketPanel(ticket);
            }

            totalLabel.setText(String.format(java.util.Locale.US, "$%.2f", total));

        } catch (SQLException e) {
            e.printStackTrace();
            new CustomDialog((Frame) getOwner(), "Error al cargar los boletos").setVisible(true);
        }
    }

    private void addTicketPanel(TicketInfo ticket) {
        JPanel ticketPanel = new JPanel(new BorderLayout());
        ticketPanel.setOpaque(false);
        ticketPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setOpaque(false);
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        
        JLabel movieLabel = new JLabel(ticket.tituloMovie + " " + ticket.idioma + " - Butaca: " + ticket.numeroButaca);
        movieLabel.setForeground(new Color(176, 176, 176));
        movieLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        detailsPanel.add(movieLabel);

        JLabel dateTimeLabel = new JLabel(ticket.fecha + " " + ticket.hora);
        dateTimeLabel.setForeground(new Color(176, 176, 176));
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        detailsPanel.add(dateTimeLabel);

        ticketPanel.add(detailsPanel, BorderLayout.WEST);

        JLabel priceLabel = new JLabel(String.format(java.util.Locale.US, "$%.2f", ticket.precio));
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        ticketPanel.add(priceLabel, BorderLayout.EAST);

        ticketsPanel.add(ticketPanel);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        buttonsPanel.setOpaque(false);
        
        StyledButton reprogramButton = new StyledButton("Reprogramar", StyledButton.ButtonStyle.TOGGLE);
        reprogramButton.addActionListener(e -> showRescheduleDialog(ticket));
        
        StyledButton cancelButton = new StyledButton("Cancelar", StyledButton.ButtonStyle.TOGGLE);
        cancelButton.addActionListener(e -> cancelTicket(ticket));
        
        buttonsPanel.add(reprogramButton);
        buttonsPanel.add(cancelButton);
        
        ticketsPanel.add(Box.createVerticalStrut(30));
        ticketsPanel.add(buttonsPanel);
        ticketsPanel.add(Box.createVerticalStrut(40));
    }

        private void showRescheduleDialog(TicketInfo ticket) {

            EditarBoletoDialog dialog = new EditarBoletoDialog(this, ticket.idBoleto);

            dialog.setVisible(true);

    

            if (dialog.seGuardo()) {

                ticketsPanel.removeAll();

                tickets.clear();

                loadTicketData();

                ticketsPanel.revalidate();

                ticketsPanel.repaint();

    

                new CustomDialog((Frame) getOwner(),

                    "Modificacion con exito!<br>Su entrada fue reprogramada correctamente").setVisible(true);

            }

        }

    private void cancelTicket(TicketInfo ticket) {
        CustomDialog confirmDialog = new CustomDialog((Frame) getOwner(),
            "¿Está seguro que desea cancelar este boleto?",
            CustomDialog.DialogType.CONFIRMATION);
        confirmDialog.setVisible(true);

        if (confirmDialog.getResult() != JOptionPane.YES_OPTION) {
            return;
        }

        String deleteQuery = "DELETE FROM Boleto WHERE ID_Boleto = ?";

        try (PreparedStatement pstmt = CINEMARX.M4.M4.getConexion().prepareStatement(deleteQuery)) {

            pstmt.setInt(1, ticket.idBoleto);
            pstmt.executeUpdate();

            ticketsPanel.removeAll();
            tickets.clear();
            loadTicketData();
            ticketsPanel.revalidate();
            ticketsPanel.repaint();

            new CustomDialog((Frame) getOwner(),
                "Cancelacion exitosa!<br>Se le estara debitando el monto completo<br>en su tarjeta en los proximos dias.").setVisible(true);

        } catch (SQLException e) {
            e.printStackTrace();
            new CustomDialog((Frame) getOwner(), "Error al cancelar el boleto").setVisible(true);
        }
    }

    private void loadProductData() {
        new SwingWorker<List<ProductInfo>, Void>() {
            @Override
            protected List<ProductInfo> doInBackground() throws Exception {
                List<ProductInfo> products = new ArrayList<>();
                String query = "SELECT p.Nombre, p.Precio, cp.Cantidad " +
                               "FROM Comprobante_Producto cp " +
                               "JOIN Producto p ON cp.ID_Prod = p.ID_Prod " +
                               "WHERE cp.ID_Comprobante = ?";
                
                try (PreparedStatement pstmt = CINEMARX.M4.M4.getConexion().prepareStatement(query)) {
                    pstmt.setInt(1, idComprobante);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        products.add(new ProductInfo(
                            rs.getString("Nombre"),
                            rs.getDouble("Precio"),
                            rs.getInt("Cantidad")
                        ));
                    }
                }
                return products;
            }

            @Override
            protected void done() {
                try {
                    List<ProductInfo> products = get();
                    double productTotal = 0;
                    for (ProductInfo product : products) {
                        addProductPanel(product);
                        productTotal += product.precio * product.cantidad;
                    }
                    
                    // Update total
                    String currentTotalText = totalLabel.getText().replace("$", "").replace(",", ".");
                    double currentTotal = Double.parseDouble(currentTotalText);
                    totalLabel.setText(String.format(java.util.Locale.US, "$%.2f", currentTotal + productTotal));

                    productsPanel.revalidate();
                    productsPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void addProductPanel(ProductInfo product) {
        JPanel productPanel = new JPanel(new BorderLayout());
        productPanel.setOpaque(false);
        productPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel productLabel = new JLabel(product.cantidad + "x " + product.nombre);
        productLabel.setForeground(new Color(176, 176, 176));
        productLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        productPanel.add(productLabel, BorderLayout.WEST);

        JLabel priceLabel = new JLabel(String.format(java.util.Locale.US, "$%.2f", product.precio * product.cantidad));
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        productPanel.add(priceLabel, BorderLayout.EAST);

        productsPanel.add(productPanel);
        productsPanel.add(Box.createVerticalStrut(20));
    }

    private static class ProductInfo {
        String nombre;
        double precio;
        int cantidad;

        ProductInfo(String n, double p, int c) {
            nombre = n;
            precio = p;
            cantidad = c;
        }
    }
}