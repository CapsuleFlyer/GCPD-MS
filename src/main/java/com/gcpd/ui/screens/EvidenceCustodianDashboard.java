package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.EvidenceDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * EvidenceCustodianDashboard: handles:
 * UC-06: Manage Evidence Lifecycle (transfer + disposal)
 * UC-11: Authorize Evidence Transfer (chain of custody)
 */
public class EvidenceCustodianDashboard extends BaseScreen {

    private final EvidenceDAO evidenceDAO = new EvidenceDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #1a1a2e;");
        tabs.getTabs().addAll(
            buildEvidenceListTab(),     // UC-06 + UC-11
            buildCustodyLogTab()        // View chain of custody
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        contentArea.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return root;
    }

    // ── UC-06 + UC-11: Evidence Management ──────────────────────────────────
    private Tab buildEvidenceListTab() {
        Tab tab = new Tab("🗂  Manage Evidence");

        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("UC-06 & UC-11: Evidence Lifecycle & Transfer");

        // Evidence table
        TableView<String[]> table = buildTable(
            new String[]{"Evidence ID", "Type", "Status", "Storage Location", "Case ID", "Collected Date"}
        );
        VBox.setVgrow(table, Priority.ALWAYS);
        loadEvidence(table);

        // Transfer section
        VBox transferCard = card("Authorize Evidence Transfer  (UC-11)");

        Label destLabel = fieldLabel("Transfer Destination *");
        TextField destField = new TextField();
        destField.setPromptText("e.g. Forensics Lab, Court Room 3");
        styleTextField(destField);

        Label notesLabel = fieldLabel("Transfer Notes");
        TextField notesField = new TextField();
        notesField.setPromptText("Optional notes for chain of custody...");
        styleTextField(notesField);

        Label transferStatus = statusLabel();
        Button transferBtn = primaryButton("Authorize Transfer");

        transferBtn.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            if (selected == null)           { showError(transferStatus, "Select evidence first."); return; }
            if (destField.getText().isBlank()){ showError(transferStatus, "Destination is required."); return; }
            if ("Disposed".equals(selected[2])){ showError(transferStatus, "Disposed evidence cannot be transferred."); return; }

            String evidenceID = selected[0];
            String oldStatus  = selected[2];
            String custodianID = SessionManager.getInstance().getCurrentUserID();
            String logID = "LOG-" + System.currentTimeMillis();

            boolean ok1 = evidenceDAO.updateEvidenceStatus(evidenceID, "Transferred");
            boolean ok2 = evidenceDAO.insertCustodyLog(
                logID, evidenceID, oldStatus, "Transferred to " + destField.getText().trim(),
                custodianID, notesField.getText().trim()
            );

            if (ok1 && ok2) {
                showSuccess(transferStatus, "Evidence " + evidenceID + " transferred to " + destField.getText());
                destField.clear(); notesField.clear();
                loadEvidence(table);
            } else {
                showError(transferStatus, "Transfer failed.");
            }
        });

        transferCard.getChildren().addAll(destLabel, destField, notesLabel, notesField,
                                           transferBtn, transferStatus);

        // Disposal section
        VBox disposalCard = card("Dispose Evidence  (UC-06)");

        Label dispNotesLabel = fieldLabel("Disposal Reason *");
        TextField dispNotesField = new TextField();
        dispNotesField.setPromptText("Reason for disposal...");
        styleTextField(dispNotesField);

        Label disposalStatus = statusLabel();
        Button disposeBtn = new Button("Mark as Disposed");
        disposeBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: #ff6666; " +
                            "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18;");
        disposeBtn.setCursor(javafx.scene.Cursor.HAND);

        disposeBtn.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            if (selected == null)              { showError(disposalStatus, "Select evidence first."); return; }
            if ("Disposed".equals(selected[2])){ showError(disposalStatus, "Already disposed."); return; }
            if (dispNotesField.getText().isBlank()){ showError(disposalStatus, "Enter disposal reason."); return; }

            // Confirm dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to dispose evidence " + selected[0] + "? This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm Disposal");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    String logID = "LOG-" + System.currentTimeMillis();
                    String custodianID = SessionManager.getInstance().getCurrentUserID();
                    boolean ok1 = evidenceDAO.updateEvidenceStatus(selected[0], "Disposed");
                    boolean ok2 = evidenceDAO.insertCustodyLog(
                        logID, selected[0], selected[2], "Disposed",
                        custodianID, dispNotesField.getText().trim()
                    );
                    if (ok1 && ok2) {
                        showSuccess(disposalStatus, "Evidence " + selected[0] + " marked as disposed.");
                        dispNotesField.clear();
                        loadEvidence(table);
                    } else {
                        showError(disposalStatus, "Disposal failed.");
                    }
                }
            });
        });

        disposalCard.getChildren().addAll(dispNotesLabel, dispNotesField, disposeBtn, disposalStatus);

        Button refreshBtn = secondaryButton("Refresh");
        refreshBtn.setOnAction(e -> loadEvidence(table));

        box.getChildren().addAll(title, table, refreshBtn, transferCard, disposalCard);
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e;");
        tab.setContent(scroll);
        return tab;
    }

    // ── Chain of Custody Log ─────────────────────────────────────────────────
    private Tab buildCustodyLogTab() {
        Tab tab = new Tab("🔗  Chain of Custody");

        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("Track Chain of Custody");

        Label evLabel = fieldLabel("Evidence ID:");
        TextField evField = new TextField();
        evField.setPromptText("Enter Evidence ID...");
        styleTextField(evField);

        TableView<String[]> logTable = buildTable(
            new String[]{"Log ID", "Old Status", "New Status", "Changed By", "Timestamp", "Notes"}
        );
        VBox.setVgrow(logTable, Priority.ALWAYS);

        Label status = statusLabel();
        Button loadBtn = primaryButton("Load Custody Log");
        loadBtn.setOnAction(e -> {
            String evID = evField.getText().trim();
            if (evID.isEmpty()) { showError(status, "Enter an Evidence ID."); return; }
            List<String[]> log = evidenceDAO.getCustodyLog(evID);
            logTable.setItems(FXCollections.observableArrayList(log));
            if (log.isEmpty()) showError(status, "No custody log found for " + evID);
            else showSuccess(status, log.size() + " custody entries found.");
        });

        HBox row = new HBox(10, evField, loadBtn);
        HBox.setHgrow(evField, Priority.ALWAYS);
        row.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(title, row, status, logTable);
        tab.setContent(box);
        return tab;
    }

    private void loadEvidence(TableView<String[]> table) {
        table.setItems(FXCollections.observableArrayList(evidenceDAO.getAllEvidence()));
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
