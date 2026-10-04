# Relais

![Baut Brücken.](oredict:oc:relay)

Das Relais kann verwendet werden, um verschiedenen Subnetzwerken das Senden von Netzwerknachrichten zueinander zu ermöglichen, ohne Komponenten für [Computer](../general/computer.md) in anderen Netzwerken zugänglich zu machen. Grundsätzlich ist es eine gute Idee, Komponenten lokal zu behalten, damit [Computer](../general/computer.md) nicht den falschen [Bildschirm](screen1.md) ansprechen.

Das Relais kann mit einer [Drahtlosnetzwerkkarte](../item/wlanCard1.md) aufgerüstet werden, um Nachrichten auch kabellos weiterzuleiten. Kabellose Nachrichten können von anderen Relais mit einer Drahtlosnetzwerkkarte oder von [Computern](../general/computer.md) mit einer Drahtlosnetzwerkkarte empfangen und weitergeleitet werden. Netzwerkkarten erscheinen unter Linux als Ethernet-Schnittstellen, und Frames werden über das Relais als normale Netzwerknachrichten übertragen, sodass TCP/IP zwischen Maschinen funktioniert.

Alternativ kann das Relais mit [Verbindungskarten](../item/linkedCard.md) aufgerüstet werden. In diesem Fall leitet es Nachrichten auch durch den Tunnel der Verbindungskarte weiter; zu den üblichen Kosten, daher muss sichergestellt werden, dass das Relais ausreichend mit Energie versorgt ist.

Relais führen *kein Protokoll* über kürzlich weitergeleitete Pakete, also sollten Kreisläufe im Netzwerk vermieden werden, sonst kann dasselbe Paket mehrmals empfangen werden. Aufgrund der geringen Puffergröße von Relais führt zu häufiges Senden von Nachrichten zu Paketverlust. Die Geschwindigkeit, mit der sie Nachrichten weiterleiten, ist begrenzt; für mehr Geschwindigkeit können die Grenzwerte in der Konfiguration erhöht werden.

Pakete werden nur eine bestimmte Anzahl von Malen weitergeschickt, daher ist es nicht möglich, eine beliebige Anzahl von Relais hintereinander zu schalten. Standardmäßig wird ein Paket bis zu fünf Mal weitergeschickt.
