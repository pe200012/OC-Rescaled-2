# Internet Card

![Cat videos in 3, 2, ...](oredict:oc:internetCard)

The internet card grants [computers](../general/computer.md) access to the internet. In Linux it is an Ethernet interface, the next one after the network cards. It works as a NAT gateway: give the machine an address and a default route, and put a DNS server into `/etc/resolv.conf`. Outgoing TCP, UDP and ping work, so tools like `wget` and `ssh` can reach the outside. Incoming connections do not work.

Internet access can be switched off in the configuration (`internet.enableTcp`).
