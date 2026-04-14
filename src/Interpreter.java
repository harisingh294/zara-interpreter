import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Interpreter {

    // Runs a ZARA program given as a source code String
    public void run(String sourceCode) {

        try {
            // ========================= FIX START =========================
            // Wrapped entire execution pipeline in try-catch
            // to prevent runtime crash and show clean error message
            // ========================= FIX END ===========================

            // Stage 1: Tokenize
            List<Token> tokens = new Tokenizer(sourceCode).tokenize();

            // Stage 2: Parse
            Parser parser = new Parser(tokens);
            parser.setSource(sourceCode);   // needed for indentation block detection
            List<Instruction> instructions = parser.parse();

            // Stage 3: Execute
            Environment env = new Environment();

            // ========================= FIX START =========================
            // If any instruction throws RuntimeException (like undefined variable),
            // it will be caught and handled instead of crashing the program
            // ========================= FIX END ===========================

            instructions.forEach(inst -> inst.execute(env));

        } catch (RuntimeException e) {

            // ========================= FIX START =========================
            // Clean error message instead of full stack trace
            // ========================= FIX END ===========================

            System.err.println("Error: " + e.getMessage());
        }
    }

    // Entry point — reads a .zara file and runs it
    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("No file given. Running built-in demo...\n");
            runAllDemos();
            return;
        }

        // ========================= OPTIONAL IMPROVEMENT =========================
        // More modern way (optional):
        // String sourceCode = Files.readString(Paths.get(args[0]));
        // =========================================================================

        String sourceCode = new String(Files.readAllBytes(Paths.get(args[0])));

        new Interpreter().run(sourceCode);
    }

    private static void runAllDemos() {
        Interpreter interp = new Interpreter();

        System.out.println("=== Program 1: Arithmetic (expected: 16) ===");
        interp.run("set x = 10\nset y = 3\nset result = x + y * 2\nshow result\n");

        System.out.println("\n=== Program 2: Strings ===");
        interp.run("set name = \"Sitare\"\nshow name\nshow \"Hello from ZARA\"\n");

        System.out.println("\n=== Program 3: Conditional (expected: Pass) ===");
        interp.run("set score = 85\nwhen score > 50:\n    show \"Pass\"\n");

        System.out.println("\n=== Program 4: Loop (expected: 1 2 3 4) ===");
        interp.run("set i = 1\nloop 4:\n    show i\n    set i = i + 1\n");
    }
}
