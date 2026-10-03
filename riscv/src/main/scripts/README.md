Guest-side scripts served to the machine as the `builtin` 9p file system, mounted at
`/mnt/builtin`.

Most of it is copied from OpenComputers II (https://github.com/fnuecke/oc2,
`common/src/main/scripts`), MIT licensed, Copyright (c) Florian "Sangar" Nücke: the device bus
daemon and libraries in `bin/` and `lib/lua`, `lib/micropython`, and `init.d/`.

Written for this port:

- `include/oc.h`: components from C through the component window, for bare-metal programs and Linux.
- `lib/oc/crt0.c`, `lib/oc/link.ld`: startup and layout of bare-metal programs.
- `bin/ocbuild`, `bin/ocflash`, `lib/oc/ocflash.c`: build them with tcc and write them to an EEPROM.
- `example/`: bare-metal examples.

Bare-metal programs also build outside the game, e.g. with MSYS2's `riscv64-unknown-elf-gcc`:

    riscv64-unknown-elf-gcc -march=rv64gc -mabi=lp64d -mcmodel=medany -mno-relax \
        -ffreestanding -nostdlib -Os -Iinclude -T lib/oc/link.ld -Wl,--no-warn-rwx-segments \
        -o blink.elf lib/oc/crt0.c example/blink.c -lgcc
    riscv64-unknown-elf-objcopy -O binary blink.elf blink.bin
