package li.cil.oc.server.machine.riscv;

/**
 * A screen that can show pixels in place of its text, for a RISC-V machine's framebuffer.
 * Screens, li.cil.oc.common.component.TextBuffer, are. Players see the pixels drawn into the area
 * the text takes, scaled to fit.
 */
public interface PixelScreen {
    /**
     * Shows pixels instead of the text, all black to begin with; or the text again, with zero.
     */
    void setPixelMode(int width, int height);

    /**
     * Changes whole rows of pixels, given as 0xRRGGBB colors, row after row.
     */
    void setPixelRows(int firstRow, int[] colors);
}
