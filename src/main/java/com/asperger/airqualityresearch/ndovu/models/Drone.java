package com.asperger.airqualityresearch.ndovu.models;

import com.asperger.airqualityresearch.ndovu.service.FlightService;
import com.asperger.airqualityresearch.ndovu.service.NoFlyZoneService;
import com.asperger.airqualityresearch.ndovu.service.SensorService;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class Drone {

    private final List<DroneMove> flightPlan;
    @Getter
    private final List<Sensor> dailySensors;
    @Getter
    private final List<NoFlyZone> noFlyZones;

    public Drone(Point startingPoint, String year, String month, String date,
                 int port) {
        this.dailySensors = SensorService.getDailySensors(date, month, year,
                port);
        this.noFlyZones = NoFlyZoneService.getNoFlyZones(port);
        FlightService flightService = new FlightService(dailySensors,
                startingPoint, noFlyZones);
        this.flightPlan = flightService.getFlightPathPlan();
    }

    public List<Feature> convertFlightPlanToFeatures() {
        return flightPlan.stream().map(DroneMove::convertToFeature).collect(Collectors.toList());
    }

    public String getFlightPlan() {
        StringBuilder plan = new StringBuilder();
        for (int i = 0; i < flightPlan.size(); i++) {
            plan.append(i).append(",").append(flightPlan.get(i).printToFile()).append("\n");
        }
        return plan.toString();
    }
}
