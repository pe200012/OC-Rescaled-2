# Carte réseau

![Entre dans le réseau.](oredict:oc:lanCard)

La carte réseau permet aux [ordinateurs](../general/computer.md) d'envoyer et de recevoir des messages réseau. Les messages (ou paquets) peuvent être diffusés à tous les noeuds de réception dans un sous-réseau, ou envoyés à un noeud spécifique avec une certaine adresse. Les [relais](../block/relay.md) peuvent être utilisés pour relier plusieurs sous-réseaux en relayant des messages entre les sous-réseaux auxquels ils sont connectés. Il est également possible d'envoyer un message ciblé si le receveur est dans un autre sous-réseau, et si les réseaux sont connectés par un ou plusieurs [relais](../block/relay.md). Sous Linux, la carte apparaît comme une interface Ethernet (`eth0`, ...), donc TCP/IP fonctionne entre les machines une fois les adresses configurées ; les messages par port sont également disponibles via le périphérique `modem` du bus de périphériques.
