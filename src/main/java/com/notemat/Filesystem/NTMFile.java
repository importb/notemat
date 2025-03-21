package com.notemat.Filesystem;

import com.notemat.Components.EditorWindow;
import com.notemat.Components.ImageComponent;
import com.notemat.Components.ToolBar;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.InlineCssTextArea;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;

/**
 * Handles saving and loading of EditorWindow files in a custom format.
 * The file format is a ZIP archive containing text content (with style spans)
 * and serialized image data.
 */
public class NTMFile {
    private static final String TEXT_FILE = "content.dat";
    private static final String IMAGES_FILE = "images.dat";
    private static boolean changedSinceLastSave = false;
    private static String lastSavedPath = null;

    /**
     * Serializable class to store image properties and image bytes.
     */
    private static class ImageData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        public final byte[] imageBytes;
        public final double layoutX;
        public final double layoutY;
        public final double width;
        public final double height;

        public ImageData(BufferedImage image, double layoutX, double layoutY, double width, double height) throws IOException {
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
     * Serializable representation for an individual style span.
     * Indicates that the next {@code length} characters should have the
     * specified inline CSS represented by {@code style}.
     */
    private static class StyleSpanData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        public final int length;
        public final String style;

        public StyleSpanData(int length, String style) {
            this.length = length;
            this.style = style;
        }
    }

    /**
     * Serializable representation of the styled document.
     */
    private static class StyledDocument implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        public final String text;
        public final ArrayList<StyleSpanData> spans;

