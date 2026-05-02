package com.gcpd.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.gcpd.ui.screens.LoginScreen;

/**
 * MainApp — JavaFX Application entry point.
 * Launches the Login screen first.
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("GCPD Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        showLogin();
        stage.show();
    }

    public static void showLogin() {
        Scene scene = new Scene(new LoginScreen().getView(), 900, 650);
        scene.getStylesheets().add(MainApp.class.getResource("/style.css") != null
                ? MainApp.class.getResource("/style.css").toExternalForm() : "");
        primaryStage.setScene(scene);
    }

    public static void setScene(javafx.scene.Parent root) {
        Scene scene = new Scene(root, 900, 650);
        primaryStage.setScene(scene);
    }

    public static Stage getStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
