# Carte internet

![Vidéos de chat dans 3, 2, ...](oredict:oc:internetCard)

La carte internet donne accès à Internet aux [ordinateurs](../general/computer.md). Sous Linux, c'est une interface Ethernet, la suivante après les cartes réseau. Elle fonctionne comme une passerelle NAT : donnez une adresse et une route par défaut à la machine, et mettez un serveur DNS dans `/etc/resolv.conf`. TCP, UDP et ping sortants fonctionnent, donc des outils comme `wget` et `ssh` peuvent atteindre l'extérieur. Les connexions entrantes ne fonctionnent pas.

L'accès à Internet peut être désactivé dans la configuration (`internet.enableTcp`).
