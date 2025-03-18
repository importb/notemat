package com.notemat;

import com.notemat.Components.EditorWindow;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        List<String> params = getParameters().getRaw();
        EditorWindow editorWindow;
        if (!params.isEmpty()) {
            String filePath = params.getFirst();
            editorWindow = new EditorWindow(filePath);
        } else {
            editorWindow = new EditorWindow();
        }
        editorWindow.show();
    }
}
