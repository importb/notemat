package com.notemat;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (args.length > 0) {
                File fileToOpen = new File(args[0]);
                new Notemat(fileToOpen);
            } else {
                new Notemat();
            }
        });
    }
}
