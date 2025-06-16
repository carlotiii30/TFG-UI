package tfg;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmr.db.ListDB;
import jmr.descriptor.color.MPEG7ColorStructure;
import jmr.descriptor.color.MPEG7ScalableColor;
import jmr.descriptor.color.SingleColorDescriptor;
import jmr.descriptor.generated.PromptGeneratedImageDescriptor;
import jmr.descriptor.generated.PromptGeneratedImageDescriptorLocal;

/**
 * Main application window for the image retrieval system using generative AI
 * descriptors. This class provides a graphical interface to manage image
 * databases, perform queries, and display generation results.
 *
 * Author: Carlota de la Vega
 */
public final class MainWindow extends javax.swing.JFrame {

    /**
     * Active database
     */
    public ListDB<BufferedImage> database = null;

    private static final int WINDOW_OFFSET = 20;
    private boolean programmaticSelection = false;

    private String customApiToken = null;

    /**
     * Initializes the main window UI components and button states.
     */
    public MainWindow() {
        initComponents();
        setSize(600, 400);
        toggleDatabaseControls(false);
        activateToolTips();
        setSelectedAPI();
    }

    public JCheckBoxMenuItem getLocalAPImenu() {
        return localAPImenu;
    }

    public JCheckBoxMenuItem getOnlineAPImenu() {
        return onlineAPImenu;
    }

    public String getCustomApiToken() {
        return customApiToken;
    }

    /**
     * Returns the selected internal window containing an image.
     *
     * @return InternalWindow if selected and valid, otherwise null.
     */
    public InternalWindow getSelectedImageFrame() {
        JInternalFrame vi = desktop.getSelectedFrame();
        return (vi instanceof InternalWindow) ? (InternalWindow) vi : null;
    }

    /**
     * Checks if the internal window is of standard image type.
     *
     * @param vi InternalWindow to check
     * @return true if standard type, false otherwise
     */
    private boolean isStandardImageFrame(InternalWindow vi) {
        return vi != null && vi.getType() == InternalWindow.TYPE_STANDAR;
    }

    /**
     * Gets the image from the currently selected internal window. Supports both
     * standard image windows and PromptWindow.
     *
     * @return BufferedImage if available, otherwise null.
     */
    public BufferedImage getSelectedImage() {
        JInternalFrame selectedFrame = desktop.getSelectedFrame();

        if (selectedFrame instanceof InternalWindow vi && isStandardImageFrame(vi)) {
            return vi.getImage();
        } else if (selectedFrame instanceof PromptWindow pw) {
            BufferedImage img = pw.getImage();
            if (img != null) {
                return img;
            }
        }

        JOptionPane.showInternalMessageDialog(desktop, "An image must be selected", "Image", JOptionPane.INFORMATION_MESSAGE);
        return null;
    }

    /**
     * Displays the specified internal window and positions it.
     *
     * @param vi the internal window to show
     */
    public void showInternalWindow(JInternalFrame vi) {
        locateInternalWindow(vi);
        desktop.add(vi);
        vi.setVisible(true);
    }

    /**
     * Displays the prompt window for image generation.
     *
     * @param pp the prompt window to show
     */
    public void showPromptWindow(JInternalFrame pp) {
        desktop.add(pp);
        pp.setVisible(true);
    }

    /**
     * Positions the given internal frame offset from the currently selected
     * one.
     *
     * @param vi the internal frame to position
     */
    private void locateInternalWindow(JInternalFrame vi) {
        JInternalFrame[] allFrames = desktop.getAllFrames();
        JInternalFrame vSel = desktop.getSelectedFrame();

        if (vSel != null && allFrames.length != 0) {
            vi.setLocation(vSel.getX() + WINDOW_OFFSET, vSel.getY() + WINDOW_OFFSET);
            vi.setSize(vSel.getSize());
        }
    }

    /**
     * Updates the state of buttons based on the database being open or closed.
     *
     * @param closed true if database is closed, false if open
     */
    private void setDataBaseButtonStatus(boolean closed) {
        toggleDatabaseControls(!closed);
    }

