import formulas.*;

import java.util.ArrayList;
import java.util.List;

public class BeliefBase {
    private List<Formula> beliefBase = new ArrayList<>();


    public void revision(Formula formula) {
        contraction(new NotFormula(formula));
        expansion(formula);
    }

    private boolean entailsFormula(Formula formula) {
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

    public void contraction(Formula formula) {
        if (!entailsFormula(formula)) {
            return;
        }
        //TODO: Not done yet.
    }

    public void expansion(Formula formula) {
        beliefBase.add(formula);
    }

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
        formula = convertToCNF(formula);

        //Extract clauses.
        List<List<Formula>> clauses = extractClauses(formula);

        //Perform resolution.
        return performResolution(clauses);
    }

    //Performs the resolution after the formula has been converted to CNF.
    private boolean performResolution(List<List<Formula>> clauses) {
        for (int i = 0; i < clauses.size(); i++) {
            for (int j = i+1; j < clauses.size(); j++) {
                for (int k = 0; k < clauses.get(i).size(); k++) {
                    for (int l = 0; l < clauses.get(j).size(); l++) {
                        if (isClash(clauses.get(i).get(k), clauses.get(j).get(l))) {
                            List<Formula> clashed = new ArrayList<>();
                            for (int a = 0; a < clauses.get(i).size(); a++) {
                                if (a != k && !clashed.contains(clauses.get(i).get(a))) {
                                    clashed.add(clauses.get(i).get(a));
                                }
                            }
                            for (int a = 0; a < clauses.get(j).size(); a++) {
                                if (a != l && !clashed.contains(clauses.get(j).get(a))) {
                                    clashed.add(clauses.get(j).get(a));
                                }
                            }
                            if (clashed.isEmpty()) { //Empty clause reached.
                                return true;
                            }
                            List<List<Formula>> copy = new ArrayList<>(List.copyOf(clauses));
                            List<Formula> clause1 = clauses.get(i);
                            List<Formula> clause2 = clauses.get(j);
                            copy.remove(clause1);
                            copy.remove(clause2);
                            copy.add(clashed);
                            boolean isValid = performResolution(copy);
                            if (isValid) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
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
}
