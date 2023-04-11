import formulas.*;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        BeliefBase beliefBase = new BeliefBase();
        String formula = "(((p & q) -> r) -> ((p -> r) | (q -> r)))";
        //String formula = "((p & q) -> r) & (p -> !r) & (q -> !r) -> !r";
        Formula parsed = Formula.parseString(formula);
        System.out.println("Parsing formula: " + formula);
        System.out.println();
        System.out.println("Internal representation: " + parsed);
        System.out.println("Pretty print: " + parsed.prettyPrint());
        System.out.println("Is valid: " + beliefBase.resolution(parsed));
        System.out.println(beliefBase.entailment(Formula.parseString("p & q"), Formula.parseString("p | q")));
        beliefBase.revision("p");
        beliefBase.revision("q");
        beliefBase.revision("p -> q");
        System.out.println(beliefBase);
        new UI(beliefBase);
    }
}