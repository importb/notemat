package com.notemat.Filesystem;

import com.notemat.Components.EditorWindow;
import java.io.*;


/**
 * Provides functions to import and export plain text files.
 * This class handles saving the content of an EditorWindow as a .txt file
 * and importing text from a .txt file into the EditorWindow.
 */
public class TXTFile {
    /**
     * Saves the contents of the EditorWindow's rich text area as a .txt file.
     *
     * @param editorWindow the EditorWindow containing the text to save
     * @param filePath     the full path of the file to write to (e.g., "document.txt")
     * @throws IOException if an I/O error occurs
     */
    public static void saveToFile(EditorWindow editorWindow, String filePath)
            throws IOException {
        String content = editorWindow.getRichTextArea().getText();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    /**
     * Imports text from a .txt file and loads it into the given EditorWindow.
     *
     * @param editorWindow the EditorWindow to load the text into
     * @param filePath     the full path of the text file to import (e.g., "document.txt")
     * @throws IOException if an I/O error occurs during reading
     */
    public static void importFromFile(EditorWindow editorWindow, String filePath)
            throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        editorWindow.getRichTextArea().replaceText(contentBuilder.toString());
    }
}
