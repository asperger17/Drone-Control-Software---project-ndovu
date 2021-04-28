package com.asperger.airqualityresearch.ndovu.models;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NoFlyZone {

    private final List<Path> noFlyZoneSegments;
    private final Feature noFlyZone;

    public NoFlyZone(Feature noFlyZone) {
        this.noFlyZone = noFlyZone;
        noFlyZoneSegments = this.generatePathSegments();
    }

    private List<Path> generatePathSegments() {
        List<Path> pathContainer = new ArrayList<>();
        List<Point> coordinates =
                ((Polygon) Objects.requireNonNull(noFlyZone.geometry())).coordinates().get(0);
        for (int i = 0; i < coordinates.size() - 1; i++) {
            pathContainer.add(new Path(coordinates.get(i),
                    coordinates.get(i + 1)));
        }
        return pathContainer;
    }

    public boolean passesThroughNoFlyZone(Path path) {
        return this.noFlyZoneSegments.stream().map(x -> x.pathIntersectsAnotherPath(path)).reduce(Boolean::logicalOr).orElse(false);
    }

    public Feature getFeature() {
        return noFlyZone;
    }
}
