import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizer — Stage 1 of the interpreter pipeline.
 *
 * Reads raw ZARA source code character by character and
 * produces a flat List<Token>.
 *
 * Example:
 *   Input:  "set x = 10"
 *   Output: [SET:"set", IDENTIFIER:"x", EQUALS:"=", NUMBER:"10", NEWLINE, EOF]
 */
public class Tokenizer {

    private final String source;
    private int pos;   // current character position
    private int line;  // current line number (1-based)

    public Tokenizer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < source.length()) {

            // Skip spaces and tabs (NOT newlines — those are meaningful)
            if (source.charAt(pos) == ' ' || source.charAt(pos) == '\t') {
                pos++;
                continue;
            }

            char c = source.charAt(pos);

            // Windows carriage return — skip silently
            if (c == '\r') { pos++; continue; }

            // Newline — marks end of a statement
            if (c == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n", line));
                line++;
                pos++;
                continue;
            }

            // Comment: # anything until end of line
            if (c == '#') {
                while (pos < source.length() && source.charAt(pos) != '\n') pos++;
                continue;
            }

            // Single-character operators
            if (c == '+') { tokens.add(new Token(TokenType.PLUS,    "+", line)); pos++; continue; }
            if (c == '-') { tokens.add(new Token(TokenType.MINUS,   "-", line)); pos++; continue; }
            if (c == '*') { tokens.add(new Token(TokenType.STAR,    "*", line)); pos++; continue; }
            if (c == '/') { tokens.add(new Token(TokenType.SLASH,   "/", line)); pos++; continue; }
            if (c == '>') { tokens.add(new Token(TokenType.GREATER, ">", line)); pos++; continue; }
            if (c == '<') { tokens.add(new Token(TokenType.LESS,    "<", line)); pos++; continue; }
            if (c == ':') { tokens.add(new Token(TokenType.COLON,   ":", line)); pos++; continue; }

            // = or ==
            if (c == '=') {
                if (pos + 1 < source.length() && source.charAt(pos + 1) == '=') {
                    tokens.add(new Token(TokenType.EQEQ, "==", line));
                    pos += 2;
                } else {
                    tokens.add(new Token(TokenType.EQUALS, "=", line));
                    pos++;
                }
                continue;
            }

            // String literal: "hello world"
            if (c == '"') {
                pos++; // skip opening quote
                StringBuilder sb = new StringBuilder();
                while (pos < source.length() && source.charAt(pos) != '"') {
                    sb.append(source.charAt(pos));
                    pos++;
                }
                if (pos < source.length()) pos++; // skip closing quote
                tokens.add(new Token(TokenType.STRING, sb.toString(), line));
                continue;
            }

            // Number literal: 10, 3, 3.14
            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (pos < source.length() &&
                       (Character.isDigit(source.charAt(pos)) || source.charAt(pos) == '.')) {
                    sb.append(source.charAt(pos));
                    pos++;
                }
                tokens.add(new Token(TokenType.NUMBER, sb.toString(), line));
                continue;
            }

            // Keywords and variable names (identifiers)
            if (Character.isLetter(c) || c == '_') {
                StringBuilder sb = new StringBuilder();
                while (pos < source.length() &&
                       (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
                    sb.append(source.charAt(pos));
                    pos++;
                }
                String word = sb.toString();
                switch (word) {
                    case "set":  tokens.add(new Token(TokenType.SET,        word, line)); break;
                    case "show": tokens.add(new Token(TokenType.SHOW,       word, line)); break;
                    case "when": tokens.add(new Token(TokenType.WHEN,       word, line)); break;
                    case "loop": tokens.add(new Token(TokenType.LOOP,       word, line)); break;
                    default:     tokens.add(new Token(TokenType.IDENTIFIER, word, line)); break;
                }
                continue;
            }

            // Unknown character — warn and skip
            System.err.println("Warning: unknown character '" + c + "' at line " + line);
            pos++;
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }
}
