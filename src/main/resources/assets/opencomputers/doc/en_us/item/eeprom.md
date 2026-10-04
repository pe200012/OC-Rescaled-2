# EEPROM

![Let's get this party started.](oredict:oc:eeprom)

The EEPROM holds 64 KB and is what contains the code used to initialize a computer when it is being booted. A computer boots what is on its EEPROM, which must be a RISC-V program. Crafting an EEPROM with the [manual](manual.md) gives an "EEPROM (Linux)", which holds the Linux boot loader.

EEPROMs can be programmed with bare-metal programs for specialized purposes, such as [drones](drone.md) and [microcontrollers](../block/microcontroller.md). If the EEPROM holds text, the computer reports "EEPROM holds text, not a RISC-V program". If there is no EEPROM or it is empty, the computer reports "no bootable EEPROM". Use the [analyzer](analyzer.md) on the machine to see boot errors.
