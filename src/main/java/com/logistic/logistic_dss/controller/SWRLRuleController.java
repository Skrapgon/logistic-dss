package com.logistic.logistic_dss.controller;

import java.util.List;

import org.semanticweb.owlapi.model.OWLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistic.logistic_dss.model.rule.Rule;
import com.logistic.logistic_dss.service.SWRLRuleService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(value="api/logistic/tasks/{taskID}/rules")
public class SWRLRuleController {
    
    @Autowired
    private SWRLRuleService swrlRuleService;

    @GetMapping(value="{ruleID}")
    public ResponseEntity<Rule> getRule(
        @PathVariable("taskID") String taskID,
        @PathVariable("ruleID") String ruleID
        ) {
        try {
            Rule rule = swrlRuleService.getRule(taskID, ruleID);
            return ResponseEntity.ok().body(rule);
        } catch (OWLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Rule>> getRules(
        @PathVariable("taskID") String taskID
        ) {
        try {
            List<Rule> rules = swrlRuleService.getRules(taskID);
            if (rules.isEmpty()) ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            return ResponseEntity.ok().body(rules);
        } catch (OWLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<Rule> createRule(
        @PathVariable("taskID") String taskID,
        @RequestBody Rule newRule
        ) {
        try {
            Rule rule = swrlRuleService.createRule(taskID, newRule);
            return ResponseEntity.ok().body(rule);
        } catch (OWLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping(value="{ruleID}")
    public ResponseEntity<Rule> editRule(
        @PathVariable("ruleID") String ruleID,
        @PathVariable("taskID") String taskID,
        @RequestBody Rule newRule
        ) {
        try {
            Rule rule = swrlRuleService.editRule(taskID, ruleID, newRule);
            return ResponseEntity.ok().body(rule);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        
    }

    @DeleteMapping(value="{ruleID}")
    public ResponseEntity<String> removeRule(
        @PathVariable("ruleID") String ruleID,
        @PathVariable("taskID") String taskID
    ) {
        try {
            String result = swrlRuleService.removeRule(taskID, ruleID);
            return ResponseEntity.ok(result);
        } catch (OWLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    
}
