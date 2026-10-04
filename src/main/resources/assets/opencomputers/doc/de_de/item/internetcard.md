# Internetkarte

![Katzenvideos in 3, 2, ...](oredict:oc:internetCard)

Die Internetkarte gibt [Computern](../general/computer.md) Zugriff auf das Internet. Unter Linux ist sie eine Ethernet-Schnittstelle, und zwar die nächste nach den Netzwerkkarten. Sie arbeitet als NAT-Gateway: Gib der Maschine eine Adresse und eine Standardroute und trage einen DNS-Server in `/etc/resolv.conf` ein. Ausgehendes TCP, UDP und Ping funktionieren, sodass Werkzeuge wie `wget` und `ssh` nach außen gelangen können. Eingehende Verbindungen funktionieren nicht.

Der Internetzugriff kann in der Konfiguration abgeschaltet werden (`internet.enableTcp`).
