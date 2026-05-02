package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.CaseDAO;
import com.gcpd.db.OperationDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class CommissionerDashboard extends BaseScreen {

    private final OperationDAO opDAO = new OperationDAO();
    private final CaseDAO caseDAO    = new CaseDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #0b0f1a;");
        tabs.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tabs.getTabs().addAll(buildApprovalTab(), buildAnalyticsTab());
        tabs.getTabs().forEach(t -> t.setClosable(false));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        contentArea.setFillWidth(true);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(tabs);
        return root;
    }

    // ── UC-04 ────────────────────────────────────────────────────────────────
    private Tab buildApprovalTab() {
        Tab tab = new Tab("⚠  Approve Operations");

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("UC-04 — Approve / Reject High-Risk Operations");

        TableView<String[]> table = buildStyledTable(
                new String[]{"Operation ID", "Requested By", "Status", "Risk Level",
                             "Description", "Approved By", "Reject Reason", "Timestamp"});
        VBox.setVgrow(table, Priority.ALWAYS);
        loadTable(table, opDAO.getAllOperations());

        Label reasonLabel = fieldLabel("Rejection Reason (required when rejecting):");
        TextField reasonField = new TextField();
        reasonField.setPromptText("Enter reason...");
        styleTextField(reasonField);

        Label status = statusLabel();
        Button refreshBtn = secondaryButton("⟳  Refresh");
        Button approveBtn = primaryButton("✔  Approve");
        Button rejectBtn  = new Button("✖  Reject");
        rejectBtn.setStyle("-fx-background-color:#374151;-fx-text-fill:#f87171;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:8 18;");
        rejectBtn.setCursor(javafx.scene.Cursor.HAND);

        refreshBtn.setOnAction(e -> loadTable(table, opDAO.getAllOperations()));

        approveBtn.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel == null)                 { showError(status, "Select an operation."); return; }
            if (!"Pending".equals(sel[2]))   { showError(status, "Only pending ops can be approved."); return; }
            boolean ok = opDAO.approveOperation(sel[0], SessionManager.getInstance().getCurrentUserID());
            if (ok) { showSuccess(status, "Operation " + sel[0] + " APPROVED."); loadTable(table, opDAO.getAllOperations()); }
            else      showError(status, "Approval failed.");
        });

        rejectBtn.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel == null)               { showError(status, "Select an operation."); return; }
            if (!"Pending".equals(sel[2])) { showError(status, "Only pending ops can be rejected."); return; }
            if (reasonField.getText().isBlank()) { showError(status, "Enter a rejection reason."); return; }
            boolean ok = opDAO.rejectOperation(sel[0], reasonField.getText().trim());
            if (ok) { showSuccess(status, "Operation " + sel[0] + " REJECTED."); reasonField.clear(); loadTable(table, opDAO.getAllOperations()); }
            else      showError(status, "Rejection failed.");
        });

        HBox btnRow = new HBox(10, refreshBtn, approveBtn, rejectBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(title, table, reasonLabel, reasonField, btnRow, status);
        tab.setContent(box);
        return tab;
    }

    // ── UC-12 ────────────────────────────────────────────────────────────────
    private Tab buildAnalyticsTab() {
        Tab tab = new Tab("📊  Crime Analytics");

        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("UC-12 — Crime Analytics Dashboard");

        List<String[]> allCases = caseDAO.getAllCases();
        long total    = allCases.size();
        long open     = allCases.stream().filter(c -> !"Closed".equals(c[1])).count();
        long closed   = allCases.stream().filter(c ->  "Closed".equals(c[1])).count();
        long escalated= allCases.stream().filter(c ->  "Escalated".equals(c[1])).count();

        HBox summaryRow = new HBox(15,
                statCard("Total Cases",  String.valueOf(total),    "#facc15"),
                statCard("Open",         String.valueOf(open),     "#60a5fa"),
                statCard("Closed",       String.valueOf(closed),   "#22c55e"),
                statCard("Escalated",    String.valueOf(escalated),"#f87171")
        );

        Label breakLabel = sectionTitle("Crime Type Breakdown");
        TableView<String[]> breakTable = buildStyledTable(new String[]{"Crime Type", "Case Count"});
        breakTable.setMaxHeight(220);
        loadTable(breakTable, caseDAO.getCrimeTypeCounts());

        Label allLabel = sectionTitle("All Cases");
        TableView<String[]> allTable = buildStyledTable(
                new String[]{"Case ID", "Status", "Priority", "Start Date", "Detective", "Location", "Crime Type"});
        VBox.setVgrow(allTable, Priority.ALWAYS);
        loadTable(allTable, allCases);

        Button refreshBtn = secondaryButton("⟳  Refresh Dashboard");
        refreshBtn.setOnAction(e -> {
            loadTable(allTable, caseDAO.getAllCases());
            loadTable(breakTable, caseDAO.getCrimeTypeCounts());
        });

        box.getChildren().addAll(title, summaryRow, breakLabel, breakTable, allLabel, allTable, refreshBtn);
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#0b0f1a;-fx-background-color:#0b0f1a;");
        tab.setContent(scroll);
        return tab;
    }

    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:#111827;-fx-background-radius:12;-fx-border-color:#334155;-fx-border-radius:12;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label val = new Label(value);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        val.setStyle("-fx-text-fill:" + color + ";");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:12;");

        card.getChildren().addAll(val, lbl);
        return card;
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
