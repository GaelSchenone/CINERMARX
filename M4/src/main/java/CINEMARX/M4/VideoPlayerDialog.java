package CINEMARX.M4;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VideoPlayerDialog extends JDialog {

    private final JFXPanel jfxPanel = new JFXPanel();
    private MediaPlayer mediaPlayer;

    public VideoPlayerDialog(Frame owner, String videoUrl) {
        super(owner, "Trailer", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(800, 600);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CinemarxTheme.BG_MAIN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createLineBorder(CinemarxTheme.BORDER_COLOR, 2, true));
        setContentPane(mainPanel);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(new EmptyBorder(10, 20, 10, 10));

        JLabel titleLabel = new JLabel("Trailer");
        titleLabel.setFont(CinemarxTheme.FONT_H2);
        titleLabel.setForeground(CinemarxTheme.TEXT_LIGHT);
        titleBar.add(titleLabel, BorderLayout.CENTER);

        JButton closeButton = new JButton("X");
        closeButton.setFont(CinemarxTheme.FONT_H2);
        closeButton.setForeground(CinemarxTheme.TEXT_LIGHT);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            dispose();
        });
        titleBar.add(closeButton, BorderLayout.EAST);

        mainPanel.add(titleBar, BorderLayout.NORTH);
        mainPanel.add(jfxPanel, BorderLayout.CENTER);

        createScene(videoUrl);

        // Make dialog draggable
        Point initialClick = new Point();
        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick.setLocation(e.getPoint());
            }
        });

        titleBar.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });
    }

    private void createScene(String videoUrl) {
        Platform.runLater(() -> {
            try {
                Media media = new Media(videoUrl);
                mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);

                Group root = new Group();
                root.getChildren().add(mediaView);

                Scene scene = new Scene(root);
                mediaView.fitWidthProperty().bind(scene.widthProperty());
                mediaView.fitHeightProperty().bind(scene.heightProperty());
                
                jfxPanel.setScene(scene);

                mediaPlayer.play();

                mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                    System.out.println("MediaPlayer Status: " + newStatus);
                });

                mediaPlayer.setOnError(() -> {
                    Throwable error = mediaPlayer.getError();
                    System.err.println("MediaPlayer Error: " + error);
                    SwingUtilities.invokeLater(() -> {
                        String errorMessage = "Error en la reproducción del video.";
                        if (error != null) {
                            errorMessage += "\nDetalles: " + error.getMessage();
                        }
                        JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                        dispose();
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al cargar el video.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
