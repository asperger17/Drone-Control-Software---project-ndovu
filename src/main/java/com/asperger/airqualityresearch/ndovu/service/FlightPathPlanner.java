package com.asperger.airqualityresearch.ndovu.service;

import com.asperger.airqualityresearch.ndovu.models.DroneMove;
import com.asperger.airqualityresearch.ndovu.models.NoFlyZone;
import com.asperger.airqualityresearch.ndovu.models.Path;
import com.asperger.airqualityresearch.ndovu.util.GeometryHelper;
import com.asperger.airqualityresearch.ndovu.util.What3WordsConverter;
import com.mapbox.geojson.Point;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FlightPathPlanner {

    private final double MAX_DRONE_TRAVEL_DISTANCE = 0.0003;
    private final double EPSILON = 0.00001;
    private final double RADIUS = 0.0002;
    private final String sensorPositionString;
    private final Point sensorPosition;
    @Getter
    private final List<DroneMove> pathPlan = new ArrayList<>();
    private final List<NoFlyZone> noFlyZones;
    private Point droneCurrentPosition;

    public FlightPathPlanner(Point droneCurrentPosition,
                             String sensorPositionString,
                             List<NoFlyZone> noFlyZones) {
        this.sensorPositionString = sensorPositionString;
        this.droneCurrentPosition = droneCurrentPosition;
        this.noFlyZones = noFlyZones;
        this.sensorPosition =
                What3WordsConverter.fromStringLocationToPoint(sensorPositionString);
    }

    private boolean isNotValidPath(Path path) {
        return noFlyZones.stream().map(x -> x.passesThroughNoFlyZone(path)).reduce(Boolean::logicalOr).orElse(true);
    }

    private Path getValidPath(Path invalidPath, double angle) {
        Path tempPath = new Path(invalidPath.getFrom(), invalidPath.getTo());
        int i = 2;
        boolean negative = true;
        double tempAngle;
        while (isNotValidPath(tempPath) || pathPlan.contains(tempPath)) {
            int delta =10 * Math.round(i / 2);
            tempAngle = angle + (negative ? -delta : delta);
            // move MAX_DIST in the given angle and generate path
            Point tempPoint =
                    GeometryHelper.findNewPoint(droneCurrentPosition,
                            tempAngle, MAX_DRONE_TRAVEL_DISTANCE);
            tempPath = new Path(invalidPath.getFrom(), tempPoint);
            negative = !negative;
            i++;
        }
        return tempPath;
    }

    private DroneMove generateValidPathFromPosition() {
        // find angle to sensor
        double angle =
                Math.round((GeometryHelper.angleBetweenTwoPoints(droneCurrentPosition, sensorPosition)) / 10.0) * 10;
        // move MAX_DIST in the given angle and generate path
        Point end = GeometryHelper.findNewPoint(droneCurrentPosition, angle,
                MAX_DRONE_TRAVEL_DISTANCE);
        Path miniPath = new Path(droneCurrentPosition, end);
        if (isNotValidPath(miniPath)) {
            miniPath = getValidPath(miniPath, angle);
            droneCurrentPosition = miniPath.getTo();
            return new DroneMove(miniPath.getFrom(), miniPath.getTo(),
                    miniPath.pathIntersectsCircle(sensorPosition, RADIUS) ?
                            sensorPositionString : "null", false);
        }
        // find dist between drone and sensor
        double dist =
                GeometryHelper.distanceBetweenTwoPoints(droneCurrentPosition,
                        sensorPosition);
        // if drone within radius, move to sensor and take the reading
        if (dist < RADIUS) {
            return (new DroneMove(droneCurrentPosition, sensorPosition,
                    sensorPositionString,
                    isNotValidPath(new Path(droneCurrentPosition,
                            sensorPosition))));
            // otherwise if drone is one move away to the sensor
        } else if (dist < RADIUS + MAX_DRONE_TRAVEL_DISTANCE - EPSILON) {
            // check if adjusted path is within sensor
            if (miniPath.pathIntersectsCircle(sensorPosition, RADIUS)) {
                // if it is, find point within drone radius and return path
                List<Point> intersectionPoints =
                        GeometryHelper.getPointsOfIntersectionOfPathAndCircle(miniPath, sensorPosition, RADIUS - EPSILON);
                List<Path> pathSegments =
                        intersectionPoints.stream().map(x -> new Path(droneCurrentPosition, x)).collect(Collectors.toList());
                miniPath = Collections.min(pathSegments,
                        Comparator.comparing(Path::getDistance));
                droneCurrentPosition = miniPath.getTo();
                return (new DroneMove(miniPath.getFrom(), miniPath.getTo(),
                        sensorPositionString, isNotValidPath(miniPath)));
                // if not; MiniPath does not intersect Circle (RARE CASE OMLY HAPPENS IF RADIUS IS TOO SMALL)
            } else {
                // loop while adjusting angle till you get a valid path
                int i = 1;
                boolean negative = true;
                while (miniPath.pathIntersectsCircle(sensorPosition, RADIUS) || isNotValidPath(miniPath)) {
                    double tempAngle = angle + (negative ? (-10 * i) :
                            (10 * i));
                    // move MAX_DIST in the given angle and generate path
                    Point tempPoint =
                            GeometryHelper.findNewPoint(droneCurrentPosition,
                                    tempAngle, MAX_DRONE_TRAVEL_DISTANCE);
                    miniPath = new Path(droneCurrentPosition, tempPoint);
                    negative = !negative;
                    i++;
                }
                droneCurrentPosition = miniPath.getTo();
                return (new DroneMove(miniPath.getFrom(), miniPath.getTo(),
                        sensorPositionString, isNotValidPath(miniPath)));
            }
            // Otherwise if the drone is far away move once in the general
            // direction
        } else {
            droneCurrentPosition = miniPath.getTo();
            return (new DroneMove(miniPath.getFrom(), miniPath.getTo(), "null"
                    , isNotValidPath(miniPath)));
        }
    }

    public void planPathToSensor() {
        int i = 0;
        while (pathPlan.isEmpty() || pathPlan.get(pathPlan.size() - 1).getLocation().equals("null")) {
            DroneMove move = generateValidPathFromPosition();
            pathPlan.add(move);
            i++;
        }
    }
}
