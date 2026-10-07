package com.mr_rabbit.polishedcityfoodcorner.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SceneSwitcher {
    public void switchscene(ActionEvent e, String path) throws IOException {
        String resolvedPath = path;
        if (!path.startsWith("/")) {
            if (path.startsWith("com/")) {
                resolvedPath = "/" + path;
            } else {
                resolvedPath = "/com/mr_rabbit/polishedcityfoodcorner/" + path;
            }
        }
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resolvedPath)));
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow(); //usages current stage rather than creating another
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}