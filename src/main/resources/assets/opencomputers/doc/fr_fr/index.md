# Manuel d'OpenComputers

OpenComputers est un mod qui ajoute au jeu des [ordinateurs](general/computer.md), des [serveurs](item/server1.md), des [robots](block/robot.md), et des [drones](item/drone.md) persistants, modulaires et très configurables. Dans cette version, chaque appareil est une machine RISC-V 64 bits émulée : les ordinateurs, les serveurs, les robots et les tablettes exécutent [Linux](general/linux.md), tandis que les drones et les micro-contrôleurs exécutent de petits programmes bare-metal depuis leur [EEPROM](item/eeprom.md). Vous les programmez en micropython ou en C, voir [programmation](general/programming.md).

Pour apprendre à utiliser ce manuel, allez sur [la page parlant du manuel](item/manual.md) (Ce texte en vert est un lien, vous pouvez cliquer dessus).

## Table des matières

### Appareils
- [Ordinateurs](general/computer.md)
- [Serveurs](item/server1.md)
- [Micro-contrôleurs](block/microcontroller.md)
- [Robots](block/robot.md)
- [Drones](item/drone.md)
- [Tablettes](item/tablet.md)

### Logiciel et programmation
- [Linux](general/linux.md)
- [Programmation](general/programming.md)

### Blocs et objets
- [Objets](item/index.md)
- [Blocs](block/index.md)

### Guides
- [Bien démarrer](general/quickstart.md)

## Vue d'ensemble

Les ordinateurs d'OpenComputers sont persistants : quand le monde est sauvegardé ou que le chunk dans lequel se trouve un [ordinateur](general/computer.md) est déchargé, la machine entière, mémoire comprise, est sauvegardée, et elle reprend là où elle s'était arrêtée quand le chunk est rechargé. Les programmes continuent de s'exécuter comme si de rien n'était. Cela fonctionne pour tous les appareils sauf les [tablettes](item/tablet.md), qui repartent à zéro.

Tous les appareils sont modulaires et peuvent être assemblés avec une grande variété de composants, comme les [ordinateurs](general/computer.md) de la vie réelle. Le matériel n'a pas de niveaux : il n'existe qu'un seul type de [processeur](item/cpu1.md), de [boîtier d'ordinateur](block/case1.md), d'[écran](block/screen1.md) et ainsi de suite, et chaque emplacement accepte n'importe quel composant. Les machines diffèrent plutôt par ce que vous y mettez : la fréquence d'horloge du [processeur](item/cpu1.md), la quantité de [mémoire](item/ram1.md) et la taille des [disques durs](item/hdd1.md). Les machines plus rapides et plus grandes consomment plus d'énergie.

Les programmes accèdent à tous les composants d'OpenComputers, et aux blocs des autres mods via l'[adaptateur](block/adapter.md), par le bus de périphériques décrit dans [programmation](general/programming.md). L'énergie peut être fournie grâce à une large gamme de mods, incluant, sans limitation, les Redstone Flux, les EU d'IndustrialCraft2, les Joules de Mekanism, l'énergie d'Applied Energistics 2 autant que la charge de Factorization.

Les [ordinateurs](general/computer.md) sont la base. Les [serveurs](item/server1.md) fonctionnent de la même manière mais vivent dans un [rack](block/rack.md). Les [robots](block/robot.md) sont des [ordinateurs](general/computer.md) mobiles capables d'interagir avec le monde, et les [tablettes](item/tablet.md) sont des ordinateurs que l'on porte sur soi ; les deux exécutent [Linux](general/linux.md) avec un [écran](block/screen1.md) intégré. Une fois qu'un [robot](block/robot.md) est construit, les composants à l'intérieur ne peuvent pas être retirés ; construisez-le avec des conteneurs d'[amélioration](item/upgradeContainer1.md) ou de [carte](item/cardContainer1.md) pour échanger des éléments plus tard, ou [désassemblez](block/disassembler.md)-le. Les [drones](item/drone.md) et les [micro-contrôleurs](block/microcontroller.md) n'ont pas de disque et trop peu de place pour Linux : ils exécutent un seul programme bare-metal écrit sur leur [EEPROM](item/eeprom.md).

Ce manuel contient des informations sur tous les blocs et objets, sur la mise en place des différents types d'appareils, ainsi qu'une introduction à [Linux](general/linux.md) et à leur [programmation](general/programming.md).