    /**
     * Toggles the UI controls related to database status.
     *
     * @param enabled true to enable database-related buttons, false to disable
     */
    private void toggleDatabaseControls(boolean enabled) {
        this.newDBButton.setEnabled(!enabled);
        this.openDBButton.setEnabled(!enabled);
        this.closeDBButton.setEnabled(enabled);
        this.saveDBButton.setEnabled(enabled);
        this.addRecordDBButton.setEnabled(enabled);
        this.searchDBButton.setEnabled(enabled);
        this.informationButton.setEnabled(enabled);
    }

    /**
     * Creates a panel with descriptor selection checkboxes.
     *
     * @return JPanel with descriptor checkboxes
     */
    private JPanel createCheckPanel(JCheckBox cb1, JCheckBox cb2, JCheckBox cb3) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Select descriptors to include in the database:"));
        panel.add(cb1);
        panel.add(cb2);
        panel.add(cb3);
        return panel;
    }

    /**
     * Adds a new generated image and its corresponding prompt to the history
     * combo box. This method creates an {@link ImagePromptItem} combining the
     * image and prompt, appends it to the {@link JComboBox} model used for the
     * history dropdown ({@code historicBox}), and sets it as the currently
     * selected item.
     *
     * @param image the generated {@link BufferedImage} to be added to the
     * history
     * @param prompt the text prompt that was used to generate the image
     */
    public void addToHistory(BufferedImage image, String prompt) {
        ImagePromptItem item = new ImagePromptItem(image, prompt);
        DefaultComboBoxModel<ImagePromptItem> model = (DefaultComboBoxModel<ImagePromptItem>) historicBox.getModel();
        programmaticSelection = true;
        model.addElement(item);
        historicBox.setSelectedItem(null);
        programmaticSelection = false;
    }

    /**
     * Generates an image based on the provided text prompt using the
     * PromptGeneratedImageDescriptor class.
     *
     * @param prompt the textual description to be used for image generation
     * @return a BufferedImage created from the prompt, or null if generation
     * fails
     */
    private BufferedImage generateImageFromPrompt(String prompt) {
        try {
            if (onlineAPImenu.isSelected()) {
                if (customApiToken == null || customApiToken.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "API token not set.", "Missing Token", JOptionPane.WARNING_MESSAGE);
                    return null;
                }
                PromptGeneratedImageDescriptor descriptor
                        = new PromptGeneratedImageDescriptor(prompt, customApiToken);
                return descriptor.getGeneratedImage();
            } else {
                PromptGeneratedImageDescriptorLocal descriptor = new PromptGeneratedImageDescriptorLocal(prompt);
                return descriptor.getGeneratedImage();
            }
        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this, "Failed to generate image from prompt.", "Generation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Performs a similarity search in the database using the provided image as
     * a query. Displays the top results in a new ListInternalWindow.
     *
     * @param queryImage the image to be used as the basis for the similarity
     * query
     */
    private void performImageQuery(BufferedImage queryImage) {
        try {
            List<ListDB<BufferedImage>.Record> queryResult = database.query(queryImage, 10);
            ListInternalWindow listWindow = new ListInternalWindow();

            for (ListDB.Record r : queryResult) {
                if (r.getLocator() != null) {
                    listWindow.add(r.getLocator(), r.getLocator().getFile());
                }
            }

            this.desktop.add(listWindow);
            listWindow.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to perform query", "Query Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Sets the behavior of the API selection menu items.
     *
     * This method adds listeners to the "Local API" and "Online API" checkbox
     * menu items. It ensures that only one of the two options can be selected
     * at a time. When "Online API" is selected, it prompts the user to enter a
     * Hugging Face API token. If the token is not provided or is empty, it
     * reverts to "Local API".
     */
    private void setSelectedAPI() {
        localAPImenu.addActionListener(e -> {
            if (localAPImenu.isSelected()) {
                onlineAPImenu.setSelected(false);
            } else {
                localAPImenu.setSelected(true);
            }
        });

        onlineAPImenu.addActionListener(e -> {
            if (onlineAPImenu.isSelected()) {
                localAPImenu.setSelected(false);
                setToken();
            } else {
                onlineAPImenu.setSelected(true);
            }
        });

    }

    /**
     * Prompts the user to input a Hugging Face API token.
     *
     * This method displays a dialog with a text field for the user to paste or
     * type their Hugging Face API token. If a valid token is entered, it is
     * saved for future use during image generation. If the token is empty or
     * the user cancels the dialog, the selection is reverted to use the "Local
     * API" instead.
     *
     * Supports keyboard paste operations such as Ctrl+V / Cmd+V.
     */
    private void setToken() {
        JTextField tokenField = new JTextField(40);
        tokenField.setText(customApiToken != null ? customApiToken : "");
        tokenField.setCaretPosition(tokenField.getText().length());

        int result = JOptionPane.showConfirmDialog(
                this,
                tokenField,
                "Enter Hugging Face API Token",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String token = tokenField.getText().trim();
            if (token.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Token cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                onlineAPImenu.setSelected(false);
                localAPImenu.setSelected(true);
            } else {
                System.out.println(token);
                customApiToken = token;
            }
        } else {
            onlineAPImenu.setSelected(false);
            localAPImenu.setSelected(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        toolBar = new javax.swing.JToolBar();
        newDBButton = new javax.swing.JButton();
        openDBButton = new javax.swing.JButton();
        saveDBButton = new javax.swing.JButton();
        closeDBButton = new javax.swing.JButton();
        addRecordDBButton = new javax.swing.JButton();
        informationButton = new javax.swing.JButton();
        searchDBButton = new javax.swing.JButton();
        promptToSearch = new javax.swing.JTextField();
        separator1 = new javax.swing.JToolBar.Separator();
        generateImageButton = new javax.swing.JButton();
        historicBox = new javax.swing.JComboBox<>();
        separator = new javax.swing.JToolBar.Separator();
        botonSingleColor = new javax.swing.JButton();
        desktop = new javax.swing.JDesktopPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenu = new javax.swing.JMenuItem();
        saveMenu = new javax.swing.JMenuItem();
        duplicateMenu = new javax.swing.JMenuItem();
        separador = new javax.swing.JPopupMenu.Separator();
        closeAll = new javax.swing.JMenuItem();
        apiMenu = new javax.swing.JMenu();
        onlineAPImenu = new javax.swing.JCheckBoxMenuItem();
        localAPImenu = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        toolBar.setRollover(true);

        newDBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/DataBase.png"))); // NOI18N
        newDBButton.setToolTipText("Create a new database");
        newDBButton.setBorderPainted(false);
        newDBButton.setFocusable(false);
        newDBButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newDBButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDBButtonActionPerformed(evt);
            }
        });
        toolBar.add(newDBButton);

        openDBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/openDB.png"))); // NOI18N
        openDBButton.setToolTipText("Open a database");
        openDBButton.setFocusable(false);
        openDBButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openDBButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDBButtonActionPerformed(evt);
            }
        });
        toolBar.add(openDBButton);

        saveDBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/saveDB.png"))); // NOI18N
        saveDBButton.setToolTipText("Save the database");
        saveDBButton.setFocusable(false);
        saveDBButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveDBButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDBButtonActionPerformed(evt);
            }
        });
        toolBar.add(saveDBButton);

        closeDBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/deleteBD.png"))); // NOI18N
        closeDBButton.setToolTipText("Close the database");
        closeDBButton.setFocusable(false);
        closeDBButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeDBButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        closeDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDBButtonActionPerformed(evt);
            }
        });
        toolBar.add(closeDBButton);

        addRecordDBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/addBD.png"))); // NOI18N
        addRecordDBButton.setToolTipText("Add file to database");
        addRecordDBButton.setFocusable(false);
        addRecordDBButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addRecordDBButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addRecordDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRecordDBButtonActionPerformed(evt);
            }
        });
        toolBar.add(addRecordDBButton);

        informationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/informacion.png"))); // NOI18N
        informationButton.setToolTipText("Information about database");
        informationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        informationButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        informationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                informationButtonActionPerformed(evt);
            }
        });
        toolBar.add(informationButton);

        searchDBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/searchDB.png"))); // NOI18N
        searchDBButton.setFocusable(false);
        searchDBButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        searchDBButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchDBButtonActionPerformed(evt);
            }
        });
        toolBar.add(searchDBButton);

        promptToSearch.setToolTipText("Write a prompt to search on database");
        promptToSearch.setMinimumSize(new java.awt.Dimension(100, 25));
        promptToSearch.setPreferredSize(new java.awt.Dimension(100, 25));
        toolBar.add(promptToSearch);
        toolBar.add(separator1);

        generateImageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/generarImagen.png"))); // NOI18N
        generateImageButton.setToolTipText("Generate image from prompt");
        generateImageButton.setFocusable(false);
        generateImageButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        generateImageButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        generateImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateImageButtonActionPerformed(evt);
            }
        });
        toolBar.add(generateImageButton);

        historicBox.setRenderer(new ImagePromptListRenderer());

        DefaultComboBoxModel<ImagePromptItem> model = new DefaultComboBoxModel<>();
        historicBox.setModel(model);
        historicBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                historicBoxActionPerformed(evt);
            }
        });
        toolBar.add(historicBox);
        toolBar.add(separator);

        botonSingleColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/mean24.png"))); // NOI18N
        botonSingleColor.setToolTipText("Mean color descriptor");
        botonSingleColor.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        botonSingleColor.setDefaultCapable(false);
        botonSingleColor.setFocusable(false);
        botonSingleColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonSingleColor.setMaximumSize(new java.awt.Dimension(31, 31));
        botonSingleColor.setMinimumSize(new java.awt.Dimension(31, 31));
        botonSingleColor.setPreferredSize(new java.awt.Dimension(31, 31));
        botonSingleColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonSingleColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonSingleColorActionPerformed(evt);
            }
        });
        toolBar.add(botonSingleColor);

        getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);
        getContentPane().add(desktop, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");

        openMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        openMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/abrir.png"))); // NOI18N
        openMenu.setText("Open");
        openMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuActionPerformed(evt);
            }
        });
        fileMenu.add(openMenu);

        saveMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/guardar.png"))); // NOI18N
        saveMenu.setText("Save");
        saveMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenu);

        duplicateMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        duplicateMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/duplicar.png"))); // NOI18N
        duplicateMenu.setText("Duplicate");
        duplicateMenu.setToolTipText("");
        duplicateMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateMenuActionPerformed(evt);
            }
        });
        fileMenu.add(duplicateMenu);
        fileMenu.add(separador);

        closeAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        closeAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cerrar.png"))); // NOI18N
        closeAll.setText("Close all");
        closeAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAllActionPerformed(evt);
            }
        });
        fileMenu.add(closeAll);

        menuBar.add(fileMenu);

        apiMenu.setText("API");
        apiMenu.setToolTipText("Select API used to generate images");

        onlineAPImenu.setText("Online API");
        apiMenu.add(onlineAPImenu);

        localAPImenu.setSelected(true);
        localAPImenu.setText("Local API");
        localAPImenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localAPImenuActionPerformed(evt);
            }
        });
        apiMenu.add(localAPImenu);

        menuBar.add(apiMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Opens multiple images and displays them as internal windows.
     */
    private void openMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuActionPerformed
        JFileChooser dlg = new JFileChooser();
        dlg.setMultiSelectionEnabled(true);
        if (dlg.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = dlg.getSelectedFiles();
            for (File f : files) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        InternalWindow vi = new InternalWindow(this, img, f.toURI().toURL());
                        vi.setTitle(f.getName());
                        this.showInternalWindow(vi);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Could not open file: " + f.getName(), "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_openMenuActionPerformed

    /**
     * Saves the currently selected image to disk as PNG.
     */
    private void saveMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuActionPerformed
        BufferedImage img = this.getSelectedImage();
        if (img == null) {
            return;
        }

        JFileChooser dlg = new JFileChooser();
        if (dlg.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = dlg.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".png")) {
                f = new File(f.getAbsolutePath() + ".png");
            }
            try {
                ImageIO.write(img, "png", f);
                desktop.getSelectedFrame().setTitle(f.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save image", "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_saveMenuActionPerformed

    /**
     * Closes all active windows.
     */
    private void closeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeAllActionPerformed
        desktop.removeAll();
        desktop.repaint();
    }//GEN-LAST:event_closeAllActionPerformed

    /**
     * Opens a new Internal Window to wirte de Prompt to generate an Image.
     */
    private void generateImageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateImageButtonActionPerformed
        PromptWindow pp = new PromptWindow(this);
        this.showPromptWindow(pp);
    }//GEN-LAST:event_generateImageButtonActionPerformed

    /**
     * Opens a descriptor selection dialog and initializes a new database.
     */
    private void newDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDBButtonActionPerformed
        JCheckBox cbColorStructure = new JCheckBox("MPEG7ColorStructure");
        JCheckBox cbScalableColor = new JCheckBox("MPEG7ScalableColor");
        JCheckBox cbMeanColor = new JCheckBox("SingleColorDescriptor");

        JPanel panel = createCheckPanel(cbColorStructure, cbScalableColor, cbMeanColor);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Descriptor selection",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            ArrayList<Class<?>> selected = new ArrayList<>();

            if (cbColorStructure.isSelected()) {
                selected.add(MPEG7ColorStructure.class);
            }
            if (cbScalableColor.isSelected()) {
                selected.add(MPEG7ScalableColor.class);
            }
            if (cbMeanColor.isSelected()) {
                selected.add(SingleColorDescriptor.class);
            }

            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must select at least one descriptor.", "Empty Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Class<?>[] descriptorClasses = selected.toArray(Class[]::new);
            database = new ListDB<>(descriptorClasses);
            setDataBaseButtonStatus(false);
        }
    }//GEN-LAST:event_newDBButtonActionPerformed

    /**
     * Opens an existing database from the current directory.
     */
    private void openDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDBButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open a database");
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JMR Database Files", "db"));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                database = ListDB.open(file);
                setDataBaseButtonStatus(false);
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Error opening database: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Could not open the selected database.", "Open Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_openDBButtonActionPerformed

    /**
     * Saves current database.
     */
    private void saveDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDBButtonActionPerformed
        String name = JOptionPane.showInputDialog(this, "Enter a name for the database:", "Save Database", JOptionPane.PLAIN_MESSAGE);

        if (name != null && !name.trim().isEmpty()) {
            if (!name.endsWith(".jmr.db")) {
                name += ".jmr.db";
            }

            File file = new File(name);
            try {
                database.save(file);
            } catch (IOException ex) {
                System.err.println("Error saving database: " + ex.getLocalizedMessage());
                JOptionPane.showMessageDialog(this, "Failed to save the database.", "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_saveDBButtonActionPerformed

    /**
     * Closes current database.
     */
    private void closeDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeDBButtonActionPerformed
        database.clear();
        database = null;
        setDataBaseButtonStatus(true);
    }//GEN-LAST:event_closeDBButtonActionPerformed

    /**
     * Adds records to the database from all internal windows.
     */
    private void addRecordDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRecordDBButtonActionPerformed
        if (database == null) {
            return;
        }
        Cursor previous = getCursor();
        try {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            for (JInternalFrame vi : desktop.getAllFrames()) {
                if (vi instanceof InternalWindow iw) {
                    if (iw.getImage() != null) {
                        database.add(iw.getImage(), iw.getURL());
                        System.out.println("Added image: " + iw.getURL() + " with descriptors: " + database.getDescriptorClasses());
                    }
                }
            }
        } finally {
            setCursor(previous);
        }
    }//GEN-LAST:event_addRecordDBButtonActionPerformed

    /**
     * Queries the database using either: - the currently selected image, or - a
     * text prompt (entered in promptToSearch) to generate an image. Displays
     * the results in a new internal window.
     */
    private void searchDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchDBButtonActionPerformed
        if (database == null) {
            return;
        }

        BufferedImage queryImage;
        String prompt = promptToSearch.getText().trim();

        if (!prompt.isEmpty()) {
            queryImage = generateImageFromPrompt(prompt);
            if (queryImage == null) {
                return;
            }

            addToHistory(queryImage, prompt);
            promptToSearch.setText("");
        } else {
            queryImage = getSelectedImage();
            if (queryImage == null) {
                return;
            }
        }

        performImageQuery(queryImage);
    }//GEN-LAST:event_searchDBButtonActionPerformed

    /**
     * Displays information about current DataBase.
     */
    private void informationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_informationButtonActionPerformed
        if (database == null) {
            JOptionPane.showMessageDialog(this, "No database is currently loaded.", "Database Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("Database Information:\n\n");
        info.append("Number of records: ").append(database.size()).append("\n");

        List<Class> descriptors = database.getDescriptorClasses();
        info.append("Descriptors used:\n");
        for (Class c : descriptors) {
            info.append(" - ").append(c.getSimpleName()).append("\n");
        }

        JOptionPane.showMessageDialog(this, info.toString(), "Database Info", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_informationButtonActionPerformed

    private void botonSingleColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSingleColorActionPerformed
        BufferedImage img = this.getSelectedImage();
        if (img != null) {
            java.awt.Cursor cursor = this.getCursor();
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            ColorSetPanel panelColor = new ColorSetPanel();
            SingleColorDescriptor d = new SingleColorDescriptor(img);
            panelColor.addColor(d.getColor());
            setCursor(cursor);

            InternalWindow vi = this.getSelectedImageFrame();
            vi.add(panelColor, BorderLayout.EAST);
            vi.validate();
            vi.repaint();
        }
    }//GEN-LAST:event_botonSingleColorActionPerformed

    private void historicBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_historicBoxActionPerformed
        if (programmaticSelection) {
            return;
        }

        ImagePromptItem selected = (ImagePromptItem) historicBox.getSelectedItem();
        if (selected != null && selected.getImage() != null) {
            try {
                String prompt = selected.getPrompt();
                BufferedImage img = selected.getImage();

                String safePrompt = prompt.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
                File outFile = new File("generated_images/" + safePrompt + "_" + java.util.UUID.randomUUID() + ".png");
                outFile.getParentFile().mkdirs();
                javax.imageio.ImageIO.write(img, "png", outFile);

                URL fileURL = outFile.toURI().toURL();

                InternalWindow vi = new InternalWindow(this, img, fileURL);
                vi.setTitle("From history: " + prompt);
                this.showInternalWindow(vi);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to save or locate the image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_historicBoxActionPerformed

    private void duplicateMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateMenuActionPerformed
        InternalWindow selected = (InternalWindow) desktop.getSelectedFrame();

        if (selected != null) {
            BufferedImage original = selected.getImage();

            if (original != null) {
                ColorModel cm = original.getColorModel();
                boolean isAlphaPremultiplied = original.isAlphaPremultiplied();
                WritableRaster raster = original.copyData(null);
                BufferedImage copy = new BufferedImage(cm, raster, isAlphaPremultiplied, null);

                InternalWindow duplicated = new InternalWindow(this, copy, selected.getURL());
                duplicated.setTitle(selected.getTitle() + " (copy)");

                this.desktop.add(duplicated);
                duplicated.setVisible(true);
            }
        }
    }//GEN-LAST:event_duplicateMenuActionPerformed

    private void localAPImenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localAPImenuActionPerformed

    }//GEN-LAST:event_localAPImenuActionPerformed

    public void activateToolTips() {
        historicBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                Object comp = historicBox.getUI().getAccessibleChild(historicBox, 0);
                if (comp instanceof javax.swing.plaf.basic.ComboPopup popup) {
                    JList<?> list = popup.getList();
                    list.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                        @Override
                        public void mouseMoved(java.awt.event.MouseEvent evt) {
                            int index = list.locationToIndex(evt.getPoint());
                            if (index > -1) {
                                Object item = list.getModel().getElementAt(index);
                                if (item instanceof ImagePromptItem ipi) {
                                    list.setToolTipText(ipi.getPrompt());
                                } else {
                                    list.setToolTipText(null);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRecordDBButton;
    private javax.swing.JMenu apiMenu;
    private javax.swing.JButton botonSingleColor;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JMenuItem closeAll;
    private javax.swing.JButton closeDBButton;
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JMenuItem duplicateMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton generateImageButton;
    private javax.swing.JComboBox<ImagePromptItem> historicBox;
    private javax.swing.JButton informationButton;
    private javax.swing.JCheckBoxMenuItem localAPImenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton newDBButton;
    private javax.swing.JCheckBoxMenuItem onlineAPImenu;
    private javax.swing.JButton openDBButton;
    private javax.swing.JMenuItem openMenu;
    private javax.swing.JTextField promptToSearch;
    private javax.swing.JButton saveDBButton;
    private javax.swing.JMenuItem saveMenu;
    private javax.swing.JButton searchDBButton;
    private javax.swing.JPopupMenu.Separator separador;
    private javax.swing.JToolBar.Separator separator;
    private javax.swing.JToolBar.Separator separator1;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
}
