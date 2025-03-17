package com.notemat.Filesystem;

import com.notemat.Components.EditorWindow;
import com.notemat.Components.ImageComponent;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

/**
 * Handles saving and loading of EditorWindow files in a custom format.
 */
public class NTMFile {
    private static final String TEXT_FILE = "content.txt";
    private static final String IMAGES_FILE = "images.dat";

    /**
     * Serializable class to store image properties and bytes.
     */
    private static class ImageData implements Serializable {
        private static final long serialVersionUID = 1L;

        public final byte[] imageBytes;
        public final double layoutX;
        public final double layoutY;
        public final double width;
        public final double height;

        public ImageData(BufferedImage image,
                         double layoutX,
                         double layoutY,
                         double width,
                         double height)
                throws IOException {
            this.layoutX = layoutX;
            this.layoutY = layoutY;
            this.width = width;
            this.height = height;

            // Save image as PNG bytes.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            this.imageBytes = baos.toByteArray();
        }

        public BufferedImage getImage() throws IOException {
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        }
    }

    /**
     * Saves the current state of the EditorWindow to a file.
     *
     * @param editor   the EditorWindow instance to save
     * @param filePath the path to the file
     * @throws IOException if an I/O error occurs
     */
    public static void saveToFile(EditorWindow editor, String filePath)
            throws IOException {
        if (!filePath.endsWith(".ntm")) {
            filePath += ".ntm";
        }

        try (FileOutputStream fos = new FileOutputStream(filePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Save text content.
            saveTextContent(editor, zos);

            // Save images.
            saveImages(editor, zos);
        }
    }

    private static void saveTextContent(EditorWindow editor,
                                        ZipOutputStream zos)
            throws IOException {
        ZipEntry textEntry = new ZipEntry(TEXT_FILE);
        zos.putNextEntry(textEntry);

        // Save current text content.
        String content = editor.getRichTextArea().getText();
        byte[] textBytes = content.getBytes();
        zos.write(textBytes, 0, textBytes.length);
        zos.closeEntry();
    }

    private static void saveImages(EditorWindow editor,
                                   ZipOutputStream zos)
            throws IOException {
        ZipEntry imagesEntry = new ZipEntry(IMAGES_FILE);
        zos.putNextEntry(imagesEntry);

        ArrayList<ImageData> imageDataList = new ArrayList<>();

        editor.getImageLayer().getChildren().forEach(node -> {
            if (node instanceof ImageComponent imageComponent) {
                try {
                    // Use the current displayed image.
                    Image fxImage = imageComponent.getImage();
                    BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
                    ImageData data = new ImageData(
                            bImage,
                            imageComponent.getLayoutX(),
                            imageComponent.getLayoutY(),
                            imageComponent.getWidth(),
                            imageComponent.getHeight()
                    );
                    imageDataList.add(data);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Write ArrayList of ImageData.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(imageDataList);
        }
        byte[] bytes = baos.toByteArray();
        zos.write(bytes, 0, bytes.length);
        zos.closeEntry();
    }

    /**
     * Loads the state of the EditorWindow from a file.
     *
     * @param editor   the EditorWindow instance to load into
     * @param filePath the path to the file
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if a class cannot be found
     */
    public static void loadFromFile(EditorWindow editor, String filePath)
            throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                switch (entry.getName()) {
                    case TEXT_FILE -> loadTextContent(editor, zis);
                    case IMAGES_FILE -> loadImages(editor, zis);
                }
                zis.closeEntry();
            }
        }
    }

    private static void loadTextContent(EditorWindow editor,
                                        ZipInputStream zis)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        String content = baos.toString();
        editor.getRichTextArea().replaceText(content);
    }

    private static void loadImages(EditorWindow editor,
                                   ZipInputStream zis)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        byte[] bytes = baos.toByteArray();

        // Remove existing ImageComponents.
        editor.getImageLayer().getChildren().removeIf(
                node -> node instanceof ImageComponent);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {

            @SuppressWarnings("unchecked")
            ArrayList<ImageData> imageDataList =
                    (ArrayList<ImageData>) ois.readObject();

            // Recreate ImageComponents from saved data.
            for (ImageData data : imageDataList) {
                BufferedImage bImage = data.getImage();
                Image fxImage = SwingFXUtils.toFXImage(bImage, null);
                ImageComponent imageComponent = new ImageComponent(fxImage);
                imageComponent.setLayoutX(data.layoutX);
                imageComponent.setLayoutY(data.layoutY);
                // Set width and height if your ImageComponent supports resizing.
                imageComponent.setPrefWidth(data.width);
                imageComponent.setPrefHeight(data.height);

                editor.getImageLayer().getChildren().add(imageComponent);
            }
        }
    }
}
