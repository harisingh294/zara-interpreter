import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Interpreter {

    public void run(String sourceCode) {

        // Stage 1: TOKENIZE
        Tokenizer tokenizer = new Tokenizer(sourceCode);
        List<Token> tokens = tokenizer.tokenize();

        // Stage 2: PARSE
        Parser parser = new Parser(tokens);
        parser.setSource(sourceCode); // needed for indentation block detection
        List<Instruction> instructions = parser.parse();

        // Stage 3: EXECUTE
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
