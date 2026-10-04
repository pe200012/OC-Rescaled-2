# 红石I/O端口

![红石你好。](oredict:oc:redstone)

红石I/O端口可用于远程读取与发出红石信号。其工作方式类似[红石卡](../item/redstoneCard1.md)：它能收发简单模拟信号与集束信号，但不能收发无线红石信号。

在调用此方块提供的组件方法并需要提供方向时，所用方向为世界的全局基准方向（绝对方向）。此方块的各个面贴图上有不明显的凹痕，凹痕个数对应了每个面的数字编号。在micropython中，先`from devices import bus`，再用`rs = bus.find("redstone")`，之后即可调用例如`rs.setOutput(1, 15)`。

和[红石卡](../item/redstoneCard1.md)一样，当红石信号的状态变化，无论是模拟红石信号还是集束红石信号，此方块会向所连接的[电脑](../general/computer.md)推送一个信号事件（用`bus.wait_event()`读取）。此方块还能启用输入强度达到某一值后唤醒相连[电脑](../general/computer.md)的功能，这一功能可用于自动开启[电脑](../general/computer.md)。
