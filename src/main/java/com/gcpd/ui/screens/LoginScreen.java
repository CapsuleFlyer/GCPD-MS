package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.bl.model.User;
import com.gcpd.db.UserDAO;
import com.gcpd.ui.MainApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * LoginScreen — Gotham themed (aligned with BaseScreen UI)
 */
public class LoginScreen {

    private final UserDAO userDAO = new UserDAO();

    public VBox getView() {

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));

        // 🦇 Gotham background
        root.setStyle("-fx-background-color: #0b0f1a;");

        // Title
        Text title = new Text("GOTHAM SYSTEM");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setStyle("-fx-fill: #facc15;");

        Text subtitle = new Text("Gotham City Police Department Management System");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setStyle("-fx-fill: #cbd5e1;");

        // Card
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(420);

        // 🧱 Gotham panel styling
        card.setStyle(
                "-fx-background-color: #111827;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 12, 0, 0, 3);"
        );

        Text loginTitle = new Text("Officer Login");
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        loginTitle.setStyle("-fx-fill: #f8fafc;");

        // User ID
        Label userLabel = styledLabel("User ID");
        TextField userField = new TextField();
        userField.setPromptText("Enter your User ID");
        styleField(userField);

        // Password
        Label passLabel = styledLabel("Password");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        styleField(passField);

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12;");

        // Login button (Gotham accent)
        Button loginBtn = new Button("LOGIN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setCursor(Cursor.HAND);

        loginBtn.setStyle(
                "-fx-background-color: #facc15;" +
                        "-fx-text-fill: #0b0f1a;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 10;"
        );

        loginBtn.setOnAction(e -> {
            String uid = userField.getText().trim();
            String pwd = passField.getText().trim();

            if (uid.isEmpty() || pwd.isEmpty()) {
                errorLabel.setText("Please enter both User ID and Password.");
                return;
            }

            User user = userDAO.authenticate(uid, pwd);

            if (user == null) {
                errorLabel.setText("Invalid credentials.");
                passField.clear();
            } else {
                SessionManager.getInstance().createSession(user);
                routeToDashboard(user);
            }
        });

        passField.setOnAction(e -> loginBtn.fire());

        card.getChildren().addAll(
                loginTitle,
                userLabel, userField,
                passLabel, passField,
                errorLabel,
                loginBtn
        );

        Text hint = new Text("Demo: USR001/admin123 • USR002/gordon123 • USR003/bullock123");
        hint.setStyle("-fx-fill: #64748b; -fx-font-size: 11;");

        root.getChildren().addAll(title, subtitle, card, hint);
        return root;
    }

    private void routeToDashboard(User user) {
        switch (user.getRole()) {
            case "Detective" -> MainApp.setScene(new DetectiveDashboard().getView());
            case "Sergeant" -> MainApp.setScene(new SergeantDashboard().getView());
            case "Commissioner" -> MainApp.setScene(new CommissionerDashboard().getView());
            case "ForensicAnalyst" -> MainApp.setScene(new ForensicAnalystDashboard().getView());
            case "EvidenceCustodian" -> MainApp.setScene(new EvidenceCustodianDashboard().getView());
            case "SystemAdmin" -> MainApp.setScene(new AdminDashboard().getView());
            default -> MainApp.setScene(new DetectiveDashboard().getView());
        }
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");
        return l;
    }

    private void styleField(TextField f) {
        f.setStyle(
                "-fx-background-color: #1f2937;" +
                        "-fx-text-fill: #f8fafc;" +
                        "-fx-prompt-text-fill: #64748b;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8;"
        );
    }
}