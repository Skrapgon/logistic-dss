package com.logistic.logistic_dss.model;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Ontology {
    private String ID;
    private String name;
    private String description;
    private  Set<String> classesProperties;
}