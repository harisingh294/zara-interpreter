import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tokenizer {

    
    private static final Map<String, TokenType> KEYWORDS = Map.of(
        "set",  TokenType.SET,
        "show", TokenType.SHOW,
        "when", TokenType.WHEN,
        "loop", TokenType.LOOP
    );

    
    private static final Map<Character, TokenType> SINGLE_OPS = Map.of(
        '+', TokenType.PLUS,
        '-', TokenType.MINUS,
        '*', TokenType.STAR,
        '/', TokenType.SLASH,
        '>', TokenType.GREATER,
        '<', TokenType.LESS,
        ':', TokenType.COLON
    );

    private final String source;  // full source code — immutable (final)
    private int pos;              // current character position
    private int line;             // current line number (starts at 1)

    public Tokenizer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < source.length()) {

            char c = source.charAt(pos);

           
            if (c == ' ' || c == '\t') {
                pos++;
                continue;
            }

            // skip Windows-style carriage return \r
            if (c == '\r') {
                pos++;
                continue;
            }

            // newline marks the end of a statement — produce a NEWLINE token
            if (c == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n", line));
                line++;
                pos++;
                continue;
            }

            // '#' starts a comment — skip everything until end of line
            if (c == '#') {
                while (pos < source.length() && source.charAt(pos) != '\n') {
                    pos++;
                }
                continue;
            }

            // Map.containsKey + Map.get replaces 7 separate if-statements
            if (SINGLE_OPS.containsKey(c)) {
                tokens.add(new Token(SINGLE_OPS.get(c), String.valueOf(c), line));
                pos++;
                continue;
            }

            // = or == — need to look ahead one character
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

            // string literal  "hello world"
            if (c == '"') {
                tokens.add(readString());
                continue;
            }

            // number literal  10  or  3.14
            if (Character.isDigit(c)) {
                tokens.add(readNumber());
                continue;
            }

            // keyword or variable name (identifier)
            if (Character.isLetter(c) || c == '_') {
                tokens.add(readWord());
                continue;
            }

            // unknown character — warn and skip
            System.err.println("Warning: unknown character '" + c + "' at line " + line);
            pos++;
        }

        // EOF token tells the Parser when to stop
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }


    // reads:  "hello world"  →  Token(STRING, "hello world", line)
    private Token readString() {
        pos++; // skip opening "
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && source.charAt(pos) != '"') {
            sb.append(source.charAt(pos));
            pos++;
        }
        if (pos < source.length()) pos++; // skip closing "
        return new Token(TokenType.STRING, sb.toString(), line);
    }


    private Token readNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() &&
               (Character.isDigit(source.charAt(pos)) || source.charAt(pos) == '.')) {
            sb.append(source.charAt(pos));
            pos++;
        }
        return new Token(TokenType.NUMBER, sb.toString(), line);
    }

    
    private Token readWord() {
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() &&
               (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
            sb.append(source.charAt(pos));
            pos++;
        }
        String word = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
        return new Token(type, word, line);
    }
}
