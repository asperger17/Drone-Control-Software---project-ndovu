package com.asperger.airqualityresearch.ndovu.models;

import com.asperger.airqualityresearch.ndovu.util.What3WordsConverter;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class Sensor extends BaseEntity {

    @Getter
    private final String location;
    @Getter
    private final Point geoJsonPoint;
    @Getter
    @Setter
    private float battery;
    @Setter
    private float reading;

    public Sensor(String location) {
        super();
        this.location = location;
        this.geoJsonPoint =
                What3WordsConverter.fromStringLocationToPoint(location);

    }

    private String getSensorMarkerSymbol() {
        if (this.isMalfunctioning()) {
            return "cross";
        } else if (this.reading < 128 && this.reading >= 0) {
            return "lighthouse";
        } else if (this.reading >= 128 && this.reading < 256) {
            return "cross";
        } else {
            return "";
        }
    }

    private String getSensorMarkerColorInfo() {
        if (this.needsNewBattery()) {
            return "#000000";
        } else if (this.reading >= 0 && this.reading < 32) {
            return "#00ff00";
        } else if (this.reading >= 32 && this.reading < 64) {
            return "#40ff00";
        } else if (this.reading >= 64 && this.reading < 96) {
            return "#80ff00";
        } else if (this.reading >= 96 && this.reading < 128) {
            return "#c0ff00";
        } else if (this.reading >= 128 && this.reading < 160) {
            return "#ffc000";
        } else if (this.reading >= 160 && this.reading < 192) {
            return "#ff8000";
        } else if (this.reading >= 192 && this.reading < 224) {
            return "#ff4000";
        } else if (this.reading >= 224 && this.reading < 256) {
            return "ff0000";
        } else {
            return "#aaaaaa";
        }
    }

    private boolean isMalfunctioning() {
        return this.needsNewBattery() || Float.isNaN(this.reading);
    }

    private boolean needsNewBattery() {
        return this.battery < 10;
    }

    public float getSensorReading() {
        if (this.isMalfunctioning()) {
            throw new IllegalStateException("sensor is malfunctioning");
        }
        return this.reading;
    }

    public Feature convertToFeature() {
        Feature feature = Feature.fromGeometry(this.geoJsonPoint);
        feature.addStringProperty("marker-size", "medium");
        feature.addStringProperty("location", this.location);
        feature.addStringProperty("marker-symbol",
                this.getSensorMarkerSymbol());
        feature.addStringProperty("rgb-string", getSensorMarkerColorInfo());
        feature.addStringProperty("marker-color", getSensorMarkerColorInfo());
//        feature.addStringProperty("id", this.getId().toString());
        return feature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sensor)) return false;
        Sensor sensor = (Sensor) o;
        return getLocation().equals(sensor.getLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLocation());
    }

    @Override
    public String toString() {
        return "Sensor{" + "location='" + location + '\'' + ", battery=" + battery + ", reading=" + reading + " id= " + getId() + '}';
    }
}
