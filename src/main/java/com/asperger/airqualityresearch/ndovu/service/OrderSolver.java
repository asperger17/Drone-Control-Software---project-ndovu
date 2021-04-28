package com.asperger.airqualityresearch.ndovu.service;

import com.asperger.airqualityresearch.ndovu.models.Sensor;

import java.util.List;

public interface OrderSolver {

    List<Sensor> findBestVisitationOrder();

}
