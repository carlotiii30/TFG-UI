package tfg;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import jmr.result.ResultMetadata;

/**
 * A JInternalFrame that displays a list of images, typically used to visualize
 * the results of a search or similarity query. This window allows adding images
 * directly or with associated labels, and also supports loading images from
 * URLs.
 *
 * It integrates a custom component {@code ImageListPanel} for displaying image
 * thumbnails in a scrollable panel.
 *
 * @author Carlota de la Vega
 */
public class ListInternalWindow extends javax.swing.JInternalFrame {

    /**
     * Default constructor. Initializes the UI components and prepares the image
     * list panel.
     */
    public ListInternalWindow() {
        initComponents();
    }

    /**
     * Constructor that initializes the window and populates it with a list of
     * result metadata, typically used to show the results of a database query.
     *
     * @param list A list of {@code ResultMetadata} objects to be displayed.
     */
    public ListInternalWindow(List<ResultMetadata> list) {
        this();
        if (list != null) {
            imageListPanel.add(list);
        }
    }

    /**
     * Adds an image to the list panel without a label.
     *
     * @param image A {@code BufferedImage} to be displayed.
     */
    public void add(BufferedImage image) {
        imageListPanel.add(image);
    }

    /**
     * Adds an image to the list panel with an associated label.
     *
     * @param image A {@code BufferedImage} to be displayed.
     * @param label A {@code String} label describing the image.
     */
    public void add(BufferedImage image, String label) {
        imageListPanel.add(image, label);
    }

    /**
     * Loads an image from a URL and adds it to the list panel with a label.
     *
     * @param imageURL The {@code URL} pointing to the image resource.
     * @param label A {@code String} label describing the image.
     */
    public void add(URL imageURL, String label) {
        BufferedImage image;
        try {
            image = ImageIO.read(imageURL);
            if (image != null) {
                imageListPanel.add(image, label);
            }
        } catch (IOException ex) {
            System.err.println("Error loading image from URL: " + ex);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imageListPanel = new jmr.iu.ImageListPanel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Result");
        getContentPane().add(imageListPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private jmr.iu.ImageListPanel imageListPanel;
    // End of variables declaration//GEN-END:variables
}
