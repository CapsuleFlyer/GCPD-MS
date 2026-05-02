package com.gcpd.ui.screens;

import com.gcpd.db.UserDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class AdminDashboard extends BaseScreen {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public Parent getView() {

        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #0b0f1a;");
        tabs.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        tabs.getTabs().addAll(
                buildRegisterTab(),
                buildAllUsersTab()
        );

        tabs.getTabs().forEach(t -> t.setClosable(false));

        VBox.setVgrow(tabs, Priority.ALWAYS);
        contentArea.setFillWidth(true);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(tabs);

        return root;
    }

    // ── REGISTER TAB ──────────────────────────────────────────
    private Tab buildRegisterTab() {

        Tab tab = new Tab("➕ Register User");

        VBox box = card("UC-07 — Register New System User");

        Label idLabel = fieldLabel("User ID *");
        TextField idField = new TextField();
        idField.setPromptText("e.g. USR007");
        styleTextField(idField);

        Label nameLabel = fieldLabel("Full Name *");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. John Blake");
        styleTextField(nameField);

        Label passLabel = fieldLabel("Password *");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter a secure password");
        styleTextField(passField);

        Label roleLabel = fieldLabel("Role *");
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList(
                "Detective", "Sergeant", "Commissioner",
                "ForensicAnalyst", "EvidenceCustodian", "SystemAdmin"
        ));
        roleBox.setPromptText("Select a role...");
        styleComboBox(roleBox);

        Label extraLabel = fieldLabel("Role-Specific ID");
        TextField extraField = new TextField();
        extraField.setPromptText("e.g. DET02 / SQ2 / LAB2 / VLT2 / DEPT2");
        extraField.setDisable(true); // disabled until role is selected
        styleTextField(extraField);

        // Update placeholder and enable/disable based on selected role
        roleBox.setOnAction(e -> {
            String selected = roleBox.getValue();
            if (selected == null) return;
            extraField.setDisable(false);
            switch (selected) {
                case "Detective"         -> extraField.setPromptText("Badge Number — e.g. DET02");
                case "Sergeant"          -> extraField.setPromptText("Squad ID — e.g. SQ2");
                case "Commissioner"      -> extraField.setPromptText("Department ID — e.g. DEPT2");
                case "ForensicAnalyst"   -> extraField.setPromptText("Lab ID — e.g. LAB2");
                case "EvidenceCustodian" -> extraField.setPromptText("Vault ID — e.g. VLT2");
                case "SystemAdmin"       -> {
                    extraField.setPromptText("No role-specific ID required");
                    extraField.setDisable(true);
                }
            }
        });

        Label status = statusLabel();
        Button registerBtn = primaryButton("Register User");

        box.getChildren().addAll(
                idLabel, idField,
                nameLabel, nameField,
                passLabel, passField,
                roleLabel, roleBox,
                extraLabel, extraField,
                registerBtn,
                status
        );

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0b0f1a; -fx-background-color: #0b0f1a;");

        tab.setContent(scroll);
        return tab;
    }

    // ── USERS TAB ────────────────────────────────────────────
    private Tab buildAllUsersTab() {

        Tab tab = new Tab("👥 All Users");

        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20));
        panel.setStyle(
                "-fx-background-color: #111827;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 12;"
        );

        Label title = new Label("Registered System Users");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setStyle("-fx-text-fill: #facc15;");

        TableView<String[]> table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setStyle(
                "-fx-background-color: #1f2937;" +
                        "-fx-control-inner-background: #1f2937;" +
                        "-fx-control-inner-background-alt: #253347;" +
                        "-fx-table-cell-border-color: #2d3748;" +
                        "-fx-table-header-border-color: #2d3748;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(38);

        String[] colNames = {"User ID", "Name", "Role"};

        for (int i = 0; i < colNames.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(colNames[i]);

            col.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().length > idx ? d.getValue()[idx] : ""
                    )
            );

            col.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        String bg = getIndex() % 2 == 0 ? "#1f2937" : "#253347";
                        setStyle("-fx-background-color: " + bg + ";");
                    } else {
                        setText(item);
                        String bg = getIndex() % 2 == 0 ? "#1f2937" : "#253347";
                        setStyle(
                                "-fx-background-color: " + bg + ";" +
                                        "-fx-text-fill: #f1f5f9;" +
                                        "-fx-font-size: 13px;" +
                                        "-fx-padding: 0 12px;"
                        );
                    }
                }
            });

            table.getColumns().add(col);
        }

        loadUsers(table);

        Platform.runLater(() -> applyHeaderStyles(table));

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                Platform.runLater(() -> applyHeaderStyles(table));
            }
        });

        Button refreshBtn = secondaryButton("⟳  Refresh");
        refreshBtn.setOnAction(e -> {
            loadUsers(table);
            Platform.runLater(() -> applyHeaderStyles(table));
        });

        HBox footer = new HBox(refreshBtn);
        footer.setStyle("-fx-alignment: center-right;");

        panel.getChildren().addAll(title, table, footer);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0b0f1a; -fx-background-color: #0b0f1a;");

        tab.setContent(scroll);
        return tab;
    }

    private void applyHeaderStyles(TableView<String[]> table) {
        table.lookupAll(".column-header").forEach(node ->
                node.setStyle(
                        "-fx-background-color: #1a2236;" +
                                "-fx-border-color: #334155;" +
                                "-fx-border-width: 0 1px 2px 0;"
                )
        );
        table.lookupAll(".column-header .label").forEach(node ->
                node.setStyle(
                        "-fx-text-fill: #facc15;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 12px;" +
                                "-fx-padding: 0 12px;"
                )
        );
        table.lookupAll(".column-header-background").forEach(node ->
                node.setStyle(
                        "-fx-background-color: #1a2236;" +
                                "-fx-border-color: #334155;" +
                                "-fx-border-width: 0 0 2px 0;"
                )
        );
        table.lookupAll(".corner").forEach(node ->
                node.setStyle("-fx-background-color: #1a2236;")
        );
    }

    private void loadUsers(TableView<String[]> table) {
        List<String[]> users = userDAO.getAllUsers().stream()
                .map(u -> new String[]{u.getUserID(), u.getName(), u.getRole()})
                .toList();
        table.setItems(FXCollections.observableArrayList(users));
    }
}