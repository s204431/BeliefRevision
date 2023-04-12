package masterMind;

import formulas.AndFormula;
import formulas.Formula;
import formulas.NotFormula;
import formulas.OrFormula;
import main.BeliefBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MasterMindAI {

    private BeliefBase beliefBase;
    private List<Integer>[] notGuessedColors = new ArrayList[MasterMindGame.CODE_LENGTH]; //Colors not yet guessed for each position.

    public MasterMindAI(BeliefBase beliefBase) {
        this.beliefBase = beliefBase;
        for (int i = 0; i < notGuessedColors.length; i++) {
            notGuessedColors[i] = new ArrayList<>();
            for (int j = 1; j <= MasterMindGame.NUMBER_OF_COLORS; j++) {
                notGuessedColors[i].add(j);
            }
        }
    }

    public int[] makeMove(boolean isFinalMove) {
        done = false;
        int[] result = new int[MasterMindGame.CODE_LENGTH];
        Random r = new Random();
        if (isFinalMove) {
            for (int i = 0; i < MasterMindGame.CODE_LENGTH; i++) {
                int known = getKnownColor(i);
                if (known >= 0) {
                    result[i] = known;
                }
                else {
                    List<Integer> possibilities = getPossibilities(i);
                    if (possibilities.isEmpty()) {
                        result[i] = r.nextInt(1, MasterMindGame.NUMBER_OF_COLORS+1);
                    }
                    else {
                        result[i] = possibilities.get(r.nextInt(possibilities.size()));
                    }
                }
            }
        }
        else {
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
        }
        return result;
    }

    boolean done = false;
    private String getKnowledgeRecursive(String formulaString, int[] colors, int[] feedback, int[] indices, int index, List<Integer> alreadyTried) {
        if (done) {
            return formulaString;
        }
        int red = MasterMindGame.Color.RED.ordinal();
        int white = MasterMindGame.Color.WHITE.ordinal();
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
                            beliefBase.revision(Formula.parseString(getPredicate(j, colors[j])));
                            for (int k = 0; k < MasterMindGame.CODE_LENGTH; k++) {
                                if (k != j && feedback[indices[k]] != red) {
                                    beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(k, colors[j])));
                                    formulaString += NotFormula.operator+getPredicate(k, colors[j])+AndFormula.operator;
                                }
                            }
                            formulaString = formulaString.substring(0, formulaString.length()-1);
                        }
                        else if (feedback[indices[j]] == white) {
                            formulaString += NotFormula.operator+getPredicate(j, colors[j])+AndFormula.operator+"(";
                            beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(j, colors[j])));
                            for (int k = 0; k < MasterMindGame.CODE_LENGTH; k++) {
                                if (k != j && feedback[indices[k]] != red) {
                                    beliefBase.revision(Formula.parseString(getPredicate(k, colors[j])));
                                    formulaString += getPredicate(k, colors[j])+OrFormula.operator;
                                }
                            }
                            formulaString = formulaString.substring(0, formulaString.length()-1)+")";
                        }
                        else {
                            formulaString += NotFormula.operator+getPredicate(j, colors[j])+AndFormula.operator;
                            beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(j, colors[j])));
                            for (int k = 0; k < MasterMindGame.CODE_LENGTH; k++) {
                                if (feedback[indices[k]] != red) {
                                    beliefBase.revision(Formula.parseString(NotFormula.operator+getPredicate(k, colors[j])));
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
        String formulaString = getKnowledgeRecursive("", colors, feedback, new int[MasterMindGame.CODE_LENGTH], 0, new ArrayList<>());
        //beliefBase.revision(Formula.parseString(formulaString));
        /*for (int i = 0; i < feedback.length; i++) {
            if (feedback[i] == MasterMindGame.Color.RED.ordinal()) {
                String formulaString = "";
               for (int j = 0; j < MasterMindGame.CODE_LENGTH; j++) {
                   formulaString += getPredicate(j, colors[j]);
                   if (j != MasterMindGame.CODE_LENGTH-1) {
                       formulaString += OrFormula.operator;
                   }
               }
               beliefBase.expansion(Formula.parseString(formulaString));
               break;
            }
            else if (feedback[i] == MasterMindGame.Color.WHITE.ordinal()) {

            }
        }*/
    }

    private String getPredicate(int position, int color) {
        return "p"+position+"c"+color;
    }

}
