public class AssignInstruction implements Instruction {

    private final String     variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression   = expression;
    }

    @Override
    public void execute(Environment env) {
        Object value = expression.evaluate(env);  // compute right-hand side
        env.set(variableName, value);              // store in variable notebook
    }
}
