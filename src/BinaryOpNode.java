import java.util.Map;
import java.util.function.BiFunction;
// This is a simple generic — left value, right value → result
public class BinaryOpNode implements Expression {
    // Each lambda is a BiFunction — takes left and right values, returns result
    private static final Map<String, BiFunction<Object, Object, Object>> OPS = Map.of(
        "+",  (l, r) -> isStringConcatenation(l, r)
                        ? l.toString() + r.toString()        
                        : toDouble(l) + toDouble(r),         
        "-",  (l, r) -> toDouble(l) - toDouble(r),
        "*",  (l, r) -> toDouble(l) * toDouble(r),
        "/",  (l, r) -> toDouble(l) / toDouble(r),
        ">",  (l, r) -> toDouble(l) >  toDouble(r),          // returns Boolean
        "<",  (l, r) -> toDouble(l) <  toDouble(r),          // returns Boolean
        "==", (l, r) -> l.equals(r)                          // returns Boolean
    );

    private final Expression left;
    private final String     op;
    private final Expression right;

    public BinaryOpNode(Expression left, String op, Expression right) {
        this.left  = left;
        this.op    = op;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) {
        // Step 1 — evaluate both sides of the expression first
        Object leftVal  = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        // Step 2 — look up the operator in the OPS map and apply it
        BiFunction<Object, Object, Object> operation = OPS.get(op);
        if (operation == null) {
            throw new RuntimeException("Unknown operator: " + op);
        }
        return operation.apply(leftVal, rightVal);
    }

    // true if either side is a String (triggers string concatenation for +)
    private static boolean isStringConcatenation(Object l, Object r) {
        return l instanceof String || r instanceof String;
    }

    // safely converts Object to double — gives clear error if not a number
    private static double toDouble(Object val) {
        if (val instanceof Double) return (Double) val;
        throw new RuntimeException(
            "Expected a number but got: \"" + val + "\" (" + val.getClass().getSimpleName() + ")");
    }
}
