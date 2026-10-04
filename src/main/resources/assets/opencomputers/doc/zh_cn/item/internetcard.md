# 因特网卡

![猫片播放倒数，3，2，……](oredict:oc:internetCard)

因特网卡为[电脑](../general/computer.md)提供了连接现实中因特网的能力。在Linux中它是一个以太网接口，排在网卡之后的下一个。它相当于一个NAT网关：给机器配置一个地址和默认路由，并在`/etc/resolv.conf`中写入DNS服务器即可。向外的TCP、UDP和ping都可用，因此`wget`和`ssh`等工具可以访问外部。外部发来的连接则不可用。

可以在配置文件中关闭因特网访问（`internet.enableTcp`）。
