package com.notemat;

import com.notemat.Components.EditorWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        EditorWindow editorWindow = new EditorWindow();
        editorWindow.show();
    }
}
