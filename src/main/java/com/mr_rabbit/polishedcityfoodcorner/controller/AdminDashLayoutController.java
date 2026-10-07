package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import com.mr_rabbit.polishedcityfoodcorner.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AdminDashLayoutController implements AdminContentHost {

    @FXML
    private AnchorPane carieranchor;

    @FXML
    private void initialize() {
        // Per the spec, the dashboard opens showing Cash Flow by default.
        loadContent("CashFlow.fxml");
    }

    @Override
    public void loadContent(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/mr_rabbit/polishedcityfoodcorner/" + fxmlFileName));
            Parent content = loader.load();

            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);

            carieranchor.getChildren().setAll(content);

            Object controller = loader.getController();
            if (controller instanceof HostAware) {
                ((HostAware) controller).setHost(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.error("Navigation Error", "Could not open " + fxmlFileName + ": " + e.getMessage());
        }
    }

    @FXML
    private void cashflowbtn(ActionEvent event) {
        loadContent("CashFlow.fxml");
    }

    @FXML
    private void admininventorybtn(ActionEvent event) {
        loadContent("AdminInventory.fxml");
    }

    @FXML
    private void historybtn(ActionEvent event) {
        loadContent("History.fxml");
    }

    @FXML
    private void employeebtn(ActionEvent event) {
        loadContent("EmployeeAcces.fxml");
    }

    @FXML
    private void backtologinpagebtn(ActionEvent event) throws IOException {
        SceneSwitcher sceneSwitcher = new SceneSwitcher();
        sceneSwitcher.switchscene(event, "UserLogin.fxml");

    }
}

