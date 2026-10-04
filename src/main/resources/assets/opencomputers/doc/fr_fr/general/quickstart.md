# Bien démarrer

Aussi connu en tant que "comment construire votre premier ordinateur". Pour faire démarrer votre premier [ordinateur](computer.md), vous aurez d'abord besoin de l'installer correctement. Il y a plusieurs types différents d'ordinateurs dans OpenComputers, mais commençons avec la base : l'ordinateur standard.

**Avertissement**: ce sera un pas-à-pas, et il fournira des informations sur la manière de chercher des problèmes vous-même plus tard, donc ça sera plutôt long. Si vous n'avez jamais construit d'ordinateur dans la vie réelle, et/ou que vous êtes complètement nouveau sur ce mod, il est vivement recommandé que vous lisiez tout ceci.

Premièrement, vous aurez besoin d'un [boîtier](../block/case1.md). C'est le bloc qui contiendra tous les composants, en définissant le comportement de l'ordinateur que vous construisez.

![Un boîtier d'ordinateur.](oredict:oc:case3)

Quand vous ouvrez l'interface du [boîtier d'ordinateur](../block/case1.md), vous verrez quelques emplacements à droite. Chaque composant va dans n'importe quel emplacement de son type : il n'y a pas de niveaux dont il faille se soucier.

Quand ils sont vides, les [boîtiers](../block/case1.md) sont assez inutiles. Vous pouvez essayer d'allumer votre [ordinateur](computer.md), mais il affichera immédiatement un message d'erreur dans votre tchat, et fera entendre son mécontentement en vous bipant dessus. Heureusement que le message d'erreur vous dit quoi faire pour résoudre la situation : il demande de l'énergie. Connectez votre [ordinateur](computer.md) à un peu d'énergie, soit directement soit via un [convertisseur énergétique](../block/powerConverter.md).

Maintenant, si vous essayez de le démarrer, il vous dira qu'il a besoin d'un [processeur](../item/cpu1.md). Il n'existe qu'un seul type de [processeur](../item/cpu1.md), mais il peut tourner à 25, 50, 100 ou 200 MHz : utilisez-le en vous accroupissant pour changer. Plus rapide, c'est mieux, mais ça consomme plus d'énergie ; 50 MHz est un bon début. Mettez-le dans votre [boîtier](../block/case1.md).

Ensuite, il vous sera demandé d'insérer de la [mémoire (RAM)](../item/ram1.md). Remarquez que le bip est différent maintenant : long-court. La [mémoire](../item/ram1.md) existe en tailles de 1 Mo à 32 Mo, et les barrettes que vous installez s'additionnent. Linux a besoin d'au moins 8 Mo ; 16 Mo est confortable.

Et voilà, mettre l'ordinateur sous tension n'affichera plus de message d'erreur ! Hélas, il ne fait quand même pas grand chose. Au moins, il émet 2 bips maintenant. Ce qui veut dire que l'[ordinateur](computer.md) a démarré mais a échoué tout de suite. C'est là qu'intervient un outil très utile : l'[analyseur](../item/analyzer.md). Cet outil permet d'inspecter beaucoup de blocs d'OpenComputers, ainsi que quelques blocs d'autres mods. Pour l'utiliser sur l'[ordinateur](computer.md), utilisez l'[analyseur](../item/analyzer.md) sur le boîtier en vous accroupissant.

Vous devriez maintenant voir l'erreur qui a causé le crash de l'[ordinateur](computer.md) :
`no bootable EEPROM`

L'ordinateur exécute le programme qui se trouve sur son [EEPROM](../item/eeprom.md), et il n'en a aucune. Fabriquer une [EEPROM](../item/eeprom.md) est plutôt simple, et pour un [ordinateur](computer.md) nous en voulons une contenant le programme de démarrage de Linux : fabriquez une [EEPROM](../item/eeprom.md) avec un [manuel](../item/manual.md) pour obtenir une « EEPROM (Linux) ». Mettez-la dans votre [ordinateur](computer.md).

Le programme de démarrage cherche Linux sur le premier [disque dur](../item/hdd1.md). Placez un [disque dur](../item/hdd1.md) dans le boîtier : au premier démarrage de l'ordinateur, Linux y est installé, et tout ce que vous y enregistrez est conservé. Sans [disque dur](../item/hdd1.md) l'ordinateur démarre quand même, mais exécute Linux depuis la mémoire et oublie tout quand il s'arrête ; c'est suffisant pour faire des essais.

Appuyez sur le bouton d'alimentation. Il vit ! Ou il devrait, en tout cas. Si ce n'est pas le cas quelque chose ne va pas, et vous devriez investiguer en utilisant l'[analyseur](../item/analyzer.md). Mais en supposant qu'il fonctionne maintenant, vous avez presque fini. Tout ce qui reste à faire est de lui permettre d'accepter des entrées, et d'afficher des sorties.

Pour voir ce que fait l'[ordinateur](computer.md), vous devrez vous munir d'un [écran](../block/screen1.md). Aucune carte graphique n'est nécessaire : la console est dessinée directement sur l'[écran](../block/screen1.md).
![Non, ce n'est pas un écran plat.](oredict:oc:screen3)

Placez l'[écran](../block/screen1.md) à côté de votre [boîtier](../block/case1.md) ou connectez-le en utilisant des [câbles](../block/cable.md). Vous devriez maintenant voir Linux démarrer sur l'[écran](../block/screen1.md). Finalement, placez un [clavier](../block/keyboard.md) soit sur l'[écran](../block/screen1.md) lui-même, soit juste en face de l'[écran](../block/screen1.md), pour activer l'entrée de données au [clavier](../block/keyboard.md).

Avec ça, vous avez fini. Connectez-vous en tant que `root` ; il n'y a pas de mot de passe. Vous êtes maintenant dans un shell Linux. Essayez `ls /mnt/builtin` pour voir ce qu'apporte le mod, ou `micropython` pour obtenir une invite Python. La page [Linux](linux.md) vous en dit plus sur le système, et [programmation](programming.md) explique comment contrôler la redstone, les robots et tout le reste depuis celui-ci.

Amusez-vous à construire des [ordinateurs](computer.md) plus complexes, jouer avec des [serveurs](../item/server1.md) et assembler des [robots](../block/robot.md), des [drones](../item/drone.md), des [micro-contrôleurs](../block/microcontroller.md) et des [tablettes](../item/tablet.md) dans l'[assembleur électronique](../block/assembler.md).

Bon code !
