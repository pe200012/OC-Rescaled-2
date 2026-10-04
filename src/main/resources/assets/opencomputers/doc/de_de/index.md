# OpenComputers Bedienungsanleitung

OpenComputers ist eine Modifikation, welche dauerhafte, modulare und hochkonfigurierbare [Computer](general/computer.md), [Server](item/server1.md), [Roboter](block/robot.md) und [Drohnen](item/drone.md) zum Spiel hinzufügt. In dieser Version ist jedes Gerät eine emulierte 64-Bit-RISC-V-Maschine: Computer, Server, Roboter und Tablets führen [Linux](general/linux.md) aus, während Drohnen und Mikrocontroller kleine Bare-Metal-Programme aus ihrem [EEPROM](item/eeprom.md) ausführen. Du programmierst sie in micropython oder C, siehe [Programmierung](general/programming.md).

Um zu lernen, wie man die Bedienungsanleitung verwendet, siehe [die Seite über das Handbuch](item/manual.md) (der grüne Text ist ein Link - du kannst ihn anklicken!).

## Inhaltsverzeichnis

### Geräte
- [Computer](general/computer.md)
- [Server](item/server1.md)
- [Mikrocontroller](block/microcontroller.md)
- [Roboter](block/robot.md)
- [Drohnen](item/drone.md)
- [Tablets](item/tablet.md)

### Software und Programmierung
- [Linux](general/linux.md)
- [Programmierung](general/programming.md)

### Blöcke und Items
- [Items](item/index.md)
- [Blöcke](block/index.md)

### Guides
- [Erste Schritte](general/quickstart.md)

## Überblick

Computer in OpenComputers sind dauerhaft: Wenn die Welt gespeichert wird oder der Chunk, in dem sich ein [Computer](general/computer.md) befindet, entladen wird, wird die gesamte Maschine samt Speicher gesichert und macht weiter, wo sie aufgehört hat, sobald der Chunk wieder geladen wird. Programme laufen weiter, als wäre nichts geschehen. Dies funktioniert bei allen Geräten außer [Tablets](item/tablet.md), die neu starten.

Alle Geräte sind modular und können mit einer großen Palette von Komponenten zusammengestellt werden, ähnlich wie es bei Computern im echten Leben der Fall ist. Die Hardware ist nicht in Stufen unterteilt: Es gibt eine Art von [CPU](item/cpu1.md), [Computergehäuse](block/case1.md), [Bildschirm](block/screen1.md) und so weiter, und jeder Slot nimmt jede Komponente auf. Maschinen unterscheiden sich stattdessen in dem, was man einbaut: der Taktrate der [CPU](item/cpu1.md), der Menge an [Speicher](item/ram1.md) und der Größe der [Festplatten](item/hdd1.md). Schnellere und größere Maschinen verbrauchen mehr Energie.

Programme erreichen alle OpenComputers-Komponenten, und über den [Adapter](block/adapter.md) auch die Blöcke anderer Mods, über den Geräte-Bus, der unter [Programmierung](general/programming.md) beschrieben ist. Strom kann mit einer großen Palette von Mods zur Verfügung gestellt werden, darunter Redstone Flux, IndustrialCraft2 EU, Mekanism Joules, Applied Energistics 2-Energie sowie Factorization Charge.

[Computer](general/computer.md) sind die Grundlinie. [Server](item/server1.md) funktionieren genauso, befinden sich aber in einem [Serverschrank](block/rack.md). [Roboter](block/robot.md) sind sich bewegende [Computer](general/computer.md), die mit der Welt interagieren können, und [Tablets](item/tablet.md) sind Computer, die man bei sich trägt; beide führen [Linux](general/linux.md) mit einem eingebauten [Bildschirm](block/screen1.md) aus. Sobald ein [Roboter](block/robot.md) gebaut ist, können die Komponenten in seinem Inneren nicht mehr entfernt werden; baue ihn mit [Upgrade-Containern](item/upgradeContainer1.md) oder [Karten-Containern](item/cardContainer1.md), um später Teile auszutauschen, oder [demontiere](block/disassembler.md) ihn. [Drohnen](item/drone.md) und [Mikrocontroller](block/microcontroller.md) haben keinen Datenträger und zu wenig Platz für Linux: Sie führen ein einzelnes Bare-Metal-Programm aus, das auf ihren [EEPROM](item/eeprom.md) geschrieben wurde.

Diese Bedienungsanleitung enthält Informationen über alle Blöcke und Items, wie man verschiedene Gerätetypen aufsetzt, und eine Einführung in [Linux](general/linux.md) und die [Programmierung](general/programming.md) dieser Geräte.
