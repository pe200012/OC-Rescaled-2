# EEPROM

![Let's get this party started.](oredict:oc:eeprom)

Das EEPROM fasst 64 KB und enthält den Code, der verwendet wird, um einen Computer beim Starten zu initialisieren. Ein Computer startet das, was auf seinem EEPROM liegt, und das muss ein RISC-V-Programm sein. Wenn ein EEPROM mit dem [Handbuch](manual.md) gecraftet wird, entsteht ein "EEPROM (Linux)", das den Linux-Bootloader enthält.

EEPROMs können mit Bare-Metal-Programmen für spezialisierte Aufgaben programmiert werden, wie es bei [Drohnen](drone.md) oder [Microcontrollern](../block/microcontroller.md) der Fall ist. Wenn das EEPROM Text enthält, meldet der Computer "EEPROM holds text, not a RISC-V program". Wenn kein EEPROM vorhanden oder es leer ist, meldet der Computer "no bootable EEPROM". Mit dem [Messgerät](analyzer.md) an der Maschine können Bootfehler angezeigt werden.
