# Carte de réseau sans-fil

![Peut provoquer le cancer. Ou pas.](oredict:oc:wlanCard2)

La carte de réseau sans fil est une [carte réseau](lanCard.md) améliorée qui peut envoyer et recevoir des messages réseau sans fil. Elle peut aussi envoyer et recevoir des messages filaires. Sous Linux, elle apparaît comme une interface Ethernet (comme `eth0`), et elle peut toujours envoyer des messages par port d'OC via le périphérique `modem` du bus de périphériques. La force du signal contrôle directement la distance jusqu'à laquelle un message envoyé peut être reçu, avec cette force étant égale à la distance en blocs.

Plus la puissance du signal est importante, plus il faudra d'énergie pour envoyer un message. Le terrain entre l'émetteur et le destinataire détermine aussi si le message sera correctement transmis ou pas. Pour traverser un bloc, la dureté du bloc est soustraite de la puissance du signal - avec un minimum de 1 pour les blocs d'air. S'il n'y a plus de puissance pour atteindre le destinataire, le message ne sera pas reçu. Cependant, ce n'est pas une science exacte - quelques fois, les messages peuvent quand même atteindre leur cible. En général, assurez-vous que la ligne de vue entre l'émetteur et le destinataire soit dégagée.
