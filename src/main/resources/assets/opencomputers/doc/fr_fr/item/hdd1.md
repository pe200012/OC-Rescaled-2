# Disque dur

![Spaaaace.](oredict:oc:hdd1)

Les disques durs sont le moyen de stockage de données principal dans OpenComputers. Ils existent en 16, 32 et 64 Mo. Chacun est un disque brut sous Linux : `/dev/vda` est le premier, `/dev/vdb` le suivant, et ainsi de suite. Linux est installé automatiquement sur le premier disque dur quand l'[ordinateur](../general/computer.md) démarre pour la première fois. Les autres disques sont vierges ; utilisez `mke2fs` et `mount` pour les préparer. N'échangez les disques durs que lorsque la machine est éteinte.

Les disques durs peuvent être placés dans un [raid](../block/raid.md), qui les combine en un seul système de fichiers OpenComputers. Linux ne le voit que comme un périphérique `filesystem` sur le bus de périphériques, pas comme un disque. Remarquez que placer un disque dur dans un [raid](../block/raid.md) efface le contenu du disque.
