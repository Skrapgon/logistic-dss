package com.logistic.logistic_dss.model.rule;

import java.util.Arrays;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class DataPropertyAtom extends Atom {
    private final String dataPropertyName;
    private final Argument subject;
    private final Argument value;

    @Override
    public String getPredicate() {
        return dataPropertyName;
    }

    @Override
    public List<Argument> getArguments() {
        return Arrays.asList(subject, value);
    }
}
