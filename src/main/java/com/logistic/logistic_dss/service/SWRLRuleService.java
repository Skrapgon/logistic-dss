package com.logistic.logistic_dss.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.logistic.logistic_dss.model.rule.Rule;
import com.logistic.logistic_dss.util.ParserSWRLRule;

@Service
public class SWRLRuleService {

    private OWLOntologyManager manager;
    private OWLDataFactory factory;
    private OWLAnnotationProperty idProperty;

    @Value("${ontology.base.path}")
    private String ontologiesDirectory;

    @Value("${ontology.base.IRI}")
    private String baseOntologyIRI;

    @Value("${ontology.base.ruleID}")
    private String ruleIDIRI;

    @Autowired
    private OntologyManagerService ontologyManagerService;

    public SWRLRuleService(
        @Value("${ontology.base.path}") String ontologiesDirectory,
        @Value("${ontology.base.IRI}") String baseOntologyIRI,
        @Value("${ontology.base.ruleID}") String ruleIDIRI
        ) {
        this.manager = OWLManager.createOWLOntologyManager();
        this.factory = manager.getOWLDataFactory();

        this.ontologiesDirectory = ontologiesDirectory;
        this.baseOntologyIRI = baseOntologyIRI;
        this.ruleIDIRI = ruleIDIRI;

        this.idProperty = factory.getOWLAnnotationProperty(IRI.create(ruleIDIRI));
    }

    public Rule createRule(String taskID, Rule rule) throws OWLException {
        OWLOntology o = ontologyManagerService.getOWLOntology(taskID);

        String ruleID = UUID.randomUUID().toString();
        SWRLRule swrlRule = ParserSWRLRule.toSWRLRule(rule, o);
        OWLAnnotation annotation = factory.getOWLAnnotation(idProperty, factory.getOWLLiteral(ruleID));
        OWLAxiom annotatedAxiom = swrlRule.getAnnotatedAxiom(Collections.singleton(annotation));
        manager.addAxiom(o, annotatedAxiom);

        File saveFile = new File(ontologiesDirectory + ontologyManagerService.getOntology(taskID).getName() + ".owl");
        manager.saveOntology(o, IRI.create(saveFile));

        rule.setID(ruleID);

        return rule;
    }

    public ArrayList<Rule> getRules(String taskID) throws OWLException {
        OWLOntology o = ontologyManagerService.getOWLOntology(taskID);
        ArrayList<Rule> rules = new ArrayList<Rule>();
        for (SWRLRule swrlRule: o.getAxioms(AxiomType.SWRL_RULE)) {
            String ruleID = null;
            for (OWLAnnotation annotation : swrlRule.getAnnotations()) {
                if (annotation.getProperty().equals(idProperty)) {
                    OWLAnnotationValue value = annotation.getValue();
                    if (value instanceof OWLLiteral) ruleID = ((OWLLiteral) value).getLiteral();
                }
            }
            Rule rule = ParserSWRLRule.fromSWRLRule(swrlRule, o);
            rule.setID(ruleID);
            rules.add(rule);
        }
        return rules;
    }

    public Rule getRule(String taskID, String ruleID) throws OWLException {
        OWLOntology o = ontologyManagerService.getOWLOntology(taskID);
        for (SWRLRule swrlRule: o.getAxioms(AxiomType.SWRL_RULE)) {
            for (OWLAnnotation annotation : swrlRule.getAnnotations()) {
                if (annotation.getProperty().equals(idProperty)) {
                    OWLAnnotationValue value = annotation.getValue();
                    if (value instanceof OWLLiteral) {
                        String curRuleID = ((OWLLiteral) value).getLiteral();
                        if (curRuleID.equals(ruleID)) {
                            Rule rule = ParserSWRLRule.fromSWRLRule(swrlRule, o);
                            rule.setID(ruleID);
                            return rule;
                        }
                    }
                }
            }
        }
        throw new OWLException("Правила с таким ID не существует");
    }

    public Rule editRule(String taskID, String ruleID, Rule newRule) throws OWLException {
        this.removeRule(taskID, ruleID);

        OWLOntology o = ontologyManagerService.getOWLOntology(taskID);

        SWRLRule swrlRule = ParserSWRLRule.toSWRLRule(newRule, o);
        OWLAnnotation annotation = factory.getOWLAnnotation(idProperty, factory.getOWLLiteral(ruleID));
        OWLAxiom annotatedAxiom = swrlRule.getAnnotatedAxiom(Collections.singleton(annotation));
        manager.addAxiom(o, annotatedAxiom);

        newRule.setID(ruleID);

        File saveFile = new File(ontologiesDirectory + ontologyManagerService.getOntology(taskID).getName() + ".owl");
        manager.saveOntology(o, IRI.create(saveFile));

        return newRule;
    }

    public String removeRule(String taskID, String ruleID) throws OWLException {
        OWLOntology o = ontologyManagerService.getOWLOntology(taskID);
        for (SWRLRule swrlRule: o.getAxioms(AxiomType.SWRL_RULE)) {
            for (OWLAnnotation annotation : swrlRule.getAnnotations()) {
                if (annotation.getProperty().equals(idProperty)) {
                    OWLAnnotationValue value = annotation.getValue();
                    if (value instanceof OWLLiteral) {
                        String curRuleID = ((OWLLiteral) value).getLiteral();
                        if (curRuleID.equals(ruleID)) {
                            o.remove(swrlRule);
                            File saveFile = new File(ontologiesDirectory + ontologyManagerService.getOntology(taskID).getName() + ".owl");
                            manager.saveOntology(o, IRI.create(saveFile));
                            return("Правило с ID: " + ruleID + " удалено");
                        }
                    }
                }
            }
        }
        throw new OWLException("Правила с таким ID не существует");
    }
}
