# 红石卡

![见红了。](oredict:oc:redstoneCard1)

红石卡能让[电脑](../general/computer.md)从临近方块读取，或向临近方块发出模拟红石信号。当传入的红石信号强度改变时，会向[电脑](../general/computer.md)中传入一个事件。程序通过设备总线使用该卡：

`from devices import bus`
`rs = bus.find("redstone")`
`rs.setOutput(1, 15)`
`e = bus.wait_event(5000)`

如果装有受支持且提供了集束红石功能的模组，例如RedLogic、Project Red或MineFactory Reloaded；又或者有提供了无线红石功能的模组，例如WR-CBE、Slimevoid's Wireless，那么红石卡还能与上述系统交互。

调用某些方法时提供的方向是相对于[机箱](../block/case1.md)/[机器人](../block/robot.md)/[机架](../block/rack.md)的朝向的相对方向。也就是假如你面向电脑的正面，右侧在你的左边，其他方向同理。
