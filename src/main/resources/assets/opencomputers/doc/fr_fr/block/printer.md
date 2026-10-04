# Imprimante 3D

![L'impression en 2D, c'est tellement dépassé.](oredict:oc:printer)

Les imprimantes 3D vous permettent d'imprimer n'importe quel bloc de n'importe quelle forme, avec n'importe quelle texture. Pour démarrer avec les imprimantes 3D, vous devrez placer un bloc d'imprimante 3D à côté d'un ordinateur. Cela vous donnera accès à l'API de composant `printer3d`, ce qui permet de paramétrer et d'imprimer des [modèles](print.md) en utilisant les fonctions fournies.

Sous Linux, l'imprimante est un périphérique du bus de périphériques, donc un programme micropython peut l'utiliser :
`from devices import bus`
`p = bus.find("printer3d")`
`print(p)` liste ses méthodes.

Afin d'imprimer les modèles, une imprimante 3D doit être configurée par un [ordinateur](../general/computer.md). S'il est mis en mode sans-arrêt, l'ordinateur ne sera plus requis par la suite. Vous devrez également lui fournir une [cartouche d'encre](../item/inkCartridge.md) et un peu de [chamélium](../item/chamelium.md) pour les matières premières. La quantité de chamélium utilisée dépend du volume de l'impression 3D, tandis que la quantité d'encre utilisée dépend de la surface de l'objet imprimé.

Définissez les formes, les textures et le nom avec les méthodes du composant, puis lancez l'impression avec `p.commit(1)`.
