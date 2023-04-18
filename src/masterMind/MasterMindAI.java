package masterMind;

import formulas.AndFormula;
import formulas.Formula;
import formulas.NotFormula;
import formulas.OrFormula;
import main.BeliefBase;

import java.util.*;

public class MasterMindAI {

    private BeliefBase beliefBase;
    private BeliefBase factBeliefBase;
    private List<Integer>[] notGuessedColors = new ArrayList[MasterMindGame.CODE_LENGTH]; //Colors not yet guessed for each position.
    private static final int FACT_PRIORITY = 100000;
    private List<Integer> alreadyGuessed = new ArrayList<>();
    private List<int[]> colorsHistory = new ArrayList<>();
    private List<int[]> feedbackHistory = new ArrayList<>();

    public MasterMindAI(BeliefBase beliefBase) {
        this.beliefBase = beliefBase;
        factBeliefBase = new BeliefBase();
        for (int i = 0; i < notGuessedColors.length; i++) {
            String rule = "";
            notGuessedColors[i] = new ArrayList<>();
            for (int j = 1; j <= MasterMindGame.NUMBER_OF_COLORS; j++) {
                notGuessedColors[i].add(j);
                rule += getPredicate(i, j);
                if (j != MasterMindGame.NUMBER_OF_COLORS) {
                    rule += OrFormula.operator;
                }
            }
            //Add game rule with high priority.
            beliefBase.revisionMastermind(rule, FACT_PRIORITY);
            factBeliefBase.revisionMastermind(rule, FACT_PRIORITY);
        }
        //Initial guess.
        beliefBase.revisionMastermind(getPredicate(0, 1), 0);
        beliefBase.revisionMastermind(getPredicate(1, 1), 0);
        beliefBase.revisionMastermind(getPredicate(2, 2), 0);
        beliefBase.revisionMastermind(getPredicate(3, 2), 0);
    }

    private int proveColor(int position) {
        for (int i = 1; i <= MasterMindGame.NUMBER_OF_COLORS; i++) {
            if (factBeliefBase.entailsFormula(Formula.parseString(getPredicate(position, i)))) {
                return i;
            }
        }
        List<Integer> possibilities = new ArrayList<>();
        for (int i = 1; i <= MasterMindGame.NUMBER_OF_COLORS; i++) {
            if (!factBeliefBase.entailsFormula(Formula.parseString("!"+getPredicate(position, i)))) {
                possibilities.add(i);
            }
        }
        if (possibilities.size() == 1) {
            return possibilities.get(0);
        }
        return -1;
    }

    private int[] knownCode() {
        int[] result = new int[MasterMindGame.CODE_LENGTH];
        for (int i = 0; i < result.length; i++) {
            int color = proveColor(i);
            if (color == -1) {
                return null;
            }
            result[i] = color;
        }
        return result;
    }

