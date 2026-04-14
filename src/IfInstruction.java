import java.util.Collections;
import java.util.List;

public class IfInstruction implements Instruction {

    private final Expression        condition;
    private final List<Instruction> thenBody;  // run if condition is true
    private final List<Instruction> elseBody;  // run if condition is false

    // Constructor without else — elseBody is empty
    public IfInstruction(Expression condition, List<Instruction> thenBody) {
        this(condition, thenBody, Collections.emptyList());
    }

    // Constructor with else
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
        boolean conditionIsTrue = result instanceof Boolean && (Boolean) result;

        // pick the right branch, then run it using forEach + lambda
        List<Instruction> branch = conditionIsTrue ? thenBody : elseBody;
        branch.forEach(inst -> inst.execute(env));  // .forEach() — terminal operation
    }
}
