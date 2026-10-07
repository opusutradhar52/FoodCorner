package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.dao.EmployeeDAO;
import com.mr_rabbit.polishedcityfoodcorner.dao.ShiftDAO;
import com.mr_rabbit.polishedcityfoodcorner.model.Employee;
import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import com.mr_rabbit.polishedcityfoodcorner.util.AppConfig;
import com.mr_rabbit.polishedcityfoodcorner.util.PasswordUtil;
import com.mr_rabbit.polishedcityfoodcorner.util.SceneSwitcher;
import com.mr_rabbit.polishedcityfoodcorner.util.SessionManager;
import com.mr_rabbit.polishedcityfoodcorner.util.ValidationUtil;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.IOException;

public class UserLoginController {

    private static final double SLIDE_DISTANCE = 420.0;

    @FXML private AnchorPane slidePane;
    @FXML private Button createnewaccountbtn;

    // ---- Sign In (right panel) ----
    @FXML private TextField emploginemailfield;
    @FXML private PasswordField emploginpasswordfield;
    @FXML private ComboBox<String> emploginrolecombox;

    // ---- Sign Up (left panel) ----
    @FXML private TextField empregnamefield;
    @FXML private TextField empregemailfield;
    @FXML private TextField empregmobilefield;
    @FXML private PasswordField empregpasswordfield;
    @FXML private ComboBox<String> empregrolecombox;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ShiftDAO shiftDAO = new ShiftDAO();

    private boolean showingSignUp = false;

    @FXML
    private void initialize() {
        emploginrolecombox.setItems(FXCollections.observableArrayList("Salesman", "Inventory Manager"));
        empregrolecombox.setItems(FXCollections.observableArrayList("Salesman", "Inventory Manager"));
        
        javafx.application.Platform.runLater(() -> {
            if (slidePane.getParent() != null) {
                slidePane.getParent().requestFocus();
            }
        });
    }

    @FXML
    private void adminloginbtn(ActionEvent event) throws IOException {
        SceneSwitcher sceneSwitcher = new SceneSwitcher();
        sceneSwitcher.switchscene(event, "com/mr_rabbit/polishedcityfoodcorner/AdminLogin.fxml");
    }

    @FXML
    private void createnewaccountbtn(ActionEvent event) {
        showingSignUp = !showingSignUp;
        double target = showingSignUp ? SLIDE_DISTANCE : 0.0;

        TranslateTransition transition = new TranslateTransition(Duration.millis(400), slidePane);
        transition.setToX(target);
        transition.play();

        createnewaccountbtn.setText(showingSignUp ? "Sign In" : "Create New Account");
    }

