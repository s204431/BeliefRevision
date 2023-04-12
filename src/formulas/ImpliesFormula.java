package formulas;

public class ImpliesFormula extends Formula {
    public static String operator = "->";
    protected static int precedence = 1;
    protected static Associativity associativity = Associativity.right;

    protected ImpliesFormula() {

    }

    public ImpliesFormula(Formula o1, Formula o2) {
        operands = new Formula[] {o1, o2};
    }

    public String getOperator() {
        return operator;
    }

    public int getPrecedence() {
        return precedence;
    }

    public Associativity getAssociativity() {
        return associativity;
    }

    protected Formula process(int operatorIndex, String expression) {
        Formula f1 = Formula.parseString(expression.substring(0, operatorIndex));
        Formula f2 = Formula.parseString(expression.substring(operatorIndex+operator.length()));
        return new ImpliesFormula(f1, f2);
    }

    public ImpliesFormula copy() {
        return new ImpliesFormula(operands[0].copy(), operands[1].copy());
    }

    public String toString() {
        return "Implies(" + operands[0] + ", " + operands[1] + ")";
    }
}
