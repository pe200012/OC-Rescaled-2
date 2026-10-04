# Relay

![Building bridges.](oredict:oc:relay)

The relay can be used to allow different subnetworks to send network messages to each other, without exposing components to [computers](../general/computer.md) in other networks. Keeping components local is usually a good idea, to avoid [computers](../general/computer.md) using the wrong [screen](screen1.md).

The relay can be upgraded by inserting a [wireless network card](../item/wlanCard1.md) to also relay messages wirelessly. Wireless messages can be received and relayed by other relays with a wireless network card, or by [computers](../general/computer.md) with a wireless network card. Network cards appear as Ethernet interfaces in Linux, and frames travel over the relay as normal network messages, so TCP/IP works between machines.

Alternatively the relay can be upgraded using [linked cards](../item/linkedCard.md). In this case it will forward messages through the tunnel provided by the linked card, too; at the usual cost, so make sure the relay is sufficiently powered.

Relays do *not* keep track of which packets they forwarded recently, so avoid cycles in your network or you may receive the same packet multiple times. Due to the limited buffer size of relays, sending messages too frequently will result in packet loss. The speed with which they relay messages is limited; raise the limits in the config for more speed.

Packets are only re-sent a certain number of times, so chaining an arbitrary number of relays is not possible. By default, a packet will be re-sent up to five times.
