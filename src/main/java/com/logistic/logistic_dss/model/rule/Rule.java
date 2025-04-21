package com.logistic.logistic_dss.model.rule;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Rule {
    private String ID;
    private List<Atom> antecedent;
    private List<Atom> consequent;
}