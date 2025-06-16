/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfg;

import java.awt.image.BufferedImage;

/**
 * Represents an item containing a generated image and its associated text prompt.
 * This class is useful for displaying image-text pairs in UI components such as lists.
 * 
 * Each instance stores:
 * <ul>
 *   <li>A {@link BufferedImage} representing the generated or loaded image.</li>
 *   <li>A {@link String} representing the text prompt used to generate the image.</li>
 * </ul>
 * 
 * 
 * @author Carlota de la Vega
 */
public class ImagePromptItem {

    /** The image associated with the prompt */
    private final BufferedImage image;

    /** The textual description or prompt that corresponds to the image */
    private final String prompt;

    /**
     * Constructs a new ImagePromptItem.
     *
     * @param image  the image to associate with the prompt; can be null if not yet available
     * @param prompt the prompt text describing or generating the image
     */
    public ImagePromptItem(BufferedImage image, String prompt) {
        this.image = image;
        this.prompt = prompt;
    }

    /**
     * Returns the image associated with this item.
     *
     * @return the BufferedImage representing the image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Returns the text prompt associated with this item.
     *
     * @return the prompt string
     */
    public String getPrompt() {
        return prompt;
    }
}
