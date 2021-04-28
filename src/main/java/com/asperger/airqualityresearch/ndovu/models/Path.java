package com.asperger.airqualityresearch.ndovu.models;

import com.asperger.airqualityresearch.ndovu.util.GeometryHelper;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

public class Path extends BaseEntity {
    @Getter
    private final Point from;
    @Getter
    private final Point to;
    @Getter
    private final double distance;

    public Path(Point from, Point to) {
        this.from = from;
        this.to = to;
        this.distance = GeometryHelper.distanceBetweenTwoPoints(to, from);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path)) return false;
        Path path = (Path) o;
        return (getFrom().equals(path.getFrom()) && getTo().equals(path.getTo())) ||
                (getFrom().equals(path.getTo()) && getTo().equals(path.getFrom()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFrom(), getTo());
    }

    public boolean pathIntersectsAnotherPath(Path otherPath) {
        return GeometryHelper.pathsIntersect(this, otherPath);
    }

    public boolean pathIntersectsCircle(Point center, double radius) {
        return GeometryHelper.pathIntersectsCircle(this, center, radius);
    }

    public Feature convertToFeature() {
        Feature feature =
                Feature.fromGeometry(LineString.fromLngLats(Arrays.asList(this.from, this.to)));
        feature.addStringProperty("from", from.coordinates().toString());
        feature.addStringProperty("to", to.coordinates().toString());
        return feature;
    }

}
