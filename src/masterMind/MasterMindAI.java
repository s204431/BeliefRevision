package masterMind;

import formulas.AndFormula;
import formulas.Formula;
import formulas.NotFormula;
import formulas.OrFormula;
import main.BeliefBase;
import main.Main;

import java.util.*;

public class MasterMindAI {

    private BeliefBase beliefBase;
    private List<Integer>[] notGuessedColors = new ArrayList[MasterMindGame.CODE_LENGTH]; //Colors not yet guessed for each position.
    private static final int FACT_PRIORITY = 100000;
    private List<Integer> alreadyGuessed = new ArrayList<>();

    public MasterMindAI(BeliefBase beliefBase) {
        this.beliefBase = beliefBase;
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
            beliefBase.revision(rule, FACT_PRIORITY);
        }
        //Initial guess.
        beliefBase.revision(getPredicate(0, 1), 0);
        beliefBase.revision(getPredicate(1, 1), 0);
        beliefBase.revision(getPredicate(2, 2), 0);
        beliefBase.revision(getPredicate(3, 2), 0);
    }

    public int[] makeMove(boolean isFinalMove) {
        done = false;
        int[] result = new int[MasterMindGame.CODE_LENGTH];
        Random r = new Random();
        //if (isFinalMove) {
            for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
                int known = getKnownColor(i);
                if (known >= 0) {
                    if (isFinalMove) {
                        result[i] = known;
                    }
                    else {
                        result[i] = r.nextInt(1, MasterMindGame.NUMBER_OF_COLORS+1);
                    }
                }
                else {
                    List<Integer> possibilities = getPossibilities(i);
                    if (possibilities.isEmpty() || (!isFinalMove && possibilities.size() == 1)) {
                        result[i] = r.nextInt(1, MasterMindGame.NUMBER_OF_COLORS+1);
                    }
                    else {
                        result[i] = possibilities.get(r.nextInt(possibilities.size()));
                    }
                }
            }
            String string = "";
            for (int i : result) {
                string += i;
            }
            if (!isFinalMove && alreadyGuessed.contains(Integer.parseInt(string))) {
                result = getRandomGuess();
            }
        //}
        /*else {
            result = getRandomGuess();
        }*/
        String string2 = "";
        for (int i : result) {
            string2 += i;
        }
        alreadyGuessed.add(Integer.parseInt(string2));
        return result;
    }

    private int[] getRandomGuess() {
        int[] result = new int[MasterMindGame.CODE_LENGTH];
        Random r = new Random();
        for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
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

    private int choose(int n, int k) {
        return factorial(n)/(factorial(n-k)*factorial(k));
    }

    private void getKnowledge(int[] colors, int[] feedback) {
        int red = MasterMindGame.Color.RED.ordinal();
        int white = MasterMindGame.Color.WHITE.ordinal();
        int nReds = 0;
        int nWhites = 0;
        List<Integer> feedbackList = new ArrayList<>();
        for (int item : feedback) {
            if (item == red) {
                nReds++;
            } else if (item == white) {
                nWhites++;
            }
            feedbackList.add(item);
        }
        int priority = factorial(MasterMindGame.CODE_LENGTH) - choose(MasterMindGame.CODE_LENGTH, nReds)*choose(MasterMindGame.CODE_LENGTH-nReds, nWhites);
        if (priority == factorial(MasterMindGame.CODE_LENGTH)-1) {
            priority = FACT_PRIORITY;
        }
        else {
            priority += nReds*2+nWhites;
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
                    beliefBase.revision(NotFormula.operator + getPredicate(j, i), FACT_PRIORITY);
                }
            }
        }
        Collections.shuffle(feedbackList);
        if (nReds == 0) {
            for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
                beliefBase.revision(NotFormula.operator+getPredicate(i, colors[i]), FACT_PRIORITY);
            }
        }
        for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
            if (feedbackList.get(i) == red) {
                //beliefBase.revision(Formula.parseString(getPredicate(j, colors[j])), priority);
                String formulaString = getPredicate(i, colors[i]) + AndFormula.operator;
                for (int j = 1; j <= MasterMindGame.NUMBER_OF_COLORS; j++) {
                    if (j != colors[i]) {
                        formulaString += NotFormula.operator+getPredicate(i, j);
                        formulaString += AndFormula.operator;
                    }
                }
                formulaString = formulaString.substring(0, formulaString.length()-1);
                beliefBase.revision(formulaString, priority);
                for (int j = 0; j < MasterMindGame.CODE_LENGTH; j++) {
                    if (j != i && feedbackList.get(j) != red) {
                        beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(j, colors[i])), priority);
                    }
                }
            }
            else if (feedbackList.get(i) == white) {
                String formulaString = "";
                beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(i, colors[i])), priority);
                for (int j = 0; j < MasterMindGame.CODE_LENGTH; j++) {
                    if (j != i && feedbackList.get(j) != red) {
                        formulaString += getPredicate(j, colors[i])+OrFormula.operator;
                    }
                }
                formulaString = formulaString.substring(0, formulaString.length()-1);
                //beliefBase.revision(formulaString, priority);
            }
            else {
                for (int j = 0; j < MasterMindGame.CODE_LENGTH; j++) {
                    if (feedbackList.get(j) != red) {
                        beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(j, colors[i])), priority);
                    }
                }
            }
        }
    }


    boolean done = false;
    private String getKnowledgeRecursive(String formulaString, int[] colors, int[] feedback, int[] indices, int index, List<Integer> alreadyTried) {
        if (done) {
            return formulaString;
        }
        int red = MasterMindGame.Color.RED.ordinal();
        int white = MasterMindGame.Color.WHITE.ordinal();
        int nReds = 0;
        int nWhites = 0;
        for (int i = 0; i < feedback.length; i++) {
            if (feedback[i] == red) {
                nReds++;
            }
            else if (feedback[i] == white) {
                nWhites++;
            }
        }
        int priority = factorial(MasterMindGame.CODE_LENGTH) - choose(MasterMindGame.CODE_LENGTH, nReds)*choose(MasterMindGame.CODE_LENGTH-nReds, nWhites);
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
                    formulaString = getKnowledgeRecursive(formulaString, colors, feedback, indices, index+1, alreadyTried);
                    return formulaString;
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
                    formulaString = "";
                    if (formulaString.equals("")) {
                        formulaString += "(";
                    }
                    else {
                        formulaString += OrFormula.operator+"(";
                    }
                    for (int j = 0; j < MasterMindGame.CODE_LENGTH; j++) {
                        formulaString = "";
                        if (feedback[indices[j]] == red) {
                            formulaString += getPredicate(j, colors[j])+AndFormula.operator;
                            beliefBase.revision(Formula.parseString(getPredicate(j, colors[j])), priority);
                            for (int k = 0; k < MasterMindGame.CODE_LENGTH; k++) {
                                if (k != j && feedback[indices[k]] != red) {
                                    beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(k, colors[j])), priority);
                                    formulaString += NotFormula.operator+getPredicate(k, colors[j])+AndFormula.operator;
                                }
                            }
                            formulaString = formulaString.substring(0, formulaString.length()-1);
                        }
                        else if (feedback[indices[j]] == white) {
                            //formulaString += NotFormula.operator+getPredicate(j, colors[j])+AndFormula.operator+"(";
                            formulaString = "";
                            beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(j, colors[j])), priority);
                            for (int k = 0; k < MasterMindGame.CODE_LENGTH; k++) {
                                if (k != j && feedback[indices[k]] != red) {
                                    //beliefBase.revision(Formula.parseString(getPredicate(k, colors[j])), priority);
                                    formulaString += getPredicate(k, colors[j])+OrFormula.operator;
                                }
                            }
                            formulaString = formulaString.substring(0, formulaString.length()-1);
                            beliefBase.revision(formulaString, priority);
                        }
                        else {
                            formulaString += NotFormula.operator+getPredicate(j, colors[j])+AndFormula.operator;
                            //beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(j, colors[j])), priority);
                            for (int k = 0; k < MasterMindGame.CODE_LENGTH; k++) {
                                if (feedback[indices[k]] != red) {
                                    beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(k, colors[j])), priority);
                                    formulaString += NotFormula.operator+getPredicate(k, colors[j])+AndFormula.operator;
                                }
                            }
                            formulaString = formulaString.substring(0, formulaString.length()-1);
                        }
                        //formulaString += AndFormula.operator;
                        //beliefBase.revision(Formula.parseString(formulaString));
                    }
                    formulaString += ")";
                    //formulaString = formulaString.substring(0, formulaString.length()-1)+")";
                    done = true;
                }
            }
        }
        return formulaString;
    }

    //Returns a color at a position if we know the color. Otherwise, returns -1.
    private int getKnownColor(int position) {
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
        //String formulaString = getKnowledgeRecursive("", colors, feedback, new int[MasterMindGame.CODE_LENGTH], 0, new ArrayList<>());
        getKnowledge(colors, feedback);
    }

    private String getPredicate(int position, int color) {
        return "p"+position+"c"+color;
    }

}
