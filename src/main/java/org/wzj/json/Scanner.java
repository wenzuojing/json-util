package org.wzj.json;


/**
 * Created by wens on 15-4-30.
 */
public class Scanner {

    //defined event

    public final static int SCAN_CONTINUE = 100; // uninteresting byte
    public final static int SCAN_BEGIN_LITERAL = 101;      // end implied by next result != SCAN_CONTINUE
    public final static int SCAN_BEGIN_OBJECT = 102;        // begin object
    public final static int SCAN_OBJECT_KEY = 103;      // just finished object key (string)
    public final static int SCAN_OBJECT_VALUE = 104;   // just finished non-last object value
    public final static int SCAN_END_OBJECT = 105; // end object (implies SCAN_OBJECT_VALUE if possible)
    public final static int SCAN_BEGIN_ARRAY = 106;  // begin array
    public final static int SCAN_ARRAY_VALUE = 107; // just finished array value
    public final static int SCAN_END_ARRAY = 108;// end array (implies SCAN_ARRAY_VALUE if possible)
    public final static int SCAN_SKIP_SPACE = 109;// space byte; can skip; known to be last "continue" result

    public final static int SCAN_END = 200;  // top-level value ended *before* this byte; known to be first "stop" result
    public final static int SCAN_ERROR = 201; // hit an error, scanner.err.

    public final static int PARSE_OBJECT_KEY = 300; // parsing object key (before colon)
    public final static int PARSE_OBJECT_VALUE = 301;     // parsing object value (after colon)
    public final static int PARSE_ARRAY_VALUE = 302;    // parsing array value


    private State state;

    private Stack<Integer> parseStack;

    private int posiction = 0;


    public Scanner() {
        parseStack = new Stack<Integer>();
        setState(stateBeginValue);
        posiction = 0;
    }

    public void reset() {
        parseStack.clear();
        setState(stateBeginValue);
        posiction = 0;
    }


    public int step(int c) {
        posiction++;


        return this.state.apply(this, c);
    }

    private void setState(State state) {
        this.state = state;
    }

    private void pushParseState(int s) {
        parseStack.push(s);
    }

    private Integer popParseState() {
        return parseStack.pop();
    }


    private static boolean isSpace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    interface State {
        int apply(Scanner scanner, int c);
    }


    static State stateBeginValueOrEmpty = new StateBeginValueOrEmpty();

    // stateBeginValueOrEmpty is the state after reading `[`.
    static class StateBeginValueOrEmpty implements State {

        public int apply(Scanner scanner, int c) {
            if (c <= ' ' && isSpace((char) c)) {
                return SCAN_SKIP_SPACE;
            }
            if (c == ']') {
                return stateEndValue.apply(scanner, c);
            }
            return stateBeginValue.apply(scanner, c);
        }
    }


    static State stateBeginValue = new StateBeginValue();


    /**
     * stateBeginValue is the state at the beginning of the input.
     */
    static class StateBeginValue implements State {

        public int apply(Scanner scanner, int c) {
            if (c <= ' ' && isSpace((char) c)) {
                return SCAN_SKIP_SPACE;
            }
            switch (c) {
                case '{':
                    scanner.setState(stateBeginStringOrEmpty);
                    scanner.pushParseState(PARSE_OBJECT_KEY);
                    return SCAN_BEGIN_OBJECT;
                case '[':
                    scanner.setState(stateBeginValueOrEmpty);
                    scanner.pushParseState(PARSE_ARRAY_VALUE);
                    return SCAN_BEGIN_ARRAY;
                case '"':
                    scanner.setState(stateInString);
                    return SCAN_BEGIN_LITERAL;
                case '-':
                    scanner.setState(stateNeg);
                    return SCAN_BEGIN_LITERAL;
                case '0': // beginning of 0.123
                    scanner.setState(state0);
                    return SCAN_BEGIN_LITERAL;
                case 't': // beginning of true
                    scanner.setState(stateT);
                    return SCAN_BEGIN_LITERAL;
                case 'f': // beginning of false
                    scanner.setState(stateF);
                    return SCAN_BEGIN_LITERAL;
                case 'n': // beginning of null
                    scanner.setState(stateN);
                    return SCAN_BEGIN_LITERAL;
            }
            if ('1' <= c && c <= '9') { // beginning of 1234.5
                scanner.setState(state1);
                return SCAN_BEGIN_LITERAL;
            }
            throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
        }
    }

