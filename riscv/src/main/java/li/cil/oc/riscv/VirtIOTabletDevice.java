package li.cil.oc.riscv;

import li.cil.sedna.api.memory.MemoryMap;
import li.cil.sedna.device.virtio.AbstractVirtIOInputDevice;
import li.cil.sedna.evdev.EvdevEvents;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

/**
 * A pointer at absolute positions, in pixels of the framebuffer, with three buttons and a wheel.
 * Linux sees it as a tablet, the input device next to the keyboard.
 */
public final class VirtIOTabletDevice extends AbstractVirtIOInputDevice {
    public static final int BUTTON_LEFT = 0x110;
    public static final int BUTTON_RIGHT = 0x111;
    public static final int BUTTON_MIDDLE = 0x112;

    private static final String NAME = "virtio_tablet";
    private static final int ABS_X = 0x00;
    private static final int ABS_Y = 0x01;
    private static final int REL_WHEEL = 0x08;

    private final int width;
    private final int height;

    public VirtIOTabletDevice(final MemoryMap memoryMap, final int width, final int height) {
        super(memoryMap);
        this.width = width;
        this.height = height;
    }

    public void move(final int x, final int y) {
        putEvents(
            packEvent(EvdevEvents.EV_ABS, ABS_X, x),
            packEvent(EvdevEvents.EV_ABS, ABS_Y, y),
            packEvent(EvdevEvents.EV_SYN, 0, 0));
    }

    public void button(final int button, final boolean isDown) {
        putEvents(
            packEvent(EvdevEvents.EV_KEY, button, isDown ? 1 : 0),
            packEvent(EvdevEvents.EV_SYN, 0, 0));
    }

    /**
     * Turns the wheel by notches, positive away from the user.
     */
    public void scroll(final int delta) {
        putEvents(
            packEvent(EvdevEvents.EV_REL, REL_WHEEL, delta),
            packEvent(EvdevEvents.EV_SYN, 0, 0));
    }

    @Override
    protected int generateConfigUnion(final int select, final int subsel, final ByteBuffer config) {
        config.order(ByteOrder.LITTLE_ENDIAN);
        switch (select) {
            case VIRTIO_INPUT_CFG_SELECT_ID_NAME -> {
                for (final char ch : NAME.toCharArray()) {
                    config.put((byte) ch);
                }
            }
            case VIRTIO_INPUT_CFG_SELECT_EV_BITS -> {
                switch (subsel) {
                    case EvdevEvents.EV_KEY -> config.put(bits(BUTTON_LEFT, BUTTON_RIGHT, BUTTON_MIDDLE));
                    case EvdevEvents.EV_ABS -> config.put(bits(ABS_X, ABS_Y));
                    case EvdevEvents.EV_REL -> config.put(bits(REL_WHEEL));
                    default -> {
                    }
                }
            }
            case VIRTIO_INPUT_CFG_SELECT_ABS_INFO -> {
                // struct virtio_input_absinfo { le32 min, max, fuzz, flat, res; }
                if (subsel == ABS_X || subsel == ABS_Y) {
                    config.putInt(0).putInt((subsel == ABS_X ? width : height) - 1).putInt(0).putInt(0).putInt(0);
                }
            }
            default -> {
            }
        }
        return config.position();
    }

    private static byte[] bits(final int... codes) {
        final BitSet bitmap = new BitSet();
        for (final int code : codes) {
            bitmap.set(code);
        }
        return bitmap.toByteArray();
    }
}
