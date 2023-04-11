package formulas;

public class PredicateFormula extends Formula {
    protected String name;

    public PredicateFormula(String name) {
        this.name = name;
    }

    public PredicateFormula copy() {
        return new PredicateFormula(name);
    }

    public String prettyPrint() {
        return name;
    }

    public boolean equals(Object o) {
        return o instanceof PredicateFormula && ((PredicateFormula) o).name.equals(name);
    }

    public String toString() {
        return "Pred(" + name + ")";
    }
}