    static State stateBeginStringOrEmpty = new StateBeginStringOrEmpty();


    /**
     * stateBeginStringOrEmpty is the state after reading `{`.
     */
    static class StateBeginStringOrEmpty implements State {

        public int apply(Scanner scanner, int c) {
            if (c <= ' ' && isSpace((char) c)) {
                return SCAN_SKIP_SPACE;
            }
            if (c == '}') {
                scanner.popParseState();
                scanner.pushParseState(PARSE_OBJECT_VALUE);
                return stateEndValue.apply(scanner, c);
            }
            return stateBeginString.apply(scanner, c);
        }
    }

    static State stateBeginString = new StateBeginString();


    /**
     * stateBeginString is the state after reading `{"key": value,`.
     */
    static class StateBeginString implements State {

        public int apply(Scanner scanner, int c) {
            if (c <= ' ' && isSpace((char) c)) {
                return SCAN_SKIP_SPACE;
            }
            if (c == '"') {
                scanner.setState(stateInString);
                return SCAN_BEGIN_LITERAL;
            }
            throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
        }
    }

    static State stateEndValue = new StateEndValue();

    /**
     * stateEndValue is the state after completing a value,
     * such as after reading `{}` or `true` or `["x"`.
     */
    static class StateEndValue implements State {

        public int apply(Scanner scanner, int c) {

            if (c <= ' ' && isSpace((char) c)) {
                return SCAN_SKIP_SPACE;
            }

            Integer ps = scanner.popParseState();
            if (ps == null) {
                // Completed top-level before the current byte.
                scanner.setState(stateEndTop);
                return stateEndTop.apply(scanner, c);
            }
            if (c <= ' ' && isSpace((char) c)) {
                scanner.setState(stateEndValue);
                return SCAN_SKIP_SPACE;
            }
            switch (ps.intValue()) {
                case PARSE_OBJECT_KEY:
                    if (c == ':') {
                        scanner.pushParseState(PARSE_OBJECT_VALUE);
                        scanner.setState(stateBeginValue);
                        return SCAN_OBJECT_KEY;
                    }
                    throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
                case PARSE_OBJECT_VALUE:
                    if (c == ',') {
                        scanner.pushParseState(PARSE_OBJECT_KEY);
                        scanner.setState(stateBeginString);
                        return SCAN_OBJECT_VALUE;
                    }
                    if (c == '}') {
                        return SCAN_END_OBJECT;
                    }
                    throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
                case PARSE_ARRAY_VALUE:
                    if (c == ',') {
                        scanner.pushParseState(PARSE_ARRAY_VALUE);
                        scanner.setState(stateBeginValue);
                        return SCAN_ARRAY_VALUE;
                    }
                    if (c == ']') {
                        return SCAN_END_ARRAY;
                    }
                    throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
            }
            throw new JsonException();
        }
    }

    static State stateEndTop = new StateEndTop();

    /**
     * stateEndTop is the state after finishing the top-level value,
     * such as after reading `{}` or `[1,2,3]`.
     * Only space characters should be seen now.
     */
    static class StateEndTop implements State {

