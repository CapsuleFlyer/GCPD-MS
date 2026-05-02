package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.CaseDAO;
import com.gcpd.db.OperationDAO;
import com.gcpd.db.UserDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class SergeantDashboard extends BaseScreen {

    private final CaseDAO caseDAO    = new CaseDAO();
    private final UserDAO userDAO    = new UserDAO();
    private final OperationDAO opDAO = new OperationDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #0b0f1a;");
        tabs.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tabs.getTabs().addAll(buildAssignTab(), buildAllCasesTab(), buildSubmitOpTab());
        tabs.getTabs().forEach(t -> t.setClosable(false));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        contentArea.setFillWidth(true);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(tabs);
        return root;
    }

    // ── UC-02 ────────────────────────────────────────────────────────────────
    private Tab buildAssignTab() {
        Tab tab = new Tab("👮  Assign Detective");

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("UC-02 — Assign Detective to Case");

        TableView<String[]> caseTable = buildStyledTable(
                new String[]{"Case ID", "Status", "Priority", "Location", "Crime Type"});
        VBox.setVgrow(caseTable, Priority.ALWAYS);
        loadTable(caseTable, caseDAO.getUnassignedCases());

        Label detLabel = fieldLabel("Select Detective:");
        ComboBox<String> detectiveBox = new ComboBox<>();
        detectiveBox.setPromptText("Choose detective...");
        detectiveBox.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(detectiveBox);
        loadDetectives(detectiveBox);

        Label info = new Label("ℹ  Detectives with 5+ active cases are at capacity.");
        info.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");

        Label status  = statusLabel();
        Button refreshBtn = secondaryButton("⟳  Refresh");
        Button assignBtn  = primaryButton("Assign Detective");

        refreshBtn.setOnAction(e -> { loadTable(caseTable, caseDAO.getUnassignedCases()); loadDetectives(detectiveBox); });

        assignBtn.setOnAction(e -> {
            String[] sel = caseTable.getSelectionModel().getSelectedItem();
            String   det = detectiveBox.getValue();
            if (sel == null) { showError(status, "Select a case."); return; }
            if (det == null) { showError(status, "Select a detective."); return; }
            String detID = det.substring(det.lastIndexOf("(") + 1, det.lastIndexOf(")"));
            boolean ok = caseDAO.assignDetective(sel[0], detID);
            if (ok) {
                userDAO.updateWorkload(detID, getWorkloadScore(detID) + 1);
                showSuccess(status, det + " assigned to case " + sel[0]);
                loadTable(caseTable, caseDAO.getUnassignedCases());
            } else showError(status, "Assignment failed.");
        });

        HBox btnRow = new HBox(10, refreshBtn, assignBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(title, caseTable, detLabel, detectiveBox, info, btnRow, status);
        tab.setContent(box);
        return tab;
    }

    // ── All Cases ────────────────────────────────────────────────────────────
    private Tab buildAllCasesTab() {
        Tab tab = new Tab("📁  All Cases");

        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("All Active Cases");
        TableView<String[]> table = buildStyledTable(
                new String[]{"Case ID", "Status", "Priority", "Start Date", "Detective", "Location", "Crime Type"});
        VBox.setVgrow(table, Priority.ALWAYS);
        loadTable(table, caseDAO.getAllCases());

        Button refreshBtn = secondaryButton("⟳  Refresh");
        refreshBtn.setOnAction(e -> loadTable(table, caseDAO.getAllCases()));

        box.getChildren().addAll(title, table, refreshBtn);
        tab.setContent(box);
        return tab;
    }

    // ── UC-04 Sergeant side ──────────────────────────────────────────────────
    private Tab buildSubmitOpTab() {
        Tab tab = new Tab("⚠  Request Operation");

        VBox box = card("UC-04 — Submit High-Risk Operation Request");

        Label descLabel = fieldLabel("Operation Description *");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the high-risk operation...");
        descArea.setPrefRowCount(4);
        styleTextArea(descArea);

        Label riskLabel = fieldLabel("Risk Level");
        ComboBox<String> riskBox = new ComboBox<>(FXCollections.observableArrayList("High", "Critical"));
        riskBox.setValue("High");
        riskBox.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(riskBox);

        Label caseLabel = fieldLabel("Linked Case ID (optional)");
        TextField caseField = new TextField();
        caseField.setPromptText("e.g. CASE-123456");
        styleTextField(caseField);

        Label status = statusLabel();
        Button submitBtn = primaryButton("Submit for Commissioner Approval");

        submitBtn.setOnAction(e -> {
            if (descArea.getText().isBlank()) { showError(status, "Description is required."); return; }
            String opID  = "OP-" + System.currentTimeMillis();
            String reqBy = SessionManager.getInstance().getCurrentUserID();
            String caseID = caseField.getText().isBlank() ? null : caseField.getText().trim();
            boolean ok = opDAO.insertOperation(opID, reqBy, riskBox.getValue(), caseID, descArea.getText().trim());
            if (ok) { showSuccess(status, "Operation " + opID + " submitted."); descArea.clear(); caseField.clear(); }
            else      showError(status, "Submission failed.");
        });

        box.getChildren().addAll(descLabel, descArea, riskLabel, riskBox, caseLabel, caseField, submitBtn, status);
        tab.setContent(styledScroll(box));
        return tab;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void loadDetectives(ComboBox<String> box) {
        box.getItems().clear();
        userDAO.getAllDetectives().forEach(u -> box.getItems().add(u.getName() + " (" + u.getUserID() + ")"));
    }

    private int getWorkloadScore(String detID) {
        return (int) caseDAO.getAllCases().stream().filter(c -> detID.equals(c[4]) && !"Closed".equals(c[1])).count();
    }

    private void loadTable(TableView<String[]> table, List<String[]> data) {
        table.setItems(FXCollections.observableArrayList(data));
        Platform.runLater(() -> applyHeaderStyles(table));
    }

    TableView<String[]> buildStyledTable(String[] columns) {
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(38);
        table.setStyle(
                "-fx-background-color:#1f2937;-fx-control-inner-background:#1f2937;" +
                "-fx-control-inner-background-alt:#253347;-fx-table-cell-border-color:#2d3748;" +
                "-fx-border-color:#334155;-fx-border-radius:8;-fx-background-radius:8;"
        );
        for (int i = 0; i < columns.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(columns[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    d.getValue().length > idx ? d.getValue()[idx] : ""));
            col.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    String bg = getIndex() % 2 == 0 ? "#1f2937" : "#253347";
                    if (empty || item == null) { setText(null); setStyle("-fx-background-color:" + bg + ";"); }
                    else { setText(item); setStyle("-fx-background-color:" + bg + ";-fx-text-fill:#f1f5f9;-fx-font-size:13px;-fx-padding:0 12px;"); }
                }
            });
            table.getColumns().add(col);
        }
        Platform.runLater(() -> applyHeaderStyles(table));
        return table;
    }

    private void applyHeaderStyles(TableView<?> table) {
        table.lookupAll(".column-header").forEach(n -> n.setStyle("-fx-background-color:#1a2236;-fx-border-color:#334155;-fx-border-width:0 1px 2px 0;"));
        table.lookupAll(".column-header .label").forEach(n -> n.setStyle("-fx-text-fill:#facc15;-fx-font-weight:bold;-fx-font-size:12px;-fx-padding:0 12px;"));
        table.lookupAll(".column-header-background").forEach(n -> n.setStyle("-fx-background-color:#1a2236;-fx-border-color:#334155;-fx-border-width:0 0 2px 0;"));
        table.lookupAll(".corner").forEach(n -> n.setStyle("-fx-background-color:#1a2236;"));
    }

    private ScrollPane styledScroll(javafx.scene.Node content) {
        ScrollPane s = new ScrollPane(content);
        s.setFitToWidth(true);
        s.setStyle("-fx-background:#0b0f1a;-fx-background-color:#0b0f1a;");
        return s;
    }
}
