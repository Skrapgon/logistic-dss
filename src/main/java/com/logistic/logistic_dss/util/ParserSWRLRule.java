package com.logistic.logistic_dss.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;

import com.logistic.logistic_dss.model.rule.Argument;
import com.logistic.logistic_dss.model.rule.Atom;
import com.logistic.logistic_dss.model.rule.BuiltInAtom;
import com.logistic.logistic_dss.model.rule.ClassAtom;
import com.logistic.logistic_dss.model.rule.DataPropertyAtom;
import com.logistic.logistic_dss.model.rule.ObjectPropertyAtom;
import com.logistic.logistic_dss.model.rule.Rule;

import openllet.owlapi.SWRL;

public class ParserSWRLRule {
    public static Set<SWRLAtom> getSWRLAtomsSet(List<Atom> atoms, OWLOntology o) throws OWLException {
        OWLOntologyManager om = o.getOWLOntologyManager();
        OWLDataFactory factory = om.getOWLDataFactory();
        String ontologyIRI = o.getOntologyID().getOntologyIRI().get().toString();
        ArrayList<OWLClass> owlclasses = new ArrayList<OWLClass>();
        o.classesInSignature().forEach(owlclasses::add);
        ArrayList<String> classes = new ArrayList<String>();
        for (OWLClass c : owlclasses) classes.add(c.getIRI().getFragment());
        
        ArrayList<OWLDataProperty> owldataproperties = new ArrayList<OWLDataProperty>();
        o.dataPropertiesInSignature().forEach(owldataproperties::add);
        ArrayList<String> dataproperties = new ArrayList<String>();
        for (OWLDataProperty d : owldataproperties) dataproperties.add(d.getIRI().getFragment());
        
        ArrayList<OWLObjectProperty> owlobjproperties = new ArrayList<OWLObjectProperty>();
        o.objectPropertiesInSignature().forEach(owlobjproperties::add);
        ArrayList<String> objproperties = new ArrayList<String>();
        for (OWLObjectProperty ob : owlobjproperties) objproperties.add(ob.getIRI().getFragment());

        Set<SWRLAtom> swrlAtoms = new HashSet<>();
        for (Atom atom : atoms) {
            SWRLAtom a;

            if (atom instanceof ClassAtom) {
                SWRLVariable r = SWRL.variable(IRI.create(ontologyIRI + "#" + atom.getArguments().get(0).getValue().substring(1)));
                a = SWRL.classAtom(factory.getOWLClass(IRI.create(ontologyIRI + "#" + atom.getPredicate())), r);
            }
            else if (atom instanceof DataPropertyAtom) {
                OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#" + atom.getPredicate()));
                SWRLVariable subject = SWRL.variable(IRI.create(ontologyIRI + "#" +  atom.getArguments().get(0).getValue().substring(1)));
                SWRLDArgument value;
                if (atom.getArguments().get(1).getValue().contains("?")) value = SWRL.variable(IRI.create(ontologyIRI + "#" + atom.getArguments().get(1).getValue().substring(1)));
                else {
                    if (atom.getArguments().get(1).getType().matches("xsd:(int|integer|float|double|decimal|long|short|byte)")) {
                        double t = Double.parseDouble(atom.getArguments().get(1).getValue());
                        value = SWRL.constant(t);
                    } else value = SWRL.constant(atom.getArguments().get(1).getValue());
                }
                a = SWRL.propertyAtom(dataProperty, subject, value);
            }
            else if (atom instanceof ObjectPropertyAtom) {
                OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#" + atom.getPredicate()));
                SWRLVariable subject = SWRL.variable(IRI.create(ontologyIRI + "#" + atom.getArguments().get(0).getValue().substring(1)));
                SWRLVariable object = SWRL.variable(IRI.create(ontologyIRI + "#" + atom.getArguments().get(1).getValue().substring(1)));
                a = SWRL.propertyAtom(objectProperty, subject, object);
            }
            else {
                ArrayList<SWRLDArgument> args = new ArrayList<SWRLDArgument>();
                for (Argument argum : atom.getArguments()) {
                    if (argum.getValue().contains("?")) {
                        SWRLVariable v = SWRL.variable(IRI.create(ontologyIRI + "#" + argum.getValue().substring(1)));
                        args.add(v);
                    }
                    else {
                        if (argum.getType().matches("xsd:(int|integer|float|double|decimal|long|short|byte)")) {
                            double t = Double.parseDouble(argum.getValue());
                            args.add(SWRL.constant(t));
                        } else args.add(SWRL.constant(argum.getValue()));
                    }
                }
                a = SWRL.builtIn(SWRLBuiltInsVocabulary.getBuiltIn(IRI.create("http://www.w3.org/2003/11/swrlb#" + atom.getPredicate())), args); 
            }
            swrlAtoms.add(a);
        }
        return swrlAtoms;
    }

    public static SWRLRule toSWRLRule(Rule rule, OWLOntology o) throws OWLException {
        Set<SWRLAtom> swrlbodyatoms = getSWRLAtomsSet(rule.getAntecedent(), o);
        Set<SWRLAtom> swrlheadatoms = getSWRLAtomsSet(rule.getConsequent(), o);

        SWRLRule swrlRule = SWRL.rule(swrlbodyatoms, swrlheadatoms);
        return swrlRule;
    }

