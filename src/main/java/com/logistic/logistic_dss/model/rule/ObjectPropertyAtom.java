package com.logistic.logistic_dss.model.rule;

import java.util.Arrays;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class ObjectPropertyAtom extends Atom {
    private final String objectPropertyName;
    private final Argument subject;
    private final Argument object;

    @Override
    public String getPredicate() {
        return objectPropertyName;
    }

    @Override
    public List<Argument> getArguments() {
        return Arrays.asList(subject, object);
    }
}