        public int apply(Scanner scanner, int c) {
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                // Complain about non-space byte on next call.
                throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
            }
            return SCAN_END;
        }
    }


    static State stateInString = new StateInString();

    /**
     * stateInString is the state after reading `"`.
     */
    static class StateInString implements State {

        public int apply(Scanner scanner, int c) {
            if (c == '"') {
                scanner.setState(stateEndValue);

                return SCAN_CONTINUE;
            }
            if (c == '\\') {
                scanner.setState(stateInStringEsc);
                return SCAN_CONTINUE;
            }
            if (c < 0x20) {
                throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
            }
            return SCAN_CONTINUE;
        }
    }

    static State stateInStringEsc = new StateInStringEsc();

    /**
     * stateInStringEsc is the state after reading `"\` during a quoted string.
     */
    static class StateInStringEsc implements State {

        public int apply(Scanner scanner, int c) {
            if (c == 'b' || c == 'f' || c == 'n' || c == 'r' || c == 't' || c == '\\' || c == '/' || c == '"') {
                scanner.setState(stateInString);
                return SCAN_CONTINUE;
            }
            if (c == 'u') {
                scanner.setState(stateInStringEscU);
                return SCAN_CONTINUE;
            }
            throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
        }
    }


    static State stateInStringEscU = new StateInStringEscU();


    /**
     * stateInStringEscU is the state after reading \\u during a quoted string.
     */
    static class StateInStringEscU implements State {

        public int apply(Scanner scanner, int c) {
            if ('0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F') {
                scanner.setState(stateInStringEscU1);
                return SCAN_CONTINUE;
            }
            // numbers
            throw new JsonException((char) c + " in \\u hexadecimal character escape");
        }
    }

    static State stateInStringEscU1 = new StateInStringEscU1();

    /**
     * stateInStringEscU1 is the state after reading \\u1  during a quoted string.
     */
    static class StateInStringEscU1 implements State {

        public int apply(Scanner scanner, int c) {
            if ('0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F') {
                scanner.setState(stateInStringEscU12);
                return SCAN_CONTINUE;
            }
            // numbers
            throw new JsonException((char) c + " in \\u hexadecimal character escape");
        }
    }

    static State stateInStringEscU12 = new StateInStringEscU12();

    /**
     * stateInStringEscU12 is the state after reading \\u12 during a quoted string.
     */
    static class StateInStringEscU12 implements State {

        public int apply(Scanner scanner, int c) {
            if ('0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F') {
                scanner.setState(stateInStringEscU123);
                return SCAN_CONTINUE;
            }
            // numbers
            throw new JsonException((char) c + " in \\u hexadecimal character escape");
        }
    }


    static State stateInStringEscU123 = new StateInStringEscU123();

    /**
     * stateInStringEscU123 is the state after reading \\u123 during a quoted string.
     */
    static class StateInStringEscU123 implements State {

        public int apply(Scanner scanner, int c) {
            if ('0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F') {
                scanner.setState(stateInString);
                return SCAN_CONTINUE;
            }
            // numbers
            throw new JsonException((char) c + " in \\u hexadecimal character escape");
        }
    }


    static State stateNeg = new StateNeg();


    /**
     * stateNeg is the state after reading `-` during a number.
     */
    static class StateNeg implements State {

        public int apply(Scanner scanner, int c) {
            if (c == '0') {
                scanner.setState(state0);
                return SCAN_CONTINUE;
            }
            if ('1' <= c && c <= '9') {
                scanner.setState(state1);
                return SCAN_CONTINUE;
            }
            throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
        }
    }


    static State state1 = new State1();


    /**
     * state1 is the state after reading a non-zero integer during a number,
     * such as after reading `1` or `100` but not `0`.
     */
    static class State1 implements State {
        public int apply(Scanner scanner, int c) {
            if ('0' <= c && c <= '9') {
                scanner.setState(state1);
                return SCAN_CONTINUE;
            }
            return state0.apply(scanner, c);
        }
    }


    static State state0 = new State0();

    /**
     * state0 is the state after reading `0` during a number.
     */
    static class State0 implements State {

        public int apply(Scanner scanner, int c) {
            if (c == '.') {
                scanner.setState(stateDot);
                return SCAN_CONTINUE;
            }
            if (c == 'e' || c == 'E') {
                scanner.setState(stateE);
                return SCAN_CONTINUE;
            }
            return stateEndValue.apply(scanner, c);
        }
    }

    static State stateDot = new StateDot();


    /**
     * stateDot is the state after reading the integer and decimal point in a number,
     * such as after reading `1.`.
     */
    static class StateDot implements State {
        public int apply(Scanner scanner, int c) {
            if ('0' <= c && c <= '9') {
                scanner.setState(stateDot0);
                return SCAN_CONTINUE;
            }
            throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
        }
    }


    static State stateDot0 = new StateDot0();

    /**
     * stateDot0 is the state after reading the integer, decimal point, and subsequent
     * digits of a number, such as after reading `3.14`.
     */
    static class StateDot0 implements State {
        public int apply(Scanner scanner, int c) {
            if ('0' <= c && c <= '9') {
                scanner.setState(stateDot0);
                return SCAN_CONTINUE;
            }
            if (c == 'e' || c == 'E') {
                scanner.setState(stateE);
                return SCAN_CONTINUE;
            }
            return stateEndValue.apply(scanner, c);
        }
    }


    static State stateE = new StateE();

    /**
     * stateE is the state after reading the mantissa and e in a number,
     * such as after reading `314e` or `0.314e`.
     */
    static class StateE implements State {

        public int apply(Scanner scanner, int c) {
            if (c == '+') {
                scanner.setState(stateESign);
                return SCAN_CONTINUE;
            }
            if (c == '-') {
                scanner.setState(stateESign);
                return SCAN_CONTINUE;
            }
            return stateESign.apply(scanner, c);
        }
    }


    static State stateESign = new StateESign();

    /**
     * stateESign is the state after reading the mantissa, e, and sign in a number,
     * such as after reading `314e-` or `0.314e+`.
     */
    static class StateESign implements State {
        public int apply(Scanner scanner, int c) {
            if ('0' <= c && c <= '9') {
                scanner.setState(stateE0);
                return SCAN_CONTINUE;
            }
            throw new JsonException("json format is incorrect:,exception at " + scanner.posiction);
        }
    }


    static State stateE0 = new StateE0();


    /**
     * stateE0 is the state after reading the mantissa, e, optional sign,
     * and at least one digit of the exponent in a number,
     * such as after reading `314e-2` or `0.314e+1` or `3.14e0`.
     */
    static class StateE0 implements State {
        public int apply(Scanner scanner, int c) {
            if ('0' <= c && c <= '9') {
                scanner.setState(stateE0);
                return SCAN_CONTINUE;
            }
            return stateEndValue.apply(scanner, c);
        }
    }

    static State stateT = new StateT();


    /**
     * stateT is the state after reading `t`.
     */
    static class StateT implements State {

        public int apply(Scanner scanner, int c) {
            if (c == 'r') {
                scanner.setState(stateTr);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal true (expecting 'r')");
        }
    }


    static State stateTr = new StateTr();

    /**
     * stateTr is the state after reading `tr`.
     */
    static class StateTr implements State {

        public int apply(Scanner scanner, int c) {
            if (c == 'u') {
                scanner.setState(stateTru);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal true (expecting 'u')");
        }
    }


    static State stateTru = new StateTru();


    /**
     * stateTru is the state after reading `tru`.
     */
    static class StateTru implements State {
        public int apply(Scanner scanner, int c) {
            if (c == 'e') {
                scanner.setState(stateEndValue);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal true (expecting 'e')");
        }
    }


    static State stateF = new StateF();

    /**
     * stateF is the state after reading `f`.
     */
    static class StateF implements State {
        public int apply(Scanner scanner, int c) {
            if (c == 'a') {
                scanner.setState(stateFa);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal false (expecting 'a')");
        }
    }


    static State stateFa = new StateFa();


    /**
     * stateFa is the state after reading `fa`.
     */
    static class StateFa implements State {
        public int apply(Scanner scanner, int c) {
            if (c == 'l') {
                scanner.setState(stateFal);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal false (expecting 'l')");
        }
    }


    static State stateFal = new StateFal();


    /**
     * stateFal is the state after reading `fal`.
     */
    static class StateFal implements State {
        public int apply(Scanner scanner, int c) {
            if (c == 's') {
                scanner.setState(stateFals);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal false (expecting 's')");
        }
    }

    static State stateFals = new StateFals();


    /**
     * stateFals is the state after reading `fals`.
     */
    static class StateFals implements State {
        public int apply(Scanner scanner, int c) {
            if (c == 'e') {
                scanner.setState(stateEndValue);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal false (expecting 'e')");
        }
    }


    static State stateN = new StateN();

    /**
     * stateN is the state after reading `n`.
     */
    static class StateN implements State {
        public int apply(Scanner scanner, int c) {
            if (c == 'u') {
                scanner.setState(stateNu);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal null (expecting 'u')");
        }
    }


    static State stateNu = new StateNu();

    /**
     * stateNu is the state after reading `nu`.
     */
    static class StateNu implements State {
        public int apply(Scanner scanner, int c) {
            if (c == 'l') {
                scanner.setState(stateNul);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal null (expecting 'l')");
        }
    }


    static State stateNul = new StateNul();

    /**
     * stateNul is the state after reading `nul`.
     */
    static class StateNul implements State {
        public int apply(Scanner scanner, int c) {
            if (c == 'l') {
                scanner.setState(stateEndValue);
                return SCAN_CONTINUE;
            }
            throw new JsonException((char) c + " in literal null (expecting 'l')");
        }
    }


}
