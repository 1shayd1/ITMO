package org.shayd1.gui.controllers;

import org.shayd1.gui.localization.LocaleManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.shayd1.model.Coordinates;
import org.shayd1.model.Location;
import org.shayd1.model.Route;

import java.time.LocalDate;

public class EditDialogController {

    @FXML private Label     lblTitle;
    @FXML private Label     lblKey;
    @FXML private Label     lblFromName;
    @FXML private Label     lblCoords;
    @FXML private Label     lblFrom;
    @FXML private Label     lblTo;
    @FXML private Label     lblDistance;

    @FXML private TextField tfKey;
    @FXML private TextField tfName;
    @FXML private TextField tfCoordX;
    @FXML private TextField tfCoordY;
    @FXML private TextField tfFromX;
    @FXML private TextField tfFromY;
    @FXML private TextField tfFromZ;
    @FXML private TextField tfFromName;
    @FXML private TextField tfToX;
    @FXML private TextField tfToY;
    @FXML private TextField tfToZ;
    @FXML private TextField tfToName;
    @FXML private TextField tfDistance;

    @FXML private Button    btnSave;
    @FXML private Button    btnCancel;
    @FXML private Label     lblError;

    private final LocaleManager lm = LocaleManager.getInstance();

    private boolean editMode = false;
    private int     fixedKey = -1;

    private Route  resultRoute;
    private int    resultKey;
    private boolean saved = false;

    @FXML
    public void initialize() {
        applyLocale();
        btnSave.setOnAction(e -> onSave());
        btnCancel.setOnAction(e -> close());
    }

    public void setForEdit(int key, Route route) {
        editMode = true;
        fixedKey = key;
        lblTitle.setText(lm.get("dialog.edit.title"));
        tfKey.setText(String.valueOf(key));
        tfKey.setDisable(true);
        if (route != null) fill(route);
    }

    public void setForAdd() {
        editMode = false;
        lblTitle.setText(lm.get("dialog.add.title"));
        tfKey.setDisable(false);
    }

    public boolean isSaved()    { return saved; }
    public Route  getRoute()    { return resultRoute; }
    public int    getKey()      { return resultKey; }

    private void fill(Route r) {
        tfName.setText(r.getName() != null ? r.getName() : "");
        if (r.getCoordinates() != null) {
            tfCoordX.setText(String.valueOf(r.getCoordinates().getX()));
            tfCoordY.setText(String.valueOf(r.getCoordinates().getY()));
        }
        tfDistance.setText(String.valueOf(r.getDistance()));
        if (r.getFrom() != null) {
            tfFromX.setText(String.valueOf(r.getFrom().getX()));
            tfFromY.setText(String.valueOf(r.getFrom().getIntY()));
            if (r.getFrom().getIntZ() != null) tfFromZ.setText(String.valueOf(r.getFrom().getIntZ()));
            if (r.getFrom().getName() != null)  tfFromName.setText(r.getFrom().getName());
        }
        if (r.getTo() != null) {
            tfToX.setText(String.valueOf(r.getTo().getX()));
            tfToY.setText(String.valueOf(r.getTo().getIntY()));
            if (r.getTo().getIntZ() != null) tfToZ.setText(String.valueOf(r.getTo().getIntZ()));
            if (r.getTo().getName() != null)  tfToName.setText(r.getTo().getName());
        }
    }

    private void onSave() {
        lblError.setText("");
        try {
            int key;
            if (editMode) {
                key = fixedKey;
            } else {
                if (tfKey.getText().isBlank()) { lblError.setText(lm.get("dialog.error.key")); return; }
                key = Integer.parseInt(tfKey.getText().trim());
            }

            if (tfName.getText().isBlank()) { lblError.setText(lm.get("dialog.error.name")); return; }

            if (tfCoordX.getText().isBlank() || tfCoordY.getText().isBlank()) {
                lblError.setText(lm.get("dialog.error.coords")); return;
            }
            float cX = Float.parseFloat(tfCoordX.getText().trim());
            float cY = Float.parseFloat(tfCoordY.getText().trim());

            if (tfDistance.getText().isBlank()) { lblError.setText(lm.get("dialog.error.distance")); return; }
            float distance = Float.parseFloat(tfDistance.getText().trim());
            if (distance <= 0) { lblError.setText(lm.get("dialog.error.distance")); return; }

            Location from = null;
            if (!tfFromX.getText().isBlank() || !tfFromY.getText().isBlank()) {
                double fx = Double.parseDouble(tfFromX.getText().trim());
                double fy = Double.parseDouble(tfFromY.getText().trim());
                Integer fz = tfFromZ.getText().isBlank() ? null : Integer.parseInt(tfFromZ.getText().trim());
                String  fn = tfFromName.getText().isBlank() ? null : tfFromName.getText().trim();
                from = new Location(fx, fy, fz == null ? 0 : fz);
                if (fn != null) from.setName(fn);
            }

            Location to = null;
            if (!tfToX.getText().isBlank() || !tfToY.getText().isBlank()) {
                double tx = Double.parseDouble(tfToX.getText().trim());
                double ty = Double.parseDouble(tfToY.getText().trim());
                Integer tz = tfToZ.getText().isBlank() ? null : Integer.parseInt(tfToZ.getText().trim());
                String  tn = tfToName.getText().isBlank() ? null : tfToName.getText().trim();
                to = new Location(tx, ty, tz == null ? 0 : tz);
                if (tn != null) to.setName(tn);
            }

            Route r = new Route();
            r.setName(tfName.getText().trim());
            r.setCoordinates(new Coordinates(cX, cY));
            r.setDistance(distance);
            r.setFrom(from);
            r.setTo(to);
            r.setCreationDate(LocalDate.now());

            resultRoute = r;
            resultKey   = key;
            saved       = true;
            close();

        } catch (NumberFormatException ex) {
            lblError.setText(lm.get("dialog.error.coords"));
        }
    }

    private void applyLocale() {
        btnSave.setText(lm.get("dialog.btn.save"));
        btnCancel.setText(lm.get("dialog.btn.cancel"));
        if (lblKey != null)       lblKey.setText(lm.get("col.key") + ":");
        if (lblFromName != null)  lblFromName.setText(lm.get("col.name") + ":");
        if (lblCoords != null)    lblCoords.setText(lm.get("dialog.label.coords") + ":");
        if (lblFrom != null)      lblFrom.setText(lm.get("col.from") + ":");
        if (lblTo != null)        lblTo.setText(lm.get("col.to") + ":");
        if (lblDistance != null)  lblDistance.setText(lm.get("col.distance") + ":");
    }

    private void close() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
}