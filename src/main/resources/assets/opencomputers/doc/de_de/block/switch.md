# Switch

![Baut Brücken.](oredict:oc:switch)

*Dieser Block ist veraltet und wird in einer zukünftigen Version entfernt.* Er kann zu einem [Relais](relay.md) gecraftet werden, um ihn nicht zu verlieren.

Der Switch kann verwendet werden, um verschiedenen Subnetzwerken das Senden von Netzwerknachrichten zueinander zu ermöglichen, ohne Komponenten für [Computer](../general/computer.md) in anderen Netzwerken zugänglich zu machen. Grundsätzlich ist es eine gute Idee, Komponenten lokal zu behalten, damit [Computer](../general/computer.md) nicht den falschen [Bildschirm](screen1.md) ansprechen.

Es gibt auch eine kabellose Variante dieses Blocks, den [Access Point](accessPoint.md), der Nachrichten ebenfalls kabellos weiterleitet. Kabellose Nachrichten können von anderen [Access Points](accessPoint.md) oder von [Computern](../general/computer.md) mit einer [Drahtlosnetzwerkkarte](../item/wlanCard1.md) empfangen und weitergeleitet werden.

Switches und [Access Points](accessPoint.md) führen *kein Protokoll* über kürzlich weitergeleitete Pakete, also sollten Kreisläufe im Netzwerk vermieden werden, sonst kann dasselbe Paket mehrmals empfangen werden. Aufgrund der geringen Puffergröße von Switches führt zu häufiges Senden von Nachrichten zu Paketverlust. Die Geschwindigkeit, mit der sie Nachrichten weiterleiten, ist begrenzt; für mehr Geschwindigkeit können die Grenzwerte in der Konfiguration erhöht werden.

Pakete werden nur eine bestimmte Anzahl von Malen weitergeschickt, daher ist es nicht möglich, eine beliebige Anzahl von Switches oder Access Points hintereinander zu schalten. Standardmäßig wird ein Paket bis zu fünf Mal weitergeschickt.
