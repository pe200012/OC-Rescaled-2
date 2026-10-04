# Redstonekarte

![Sieht rot.](oredict:oc:redstoneCard1)

Die Redstonekarte ermöglicht [Computern](../general/computer.md) das Lesen und Senden von analogen Redstonesignalen in benachbarten Blöcken. Wenn sich die Stärke eines eingehenden Signals ändert, wird ein Ereignis in den [Computer](../general/computer.md) eingespeist. Programme nutzen die Karte über den Gerätebus:

`from devices import bus`
`rs = bus.find("redstone")`
`rs.setOutput(1, 15)`
`e = bus.wait_event(5000)`

Wenn unterstützte Mods vorhanden sind, die gebündelte Redstone-Anbindungen (wie RedLogic, Project Red oder MineFactory Reloaded) oder kabellose Redstone-Anbindungen (wie WR-CBE oder Slimevoids Wireless Mod) zur Verfügung stellen, ermöglicht die Karte auch die Interaktion mit diesen Systemen.

Die den verschiedenen Methoden übergebenen Seiten sind aus Sicht des [Computergehäuses](../block/case1.md) / [Roboters](../block/robot.md) / [Serverschranks](../block/rack.md) zu sehen. Mit Blick auf die Vorderseite des Computers ist die rechte Seite also auf der linken Seite und andersrum.
