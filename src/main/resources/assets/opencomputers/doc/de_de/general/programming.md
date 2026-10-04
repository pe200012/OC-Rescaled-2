# Programmierung

Programme auf einem [Computer](computer.md) erreichen die Welt über den Geräte-Bus: Jede Komponente, die die Maschine sehen kann, etwa eine [Redstonekarte](../item/redstoneCard1.md), ein [Roboter](../block/robot.md), ein [Bildschirm](../block/screen1.md) oder der Block eines anderen Mods über einen [Adapter](../block/adapter.md), ist ein Gerät, das du aufrufen kannst. Komponenten haben dieselben Methoden wie im ursprünglichen Mod; die Tooltips und Seiten dieser Bedienungsanleitung oder das Ausgeben eines Geräts verraten dir, welche.

## micropython

`from devices import bus`
`rs = bus.find("redstone")`
`rs.setOutput(1, 15)`
`print(rs)`

`bus.find` liefert das erste Gerät einer Art, `bus.list()` listet alle auf, und die Ausgabe eines Geräts listet seine Methoden auf. Eine Methode, die mehrere Werte zurückgibt, liefert sie als Liste. Binärdaten kommen als Bytes zurück und können als Bytes übergeben werden; für mehr als 4 KB übergibst du `bus.blob(data)`.

Signale, etwa Tastendrücke, Netzwerknachrichten oder Redstone-Änderungen, kommen als Ereignisse an:
`e = bus.wait_event(5000)`
wartet bis zu fünf Sekunden und liefert das nächste mit seinem `type`, der `deviceId`, von der es kam, und seinen `data`. `bus.wait_event(5000, "redstone_changed")` wartet nur auf eine Art.

Die Maschine selbst ist das Gerät `computer`, immer das erste, mit `beep`, `energy`, `maxEnergy`, `uptime`, `users`, `addUser`, `removeUser` und `pushSignal`.

## Die Shell

`component` ruft Komponenten direkt aus der Shell auf, auch über `ssh`. Allein aufgerufen listet es die Geräte auf, `component redstone` listet die Methoden eines Geräts, nach Typ oder ID, und
`component redstone setOutput 1 15`
ruft eine davon auf. Argumente werden als JSON gelesen, wo das geht, sonst als Text, und Ergebnisse werden als JSON ausgegeben. `component wait` wartet auf das nächste Signal und gibt es aus.

## Roboter

`import robot`
`robot.forward()`
`robot.turn_left()`
`robot.swing(robot.DOWN)`
Das Modul `robot` bewegt den [Roboter](../block/robot.md) und lässt ihn handeln: `forward`, `back`, `up`, `down`, `turn_left`, `turn_right`, `detect`, `swing`, `use`, `place`, `drop`, `suck`, `select`, `count` und mehr. Aktionen wirken nach vorne, sofern keine Seite angegeben wird (`robot.UP` oder `robot.DOWN`), und liefern `True`, oder `None`, wenn sie fehlschlugen; `robot.component` enthält alle Methoden des Roboters und sagt, warum etwas fehlschlug. Die Maschine pausiert, während sich der [Roboter](../block/robot.md) bewegt.

## Lua und C

Lua funktioniert auf dieselbe Weise:
`local bus = require("devices")`
`local rs = bus:find("redstone")`
`rs:setOutput(1, 15)`
`lua /mnt/builtin/bin/lsdev.lua` listet alle Geräte auf.

Aus C bindest du `oc.h` aus `/mnt/builtin/include` ein, das direkt mit den Komponenten spricht (als `root`). Kompiliere mit `tcc`.

## Bare-Metal-Programme

[Drohnen](../item/drone.md) und [Mikrocontroller](../block/microcontroller.md) führen kein Linux aus. Sie führen ein einzelnes Programm aus ihrem [EEPROM](../item/eeprom.md) aus, in C gegen dasselbe `oc.h` geschrieben. Baue es auf einem [Computer](computer.md) mit Linux und schreibe es in den [EEPROM](../item/eeprom.md) in diesem Computer:
`ocbuild -o blink.bin blink.c`
`ocflash blink.bin blink`
Nimm dann den [EEPROM](../item/eeprom.md) heraus und setze ihn in den [Mikrocontroller](../block/microcontroller.md) oder die [Drohne](../item/drone.md) ein. Das Programm startet, wenn das Gerät startet, und das Gerät schaltet sich ab, wenn `main` zurückkehrt. Stürzt das Programm ab, zeigt das [Messgerät](../item/analyzer.md), wo. `/mnt/builtin/example` enthält einen [Mikrocontroller](../block/microcontroller.md), der Redstone blinken lässt (`blink.c`), und eine [Drohne](../item/drone.md), die ein Quadrat fliegt (`drone.c`).
