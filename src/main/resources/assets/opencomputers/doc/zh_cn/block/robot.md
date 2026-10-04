# 机器人

![他的名字叫Tobor。](block:OpenComputers:robot)

不像[电脑](../general/computer.md)，机器人可以像玩家一样进行移动并与世界互动。但是它们**不能**与外部组件交互！如果你需要让机器人与[电脑](../general/computer.md)或其他机器人通信，那么需要使用[无线网卡](../item/wlanCard1.md)。或者使用[红石卡](../item/redstoneCard1.md)并通过红石信号创建一些低级的通讯协议。

将[机箱](case1.md)放入[电子装配机](assembler.md)即可构建机器人。和[电脑](../general/computer.md)一样，机器人运行Linux。在Linux中，于micropython里`import robot`即可使用`robot.forward()`、`robot.turn_left()`、`robot.swing()`和`robot.place()`等函数；`robot.component`包含机器人的全部方法。机器人移动或转向时，机器会暂停运行。

机器人可安装多种升级以增强其功能。可用升级包括[物品栏升级](../item/inventoryUpgrade.md)、[物品栏交互升级](../item/inventoryControllerUpgrade.md)、[储罐升级](../item/tankUpgrade.md)、[导航升级](../item/navigationUpgrade.md)等等。机器人还可安装[升级容器](../item/upgradeContainer1.md)和[扩展卡容器](../item/cardContainer1.md)，安装后机器人能在运行中热插拔升级与扩展卡。机器人也可安装[软盘驱动器](diskDrive.md)，安装后机器人可以插入[软盘](../item/floppy.md)。给机器人装上[硬盘](../item/hdd1.md)，它首次启动时就会安装[Linux](../general/linux.md)。
