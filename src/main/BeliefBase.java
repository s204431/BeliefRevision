package main;

import formulas.*;

import java.text.Normalizer;
import java.util.*;

public class BeliefBase {
    protected List<Formula> beliefBase = new ArrayList<>(); //Formulas in the belief base.
    protected List<Integer> priorities = new ArrayList<>(); //Priority of each formula.

    public int size() {
        return beliefBase.size();
    }

    public void revision(String formula, int priority) {
        revision(Formula.parseString(formula), priority);
    }

    public void revision(Formula formula, int priority) {
        boolean success = contraction(new NotFormula(formula), priority);
        if (success) {
            int index = beliefBase.indexOf(formula);
            if (index >= 0) {
                priority = Math.max(priorities.get(index), priority);
            }
            expansion(formula, priority);
        }
    }

    public boolean entailsFormula(Formula formula) {
        Formula beliefBaseFormula = null;
        if (beliefBase.size() >= 2) {
            beliefBaseFormula = new AndFormula(beliefBase.get(0), beliefBase.get(1));
            for (int i = 2; i < beliefBase.size(); i++) {
                beliefBaseFormula = new AndFormula(beliefBaseFormula, beliefBase.get(i));
            }
        }
        else if (beliefBase.size() == 1) {
            beliefBaseFormula = beliefBase.get(0);
        }
        if (beliefBaseFormula == null) {
            return false;
        }
        return entailment(beliefBaseFormula, formula);
    }

    public boolean contraction(Formula formula, int priority) {
        if (!entailsFormula(formula)) {
            return true;
        }
        List<Formula> beliefBaseCopy = new ArrayList<>(beliefBase);
        List<Integer> prioritiesCopy = new ArrayList<>(priorities);
        beliefBase = new ArrayList<>();
        priorities = new ArrayList<>();
        for (int i = beliefBaseCopy.size()-1; i >= 0; i--) {
            beliefBase.add(0, beliefBaseCopy.get(i));
            priorities.add(0, prioritiesCopy.get(i));
            if (entailsFormula(formula)) {
                if (prioritiesCopy.get(i) > priority) { //Can't add because priority is too low.
                    beliefBase = beliefBaseCopy;
                    priorities = prioritiesCopy;
                    return false;
                }
                beliefBase.remove(0);
                priorities.remove(0);
            }
        }
        return true;
    }

    public void expansion(Formula formula, int priority) {
        if (beliefBase.contains(formula)) {
            removeFormula(formula);
        }
        int i = 0;
        while (i < beliefBase.size() && priorities.get(i) <= priority) {
            i++;
        }
        beliefBase.add(i, formula);
        priorities.add(i, priority);
    }

    public boolean entailment(Formula f1, Formula f2) {
        return resolution(new ImpliesFormula(f1, f2));
    }

    private void removeFormula(Formula formula) {
        int index = beliefBase.indexOf(formula);
        beliefBase.remove(index);
        priorities.remove(index);
    }

    //Converts a formula to CNF and performs resolution on the formula. Returns true if it is valid, false if it is not valid.
    public boolean resolution(Formula originalFormula) {
        //Copy and negate formula.
        Formula formula = new NotFormula(originalFormula.copy());

        //Remove implies and biconditional.
        formula = removeImpliesAndBiconditional(formula);

        //Push negation inwards.
        formula = pushNegationInwards(formula);

        //Convert to CNF.
        formula = convertToCNF(formula);

        //Extract clauses.
        List<List<Formula>> clauses = extractClauses(formula);

        //Perform resolution.
        return performResolution(clauses);
    }

    private boolean performResolution(List<List<Formula>> clauses) {
        int prevSize = 0;
        while(true) {
            //Clash all clauses.
            List<List<Formula>> newClauses = new ArrayList<>();
            for (int i = 0; i < clauses.size(); i++) {
                for (int j = Math.max(i+1, prevSize); j < clauses.size(); j++) {
                    List<List<Formula>> resolvents = clashClauses(clauses.get(i), clauses.get(j));
                    for (List<Formula> clause : resolvents) {
                        if (clause.isEmpty()) {
                            return true;
                        }
                        if (!newClauses.contains(clause)) {
                            newClauses.add(clause);
                        }
                    }
                }
            }
            prevSize = clauses.size();
            for (List<Formula> clause : newClauses) {
                if (!clauses.contains(clause)) {
                    clauses.add(clause);
                }
            }
            if (clauses.size() == prevSize) {
                return false;
            }
        }
    }

    private List<List<Formula>> clashClauses(List<Formula> clause1, List<Formula> clause2) {
        List<Formula> clashing = new ArrayList<>();
        for (Formula formula1 : clause1) {
            for (Formula formula2 : clause2) {
                if (isClash(formula1, formula2)) {
                    clashing.add(formula1 instanceof NotFormula ? formula2 : formula1);
                }
            }
        }
        List<List<Formula>> result = new ArrayList<>();
        if (clashing.size() > 1) {
            return result;
        }
        for (Formula formula : clashing) {
            List<Formula> resolvent = new ArrayList<>();
            for (Formula clause : clause1) {
                if (!clause.equals(formula) && !isClash(clause, formula) && !resolvent.contains(clause)) {
                    resolvent.add(clause);
                }
            }
            for (Formula clause : clause2) {
                if (!clause.equals(formula) && !isClash(clause, formula) && !resolvent.contains(clause)) {
                    resolvent.add(clause);
                }
            }
            result.add(resolvent);
        }
        return result;
    }

