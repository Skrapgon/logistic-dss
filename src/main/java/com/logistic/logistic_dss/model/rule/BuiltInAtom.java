package com.logistic.logistic_dss.model.rule;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class BuiltInAtom extends Atom {
    private final String builtInFunction;
    private final List<Argument> arguments;

    @Override
    public String getPredicate() {
        return builtInFunction;
    }

    @Override
    public List<Argument> getArguments() {
        return arguments;
    }
}