    public static List<Atom> getRuleAtomsList(Set<SWRLAtom> swrlatoms, OWLOntology o, HashMap<String, String> varTypes) {
        List<Atom> atoms = new ArrayList<Atom>();

        for (SWRLAtom atom : swrlatoms) {
            Atom newAtom = null;
            if (atom instanceof SWRLClassAtom classAtom) {
                String predicate = classAtom.getPredicate().asOWLClass().getIRI().getFragment();

                ArrayList<SWRLArgument> arguments = (ArrayList<SWRLArgument>) atom.getAllArguments();
                SWRLVariable varib = (SWRLVariable) arguments.get(0);

                Argument var = new Argument();
                var.setType(predicate);
                var.setValue("?" + varib.getIRI().getFragment());

                if (!varTypes.containsKey(var.getValue())) varTypes.put(var.getValue(), var.getType());

                newAtom = new ClassAtom(predicate, var);
            } else if (atom instanceof SWRLObjectPropertyAtom objPropAtom) {
                String predicate = ((OWLObjectProperty) objPropAtom.getPredicate()).getIRI().getFragment();

                ArrayList<SWRLArgument> arguments = (ArrayList<SWRLArgument>) atom.getAllArguments();
                SWRLVariable subVarib = (SWRLVariable) arguments.get(0);
                SWRLVariable obVarib = (SWRLVariable) arguments.get(1);

                OWLObjectProperty objProp = (OWLObjectProperty) objPropAtom.getPredicate();
                OWLClassExpression domain = EntitySearcher.getDomains(objProp, o).findFirst().orElse(null);
                OWLClassExpression range = EntitySearcher.getRanges(objProp, o).findFirst().orElse(null);

                Argument subject = new Argument();
                subject.setType(domain.asOWLClass().getIRI().getFragment());
                subject.setValue("?" + subVarib.getIRI().getFragment());

                if (!varTypes.containsKey(subject.getValue())) varTypes.put(subject.getValue(), subject.getType());

                Argument object = new Argument();
                object.setType(range.asOWLClass().getIRI().getFragment());
                object.setValue("?" + obVarib.getIRI().getFragment());

                if (!varTypes.containsKey(object.getValue())) varTypes.put(object.getValue(), object.getType());
                
                newAtom = new ObjectPropertyAtom(predicate, subject, object);
            } else if (atom instanceof SWRLDataPropertyAtom dataPropAtom) {
                String predicate = ((OWLDataProperty) dataPropAtom.getPredicate()).getIRI().getFragment();

                ArrayList<SWRLArgument> arguments = (ArrayList<SWRLArgument>) atom.getAllArguments();
                SWRLVariable subVarib = (SWRLVariable) arguments.get(0);
                SWRLDArgument val = (SWRLDArgument) arguments.get(1);

                OWLDataProperty dataProp = (OWLDataProperty) dataPropAtom.getPredicate();
                OWLClassExpression domain = EntitySearcher.getDomains(dataProp, o).findFirst().orElse(null);
                OWLDataRange range = EntitySearcher.getRanges(dataProp, o).findFirst().orElse(null);

                Argument subject = new Argument();
                subject.setType(domain.asOWLClass().getIRI().getFragment());
                subject.setValue("?" + subVarib.getIRI().getFragment());

                if (!varTypes.containsKey(subject.getValue())) varTypes.put(subject.getValue(), subject.getType());

                Argument value = new Argument();
                value.setType(range.asOWLDatatype().getIRI().getFragment());
                value.setValue(((SWRLLiteralArgument) val).getLiteral().getLiteral());
                
                newAtom = new ObjectPropertyAtom(predicate, subject, value);

            } else if (atom instanceof SWRLBuiltInAtom builtInAtom) {
                String predicate = builtInAtom.getPredicate().getFragment();

                List<Argument> args = new ArrayList<Argument>();

                ArrayList<SWRLArgument> arguments = (ArrayList<SWRLArgument>) atom.getAllArguments();
                for (SWRLArgument argum : arguments) {
                    Argument arg = new Argument();
                    if (argum instanceof SWRLVariable swrlVar) {
                        arg.setValue("?" + swrlVar.getIRI().getFragment());
                    }
                    else if (argum instanceof SWRLLiteralArgument val) {
                        arg.setType(val.getLiteral().getDatatype().getIRI().getFragment());
                        arg.setValue(val.getLiteral().getLiteral());
                    }

                    args.add(arg);
                }
                
                newAtom = new BuiltInAtom(predicate, args);
            }
            atoms.add(newAtom);
        }

        for (Atom a : atoms) {
            if (a instanceof BuiltInAtom) {
                for (Argument arg : a.getArguments()) {
                    if (arg.getValue().contains("?")) arg.setType(varTypes.get(arg.getValue()));
                }
             }
        }

        return atoms;
    }

    public static Rule fromSWRLRule(SWRLRule swrlRule, OWLOntology o) {
        Rule rule = new Rule();

        Set<SWRLAtom> swrlbodyatoms = swrlRule.getBody();
        Set<SWRLAtom> swrlheadatoms = swrlRule.getHead();
        HashMap<String, String> varTypes = new HashMap<String, String>();
        List<Atom> antecedent = getRuleAtomsList(swrlbodyatoms, o, varTypes);
        List<Atom> consequent = getRuleAtomsList(swrlheadatoms, o, varTypes);

        rule.setAntecedent(antecedent);
        rule.setConsequent(consequent);
        
        return rule;
    }
}
