# Access Point

![AAA](oredict:oc:accessPoint)

*Dieser Block ist veraltet und wird in einer zukünftigen Version entfernt.* Er kann zu einem [Relais](relay.md) gecraftet werden, um ihn nicht zu verlieren.

Der Access Point ist die kabellose Version des [Switches](switch.md). Er kann verwendet werden, um Subnetzwerke voneinander zu trennen, sodass Maschinen darin keine [Komponenten](../general/computer.md) in anderen Netzwerken sehen, aber dennoch Netzwerknachrichten an die Maschinen in anderen Netzwerken senden können.

Zusätzlich kann dieser Block als Repeater verwendet werden: Er kann Nachrichten aus verkabelten Netzwerken als verkabelte Nachrichten an andere Geräte weiterleiten, oder kabellose Nachrichten als verkabelte oder kabellose Nachrichten.

[Switches](switch.md) und Access Points führen *kein Protokoll* über kürzlich weitergeleitete Pakete, also sollten Kreisläufe im Netzwerk vermieden werden, sonst kann dasselbe Paket mehrmals empfangen werden. Aufgrund der geringen Puffergröße von Switches kann Paketverlust auftreten, wenn Netzwerknachrichten zu oft gesendet werden. Die Geschwindigkeit, mit der sie Nachrichten weiterleiten, ist begrenzt; für mehr Geschwindigkeit können die Grenzwerte in der Konfiguration erhöht werden.

Pakete werden nur eine bestimmte Anzahl von Malen weitergeschickt, daher ist es nicht möglich, eine beliebige Anzahl von [Switches](switch.md) oder Access Points hintereinander zu schalten. Standardmäßig wird ein Paket bis zu fünf Mal weitergeschickt.
