# Relai

![Construit des ponts.](oredict:oc:relay)

Le relai peut être utilisé pour permettre à différents sous-réseaux de s'envoyer des messages réseau entre eux, sans exposer leurs composants aux [ordinateurs](../general/computer.md) des autres réseaux. Maintenir l'état "local" d'un composant est généralement une bonne idée, pour éviter aux [ordinateurs](../general/computer.md) d'utiliser le mauvais [écran](screen1.md).

Le relai peut être amélioré en insérant une [carte de réseau sans-fil](../item/wlanCard1.md) pour relayer aussi des messages sans-fil. Les messages sans-fil peuvent être reçus et relayés par d'autres relais avec une carte de réseau sans-fil, ou par des [ordinateurs](../general/computer.md) équipés d'une carte réseau sans-fil. Les cartes réseau apparaissent comme des interfaces Ethernet sous Linux, et les trames transitent par le relai comme des messages réseau normaux, donc TCP/IP fonctionne entre les machines.

Par ailleurs, le relai peut être amélioré en utilisant des [cartes liées](../item/linkedCard.md). Dans ce cas, il transmettra aussi les messages à travers le tunnel de la carte liée ; ceci aura le coût habituel, donc assurez-vous que le relai a suffisamment d'énergie.

Les relais *ne gardent pas* de trace des paquets qu'ils ont transmis récemment, donc évitez les boucles dans votre réseau, ou vous pourriez recevoir le même paquet plusieurs fois. Envoyer des messages trop souvent causera la perte de paquets, à cause de la taille limitée du tampon des relais. La vitesse à laquelle ils relaient les messages est limitée ; augmentez les limites dans la configuration pour plus de vitesse.

Les paquets sont seulement renvoyés un certain nombre de fois, donc enchaîner un nombre arbitraire de relais n'est pas possible. Par défaut, un paquet sera renvoyé jusqu'à 5 fois.
