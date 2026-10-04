# Drahtlosnetzwerkkarte

![Kann Krebs verursachen. Oder nicht.](oredict:oc:wlanCard2)

Die Drahtlosnetzwerkkarte ist eine aufgewertete [Netzwerkkarte](lanCard.md), die kabellose Netzwerknachrichten senden und empfangen kann. Sie kann außerdem verkabelte Nachrichten senden und empfangen. Unter Linux erscheint sie als Ethernet-Schnittstelle (wie `eth0`) und kann weiterhin OC-Port-Nachrichten über das Gerät `modem` auf dem Gerätebus senden. Die Signalstärke kontrolliert direkt die Distanz, bis zu der eine gesendete Nachricht empfangen werden kann, wobei die Stärke der Distanz in Blöcken entspricht.

Je höher die Signalstärke, desto mehr Energie benötigt das Senden einer einzelnen Nachricht. Das Terrain zwischen dem Sender und dem Empfänger bestimmt zudem, ob eine Nachricht erfolgreich übertragen wird oder nicht. Um einen Block zu durchdringen, wird die Blockhärte von der Signalstärke subtrahiert - wobei 1 (für Luftblöcke) das Minimum ist. Wenn keine Stärke mehr übrig ist, um den Empfänger zu erreichen, wird die Nachricht nicht empfangen. Dies ist allerdings keine exakte Wissenschaft - manchmal können Nachrichten trotzdem das Ziel erreichen. Generell sollte die Sichtlinie zwischen Sender und Empfänger möglichst frei gehalten werden.
