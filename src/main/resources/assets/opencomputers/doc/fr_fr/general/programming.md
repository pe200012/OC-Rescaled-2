# Programmation

Les programmes d'un [ordinateur](computer.md) accèdent au monde par le bus de périphériques : chaque composant que la machine peut voir, comme une [carte de redstone](../item/redstoneCard1.md), un [robot](../block/robot.md), un [écran](../block/screen1.md), ou le bloc d'un autre mod via un [adaptateur](../block/adapter.md), est un périphérique que vous pouvez appeler. Les composants ont les mêmes méthodes que dans le mod d'origine ; les infobulles et les pages de ce manuel, ou l'affichage d'un périphérique, vous indiquent lesquelles.

## micropython

`from devices import bus`
`rs = bus.find("redstone")`
`rs.setOutput(1, 15)`
`print(rs)`

`bus.find` renvoie le premier périphérique d'un type, `bus.list()` les liste tous, et afficher un périphérique liste ses méthodes. Une méthode qui renvoie plusieurs valeurs les renvoie sous forme de liste. Les données binaires reviennent sous forme de bytes et peuvent être passées sous forme de bytes ; au-delà de 4 Ko, passez `bus.blob(data)`.

Les signaux, comme les pressions de touches, les messages réseau ou les changements de redstone, arrivent sous forme d'événements :
`e = bus.wait_event(5000)`
attend jusqu'à cinq secondes et renvoie le suivant, avec son `type`, le `deviceId` d'où il vient et ses `data`. `bus.wait_event(5000, "redstone_changed")` n'attend qu'un seul type.

La machine elle-même est le périphérique `computer`, toujours le premier, avec `beep`, `energy`, `maxEnergy`, `uptime`, `users`, `addUser`, `removeUser` et `pushSignal`.

## Robots

`import robot`
`robot.forward()`
`robot.turn_left()`
`robot.swing(robot.DOWN)`
Le module `robot` déplace le [robot](../block/robot.md) et lui permet d'agir : `forward`, `back`, `up`, `down`, `turn_left`, `turn_right`, `detect`, `swing`, `use`, `place`, `drop`, `suck`, `select`, `count` et plus encore. Les actions s'exercent vers l'avant sauf si un côté est indiqué (`robot.UP` ou `robot.DOWN`) et renvoient `True`, ou `None` en cas d'échec ; `robot.component` contient toutes les méthodes du robot et indique pourquoi quelque chose a échoué. La machine se met en pause pendant que le [robot](../block/robot.md) se déplace.

## Lua et C

Lua fonctionne de la même manière :
`local bus = require("devices")`
`local rs = bus:find("redstone")`
`rs:setOutput(1, 15)`
`lua /mnt/builtin/bin/lsdev.lua` liste tous les périphériques.

En C, incluez `oc.h` depuis `/mnt/builtin/include`, qui communique directement avec les composants (en tant que `root`). Compilez avec `tcc`.

## Programmes bare-metal

Les [drones](../item/drone.md) et les [micro-contrôleurs](../block/microcontroller.md) n'exécutent pas Linux. Ils exécutent un seul programme depuis leur [EEPROM](../item/eeprom.md), écrit en C avec le même `oc.h`. Compilez-le sur un [ordinateur](computer.md) Linux et écrivez-le sur l'[EEPROM](../item/eeprom.md) de cet ordinateur :
`ocbuild -o blink.bin blink.c`
`ocflash blink.bin blink`
Ensuite retirez l'[EEPROM](../item/eeprom.md) et placez-la dans le [micro-contrôleur](../block/microcontroller.md) ou le [drone](../item/drone.md). Le programme démarre quand l'appareil démarre, et l'appareil s'éteint quand `main` retourne. Si le programme plante, l'[analyseur](../item/analyzer.md) indique où. `/mnt/builtin/example` contient un [micro-contrôleur](../block/microcontroller.md) qui fait clignoter de la redstone (`blink.c`) et un [drone](../item/drone.md) qui vole en carré (`drone.c`).
