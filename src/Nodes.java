class NumberNode implements Expression {
    private final double value;

    public NumberNode(double value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value; // Just return the number — nothing to compute
    }
}


class StringNode implements Expression {
    private final String value;

    public StringNode(String value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}

class VariableNode implements Expression {
    private final String name; // e.g. "x", "score"

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public Object evaluate(Environment env) {
        // If variable was never set, Environment throws a RuntimeException
        return env.get(name);
    }
}


class BinaryOpNode implements Expression {
    private final Expression left;
    private final String op;         // "+", "-", "*", "/", ">", "<", "=="
    private final Expression right;

    public BinaryOpNode(Expression left, String op, Expression right) {
        this.left  = left;
        this.op    = op;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) {
        // Always evaluate BOTH sides first
        Object leftVal  = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        switch (op) {

            case "+":
                // Special case: if either side is String, concatenate
                if (leftVal instanceof String || rightVal instanceof String)
                    return leftVal.toString() + rightVal.toString();
                return toDouble(leftVal) + toDouble(rightVal);

            case "-": return toDouble(leftVal) - toDouble(rightVal);
            case "*": return toDouble(leftVal) * toDouble(rightVal);
            case "/":
                if (toDouble(rightVal) == 0)
                    throw new RuntimeException("Division by zero");
                return toDouble(leftVal) / toDouble(rightVal);

            // ── Comparison — returns Boolean ─────────────────
            case ">":  return toDouble(leftVal) >  toDouble(rightVal);
            case "<":  return toDouble(leftVal) <  toDouble(rightVal);
            case "==": return leftVal.equals(rightVal);

            default:
                throw new RuntimeException("Unknown operator: " + op);
        }
    }

    // Helper: safely convert Object to double, with a clear error message
    private double toDouble(Object val) {
        if (val instanceof Double) return (Double) val;
        throw new RuntimeException(
            "Expected a number but got: \"" + val + "\" (type: "
            + val.getClass().getSimpleName() + ")");
    }
}
