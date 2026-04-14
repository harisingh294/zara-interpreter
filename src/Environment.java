import java.util.HashMap;
import java.util.Map;
public class Environment {

    
    private final Map<String, Object> store = new HashMap<>();

    // Store or update a variable value
    public void set(String name, Object value) {
        store.put(name, value);
    }

    // Retrieve a variable value
    public Object get(String name) {
        if (!store.containsKey(name)) {
            throw new RuntimeException(
                "Variable not defined: \"" + name + "\". " +
                "Did you forget to write   set " + name + " = ...   first?");
        }
        return store.get(name);
    }

    // Check if a variable has been defined
    public boolean isDefined(String name) {
        return store.containsKey(name);
    }

    // Useful for debugging — prints all current variable values
    @Override
    public String toString() {
        return "Environment" + store;
    }
}
