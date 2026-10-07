package com.mr_rabbit.polishedcityfoodcorner;

import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();
            Platform.runLater(() -> AlertHelper.error("Unexpected Error",
                    throwable.getMessage() != null ? throwable.getMessage() : throwable.toString()));
        });

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("UserLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("City Food Corner");
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.getIcons().add(new Image(HelloApplication.class.getResourceAsStream(
                "/IconAndPicture/iconlogo.png")));
        stage.setScene(scene);
        stage.show();
    }
}
