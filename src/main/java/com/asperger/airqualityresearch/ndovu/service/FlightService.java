package com.asperger.airqualityresearch.ndovu.service;

import com.asperger.airqualityresearch.ndovu.models.DroneMove;
import com.asperger.airqualityresearch.ndovu.models.NoFlyZone;
import com.asperger.airqualityresearch.ndovu.models.Sensor;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

public class FlightService {

    private final List<Sensor> sensors;
    private final List<NoFlyZone> noFlyZones;
    private Point currPosition;

    public FlightService(List<Sensor> sensors, Point currPosition,
                         List<NoFlyZone> noFlyZones) {
        this.sensors = sensors;
        this.currPosition = currPosition;
        this.noFlyZones = noFlyZones;
    }

    public List<DroneMove> getFlightPathPlan() {
        /*
        //  TSP implementation of the Simulated Annealing solution
        CustomTSPAnnealingSolution tsp =
                new CustomTSPAnnealingSolution(sensors, currPosition);
        List<Sensor> sensorVisitationOrder = tsp.findBestVisitationOrder();
        */

        /* TSP implementation of the Google OR Tools solution using
           graph algorithms */
        OrderSolver orderSolver = new CustomGoogleORToolsTSPSolution(sensors,
                currPosition);
        List<Sensor> sensorVisitationOrder =
                orderSolver.findBestVisitationOrder();

        List<DroneMove> droneMoves = new ArrayList<>();
        int index = 0;
        while (droneMoves.isEmpty() || !droneMoves.get(droneMoves.size() - 1).getLocation().equals(sensorVisitationOrder.get(sensorVisitationOrder.size() - 1).getLocation())) {
            Sensor currSensor = sensorVisitationOrder.get(index);
            FlightPathPlanner fpp = new FlightPathPlanner(currPosition,
                    currSensor.getLocation(), noFlyZones);
            fpp.planPathToSensor();
            droneMoves.addAll(fpp.getPathPlan());
            currPosition =
                    fpp.getPathPlan().get(fpp.getPathPlan().size() - 1).getTo();
            if (!droneMoves.get(droneMoves.size() - 1).getLocation().equals(
                    "null")) {
                index++;
            }
        }
        return droneMoves;
    }


}
