package com.gcpd.ui.screens;

import com.gcpd.db.UserDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * AdminDashboard — handles:
 * UC-07: Register System User
 */
public class AdminDashboard extends BaseScreen {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #1a1a2e;");
        tabs.getTabs().addAll(
            buildRegisterTab(),   // UC-07
            buildAllUsersTab()    // View all users
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        contentArea.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return root;
    }

    // ── UC-07: Register System User ──────────────────────────────────────────
    private Tab buildRegisterTab() {
        Tab tab = new Tab("➕  Register User");

        VBox box = card("UC-07 — Register New System User");
        box.setMaxWidth(550);

        Label idLabel = fieldLabel("User ID *");
        TextField idField = new TextField();
        idField.setPromptText("e.g. USR010");
        styleTextField(idField);

        Label nameLabel = fieldLabel("Full Name *");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. John Blake");
        styleTextField(nameField);

        Label passLabel = fieldLabel("Password *");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Set password");
        styleTextField(passField);

        Label roleLabel = fieldLabel("Role *");
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList(
            "Detective", "Sergeant", "Commissioner",
            "ForensicAnalyst", "EvidenceCustodian", "SystemAdmin"
        ));
        roleBox.setPromptText("Select role");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(roleBox);

        // Role-specific extra field
        Label extraLabel = fieldLabel("Role-Specific ID");
        TextField extraField = new TextField();
        extraField.setPromptText("Badge/Squad/Dept/Vault/Lab ID (if applicable)");
        styleTextField(extraField);

        roleBox.setOnAction(e -> {
            switch (roleBox.getValue() == null ? "" : roleBox.getValue()) {
                case "Detective"         -> { extraLabel.setText("Badge Number"); extraField.setPromptText("e.g. DET05"); }
                case "Sergeant"          -> { extraLabel.setText("Squad ID");     extraField.setPromptText("e.g. SQ2"); }
                case "Commissioner"      -> { extraLabel.setText("Department ID"); extraField.setPromptText("e.g. DEPT2"); }
                case "EvidenceCustodian" -> { extraLabel.setText("Vault ID");     extraField.setPromptText("e.g. VLT2"); }
                case "ForensicAnalyst"   -> { extraLabel.setText("Lab ID");       extraField.setPromptText("e.g. LAB2"); }
                default                  -> { extraLabel.setText("Role-Specific ID"); extraField.setPromptText("N/A"); }
            }
        });

        Label status = statusLabel();
        Button registerBtn = primaryButton("Register User");

        registerBtn.setOnAction(e -> {
            String uid  = idField.getText().trim();
            String name = nameField.getText().trim();
            String pass = passField.getText().trim();
            String role = roleBox.getValue();

            if (uid.isEmpty() || name.isEmpty() || pass.isEmpty() || role == null) {
                showError(status, "User ID, Name, Password, and Role are required.");
                return;
            }
            if (userDAO.userExists(uid)) {
                showError(status, "User ID '" + uid + "' already exists.");
                return;
            }

            String extra = extraField.getText().trim();
            boolean ok = userDAO.insertUser(
                uid, name, role, pass,
                "Detective".equals(role) ? extra : null,
                "Sergeant".equals(role) ? extra : null,
                "Commissioner".equals(role) ? extra : null,
                "EvidenceCustodian".equals(role) ? extra : null,
                "ForensicAnalyst".equals(role) ? extra : null
            );

            if (ok) {
                showSuccess(status, "User " + uid + " (" + role + ") registered successfully.");
                idField.clear(); nameField.clear(); passField.clear();
                roleBox.setValue(null); extraField.clear();
            } else {
                showError(status, "Registration failed. Check DB.");
            }
        });

        box.getChildren().addAll(
            idLabel, idField, nameLabel, nameField,
            passLabel, passField, roleLabel, roleBox,
            extraLabel, extraField, registerBtn, status
        );

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e;");
        tab.setContent(scroll);
        return tab;
    }

    // ── View All Users ───────────────────────────────────────────────────────
    private Tab buildAllUsersTab() {
        Tab tab = new Tab("👥  All Users");

        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("Registered System Users");
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: #16213e;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        String[] cols = {"User ID", "Name", "Role"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().length > idx ? d.getValue()[idx] : ""));
            table.getColumns().add(col);
        }

        Button refreshBtn = secondaryButton("Refresh");
        refreshBtn.setOnAction(e -> loadUsers(table));
        loadUsers(table);

        box.getChildren().addAll(title, table, refreshBtn);
        tab.setContent(box);
        return tab;
    }

    private void loadUsers(TableView<String[]> table) {
        List<String[]> users = userDAO.getAllUsers().stream()
            .map(u -> new String[]{u.getUserID(), u.getName(), u.getRole()})
            .toList();
        table.setItems(FXCollections.observableArrayList(users));
    }
}
