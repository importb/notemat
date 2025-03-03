package com.notemat.filesystem;

import com.notemat.components.ImageComponent;
import com.notemat.main.Notemat;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;



/**
 * Handles saving and loading of Notemat files in a custom format.
 */
public class NTMFile {
    private static final String TEXT_FILE = "content.rtf";
    private static final String IMAGES_FILE = "images.dat";

    // Helper class to store image data in a serializable format
    private static class ImageData implements Serializable {
        private static final long serialVersionUID = 1L;

        public final byte[] imageBytes;        // Original image bytes
        public final byte[] displayImageBytes; // Resized image bytes
        public final int x;
        public final int y;
        public final int width;
        public final int height;

        public ImageData(BufferedImage originalImage, BufferedImage displayImage,
                         int x, int y, int width, int height) throws IOException {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            // Store both original and display (resized) images
            ByteArrayOutputStream baosOriginal = new ByteArrayOutputStream();
            ByteArrayOutputStream baosDisplay = new ByteArrayOutputStream();

            ImageIO.write(originalImage, "png", baosOriginal);
            ImageIO.write(displayImage, "png", baosDisplay);

            this.imageBytes = baosOriginal.toByteArray();
            this.displayImageBytes = baosDisplay.toByteArray();
        }

        public BufferedImage getOriginalImage() throws IOException {
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        }

        public BufferedImage getDisplayImage() throws IOException {
            return ImageIO.read(new ByteArrayInputStream(displayImageBytes));
        }
    }

    /**
     * Saves the current state of the Notemat application to a file.
     *
     * @param notemat  the Notemat instance to save
     * @param filePath the path to the file
     * @throws IOException if an I/O error occurs
     */
    public static void saveToFile(Notemat notemat, String filePath) throws IOException {
        if (!filePath.endsWith(".ntm")) {
            filePath += ".ntm";
        }

        try (FileOutputStream fos = new FileOutputStream(filePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Save text content and styling using RTF format
            saveTextContent(notemat, zos);

            // Save images
            saveImages(notemat, zos);
        }
    }

    private static void saveTextContent(Notemat notemat, ZipOutputStream zos)
            throws IOException {
        ZipEntry textEntry = new ZipEntry(TEXT_FILE);
        zos.putNextEntry(textEntry);

        RTFEditorKit kit = new RTFEditorKit();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            kit.write(baos, notemat.getTextPane().getDocument(), 0,
                    notemat.getTextPane().getDocument().getLength());
            byte[] bytes = baos.toByteArray();
            zos.write(bytes, 0, bytes.length);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        } finally {
            baos.close();
        }

        zos.closeEntry();
    }

    private static void saveImages(Notemat notemat, ZipOutputStream zos)
            throws IOException {
        ZipEntry imagesEntry = new ZipEntry(IMAGES_FILE);
        zos.putNextEntry(imagesEntry);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            Component[] components = notemat.getContentLayeredPane().getComponents();
            ArrayList<ImageData> imageDataList = new ArrayList<>();

            for (Component comp : components) {
                if (comp instanceof ImageComponent) {
                    ImageComponent imgComp = (ImageComponent) comp;

                    // Get both the original and current (possibly resized) images
                    BufferedImage originalImage = imgComp.getOriginalImage();
                    BufferedImage displayImage = new BufferedImage(
                            imgComp.getWidth(),
                            imgComp.getHeight(),
                            BufferedImage.TYPE_INT_ARGB
                    );

                    // Draw the current display state into the new image
                    Graphics2D g2d = displayImage.createGraphics();
                    imgComp.paint(g2d);
                    g2d.dispose();

                    ImageData imageData = new ImageData(
                            originalImage,
                            displayImage,
                            imgComp.getX(),
                            imgComp.getY(),
                            imgComp.getWidth(),
                            imgComp.getHeight()
                    );
                    imageDataList.add(imageData);
                }
            }

            oos.writeObject(imageDataList);
        }

        byte[] bytes = baos.toByteArray();
        zos.write(bytes, 0, bytes.length);
        zos.closeEntry();
    }

    /**
     * Loads the state of the Notemat application from a file.
     *
     * @param notemat  the Notemat instance to load into
     * @param filePath the path to the file
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if a class cannot be found
     */
    public static void loadFromFile(Notemat notemat, String filePath)
            throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                switch (entry.getName()) {
                    case TEXT_FILE:
                        loadTextContent(notemat, zis);
                        break;
                    case IMAGES_FILE:
                        loadImages(notemat, zis);
                        break;
                }
                zis.closeEntry();
            }
        }
    }

    private static void loadTextContent(Notemat notemat, ZipInputStream zis)
            throws IOException {
        notemat.getTextPane().setText("");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        RTFEditorKit kit = new RTFEditorKit();
        try {
            kit.read(bais, notemat.getTextPane().getDocument(), 0);
        } catch (BadLocationException e) {
            throw new IOException("Error loading text content", e);
        } finally {
            bais.close();
            baos.close();
        }
    }

    private static void loadImages(Notemat notemat, ZipInputStream zis)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }

        // Remove existing images
        Component[] components = notemat.getContentLayeredPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof ImageComponent) {
                notemat.getContentLayeredPane().remove(comp);
            }
        }

        // Load new images
        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
             ObjectInputStream ois = new ObjectInputStream(bais)) {

            @SuppressWarnings("unchecked")
            ArrayList<ImageData> imageDataList =
                    (ArrayList<ImageData>) ois.readObject();

            for (ImageData imageData : imageDataList) {
                BufferedImage originalImage = imageData.getOriginalImage();
                BufferedImage displayImage = imageData.getDisplayImage();

                // Create new ImageComponent with both original and display images
                ImageIcon displayIcon = new ImageIcon(displayImage);
                ImageComponent imgComp = new ImageComponent(displayIcon, originalImage);

                // Set the bounds to match the saved position and size
                imgComp.setBounds(
                        imageData.x,
                        imageData.y,
                        imageData.width,
                        imageData.height
                );

                notemat.getContentLayeredPane().add(imgComp, JLayeredPane.PALETTE_LAYER);
            }
        }

        notemat.getContentLayeredPane().revalidate();
        notemat.getContentLayeredPane().repaint();
    }
}
