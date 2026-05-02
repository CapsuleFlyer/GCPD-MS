package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.bl.model.User;
import com.gcpd.db.UserDAO;
import com.gcpd.ui.MainApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * LoginScreen — UC-08: Authenticate User Login.
 * All roles log in here; system routes them to their dashboard.
 */
public class LoginScreen {

    private final UserDAO userDAO = new UserDAO();

    public VBox getView() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Title
        Text title = new Text("GCPD");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setStyle("-fx-fill: #e94560;");

        Text subtitle = new Text("Gotham City Police Department Management System");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setStyle("-fx-fill: #aaaaaa;");

        // Card
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(420);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 4);");

        Text loginTitle = new Text("Officer Login");
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        loginTitle.setStyle("-fx-fill: white;");

        // User ID field
        Label userLabel = styledLabel("User ID");
        TextField userField = new TextField();
        userField.setPromptText("Enter your User ID");
        styleField(userField);

        // Password field
        Label passLabel = styledLabel("Password");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        styleField(passField);

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12;");

        // Login button
        Button loginBtn = new Button("Login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; " +
                         "-fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 6; " +
                         "-fx-padding: 10;");
        loginBtn.setCursor(javafx.scene.Cursor.HAND);

        loginBtn.setOnAction(e -> {
            String uid = userField.getText().trim();
            String pwd = passField.getText().trim();

            if (uid.isEmpty() || pwd.isEmpty()) {
                errorLabel.setText("Please enter both User ID and Password.");
                return;
            }

            User user = userDAO.authenticate(uid, pwd);
            if (user == null) {
                errorLabel.setText("Invalid credentials. Please try again.");
                passField.clear();
            } else {
                SessionManager.getInstance().createSession(user);
                routeToDashboard(user);
            }
        });

        // Allow Enter key to trigger login
        passField.setOnAction(e -> loginBtn.fire());

        card.getChildren().addAll(loginTitle, userLabel, userField,
                passLabel, passField, errorLabel, loginBtn);

        // Hint
        Text hint = new Text("Demo logins: USR001/admin123 • USR002/gordon123 • USR003/bullock123");
        hint.setStyle("-fx-fill: #555577; -fx-font-size: 11;");

        root.getChildren().addAll(title, subtitle, card, hint);
        return root;
    }

    private void routeToDashboard(User user) {
        switch (user.getRole()) {
            case "Detective"         -> MainApp.setScene(new DetectiveDashboard().getView());
            case "Sergeant"          -> MainApp.setScene(new SergeantDashboard().getView());
            case "Commissioner"      -> MainApp.setScene(new CommissionerDashboard().getView());
            case "ForensicAnalyst"   -> MainApp.setScene(new ForensicAnalystDashboard().getView());
            case "EvidenceCustodian" -> MainApp.setScene(new EvidenceCustodianDashboard().getView());
            case "SystemAdmin"       -> MainApp.setScene(new AdminDashboard().getView());
            default -> MainApp.setScene(new DetectiveDashboard().getView());
        }
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12;");
        return l;
    }

    private void styleField(TextField f) {
        f.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                   "-fx-prompt-text-fill: #555577; -fx-background-radius: 6; " +
                   "-fx-padding: 8; -fx-font-size: 13;");
    }
}
