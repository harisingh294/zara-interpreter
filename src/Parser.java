import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parser — Stage 2 of the interpreter pipeline.
 *
 * Reads the List<Token> from the Tokenizer and builds
 * a List<Instruction>. Each instruction may contain an expression tree.
 *
 * KEY CONCEPT — Operator Precedence via Recursive Descent:
 *
 *   parseExpression()  handles + and -   <- called FIRST (lowest priority)
 *     calls parseTerm()  handles * and / <- called inside parseExpression
 *       calls parsePrimary()  handles a single value (highest priority)
 *
 *   Because parseTerm is called INSIDE parseExpression, any * or /
 *   operations end up DEEPER in the tree than + or -.
 *   Deeper nodes evaluate first -> * and / always run before + and -.
 *   No special logic needed — the tree shape does it automatically.
 *
 *   Example:  x + y * 2
 *     Tree:       Add
 *                /   \
 *               x   Multiply
 *                   /    \
 *                  y      2
 *   Multiply (deeper) runs first -> 6, then Add -> x + 6
 *
 * KEY CONCEPT — Block detection (how "when" and "loop" bodies work):
 *
 *   ZARA uses indentation just like Python. Lines indented with
 *   spaces or tabs belong to the block body. The Parser pre-scans
 *   the source to record which line numbers are indented, then
 *   keeps reading instructions as long as the next token's line
 *   number is in the "indented lines" set.
 */
public class Parser {

    private final List<Token> tokens;
    private int current;

    // Line numbers that start with spaces/tabs in the source
    private final Set<Integer> indentedLineNumbers = new HashSet<>();

    public Parser(List<Token> tokens) {
        this.tokens  = tokens;
        this.current = 0;
    }

    /**
     * Call this before parse() to enable indentation-based block detection.
     * Pass the same source string that was given to the Tokenizer.
     */
    public void setSource(String source) {
        String[] lines = source.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String ln = lines[i];
            if (!ln.isEmpty() && (ln.charAt(0) == ' ' || ln.charAt(0) == '\t')) {
                indentedLineNumbers.add(i + 1); // line numbers are 1-based
            }
        }
    }

    // ─────────────────────────────────────────────
    // PUBLIC ENTRY POINT
    // ─────────────────────────────────────────────

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

    // ─────────────────────────────────────────────
    // INSTRUCTION PARSERS
    // ─────────────────────────────────────────────

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

    // Parses:  when condition:\n  body...
    private IfInstruction parseIf() {
        consume(TokenType.WHEN);
        Expression condition = parseExpression();
        consume(TokenType.COLON);
        skipNewlines();
        List<Instruction> body = parseBlock();
        return new IfInstruction(condition, body);
    }

    // Parses:  loop N:\n  body...
    private RepeatInstruction parseLoop() {
        consume(TokenType.LOOP);
        int count = (int) Double.parseDouble(consume(TokenType.NUMBER).getValue());
        consume(TokenType.COLON);
        skipNewlines();
        List<Instruction> body = parseBlock();
        return new RepeatInstruction(count, body);
    }

    // ─────────────────────────────────────────────
    // BLOCK PARSER
    // Reads indented instructions until a non-indented line is found.
    // ─────────────────────────────────────────────

    private List<Instruction> parseBlock() {
        List<Instruction> block = new ArrayList<>();
        while (!isAtEnd()) {
            while (check(TokenType.NEWLINE)) advance(); // skip blank lines
            if (isAtEnd()) break;

            // Stop if this line is not indented
            if (!indentedLineNumbers.contains(peek().getLine())) break;

            Instruction inst = parseInstruction();
            if (inst != null) block.add(inst);
        }
        return block;
    }

    // ─────────────────────────────────────────────
    // EXPRESSION PARSERS  (Recursive Descent)
    // ─────────────────────────────────────────────

    // Handles:  expr + expr   expr - expr   expr > expr   expr < expr   expr == expr
    private Expression parseExpression() {
        Expression left = parseTerm();
        while (check(TokenType.PLUS)    || check(TokenType.MINUS) ||
               check(TokenType.GREATER) || check(TokenType.LESS)  ||
               check(TokenType.EQEQ)) {
            String op = advance().getValue();
            Expression right = parseTerm();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    // Handles:  expr * expr   expr / expr
    private Expression parseTerm() {
        Expression left = parsePrimary();
        while (check(TokenType.STAR) || check(TokenType.SLASH)) {
            String op = advance().getValue();
            Expression right = parsePrimary();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    // Handles a single value: number, string, or variable name
    private Expression parsePrimary() {
        Token t = advance();
        switch (t.getType()) {
            case NUMBER:     return new NumberNode(Double.parseDouble(t.getValue()));
            case STRING:     return new StringNode(t.getValue());
            case IDENTIFIER: return new VariableNode(t.getValue());
            default:
                throw new RuntimeException(
                    "Expected a value at line " + t.getLine()
                    + " but got: '" + t.getValue() + "' (" + t.getType() + ")");
        }
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private Token peek()           { return tokens.get(current); }
    private Token advance()        { if (!isAtEnd()) current++; return tokens.get(current - 1); }
    private boolean check(TokenType t) { return !isAtEnd() && peek().getType() == t; }
    private boolean isAtEnd()      { return current >= tokens.size() || peek().getType() == TokenType.EOF; }

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

    private void skipNewlines() { while (check(TokenType.NEWLINE)) advance(); }
    private void skipLine()     { while (!isAtEnd() && !check(TokenType.NEWLINE)) advance(); if (check(TokenType.NEWLINE)) advance(); }
}
