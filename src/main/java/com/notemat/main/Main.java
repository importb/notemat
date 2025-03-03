package com.notemat.main;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Notemat notemat;
            if (args.length > 0) {
                File fileToOpen = new File(args[0]);
                notemat = new Notemat(fileToOpen);
            } else {
                notemat = new Notemat();
            }
        });
    }
}
