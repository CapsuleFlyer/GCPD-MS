package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.EvidenceDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class EvidenceCustodianDashboard extends BaseScreen {

    private final EvidenceDAO evidenceDAO = new EvidenceDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #0b0f1a;");
        tabs.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tabs.getTabs().addAll(buildEvidenceTab(), buildCustodyLogTab());
        tabs.getTabs().forEach(t -> t.setClosable(false));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        contentArea.setFillWidth(true);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(tabs);
        return root;
    }

    // ── UC-06 + UC-11 ────────────────────────────────────────────────────────
    private Tab buildEvidenceTab() {
        Tab tab = new Tab("🗂  Manage Evidence");

        VBox box = new VBox(18);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("UC-06 & UC-11 — Evidence Lifecycle & Transfer");

        TableView<String[]> table = buildStyledTable(
                new String[]{"Evidence ID", "Type", "Status", "Storage Location", "Case ID", "Collected Date"});
        VBox.setVgrow(table, Priority.ALWAYS);
        loadTable(table, evidenceDAO.getAllEvidence());

        Button refreshBtn = secondaryButton("⟳  Refresh");
        refreshBtn.setOnAction(e -> loadTable(table, evidenceDAO.getAllEvidence()));

        // Transfer card
        VBox transferCard = card("Authorize Evidence Transfer  (UC-11)");

        Label destLabel = fieldLabel("Transfer Destination *");
        TextField destField = new TextField();
        destField.setPromptText("e.g. Forensics Lab, Court Room 3");
        styleTextField(destField);

        Label notesLabel = fieldLabel("Transfer Notes");
        TextField notesField = new TextField();
        notesField.setPromptText("Optional notes...");
        styleTextField(notesField);

        Label transferStatus = statusLabel();
        Button transferBtn = primaryButton("Authorize Transfer");

        transferBtn.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel == null)                 { showError(transferStatus, "Select evidence first."); return; }
            if (destField.getText().isBlank()){ showError(transferStatus, "Destination is required."); return; }
            if ("Disposed".equals(sel[2]))   { showError(transferStatus, "Disposed evidence cannot be transferred."); return; }

            String custodianID = SessionManager.getInstance().getCurrentUserID();
            String logID = "LOG-" + System.currentTimeMillis();
            boolean ok1 = evidenceDAO.updateEvidenceStatus(sel[0], "Transferred");
            boolean ok2 = evidenceDAO.insertCustodyLog(logID, sel[0], sel[2],
                    "Transferred to " + destField.getText().trim(), custodianID, notesField.getText().trim());

            if (ok1 && ok2) {
                showSuccess(transferStatus, "Evidence " + sel[0] + " transferred.");
                destField.clear(); notesField.clear();
                loadTable(table, evidenceDAO.getAllEvidence());
            } else showError(transferStatus, "Transfer failed.");
        });

        transferCard.getChildren().addAll(destLabel, destField, notesLabel, notesField, transferBtn, transferStatus);

        // Disposal card
        VBox disposalCard = card("Dispose Evidence  (UC-06)");

        Label dispLabel = fieldLabel("Disposal Reason *");
        TextField dispField = new TextField();
        dispField.setPromptText("Reason for disposal...");
        styleTextField(dispField);

        Label disposalStatus = statusLabel();
        Button disposeBtn = new Button("Mark as Disposed");
        disposeBtn.setStyle("-fx-background-color:#374151;-fx-text-fill:#f87171;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:8 18;");
        disposeBtn.setCursor(javafx.scene.Cursor.HAND);

        disposeBtn.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel == null)                { showError(disposalStatus, "Select evidence first."); return; }
            if ("Disposed".equals(sel[2])) { showError(disposalStatus, "Already disposed."); return; }
            if (dispField.getText().isBlank()){ showError(disposalStatus, "Enter disposal reason."); return; }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Dispose evidence " + sel[0] + "? This cannot be undone.",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm Disposal");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    String logID = "LOG-" + System.currentTimeMillis();
                    String custodianID = SessionManager.getInstance().getCurrentUserID();
                    boolean ok1 = evidenceDAO.updateEvidenceStatus(sel[0], "Disposed");
                    boolean ok2 = evidenceDAO.insertCustodyLog(logID, sel[0], sel[2],
                            "Disposed", custodianID, dispField.getText().trim());
                    if (ok1 && ok2) {
                        showSuccess(disposalStatus, "Evidence " + sel[0] + " disposed.");
                        dispField.clear();
                        loadTable(table, evidenceDAO.getAllEvidence());
                    } else showError(disposalStatus, "Disposal failed.");
                }
            });
        });

        disposalCard.getChildren().addAll(dispLabel, dispField, disposeBtn, disposalStatus);

        box.getChildren().addAll(title, table, refreshBtn, transferCard, disposalCard);
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#0b0f1a;-fx-background-color:#0b0f1a;");
        tab.setContent(scroll);
        return tab;
    }

    // ── Chain of Custody ─────────────────────────────────────────────────────
    private Tab buildCustodyLogTab() {
        Tab tab = new Tab("🔗  Chain of Custody");

        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0b0f1a;");

        Label title = sectionTitle("Track Chain of Custody");

        TextField evField = new TextField();
        evField.setPromptText("Enter Evidence ID...");
        styleTextField(evField);

        TableView<String[]> logTable = buildStyledTable(
                new String[]{"Log ID", "Old Status", "New Status", "Changed By", "Timestamp", "Notes"});
        VBox.setVgrow(logTable, Priority.ALWAYS);

        Label status = statusLabel();
        Button loadBtn = primaryButton("Load Custody Log");
        loadBtn.setOnAction(e -> {
            String evID = evField.getText().trim();
            if (evID.isEmpty()) { showError(status, "Enter an Evidence ID."); return; }
            List<String[]> log = evidenceDAO.getCustodyLog(evID);
            loadTable(logTable, log);
            if (log.isEmpty()) showError(status, "No custody log for " + evID);
            else showSuccess(status, log.size() + " entries found.");
        });

        HBox row = new HBox(10, evField, loadBtn);
        HBox.setHgrow(evField, Priority.ALWAYS);
        row.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(title, row, status, logTable);
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
