package com.logistic.logistic_dss.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.logistic.logistic_dss.model.Ontology;
import com.logistic.logistic_dss.util.OntologyTransferUtil;

@Service
public class OntologyManagerService {
    private final OWLOntologyManager manager;
    private final OWLDataFactory factory;
    private Map<String, Ontology> ontologyMap;
    private Map<String, OWLOntology> owlOntologyMap;

    private final String ontologiesDirectory;
    private final String baseOntologyIRI;
    private final String taskNameIRI;

    public OntologyManagerService(
        @Value("${ontology.base.path}") String ontologiesDirectory,
        @Value("${ontology.base.IRI}") String baseOntologyIRI,
        @Value("${ontology.base.taskNameIRI}") String taskNameIRI
        ) throws OWLException {
        this.manager = OWLManager.createOWLOntologyManager();
        this.factory = manager.getOWLDataFactory();
        this.ontologyMap = new HashMap<String, Ontology>();
        this.owlOntologyMap = new HashMap<String, OWLOntology>();

        this.ontologiesDirectory = ontologiesDirectory;
        this.baseOntologyIRI = baseOntologyIRI;
        this.taskNameIRI = taskNameIRI;
    }

    public Ontology createOntology(String name, String description, Set<String> classesProperties) throws OWLException {
        try {
            String taskID = UUID.randomUUID().toString();
            IRI ontologyIRI = IRI.create(baseOntologyIRI + taskID);
            OWLOntology ontology = manager.createOntology(ontologyIRI);

            OWLAnnotation descriptionAnnotation = factory.getRDFSComment(description);
            OWLAnnotationAssertionAxiom descriptionAxiom = factory.getOWLAnnotationAssertionAxiom(ontologyIRI, descriptionAnnotation);
            
            OWLAnnotationProperty nameProp = factory.getOWLAnnotationProperty(IRI.create(taskNameIRI));
            OWLAnnotation nameAnnotation = factory.getOWLAnnotation(nameProp, factory.getOWLLiteral(name));
            OWLAnnotationAssertionAxiom nameAxiom = factory.getOWLAnnotationAssertionAxiom(ontologyIRI, nameAnnotation);

            ontology.add(descriptionAxiom);
            ontology.add(nameAxiom);

            File owlPath = new File(ontologiesDirectory + "base.owx");
            OWLOntology baseOntology = manager.loadOntologyFromOntologyDocument(owlPath);

            OntologyTransferUtil.importCLassesProperties(ontology, baseOntology, classesProperties);

            Ontology curOntology = new Ontology();
            curOntology.setID(taskID);
            curOntology.setName(name);
            curOntology.setDescription(description);
            curOntology.setClassesProperties(classesProperties);

            File saveFile = new File(ontologiesDirectory + taskID + ".owx");
            manager.saveOntology(ontology, IRI.create(saveFile));

            ontologyMap.put(taskID, curOntology);
            owlOntologyMap.put(taskID, ontology);

            return curOntology;
        } catch (OWLOntologyCreationException e) {
            throw new OWLException("Ошибка при создании онтологии", e);
        }
    }

    public ArrayList<Ontology> getOntologies() {
        ArrayList<Ontology> ontologies = new ArrayList<Ontology>();
        ontologies.addAll(ontologyMap.values());
        return ontologies;
    }

    public Ontology getOntology(String taskID) throws OWLException {
        Ontology curOntology = ontologyMap.get(taskID);
        if (curOntology == null) {
            throw new OWLException("Онтология с таким ID не найдена");
        }
        return curOntology;
    }

    public Ontology ediOntology(
        String taskID, String name, String description, Set<String> classesProperties
        ) throws OWLException {
        Ontology curOntology = ontologyMap.get(taskID);
        if (curOntology == null) {
            throw new OWLException("Онтология с таким ID не найдена");
        }

        curOntology.setName(name);
        curOntology.setDescription(description);
        curOntology.setClassesProperties(classesProperties);

        OWLOntology ontology = owlOntologyMap.get(taskID);

        ontology.removeAxioms(ontology.getAxioms());

        IRI ontologyIRI = IRI.create(baseOntologyIRI + taskID);

        OWLAnnotation descriptionAnnotation = factory.getRDFSComment(description);
        OWLAnnotationAssertionAxiom descriptionAxiom = factory.getOWLAnnotationAssertionAxiom(ontologyIRI, descriptionAnnotation);
            
        OWLAnnotationProperty nameProp = factory.getOWLAnnotationProperty(IRI.create(taskNameIRI));
        OWLAnnotation nameAnnotation = factory.getOWLAnnotation(nameProp, factory.getOWLLiteral(name));
        OWLAnnotationAssertionAxiom nameAxiom = factory.getOWLAnnotationAssertionAxiom(ontologyIRI, nameAnnotation);

        ontology.add(descriptionAxiom);
        ontology.add(nameAxiom);

        File owlPath = new File(ontologiesDirectory + "base.owx");
        OWLOntology baseOntology = manager.loadOntologyFromOntologyDocument(owlPath);

        OntologyTransferUtil.importCLassesProperties(ontology, baseOntology, classesProperties);

        File saveFile = new File(ontologiesDirectory + taskID + ".owx");
        manager.saveOntology(ontology, IRI.create(saveFile));
        
        return curOntology;
    }

    public String removeOntology(String taskID) throws OWLException {
        Ontology curOntology = ontologyMap.get(taskID);
        if (curOntology == null) {
            throw new OWLException("Онтология с таким ID не найдена");
        }

        manager.removeOntology(owlOntologyMap.get(taskID));
        File file = new File(ontologiesDirectory + taskID + ".owx");
        file.delete();
        ontologyMap.remove(taskID);
        return("Онтология с ID: " + taskID + " удалена");
    }

    public OWLOntology getOWLOntology(String taskID) throws OWLException {
        OWLOntology ontology = owlOntologyMap.get(taskID);
        if (ontology == null) {
            throw new OWLException("Онтология с таким ID не найдена");
        }
        return ontology;
    }
}