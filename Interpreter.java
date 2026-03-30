import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Interpreter — the entry point. Wires all three pipeline stages together.
 *
 * HOW TO COMPILE AND RUN:
 *   Step 1 — Open a terminal/command prompt in the zara/ folder
 *
 *   Step 2 — Compile:
 *     Windows:   javac src\*.java -d out
 *     Mac/Linux: mkdir -p out && javac src/*.java -d out
 *
 *   Step 3 — Run a .zara file:
 *     Windows:   java -cp out Interpreter samples\program1.zara
 *     Mac/Linux: java -cp out Interpreter samples/program1.zara
 *
 *   Step 4 — Run built-in demo (no file needed):
 *     java -cp out Interpreter
 */
public class Interpreter {

    /**
     * Run a ZARA program from a source code String.
     * This is the heart of the project — three clean stages.
     */
    public void run(String sourceCode) {

        // Stage 1: TOKENIZE
        // "set x = 10" -> [SET, IDENTIFIER:x, EQUALS, NUMBER:10, NEWLINE]
        Tokenizer tokenizer = new Tokenizer(sourceCode);
        List<Token> tokens = tokenizer.tokenize();

        // Stage 2: PARSE
        // [SET, IDENTIFIER:x, EQUALS, NUMBER:10] -> AssignInstruction("x", NumberNode(10))
        Parser parser = new Parser(tokens);
        parser.setSource(sourceCode); // needed for indentation block detection
        List<Instruction> instructions = parser.parse();

        // Stage 3: EXECUTE
        // Run each instruction in order, sharing one Environment (variable store)
        Environment env = new Environment();
        for (Instruction instruction : instructions) {
            instruction.execute(env);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("No file given. Running built-in demo...\n");
            runAllDemos();
            return;
        }
        String sourceCode = new String(Files.readAllBytes(Paths.get(args[0])));
        new Interpreter().run(sourceCode);
    }

    private static void runAllDemos() {
        Interpreter interp = new Interpreter();

        System.out.println("=== Program 1: Arithmetic (expected: 16) ===");
        interp.run("set x = 10\nset y = 3\nset result = x + y * 2\nshow result\n");

        System.out.println("\n=== Program 2: Strings (expected: Sitare / Hello from ZARA) ===");
        interp.run("set name = \"Sitare\"\nshow name\nshow \"Hello from ZARA\"\n");

        System.out.println("\n=== Program 3: Conditional (expected: Pass) ===");
        interp.run("set score = 85\nwhen score > 50:\n    show \"Pass\"\n");

        System.out.println("\n=== Program 4: Loop (expected: 1 2 3 4) ===");
        interp.run("set i = 1\nloop 4:\n    show i\n    set i = i + 1\n");
    }
}
