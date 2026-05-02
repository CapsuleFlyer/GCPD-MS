package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.ui.MainApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * BaseScreen — Gotham-themed shared UI structure
 * Fixes readability issues + applies consistent dark UI styling
 */
public abstract class BaseScreen {

    protected BorderPane root;
    protected VBox contentArea;

    public BaseScreen() {

        // 🦇 Gotham background (fixed contrast)
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0b0f1a;");

        root.setTop(buildNavbar());

        contentArea = new VBox(15);
        contentArea.setFillWidth(true);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        contentArea.setPadding(new Insets(25));
        contentArea.setStyle("-fx-background-color: transparent;");
        root.setCenter(contentArea);
    }

    private HBox buildNavbar() {

        HBox nav = new HBox(20);
        nav.setPadding(new Insets(12, 20, 12, 20));
        nav.setAlignment(Pos.CENTER_LEFT);

        // Gotham panel navbar
        nav.setStyle(
                "-fx-background-color: #111827;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 12, 0, 0, 3);"
        );

        // 🦇 Brand
        Label brand = new Label("GOTHAM SYSTEM");
        brand.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        brand.setStyle("-fx-text-fill: #facc15;");

        // 👤 Role display (fixed visibility)
        Label role = new Label(
                "| " + SessionManager.getInstance().getCurrentRole()
                        + " — "
                        + SessionManager.getInstance().getCurrentUser().getName()
        );
        role.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 🔴 Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setCursor(Cursor.HAND);
        logoutBtn.setStyle(
                "-fx-background-color: #dc2626;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 14;"
        );

        logoutBtn.setOnAction(e -> {
            SessionManager.getInstance().endSession();
            MainApp.showLogin();
        });

        nav.getChildren().addAll(brand, role, spacer, logoutBtn);
        return nav;
    }

    // -----------------------------
    // UI HELPERS (THEMED FIXES)
    // -----------------------------

    protected Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        l.setStyle("-fx-text-fill: #f8fafc;");
        return l;
    }

    protected Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");
        return l;
    }

    // 🔵 FIXED TextField (no invisible text)
    protected void styleTextField(TextField f) {
        f.setStyle(
                "-fx-background-color: #1f2937;" +
                        "-fx-text-fill: #f8fafc;" +
                        "-fx-prompt-text-fill: #64748b;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 7;"
        );
    }

    // 🔵 FIXED TextArea
    protected void styleTextArea(TextArea f) {
        f.setStyle(
                "-fx-background-color: #1f2937;" +
                        "-fx-text-fill: #f8fafc;" +
                        "-fx-control-inner-background: #1f2937;" +
                        "-fx-prompt-text-fill: #64748b;" +
                        "-fx-background-radius: 6;"
        );
    }

    protected void styleComboBox(javafx.scene.control.ComboBox<?> c) {

        c.setStyle(
                "-fx-background-color: #1f2937;" +
                        "-fx-text-fill: #f8fafc;" +
                        "-fx-background-radius: 6;"
        );

        // 🔥 dropdown items fix (NO LAMBDA — avoids type errors)
        c.setCellFactory(listView -> new javafx.scene.control.ListCell() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle(
                            "-fx-text-fill: #f8fafc;" +
                                    "-fx-background-color: #111827;"
                    );
                }
            }
        });

        // selected value display
        c.setButtonCell(new javafx.scene.control.ListCell() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #f8fafc;");
                }
            }
        });
    }

    protected Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: #facc15;" +
                        "-fx-text-fill: #0b0f1a;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 18;"
        );
        b.setCursor(Cursor.HAND);
        return b;
    }

    protected Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: #1f2937;" +
                        "-fx-text-fill: #f8fafc;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 18;"
        );
        b.setCursor(Cursor.HAND);
        return b;
    }

    // 🧱 Gotham card design
    protected VBox card(String title) {
        VBox c = new VBox(12);
        c.setPadding(new Insets(20));

        c.setStyle(
                "-fx-background-color: #111827;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 12;"
        );

        if (title != null && !title.isBlank()) {
            Label t = new Label(title);
            t.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            t.setStyle("-fx-text-fill: #facc15;");
            c.getChildren().add(t);
        }

        return c;
    }

    protected Label statusLabel() {
        Label l = new Label("");
        l.setStyle("-fx-font-size: 12; -fx-text-fill: #22c55e;");
        return l;
    }

    protected void showSuccess(Label lbl, String msg) {
        lbl.setStyle("-fx-text-fill: #22c55e;");
        lbl.setText("✔ " + msg);
    }

    protected void showError(Label lbl, String msg) {
        lbl.setStyle("-fx-text-fill: #ef4444;");
        lbl.setText("✖ " + msg);
    }

    public abstract javafx.scene.Parent getView();
}