module com.mr_rabbit.polishedcityfoodcorner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens com.mr_rabbit.polishedcityfoodcorner to javafx.fxml;
    exports com.mr_rabbit.polishedcityfoodcorner;

    opens com.mr_rabbit.polishedcityfoodcorner.controller to javafx.fxml;
    exports com.mr_rabbit.polishedcityfoodcorner.controller;
}