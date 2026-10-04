# Boitier de drone

![Le vol du bourdon.](oredict:oc:droneCase1)

Le boitier de drone est utilisé pour construire des [drones](drone.md) dans l'[assembleur](../block/assembler.md). Les [drones](drone.md) sont des machines légères, rapides et très mobiles avec des fonctionnalités réduites (moins d'améliorations et d'emplacements de composant disponibles). Contrairement aux [robots](../block/robot.md), ils ne peuvent pas utiliser d'outils, et peuvent seulement interagir avec le monde de manière relativement limitée.

Ils compensent leurs limitations par leur vitesse et leur faible coût de fonctionnement en énergie. Ils sont bien adaptés au transport de petites quantités d'objets, et sont idéaux pour de la reconnaissance. Appairer un [drone](drone.md) à un [robot](../block/robot.md) peut être très intéressant, si le [robot](../block/robot.md) fait le "gros" du travail, et que le [drone](drone.md) fournit des informations sur l'environnement et transporte les objets vers et depuis un point central, par exemple.

Comme les [micro-contrôleurs](../block/microcontroller.md), les [drones](drone.md) n'ont pas de disque. Ils exécutent un programme bare-metal depuis leur [EEPROM](eeprom.md). Par conséquent, l'[EEPROM](eeprom.md) peut être changée en re-fabriquant le [drone](drone.md) avec une autre [EEPROM](eeprom.md) ; l'[EEPROM](eeprom.md) existante sera rendue dans l'inventaire du joueur.

Il n'existe qu'un seul type de boitier de drone (plus un type créatif). Il est capable d'accepter les composants suivants :
- 1x [processeur](cpu1.md)
- [RAM](ram1.md)
- 1x [EEPROM](eeprom.md)
- Cartes d'extension
- Améliorations

Chaque emplacement accepte des objets de n'importe quel niveau.
