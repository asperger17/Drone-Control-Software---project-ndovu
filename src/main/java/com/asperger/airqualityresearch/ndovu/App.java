package com.asperger.airqualityresearch.ndovu;

import com.asperger.airqualityresearch.ndovu.models.Drone;
import com.asperger.airqualityresearch.ndovu.models.NoFlyZone;
import com.asperger.airqualityresearch.ndovu.models.Sensor;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        String date = args[0];
        String month = args[1];
        String year = args[2];
        int port = Integer.parseInt(args[6]);
        Drone drone = new Drone(Point.fromLngLat(Double.parseDouble(args[4]),
                Double.parseDouble(args[3])), year, month, date, port);
        int seed = Integer.parseInt(args[5]);
        List<Feature> features = drone.convertFlightPlanToFeatures();
        features.addAll(drone.getDailySensors().stream().map(Sensor::convertToFeature).collect(Collectors.toList()));
        features.addAll(drone.getNoFlyZones().stream().map(NoFlyZone::getFeature).collect(Collectors.toList()));
        FeatureCollection map = FeatureCollection.fromFeatures(features);

        String droneMovesFileName =
                "ilp-results/flightpath/flightpath-" + String.join("-", date,
                month, year) + ".txt";
        String readingsGeoJsonFileName =
                "ilp-results/reading/readings-" + String.join("-", date,
                month, year) + ".txt";

        writeFile(droneMovesFileName, drone.getFlightPlan());
        writeFile(readingsGeoJsonFileName, map.toJson());

    }

    public static void writeFile(String fileName, String content) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error occurred while writing output file!");
            e.printStackTrace();
        }
    }
}
