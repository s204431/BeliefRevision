package formulas;

public class OrFormula extends Formula {
    public static String operator = "|";
    protected static int precedence = 2;
    protected static int priority = 1;

    protected OrFormula() {

    }

    public OrFormula(Formula o1, Formula o2) {
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
        return new OrFormula(f1, f2);
    }

    public OrFormula copy() {
        return new OrFormula(operands[0].copy(), operands[1].copy());
    }

    public int getOperatorPriority() {
        return priority;
    }

    public String toString() {
        return "Or(" + operands[0] + ", " + operands[1] + ")";
    }
}
