package com.logistic.logistic_dss.model.rule;

import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class ClassAtom extends Atom {
    private final String className;
    private final Argument variable;

    @Override
    public String getPredicate() {
        return className;
    }

    @Override
    public List<Argument> getArguments() {
        return Collections.singletonList(variable);
    }
}