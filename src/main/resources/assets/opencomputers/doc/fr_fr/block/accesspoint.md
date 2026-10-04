# Point d'accès

![AAA](oredict:oc:accessPoint)

*Ce bloc est déprécié et sera retiré dans une version future.* Transformez-le en [relai](relay.md) pour éviter de le perdre.

Le point d'accès est la version sans-fil du [routeur](switch.md). Il peut être utilisé pour séparer des sous-réseaux pour que les machines qui les composent ne voient pas les [composants](../general/computer.md) des autres réseaux, tout en leur permettant d'envoyer des messages réseau aux machines d'autres réseaux.

En plus de ça, ce bloc peut faire office de répéteur : il peut renvoyer des messages filaires en tant que messages filaires à d'autres appareils, ou des messages sans-fil en tant que messages filaires ou sans-fil.

Les [routeurs](switch.md) et points d'accès ne gardent *pas* de trace des paquets qu'ils ont récemment relayés, donc évitez les boucles dans votre réseau ou vous pourriez recevoir le même paquet plusieurs fois. À cause de la taille limitée de la mémoire tampon des routeurs, une perte de paquets peut survenir si vous essayez d'envoyer des messages réseau trop fréquemment. La vitesse à laquelle ils relaient les messages est limitée ; augmentez les limites dans la configuration pour plus de vitesse.

Les paquets sont seulement renvoyés un certain nombre de fois, donc enchaîner un nombre arbitraire de [routeurs](switch.md) ou de points d'accès n'est pas possible. Par défaut, un paquet sera renvoyé jusqu'à 5 fois.
