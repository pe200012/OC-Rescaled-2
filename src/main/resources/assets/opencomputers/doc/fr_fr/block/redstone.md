# Redstone E/S

![Salut Red.](oredict:oc:redstone)

Le bloc d'E/S de redstone peut être utilisé pour lire et émettre des signaux de redstone à distance. Il se comporte comme une [carte de redstone](../item/redstoneCard1.md) : il peut aussi bien lire et émettre des signaux analogiques que des signaux empaquetés (bundle), mais il ne peut pas lire ou émettre de signaux redstone sans-fil.

En indiquant un côté aux méthodes du composant exposé par ce bloc, les directions sont les points cardinaux du monde. La texture du bloc présente de subtiles empreintes correspondant à la valeur numérique de chaque côté. En micropython, utilisez `rs = bus.find("redstone")` après `from devices import bus`, puis par exemple `rs.setOutput(1, 15)`.

De même que les [cartes de redstone](../item/redstoneCard1.md), ce bloc envoie un événement de signal (lu avec `bus.wait_event()`) aux [ordinateurs](../general/computer.md) connectés quand l'état du signal de redstone change - autant pour les signaux analogiques qu'empaquetés (bundle). Ce bloc peut également être configuré pour démarrer des [ordinateurs](../general/computer.md) connectés quand une certaine puissance de signal est dépassée, ce qui permet de démarrer automatiquement des [ordinateurs](../general/computer.md).
