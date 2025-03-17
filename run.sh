#!/bin/bash

MODULE_PATH="`find ./ -name javafx-base-20.0.2-linux.jar`:`find ./ -name javafx-controls-20.0.2-linux.jar`:`find ./ -name javafx-fxml-20.0.2-linux.jar`:`find ./ -name javafx-graphics-20.0.2-linux.jar`:`find ./ -name javafx-swing-20.0.2-linux.jar`"

java --module-path "$MODULE_PATH" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.swing -jar notematjavafx-1.0-SNAPSHOT.jar
