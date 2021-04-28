package com.asperger.airqualityresearch.ndovu.models;

import com.asperger.airqualityresearch.ndovu.util.GeometryHelper;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import lombok.Getter;
import lombok.Setter;

public class DroneMove extends Path {

    private final boolean isNotValid;
    private final int angle;
    @Getter
    @Setter
    private String location;

    public DroneMove(Point from, Point to, String location,
                     boolean isNotValid) {
        super(from, to);
        this.location = location;
        this.isNotValid = isNotValid;
        this.angle =
                (int) Math.round(GeometryHelper.angleBetweenTwoPoints(from,
                        to));
    }

    @Override
    public String toString() {
        return "DroneMove{" + " '" + getFrom().toString() + " '" + getTo().toString() + " Sensor : " + getLocation() + "}\n";
    }

    @Override
    public Feature convertToFeature() {
        Feature feature = super.convertToFeature();
        if (isNotValid) {
            feature.addNumberProperty("stroke-width", 6);
        }
        return feature;
    }

    public String printToFile() {
        return String.join(",", String.valueOf(getFrom().longitude()),
                String.valueOf(getFrom().latitude()), String.valueOf(angle),
                String.valueOf(getTo().longitude()),
                String.valueOf(getTo().latitude()), location);
    }
}
