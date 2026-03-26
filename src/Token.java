// ============================================================
// Token.java
// One labelled piece of source code.
// Example: the word "set" on line 1 becomes Token(SET,"set",1)
// Immutable - fields are set once in constructor, never changed.
// ============================================================
public class Token {

    private final TokenType type;   // Category of this token
    private final String value;     // Exact text from source code
    private final int line;         // Line number (for error messages)

    public Token(TokenType type, String value, int line) {
        this.type  = type;
        this.value = value;
        this.line  = line;
    }

    public TokenType getType()  { return type;  }
    public String    getValue() { return value; }
    public int       getLine()  { return line;  }

    @Override
    public String toString() {
        return "Token[" + type + ", \"" + value + "\", line=" + line + "]";
    }
}
