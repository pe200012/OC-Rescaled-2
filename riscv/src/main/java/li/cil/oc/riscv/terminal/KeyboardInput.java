package li.cil.oc.riscv.terminal;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Translates keyboard events, as OpenComputers reports them (a character plus an LWJGL 2 key
 * code), into the bytes a VT100 terminal sends to the host.
 */
public final class KeyboardInput {
    private static final int KEY_ESCAPE = 1;
    private static final int KEY_BACK = 14;
    private static final int KEY_TAB = 15;
    private static final int KEY_RETURN = 28;
    private static final int KEY_LCONTROL = 29;
    private static final int KEY_LMENU = 56;
    private static final int KEY_NUMPADENTER = 156;
    private static final int KEY_RCONTROL = 157;
    private static final int KEY_RMENU = 184;
    private static final int KEY_HOME = 199;
    private static final int KEY_UP = 200;
    private static final int KEY_PRIOR = 201;
    private static final int KEY_LEFT = 203;
    private static final int KEY_RIGHT = 205;
    private static final int KEY_END = 207;
    private static final int KEY_DOWN = 208;
    private static final int KEY_NEXT = 209;
    private static final int KEY_INSERT = 210;
    private static final int KEY_DELETE = 211;
    private static final int KEY_F1 = 59;
    private static final int KEY_F10 = 68;
    private static final int KEY_F11 = 87;
    private static final int KEY_F12 = 88;

    // Letters by key code, for when the client reports no character for Ctrl+letter.
    private static final String LETTER_ROWS_BY_CODE =
        "qwertyuiop" + "    " + "asdfghjkl" + "     " + "zxcvbnm";
    private static final int FIRST_LETTER_CODE = 16;

    private static final String[] FUNCTION_KEYS = {
        "\033OP", "\033OQ", "\033OR", "\033OS", "\033[15~", "\033[17~",
        "\033[18~", "\033[19~", "\033[20~", "\033[21~", "\033[23~", "\033[24~",
    };

    private final Terminal terminal;
    private boolean leftControl, rightControl, leftAlt, rightAlt;

    public KeyboardInput(final Terminal terminal) {
        this.terminal = terminal;
    }

    public void keyDown(final int character, final int code) {
        switch (code) {
            case KEY_LCONTROL -> leftControl = true;
            case KEY_RCONTROL -> rightControl = true;
            case KEY_LMENU -> leftAlt = true;
            case KEY_RMENU -> rightAlt = true;
            case KEY_RETURN, KEY_NUMPADENTER -> send(terminal.isNewLineMode() ? "\r\n" : "\r");
            case KEY_BACK -> send("\177");
            case KEY_TAB -> send("\t");
            case KEY_ESCAPE -> send("\033");
            case KEY_UP -> sendCursorKey('A');
            case KEY_DOWN -> sendCursorKey('B');
            case KEY_RIGHT -> sendCursorKey('C');
            case KEY_LEFT -> sendCursorKey('D');
            case KEY_HOME -> sendCursorKey('H');
            case KEY_END -> sendCursorKey('F');
            case KEY_INSERT -> send("\033[2~");
            case KEY_DELETE -> send("\033[3~");
            case KEY_PRIOR -> send("\033[5~");
            case KEY_NEXT -> send("\033[6~");
            case KEY_F11 -> send(FUNCTION_KEYS[10]);
            case KEY_F12 -> send(FUNCTION_KEYS[11]);
            default -> {
                if (code >= KEY_F1 && code <= KEY_F10) {
                    send(FUNCTION_KEYS[code - KEY_F1]);
                } else {
                    sendCharacter(character, code);
                }
            }
        }
    }

    public void keyUp(final int code) {
        switch (code) {
            case KEY_LCONTROL -> leftControl = false;
            case KEY_RCONTROL -> rightControl = false;
            case KEY_LMENU -> leftAlt = false;
            case KEY_RMENU -> rightAlt = false;
            default -> {
            }
        }
    }

    public void paste(final String text) {
        terminal.putPaste(text);
    }

    // --------------------------------------------------------------------- //

    private void sendCharacter(final int character, final int code) {
        final boolean control = leftControl || rightControl;
        int value = character;
        if (control) {
            if (value == 0) {
                value = letterForCode(code);
            }
            if (value == ' ' || value == '@') {
                value = 0;
            } else if (value >= 'A' && value <= '_' || value >= 'a' && value <= 'z') {
                value &= 0x1F;
            } else if (value == 0) {
                return;
            }
        } else if (value == 0) {
            return;
        }

        if (leftAlt || rightAlt) {
            terminal.putInput((byte) 0x1B);
        }
        terminal.putInput(ByteBuffer.wrap(new String(Character.toChars(value)).getBytes(StandardCharsets.UTF_8)));
    }

    private void sendCursorKey(final char key) {
        send((terminal.isCursorKeyApplicationMode() ? "\033O" : "\033[") + key);
    }

    private void send(final String value) {
        terminal.putInput(ByteBuffer.wrap(value.getBytes(StandardCharsets.US_ASCII)));
    }

    private static int letterForCode(final int code) {
        final int index = code - FIRST_LETTER_CODE;
        if (index < 0 || index >= LETTER_ROWS_BY_CODE.length()) {
            return 0;
        }
        final char letter = LETTER_ROWS_BY_CODE.charAt(index);
        return letter == ' ' ? 0 : letter;
    }
}
