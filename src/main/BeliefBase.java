package main;

import formulas.*;
import java.util.*;
import java.util.List;

public class BeliefBase {
    protected List<Formula> beliefBase = new ArrayList<>(); //Formulas in the belief base.
    protected List<Integer> priorities = new ArrayList<>(); //Priority of each formula.

    //Returns the size of the belief base.
    public int size() {
        return beliefBase.size();
    }

    /*//Revision of formula with custom priority.
    public void revision(String formula, int priority) {
        revision(Formula.parseString(formula), priority);
    }

    //Revision of formula with custom priority.
    public void revision(Formula formula, int priority) {
        contraction(new NotFormula(formula));
        expansion(formula, priority);
    }*/

    //Revision of formula with generated priority.
    public void revision(String formula) {
        revision(Formula.parseString(formula));
    }

    //Perform revision of a formula with a generated priority.
    public void revision(Formula formula) {
        if (beliefBase.contains(formula)) {
            return;
        }
        contraction(new NotFormula(formula));
        expansion(formula);
    }

    //Performs expansion on a formula with a generated priority.
    public void expansion(Formula formula) {
        List<Formula> beliefBaseCopy = new ArrayList<>(beliefBase);
        beliefBaseCopy.add(formula);
        List<Formula>[] equivalences = new ArrayList[beliefBaseCopy.size()];
        boolean[] alreadyUsed = new boolean[beliefBaseCopy.size()];
        for (int i = 0; i < beliefBaseCopy.size(); i++) {
            equivalences[i] = new ArrayList<>();
            if (alreadyUsed[i]) {
                continue;
            }
            for (int j = i+1; j < beliefBaseCopy.size(); j++) {
                if (resolution(new BiconditionFormula(beliefBaseCopy.get(i), beliefBaseCopy.get(j)))) {
                    equivalences[i].add(beliefBaseCopy.get(j));
                    alreadyUsed[j] = true;
                }
            }
        }
        List<Formula> beliefBaseCopy2 = new ArrayList<>(beliefBaseCopy);
        for (List<Formula> list : equivalences) {
            for (Formula f : list) {
                beliefBaseCopy2.remove(f);
            }
        }
        List<Formula> result = new ArrayList<>();
        List<Integer> prioritiesResult = new ArrayList<>();
        while (!beliefBaseCopy2.isEmpty()) {
            List<Formula> notEntailed = new ArrayList<>();
            for (int i = 0; i < beliefBaseCopy2.size(); i++) {
                boolean entailed = false;
                for (int j = 0; j < beliefBaseCopy2.size(); j++) {
                    if (j != i && entailment(beliefBaseCopy2.get(j), beliefBaseCopy2.get(i))) {
                        entailed = true;
                        break;
                    }
                }
                if (!entailed) {
                    notEntailed.add(beliefBaseCopy2.get(i));
                }
            }
            for (int i = 0; i < notEntailed.size(); i++) {
                //Add
                int priority = notEntailed.get(i).getFormulaPriority();
                for (int j = result.size()-1; j >= 0; j--) {
                    if (entailment(result.get(j), notEntailed.get(i))) {
                        priority += prioritiesResult.get(j);
                        break;
                    }
                }
                int j = 0;
                while (j < result.size() && prioritiesResult.get(j) <= priority) {
                    j++;
                }
                result.add(j, notEntailed.get(i));
                prioritiesResult.add(j, priority);
                beliefBaseCopy2.remove(notEntailed.get(i));
            }
        }
        for (int i = 0; i < equivalences.length; i++) {
            for (Formula f : equivalences[i]) {
                for (int j = 0; j < result.size(); j++) {
                    if (result.get(j) == beliefBaseCopy.get(i)) {
                        result.add(j, f);
                        prioritiesResult.add(j, prioritiesResult.get(j));
                        break;
                    }
                }
            }
        }
        beliefBase = result;
        priorities = prioritiesResult;
        //expansion(formula, formula.getFormulaPriority());
    }

    //Returns a conjunction of all formulas in the belief base.
    public Formula getBeliefBaseFormula(List<Formula> beliefBase) {
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
        return beliefBaseFormula;
    }

    //Checks if a list of formulas entails a specific formula.
    private boolean entailsFormula(List<Formula> beliefBase, Formula formula) {
        if (resolution(formula)) {
            return true;
        }
        Formula beliefBaseFormula = getBeliefBaseFormula(beliefBase);
        if (beliefBaseFormula == null) {
            return false;
        }
        return entailment(beliefBaseFormula, formula);
    }

