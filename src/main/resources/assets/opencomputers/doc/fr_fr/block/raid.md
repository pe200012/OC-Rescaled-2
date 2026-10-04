# Raid

![Une instance à 40.](oredict:oc:raid)

Le bloc de RAID peut accueillir 3 [disques durs](../item/hdd1.md) qui seront combinés en un seul système de fichiers. Ce système de fichiers combiné a la taille de la somme des capacités des [disques durs](../item/hdd1.md) individuels, et est disponible pour tous les [ordinateurs](../general/computer.md) connectés au RAID. Le RAID n'est pas un disque Linux. Les machines Linux n'y accèdent que par le bus de périphériques, en tant que périphérique `filesystem` (open, read, write, list, ... comme dans l'API de système de fichiers d'OpenComputers).

Le RAID fonctionne uniquement (et se présente en tant que système de fichiers) quand 3 [disques durs](../item/hdd1.md) sont présents. Les [disques durs](../item/hdd1.md) peuvent avoir chacun une taille différente.

Faites attention, car l'ajout d'un [disque dur](../item/hdd1.md) au bloc de RAID effacera son contenu. Retirer un seul [disque dur](../item/hdd1.md) d'un RAID complet effacera le contenu complet du RAID. Remettre le disque en place *ne restaurera pas* les anciens fichiers ; le RAID sera ré-initialisé en tant que système de fichiers vierge.

Casser un bloc de RAID gardera son contenu, il peut donc être déplacé en toute sécurité sans risque de perte de données.
