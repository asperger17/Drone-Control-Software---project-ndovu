package com.asperger.airqualityresearch.ndovu.service;

import com.asperger.airqualityresearch.ndovu.models.Sensor;
import com.asperger.airqualityresearch.ndovu.util.GeometryHelper;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomTSPAnnealingSolution implements OrderSolver {

    private final int ITERATIONS = 1000000000;
    private final double coolingRate = 0.9999999995;
    private final Point droneStartingPoint;
    private List<Sensor> bestSensorOrder;
    private Sensor closestSensor;
    private List<Sensor> sensorListOrder;
    private List<Sensor> previousSensorListOrder;
    private double distance;
    private double temp;

    public CustomTSPAnnealingSolution(List<Sensor> sensorListOrder,
                                      Point droneStartingPoint) {
        this.sensorListOrder = new ArrayList<>(sensorListOrder);
        this.droneStartingPoint = droneStartingPoint;
        getClosestSensor();
        this.distance = this.calculateDistance();
        this.temp = 1000;
    }

    private void swapSensorOrder() {
        int a = new Random().nextInt(sensorListOrder.size());
        int b = new Random().nextInt(sensorListOrder.size());
        previousSensorListOrder = new ArrayList<>(sensorListOrder);
        Sensor temp = sensorListOrder.get(a);
        sensorListOrder.set(a, sensorListOrder.get(b));
        sensorListOrder.set(b, temp);
    }

    private void revertSwaps() {
        this.sensorListOrder = new ArrayList<>(previousSensorListOrder);
    }

    private double calculateDistance() {
        double distance = 0;
        for (int i = 0; i < sensorListOrder.size(); i++) {
            Sensor start = sensorListOrder.get(i);
            Sensor end;
            if (i + 1 < sensorListOrder.size()) {
                end = sensorListOrder.get(i + 1);
            } else {
                end = sensorListOrder.get(0);
            }
            distance += GeometryHelper.distanceBetweenTwoPoints(start.getGeoJsonPoint(), end.getGeoJsonPoint());
        }
        return distance;
    }

    private void simulateAnnealing() {
        for (int i = 0; i < ITERATIONS; i++) {
            if (temp > 0.1) {
                this.swapSensorOrder();
                double currentDistance = this.calculateDistance();
                if (currentDistance < this.distance) {
                    this.distance = currentDistance;
                    this.bestSensorOrder = List.copyOf(sensorListOrder);
                } else if (Math.exp((distance - currentDistance) / temp) < Math.random()) {
                    this.revertSwaps();
                }
                temp *= coolingRate;
            } else {
                continue;
            }
            if (i % 100 == 0) {
                System.out.println("Simulation #" + i);
            }
        }
    }

    private void getClosestSensor() {
        double distance = Double.MAX_VALUE;
        Sensor temp = sensorListOrder.get(0);
        for (Sensor sensor : sensorListOrder) {
            double dist =
                    GeometryHelper.angleBetweenTwoPoints(this.droneStartingPoint, sensor.getGeoJsonPoint());
            if (dist < distance) {
                distance = dist;
                temp = sensor;
            }
        }
        this.closestSensor = temp;
        System.out.println("CLOSEST: " + temp.toString());
    }

    @Override
    public List<Sensor> findBestVisitationOrder() {
        this.simulateAnnealing();
        int breakIndex = this.bestSensorOrder.indexOf(this.closestSensor);
        List<Sensor> finalOrder =
                new ArrayList<>(bestSensorOrder.subList(breakIndex,
                        bestSensorOrder.size()));
        finalOrder.addAll(bestSensorOrder.subList(0, breakIndex));
        System.out.println(finalOrder);
        return finalOrder;
    }
}
