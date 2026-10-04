# Linux

[Computer](computer.md), [Server](../item/server1.md), [Roboter](../block/robot.md) und [Tablets](../item/tablet.md) führen Linux auf einem emulierten 64-Bit-RISC-V-Prozessor aus. Es ist ein kleines, aber echtes Linux: eine BusyBox-Shell mit den üblichen Werkzeugen, die Editoren `nano` und `vi`, `micropython`, Lua 5.4, der C-Compiler `tcc` sowie `ssh`/`scp` (dropbear). Melde dich als `root` an; es gibt kein Passwort.

## Starten

Der [EEPROM](../item/eeprom.md) "EEPROM (Linux)", hergestellt aus einem [EEPROM](../item/eeprom.md) und einem [Handbuch](../item/manual.md), enthält den Bootloader. Er startet Linux von der ersten [Festplatte](../item/hdd1.md). Auf einer neuen ersten [Festplatte](../item/hdd1.md) wird Linux beim ersten Start der Maschine installiert. Ohne [Festplatte](../item/hdd1.md) läuft Linux als Live-System aus dem Speicher, das alles vergisst, sobald die Maschine stoppt. Linux benötigt mindestens 8 MB [Speicher](../item/ram1.md).

Wenn eine Maschine nicht startet, wende ein [Messgerät](../item/analyzer.md) beim Schleichen auf sie an, um den Grund zu sehen.

## Datenträger

[Festplatten](../item/hdd1.md) und [Disketten](../item/floppy.md) sind einfache Datenträger: `/dev/vda` ist die erste [Festplatte](../item/hdd1.md), `/dev/vdb` die nächste, und die Diskettenlaufwerke folgen nach den [Festplatten](../item/hdd1.md). Der Diskettenslot des Gehäuses und jedes [Diskettenlaufwerk](../block/diskDrive.md), das die Maschine erreichen kann, ist je ein Laufwerk. Neue Datenträger sind leer; gib ihnen ein Dateisystem und binde sie ein:
`mke2fs /dev/vdb`
`mount /dev/vdb /mnt`
[Disketten](../item/floppy.md) können eingelegt und entnommen werden, während Linux läuft; hänge sie vorher mit `umount` aus. [Festplatten](../item/hdd1.md) und neu angeschlossene [Diskettenlaufwerke](../block/diskDrive.md) erfordern einen Neustart der Maschine. Die Datenträgerabbilder liegen im Weltordner unter `opencomputers-riscv/disks`, wo du sie auch außerhalb des Spiels öffnen kannst.

## Bildschirm, Tastatur und Maus

Die Konsole hat 80x24 Zeichen. Text lässt sich mit der mittleren Maustaste oder der Einfügetaste (standardmäßig Einfg) einfügen.

Programme können auch Pixel zeichnen: `/dev/fb0` ist ein Framebuffer mit 320x192 Pixeln und 32 Bit pro Pixel. Sobald ein Programm zeichnet, zeigt der [Bildschirm](../block/screen1.md) die Pixel; wenn es eine Sekunde lang aufhört und die Konsole wieder ausgibt, zeigt der [Bildschirm](../block/screen1.md) wieder Text. Tastendrücke kommen außerdem bei `/dev/input/event0` an, und solange der [Bildschirm](../block/screen1.md) Pixel zeigt, meldet `/dev/input/event1` in Pixeln, wo geklickt, gezogen und gescrollt wird. In der Welt kannst du einen [Bildschirm](../block/screen1.md) per Rechtsklick beim Schleichen anklicken. Probiere `micropython /mnt/builtin/example/framebuffer.py`.

## Netzwerk

[Netzwerkkarten](../item/lanCard.md) und [Drahtlosnetzwerkkarten](../item/wlanCard1.md) sind Netzwerkschnittstellen, `eth0`, `eth1` und so weiter, und Linux spricht TCP/IP über die [Kabel](../block/cable.md), [Relais](../block/relay.md) und das drahtlose Netzwerk von OpenComputers. Schnittstellen starten unkonfiguriert:
`ip addr add 10.0.0.1/24 dev eth0`
`ip link set eth0 up`
Gib der nächsten Maschine `10.0.0.2`, und `ping 10.0.0.1` erreicht die erste. `lua /mnt/builtin/bin/setup-network.lua` fragt nach den Einstellungen und speichert sie.

Eine [Internetkarte](../item/internetCard.md) ist die nächste Schnittstelle nach den Netzwerkkarten, mit einem Gateway unter `10.0.2.2`:
`ip addr add 10.0.2.15/24 dev eth1`
`ip link set eth1 up`
`ip route add default via 10.0.2.2`
`echo nameserver 1.1.1.1 > /etc/resolv.conf`
Danach erreichen `wget` und `ssh` das echte Internet. Verbindungen können nur nach außen aufgebaut werden.

## Speichern und Beenden

Eine laufende Maschine wird mit der Welt gespeichert und macht weiter, wo sie aufgehört hat; [Tablets](../item/tablet.md) starten stattdessen neu. `poweroff` schaltet die Maschine aus, `reboot` startet sie neu, und nach Änderungen an [Speicher](../item/ram1.md), [Festplatten](../item/hdd1.md), Netzwerkkarten oder [EEPROM](../item/eeprom.md) ist ein Neustart nötig. Eine Maschine mit ihrem Einschaltknopf auszuschalten oder ihr die Energie ausgehen zu lassen, ist wie das Ziehen des Netzsteckers: Dateien, die noch nicht geschrieben wurden, gehen verloren, aber das Dateisystem des Systems bleibt intakt.

## Die mitgelieferten Dateien

`/mnt/builtin` enthält, was der Mod mitbringt: die Bibliotheken für den Geräte-Bus, Beispiele sowie die Werkzeuge und Header für C. Wie man sie benutzt, steht unter [Programmierung](programming.md).
