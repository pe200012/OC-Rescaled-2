# Carte de Redstone

![Voir rouge.](oredict:oc:redstoneCard1)

La carte de redstone permet aux [ordinateurs](../general/computer.md) de lire et émettre des signaux de redstone analogiques dans les blocs adjacents. Quand la force d'un signal entrant change, un événement est injecté dans l'[ordinateur](../general/computer.md). Les programmes utilisent la carte via le bus de périphériques :

`from devices import bus`
`rs = bus.find("redstone")`
`rs.setOutput(1, 15)`
`e = bus.wait_event(5000)`

Si un des mods supportés fournissant des câbles empaquetés (bundled) est présent, comme RedLogic, Project:Red ou Minefactory Reloaded ; ou un mod qui fournit des capacités de signal sans fil, comme WirelessRedstone-ChickenBonesEdition (WR-CBE), ou le Slimevoid's Wireless mod, la carte permet aussi d'interagir avec ces systèmes.

Le côté renseigné dans les méthodes de la carte est relatif à l'orientation du [boîtier d'ordinateur](../block/case1.md) / [robot](../block/robot.md) / [rack](../block/rack.md). Cela signifie que quand vous regardez l'avant d'un ordinateur, le côté droit est à votre gauche, et inversement.
