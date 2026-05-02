package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.CaseDAO;
import com.gcpd.db.EvidenceDAO;
import com.gcpd.db.ForensicReportDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ForensicAnalystDashboard extends BaseScreen {

    private final ForensicReportDAO reportDAO = new ForensicReportDAO();
    private final EvidenceDAO evidenceDAO     = new EvidenceDAO();
    private final CaseDAO caseDAO             = new CaseDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #0b0f1a;");
        tabs.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tabs.getTabs().addAll(buildLogEvidenceTab(), buildViewReportsTab());
        tabs.getTabs().forEach(t -> t.setClosable(false));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        contentArea.setFillWidth(true);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(tabs);
        return root;
    }

    // ── UC-03 ────────────────────────────────────────────────────────────────
    private Tab buildLogEvidenceTab() {
        Tab tab = new Tab("🔬  Log Forensic Evidence");

        VBox outer = new VBox(18);
        outer.setPadding(new Insets(20));
        outer.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("UC-03 — Log Forensic Evidence & Generate Report");

        // Step 1
        VBox step1 = card("Step 1 — Select Active Case");
        TableView<String[]> caseTable = buildStyledTable(
                new String[]{"Case ID", "Status", "Priority", "Start Date", "Detective", "Location", "Crime Type"});
        caseTable.setPrefHeight(200);
        Button loadCasesBtn = secondaryButton("⟳  Reload Cases");
        loadCasesBtn.setOnAction(e -> loadTable(caseTable, caseDAO.getAllCases()));
        loadTable(caseTable, caseDAO.getAllCases());
        step1.getChildren().addAll(caseTable, loadCasesBtn);

        // Step 2
        VBox step2 = card("Step 2 — Log Evidence Item");

        Label evTypeLabel = fieldLabel("Evidence Type *");
        TextField evTypeField = new TextField();
        evTypeField.setPromptText("e.g. Fingerprints, Weapon, DNA Sample");
        styleTextField(evTypeField);

        Label evLocLabel = fieldLabel("Storage Location");
        TextField evLocField = new TextField();
        evLocField.setPromptText("e.g. Vault B, Shelf 3");
        styleTextField(evLocField);

        step2.getChildren().addAll(evTypeLabel, evTypeField, evLocLabel, evLocField);

        // Step 3
        VBox step3 = card("Step 3 — Generate Forensic Report");

        Label findingsLabel = fieldLabel("Findings *");
        TextArea findingsArea = new TextArea();
        findingsArea.setPromptText("Describe forensic findings in detail...");
        findingsArea.setPrefRowCount(4);
        styleTextArea(findingsArea);

        Label verdictLabel = fieldLabel("Verdict / Conclusion");
        TextField verdictField = new TextField();
        verdictField.setPromptText("e.g. DNA matches suspect USR004");
        styleTextField(verdictField);

        step3.getChildren().addAll(findingsLabel, findingsArea, verdictLabel, verdictField);

        Label status = statusLabel();
        Button submitBtn = primaryButton("Submit Evidence + Generate Report");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            String[] sel = caseTable.getSelectionModel().getSelectedItem();
            if (sel == null)                    { showError(status, "Select a case first."); return; }
            if (evTypeField.getText().isBlank()) { showError(status, "Evidence type is required."); return; }
            if (findingsArea.getText().isBlank()){ showError(status, "Findings are required."); return; }

            String caseID    = sel[0];
            String analystID = SessionManager.getInstance().getCurrentUserID();
            long   ts        = System.currentTimeMillis();
            String evidenceID = "EV-" + ts;
            String reportID   = "RPT-" + ts;

            boolean evOk = evidenceDAO.insertEvidence(
                    evidenceID, evTypeField.getText().trim(), evLocField.getText().trim(), caseID);

            if (evOk) evidenceDAO.insertCustodyLog(
                    "LOG-" + ts, evidenceID, null, "Collected",
                    analystID, "Initial evidence log by ForensicAnalyst");

            boolean rpOk = reportDAO.insertReport(
                    reportID, findingsArea.getText().trim(), verdictField.getText().trim(),
                    analystID, evidenceID, caseID);

            if (evOk && rpOk) {
                showSuccess(status, "Evidence " + evidenceID + " logged. Report " + reportID + " generated.");
                evTypeField.clear(); evLocField.clear(); findingsArea.clear(); verdictField.clear();
            } else showError(status, "Partial failure — check DB.");
        });

        outer.getChildren().addAll(title, step1, step2, step3, submitBtn, status);
        ScrollPane scroll = new ScrollPane(outer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#0b0f1a;-fx-background-color:#0b0f1a;");
        tab.setContent(scroll);
        return tab;
    }

    // ── View Reports ─────────────────────────────────────────────────────────
    private Tab buildViewReportsTab() {
        Tab tab = new Tab("📄  My Reports");

        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("All Forensic Reports");
        TableView<String[]> table = buildStyledTable(
                new String[]{"Report ID", "Findings", "Verdict", "Analyst ID",
                             "Evidence ID", "Case ID", "Date"});
        VBox.setVgrow(table, Priority.ALWAYS);

        Button refreshBtn = secondaryButton("⟳  Refresh");
        refreshBtn.setOnAction(e -> loadTable(table, reportDAO.getAllReports()));
        loadTable(table, reportDAO.getAllReports());

        box.getChildren().addAll(title, table, refreshBtn);
        tab.setContent(box);
        return tab;
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
}
