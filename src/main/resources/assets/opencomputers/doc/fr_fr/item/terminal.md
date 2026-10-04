# Terminal à distance

![Accès à distance.](oredict:oc:terminal)

Le terminal à distance peut être utilisé pour contrôler à distance des ordinateurs via des [serveurs de terminaux](terminalServer.md). Pour l'utiliser, activez un [serveur de terminaux](terminalServer.md) installé dans un [support de serveur](../block/rack.md) (cliquez sur le bloc du [support de serveur](../block/rack.md) dans le monde, en visant le [serveur de terminaux](terminalServer.md) pour y lier le terminal).

Un [serveur de terminaux](terminalServer.md) fournit un [écran](../block/screen1.md) virtuel et un [clavier](../block/keyboard.md) qui peuvent être contrôlés via le terminal. Cela peut conduire à des comportements inattendus si un autre écran physique et/ou un clavier est connecté au même sous-réseau que le [serveur de terminaux](terminalServer.md), donc il faut généralement l'éviter. En utilisant le terminal après l'avoir lié, une interface s'ouvrira de la même manière qu'avec un [clavier](../block/keyboard.md) attaché à un [écran](../block/screen1.md).

Plusieurs terminaux peuvent être liés à un seul [serveur de terminaux](terminalServer.md), mais ils afficheront tous la même information, puisqu'ils partageront le même [écran](../block/screen1.md) virtuel et le même [clavier](../block/keyboard.md). Le nombre de terminaux qui peuvent être liés à un [serveur de terminaux](terminalServer.md) est limité. Quand le nombre de terminaux liés atteint la limite, en lier un autre déliera le premier qui avait été lié.
