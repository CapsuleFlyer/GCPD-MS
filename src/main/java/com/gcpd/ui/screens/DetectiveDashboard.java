package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * DetectiveDashboard: handles:
 * UC-01: Report New Incident
 * UC-09: Escalate Incident to Case
 * UC-10: Update Case Status
 * UC-05: Identify Repeat Offender
 */
public class DetectiveDashboard extends BaseScreen {

    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final CaseDAO caseDAO = new CaseDAO();
    private final SuspectDAO suspectDAO = new SuspectDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #1a1a2e; -fx-tab-min-width: 160;");
        tabs.getTabs().addAll(
            buildReportIncidentTab(),   // UC-01
            buildMyIncidentsTab(),      // UC-09
            buildMyCasesTab(),          // UC-10
            buildRepeatOffenderTab()    // UC-05
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        contentArea.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return root;
    }

    // ── UC-01: Report New Incident ──────────────────────────────────────────
    private Tab buildReportIncidentTab() {
        Tab tab = new Tab("📋  Report Incident");

        VBox box = card("UC-01: Report New Incident");
        box.setMaxWidth(600);

        Label locLabel  = fieldLabel("Crime Location *");
        TextField locField = new TextField();
        locField.setPromptText("e.g. Gotham Docks, Warehouse 7");
        styleTextField(locField);

        Label typeLabel = fieldLabel("Crime Type *");
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
            "Robbery", "Assault", "Homicide", "Arson", "Terrorism",
            "Kidnapping", "Fraud", "Drug Trafficking", "Other"
        ));
        typeBox.setPromptText("Select crime type");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(typeBox);

        Label descLabel = fieldLabel("Description");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the incident in detail...");
        descArea.setPrefRowCount(4);
        styleTextArea(descArea);

        Label status = statusLabel();
        Button submitBtn = primaryButton("Submit Incident Report");

        submitBtn.setOnAction(e -> {
            String loc  = locField.getText().trim();
            String type = typeBox.getValue();
            String desc = descArea.getText().trim();

            if (loc.isEmpty() || type == null) {
                showError(status, "Location and Crime Type are required.");
                return;
            }

            String incidentID = "INC-" + System.currentTimeMillis();
            String reportedBy = SessionManager.getInstance().getCurrentUserID();

            boolean ok = incidentDAO.insertIncident(incidentID, loc, type, desc, reportedBy);
            if (ok) {
                showSuccess(status, "Incident " + incidentID + " reported successfully.");
                locField.clear(); typeBox.setValue(null); descArea.clear();
            } else {
                showError(status, "Failed to save incident. Check DB connection.");
            }
        });

        box.getChildren().addAll(locLabel, locField, typeLabel, typeBox,
                                  descLabel, descArea, submitBtn, status);

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e;");
        tab.setContent(scroll);
        return tab;
    }

    // ── UC-09: Escalate Incident to Case ────────────────────────────────────
    private Tab buildMyIncidentsTab() {
        Tab tab = new Tab("⬆  Escalate to Case");

        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("UC-09: Escalate Incident to Case");

        TableView<String[]> table = buildIncidentTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        ComboBox<String> priorityBox = new ComboBox<>(FXCollections.observableArrayList(
            "Low", "Medium", "High"
        ));
        priorityBox.setValue("Medium");
        styleComboBox(priorityBox);

        Label status = statusLabel();
        Button refreshBtn = secondaryButton("Refresh");
        Button escalateBtn = primaryButton("Escalate Selected → Case");

        refreshBtn.setOnAction(e -> loadIncidents(table));

        escalateBtn.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { showError(status, "Select an incident first."); return; }
            if (!selected[4].equals("Reported")) {
                showError(status, "Only 'Reported' incidents can be escalated."); return;
            }
            String caseID = "CASE-" + System.currentTimeMillis();
            boolean ok1 = caseDAO.insertCase(caseID, selected[0], priorityBox.getValue());
            boolean ok2 = incidentDAO.updateIncidentStatus(selected[0], "Escalated");
            if (ok1 && ok2) {
                showSuccess(status, "Case " + caseID + " created from incident " + selected[0]);
                loadIncidents(table);
            } else {
                showError(status, "Escalation failed.");
            }
        });

        HBox controls = new HBox(10, new Label("Priority:"), priorityBox, refreshBtn, escalateBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setStyle("-fx-padding: 5 0 0 0;");
        controls.getChildren().get(0).setStyle("-fx-text-fill: #aaaaaa;");

        loadIncidents(table);
        box.getChildren().addAll(title, table, controls, status);
        tab.setContent(box);
        return tab;
    }

    private TableView<String[]> buildIncidentTable() {
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: #16213e; -fx-text-fill: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String[] cols = {"Incident ID", "Location", "Crime Type", "Description", "Status", "Reported By"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().length > idx ? d.getValue()[idx] : ""));
            table.getColumns().add(col);
        }
        return table;
    }

    private void loadIncidents(TableView<String[]> table) {
        String myID = SessionManager.getInstance().getCurrentUserID();
        List<String[]> all = incidentDAO.getAllIncidents();
        // Detectives see all reported incidents
        table.setItems(FXCollections.observableArrayList(all));
    }

    // ── UC-10: Update Case Status ────────────────────────────────────────────
    private Tab buildMyCasesTab() {
        Tab tab = new Tab("📁  My Cases");

        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("UC-10: Update Case Status");

        TableView<String[]> table = buildCaseTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
            "Reported", "UnderInvestigation", "Escalated", "Closed", "ColdCase"
        ));
        statusBox.setPromptText("New status");
        styleComboBox(statusBox);

        Label status = statusLabel();
        Button refreshBtn = secondaryButton("Refresh");
        Button updateBtn  = primaryButton("Update Status");

        refreshBtn.setOnAction(e -> loadCases(table));

        updateBtn.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            String newStatus  = statusBox.getValue();
            if (selected == null) { showError(status, "Select a case first."); return; }
            if (newStatus == null) { showError(status, "Select a new status."); return; }

            boolean ok = caseDAO.updateCaseStatus(selected[0], newStatus);
            if (ok) {
                showSuccess(status, "Case " + selected[0] + " updated to " + newStatus);
                loadCases(table);
            } else {
                showError(status, "Update failed.");
            }
        });

        HBox controls = new HBox(10, new Label("New Status:"), statusBox, refreshBtn, updateBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getChildren().get(0).setStyle("-fx-text-fill: #aaaaaa;");

        loadCases(table);
        box.getChildren().addAll(title, table, controls, status);
        tab.setContent(box);
        return tab;
    }

    private TableView<String[]> buildCaseTable() {
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: #16213e;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String[] cols = {"Case ID", "Status", "Priority", "Start Date", "Detective", "Location", "Crime Type"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().length > idx ? d.getValue()[idx] : ""));
            table.getColumns().add(col);
        }
        return table;
    }

    private void loadCases(TableView<String[]> table) {
        String myID = SessionManager.getInstance().getCurrentUserID();
        List<String[]> all = caseDAO.getAllCases();
        // Filter to cases assigned to this detective
        List<String[]> mine = all.stream()
            .filter(c -> myID.equals(c[4]))
            .toList();
        table.setItems(FXCollections.observableArrayList(mine));
    }

    // ── UC-05: Identify Repeat Offender ─────────────────────────────────────
    private Tab buildRepeatOffenderTab() {
        Tab tab = new Tab("🔍  Repeat Offender");

        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("UC-05: Identify Repeat Offender");

        // Search fields
        HBox searchRow = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Suspect name...");
        styleTextField(nameField);

        TextField crimeField = new TextField();
        crimeField.setPromptText("Crime keyword...");
        styleTextField(crimeField);

        Spinner<Integer> riskSpinner = new Spinner<>(1, 5, 1);
        riskSpinner.setStyle("-fx-background-color: #0f3460;");

        Label riskLabel = new Label("Min Risk:");
        riskLabel.setStyle("-fx-text-fill: #aaaaaa;");

        Button searchBtn = primaryButton("Search");
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.getChildren().addAll(nameField, crimeField, riskLabel, riskSpinner, searchBtn);
        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox.setHgrow(crimeField, Priority.ALWAYS);

        // Results table
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: #16213e;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        String[] cols = {"Suspect ID", "Name", "Criminal History", "Risk Level", "Repeat Offender?", "Linked Cases"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().length > idx ? d.getValue()[idx] : ""));
            table.getColumns().add(col);
        }

        Label status = statusLabel();
        Button flagBtn = primaryButton("Flag as Repeat Offender");

        searchBtn.setOnAction(e -> {
            String name  = nameField.getText().trim();
            String crime = crimeField.getText().trim();
            int    risk  = riskSpinner.getValue();
            List<String[]> results = suspectDAO.searchSuspects(name, crime, risk);
            table.setItems(FXCollections.observableArrayList(results));
            if (results.isEmpty()) showError(status, "No matches found.");
            else showSuccess(status, results.size() + " suspect(s) found.");
        });

        flagBtn.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { showError(status, "Select a suspect to flag."); return; }
            boolean ok = suspectDAO.flagRepeatOffender(selected[0]);
            if (ok) {
                showSuccess(status, selected[1] + " flagged as repeat offender.");
                searchBtn.fire();
            } else {
                showError(status, "Failed to flag suspect.");
            }
        });

        // Load all on open
        table.setItems(FXCollections.observableArrayList(
            suspectDAO.searchSuspects("", "", 1)
        ));

        box.getChildren().addAll(title, searchRow, table, flagBtn, status);
        tab.setContent(box);
        return tab;
    }
}
