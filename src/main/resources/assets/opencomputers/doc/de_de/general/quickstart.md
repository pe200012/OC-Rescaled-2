# Schnellstart-Guide

Auch bekannt als "Wie man seinen ersten Computer baut". Um deinen ersten [Computer](computer.md) zu starten, musst du ihn zuerst korrekt aufbauen. Es gibt verschiedene Typen von Computern in OpenComputers, aber starten wir zuerst mit dem einfachsten: dem Standardcomputer.

**Disclaimer**: Der Guide ist Schritt für Schritt und hilft beim späteren Finden von Fehlern, daher ist er recht lang. Wenn du noch nie einen Computer im echten Leben gebaut hast oder diesen Mod zum ersten Mal spielst, wird empfohlen, den ganzen Text zu lesen.

Zuerst wirst du ein [Computergehäuse](../block/case1.md) benötigen. Dies ist der Block, der alle Komponenten aufnimmt und das Verhalten des gebauten Computers bestimmt.

![Ein Computergehäuse.](oredict:oc:case3)

Wenn du die Oberfläche des [Computergehäuses](../block/case1.md) öffnest, wirst du rechts einige Slots sehen. Jede Komponente passt in jeden Slot ihrer Art: Um Stufen musst du dir keine Gedanken machen.

Ohne Komponenten sind [Computergehäuse](../block/case1.md) ziemlich nutzlos. Du kannst versuchen, deinen [Computer](computer.md) jetzt zu starten, aber er wird sofort eine Fehlermeldung in den Chatlog schreiben und sich durch ein Piepen bemerkbar machen. Zum Glück steht in der Fehlermeldung, was du tun kannst, um das Problem zu lösen: Der Computer benötigt Energie. Schließe deinen [Computer](computer.md) an eine Energiequelle an, entweder direkt oder über einen [Energiekonverter](../block/powerConverter.md).

Wenn du ihn nun starten möchtest, wird er dir sagen, dass du eine [CPU](../item/cpu1.md) brauchst. Es gibt eine Art von [CPU](../item/cpu1.md), aber sie kann mit 25, 50, 100 oder 200 MHz laufen: Benutze sie beim Schleichen, um umzuschalten. Schneller ist schöner, verbraucht aber mehr Energie; 50 MHz sind ein guter Anfang. Stecke sie in dein [Computergehäuse](../block/case1.md).

Als Nächstes wirst du aufgefordert, [Speicher (RAM)](../item/ram1.md) einzusetzen. Beachte, dass der Piep-Code jetzt anders ist: lang-kurz. [Speicher](../item/ram1.md) gibt es in Größen von 1 MB bis 32 MB, und die eingebauten Riegel addieren sich. Linux benötigt mindestens 8 MB; 16 MB sind komfortabel.

Und siehe da, den Computer jetzt einzuschalten produziert keine Fehlermeldungen mehr! Aber leider tut er auch nicht sehr viel. Zumindest piept er jetzt zweimal. Das bedeutet, dass der [Computer](computer.md) gestartet ist, aber sofort fehlgeschlagen ist. Hier kommt ein sehr nützliches Werkzeug ins Spiel: das [Messgerät](../item/analyzer.md). Es ermöglicht dir, viele OpenComputers-Blöcke und einige Blöcke anderer Mods zu analysieren. Um es auf den [Computer](computer.md) anzuwenden, benutze das [Messgerät](../item/analyzer.md) beim Schleichen auf das Gehäuse.

Du solltest jetzt den Fehler sehen, der den [Computer](computer.md) zum Absturz gebracht hat:
`no bootable EEPROM`

Der Computer führt das Programm aus, das auf seinem [EEPROM](../item/eeprom.md) liegt, und er hat keines. Einen [EEPROM](../item/eeprom.md) anzufertigen ist recht einfach, und für einen [Computer](computer.md) wollen wir einen mit dem Linux-Bootloader: Fertige einen [EEPROM](../item/eeprom.md) zusammen mit einem [Handbuch](../item/manual.md) an, um einen "EEPROM (Linux)" zu erhalten. Lege ihn in deinen [Computer](computer.md).

Der Bootloader sucht Linux auf der ersten [Festplatte](../item/hdd1.md). Setze eine [Festplatte](../item/hdd1.md) in das Gehäuse ein: Beim ersten Start des Computers wird Linux darauf installiert, und alles, was du dort speicherst, bleibt erhalten. Ohne [Festplatte](../item/hdd1.md) startet der Computer trotzdem, führt Linux aber aus dem Speicher aus und vergisst alles, wenn er stoppt; das ist gut zum Ausprobieren.

Drücke den Startknopf. Es lebt! Zumindest sollte es das. Wenn nicht, lief etwas falsch, und du solltest mit dem [Messgerät](../item/analyzer.md) nachforschen. Aber wenn er jetzt läuft, bist du so gut wie fertig. Es fehlt nur noch, dass er Eingaben entgegennimmt und eine Ausgabe anzeigt.

Um zu sehen, was der [Computer](computer.md) tut, brauchst du einen [Bildschirm](../block/screen1.md). Eine Grafikkarte wird nicht benötigt: Die Konsole wird direkt auf den [Bildschirm](../block/screen1.md) gezeichnet.
![Nein, es ist kein Flachbildschirm.](oredict:oc:screen3)

Platziere den [Bildschirm](../block/screen1.md) direkt neben deinem [Computergehäuse](../block/case1.md) oder verbinde ihn mit einem [Kabel](../block/cable.md). Du solltest jetzt Linux auf dem [Bildschirm](../block/screen1.md) starten sehen. Setze schließlich eine [Tastatur](../block/keyboard.md) entweder auf den [Bildschirm](../block/screen1.md) selbst oder so, dass sie zum [Bildschirm](../block/screen1.md) zeigt, um die Eingabe über die [Tastatur](../block/keyboard.md) zu ermöglichen.

Damit bist du fertig. Melde dich als `root` an; es gibt kein Passwort. Du befindest dich jetzt in einer Linux-Shell. Probiere `ls /mnt/builtin`, um zu sehen, was der Mod mitbringt, oder `micropython`, um eine Python-Eingabe zu erhalten. Die Seite [Linux](linux.md) erzählt dir mehr über das System, und [Programmierung](programming.md), wie du Redstone, Roboter und alles andere damit steuerst.

Viel Spaß beim Bauen komplexerer [Computer](computer.md), beim Herumspielen mit [Servern](../item/server1.md) und beim Zusammenbauen von [Robotern](../block/robot.md), [Drohnen](../item/drone.md), [Mikrocontrollern](../block/microcontroller.md) und [Tablets](../item/tablet.md) in der [Elektronik-Werkbank](../block/assembler.md).

Fröhliches Programmieren!
