package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.CaseDAO;
import com.gcpd.db.OperationDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * CommissionerDashboard — handles:
 * UC-04: Approve / Reject High-Risk Operations
 * UC-12: View Crime Analytics Dashboard
 */
public class CommissionerDashboard extends BaseScreen {

    private final OperationDAO opDAO = new OperationDAO();
    private final CaseDAO caseDAO    = new CaseDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #1a1a2e;");
        tabs.getTabs().addAll(
            buildApprovalTab(),    // UC-04
            buildAnalyticsTab()    // UC-12
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        contentArea.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return root;
    }

    // ── UC-04: Approve High-Risk Operation ──────────────────────────────────
    private Tab buildApprovalTab() {
        Tab tab = new Tab("⚠  Approve Operations");

        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("UC-04 — Approve / Reject High-Risk Operations");

        // Pending operations table
        TableView<String[]> table = buildTable(
            new String[]{"Operation ID", "Requested By", "Status", "Risk Level",
                         "Description", "Approved By", "Reject Reason", "Timestamp"}
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        // Reject reason field
        Label reasonLabel = fieldLabel("Rejection Reason (if rejecting):");
        TextField reasonField = new TextField();
        reasonField.setPromptText("Enter reason for rejection...");
        styleTextField(reasonField);

        Label status = statusLabel();

        HBox btnRow = new HBox(10);
        Button refreshBtn = secondaryButton("Refresh");
        Button approveBtn = primaryButton("✔ Approve");
        Button rejectBtn  = new Button("✖ Reject");
        rejectBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18;");
        rejectBtn.setCursor(javafx.scene.Cursor.HAND);

        refreshBtn.setOnAction(e -> loadAllOps(table));

        approveBtn.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            if (selected == null)             { showError(status, "Select an operation."); return; }
            if (!"Pending".equals(selected[2])) { showError(status, "Only pending ops can be approved."); return; }

            String commID = SessionManager.getInstance().getCurrentUserID();
            boolean ok = opDAO.approveOperation(selected[0], commID);
            if (ok) {
                showSuccess(status, "Operation " + selected[0] + " APPROVED.");
                loadAllOps(table);
            } else {
                showError(status, "Approval failed.");
            }
        });

        rejectBtn.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            if (selected == null)               { showError(status, "Select an operation."); return; }
            if (!"Pending".equals(selected[2])) { showError(status, "Only pending ops can be rejected."); return; }
            String reason = reasonField.getText().trim();
            if (reason.isEmpty())               { showError(status, "Enter a rejection reason."); return; }

            boolean ok = opDAO.rejectOperation(selected[0], reason);
            if (ok) {
                showSuccess(status, "Operation " + selected[0] + " REJECTED.");
                reasonField.clear();
                loadAllOps(table);
            } else {
                showError(status, "Rejection failed.");
            }
        });

        btnRow.getChildren().addAll(refreshBtn, approveBtn, rejectBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        loadAllOps(table);
        box.getChildren().addAll(title, table, reasonLabel, reasonField, btnRow, status);
        tab.setContent(box);
        return tab;
    }

    private void loadAllOps(TableView<String[]> table) {
        table.setItems(FXCollections.observableArrayList(opDAO.getAllOperations()));
    }

    // ── UC-12: Crime Analytics Dashboard ────────────────────────────────────
    private Tab buildAnalyticsTab() {
        Tab tab = new Tab("📊  Crime Analytics");

        VBox box = new VBox(20);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("UC-12 — Crime Analytics Dashboard");

        // Summary cards row
        HBox summaryRow = new HBox(15);
        List<String[]> allCases = caseDAO.getAllCases();
        long total    = allCases.size();
        long open     = allCases.stream().filter(c -> !"Closed".equals(c[1])).count();
        long closed   = allCases.stream().filter(c -> "Closed".equals(c[1])).count();
        long escalated= allCases.stream().filter(c -> "Escalated".equals(c[1])).count();

        summaryRow.getChildren().addAll(
            statCard("Total Cases",    String.valueOf(total),    "#e94560"),
            statCard("Open Cases",     String.valueOf(open),     "#f5a623"),
            statCard("Closed Cases",   String.valueOf(closed),   "#44cc88"),
            statCard("Escalated",      String.valueOf(escalated),"#9b59b6")
        );

        // Crime type breakdown table
        Label breakdownLabel = sectionTitle("Crime Type Breakdown");
        TableView<String[]> breakdownTable = buildTable(new String[]{"Crime Type", "Case Count"});
        breakdownTable.setMaxHeight(200);

        List<String[]> counts = caseDAO.getCrimeTypeCounts();
        breakdownTable.setItems(FXCollections.observableArrayList(counts));

        // All cases table
        Label allLabel = sectionTitle("All Cases");
        TableView<String[]> allTable = buildTable(
            new String[]{"Case ID", "Status", "Priority", "Start Date", "Detective", "Location", "Crime Type"}
        );
        VBox.setVgrow(allTable, Priority.ALWAYS);
        allTable.setItems(FXCollections.observableArrayList(allCases));

        Button refreshBtn = secondaryButton("Refresh Dashboard");
        refreshBtn.setOnAction(e -> {
            List<String[]> fresh = caseDAO.getAllCases();
            allTable.setItems(FXCollections.observableArrayList(fresh));
            breakdownTable.setItems(FXCollections.observableArrayList(caseDAO.getCrimeTypeCounts()));
        });

        box.getChildren().addAll(title, summaryRow, breakdownLabel, breakdownTable,
                                  allLabel, allTable, refreshBtn);
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e;");
        tab.setContent(scroll);
        return tab;
    }

    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18));
        card.setMinWidth(150);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label val = new Label(value);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        val.setStyle("-fx-text-fill: " + color + ";");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12;");

        card.getChildren().addAll(val, lbl);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
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
