import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private final String source;
    private int pos;   
    private int line;  

    public Tokenizer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < source.length()) {

            
            if (source.charAt(pos) == ' ' || source.charAt(pos) == '\t') {
                pos++;
                continue;
            }

            char c = source.charAt(pos);

            if (c == '\r') { pos++; continue; }

            if (c == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n", line));
                line++;
                pos++;
                continue;
            }

            if (c == '#') {
                while (pos < source.length() && source.charAt(pos) != '\n') pos++;
                continue;
            }

            if (c == '+') { tokens.add(new Token(TokenType.PLUS,    "+", line)); pos++; continue; }
            if (c == '-') { tokens.add(new Token(TokenType.MINUS,   "-", line)); pos++; continue; }
            if (c == '*') { tokens.add(new Token(TokenType.STAR,    "*", line)); pos++; continue; }
            if (c == '/') { tokens.add(new Token(TokenType.SLASH,   "/", line)); pos++; continue; }
            if (c == '>') { tokens.add(new Token(TokenType.GREATER, ">", line)); pos++; continue; }
            if (c == '<') { tokens.add(new Token(TokenType.LESS,    "<", line)); pos++; continue; }
            if (c == ':') { tokens.add(new Token(TokenType.COLON,   ":", line)); pos++; continue; }

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

            if (c == '"') {
                pos++; 
                StringBuilder sb = new StringBuilder();
                while (pos < source.length() && source.charAt(pos) != '"') {
                    sb.append(source.charAt(pos));
                    pos++;
                }
                if (pos < source.length()) pos++; 
                tokens.add(new Token(TokenType.STRING, sb.toString(), line));
                continue;
            }

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
