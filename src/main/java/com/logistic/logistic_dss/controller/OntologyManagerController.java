package com.logistic.logistic_dss.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistic.logistic_dss.model.Ontology;
import com.logistic.logistic_dss.service.OntologyManagerService;

import java.util.List;

import org.semanticweb.owlapi.model.OWLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(value="api/logistic/tasks")
public class OntologyManagerController {

    @Autowired
    private OntologyManagerService ontologyManagerService;

    @GetMapping(value="{taskID}")
    public ResponseEntity<Ontology> getOntology(@PathVariable("taskID") String taskID) {
        try {
            Ontology ontology = ontologyManagerService.getOntology(taskID);
            return ResponseEntity.ok(ontology);
        } catch (OWLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Ontology>> getOntologies() {
        List<Ontology> ontologies = ontologyManagerService.getOntologies();

        if (ontologies.isEmpty()) return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);

        return ResponseEntity.ok(ontologies);
    }

    @PostMapping
    public ResponseEntity<Ontology> createOntology(@RequestBody Ontology ontologyInfo) {
        try {
            Ontology ontology = ontologyManagerService.createOntology(
                ontologyInfo.getName(),
                ontologyInfo.getDescription(),
                ontologyInfo.getClassesProperties());
            return ResponseEntity.ok(ontology);
        } catch (OWLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping(value="{taskID}")
    public  ResponseEntity<Ontology> editOntology(
        @PathVariable("taskID") String taskID,
        @RequestBody Ontology newOntologyInfo
        ) {
        try {
            Ontology ontology = ontologyManagerService.ediOntology(
                taskID,
                newOntologyInfo.getName(),
                newOntologyInfo.getDescription(),
                newOntologyInfo.getClassesProperties());
            return ResponseEntity.ok(ontology);
        } catch (OWLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    @DeleteMapping(value="{taskID}")
    public ResponseEntity<String> removeOntology(@PathVariable("taskID") String taskID) {
        try {
            String result = ontologyManagerService.removeOntology(taskID);
            return ResponseEntity.ok(result);
        } catch (OWLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