    //Checks if the belief base entails a specific formula.
    public boolean entailsFormula(Formula formula) {
        return entailsFormula(beliefBase, formula);
    }

    //Finds all inclusion subsets of a specific size for the belief base when contracting with a formula.
    private List<List<Formula>> getInclusionSubsets(List<Formula> formulas, Formula formulaToContract, int nFormulas, int startIndex, List<List<Formula>> result) {
        if (formulas.size() == nFormulas) {
            if (!entailsFormula(formulas, formulaToContract)) {
                result.add(new ArrayList<>(formulas));
            }
        }
        else {
            for (int i = startIndex; i < formulas.size(); i++) {
                Formula formula = formulas.get(i);
                formulas.remove(i);
                result = getInclusionSubsets(formulas, formulaToContract, nFormulas, i, result);
                formulas.add(i, formula);
            }
        }
        return result;
    }

    //Performs partial meet contraction with a specific formula.
    public void contraction(Formula formula) {
        if (!entailsFormula(formula)) {
            return;
        }
        List<Formula> beliefBaseCopy = new ArrayList<>(beliefBase);
        List<Integer> prioritiesCopy = new ArrayList<>(priorities);
        List<List<Formula>> result = new ArrayList<>();
        for (int i = beliefBaseCopy.size()-1; i >= 1; i--) {
            result = getInclusionSubsets(beliefBaseCopy, formula, i, 0, new ArrayList<>());
            if (!result.isEmpty()) {
                break;
            }
        }
        if (result.isEmpty()) {
            beliefBase = new ArrayList<>();
            priorities = new ArrayList<>();
        }
        else {
            int best = -1;
            int bestIndex = -1;
            for (int i = 0; i < result.size(); i++) {
                int score = 0;
                for (int j = 0; j < result.get(i).size(); j++) {
                    score += priorities.get(beliefBase.indexOf(result.get(i).get(j)));
                }
                if (score > best) {
                    best = score;
                    bestIndex = i;
                }
            }
            beliefBase = result.get(bestIndex);
            priorities = new ArrayList<>();
            for (int i = 0; i < beliefBase.size(); i++) {
                priorities.add(prioritiesCopy.get(beliefBaseCopy.indexOf(beliefBase.get(i))));
            }
        }
    }

    //Checks if one formula entails another formula.
    public boolean entailment(Formula f1, Formula f2) {
        return resolution(new ImpliesFormula(f1, f2));
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
        formula = distributeAndOverOr(formula);

        //Extract clauses.
        List<List<Formula>> clauses = extractClauses(formula);

        //Perform resolution.
        return performResolution(clauses);
    }

    //Performs resolution on a list of clauses.
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

    //Returns the resultants from clashing two clauses.
    private List<List<Formula>> clashClauses(List<Formula> clause1, List<Formula> clause2) {
        List<Formula> clashing = new ArrayList<>();
        List<int[]> indices = new ArrayList<>();
        for (int i = 0; i < clause1.size(); i++) {
            for (int j = 0; j < clause2.size(); j++) {
                if (isClash(clause1.get(i), clause2.get(j))) {
                    clashing.add(clause1.get(i) instanceof NotFormula ? clause2.get(j) : clause1.get(i));
                    indices.add(new int[] {i, j});
                }
            }
        }
        List<List<Formula>> result = new ArrayList<>();
        if (clashing.size() > 1) {
            return result;
        }
        for (int i = 0; i < clashing.size(); i++) {
            List<Formula> resolvent = new ArrayList<>();
            for (int j = 0; j < clause1.size(); j++) {
                if (j != indices.get(i)[0] && !resolvent.contains(clause1.get(j))) {
                    resolvent.add(clause1.get(j));
                }
            }
            for (int j = 0; j < clause2.size(); j++) {
                if (j != indices.get(i)[1] && !resolvent.contains(clause2.get(j))) {
                    resolvent.add(clause2.get(j));
                }
            }
            result.add(resolvent);
        }
        return result;
    }

    //Checks if two literals clash.
    private boolean isClash(Formula f1, Formula f2) {
        if (f1 instanceof NotFormula && f2 instanceof PredicateFormula) {
            return f1.operands[0].equals(f2);
        }
        else if (f1 instanceof PredicateFormula && f2 instanceof NotFormula) {
            return f2.operands[0].equals(f1);
        }
        return false;
    }

