# 3D-Drucker

![2D printing is so yesteryear.](oredict:oc:printer)

3D-Drucker erlauben es, Blöcke von jeder Form mit jeder Art von Textur zu drucken. Um mit 3D-Druckern anzufangen, wird ein 3D-Drucker-Block neben einem Computer platziert. Dadurch erhält man Zugriff auf die `printer3d`-Komponenten-API. Hiermit können [Modelle](print.md) mit den bereitgestellten Funktionen erstellt und gedruckt werden.

Unter Linux ist der Drucker ein Gerät am Geräte-Bus, daher kann ein Micropython-Programm ihn verwenden:
`from devices import bus`
`p = bus.find("printer3d")`
`print(p)` listet seine Methoden auf.

Um Modelle drucken zu können, muss ein 3D-Drucker über einen [Computer](../general/computer.md) konfiguriert werden. Wenn der Drucker auf Non-Stop gesetzt wird, wird der Computer danach nicht mehr benötigt. Auch eine [Druckerpatrone](../item/inkCartridge.md) und ein bisschen [Chamelium](../item/chamelium.md) werden als Ausgangsmaterial benötigt. Die Menge an Chamelium hängt vom Volumen des Drucks ab, während die Menge der benötigten Tinte von der Oberfläche des gedruckten Items abhängt.

Formen, Texturen und Bezeichnung werden mit den Methoden der Komponente festgelegt, danach wird der Druck mit `p.commit(1)` gestartet.
