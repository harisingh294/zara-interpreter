import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Parser {

    private final List<Token> tokens;   // token list from Tokenizer
    private int current;                // index of token we are currently looking at

   
    private Set<Integer> indentedLines;

    public Parser(List<Token> tokens) {
        this.tokens        = tokens;
        this.current       = 0;
        this.indentedLines = Set.of(); // empty by default
    }

    // Call this BEFORE parse() — detects indented lines using Stream pipeline

    public void setSource(String source) {
        String[] lines = source.split("\n", -1);

        indentedLines = IntStream.range(0, lines.length)
            .filter(i -> lines[i].length() > 0
                      && (lines[i].charAt(0) == ' ' || lines[i].charAt(0) == '\t'))
            .map(i -> i + 1)        // line numbers are 1-based
            .boxed()
            .collect(Collectors.toSet());
    }

    // Returns List<Instruction> — generics ensure type safety
    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();

        while (!isAtEnd()) {
            skipNewlines();
            if (isAtEnd()) break;
            Instruction inst = parseInstruction();
            if (inst != null) instructions.add(inst);
        }

        return instructions;
    }


    // Dispatches to the right parse method based on current token type
    private Instruction parseInstruction() {
        switch (peek().getType()) {
            case SET:  return parseAssign();
            case SHOW: return parsePrint();
            case WHEN: return parseIf();
            case LOOP: return parseLoop();
            default:
                System.err.println("Unexpected token at line "
                    + peek().getLine() + ": '" + peek().getValue() + "'");
                skipLine();
                return null;
        }
    }

    // Parses:  set x = expression
    private AssignInstruction parseAssign() {
        consume(TokenType.SET);
        String name = consume(TokenType.IDENTIFIER).getValue();
        consume(TokenType.EQUALS);
        Expression expr = parseExpression();
        skipNewlines();
        return new AssignInstruction(name, expr);
    }

    // Parses:  show expression
    private PrintInstruction parsePrint() {
        consume(TokenType.SHOW);
        Expression expr = parseExpression();
        skipNewlines();
        return new PrintInstruction(expr);
    }

    // Parses:  when condition:
    private IfInstruction parseIf() {
        consume(TokenType.WHEN);
        Expression condition = parseExpression();
        consume(TokenType.COLON);
        skipNewlines();
        return new IfInstruction(condition, parseBlock());
    }

    // Parses:  loop N:
    private RepeatInstruction parseLoop() {
        consume(TokenType.LOOP);
        int count = (int) Double.parseDouble(consume(TokenType.NUMBER).getValue());
        consume(TokenType.COLON);
        skipNewlines();
        return new RepeatInstruction(count, parseBlock());
    }

    // Stops when it hits a non-indented line or EOF
    private List<Instruction> parseBlock() {
        List<Instruction> block = new ArrayList<>();

        while (!isAtEnd()) {
            while (check(TokenType.NEWLINE)) advance(); // skip blank lines
            if (isAtEnd()) break;

            // if this line is NOT indented, the block has ended
            if (!indentedLines.contains(peek().getLine())) break;

            Instruction inst = parseInstruction();
            if (inst != null) block.add(inst);
        }

        return block;
    }

    // Calls parseTerm() first to ensure * / are handled with higher priority
    private Expression parseExpression() {
        Expression left = parseTerm();

        while (check(TokenType.PLUS)    || check(TokenType.MINUS) ||
               check(TokenType.GREATER) || check(TokenType.LESS)  ||
               check(TokenType.EQEQ)) {
            String op        = advance().getValue();
            Expression right = parseTerm();
            left = new BinaryOpNode(left, op, right);
        }

        return left;
    }

    // Calls parsePrimary() first to handle individual values
    private Expression parseTerm() {
        Expression left = parsePrimary();

        while (check(TokenType.STAR) || check(TokenType.SLASH)) {
            String op        = advance().getValue();
            Expression right = parsePrimary();
            left = new BinaryOpNode(left, op, right);
        }

        return left;
    }

    // Returns: NumberNode, StringNode, or VariableNode
    private Expression parsePrimary() {
        Token t = advance();

        switch (t.getType()) {
            case NUMBER:     return new NumberNode(Double.parseDouble(t.getValue()));
            case STRING:     return new StringNode(t.getValue());
            case IDENTIFIER: return new VariableNode(t.getValue());
            default:
                throw new RuntimeException(
                    "Expected a value (number, string, or variable) at line "
                    + t.getLine() + " but got: '" + t.getValue() + "'");
        }
    }

    // look at current token WITHOUT consuming it
    private Token peek() {
        return tokens.get(current);
    }

    // consume current token and move to the next
    private Token advance() {
        if (!isAtEnd()) current++;
        return tokens.get(current - 1);
    }

    // does current token match the given type? (does NOT consume)
    private boolean check(TokenType type) {
        return !isAtEnd() && peek().getType() == type;
    }

    // are we at the end of the token list?
    private boolean isAtEnd() {
        return current >= tokens.size() || peek().getType() == TokenType.EOF;
    }

    // consume a token, asserting it must be the expected type
    // throws a clear error if the wrong token is found
    private Token consume(TokenType expected) {
        if (!check(expected)) {
            Token got = peek();
            throw new RuntimeException(
                "Syntax error at line " + got.getLine()
                + ": expected " + expected
                + " but found '" + got.getValue() + "' (" + got.getType() + ")");
        }
        return advance();
    }

    // skip over blank lines (NEWLINE tokens)
    private void skipNewlines() {
        while (check(TokenType.NEWLINE)) advance();
    }

    // skip to end of current line — used for error recovery
    private void skipLine() {
        while (!isAtEnd() && !check(TokenType.NEWLINE)) advance();
        if (check(TokenType.NEWLINE)) advance();
    }
}
