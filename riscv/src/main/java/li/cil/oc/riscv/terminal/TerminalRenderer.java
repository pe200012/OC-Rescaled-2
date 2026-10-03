package li.cil.oc.riscv.terminal;

import java.util.Arrays;

/**
 * Copies the contents of a {@link Terminal} onto a text surface, sending only cells that changed
 * since the last call, merged into runs of equal colors.
 */
public final class TerminalRenderer {
    public interface Surface {
        void set(int column, int row, String text, int foreground, int background);
    }

    private static final int CURSOR_FLAG = 1 << 24;
    private static final int NOTHING_SHOWN = -1;

    private static final int[] PALETTE = {
        0x000000, 0xAA0000, 0x00AA00, 0xAA5500, 0x0000AA, 0xAA00AA, 0x00AAAA, 0xAAAAAA,
        0x555555, 0xFF5555, 0x55FF55, 0xFFFF55, 0x5555FF, 0xFF55FF, 0x55FFFF, 0xFFFFFF,
    };

    // DEC special graphics, which the terminal stores as glyph index (ch - '_').
    private static final char[] GRAPHICS =
        " ◆▒␉␌␍␊°±␤␋┘┐┌└┼⎺⎻─⎼⎽├┤┴┬│≤≥π≠£·".toCharArray();

    private final int[] shown = new int[Terminal.WIDTH * Terminal.HEIGHT];

    public TerminalRenderer() {
        invalidate();
    }

    public void invalidate() {
        Arrays.fill(shown, NOTHING_SHOWN);
    }

    public void render(final Terminal terminal, final Surface surface) {
        final int cursor = terminal.isCursorVisible()
            ? terminal.getCursorX() + terminal.getCursorY() * Terminal.WIDTH
            : -1;
        final StringBuilder text = new StringBuilder(Terminal.WIDTH);

        for (int row = 0; row < Terminal.HEIGHT; row++) {
            int column = 0;
            while (column < Terminal.WIDTH) {
                final int index = column + row * Terminal.WIDTH;
                final int cell = cellAt(terminal, index, cursor);
                if (cell == shown[index]) {
                    column++;
                    continue;
                }

                final int start = column;
                final int foreground = foreground(cell);
                final int background = background(cell);
                text.setLength(0);
                while (column < Terminal.WIDTH) {
                    final int runIndex = column + row * Terminal.WIDTH;
                    final int runCell = cellAt(terminal, runIndex, cursor);
                    if (runCell == shown[runIndex] || foreground(runCell) != foreground || background(runCell) != background) {
                        break;
                    }
                    text.append(glyph(runCell));
                    shown[runIndex] = runCell;
                    column++;
                }
                surface.set(start, row, text.toString(), foreground, background);
            }
        }
    }

    // --------------------------------------------------------------------- //

    private static int cellAt(final Terminal terminal, final int index, final int cursor) {
        final int cell = terminal.getCell(index);
        return index == cursor ? cell | CURSOR_FLAG : cell;
    }

    private static int foreground(final int cell) {
        return (cell & CURSOR_FLAG) != 0 ? baseBackground(cell) : baseForeground(cell);
    }

    private static int background(final int cell) {
        return (cell & CURSOR_FLAG) != 0 ? baseForeground(cell) : baseBackground(cell);
    }

    private static int baseForeground(final int cell) {
        final boolean bright = Terminal.isForegroundBright(cell) || Terminal.isBold(cell);
        return PALETTE[Terminal.getForegroundColorIndex(cell) + (bright ? 8 : 0)];
    }

    private static int baseBackground(final int cell) {
        return PALETTE[Terminal.getBackgroundColorIndex(cell) + (Terminal.isBackgroundBright(cell) ? 8 : 0)];
    }

    private static char glyph(final int cell) {
        if (!Terminal.isVisible(cell)) {
            return ' ';
        }
        final int character = Terminal.getCharacter(cell);
        return character < GRAPHICS.length ? GRAPHICS[character] : (char) character;
    }
}
