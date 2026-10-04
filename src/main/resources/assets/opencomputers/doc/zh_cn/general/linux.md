# Linux

[电脑](computer.md)、[服务器](../item/server1.md)、[机器人](../block/robot.md)和[平板电脑](../item/tablet.md)在模拟的64位RISC-V处理器上运行Linux。这是一个小巧但货真价实的Linux：BusyBox shell及常用工具、编辑器`nano`和`vi`、`micropython`、Lua 5.4、C编译器`tcc`，以及`ssh`/`scp`（dropbear）。以`root`登录，没有密码。

## 启动

用[EEPROM](../item/eeprom.md)和[手册](../item/manual.md)合成得到的“EEPROM (Linux)”存有引导程序，它会从第一块[硬盘](../item/hdd1.md)启动Linux。新的第一块[硬盘](../item/hdd1.md)会在机器首次启动时安装Linux。没有[硬盘](../item/hdd1.md)时，Linux会从内存中作为临时系统运行，机器停止后一切内容都会丢失。Linux至少需要8 MB[内存](../item/ram1.md)。

如果机器无法启动，请潜行使用[分析器](../item/analyzer.md)查看原因。

## 磁盘

[硬盘](../item/hdd1.md)和[软盘](../item/floppy.md)都是普通磁盘：`/dev/vda`是第一块[硬盘](../item/hdd1.md)，`/dev/vdb`是下一块，软盘驱动器排在[硬盘](../item/hdd1.md)之后。机箱的软盘槽位以及机器能访问到的每个[软盘驱动器](../block/diskDrive.md)各算一个驱动器。新磁盘是空白的，需要先建立文件系统再挂载：
`mke2fs /dev/vdb`
`mount /dev/vdb /mnt`
Linux运行时可以插入和取出[软盘](../item/floppy.md)，取出前请先`umount`。更换[硬盘](../item/hdd1.md)以及新接入[软盘驱动器](../block/diskDrive.md)都需要重启机器。磁盘映像保存在世界存档的`opencomputers-riscv/disks`中，你也可以在游戏之外打开它们。

## 显示屏、键盘与鼠标

控制台为80x24字符。可以用鼠标中键或粘贴键（默认为Insert）粘贴文本。

程序还可以绘制像素：`/dev/fb0`是一个320x192、32位像素的帧缓冲。程序一旦绘制，[显示屏](../block/screen1.md)就会显示这些像素；停止绘制一秒后控制台再次输出内容，[显示屏](../block/screen1.md)就会重新显示文本。按键同样会送到`/dev/input/event0`，而当[显示屏](../block/screen1.md)显示像素时，`/dev/input/event1`会以像素为单位报告点击、拖动和滚动的位置。在世界中潜行使用[显示屏](../block/screen1.md)即可点击它。可以试试`micropython /mnt/builtin/example/framebuffer.py`。

## 网络

[网卡](../item/lanCard.md)和[无线网卡](../item/wlanCard1.md)是网络接口，名为`eth0`、`eth1`等等，Linux通过OpenComputers的[线缆](../block/cable.md)、[中继器](../block/relay.md)和无线网络使用TCP/IP通信。接口一开始是未配置的：
`ip addr add 10.0.0.1/24 dev eth0`
`ip link set eth0 up`
给另一台机器配置`10.0.0.2`，然后`ping 10.0.0.1`就能连通第一台。`lua /mnt/builtin/bin/setup-network.lua`会询问各项设置并将其保存。

[因特网卡](../item/internetCard.md)是网卡之后的下一个接口，网关为`10.0.2.2`：
`ip addr add 10.0.2.15/24 dev eth1`
`ip link set eth1 up`
`ip route add default via 10.0.2.2`
`echo nameserver 1.1.1.1 > /etc/resolv.conf`
之后`wget`和`ssh`就能访问真实的互联网了。只能向外发起连接。

## 保存与停止

运行中的机器会随世界保存并在中断处继续运行；[平板电脑](../item/tablet.md)则是重新开始。`poweroff`会关闭机器，`reboot`会重启机器，更改[内存](../item/ram1.md)、[硬盘](../item/hdd1.md)、网卡或[EEPROM](../item/eeprom.md)之后需要重启。用电源按钮关闭机器，或让机器耗尽能量，就相当于直接拔掉电源：尚未写入的文件会丢失，但系统的文件系统仍然完好。

## 模组自带的文件

`/mnt/builtin`中是模组自带的内容：设备总线库、示例，以及C语言的工具和头文件。它们的用法见[编程](programming.md)。
