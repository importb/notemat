package com.notemat.main;// com.notemat.main.Main.java

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Notemat noteMat;
            if (args.length > 0) {
                File fileToOpen = new File(args[0]);
                noteMat = new Notemat(fileToOpen);
            } else {
                noteMat = new Notemat();
            }
        });
    }
}
