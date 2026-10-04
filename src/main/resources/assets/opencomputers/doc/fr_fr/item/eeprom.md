# EEPROM

![Let's get this party started.](oredict:oc:eeprom)

L'EEPROM contient 64 Ko et est ce qui contient le code utilisé pour initialiser un ordinateur quand il démarre. Un ordinateur démarre ce qui se trouve sur son EEPROM, qui doit être un programme RISC-V. Fabriquer une EEPROM avec le [manuel](manual.md) donne une « EEPROM (Linux) », qui contient le programme de démarrage de Linux.

Les EEPROMs peuvent être programmées avec des programmes bare-metal pour un usage spécifique, comme les [drones](drone.md) et les [microcontrôleurs](../block/microcontroller.md). Si l'EEPROM contient du texte, l'ordinateur signale « EEPROM holds text, not a RISC-V program ». S'il n'y a pas d'EEPROM ou si elle est vide, l'ordinateur signale « no bootable EEPROM ». Utilisez l'[analyseur](analyzer.md) sur la machine pour voir les erreurs de démarrage.
