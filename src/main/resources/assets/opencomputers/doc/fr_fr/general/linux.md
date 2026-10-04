# Linux

Les [ordinateurs](computer.md), les [serveurs](../item/server1.md), les [robots](../block/robot.md) et les [tablettes](../item/tablet.md) exécutent Linux sur un processeur RISC-V 64 bits émulé. C'est un Linux petit mais bien réel : un shell BusyBox avec les outils habituels, les éditeurs `nano` et `vi`, `micropython`, Lua 5.4, le compilateur C `tcc`, et `ssh`/`scp` (dropbear). Connectez-vous en tant que `root` ; il n'y a pas de mot de passe.

## Démarrage

L'[EEPROM](../item/eeprom.md) « EEPROM (Linux) », fabriquée à partir d'une [EEPROM](../item/eeprom.md) et d'un [manuel](../item/manual.md), contient le programme de démarrage. Il lance Linux depuis le premier [disque dur](../item/hdd1.md). Linux est installé sur un nouveau premier [disque dur](../item/hdd1.md) au premier démarrage de la machine. Sans [disque dur](../item/hdd1.md), Linux fonctionne comme un système « live » depuis la mémoire, qui oublie tout quand la machine s'arrête. Linux a besoin d'au moins 8 Mo de [mémoire](../item/ram1.md).

Si une machine ne démarre pas, utilisez un [analyseur](../item/analyzer.md) dessus en vous accroupissant pour voir pourquoi.

## Disques

Les [disques durs](../item/hdd1.md) et les [disquettes](../item/floppy.md) sont de simples disques : `/dev/vda` est le premier [disque dur](../item/hdd1.md), `/dev/vdb` le suivant, et les lecteurs de disquettes viennent après les [disques durs](../item/hdd1.md). L'emplacement de disquette du boîtier et chaque [lecteur de disquettes](../block/diskDrive.md) que la machine peut atteindre comptent chacun pour un lecteur. Les nouveaux disques sont vierges ; donnez-leur un système de fichiers et montez-les :
`mke2fs /dev/vdb`
`mount /dev/vdb /mnt`
Les [disquettes](../item/floppy.md) peuvent être insérées et retirées pendant que Linux fonctionne ; démontez-les d'abord avec `umount`. Les [disques durs](../item/hdd1.md) et les [lecteurs de disquettes](../block/diskDrive.md) nouvellement connectés nécessitent un redémarrage de la machine. Les images de disque sont conservées dans la sauvegarde du monde, dans `opencomputers-riscv/disks`, où vous pouvez aussi les ouvrir depuis l'extérieur du jeu.

## Écran, clavier et souris

La console fait 80x24 caractères. Collez du texte avec le bouton du milieu de la souris ou la touche de collage (Insert par défaut).

Les programmes peuvent aussi dessiner des pixels : `/dev/fb0` est un framebuffer de 320x192 avec des pixels sur 32 bits. Dès qu'un programme dessine, l'[écran](../block/screen1.md) affiche les pixels ; quand il s'arrête pendant une seconde et que la console affiche à nouveau du texte, l'[écran](../block/screen1.md) affiche de nouveau du texte. Les touches arrivent aussi sur `/dev/input/event0`, et tant que l'[écran](../block/screen1.md) affiche des pixels, `/dev/input/event1` indique où l'on clique, fait glisser et défiler, en pixels. Dans le monde, utilisez un [écran](../block/screen1.md) en vous accroupissant pour cliquer dessus. Essayez `micropython /mnt/builtin/example/framebuffer.py`.

## Réseau

Les [cartes réseau](../item/lanCard.md) et les [cartes de réseau sans-fil](../item/wlanCard1.md) sont des interfaces réseau, `eth0`, `eth1` et ainsi de suite, et Linux parle TCP/IP à travers les [câbles](../block/cable.md), les [relais](../block/relay.md) et le réseau sans-fil d'OpenComputers. Les interfaces démarrent sans configuration :
`ip addr add 10.0.0.1/24 dev eth0`
`ip link set eth0 up`
Donnez `10.0.0.2` à la machine suivante, et `ping 10.0.0.1` atteint la première. `lua /mnt/builtin/bin/setup-network.lua` demande les paramètres et les conserve.

Une [carte internet](../item/internetCard.md) est l'interface suivante après les cartes réseau, avec une passerelle en `10.0.2.2` :
`ip addr add 10.0.2.15/24 dev eth1`
`ip link set eth1 up`
`ip route add default via 10.0.2.2`
`echo nameserver 1.1.1.1 > /etc/resolv.conf`
Ensuite `wget` et `ssh` atteignent le véritable internet. Les connexions ne peuvent être établies que vers l'extérieur, à une exception près : l'ordinateur réel sur lequel tourne le jeu, ou son serveur, peut atteindre un port que la configuration du mod redirige (`forwardedPorts` dans sa section `internet`). Avec `"2222:10.0.2.15:22"`, démarrez le serveur SSH
`/etc/init.d/dropbear start`
et connectez-vous depuis l'extérieur du jeu avec `ssh -p 2222 root@localhost` ; `scp` et `sshfs` fonctionnent aussi. Renommé en `/etc/init.d/S50dropbear`, le script démarre à chaque amorçage. Toute personne qui atteint la machine par un réseau peut alors se connecter en `root`, sauf si vous lui donnez un mot de passe avec `passwd`.

## Sauvegarde et arrêt

Une machine en fonctionnement est sauvegardée avec le monde et reprend là où elle s'était arrêtée ; les [tablettes](../item/tablet.md) repartent à zéro à la place. `poweroff` éteint la machine, `reboot` la redémarre, et un redémarrage est nécessaire après avoir changé la [mémoire](../item/ram1.md), les [disques durs](../item/hdd1.md), les cartes réseau ou l'[EEPROM](../item/eeprom.md). Éteindre une machine avec son bouton d'alimentation, ou la laisser tomber à court d'énergie, revient à débrancher la prise : les fichiers qui n'avaient pas encore été écrits sont perdus, mais le système de fichiers reste intact.

## Les fichiers propres au mod

`/mnt/builtin` contient ce qu'apporte le mod : les bibliothèques du bus de périphériques, des exemples, ainsi que les outils et en-têtes pour le C. Leur utilisation est décrite dans [programmation](programming.md).
