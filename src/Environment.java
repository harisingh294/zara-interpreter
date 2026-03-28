import java.util.HashMap;
import java.util.Map;


public class Environment {

    // The internal map: variable name -> current value (Double or String)
    private final Map<String, Object> store = new HashMap<>();

    /** Store or update a variable's value. */
    public void set(String name, Object value) {
        store.put(name, value);
    }

    /**
     * Retrieve a variable's current value.
     * Throws RuntimeException with a helpful message if undefined.
     */
    public Object get(String name) {
        if (!store.containsKey(name)) {
            throw new RuntimeException(
                "Variable not defined: \"" + name + "\". " +
                "Did you forget   set " + name + " = ...   before using it?");
        }
        return store.get(name);
    }

    @Override
    public String toString() {
        return "Environment" + store.toString();
    }
}
