package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.CaseDAO;
import com.gcpd.db.EvidenceDAO;
import com.gcpd.db.ForensicReportDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * ForensicAnalystDashboard: handles:
 * UC-03: Log Forensic Evidence (generate report + attach to case)
 */
public class ForensicAnalystDashboard extends BaseScreen {

    private final ForensicReportDAO reportDAO = new ForensicReportDAO();
    private final EvidenceDAO evidenceDAO     = new EvidenceDAO();
    private final CaseDAO caseDAO             = new CaseDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #1a1a2e;");
        tabs.getTabs().addAll(
            buildLogEvidenceTab(),    // UC-03
            buildViewReportsTab()     // View all reports
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        contentArea.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return root;
    }

    // ── UC-03: Log Forensic Evidence ────────────────────────────────────────
    private Tab buildLogEvidenceTab() {
        Tab tab = new Tab("🔬  Log Forensic Evidence");

        VBox outer = new VBox(20);
        outer.setPadding(new Insets(15));

        Label title = sectionTitle("UC-03: Log Forensic Evidence & Generate Report");

        // Step 1: Select Case
        VBox step1 = card("Step 1: Select Active Case");
        TableView<String[]> caseTable = buildTable(
            new String[]{"Case ID", "Status", "Priority", "Start Date", "Detective", "Location", "Crime Type"}
        );
        caseTable.setPrefHeight(200);
        Button loadCasesBtn = secondaryButton("Load Cases");
        loadCasesBtn.setOnAction(e ->
            caseTable.setItems(FXCollections.observableArrayList(caseDAO.getAllCases()))
        );
        caseTable.setItems(FXCollections.observableArrayList(caseDAO.getAllCases()));
        step1.getChildren().addAll(caseTable, loadCasesBtn);

        // Step 2: Log Evidence
        VBox step2 = card("Step 2: Log Evidence Item");

        Label evTypeLabel = fieldLabel("Evidence Type *");
        TextField evTypeField = new TextField();
        evTypeField.setPromptText("e.g. Fingerprints, Weapon, DNA Sample");
        styleTextField(evTypeField);

        Label evLocLabel = fieldLabel("Storage Location");
        TextField evLocField = new TextField();
        evLocField.setPromptText("e.g. Vault B, Shelf 3");
        styleTextField(evLocField);

        step2.getChildren().addAll(evTypeLabel, evTypeField, evLocLabel, evLocField);

        // Step 3: Generate Forensic Report
        VBox step3 = card("Step 3: Generate Forensic Report");

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

        // Submit
        Label status = statusLabel();
        Button submitBtn = primaryButton("Submit Evidence + Generate Report");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            String[] selectedCase = caseTable.getSelectionModel().getSelectedItem();
            if (selectedCase == null)          { showError(status, "Select a case first."); return; }
            if (evTypeField.getText().isBlank()){ showError(status, "Evidence type is required."); return; }
            if (findingsArea.getText().isBlank()){ showError(status, "Findings are required."); return; }

            String caseID    = selectedCase[0];
            String analystID = SessionManager.getInstance().getCurrentUserID();
            long   ts        = System.currentTimeMillis();

            // Insert evidence
            String evidenceID = "EV-" + ts;
            boolean evOk = evidenceDAO.insertEvidence(
                evidenceID,
                evTypeField.getText().trim(),
                evLocField.getText().trim(),
                caseID
            );

            // Log initial chain of custody
            if (evOk) {
                evidenceDAO.insertCustodyLog(
                    "LOG-" + ts, evidenceID,
                    null, "Collected",
                    analystID, "Initial evidence log by ForensicAnalyst"
                );
            }

            // Insert forensic report
            String reportID = "RPT-" + ts;
            boolean rpOk = reportDAO.insertReport(
                reportID,
                findingsArea.getText().trim(),
                verdictField.getText().trim(),
                analystID,
                evidenceID,
                caseID
            );

            if (evOk && rpOk) {
                showSuccess(status, "Evidence " + evidenceID + " logged. Report " + reportID + " generated.");
                evTypeField.clear(); evLocField.clear();
                findingsArea.clear(); verdictField.clear();
            } else {
                showError(status, "Partial failure: check DB.");
            }
        });

        outer.getChildren().addAll(title, step1, step2, step3, submitBtn, status);
        ScrollPane scroll = new ScrollPane(outer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e;");
        tab.setContent(scroll);
        return tab;
    }

    // ── View All Reports ─────────────────────────────────────────────────────
    private Tab buildViewReportsTab() {
        Tab tab = new Tab("📄  My Reports");

        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("All Forensic Reports");
        TableView<String[]> table = buildTable(
            new String[]{"Report ID", "Findings", "Verdict", "Analyst ID",
                         "Evidence ID", "Case ID", "Date"}
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        Button refreshBtn = secondaryButton("Refresh");
        refreshBtn.setOnAction(e ->
            table.setItems(FXCollections.observableArrayList(reportDAO.getAllReports()))
        );
        table.setItems(FXCollections.observableArrayList(reportDAO.getAllReports()));

        box.getChildren().addAll(title, table, refreshBtn);
        tab.setContent(box);
        return tab;
    }

    private TableView<String[]> buildTable(String[] columns) {
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: #16213e;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (int i = 0; i < columns.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(columns[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().length > idx ? d.getValue()[idx] : ""));
            table.getColumns().add(col);
        }
        return table;
    }
}
