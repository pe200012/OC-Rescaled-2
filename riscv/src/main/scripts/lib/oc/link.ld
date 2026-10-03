/* link.ld - lays out bare-metal programs built with gcc or clang.
 *
 * The machine copies the EEPROM to the start of RAM, 0x80000000, and jumps
 * there. Turn the result into an image with objcopy -O binary.
 */

OUTPUT_ARCH(riscv)
ENTRY(_start)

SECTIONS
{
    . = 0x80000000;

    .text : {
        KEEP(*(.text.start))
        *(.text .text.*)
    }

    .rodata : {
        *(.rodata .rodata.* .srodata .srodata.*)
    }

    .data : {
        *(.data .data.* .sdata .sdata.*)
    }

    .bss (NOLOAD) : ALIGN(8) {
        __bss_start = .;
        *(.sbss .sbss.* .bss .bss.* COMMON)
        . = ALIGN(8);
        _end = .;
    }

    /DISCARD/ : {
        *(.comment .note .note.* .eh_frame .riscv.attributes)
    }
}