        public StyledDocument(String text, ArrayList<StyleSpanData> spans) {
            this.text = text;
            this.spans = spans;
        }
    }

    /**
     * Saves the current state of the EditorWindow to a file.
     *
     * @param editor   the EditorWindow instance to save
     * @param filePath the path to the file
     * @throws IOException if an I/O error occurs
     */
    public static void saveToFile(EditorWindow editor, String filePath) throws IOException {

        if (!filePath.endsWith(".ntm")) {
            filePath += ".ntm";
        }
        try (FileOutputStream fos = new FileOutputStream(filePath); ZipOutputStream zos = new ZipOutputStream(fos)) {
            // Save the file path and update changedSinceLastSave.
            lastSavedPath = filePath;
            changedSinceLastSave = false;

            // Save styled text content and images.
            saveTextContent(editor, zos);
            saveImages(editor, zos);
        }
    }

    /**
     * Saves the styled text content, including the plain text and its style spans.
     *
     * @param editor the EditorWindow to retrieve content from.
     * @param zos    the ZipOutputStream to write to.
     * @throws IOException if an I/O error occurs.
     */
    private static void saveTextContent(EditorWindow editor, ZipOutputStream zos) throws IOException {
        ZipEntry textEntry = new ZipEntry(TEXT_FILE);
        zos.putNextEntry(textEntry);

        InlineCssTextArea richTextArea = editor.getRichTextArea();
        String text = richTextArea.getText();
        StyleSpans<String> styleSpans = richTextArea.getStyleSpans(0, text.length());

        // Convert the style spans to a serializable list.
        ArrayList<StyleSpanData> spanDataList = new ArrayList<>();
        for (StyleSpan<String> span : styleSpans) {
            spanDataList.add(new StyleSpanData(span.getLength(), span.getStyle()));
        }
        StyledDocument styledDoc = new StyledDocument(text, spanDataList);

        // Write the StyledDocument via an ObjectOutputStream.
        ObjectOutputStream oos = new ObjectOutputStream(zos);
        oos.writeObject(styledDoc);
        oos.flush();
        zos.closeEntry();
    }

    /**
     * Saves all images added to the editor as a serialized ArrayList.
     *
     * @param editor the EditorWindow containing image components.
     * @param zos    the ZipOutputStream to write image data to.
     * @throws IOException if an I/O error occurs.
     */
    private static void saveImages(EditorWindow editor, ZipOutputStream zos) throws IOException {
        ZipEntry imagesEntry = new ZipEntry(IMAGES_FILE);
        zos.putNextEntry(imagesEntry);

        ArrayList<ImageData> imageDataList = new ArrayList<>();

        editor.getImageLayer().getChildren().forEach(node -> {
            if (node instanceof ImageComponent imageComponent) {
                try {
                    // Use the currently displayed image.
                    Image fxImage = imageComponent.getImage();
                    BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
                    ImageData data = new ImageData(bImage, imageComponent.getLayoutX(), imageComponent.getLayoutY(), imageComponent.getWidth(), imageComponent.getHeight());
                    imageDataList.add(data);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Write ArrayList<ImageData> object.
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
     * Restores both the styled text content and images.
     *
     * @param editor   the EditorWindow instance to load into.
     * @param filePath the path to the file.
     * @throws IOException            if an I/O error occurs.
     * @throws ClassNotFoundException if a required class is not found.
     */
    public static void loadFromFile(EditorWindow editor, String filePath) throws IOException, ClassNotFoundException {

        try (FileInputStream fis = new FileInputStream(filePath); ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                switch (entry.getName()) {
                    case TEXT_FILE -> loadTextContent(editor, zis);
                    case IMAGES_FILE -> loadImages(editor, zis);
                }
                zis.closeEntry();

                // Save the file path and update changedSinceLastSave.
                lastSavedPath = filePath;
                changedSinceLastSave = false;
            }
        }
    }

    /**
     * Loads the styled text content and re-applies both the plain text and its style spans.
     *
     * @param editor the EditorWindow to load text into.
     * @param zis    the ZipInputStream from which to read.
     * @throws IOException            if an I/O error occurs.
     * @throws ClassNotFoundException if the StyledDocument class is not found.
     */
    private static void loadTextContent(EditorWindow editor, ZipInputStream zis) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(zis);
        StyledDocument styledDoc = (StyledDocument) ois.readObject();
        InlineCssTextArea richTextArea = editor.getRichTextArea();

        // Replace all text in the text area.
        richTextArea.replaceText(styledDoc.text);

        // Build a StyleSpans object using the saved spans.
        StyleSpansBuilder<String> builder = new StyleSpansBuilder<>();
        for (StyleSpanData spanData : styledDoc.spans) {
            builder.add(spanData.style, spanData.length);
        }

        // Apply the style spans. The total length should match the text.
        richTextArea.setStyleSpans(0, builder.create());
    }

    /**
     * Loads image data from the ZIP archive and recreates ImageComponents.
     *
     * @param editor the EditorWindow to load images into.
     * @param zis    the ZipInputStream from which to read.
     * @throws IOException            if an I/O error occurs.
     * @throws ClassNotFoundException if the ImageData class is not found.
     */
    private static void loadImages(EditorWindow editor, ZipInputStream zis) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        byte[] bytes = baos.toByteArray();

        // Remove existing ImageComponents.
        editor.getImageLayer().getChildren().removeIf(node -> node instanceof ImageComponent);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
            @SuppressWarnings("unchecked") ArrayList<ImageData> imageDataList = (ArrayList<ImageData>) ois.readObject();

            // Recreate ImageComponents from saved data.
            for (ImageData data : imageDataList) {
                BufferedImage bImage = data.getImage();
                Image fxImage = SwingFXUtils.toFXImage(bImage, null);
                ImageComponent imageComponent = new ImageComponent(fxImage);
                imageComponent.setLayoutX(data.layoutX);
                imageComponent.setLayoutY(data.layoutY);
                // Set width and height if your ImageComponent supports it.
                imageComponent.setPrefWidth(data.width);
                imageComponent.setPrefHeight(data.height);
                editor.getImageLayer().getChildren().add(imageComponent);
            }
        }
    }

    /**
     * Marks the document as changed and updates the filename label in the toolbar.
     *
     * @param toolBar the ToolBar instance to update.
     */
    public static void markChanged(ToolBar toolBar) {
        changedSinceLastSave = true;
        toolBar.updateFilenameLabel();
    }

    /**
     * Indicates whether the document has been changed since the last save.
     *
     * @return true if the document has unsaved changes; false otherwise.
     */
    public static boolean getChangedSinceLastSave() {
        return changedSinceLastSave;
    }

    /**
     * Retrieves the last saved file path.
     *
     * @return the last saved file path, or null if not set.
     */
    public static String getLastSavedPath() {
        return lastSavedPath;
    }
}
