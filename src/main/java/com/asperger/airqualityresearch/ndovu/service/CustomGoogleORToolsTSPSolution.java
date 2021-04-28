package com.asperger.airqualityresearch.ndovu.service;

import com.asperger.airqualityresearch.ndovu.models.Sensor;
import com.asperger.airqualityresearch.ndovu.util.GeometryHelper;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomGoogleORToolsTSPSolution implements OrderSolver {
    private final Sensor closestSensor;
    private final List<Sensor> sensorListOrder;
    private final Point droneStartingPoint;
    private final double[][] distanceMatrix;
    private final Map<String, Integer> mappingKey = new HashMap<>();


    public CustomGoogleORToolsTSPSolution(List<Sensor> sensorListOrder,
                                          Point droneStartingPoint) {
        this.sensorListOrder = new ArrayList<>(sensorListOrder);
        this.droneStartingPoint = droneStartingPoint;
        this.closestSensor = getClosestSensor();
        this.distanceMatrix = createDistanceMatrix();
        findBestVisitationOrder();
    }

    private Map<String, Map<String, Double>> generateDistanceDataMatrix() {
        Map<String, Map<String, Double>> distanceDataMatrix = new HashMap<>();
        int size = sensorListOrder.size();
        for (int i = 0; i < size; i++) {
            Map<String, Double> map = new HashMap<>();
            for (int j = 1; j < size; j++) {
                map.put(sensorListOrder.get((i + j) % size).getLocation(),
                        GeometryHelper.distanceBetweenTwoPoints(sensorListOrder.get(i).getGeoJsonPoint(), sensorListOrder.get((i + j) % size).getGeoJsonPoint()));
            }
            distanceDataMatrix.put(sensorListOrder.get(i).getLocation(), map);
        }
        return distanceDataMatrix;
    }

    private void createMappingKey() {
        for (int i = 0; i < sensorListOrder.size(); i++) {
            this.mappingKey.put(sensorListOrder.get(i).getLocation(), i);
        }
    }

    private Sensor getClosestSensor() {
        double distance = Double.MAX_VALUE;
        Sensor temp = sensorListOrder.get(0);
        for (Sensor sensor : sensorListOrder) {
            double dist =
                    GeometryHelper.distanceBetweenTwoPoints(this.droneStartingPoint, sensor.getGeoJsonPoint());
            if (dist < distance) {
                distance = dist;
                temp = sensor;
            }
        }
        return temp;
    }

    private double[][] createDistanceMatrix() {
        createMappingKey();
        int size = sensorListOrder.size();
        double[][] distanceMatrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                distanceMatrix[i][j] =
                        GeometryHelper.distanceBetweenTwoPoints(sensorListOrder.get(i).getGeoJsonPoint(), sensorListOrder.get(j).getGeoJsonPoint());
            }
        }
        return distanceMatrix;
    }

    @Override
    public List<Sensor> findBestVisitationOrder() {
        Loader.loadNativeLibraries();
        RoutingIndexManager manager =
                new RoutingIndexManager(distanceMatrix.length, 1,
                        mappingKey.get(closestSensor.getLocation()));
        RoutingModel routing = new RoutingModel(manager);

        final int transitCallbackIndex =
                routing.registerTransitCallback((long fromIndex,
                                                 long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return Math.round(distanceMatrix[fromNode][toNode] * 100000000);
        });
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);
        RoutingSearchParameters searchParameters =
                main.defaultRoutingSearchParameters().toBuilder().setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC).build();
        Assignment solution = routing.solveWithParameters(searchParameters);
        List<Sensor> visitationOrder = new ArrayList<>();
        long index = routing.start(0);
        while (!routing.isEnd(index)) {
            visitationOrder.add(sensorListOrder.get(manager.indexToNode(index)));
            index = solution.value(routing.nextVar(index));
        }
        return visitationOrder;
    }


}
