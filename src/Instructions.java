import java.util.List;

//Instruction interface
interface Instruction {
    /**
     * Execute this instruction.
     * @param env  The shared variable store — read/write variables here
     */
    void execute(Environment env);
}

//AssignInstruction
class AssignInstruction implements Instruction {
    private final String variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression   = expression;
    }

    @Override
    public void execute(Environment env) {
        Object value = expression.evaluate(env); // Compute right-hand side
        env.set(variableName, value);             // Store in variable store
    }
}

//PrintInstruction
class PrintInstruction implements Instruction {
    private final Expression expression;

    public PrintInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        Object value = expression.evaluate(env);

        if (value instanceof Double) {
            double d = (Double) value;
            // If it's a whole number (16.0, 1.0, 4.0), print without decimal
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                System.out.println((long) d);
                return;
            }
        }
        System.out.println(value); // Strings and non-whole doubles print as-is
    }
}

//IfInstruction
class IfInstruction implements Instruction {
    private final Expression condition;
    private final List<Instruction> thenBody; // Run if condition is true
    private final List<Instruction> elseBody; // Run if condition is false (may be empty)

    // Constructor WITHOUT else
    public IfInstruction(Expression condition, List<Instruction> thenBody) {
        this(condition, thenBody, new java.util.ArrayList<>()); // empty else body
    }

    // Constructor WITH else (for extension feature)
    public IfInstruction(Expression condition,
                         List<Instruction> thenBody,
                         List<Instruction> elseBody) {
        this.condition = condition;
        this.thenBody  = thenBody;
        this.elseBody  = elseBody;
    }

    @Override
    public void execute(Environment env) {
        Object result = condition.evaluate(env);

        if (result instanceof Boolean && (Boolean) result) {
            // Condition is TRUE — run the then-block
            for (Instruction inst : thenBody) inst.execute(env);
        } else {
            // Condition is FALSE — run else-block (will be empty if no else)
            for (Instruction inst : elseBody) inst.execute(env);
        }
    }
}

//RepeatInstruction
class RepeatInstruction implements Instruction {
    private final int count;
    private final List<Instruction> body;

    public RepeatInstruction(int count, List<Instruction> body) {
        this.count = count;
        this.body  = body;
    }

    @Override
    public void execute(Environment env) {
        for (int i = 0; i < count; i++) {
            // Run every instruction in the body, once per iteration
            for (Instruction inst : body) {
                inst.execute(env);
            }
        }
    }
}
