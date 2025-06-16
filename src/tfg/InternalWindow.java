package tfg;

import jfi.iu.ImageInternalFrame;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.JFrame;

/**
 * A custom internal window used to display an image within a Swing application.
 * This class extends {@link ImageInternalFrame} to show a
 * {@link BufferedImage}, and optionally stores a URL locator associated with
 * the image.
 *
 * It is typically used in image retrieval systems to provide a visual preview
 * of generated or retrieved images in a multi-window interface.
 *
 * @author Carlota de la Vega Soriano
 */
public class InternalWindow extends ImageInternalFrame {

    /**
     * Optional locator associated with the displayed image.
     */
    private URL locator = null;

    /**
     * Constructs an InternalWindow with a parent frame and the image to be
     * displayed.
     *
     * @param parent the main parent {@link JFrame} that owns this internal
     * window
     * @param img the {@link BufferedImage} to be displayed within the window
     */
    public InternalWindow(JFrame parent, BufferedImage img) {
        super(parent, img);
        initComponents();
    }

    /**
     * Constructs an InternalWindow with a parent frame, an image, and a locator
     * URL. This constructor is useful when the image is associated with a
     * specific source (e.g., file path, database entry, or remote resource).
     *
     * @param parent the main parent {@link JFrame} that owns this internal
     * window
     * @param img the {@link BufferedImage} to be displayed
     * @param locator a {@link URL} representing the source or location of the
     * image
     */
    public InternalWindow(JFrame parent, BufferedImage img, URL locator) {
        this(parent, img);
        this.locator = locator;
    }

    /**
     * Returns the locator URL associated with the image displayed in this
     * internal window.
     *
     * @return a {@link URL} representing the image source, or {@code null} if
     * not set
     */
    public URL getURL() {
        return locator;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setClosable(true);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
