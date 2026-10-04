# Bildschirme

![Kannst du das sehen?](oredict:oc:screen1)

Ein Bildschirm zeigt die Ausgabe von [Computern](../general/computer.md). Die Linux-Konsole zeichnet direkt auf den Bildschirm, daher wird dafür keine [Grafikkarte](../item/graphicsCard1.md) benötigt; eine Grafikkarte ist dennoch eine Komponente, die Programme ansprechen können. Bildschirme haben eine Auflösung von bis zu 160x50 Zeichen und 256 Farben, die Konsole hat 80x24 Zeichen. Dies gilt auch für die in [Roboter](robot.md) und [Tablets](../item/tablet.md) eingebauten Bildschirme.

Die Konsole verwendet den am Computergehäuse anliegenden Bildschirm, falls es einen gibt, ansonsten den ersten Bildschirm im Netzwerk. Nur an diesem Bildschirm angebrachte [Tastaturen](keyboard.md) schreiben darauf. Mehrere miteinander verkabelte Computer zeichnen nicht auf die Bildschirme der anderen.

Bildschirme können nebeneinander platziert werden, um größere Bildschirme zu ermöglichen, solange sie in dieselbe Richtung zeigen. Wenn sie nach oben oder unten ausgerichtet werden, müssen sie auch gleich rotiert werden. Die Richtung wird von einem Pfeil angezeigt, wenn der Bildschirm in der Hand gehalten wird.

Die Größe eines Bildschirms hat keinen Einfluss auf die verfügbare Auflösung. Um zu steuern, wie sich benachbarte Bildschirme verbinden, können sie zudem mit jedem Färbemittel gefärbt werden. Der Bildschirm muss dafür nur mit einem Färbemittel in der Hand rechts angeklickt werden. Das Färbemittel wird nicht aufgebraucht, allerdings geht die Farbe beim Abbau verloren. Bildschirme unterschiedlicher Farben werden sich nicht verbinden.

Bildschirme unterstützen außerdem Mauseingaben. Klicks können entweder in der Bildschirm-GUI ausgeführt werden (welche nur geöffnet werden kann, wenn eine [Tastatur](keyboard.md) mit dem Bildschirm verbunden ist), oder indem der Bildschirm in der Welt schleichend rechts angeklickt wird. Während ein Programm auf den Framebuffer zeichnet, werden Mausposition und Tasten in Framebuffer-Pixeln gemeldet (/dev/input/event1). In der Bildschirm-GUI bewegt eine Mausbewegung ohne gedrückte Taste auch den Zeiger.

Linux hat außerdem einen Framebuffer, /dev/fb0, mit 320x192 Pixeln. Sobald ein Programm darauf zeichnet, zeigt der Bildschirm die Pixel an. Wenn das Zeichnen für eine Sekunde aufhört und die Konsole wieder etwas ausgibt, kehrt der Bildschirm zum Text zurück. Probiere `micropython /mnt/builtin/example/framebuffer.py`.
