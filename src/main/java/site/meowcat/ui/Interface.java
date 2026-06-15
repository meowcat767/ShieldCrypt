package site.meowcat.ui;

import java.awt.*;
import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import site.meowcat.manager.KeyManager;
import site.meowcat.manager.CryptoManager;

public class Interface {
    private JPanel contentFrame;
    private JPanel menuBarPanel;
    private JToolBar menuBar;
    private JTabbedPane tabbedPane1;
    private JTextArea secretKeyArea;
    private JTextArea recipientPublicKeyArea;
    private JPanel textEncryptPanel;
    private JTextArea textIOArea;
    private JButton encryptButton;
    private JButton decryptButton;
    private JPanel imageEncryptPanel;
    private JButton selectImageButton;
    private JLabel imagePathLabel;
    private JLabel imagePreviewLabel;
    private JButton encryptImageButton;
    private JButton decryptImageButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton exitButton;
    private JPanel fileEncryptPanel;
    private JButton selectFileButton;
    private JLabel filePathLabel;
    private JButton encryptFileButton;
    private JButton decryptFileButton;
    private JButton generateKeysButton;

    private final KeyManager keyManager = new KeyManager();

    public Interface() {

        secretKeyArea.setEditable(false);
        recipientPublicKeyArea.setEditable(true);

        Font monospacedFont = new Font("Monospaced", Font.PLAIN, 12);
        secretKeyArea.setFont(monospacedFont);
        recipientPublicKeyArea.setFont(monospacedFont);

        selectImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(contentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                imagePathLabel.setText(fileChooser.getSelectedFile().getAbsolutePath());
                // TODO: Image Preview Logic
            }
        });
        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(contentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                filePathLabel.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        exitButton.addActionListener(e -> System.exit(0));

        encryptFileButton.addActionListener(e -> {
            String filePath = filePathLabel.getText();
            if (filePath.equals("No file selected")) {
                JOptionPane.showMessageDialog(contentFrame, "Please select a file first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String publicKeyPem = recipientPublicKeyArea.getText().trim();
            if (publicKeyPem.isEmpty()) {
                JOptionPane.showMessageDialog(contentFrame, "Please provide a recipient public key.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // 1. Generate AES Key
                keyManager.generateKey();
                secretKeyArea.setText(keyManager.getSecretKeyString());

                // 2. Load Recipient Public Key
                java.security.PublicKey publicKey = CryptoManager.loadRSAPublicKey(publicKeyPem);

                // 3. Encrypt File with AES
                File inputFile = new File(filePath);
                File encryptedFile = new File(filePath + ".enc");
                CryptoManager.encryptFile(inputFile, encryptedFile, keyManager.getSecretKey());

                // 4. Encrypt AES Key with Recipient Public Key
                byte[] encryptedKey = CryptoManager.encryptAESKey(keyManager.getSecretKey(), publicKey);
                File keyFile = new File(filePath + ".key");
                Files.write(keyFile.toPath(), encryptedKey);

                JOptionPane.showMessageDialog(contentFrame,
                        "File encrypted successfully!\nEncrypted file: " + encryptedFile.getName() + "\nEncrypted key: " + keyFile.getName(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(contentFrame, "Error during encryption: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        generateKeysButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    contentFrame,
                    "Generate a new secret key? Unsaved keys will be lost.",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                keyManager.generateKey();
                secretKeyArea.setText(keyManager.getSecretKeyString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        contentFrame,
                        "Error generating key: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    public JPanel getContentPane() {
        return contentFrame;
    }
}
