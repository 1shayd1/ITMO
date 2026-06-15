package org.shayd1.gui.models;

import javafx.beans.property.*;
import org.shayd1.gui.localization.LocaleManager;
import org.shayd1.model.Route;

public class RouteTableItem {

    private final IntegerProperty key          = new SimpleIntegerProperty();
    private final StringProperty  name         = new SimpleStringProperty("");
    private final FloatProperty   coordX       = new SimpleFloatProperty();
    private final FloatProperty   coordY       = new SimpleFloatProperty();
    private final StringProperty  creationDate = new SimpleStringProperty("");
    private final ObjectProperty<Long>    fromX    = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> fromY    = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> fromZ    = new SimpleObjectProperty<>(null);
    private final StringProperty  fromName     = new SimpleStringProperty("");
    private final ObjectProperty<Double>  toX      = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Float>   toY      = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> toZ      = new SimpleObjectProperty<>(null);
    private final StringProperty  toName       = new SimpleStringProperty("");
    private final FloatProperty   distance     = new SimpleFloatProperty();
    private final LongProperty    ownerId      = new SimpleLongProperty();

    private final Route route;

    public RouteTableItem(int key, Route route) {
        this.route = route;
        this.key.set(key);
        this.name.set(route.getName() != null ? route.getName() : "");

        if (route.getCoordinates() != null) {
            this.coordX.set(route.getCoordinates().getX());
            this.coordY.set(route.getCoordinates().getY());
        }

        LocaleManager.getInstance().localeProperty().addListener((obs, oldVal, newVal) -> {
            updateFormattedDate();
        });

        if (route.getFrom() != null) {
            this.fromX.set((long) route.getFrom().getX());
            this.fromY.set((int) route.getFrom().getIntY());
            this.fromZ.set(route.getFrom().getIntZ());
        } else {
            this.fromX.set(null);
            this.fromY.set(null);
            this.fromZ.set(null);
        }

        if (route.getTo() != null) {
            this.toX.set(route.getTo().getX());
            this.toY.set((float) route.getTo().getIntY());
            this.toZ.set(route.getTo().getIntZ());
        } else {
            this.toX.set(null);
            this.toY.set(null);
            this.toZ.set(null);
        }

        this.distance.set(route.getDistance());
        this.ownerId.set(route.getOwnerId());
        updateFormattedDate();
    }

    public StringProperty  creationDateProperty() { return creationDate; }

    public int    getKey()      { return key.get(); }
    public String getName()     { return name.get(); }
    public float  getCoordX()   { return coordX.get(); }
    public float  getCoordY()   { return coordY.get(); }

    public Long    getFromX()    { return fromX.get(); }
    public Integer getFromY()    { return fromY.get(); }
    public Integer getFromZ()    { return fromZ.get(); }
    public String  getFromName() { return fromName.get(); }

    public Double  getToX()      { return toX.get(); }
    public Float   getToY()      { return toY.get(); }
    public Integer getToZ()      { return toZ.get(); }
    public String  getToName()   { return toName.get(); }

    public float  getDistance() { return distance.get(); }
    public long   getOwnerId()  { return ownerId.get(); }
    public Route  getRoute()    { return route; }

    private void updateFormattedDate() {
        var formatter = LocaleManager.getInstance().dateFormatter();
        if (route.getCreationDate() != null) {
            String formatted = route.getCreationDate().format(formatter);
            this.creationDate.set(formatted);
        }
    }
}