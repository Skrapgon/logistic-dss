package com.logistic.logistic_dss.util;

import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class OntologyTransferUtil {
    public static void importCLassesProperties(OWLOntology targetOntology, OWLOntology sourceOntology, Set<String> classesProperties) {
        String sourceIRI = sourceOntology.getOntologyID().getOntologyIRI().get().getIRIString();
        Set<OWLEntity> entitiesToCopy = classesProperties.stream().map(name -> IRI.create(sourceIRI + "#" + name))
        .map(iri -> sourceOntology.getEntitiesInSignature(iri)).flatMap(Set::stream).collect(Collectors.toSet());

        Set<OWLEntity> datatypesInUse = sourceOntology.getAxioms().stream()
        .flatMap(axiom -> axiom.signature()).filter(entity -> entity.isOWLDatatype()).collect(Collectors.toSet());

        entitiesToCopy.addAll(datatypesInUse);
        
        Set<OWLAxiom> axiomsToImport = sourceOntology.getAxioms().stream()
        .filter(axiom -> axiom.signature().allMatch(entitiesToCopy::contains))
        .collect(Collectors.toSet());

        targetOntology.addAxioms(axiomsToImport);
    }
}
