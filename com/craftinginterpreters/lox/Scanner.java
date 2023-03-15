package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

// Class for storing the resulting scanner function output
class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;
    
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }    

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // At the beginning of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    // Assign a token if possible
    private void scanToken() {
        char c = advance();
        switch(c) {
            // Simple tokens
            case '(' : addToken(LEFT_PAREN); break;
            case ')' : addToken(RIGHT_PAREN); break;
            case '{' : addToken(LEFT_BRACE); break;
            case '}' : addToken(RIGHT_BRACE); break;
            case ',' : addToken(COMMA); break;
            case '.' : addToken(DOT); break;
            case '-' : addToken(MINUS); break;
            case '+' : addToken(PLUS); break;
            case ';' : addToken(SEMICOLON); break;
            case '*' : addToken(STAR); break;

            // If the next thing is "=" add the first token, else add the second one
            // Love ternary operators
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;

            // The comment line special cases
            case '/': 
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance(); // If "//" is found this line is a comment, and it while loops through rest of it to ignore
                }
                else if (match('*')) {
                    starcomment(); 
                }
                else {
                    addToken(SLASH);
                }
                break;

            // These are spaces, returns and tabs, so just whitespace 
            case ' ':
            case '\r':
            case '\t':
            break;
          
            // End of line found, advance to the next line
            case '\n': line++; break;

            // A wild string is encountered, prepare to find it all
            case '"': string(); break;

            default:
                if (isDigit(c)) { number(); }
                else if (isAlpha(c)) { identifier(); }
                else { Lox.error(line, "unexpected character."); }
                break;
        }
    } //scanToken

    // Might be a Reserved word
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    // It's a number
    private void number() {
        while (isDigit(peek())) advance();

        // Look for a decimal point
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));  
    }
    

    // A quote was found, not loop through and find the end quote
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The end of line was not found, and it got out of the while loop, meaning the other quote was found
        advance();

        // This removes the quotes leaving just the string
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    } // string

    // A "/*" was found, denoting the start of a star, possibly multilined, comment, need to find the end 
    private void starcomment() {
        while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            // If the comment is not terminated before the end of the file, I don't think that kicks off an error
            return;
        } 

        // The end of line was not found, and it got out of the while loop, meaning the "/*" was found
        advance();
        advance();
    } 

    // Looks at next character and compares it to the "expected", if match, return true
    // If true, advances to the counter to the next unknown character
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    // Returns the next character in the line (if it exists) (does not advance counter)
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // Returns the second next character in line 
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // Is the character a letter?
    private boolean isAlpha(char c) {
        return ((c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c == '_') );
    }

    // Is the character either a letter or a number?
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // Simple isDigit
    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    // Returns true if it at the end of the scanned line
    private boolean isAtEnd() {
        return current >= source.length();
    } 

    // Get next character in line
    private char advance() {
        return source.charAt(current++);
    }

    // This adds the new token to the 
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

}
