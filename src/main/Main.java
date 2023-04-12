package main;

import formulas.*;
import masterMind.MasterMindGame;
import masterMind.MasterMindUI;

public class Main {

    public static void main(String[] args) {
        BeliefBase beliefBase = new BeliefBase();
        String formula = "(((((((((((((((((((((!p1c4) & p0c4) & p2c4) & p3c4) & (!p0c3)) & (!p1c3)) & (!p2c3)) & (!p3c3)) & (!p0c5)) & p1c5) & p2c5) & p3c5) & (!p0c2)) & (!p1c2)) & (!p2c2)) & (!p3c2)) & (!p0c1)) & (!p1c1)) & (!p2c1)) & (!p3c1)) -> p3c4)";
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