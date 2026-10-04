# RAID

![40 man instance.](oredict:oc:raid)

Der RAID-Block enthält drei [Festplatten](../item/hdd1.md), welche zu einem einzelnen Dateisystem kombiniert werden. Dieses kombinierte Dateisystem hat als Kapazität die Summe der Kapazitäten der einzelnen [Festplatten](../item/hdd1.md) und steht allen mit dem RAID verbundenen [Computern](../general/computer.md) zur Verfügung. Das RAID ist kein Linux-Laufwerk. Linux-Maschinen erreichen es nur über den Geräte-Bus, als `filesystem`-Gerät (öffnen, lesen, schreiben, auflisten, ... wie in der Dateisystem-API von OpenComputers).

Das RAID funktioniert nur (und wird nur als Dateisystem angezeigt), wenn drei [Festplatten](../item/hdd1.md) eingesetzt sind. Die [Festplatten](../item/hdd1.md) können sich in ihrer Größe unterscheiden.

Zu beachten ist, dass beim Hinzufügen einer [Festplatte](../item/hdd1.md) zum RAID-Block deren Inhalt gelöscht wird. Wird eine einzelne [Festplatte](../item/hdd1.md) aus einem vollständigen RAID entfernt, wird das gesamte RAID gelöscht. Wird die Festplatte wieder eingesetzt, werden die alten Dateien *nicht* wiederhergestellt; das RAID wird als leeres Dateisystem neu initialisiert.

Ein RAID-Block behält beim Zerstören seinen Inhalt und kann daher ohne Datenverlust sicher an einen anderen Ort gebracht werden.
