package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class DetectiveDashboard extends BaseScreen {

    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final CaseDAO caseDAO         = new CaseDAO();
    private final SuspectDAO suspectDAO   = new SuspectDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #0b0f1a;");
        tabs.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tabs.getTabs().addAll(
                buildReportIncidentTab(),
                buildEscalateTab(),
                buildMyCasesTab(),
                buildRepeatOffenderTab()
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        contentArea.setFillWidth(true);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(tabs);
        return root;
    }

    // ── UC-01 ────────────────────────────────────────────────────────────────
    private Tab buildReportIncidentTab() {
        Tab tab = new Tab("📋  Report Incident");

        VBox box = card("UC-01 — Report New Incident");

        Label locLabel = fieldLabel("Crime Location *");
        TextField locField = new TextField();
        locField.setPromptText("e.g. Gotham Docks, Warehouse 7");
        styleTextField(locField);

        Label typeLabel = fieldLabel("Crime Type *");
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                "Robbery", "Assault", "Homicide", "Arson", "Terrorism",
                "Kidnapping", "Fraud", "Drug Trafficking", "Other"));
        typeBox.setPromptText("Select crime type");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(typeBox);

        Label descLabel = fieldLabel("Description");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the incident...");
        descArea.setPrefRowCount(4);
        styleTextArea(descArea);

        Label status = statusLabel();
        Button submitBtn = primaryButton("Submit Incident Report");

        submitBtn.setOnAction(e -> {
            String loc  = locField.getText().trim();
            String type = typeBox.getValue();
            if (loc.isEmpty() || type == null) { showError(status, "Location and Crime Type are required."); return; }
            String id = "INC-" + System.currentTimeMillis();
            boolean ok = incidentDAO.insertIncident(id, loc, type, descArea.getText().trim(),
                    SessionManager.getInstance().getCurrentUserID());
            if (ok) { showSuccess(status, "Incident " + id + " reported."); locField.clear(); typeBox.setValue(null); descArea.clear(); }
            else      showError(status, "Failed to save. Check DB.");
        });

        box.getChildren().addAll(locLabel, locField, typeLabel, typeBox, descLabel, descArea, submitBtn, status);
        ScrollPane scroll = styledScroll(box);
        tab.setContent(scroll);
        return tab;
    }

    // ── UC-09 ────────────────────────────────────────────────────────────────
    private Tab buildEscalateTab() {
        Tab tab = new Tab("⬆  Escalate to Case");

        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("UC-09 — Escalate Incident to Case");

        TableView<String[]> table = buildStyledTable(
                new String[]{"Incident ID", "Location", "Crime Type", "Description", "Status", "Reported By", "Date"});
        VBox.setVgrow(table, Priority.ALWAYS);
        loadTable(table, incidentDAO.getAllIncidents());

        Label prioLabel = fieldLabel("Case Priority:");
        ComboBox<String> priorityBox = new ComboBox<>(FXCollections.observableArrayList("Low", "Medium", "High"));
        priorityBox.setValue("Medium");
        styleComboBox(priorityBox);

        Label status = statusLabel();
        Button refreshBtn = secondaryButton("⟳  Refresh");
        Button escalateBtn = primaryButton("Escalate → Create Case");

        refreshBtn.setOnAction(e -> loadTable(table, incidentDAO.getAllIncidents()));
        escalateBtn.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel == null)               { showError(status, "Select an incident."); return; }
            if (!"Reported".equals(sel[4])){ showError(status, "Only 'Reported' incidents can be escalated."); return; }
            String caseID = "CASE-" + System.currentTimeMillis();
            boolean ok = caseDAO.insertCase(caseID, sel[0], priorityBox.getValue())
                      && incidentDAO.updateIncidentStatus(sel[0], "Escalated");
            if (ok) { showSuccess(status, "Case " + caseID + " created."); loadTable(table, incidentDAO.getAllIncidents()); }
            else      showError(status, "Escalation failed.");
        });

        HBox controls = new HBox(10, prioLabel, priorityBox, refreshBtn, escalateBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(title, table, controls, status);
        tab.setContent(box);
        return tab;
    }

    // ── UC-10 ────────────────────────────────────────────────────────────────
    private Tab buildMyCasesTab() {
        Tab tab = new Tab("📁  My Cases");

        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("UC-10 — Update Case Status");

        TableView<String[]> table = buildStyledTable(
                new String[]{"Case ID", "Status", "Priority", "Start Date", "Detective", "Location", "Crime Type"});
        VBox.setVgrow(table, Priority.ALWAYS);

        String myID = SessionManager.getInstance().getCurrentUserID();
        List<String[]> mine = caseDAO.getAllCases().stream().filter(c -> myID.equals(c[4])).toList();
        loadTable(table, mine);

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
                "Reported", "UnderInvestigation", "Escalated", "Closed", "ColdCase"));
        statusBox.setPromptText("New status...");
        styleComboBox(statusBox);

        Label status = statusLabel();
        Button refreshBtn = secondaryButton("⟳  Refresh");
        Button updateBtn  = primaryButton("Update Status");

        refreshBtn.setOnAction(e -> {
            List<String[]> fresh = caseDAO.getAllCases().stream().filter(c -> myID.equals(c[4])).toList();
            loadTable(table, fresh);
        });
        updateBtn.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            String newSt = statusBox.getValue();
            if (sel == null)   { showError(status, "Select a case."); return; }
            if (newSt == null) { showError(status, "Select a status."); return; }
            boolean ok = caseDAO.updateCaseStatus(sel[0], newSt);
            if (ok) { showSuccess(status, "Case " + sel[0] + " → " + newSt);
                      List<String[]> fresh = caseDAO.getAllCases().stream().filter(c -> myID.equals(c[4])).toList();
                      loadTable(table, fresh); }
            else      showError(status, "Update failed.");
        });

        HBox controls = new HBox(10, fieldLabel("New Status:"), statusBox, refreshBtn, updateBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(title, table, controls, status);
        tab.setContent(box);
        return tab;
    }

    // ── UC-05 ────────────────────────────────────────────────────────────────
    private Tab buildRepeatOffenderTab() {
        Tab tab = new Tab("🔍  Repeat Offender");

        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("UC-05 — Identify Repeat Offender");

        TextField nameField  = new TextField(); nameField.setPromptText("Suspect name...");  styleTextField(nameField);
        TextField crimeField = new TextField(); crimeField.setPromptText("Crime keyword..."); styleTextField(crimeField);
        Spinner<Integer> riskSpinner = new Spinner<>(1, 5, 1);
        riskSpinner.setStyle("-fx-background-color: #1f2937;"); riskSpinner.setPrefWidth(70);
        Button searchBtn = primaryButton("Search");

        HBox searchRow = new HBox(10, nameField, crimeField, fieldLabel("Min Risk:"), riskSpinner, searchBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox.setHgrow(crimeField, Priority.ALWAYS);

        TableView<String[]> table = buildStyledTable(
                new String[]{"Suspect ID", "Name", "Criminal History", "Risk Level", "Repeat Offender?", "Cases"});
        VBox.setVgrow(table, Priority.ALWAYS);
        loadTable(table, suspectDAO.searchSuspects("", "", 1));

        Label status   = statusLabel();
        Button flagBtn = primaryButton("🚩 Flag as Repeat Offender");

        searchBtn.setOnAction(e -> {
            List<String[]> results = suspectDAO.searchSuspects(
                    nameField.getText().trim(), crimeField.getText().trim(), riskSpinner.getValue());
            loadTable(table, results);
            if (results.isEmpty()) showError(status, "No matches found.");
            else showSuccess(status, results.size() + " suspect(s) found.");
        });

        flagBtn.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showError(status, "Select a suspect."); return; }
            boolean ok = suspectDAO.flagRepeatOffender(sel[0]);
            if (ok) { showSuccess(status, sel[1] + " flagged."); searchBtn.fire(); }
            else      showError(status, "Failed to flag.");
        });

        box.getChildren().addAll(title, searchRow, table, flagBtn, status);
        tab.setContent(box);
        return tab;
    }

    // ── Shared helpers ───────────────────────────────────────────────────────

    private void loadTable(TableView<String[]> table, List<String[]> data) {
        table.setItems(FXCollections.observableArrayList(data));
        Platform.runLater(() -> applyHeaderStyles(table));
    }

    TableView<String[]> buildStyledTable(String[] columns) {
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(38);
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
