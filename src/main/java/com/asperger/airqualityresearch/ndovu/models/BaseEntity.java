package com.asperger.airqualityresearch.ndovu.models;

import lombok.Getter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

public class BaseEntity {

    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

}
