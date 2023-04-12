package formulas;

public class NotFormula extends Formula {
    public static String operator = "!";
    protected static int precedence = 4;
    protected static Associativity associativity = Associativity.right;

    protected NotFormula() {

    }

    public NotFormula(Formula o) {
        operands = new Formula[] {o};
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
        return new NotFormula(Formula.parseString(expression.substring(operatorIndex+operator.length())));
    }

    public boolean equals(Object o) {
        return o instanceof NotFormula && ((NotFormula) o).operands[0].equals(operands[0]);
    }

    public NotFormula copy() {
        return new NotFormula(operands[0].copy());
    }

    public String toString() {
        return "Not(" + operands[0] + ")";
    }
}