    private boolean isClash(Formula f1, Formula f2) {
        if (f1 instanceof NotFormula && f2 instanceof PredicateFormula) {
            return f1.operands[0].equals(f2);
        }
        else if (f1 instanceof PredicateFormula && f2 instanceof NotFormula) {
            return f2.operands[0].equals(f1);
        }
        return false;
    }

    private Formula convertToCNF(Formula formula) {
        boolean[] isCNF = new boolean[1];
        while (!isCNF[0]) {
            isCNF[0] = true;
            formula = convertToCNFRecursive(formula, isCNF);
        }
        return formula;
    }

    private Formula convertToCNFRecursive(Formula formula, boolean[] isCNF) {
        if (formula instanceof OrFormula) {
            if (formula.operands[0] instanceof AndFormula) {
                isCNF[0] = false;
                return new AndFormula(convertToCNFRecursive(new OrFormula(formula.operands[1], formula.operands[0].operands[0]), isCNF), convertToCNFRecursive(new OrFormula(formula.operands[1], formula.operands[0].operands[1]), isCNF));
            }
            else if (formula.operands[1] instanceof AndFormula) {
                isCNF[0] = false;
                return new AndFormula(convertToCNFRecursive(new OrFormula(formula.operands[0], formula.operands[1].operands[0]), isCNF), convertToCNFRecursive(new OrFormula(formula.operands[0], formula.operands[1].operands[1]), isCNF));
            }
            else {
                return new OrFormula(convertToCNFRecursive(formula.operands[0], isCNF), convertToCNFRecursive(formula.operands[1], isCNF));
            }
        }
        else if (formula instanceof AndFormula) {
            return new AndFormula(convertToCNFRecursive(formula.operands[0], isCNF), convertToCNFRecursive(formula.operands[1], isCNF));
        }
        return formula;
    }

    private Formula removeImpliesAndBiconditional(Formula formula) {
        if (formula instanceof ImpliesFormula) {
            return new OrFormula(new NotFormula(removeImpliesAndBiconditional(formula.operands[0])), removeImpliesAndBiconditional(formula.operands[1]));
        }
        else if (formula instanceof BiconditionFormula) {
            return new OrFormula(
                    new AndFormula(removeImpliesAndBiconditional(formula.operands[0]), removeImpliesAndBiconditional(formula.operands[1])),
                    new AndFormula(new NotFormula(removeImpliesAndBiconditional(formula.operands[0])), new NotFormula(removeImpliesAndBiconditional(formula.operands[1]))));
        }
        else if (formula instanceof NotFormula) {
            return new NotFormula(removeImpliesAndBiconditional(formula.operands[0]));
        }
        else if (formula instanceof PredicateFormula) {
            return formula;
        }
        else if (formula instanceof OrFormula) {
            return new OrFormula(removeImpliesAndBiconditional(formula.operands[0]), removeImpliesAndBiconditional(formula.operands[1]));
        }
        else { //And formula
            return new AndFormula(removeImpliesAndBiconditional(formula.operands[0]), removeImpliesAndBiconditional(formula.operands[1]));
        }
    }

    //Pushes negation inwards (assumes implies and biconditional have been removed).
    private Formula pushNegationInwards(Formula formula) {
        if (formula instanceof NotFormula) {
            if (formula.operands[0] instanceof NotFormula) {
                return pushNegationInwards(formula.operands[0].operands[0]);
            }
            else if (formula.operands[0] instanceof OrFormula) {
                return new AndFormula(pushNegationInwards(new NotFormula(formula.operands[0].operands[0])), pushNegationInwards(new NotFormula(formula.operands[0].operands[1])));
            }
            else if (formula.operands[0] instanceof AndFormula) {
                return new OrFormula(pushNegationInwards(new NotFormula(formula.operands[0].operands[0])), pushNegationInwards(new NotFormula(formula.operands[0].operands[1])));
            }
            else { //Predicate
                return formula;
            }
        }
        else if (formula instanceof PredicateFormula) {
            return formula;
        }
        else if (formula instanceof OrFormula) {
            return new OrFormula(pushNegationInwards(formula.operands[0]), pushNegationInwards(formula.operands[1]));
        }
        else { //And formula
            return new AndFormula(pushNegationInwards(formula.operands[0]), pushNegationInwards(formula.operands[1]));
        }
    }

    private List<List<Formula>> extractClauses(Formula formula) {
        List<List<Formula>> clauses = new ArrayList<>();
        if (!(formula instanceof AndFormula)) { //If there is only one clause.
            clauses.add(new ArrayList<>());
        }
        extractClauses(formula, clauses);
        return clauses;
    }

    private void extractClauses(Formula formula, List<List<Formula>> clauses) {
        if (formula instanceof OrFormula) {
            extractClauses(formula.operands[0], clauses);
            extractClauses(formula.operands[1], clauses);
        }
        else if (formula instanceof AndFormula) {
            if (!(formula.operands[0] instanceof AndFormula)) {
                clauses.add(new ArrayList<>());
            }
            extractClauses(formula.operands[0], clauses);
            if (!(formula.operands[1] instanceof AndFormula)) {
                clauses.add(new ArrayList<>());
            }
            extractClauses(formula.operands[1], clauses);
        }
        else if (!clauses.get(clauses.size()-1).contains(formula)) { //Not or predicate
            clauses.get(clauses.size()-1).add(formula);
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder("{");
        for (int i = 0; i < beliefBase.size(); i++) {
            s.append(beliefBase.get(i).prettyPrint());
            if (i != beliefBase.size()-1) {
                s.append(", ");
            }
        }
        s.append("}");
        return s.toString();
    }
}
