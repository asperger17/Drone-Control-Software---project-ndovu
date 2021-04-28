package com.asperger.airqualityresearch.ndovu.util;

import com.asperger.airqualityresearch.ndovu.models.Path;
import com.mapbox.geojson.Point;

import java.awt.geom.Line2D;
import java.util.*;

public class GeometryHelper {

    public static double distanceBetweenTwoPoints(Point pointA, Point pointB) {
        double xDelta = pointB.latitude() - pointA.latitude();
        double x2 = Math.pow(xDelta, 2);
        double yDelta = pointB.longitude() - pointA.longitude();
        double y2 = Math.pow(yDelta, 2);
        return Math.sqrt(x2 + y2);
    }

    public static double angleBetweenTwoPoints(Point from, Point to) {
        double angle =
                Math.toDegrees(Math.atan2(to.latitude() - from.latitude(),
                        to.longitude() - from.longitude()));
        if (angle < 0) {
            angle = angle + 360.0;
        }
        return angle;
    }

    public static boolean pathsIntersect(Path path1, Path path2) {
        return Line2D.linesIntersect(path1.getFrom().latitude(),
                path1.getFrom().longitude(), path1.getTo().latitude(),
                path1.getTo().longitude(), path2.getFrom().latitude(),
                path2.getFrom().longitude(), path2.getTo().latitude(),
                path2.getTo().longitude());
    }

    private static Map<String, Double> getLineAndCircleVariables(Path path,
                                                                 Point center
            , double radius) {
        Map<String, Double> variablesMap = new HashMap<>();

        double baX = path.getTo().longitude() - path.getFrom().longitude();
        double baY = path.getTo().latitude() - path.getFrom().latitude();
        double caX = center.longitude() - path.getFrom().longitude();
        double caY = center.latitude() - path.getFrom().latitude();

        variablesMap.put("baX", baX);
        variablesMap.put("baY", baY);
        variablesMap.put("caX", caX);
        variablesMap.put("caY", caY);

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - radius * radius;

        variablesMap.put("a", a);
        variablesMap.put("bBy2", bBy2);
        variablesMap.put("c", c);

        double pBy2 = bBy2 / a;
        double q = c / a;

        variablesMap.put("pBy2", pBy2);
        variablesMap.put("q", q);

        double disc = pBy2 * pBy2 - q;
        variablesMap.put("disc", disc);

        return variablesMap;
    }


    public static boolean pathIntersectsCircle(Path path, Point center,
                                               double radius) {
        Map<String, Double> variablesMap = getLineAndCircleVariables(path,
                center, radius);
        double disc = variablesMap.get("disc");
        return disc >= 0;
    }

    public static List<Point> getPointsOfIntersectionOfPathAndCircle(Path path, Point center, double radius) {
        Map<String, Double> variablesMap = getLineAndCircleVariables(path,
                center, radius);
        double disc = variablesMap.get("disc");
        double pBy2 = variablesMap.get("pBy2");
        double baX = variablesMap.get("baX");
        double baY = variablesMap.get("baY");

        if (disc < 0) {
            return Collections.emptyList();
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;

        Point p1 =
                Point.fromLngLat(path.getFrom().longitude() - baX * abScalingFactor1, path.getFrom().latitude() - baY * abScalingFactor1);
        if (disc == 0) { // abScalingFactor1 == abScalingFactor2
            return Collections.singletonList(p1);
        }
        Point p2 =
                Point.fromLngLat(path.getFrom().longitude() - baX * abScalingFactor2, path.getFrom().latitude() - baY * abScalingFactor2);
        return Arrays.asList(p1, p2);
    }

    public static Point findNewPoint(Point from, double angle, double maxDist) {
        return Point.fromLngLat(from.longitude() + maxDist * Math.cos(Math.toRadians(angle)), from.latitude() + maxDist * Math.sin(Math.toRadians(angle)));
    }
}
