package formulas;

import java.util.ArrayList;
import java.util.List;

public abstract class Formula {
    public enum Associativity {left, right};
    private static Formula[] allOperators = {new AndFormula(), new OrFormula(), new BiconditionFormula(), new ImpliesFormula(), new NotFormula()};
    public static String operator; //The string representation of the operator.
    protected static int precedence; //The precedence of the operator.
    protected static int priority = 0; //Priority of operator for belief base.
    protected static Associativity associativity = Associativity.left; //Use left associativity by default.
    public Formula[] operands;

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
        return null;
    }

    //Parses a string to a formula.
    public static Formula parseString(String formula) throws IllegalArgumentException {
        try {
            return parseStringRecursive(formula.replace(" ", ""));
        } catch(Exception e) {
            throw new IllegalArgumentException("Syntax error.");
        }
    }

    //Recursively parses a string to a formula.
    protected static Formula parseStringRecursive(String expression) throws IllegalArgumentException {
        if (expression.charAt(0) == '(' && expression.charAt(expression.length()-1) == ')') {
            int count = 1;
            boolean closed = false;
            for (int i = 1; i < expression.length()-1; i++) {
                if (expression.charAt(i) == '(') {
                    count++;
                }
                else if (expression.charAt(i) == ')') {
                    count--;
                }
                if (count == 0) {
                    closed = true;
                    break;
                }
            }
            if (!closed) {
                return parseStringRecursive(expression.substring(1, expression.length()-1));
            }
        }
        String processed = expression;
        int pIndex = processed.indexOf('(');
        List<int[]> removedParts = new ArrayList<int[]>();
        while (pIndex >= 0) {
            int count1 = 1;
            int count2 = 0;
            int index = pIndex+1;
            while (count1 > count2) {
                if (processed.charAt(index) == '(') {
                    count1++;
                }
                else if (processed.charAt(index) == ')') {
                    count2++;
                }
                index++;
            }
            removedParts.add(new int[] {pIndex, index-1});
            String sub = processed.substring(index);
            int newIndex = sub.indexOf('(');
            if (newIndex < 0) {
                break;
            }
            pIndex = newIndex + processed.length() - sub.length();
        }
        int removedLength = 0;
        for (int[] part : removedParts) {
            processed = processed.substring(0, part[0]-removedLength) + processed.substring(part[1]+1-removedLength);
            removedLength += part[1] - part[0] + 1;
        }
        if (processed.contains(")")) {
            throw new IllegalArgumentException();
        }
        //Find lowest precedence
        int lowestPrecedence = Integer.MAX_VALUE;
        Formula currentOperator = null;
        for (Formula f : allOperators) {
            String o = f.getOperator();
            if (f.getPrecedence() < lowestPrecedence && processed.contains(o)) {
                lowestPrecedence = f.getPrecedence();
                currentOperator = f;
            }
        }
        if (currentOperator == null) { //Only predicate is left.
            return new PredicateFormula(processed);
        }
        int operatorIndex;
        //Use correct associativity
        if (currentOperator.getAssociativity() == Associativity.left) {
            operatorIndex = processed.lastIndexOf(currentOperator.getOperator());
        }
        else {
            operatorIndex = processed.indexOf(currentOperator.getOperator());
        }
        for (int[] part : removedParts) {
            if (part[0] <= operatorIndex) {
                operatorIndex += part[1] - part[0] + 1;
            }
            else {
                break;
            }
        }
        return currentOperator.process(operatorIndex, expression);
    }

    public Formula copy() {
        return null;
    }

    public int getOperatorPriority() {
        return priority;
    }

    public int getFormulaPriority() {
        return getFormulaPriorityRecursive()+1;
    }

    private int getFormulaPriorityRecursive() {
        if (operands == null) {
            return 0;
        }
        int sum = 0;
        for (Formula formula : operands) {
            sum += formula.getFormulaPriorityRecursive();
        }
        return sum+getOperatorPriority();
    }

    public String prettyPrint() {
        if (operands.length == 1) {
            return "(" + getOperator() + operands[0].prettyPrint() + ")";
        }
        return "(" + operands[0].prettyPrint() + " " + getOperator() + " " + operands[1].prettyPrint() + ")";
    }

    public boolean equals(Object o) {
        return o instanceof Formula && ((Formula) o).toString().equals(toString());
    }
}
