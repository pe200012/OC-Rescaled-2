# Redstone-I/O

![Hi Red.](oredict:oc:redstone)

Der Redstone-I/O-Block kann verwendet werden, um ferngesteuert Redstonesignale einzulesen und auszugeben. Er verhält sich wie eine [Redstonekarte](../item/redstoneCard1.md): Er kann einfache analoge sowie gebündelte Signale lesen und ausgeben, aber keine kabellosen Redstonesignale lesen oder ausgeben.

Bei der Angabe einer Seite für die Methoden der von diesem Block bereitgestellten Komponente sind die Richtungen die globalen Himmelsrichtungen der Welt. Die Textur des Blocks hat dezente Einkerbungen, die dem numerischen Wert jeder Seite entsprechen. In Micropython wird nach `from devices import bus` mit `rs = bus.find("redstone")` die Komponente gesucht, danach kann zum Beispiel `rs.setOutput(1, 15)` aufgerufen werden.

Genau wie die [Redstonekarten](../item/redstoneCard1.md) sendet dieser Block ein Signal-Ereignis (gelesen mit `bus.wait_event()`) an verbundene [Computer](../general/computer.md), wenn sich der Status eines Redstonesignals ändert - sowohl bei analogen als auch bei gebündelten Signalen. Dieser Block kann zudem so konfiguriert werden, dass er verbundene [Computer](../general/computer.md) aufweckt, sobald eine gewisse Signalstärke überschritten wird, wodurch [Computer](../general/computer.md) automatisch hochgefahren werden können.
