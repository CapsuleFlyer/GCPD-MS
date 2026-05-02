package com.gcpd.ui.screens;

import com.gcpd.bl.model.SessionManager;
import com.gcpd.db.CaseDAO;
import com.gcpd.db.OperationDAO;
import com.gcpd.db.UserDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * SergeantDashboard: handles:
 * UC-02: Assign Detective to Case
 * UC-04 (submit side): Submit High-Risk Operation Request
 */
public class SergeantDashboard extends BaseScreen {

    private final CaseDAO caseDAO     = new CaseDAO();
    private final UserDAO userDAO     = new UserDAO();
    private final OperationDAO opDAO  = new OperationDAO();

    @Override
    public Parent getView() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #1a1a2e;");
        tabs.getTabs().addAll(
            buildAssignTab(),       // UC-02
            buildAllCasesTab(),     // View all cases
            buildSubmitOpTab()      // UC-04 (Sergeant side)
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        contentArea.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return root;
    }

    // ── UC-02: Assign Detective to Case ─────────────────────────────────────
    private Tab buildAssignTab() {
        Tab tab = new Tab("👮  Assign Detective");

        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("UC-02: Assign Detective to Case");

        // Unassigned cases table
        Label unassignedLabel = fieldLabel("Unassigned Cases:");
        TableView<String[]> caseTable = buildTable(
            new String[]{"Case ID", "Status", "Priority", "Location", "Crime Type"}
        );
        VBox.setVgrow(caseTable, Priority.ALWAYS);
        loadUnassignedCases(caseTable);

        // Detective picker
        Label detLabel = fieldLabel("Select Detective:");
        ComboBox<String> detectiveBox = new ComboBox<>();
        detectiveBox.setPromptText("Choose detective...");
        detectiveBox.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(detectiveBox);
        loadDetectives(detectiveBox);

        // Workload info
        Label workloadInfo = new Label("ℹ  Detectives with 5+ cases are at capacity and cannot be assigned.");
        workloadInfo.setStyle("-fx-text-fill: #888888; -fx-font-size: 11;");

        Label status = statusLabel();
        HBox btnRow = new HBox(10);
        Button refreshBtn = secondaryButton("Refresh");
        Button assignBtn  = primaryButton("Assign Detective");

        refreshBtn.setOnAction(e -> {
            loadUnassignedCases(caseTable);
            loadDetectives(detectiveBox);
        });

        assignBtn.setOnAction(e -> {
            String[] selectedCase = caseTable.getSelectionModel().getSelectedItem();
            String   selectedDet  = detectiveBox.getValue();

            if (selectedCase == null) { showError(status, "Select a case first."); return; }
            if (selectedDet == null)  { showError(status, "Select a detective."); return; }

            // Extract userID from "Name (ID)" format
            String detID = selectedDet.substring(selectedDet.lastIndexOf("(") + 1,
                                                   selectedDet.lastIndexOf(")"));

            boolean ok = caseDAO.assignDetective(selectedCase[0], detID);
            if (ok) {
                userDAO.updateWorkload(detID, getWorkloadScore(detID) + 1);
                showSuccess(status, "Detective " + selectedDet + " assigned to case " + selectedCase[0]);
                loadUnassignedCases(caseTable);
            } else {
                showError(status, "Assignment failed.");
            }
        });

        btnRow.getChildren().addAll(refreshBtn, assignBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(title, unassignedLabel, caseTable,
                                  detLabel, detectiveBox, workloadInfo, btnRow, status);
        tab.setContent(box);
        return tab;
    }

    private void loadUnassignedCases(TableView<String[]> table) {
        List<String[]> list = caseDAO.getUnassignedCases();
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void loadDetectives(ComboBox<String> box) {
        box.getItems().clear();
        userDAO.getAllDetectives().forEach(u ->
            box.getItems().add(u.getName() + " (" + u.getUserID() + ")")
        );
    }

    private int getWorkloadScore(String detID) {
        // Simple: count assigned cases
        return (int) caseDAO.getAllCases().stream()
            .filter(c -> detID.equals(c[4]) && !"Closed".equals(c[1]))
            .count();
    }

    // ── All Cases overview ───────────────────────────────────────────────────
    private Tab buildAllCasesTab() {
        Tab tab = new Tab("📁  All Cases");

        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label title = sectionTitle("All Active Cases");
        TableView<String[]> table = buildTable(
            new String[]{"Case ID", "Status", "Priority", "Start Date", "Detective", "Location", "Crime Type"}
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        Button refreshBtn = secondaryButton("Refresh");
        refreshBtn.setOnAction(e -> table.setItems(
            FXCollections.observableArrayList(caseDAO.getAllCases())
        ));

        table.setItems(FXCollections.observableArrayList(caseDAO.getAllCases()));
        box.getChildren().addAll(title, table, refreshBtn);
        tab.setContent(box);
        return tab;
    }

    // ── UC-04 Sergeant side: Submit Operation Request ────────────────────────
    private Tab buildSubmitOpTab() {
        Tab tab = new Tab("⚠  Request Operation");

        VBox box = card("UC-04: Submit High-Risk Operation Request");
        box.setMaxWidth(600);

        Label descLabel = fieldLabel("Operation Description *");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the high-risk operation...");
        descArea.setPrefRowCount(4);
        styleTextArea(descArea);

        Label riskLabel = fieldLabel("Risk Level");
        ComboBox<String> riskBox = new ComboBox<>(FXCollections.observableArrayList(
            "High", "Critical"
        ));
        riskBox.setValue("High");
        styleComboBox(riskBox);
        riskBox.setMaxWidth(Double.MAX_VALUE);

        Label caseLabel = fieldLabel("Linked Case ID (optional)");
        TextField caseField = new TextField();
        caseField.setPromptText("e.g. CASE-123456");
        styleTextField(caseField);

        Label status = statusLabel();
        Button submitBtn = primaryButton("Submit for Commissioner Approval");

        submitBtn.setOnAction(e -> {
            String desc = descArea.getText().trim();
            if (desc.isEmpty()) { showError(status, "Description is required."); return; }

            String opID = "OP-" + System.currentTimeMillis();
            String reqBy = SessionManager.getInstance().getCurrentUserID();
            String caseID = caseField.getText().trim().isEmpty() ? null : caseField.getText().trim();

            boolean ok = opDAO.insertOperation(opID, reqBy, riskBox.getValue(), caseID, desc);
            if (ok) {
                showSuccess(status, "Operation " + opID + " submitted for approval.");
                descArea.clear(); caseField.clear();
            } else {
                showError(status, "Submission failed.");
            }
        });

        box.getChildren().addAll(descLabel, descArea, riskLabel, riskBox,
                                  caseLabel, caseField, submitBtn, status);

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e;");
        tab.setContent(scroll);
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
