package tfg;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Custom list cell renderer for {@link ImagePromptItem} objects.
 *
 * This renderer displays a scaled preview of the image on the left and its
 * corresponding prompt text on the right, formatted horizontally. It is
 * designed to be used with {@link JList} to visually present image-prompt
 * pairs.
 *
 * @author Carlota de la Vega
 */
public class ImagePromptListRenderer extends JPanel implements ListCellRenderer<ImagePromptItem> {

    /**
     * Label used to display the image icon
     */
    private final JLabel imageLabel = new JLabel();

    /**
     * Label used to display the prompt text
     */
    private final JLabel textLabel = new JLabel();

    /**
     * Constructs a new renderer with horizontal layout and spacing between
     * image and text.
     */
    public ImagePromptListRenderer() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(imageLabel);
        add(Box.createHorizontalStrut(10));
        add(textLabel);

        setPreferredSize(new Dimension(200, 24));
        setMaximumSize(getPreferredSize());

        textLabel.setMaximumSize(new Dimension(170, 24));
        textLabel.setPreferredSize(new Dimension(170, 24));
        textLabel.setMinimumSize(new Dimension(170, 24));

        textLabel.setToolTipText("");
        textLabel.setOpaque(false);
    }

    /**
     * Configures the appearance of a list cell based on the provided item.
     *
     * @param list the {@code JList} we're painting
     * @param value the value returned by
     * {@code list.getModel().getElementAt(index)}
     * @param index the cell index
     * @param isSelected {@code true} if the specified cell is selected
     * @param cellHasFocus {@code true} if the specified cell has the focus
     * @return a component that has been configured to display the specified
     * value
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends ImagePromptItem> list,
            ImagePromptItem value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        if (value != null) {
            BufferedImage image = value.getImage();
            String prompt = value.getPrompt();

            if (image != null) {
                Image scaled = image.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
            } else {
                imageLabel.setIcon(null);
            }

            textLabel.setText(prompt);
            textLabel.setToolTipText(prompt);
            
        } else {
            imageLabel.setIcon(null);
            textLabel.setText("");
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setOpaque(true);
        return this;
    }
}
