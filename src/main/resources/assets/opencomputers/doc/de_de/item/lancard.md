# Netzwerkkarte

![Tritt ins Netzwerk ein.](oredict:oc:lanCard)

Die Netzwerkkarte erlaubt es [Computern](../general/computer.md), Netzwerknachrichten zu senden und zu empfangen. Nachrichten (oder Pakete) können an alle empfangenden Knoten in einem Subnetzwerk gesendet werden oder gezielt an einen Knoten mit einer bestimmten Adresse. [Relais](../block/relay.md) können verwendet werden, um mehrere Subnetzwerke zu überbrücken, indem sie Nachrichten zwischen den Subnetzwerken weiterleiten, mit denen sie verbunden sind. Auch gezielte Nachrichten sind möglich, wenn sich der Empfänger in einem anderen Subnetzwerk befindet, sofern die Netzwerke über ein oder mehrere [Relais](../block/relay.md) verbunden sind. Unter Linux erscheint die Karte als Ethernet-Schnittstelle (`eth0`, ...), sodass TCP/IP zwischen Maschinen funktioniert, sobald Adressen konfiguriert sind. Port-Nachrichten sind außerdem über das Gerät `modem` auf dem Gerätebus verfügbar.
