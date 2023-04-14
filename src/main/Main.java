package main;

import formulas.*;
import masterMind.MasterMindGame;
import masterMind.MasterMindUI;

public class Main {

    public static void main(String[] args) {
        BeliefBase beliefBase = new BeliefBase();
        String formula = "(p->q)<->(!q->!p)";
        //String formula = "((p & q) -> r) & (p -> !r) & (q -> !r) -> !r";
        Formula parsed = Formula.parseString(formula);
        System.out.println("Parsing formula: " + formula);
        System.out.println();
        System.out.println("Internal representation: " + parsed);
        System.out.println("Pretty print: " + parsed.prettyPrint());
        System.out.println("Is valid: " + beliefBase.resolution(parsed));
        System.out.println(beliefBase.entailment(Formula.parseString("p & q"), Formula.parseString("p | q")));
        /*beliefBase.revision("p");
        beliefBase.revision("q");
        beliefBase.revision("p -> q");*/
        System.out.println(beliefBase);
        UI ui = new UI(beliefBase);

        MasterMindGame masterMind = new MasterMindGame();
        new MasterMindUI(masterMind, beliefBase, ui);
    }
}