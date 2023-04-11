package formulas;

public class BiconditionFormula extends Formula {
    protected static String operator = "<->";
    protected static int precedence = 0;

    protected BiconditionFormula() {

    }

    public BiconditionFormula(Formula o1, Formula o2) {
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
        return new BiconditionFormula(f1, f2);
    }

    public BiconditionFormula copy() {
        return new BiconditionFormula(operands[0].copy(), operands[1].copy());
    }

    public String toString() {
        return "Bicondition(" + operands[0] + ", " + operands[1] + ")";
    }
}
