package org.Gh0st1yAnge1.gui.controllers;

import org.Gh0st1yAnge1.gui.visualization.CanvasRenderer;
import org.Gh0st1yAnge1.gui.localization.LocaleManager;
import org.Gh0st1yAnge1.gui.models.RouteTableItem;
import org.Gh0st1yAnge1.gui.network.ServerConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.Gh0st1yAnge1.model.Route;
import org.Gh0st1yAnge1.request_and_response.Response;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainController {

    @FXML private TableView<RouteTableItem>          tableView;
    @FXML private TableColumn<RouteTableItem, Integer> colKey;
    @FXML private TableColumn<RouteTableItem, String>  colName;
    @FXML private TableColumn<RouteTableItem, Float>   colCoordX;
    @FXML private TableColumn<RouteTableItem, Float>   colCoordY;
    @FXML private TableColumn<RouteTableItem, String>  colDate;
    @FXML private TableColumn<RouteTableItem, Long>    colFromX;
    @FXML private TableColumn<RouteTableItem, Integer> colFromY;
    @FXML private TableColumn<RouteTableItem, Integer> colFromZ;
    @FXML private TableColumn<RouteTableItem, String>  colFromName;
    @FXML private TableColumn<RouteTableItem, Double>  colToX;
    @FXML private TableColumn<RouteTableItem, Float>   colToY;
    @FXML private TableColumn<RouteTableItem, Integer> colToZ;
    @FXML private TableColumn<RouteTableItem, String>  colToName;
    @FXML private TableColumn<RouteTableItem, Float>   colDistance;
    @FXML private TableColumn<RouteTableItem, Long>    colOwner;

    @FXML private Label           lblUser;
    @FXML private Button          btnAdd;
    @FXML private Button          btnEdit;
    @FXML private Button          btnDelete;
    @FXML private Button          btnRefresh;
    @FXML private Button          btnClear;
    @FXML private Button          btnLogout;
    @FXML private Button          btnInfo;

    @FXML private Label           lblLabCommandsTitle;
    @FXML private Button          btnAverageOfDistance;
    @FXML private Button          btnCountByDistance;
    @FXML private Button          btnFilterLessThanDistance;
    @FXML private Button          btnRemoveGreater;
    @FXML private Button          btnRemoveGreaterKey;
    @FXML private Button          btnReplaceIfLower;

    @FXML private Label           filterLabel;
    @FXML private Label           sortLabel;
    @FXML private TextField       tfFilter;
    @FXML private ComboBox<String> cbFilterColumn;
    @FXML private ComboBox<String> cbSortColumn;
    @FXML private ComboBox<String> cbSortDir;
    @FXML private Label           lblStatus;
    @FXML private Label           lblCanvasInfo;

    @FXML private Canvas canvas;

    private ServerConnection connection;
    private String           currentLogin;
    private long             myOwnerId = -1;

    private final LocaleManager lm = LocaleManager.getInstance();
    private final ObservableList<RouteTableItem> allItems      = FXCollections.observableArrayList();
    private final ObservableList<RouteTableItem> filteredItems = FXCollections.observableArrayList();

    private CanvasRenderer renderer;
    private ScheduledExecutorService scheduler;

    private static final List<String> COLUMN_NAMES =
            List.of("key","name","coordX","coordY","creationDate","from","to","distance","ownerId");

    public void init(ServerConnection connection, String login) {
        this.connection   = connection;
        this.currentLogin = login;

        setupTable();
        setupCanvas();
        setupButtons();
        applyLocale();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "refresh-scheduler");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::backgroundRefresh, 0, 30, TimeUnit.SECONDS);
    }

    public void setMyOwnerId(long myOwnerId) {
        this.myOwnerId = myOwnerId;
        if (renderer != null) renderer.setMyOwnerId(myOwnerId);
        Platform.runLater(() -> {
            lblUser.setText(MessageFormat.format(lm.get("main.user"), currentLogin));
            updateButtonsDisableState(tableView.getSelectionModel().getSelectedItem());
        });
    }

    public void shutdown() {
        if (scheduler != null) scheduler.shutdownNow();
        if (renderer  != null) renderer.stop();
        if (connection != null) connection.close();
    }

    private void setupTable() {
        colKey.setCellValueFactory(new PropertyValueFactory<>("key"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCoordX.setCellValueFactory(new PropertyValueFactory<>("coordX"));
        colCoordY.setCellValueFactory(new PropertyValueFactory<>("coordY"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        colFromX.setCellValueFactory(new PropertyValueFactory<>("fromX"));
        colFromY.setCellValueFactory(new PropertyValueFactory<>("fromY"));
        colFromZ.setCellValueFactory(new PropertyValueFactory<>("fromZ"));
        colFromName.setCellValueFactory(new PropertyValueFactory<>("fromName"));
        colToX.setCellValueFactory(new PropertyValueFactory<>("toX"));
        colToY.setCellValueFactory(new PropertyValueFactory<>("toY"));
        colToZ.setCellValueFactory(new PropertyValueFactory<>("toZ"));
        colToName.setCellValueFactory(new PropertyValueFactory<>("toName"));
        colDistance.setCellValueFactory(new PropertyValueFactory<>("distance"));
        colOwner.setCellValueFactory(new PropertyValueFactory<>("ownerId"));

        tableView.setItems(filteredItems);

        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        btnReplaceIfLower.setDisable(true);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButtonsDisableState(newSelection);
        });

        tableView.setRowFactory(tv -> {
            TableRow<RouteTableItem> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    RouteTableItem item = row.getItem();
                    if (item.getOwnerId() == myOwnerId) {
                        openEditDialog(item);
                    } else {
                        showAlert(Alert.AlertType.ERROR, lm.get("error.notOwner"));
                    }
                }
            });
            return row;
        });
    }

    private void updateButtonsDisableState(RouteTableItem selectedItem) {
        if (selectedItem == null) {
            btnEdit.setDisable(true);
            btnDelete.setDisable(true);
            btnReplaceIfLower.setDisable(true);
            btnEdit.setTooltip(null);
            btnDelete.setTooltip(null);
        } else {
            boolean isMyObject = selectedItem.getOwnerId() == myOwnerId;
            btnEdit.setDisable(!isMyObject);
            btnDelete.setDisable(!isMyObject);
            btnReplaceIfLower.setDisable(!isMyObject);

            if (!isMyObject) {
                btnEdit.setTooltip(new Tooltip(lm.get("main.tooltip.disabled")));
                btnDelete.setTooltip(new Tooltip(lm.get("main.tooltip.disabled")));
                btnReplaceIfLower.setTooltip(new Tooltip(lm.get("main.tooltip.disabled")));
            } else {
                btnEdit.setTooltip(null);
                btnDelete.setTooltip(null);
                btnReplaceIfLower.setTooltip(null);
            }
        }
    }
    private void setupFilters() {
        cbFilterColumn.setOnAction(null);
        cbSortColumn.setOnAction(null);
        cbSortDir.setOnAction(null);

        int prevFilterIdx = Math.max(0, cbFilterColumn.getSelectionModel().getSelectedIndex());
        int prevSortIdx   = Math.max(0, cbSortColumn.getSelectionModel().getSelectedIndex());
        int prevDirIdx    = Math.max(0, cbSortDir.getSelectionModel().getSelectedIndex());

        List<String> colLabels = COLUMN_NAMES.stream()
                .map(c -> lm.get("col." + c.replace("creationDate","date").replace("ownerId","owner")))
                .toList();

        cbFilterColumn.setItems(FXCollections.observableArrayList(colLabels));
        cbFilterColumn.getSelectionModel().select(prevFilterIdx);

        cbSortColumn.setItems(FXCollections.observableArrayList(colLabels));
        cbSortColumn.getSelectionModel().select(prevSortIdx);

        cbSortDir.setItems(FXCollections.observableArrayList(lm.get("main.sort.asc"), lm.get("main.sort.desc")));
        cbSortDir.getSelectionModel().select(prevDirIdx);

        tfFilter.textProperty().addListener((obs, o, n) -> applyFilterAndSort());
        cbFilterColumn.setOnAction(e -> applyFilterAndSort());
        cbSortColumn.setOnAction(e -> applyFilterAndSort());
        cbSortDir.setOnAction(e -> applyFilterAndSort());
    }

    private void applyFilterAndSort() {
        String filterText = tfFilter.getText().trim().toLowerCase();
        int    filterIdx  = cbFilterColumn.getSelectionModel().getSelectedIndex();
        int    sortIdx    = cbSortColumn.getSelectionModel().getSelectedIndex();
        boolean asc       = cbSortDir.getSelectionModel().getSelectedIndex() == 0;

        if (filterIdx < 0 || sortIdx < 0) return;

        List<RouteTableItem> result = allItems.stream()
                .filter(item -> {
                    if (filterText.isEmpty()) return true;
                    String val = getFieldValue(item, filterIdx);
                    return val.toLowerCase().contains(filterText);
                })
                .sorted((a, b) -> {
                    int cmp = compareField(a, b, sortIdx);
                    return asc ? cmp : -cmp;
                })
                .collect(Collectors.toList());

        filteredItems.setAll(result);
        if (renderer != null) renderer.setItems(filteredItems);
    }

    private String getFieldValue(RouteTableItem item, int colIdx) {
        return switch (colIdx) {
            case 0  -> String.valueOf(item.getKey());
            case 1  -> item.getName();
            case 2  -> String.valueOf(item.getCoordX());
            case 3  -> String.valueOf(item.getCoordY());
            case 4  -> item.creationDateProperty().get();
            case 5  -> String.format("X:%s Y:%s Z:%s %s", item.getFromX(), item.getFromY(), item.getFromZ(), item.getFromName());
            case 6  -> String.format("X:%s Y:%s Z:%s %s", item.getToX(), item.getToY(), item.getToZ(), item.getToName());
            case 7  -> String.valueOf(item.getDistance());
            case 8  -> String.valueOf(item.getOwnerId());
            default -> "";
        };
    }

    private int compareField(RouteTableItem a, RouteTableItem b, int colIdx) {
        return switch (colIdx) {
            case 0  -> Integer.compare(a.getKey(),      b.getKey());
            case 1  -> a.getName().compareTo(b.getName());
            case 2  -> Float.compare(a.getCoordX(),     b.getCoordX());
            case 3  -> Float.compare(a.getCoordY(),     b.getCoordY());
            case 4  -> a.creationDateProperty().get().compareTo(b.creationDateProperty().get());
            case 5  -> Long.compare(a.getFromX() != null ? a.getFromX() : 0L, b.getFromX() != null ? b.getFromX() : 0L);
            case 6  -> Double.compare(a.getToX() != null ? a.getToX() : 0.0, b.getToX() != null ? b.getToX() : 0.0);
            case 7  -> Float.compare(a.getDistance(),   b.getDistance());
            case 8  -> Long.compare(a.getOwnerId(),     b.getOwnerId());
            default -> 0;
        };
    }

    private void setupCanvas() {
        renderer = new CanvasRenderer(canvas);
        renderer.setOnClick(item -> {
            String msg = MessageFormat.format(lm.get("canvas.click"),
                    item.getName(), lm.numberFormat().format(item.getDistance()), item.getKey());
            Platform.runLater(() -> {
                lblCanvasInfo.setText(msg);
                tableView.getSelectionModel().select(item);
            });
        });
    }

    private void setupButtons() {
        btnAdd.setOnAction(e -> onAdd());

        btnEdit.setOnAction(e -> {
            RouteTableItem sel = tableView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                if (sel.getOwnerId() == myOwnerId) {
                    openEditDialog(sel);
                } else {
                    showAlert(Alert.AlertType.ERROR, lm.get("error.notOwner"));
                }
            }
        });

        btnDelete.setOnAction(e -> {
            RouteTableItem sel = tableView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                if (sel.getOwnerId() == myOwnerId) {
                    onDelete(sel);
                } else {
                    showAlert(Alert.AlertType.ERROR, lm.get("error.notOwner"));
                }
            }
        });

        btnRefresh.setOnAction(e -> backgroundRefresh());
        btnClear.setOnAction(e -> onClear());
        btnLogout.setOnAction(e -> onLogout());
        btnInfo.setOnAction(e -> onInfo());

        btnAverageOfDistance.setOnAction(e -> onAverageOfDistance());
        btnCountByDistance.setOnAction(e -> onCountByDistance());
        btnFilterLessThanDistance.setOnAction(e -> onFilterLessThanDistance());
        btnRemoveGreater.setOnAction(e -> onRemoveGreater());
        btnRemoveGreaterKey.setOnAction(e -> onRemoveGreaterKey());
        btnReplaceIfLower.setOnAction(e -> onReplaceIfLower());
    }

    private void handleServerResponseError(Response resp, String defaultMessageKey) {
        String serverMsg = resp.message() != null ? resp.message().toLowerCase() : "";

        if (serverMsg.contains("exist") || serverMsg.contains("найден") || serverMsg.contains("not found")) {
            showAlert(Alert.AlertType.ERROR, lm.get("main.error.not_found"));
        } else if (serverMsg.contains("owner") || serverMsg.contains("permission") || serverMsg.contains("принадлежит")) {
            showAlert(Alert.AlertType.ERROR, lm.get("error.notOwner"));
        } else {
            showAlert(Alert.AlertType.ERROR, lm.get(defaultMessageKey));
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void onAverageOfDistance() {
        new Thread(() -> {
            try {
                Response resp = connection.averageOfDistance();
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, lm.get("main.btn.average") + ": " + resp.message()));
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, lm.get("error.connection") + ": " + ex.getMessage()));
            }
        }).start();
    }

    private void onCountByDistance() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(lm.get("main.btn.countdist"));
        dialog.setHeaderText(null);
        dialog.setContentText(lm.get("main.dialog.enter_distance"));

        dialog.showAndWait().ifPresent(val -> {
            try {
                double dist = Double.parseDouble(val.trim());
                new Thread(() -> {
                    try {
                        Response resp = connection.countByDistance(dist);
                        if (resp.success()) {
                            String count = resp.message();
                            String finalMsg = MessageFormat.format(lm.get("main.btn.cntdist"), count);
                            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, finalMsg));
                        } else {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, resp.message()));
                        }
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, ex.getMessage()));
                    }
                }).start();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, lm.get("dialog.error.coords"));
            }
        });
    }

    private void onFilterLessThanDistance() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(lm.get("main.btn.filterlist"));
        dialog.setHeaderText(null);
        dialog.setContentText(lm.get("main.dialog.enter_distance"));
        dialog.showAndWait().ifPresent(val -> {
            try {
                double dist = Double.parseDouble(val.trim());
                new Thread(() -> {
                    try {
                        Response resp = connection.filterLessThanDistance(dist);
                        Platform.runLater(() -> {
                            if (resp.success() && resp.collection() != null) {
                                String joined = resp.collection().stream()
                                        .map(r -> "Key " + r.getKey() + ": " + r.getName() + " (" + r.getDistance() + ")")
                                        .collect(Collectors.joining("\n"));
                                showAlert(Alert.AlertType.INFORMATION, joined.isEmpty() ? lm.get("main.status.no_elements") : joined);
                            }
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, ex.getMessage()));
                    }
                }).start();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, lm.get("dialog.error.coords"));
            }
        });
    }

    private void onRemoveGreater() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_dialog.fxml"));
            Parent root = loader.load();
            EditDialogController dc = loader.getController();
            dc.setForAdd();

            Stage dlg = dialogStage(root, lm.get("main.btn.remgreater"));
            dlg.showAndWait();

            if (!dc.isSaved()) return;
            Route refRoute = dc.getRoute();

            new Thread(() -> {
                try {
                    Response resp = connection.removeGreater(refRoute);
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, resp.message());
                        backgroundRefresh();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, ex.getMessage()));
                }
            }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onRemoveGreaterKey() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(lm.get("main.btn.remgreater_key"));
        dialog.setHeaderText(null);
        dialog.setContentText(lm.get("main.dialog.enter_key"));
        dialog.showAndWait().ifPresent(val -> {
            try {
                int key = Integer.parseInt(val.trim());
                new Thread(() -> {
                    try {
                        Response resp = connection.removeGreaterKey(key);
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, resp.message());
                            backgroundRefresh();
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, ex.getMessage()));
                    }
                }).start();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, lm.get("dialog.error.key"));
            }
        });
    }

    private void onReplaceIfLower() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(lm.get("main.btn.replacelower"));
        dialog.setHeaderText(null);
        dialog.setContentText(lm.get("main.dialog.enter_key"));
        dialog.showAndWait().ifPresent(val -> {
            try {
                int key = Integer.parseInt(val.trim());
                RouteTableItem localItem = allItems.stream().filter(i -> i.getKey() == key).findFirst().orElse(null);
                if (localItem != null && localItem.getOwnerId() != myOwnerId) {
                    showAlert(Alert.AlertType.ERROR, lm.get("error.notOwner"));
                    return;
                }

                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_dialog.fxml"));
                        Parent root = loader.load();
                        EditDialogController dc = loader.getController();
                        dc.setForEdit(key, null);

                        Stage dlg = dialogStage(root, lm.get("main.btn.replacelower"));
                        dlg.showAndWait();

                        if (!dc.isSaved()) return;
                        Route newRoute = dc.getRoute();

                        new Thread(() -> {
                            try {
                                Response resp = connection.replaceIfLower(key, newRoute);
                                Platform.runLater(() -> {
                                    if (resp.success()) {
                                        showAlert(Alert.AlertType.INFORMATION, resp.message());
                                        backgroundRefresh();
                                    } else {
                                        handleServerResponseError(resp, "main.error.action_failed");
                                    }
                                });
                            } catch (Exception ex) {
                                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, ex.getMessage()));
                            }
                        }).start();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, lm.get("dialog.error.key"));
            }
        });
    }

    private void onAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_dialog.fxml"));
            Parent root = loader.load();
            EditDialogController dc = loader.getController();
            dc.setForAdd();

            Stage dlg = dialogStage(root, lm.get("dialog.add.title"));
            dlg.showAndWait();

            if (!dc.isSaved()) return;
            int key = dc.getKey();
            Route route = dc.getRoute();

            setStatus(lm.get("status.refreshing"));
            new Thread(() -> {
                try {
                    Response check = connection.checkInsertKey(key);
                    if (!check.success()) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, lm.get("main.error.key_exists")));
                        return;
                    }

                    Response resp = connection.insert(key, route);
                    Platform.runLater(() -> {
                        if (resp.success()) {
                            route.setOwnerId(myOwnerId);
                            route.setKey((long) key);
                            allItems.add(new RouteTableItem(key, route));
                            applyFilterAndSort();
                            setStatus(lm.get("status.connected"));
                        } else {
                            handleServerResponseError(resp, "main.error.action_failed");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, lm.get("error.connection") + ": " + ex.getMessage()));
                }
            }, "insert-thread").start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void onDelete(RouteTableItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lm.get("delete.confirm.title"));
        confirm.setHeaderText(lm.get("delete.confirm.header"));
        confirm.setContentText(MessageFormat.format(lm.get("delete.confirm.content"), item.getKey()));
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Response resp = connection.removeKey(item.getKey());
                        Platform.runLater(() -> {
                            if (resp.success()) {
                                allItems.removeIf(tableItem -> tableItem.getKey() == item.getKey());
                                applyFilterAndSort();
                                setStatus(lm.get("status.connected"));
                            } else {
                                handleServerResponseError(resp, "error.notOwner");
                                backgroundRefresh();
                            }
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, lm.get("error.connection") + ": " + ex.getMessage()));
                    }
                }, "delete-thread").start();
            }
        });
    }

    private void onClear() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lm.get("clear.confirm.title"));
        confirm.setHeaderText(lm.get("clear.confirm.header"));
        confirm.setContentText(lm.get("clear.confirm.content"));
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Response resp = connection.clear();
                        Platform.runLater(() -> {
                            if (resp.success()) {
                                allItems.removeIf(item -> item.getOwnerId() == myOwnerId);
                                applyFilterAndSort();
                                setStatus(lm.get("status.connected"));
                            } else {
                                showAlert(Alert.AlertType.ERROR, resp.message());
                            }
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, lm.get("error.connection") + ": " + ex.getMessage()));
                    }
                }, "clear-thread").start();
            }
        });
    }

    private void onInfo() {
        new Thread(() -> {
            try {
                Response resp = connection.info();
                if (!resp.success()) throw new Exception(resp.message());
                String[] data = resp.message().split("\\|");

                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle(lm.get("info.title"));
                    a.setHeaderText(null);

                    String content = MessageFormat.format(
                            lm.get("info.template"),
                            data[0],
                            data[1],
                            data[2]
                    );

                    a.setContentText(content);
                    a.showAndWait();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, lm.get("error.connection")));
            }
        }, "info-thread").start();
    }

    private void onLogout() {
        shutdown();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(lm.get("app.title"));
            stage.setScene(new Scene(root, 420, 400));
            stage.show();
            ((Stage) btnLogout.getScene().getWindow()).close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openEditDialog(RouteTableItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_dialog.fxml"));
            Parent root = loader.load();
            EditDialogController dc = loader.getController();
            dc.setForEdit(item.getKey(), item.getRoute());

            Stage dlg = dialogStage(root, lm.get("dialog.edit.title"));
            dlg.showAndWait();

            if (!dc.isSaved()) return;
            Route updated = dc.getRoute();
            int key = dc.getKey();

            setStatus(lm.get("status.refreshing"));
            new Thread(() -> {
                try {
                    Response resp = connection.update(key, updated);
                    Platform.runLater(() -> {
                        if (resp.success()) {
                            for (int i = 0; i < allItems.size(); i++) {
                                if (allItems.get(i).getKey() == key) {
                                    updated.setOwnerId(myOwnerId);
                                    updated.setKey((long) key);
                                    allItems.set(i, new RouteTableItem(key, updated));
                                    break;
                                }
                            }
                            applyFilterAndSort();
                            setStatus(lm.get("status.connected"));
                        } else {
                            handleServerResponseError(resp, "error.notOwner");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, lm.get("error.connection") + ": " + ex.getMessage()));
                }
            }, "update-thread").start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void backgroundRefresh() {
        try {
            if (!connection.isConnected()) connection.connect();
            Response resp = connection.show();
            if (resp.success() && resp.collection() != null) {
                List<Route> routes = resp.collection();
                Platform.runLater(() -> {
                    RouteTableItem currentSelection = tableView.getSelectionModel().getSelectedItem();
                    int selectedKey = currentSelection != null ? currentSelection.getKey() : -1;

                    allItems.clear();
                    for (Route route : routes) {
                        Long key = route.getKey();
                        if (key != null) {
                            allItems.add(new RouteTableItem(Math.toIntExact(key), route));
                        }
                    }
                    applyFilterAndSort();
                    if (selectedKey != -1) {
                        for (RouteTableItem item : filteredItems) {
                            if (item.getKey() == selectedKey) {
                                tableView.getSelectionModel().select(item);
                                break;
                            }
                        }
                    }

                    if (renderer != null) renderer.setMyOwnerId(myOwnerId);
                    setStatus(lm.get("status.connected"));
                });
            }
        } catch (Exception ex) {
            Platform.runLater(() -> setStatus(lm.get("status.disconnected") + ": " + ex.getMessage()));
        }
    }

    private void applyLocale() {
        filterLabel.setText(lm.get("main.label.filter_by"));
        sortLabel.setText(lm.get("main.label.sort_by"));
        tfFilter.setPromptText(lm.get("main.prompt.enter_filter"));

        btnAdd.setText(lm.get("main.btn.add"));
        btnEdit.setText(lm.get("main.btn.edit"));
        btnDelete.setText(lm.get("main.btn.delete"));
        btnRefresh.setText(lm.get("main.btn.refresh"));
        btnClear.setText(lm.get("main.btn.clear"));
        btnLogout.setText(lm.get("main.btn.logout"));
        btnInfo.setText(lm.get("main.btn.info"));

        btnAverageOfDistance.setText(lm.get("main.btn.average"));
        btnCountByDistance.setText(lm.get("main.btn.countdist"));
        btnFilterLessThanDistance.setText(lm.get("main.btn.filterlist"));
        btnRemoveGreater.setText(lm.get("main.btn.remgreater"));
        btnRemoveGreaterKey.setText(lm.get("main.btn.remgreater_key"));
        btnReplaceIfLower.setText(lm.get("main.btn.replacelower"));

        colKey.setText(lm.get("col.key"));
        colName.setText(lm.get("col.name"));
        colCoordX.setText(lm.get("col.coordX"));
        colCoordY.setText(lm.get("col.coordY"));
        colDate.setText(lm.get("col.date"));
        colFromX.setText(lm.get("col.from.x"));
        colFromY.setText(lm.get("col.from.y"));
        colFromZ.setText(lm.get("col.from.z"));
        colFromName.setText(lm.get("col.from.name"));

        colToX.setText(lm.get("col.to.x"));
        colToY.setText(lm.get("col.to.y"));
        colToZ.setText(lm.get("col.to.z"));
        colToName.setText(lm.get("col.to.name"));
        colDistance.setText(lm.get("col.distance"));
        colOwner.setText(lm.get("col.owner"));

        setupFilters();
        applyFilterAndSort();
    }

    private void setStatus(String s) {
        Platform.runLater(() -> lblStatus.setText(s));
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private Stage dialogStage(Parent root, String title) {
        Stage s = new Stage();
        s.initModality(Modality.APPLICATION_MODAL);
        s.setTitle(title);
        s.setScene(new Scene(root));
        return s;
    }
}