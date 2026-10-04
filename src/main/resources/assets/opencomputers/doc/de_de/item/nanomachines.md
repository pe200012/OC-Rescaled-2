# Nanomaschinen

![Nanomaschinen, Sohn.](oredict:oc:nanomachines)

Diese kleinen Kerlchen verbinden sich mit deinem Nervensystem, um dich härter, besser, schneller und stärker zu machen - oder um dich zu töten. Manchmal sogar beides gleichzeitig! Einfach gesagt stellen Nanomaschinen ein energiebetriebenes System bereit, um dem Spieler, in dem sie sich befinden, Buffs (und Debuffs) zu verleihen. Um Nanomaschinen zu "installieren", iss sie!

Sobald sie injiziert sind, zeigt eine neue Energieanzeige in deinem HUD an, wie viel Energie deine Nanomaschinen noch zur Verfügung haben. Du kannst sie aufladen, indem du in der Nähe eines [Ladegeräts](../block/charger.md) stehst. Je mehr du die Nanomaschinen benutzt, desto mehr Energie verbrauchen sie.

Nanomaschinen stellen eine bestimmte Anzahl an "Eingängen" bereit, die ausgelöst werden können und viele verschiedene Effekte auf den Spieler haben, von visuellen Effekten wie Partikeln in der Nähe des Spielers über ausgewählte Trankeffekte bis hin zu selteneren und besonderen Verhaltensweisen!

Welcher Eingang welchen Effekt auslöst, hängt von der aktuellen Konfiguration der Nanomaschinen ab, wobei die tatsächlichen "Verbindungen" pro Konfiguration zufällig sind. Das bedeutet, dass du verschiedene Eingänge aktivieren musst, um herauszufinden, was sie bewirken. Wenn du mit einer Konfiguration unzufrieden bist, kannst du deine Nanomaschinen jederzeit neu konfigurieren, indem du eine neue Ladung injizierst (einfach noch welche essen). Um die Nanomaschinen vollständig loszuwerden, solltest du etwas [Grog](acid.md) trinken. Beachte, dass das gleichzeitige Aktivieren zu vieler Eingänge schwere negative Auswirkungen auf dich hat!

Standardmäßig befinden sich die Nanomaschinen im Standby. Du musst sie mit drahtlosen Nachrichten steuern, daher wird dringend empfohlen, ein [Tablet](tablet.md) mit einer [Drahtlosnetzwerkkarte](wlanCard1.md) bei dir zu tragen. Nanomaschinen reagieren nur auf Funksignale von Geräten, die nicht weiter als zwei Meter entfernt sind, dafür aber auf Nachrichten an jedem Port und von jedem Gerät!

Nanomaschinen reagieren auf ein einfaches, proprietäres Protokoll: Jedes Paket muss aus mehreren Teilen bestehen, deren erster der "Header" ist und der Zeichenkette `nanomachines` entsprechen muss. Der zweite Teil muss der Befehlsname sein. Weitere Teile sind Parameter des Befehls. Die folgenden Befehle sind verfügbar, formatiert als `commandName(arg1, ...)`:

- `setResponsePort(port:number)` - Legt den Port fest, an den die Nanomaschinen Antwortnachrichten für Befehle mit Antwort senden sollen.
- `getPowerState()` - Fragt die aktuell gespeicherte und die maximal speicherbare Energie der Nanomaschinen ab.
- `getHealth()` - Fragt den Gesundheitszustand des Spielers ab.
- `getHunger()` - Fragt den Hungerzustand des Spielers ab.
- `getAge()` - Fragt das Alter des Spielers in Sekunden ab.
- `getName()` - Fragt den Anzeigenamen des Spielers ab.
- `getExperience()` - Fragt das Erfahrungslevel des Spielers ab.
- `getTotalInputCount()` - Fragt die Gesamtzahl der verfügbaren Eingänge ab.
- `getSafeActiveInputs()` - Fragt die Anzahl der *sicher* aktiven Eingänge ab.
- `getMaxActiveInputs()` - Fragt die Anzahl der *maximal* aktiven Eingänge ab.
- `getInput(index:number)` - Fragt den aktuellen Zustand des Eingangs mit dem angegebenen Index ab.
- `setInput(index:number, value:boolean)` - Setzt den Zustand des Eingangs mit dem angegebenen Index auf den angegebenen Wert.
- `getActiveEffects()` - Fragt eine Liste der aktiven Effekte ab. Beachte, dass manche Effekte möglicherweise nicht in dieser Liste erscheinen.
- `saveConfiguration()` - Benötigt einen Satz Nanomaschinen im Inventar des Spielers, in dem die aktuelle Konfiguration gespeichert wird.

Zum Beispiel in Micropython auf einer Linux-Maschine mit einer Drahtlosnetzwerkkarte:
- `bus.find("modem").broadcast(1, "nanomachines", "setInput", 1, True)` aktiviert den ersten Eingang.
- `bus.find("modem").broadcast(1, "nanomachines", "getHealth")` ruft die Gesundheitsinformationen des Spielers ab.

(Beide Zeilen setzen vorher `from devices import bus` voraus.)
