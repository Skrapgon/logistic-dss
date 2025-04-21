package com.logistic.logistic_dss.model.rule;

import java.util.List;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class Atom {
    public abstract String getPredicate();
    public abstract List<Argument> getArguments();
}