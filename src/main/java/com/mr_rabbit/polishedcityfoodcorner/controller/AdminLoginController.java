package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import com.mr_rabbit.polishedcityfoodcorner.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AdminLoginController {

    private static final String ADMIN_USERNAME = "cityfood";
    private static final String ADMIN_PASSWORD = "rabbit";

    @FXML
    private TextField adminusername;

    @FXML
    private PasswordField adminpassword;

    @FXML
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            if (adminusername.getScene() != null) {
                adminusername.getParent().requestFocus();
            }
        });
    }

    @FXML
    private void adminenterbtn(ActionEvent event) throws IOException {
        String username = adminusername.getText() == null ? "" : adminusername.getText().trim();
        String password = adminpassword.getText() == null ? "" : adminpassword.getText();

        if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
            SceneSwitcher sceneSwitcher = new SceneSwitcher();
            sceneSwitcher.switchscene(event, "AdminDashLayout.fxml");
        } else {
            AlertHelper.error("Login Failed", "Incorrect username or password.");
        }
    }

    @FXML
    private void backtologinpagebtn(ActionEvent event) throws IOException {
        SceneSwitcher sceneSwitcher = new SceneSwitcher();
        sceneSwitcher.switchscene(event, "UserLogin.fxml");
    }
}

