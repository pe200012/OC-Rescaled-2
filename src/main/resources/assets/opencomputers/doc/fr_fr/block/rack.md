# Support de serveur

![Logement gratuit.](oredict:oc:rack)

Un support de serveur peut contenir jusqu'à quatre éléments de rack, comme des [serveurs](../item/server1.md), des [serveurs de terminaux](../item/terminalServer.md) et des [lecteurs de disquettes montables](../item/diskDriveMountable.md). La connectivité des éléments d'un support peut être configurée en détail via l'interface. En particulier, si des [serveurs](../item/server1.md) contiennent des composants qui le permettent, comme des [cartes réseau](../item/lanCard.md), des connexions réservées au réseau peuvent être définies pour ces composants. Ces connexions serviront uniquement à transmettre des messages réseau, les composants ne seront pas visibles à travers elles. Ces connexions réservées au réseau se distinguent par leurs lignes plus fines que les connexions "principales", qui permettent aussi l'accès aux composants. Chaque connexion interne doit relier un élément / un composant d'un élément à un bus connecté à un côté du support. Pour connecter plusieurs éléments entre eux, connectez-les au même bus.

Les supports de serveur font office de [relai](relay.md) et de [distributeur énergétique](powerDistributor.md) à la fois. Le fait qu'il agisse ou non comme un relai peut être configuré dans l'interface du support, son activation étant indiquée par une ligne de connexion entre les bus des côtés.
