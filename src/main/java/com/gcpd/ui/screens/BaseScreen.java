package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.ui.MainApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * BaseScreen — shared UI structure: top navbar + content area.
 * All dashboards use this for consistency.
 */
public abstract class BaseScreen {

    protected BorderPane root;
    protected VBox contentArea;

    public BaseScreen() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        root.setTop(buildNavbar());

        contentArea = new VBox(15);
        contentArea.setPadding(new Insets(25));
        root.setCenter(contentArea);
    }

    private HBox buildNavbar() {
        HBox nav = new HBox(20);
        nav.setPadding(new Insets(12, 20, 12, 20));
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setStyle("-fx-background-color: #16213e; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");

        Label brand = new Label("GCPD");
        brand.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        brand.setStyle("-fx-text-fill: #e94560;");

        Label role = new Label("| " + SessionManager.getInstance().getCurrentRole()
                + "  —  " + SessionManager.getInstance().getCurrentUser().getName());
        role.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 6 14;");
        logoutBtn.setCursor(javafx.scene.Cursor.HAND);
        logoutBtn.setOnAction(e -> {
            SessionManager.getInstance().endSession();
            MainApp.showLogin();
        });

        nav.getChildren().addAll(brand, role, spacer, logoutBtn);
        return nav;
    }

    // ---- Shared styling helpers ----

    protected Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        l.setStyle("-fx-text-fill: white;");
        return l;
    }

    protected Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12;");
        return l;
    }

    protected void styleTextField(javafx.scene.control.TextField f) {
        f.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                   "-fx-prompt-text-fill: #555577; -fx-background-radius: 6; -fx-padding: 7;");
    }

    protected void styleTextArea(javafx.scene.control.TextArea f) {
        f.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                   "-fx-prompt-text-fill: #555577; -fx-background-radius: 6; -fx-control-inner-background: #0f3460;");
    }

    protected void styleComboBox(javafx.scene.control.ComboBox<?> c) {
        c.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-background-radius: 6;");
    }

    protected Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; " +
                   "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18;");
        b.setCursor(javafx.scene.Cursor.HAND);
        return b;
    }

    protected Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                   "-fx-background-radius: 6; -fx-padding: 8 18;");
        b.setCursor(javafx.scene.Cursor.HAND);
        return b;
    }

    protected VBox card(String title) {
        VBox c = new VBox(12);
        c.setPadding(new Insets(20));
        c.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        if (title != null && !title.isBlank()) {
            Label t = new Label(title);
            t.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            t.setStyle("-fx-text-fill: #e94560;");
            c.getChildren().add(t);
        }
        return c;
    }

    protected Label statusLabel() {
        Label l = new Label("");
        l.setStyle("-fx-font-size: 12; -fx-text-fill: #44cc88;");
        return l;
    }

    protected void showSuccess(Label lbl, String msg) {
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #44cc88;");
        lbl.setText("✔ " + msg);
    }

    protected void showError(Label lbl, String msg) {
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #e94560;");
        lbl.setText("✖ " + msg);
    }

    public abstract javafx.scene.Parent getView();
}
