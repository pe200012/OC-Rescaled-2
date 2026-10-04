# Festplatte

![01010011 01110000 01100001 01100011 01100101.](oredict:oc:hdd1)

Festplatten sind das wichtigste Speichermedium in OpenComputers. Sie sind in 16, 32 und 64 MB erhältlich. Jede ist unter Linux eine rohe Platte: `/dev/vda` ist die erste, `/dev/vdb` die nächste und so weiter. Auf der ersten Festplatte wird Linux automatisch installiert, wenn der [Computer](../general/computer.md) zum ersten Mal startet. Andere Festplatten sind zunächst leer; sie mit `mke2fs` und `mount` vorbereiten. Festplatten nur austauschen, während die Maschine ausgeschaltet ist.

Festplatten können in einem [RAID](../block/raid.md) platziert werden, das sie zu einem OpenComputers-Dateisystem zusammenfasst. Linux sieht es nur als `filesystem`-Gerät am Gerätebus, nicht als Platte. Zu beachten ist, dass beim Platzieren einer Festplatte in einem [RAID](../block/raid.md) ihre Daten gelöscht werden.