    //Distributes conjunctions over disjunctions for a formula.
    private Formula distributeAndOverOr(Formula formula) {
        boolean[] isCNF = new boolean[1];
        while (!isCNF[0]) {
            isCNF[0] = true;
            formula = distributeAndOverOrRecursive(formula, isCNF);
        }
        return formula;
    }

    //Recursively performs an iteration of distributing conjunctions over disjunctions for a formula.
    private Formula distributeAndOverOrRecursive(Formula formula, boolean[] isCNF) {
        if (formula instanceof OrFormula) {
            if (formula.operands[0] instanceof AndFormula) {
                isCNF[0] = false;
                return new AndFormula(distributeAndOverOrRecursive(new OrFormula(formula.operands[1], formula.operands[0].operands[0]), isCNF), distributeAndOverOrRecursive(new OrFormula(formula.operands[1], formula.operands[0].operands[1]), isCNF));
            }
            else if (formula.operands[1] instanceof AndFormula) {
                isCNF[0] = false;
                return new AndFormula(distributeAndOverOrRecursive(new OrFormula(formula.operands[0], formula.operands[1].operands[0]), isCNF), distributeAndOverOrRecursive(new OrFormula(formula.operands[0], formula.operands[1].operands[1]), isCNF));
            }
            else {
                return new OrFormula(distributeAndOverOrRecursive(formula.operands[0], isCNF), distributeAndOverOrRecursive(formula.operands[1], isCNF));
            }
        }
        else if (formula instanceof AndFormula) {
            return new AndFormula(distributeAndOverOrRecursive(formula.operands[0], isCNF), distributeAndOverOrRecursive(formula.operands[1], isCNF));
        }
        return formula;
    }

    //Replaces implications and bi-implications in a formula.
    private Formula removeImpliesAndBiconditional(Formula formula) {
        if (formula instanceof ImpliesFormula) {
            return new OrFormula(new NotFormula(removeImpliesAndBiconditional(formula.operands[0])), removeImpliesAndBiconditional(formula.operands[1]));
        }
        else if (formula instanceof BiconditionFormula) {
            return new AndFormula(
                    new OrFormula(new NotFormula(removeImpliesAndBiconditional(formula.operands[0])), removeImpliesAndBiconditional(formula.operands[1])),
                    new OrFormula(new NotFormula(removeImpliesAndBiconditional(formula.operands[1])), removeImpliesAndBiconditional(formula.operands[0])));
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

    //Extracts the clauses for a formula on CNF.
    private List<List<Formula>> extractClauses(Formula formula) {
        List<List<Formula>> clauses = new ArrayList<>();
        if (!(formula instanceof AndFormula)) { //If there is only one clause.
            clauses.add(new ArrayList<>());
        }
        extractClauses(formula, clauses);
        return clauses;
    }

    //Recursively extracts the clauses for a formula on CNF.
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

    //Resets the belief base.
    public void reset() {
        beliefBase = new ArrayList<>();
        priorities = new ArrayList<>();
    }

    //Converts the belief base to a string for debugging.
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

    //----Everything below is stuff for Mastermind only----//

    //Revision for Mastermind.
    public void revisionMastermind(String formula, int priority) {
        revisionMastermind(Formula.parseString(formula), priority);
    }

    //Revision for Mastermind.
    public void revisionMastermind(Formula formula, int priority) {
        boolean success = contractionMastermind(new NotFormula(formula), priority);
        if (success) {
            int index = beliefBase.indexOf(formula);
            if (index >= 0) {
                priority = Math.max(priorities.get(index), priority);
            }
            expansionMastermind(formula, priority);
        }
    }

    //Contraction for Mastermind.
    public boolean contractionMastermind(Formula formula, int priority) {
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

    //Expansion for Mastermind.
    public void expansionMastermind(Formula formula, int priority) {
        if (beliefBase.contains(formula)) {
            int index = beliefBase.indexOf(formula);
            beliefBase.remove(index);
            priorities.remove(index);
        }
        int i = 0;
        while (i < beliefBase.size() && priorities.get(i) <= priority) {
            i++;
        }
        beliefBase.add(i, formula);
        priorities.add(i, priority);
    }
}