    @FXML
    private void forgotpassbtn(ActionEvent event) {
        javafx.scene.control.Dialog<javafx.util.Pair<String, String>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Verify your identity");

        javafx.scene.control.ButtonType verifyButtonType = new javafx.scene.control.ButtonType("Verify", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(verifyButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 50, 10, 10));

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField mobileField = new TextField();
        mobileField.setPromptText("Mobile");

        grid.add(new javafx.scene.control.Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new javafx.scene.control.Label("Mobile:"), 0, 1);
        grid.add(mobileField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        javafx.application.Platform.runLater(emailField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == verifyButtonType) {
                return new javafx.util.Pair<>(emailField.getText(), mobileField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pair -> {
            String email = pair.getKey() == null ? "" : pair.getKey().trim();
            String mobile = pair.getValue() == null ? "" : pair.getValue().trim();

            if (email.isEmpty() || mobile.isEmpty()) {
                AlertHelper.warning("Missing Info", "Email and Mobile are required.");
                return;
            }

            Employee employee = employeeDAO.findByEmailAndMobile(email, mobile);
            if (employee == null) {
                AlertHelper.error("Verification Failed", "No account found matching this email and mobile.");
            } else {
                showPasswordResetDialog(employee);
            }
        });
    }

    private void showPasswordResetDialog(Employee employee) {
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Enter your new password for " + employee.getName());

        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 50, 10, 10));

        PasswordField passField = new PasswordField();
        passField.setPromptText("New Password");

        grid.add(new javafx.scene.control.Label("New Password:"), 0, 0);
        grid.add(passField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        javafx.application.Platform.runLater(passField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return passField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPass -> {
            if (newPass.trim().isEmpty()) {
                AlertHelper.warning("Invalid", "Password cannot be empty.");
                return;
            }
            String hashed = PasswordUtil.hash(newPass.trim());
            if (employeeDAO.updatePassword(employee.getId(), hashed)) {
                AlertHelper.info("Success", "Password updated successfully! You can now log in.");
            } else {
                AlertHelper.error("Error", "Failed to update password. Please try again later.");
            }
        });
    }

    @FXML
    private void signinbtn(ActionEvent event) throws IOException {
        String email = safeText(emploginemailfield);
        String password = emploginpasswordfield.getText() == null ? "" : emploginpasswordfield.getText();
        String role = emploginrolecombox.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null) {
            AlertHelper.warning("Missing Information", "Please enter your email, password and role.");
            return;
        }

        Employee employee = employeeDAO.findForLogin(email, role);
        if (employee == null || !PasswordUtil.matches(password, employee.getPasswordHash())) {
            AlertHelper.error("Login Failed", "No matching account found, or the password is incorrect.");
            return;
        }

        switch (employee.getStatus()) {
            case "Pending" -> {
                AlertHelper.info("Approval Pending", "Your account is still waiting for admin approval.");
                return;
            }
            case "Blocked" -> {
                AlertHelper.error("Account Blocked", "Your account has been blocked. Please contact the admin.");
                return;
            }
            default -> {
                // Approved - continue below.
            }
        }

        if (!AppConfig.ALLOW_MULTIPLE_LOGIN_PER_DAY && shiftDAO.hasCheckedInToday(employee.getEmployeeCode())) {
            AlertHelper.warning("Already Logged In", "You have already checked in today. See you tomorrow!");
            return;
        }

        shiftDAO.checkIn(employee.getEmployeeCode(), employee.getName(), employee.getRole());
        SessionManager.login(employee.getEmployeeCode(), employee.getName(), employee.getRole());

        if ("Inventory Manager".equals(role)) {
            SceneSwitcher sceneSwitcher = new SceneSwitcher();
            sceneSwitcher.switchscene(event, "InventoryManage.fxml");
        } else {
            SceneSwitcher sceneSwitcher = new SceneSwitcher();
            sceneSwitcher.switchscene(event, "SaleItems.fxml");
        }
    }

    @FXML
    private void registerbtn(ActionEvent event) {
        String name = safeText(empregnamefield);
        String email = safeText(empregemailfield);
        String mobile = safeText(empregmobilefield);
        String password = empregpasswordfield.getText() == null ? "" : empregpasswordfield.getText();
        String role = empregrolecombox.getValue();

        StringBuilder errors = new StringBuilder();
        if (!ValidationUtil.isValidName(name)) {
            errors.append("- Name must be First Middle Surname, each word starting with a capital letter.\n");
        }
        if (!ValidationUtil.isValidGmail(email)) {
            errors.append("- Email must be a valid gmail address (lowercase letters, digits, underscore, @gmail.com).\n");
        }
        if (!ValidationUtil.isValidMobile(mobile)) {
            errors.append("- Mobile number must start with 017/019/016/015/018 and be 11 digits long.\n");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            errors.append("- Password must be at least 6 characters and include one of @ # $ & ! ? % *.\n");
        }
        if (role == null) {
            errors.append("- Please select a role.\n");
        }

        if (errors.length() > 0) {
            AlertHelper.error("Please fix the following", errors.toString());
            return;
        }

        if (employeeDAO.emailExists(email)) {
            AlertHelper.error("Registration Failed", "This email is already registered.");
            return;
        }

        boolean success = employeeDAO.registerEmployee(name, email, mobile, PasswordUtil.hash(password), role);
        if (!success) {
            AlertHelper.error("Registration Failed", "Something went wrong while saving your account. Please try again.");
            return;
        }

        AlertHelper.info("Registration Successful",
                "your registration is successful. please wait for admin approval.");

        clearRegistrationForm();
        if (showingSignUp) {
            createnewaccountbtn(event);
        }
    }

    private void clearRegistrationForm() {
        empregnamefield.clear();
        empregemailfield.clear();
        empregmobilefield.clear();
        empregpasswordfield.clear();
        empregrolecombox.setValue(null);
    }

    private String safeText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}