    public int[] makeMove(boolean isFinalMove) {
        int[] result = knownCode();
        if (result != null) {
            return result;
        }
        result = new int[MasterMindGame.CODE_LENGTH];
        Random r = new Random();
        //if (isFinalMove) {
        boolean[] knowColor = new boolean[MasterMindGame.CODE_LENGTH];
            for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
                int known = proveColor(i);
                if (known >= 0) {
                    result[i] = known;
                    knowColor[i] = true;
                    continue;
                }
                int believed = getBelievedColor(i);
                if (believed >= 0) {
                    if (isFinalMove || colorsHistory.isEmpty()) {
                        result[i] = believed;
                    }
                    else {
                        result[i] = r.nextInt(1, MasterMindGame.NUMBER_OF_COLORS+1);
                    }
                }
                else {
                    List<Integer> believedPossibilities = getPossibilities(i);
                    if (believedPossibilities.isEmpty() || (!isFinalMove && believedPossibilities.size() == 1)) {
                        result[i] = r.nextInt(1, MasterMindGame.NUMBER_OF_COLORS+1);
                    }
                    else {
                        result[i] = believedPossibilities.get(r.nextInt(believedPossibilities.size()));
                    }
                }
            }
            String string = "";
            for (int i : result) {
                string += i;
            }
            if (!isFinalMove && alreadyGuessed.contains(Integer.parseInt(string))) {
                result = getRandomGuess(result, knowColor);
            }
        //}
        //else {
            //result = getRandomGuess();
        //}
        String string2 = "";
        for (int i : result) {
            string2 += i;
        }
        alreadyGuessed.add(Integer.parseInt(string2));
        return result;
    }

    private int[] getRandomGuess(int[] currentGuess, boolean[] knowColor) {
        int[] result = currentGuess;
        Random r = new Random();
        for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
            if (knowColor[i]) {
                continue;
            }
            if (!notGuessedColors[i].isEmpty()) {
                int index = r.nextInt(notGuessedColors[i].size());
                result[i] = notGuessedColors[i].get(index);
                notGuessedColors[i].remove(index);
            }
            else {
                result[i] = r.nextInt(1, MasterMindGame.NUMBER_OF_COLORS+1);
            }
        }
        return result;
    }

    private int factorial(int n) {
        if (n == 0) {
            return 1;
        }
        return n*factorial(n-1);
    }

    private List<Formula>[] getKnowledge(int[] colors, int[] feedback, int[] indices) {
        int red = MasterMindGame.Color.RED.ordinal();
        int white = MasterMindGame.Color.WHITE.ordinal();
        List<Formula>[] result = new ArrayList[MasterMindGame.CODE_LENGTH];
        for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
            result[i] = new ArrayList<>();
            if (feedback[indices[i]] == red) {
                String formulaString = getPredicate(i, colors[i]) + AndFormula.operator;
                for (int j = 1; j <= MasterMindGame.NUMBER_OF_COLORS; j++) {
                    if (j != colors[i]) {
                        formulaString += NotFormula.operator+getPredicate(i, j);
                        formulaString += AndFormula.operator;
                    }
                }
                formulaString = formulaString.substring(0, formulaString.length()-1);
                result[i].add(Formula.parseString(formulaString));
            }
            else if (feedback[indices[i]] == white) {
                result[i].add(Formula.parseString(NotFormula.operator+getPredicate(i, colors[i])));
            }
            else {
                boolean hasWhite = false;
                for (int j = 0; j < MasterMindGame.CODE_LENGTH; j++) {
                    if (colors[j] == colors[i] && feedback[indices[j]] == white) {
                        hasWhite = true;
                        break;
                    }
                }
                if (hasWhite) {
                    result[i].add(Formula.parseString(NotFormula.operator+getPredicate(i, colors[i])));
                }
                else {
                    for (int j = 0; j < MasterMindGame.CODE_LENGTH; j++) {
                        if (feedback[indices[j]] != red) {
                            result[i].add(Formula.parseString(NotFormula.operator+getPredicate(j, colors[i])));
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<List<Formula>[]> getKnowledgeRecursive(List<List<Formula>[]> formulas, List<int[]> feedbackDistributions, int[] colors, int[] feedback, int[] indices, int index, List<Integer> alreadyTried) {
        for (int i = 0; i < indices.length; i++) {
            boolean alreadyContained = false;
            for (int j = 0; j < index; j++) {
                if (indices[j] == i) {
                    alreadyContained = true;
                    break;
                }
            }
            if (!alreadyContained) {
                indices[index] = i;
                if (index < indices.length-1) {
                    formulas = getKnowledgeRecursive(formulas, feedbackDistributions, colors, feedback, indices, index+1, alreadyTried);
                }
                else {
                    String hashValueString = "";
                    for (int j = 0; j < indices.length; j++) {
                        hashValueString += feedback[indices[j]];
                    }
                    int hashValue = Integer.parseInt(hashValueString);
                    if (alreadyTried.contains(hashValue)) {
                        continue;
                    }
                    alreadyTried.add(hashValue);
                    List<Formula>[] result = getKnowledge(colors, feedback, indices);
                    String formulaString = NotFormula.operator+"(";
                    for (int j = 0; j < result.length; j++) {
                        for (int k = 0; k < result[j].size(); k++) {
                            formulaString += result[j].get(k).prettyPrint();
                            if (k != result[j].size()-1) {
                                formulaString += AndFormula.operator;
                            }
                        }
                        if (j != result.length-1) {
                            formulaString += AndFormula.operator;
                        }
                    }
                    formulaString += ")";
                    if (!factBeliefBase.entailsFormula(Formula.parseString(formulaString))) {
                        //Formula is possible.
                        formulas.add(result);
                        int[] feedbackDistribution = new int[feedback.length];
                        for (int j = 0; j < feedback.length; j++) {
                            feedbackDistribution[j] = feedback[indices[j]];
                        }
                        feedbackDistributions.add(feedbackDistribution);
                    }
                }
            }
        }
        return formulas;
    }

    //Returns a color at a position if we know the color. Otherwise, returns -1.
    private int getBelievedColor(int position) {
        for (int i = 1; i <= MasterMindGame.NUMBER_OF_COLORS; i++) {
            if (beliefBase.entailsFormula(Formula.parseString(getPredicate(position, i)))) {
                return i;
            }
        }
        return -1;
    }

    //Get the colors that could possibly be at a position.
    private List<Integer> getPossibilities(int position) {
        List<Integer> possibilities = new ArrayList<>();
        for (int i = 1; i <= MasterMindGame.NUMBER_OF_COLORS; i++) {
            if (!beliefBase.entailsFormula(Formula.parseString("!"+getPredicate(position, i)))) {
                possibilities.add(i);
            }
        }
        return possibilities;
    }

    public void updateBeliefBase(int[] colors, int[] feedback) {
        colorsHistory.add(colors);
        feedbackHistory.add(feedback);

        int sizeBefore;
        do {
            sizeBefore = factBeliefBase.size();
            for (int i = 0; i < colorsHistory.size(); i++) {
                reviseBeliefBase(colorsHistory.get(i), feedbackHistory.get(i));
            }
        } while(sizeBefore != factBeliefBase.size());
    }

    private void reviseBeliefBase(int[] colors, int[] feedback) {
        int red = MasterMindGame.Color.RED.ordinal();
        int white = MasterMindGame.Color.WHITE.ordinal();
        int nReds = 0;
        int nWhites = 0;
        for (int item : feedback) {
            if (item == red) {
                nReds++;
            } else if (item == white) {
                nWhites++;
            }
        }
        if (nReds + nWhites == MasterMindGame.CODE_LENGTH) {
            List<Integer> colorsNotPresent = new ArrayList<>();
            for (int i = 1; i <= MasterMindGame.NUMBER_OF_COLORS; i++) {
                colorsNotPresent.add(i);
            }
            for (int i : colors) {
                colorsNotPresent.remove((Integer)i);
            }
            for (int i : colorsNotPresent) {
                for (int j = 0; j < MasterMindGame.CODE_LENGTH; j++) {
                    beliefBase.revisionMastermind(NotFormula.operator + getPredicate(j, i), FACT_PRIORITY);
                    factBeliefBase.revisionMastermind(NotFormula.operator + getPredicate(j, i), FACT_PRIORITY);
                }
            }
        }
        if (nReds == 0) {
            for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
                beliefBase.revisionMastermind(NotFormula.operator + getPredicate(i, colors[i]), FACT_PRIORITY);
                factBeliefBase.revisionMastermind(NotFormula.operator + getPredicate(i, colors[i]), FACT_PRIORITY);
            }
        }
        List<int[]> feedbackDistributions = new ArrayList<>();
        List<List<Formula>[]> result = getKnowledgeRecursive(new ArrayList<>(), feedbackDistributions, colors, feedback, new int[MasterMindGame.CODE_LENGTH], 0, new ArrayList<>());

        //Choose random possibility.
        int index = new Random().nextInt(result.size());
        List<Formula>[] choice = result.get(index);
        int priority;
        if (result.size() == 1) {
            priority = FACT_PRIORITY;
        }
        else {
            priority = factorial(MasterMindGame.CODE_LENGTH) - result.size();
        }
        //Add the formulas.
        for (int i = 0; i < choice.length; i++) {
            for (int j = 0; j < choice[i].size(); j++) {
                beliefBase.revisionMastermind(choice[i].get(j), priority);
                if (priority >= FACT_PRIORITY) {
                    factBeliefBase.revisionMastermind(choice[i].get(j), priority);
                }
            }
        }
    }

    private String getPredicate(int position, int color) {
        return "p"+position+"c"+color;
    }

}
