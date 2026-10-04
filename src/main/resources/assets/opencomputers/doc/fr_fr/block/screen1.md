# Ecrans

![Tu vois ça ?](oredict:oc:screen1)

Un écran affiche la sortie des [ordinateurs](../general/computer.md). La console Linux s'affiche directement sur l'écran, donc aucune [carte graphique](../item/graphicsCard1.md) n'est nécessaire pour elle ; une carte graphique reste néanmoins un composant que les programmes peuvent appeler. Les écrans ont une résolution allant jusqu'à 160x50 caractères et 256 couleurs, et la console fait 80x24 caractères. Cela inclut les écrans intégrés aux [robots](robot.md) et aux [tablettes](../item/tablet.md).

La console utilise l'écran en contact avec le boîtier de l'ordinateur s'il y en a un, sinon le premier écran du réseau. Seuls les [claviers](keyboard.md) attachés à cet écran y saisissent du texte. Plusieurs ordinateurs reliés par des câbles ne s'affichent pas sur les écrans des autres.

Les écrans peuvent être placés les uns contre les autres pour former un écran multi-bloc, tant qu'ils sont dans la même direction. S'ils sont placés vers le haut ou le bas ils doivent également être tournés dans le même sens. Leur orientation est indiquée par une flèche superposée en tenant l'écran en main.

La taille d'un écran n'a pas d'impact sur la résolution disponible. Pour gérer la manière dont les écrans se connectent entre eux, il est possible de les colorer avec n'importe quel colorant. Faites simplement un clic-droit sur l'écran avec un colorant en main. Le colorant ne sera pas consommé, mais les écrans ne garderont pas leur couleur une fois détruits. Des écrans de différente couleur ne se connecteront pas.

Les écrans supportent également les entrées de la souris. Il est possible de cliquer soit dans l'interface de l'écran (qui peut seulement être ouverte si un [clavier](keyboard.md) est connecté à l'écran), soit en faisant un clic-droit accroupi sur un écran dans le monde. Tant qu'un programme dessine sur le framebuffer, la position de la souris et les boutons sont signalés en pixels du framebuffer (/dev/input/event1). Dans l'interface de l'écran, déplacer la souris sans bouton déplace aussi le pointeur.

Linux dispose aussi d'un framebuffer, /dev/fb0, de 320x192 pixels. Dès qu'un programme y dessine, l'écran affiche les pixels. Quand le dessin s'arrête pendant une seconde et que la console affiche à nouveau du texte, l'écran revient au texte. Essayez `micropython /mnt/builtin/example/framebuffer.py`.
