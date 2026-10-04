# 编程

[电脑](computer.md)上的程序通过设备总线与世界交互：机器能看到的每个组件，例如[红石卡](../item/redstoneCard1.md)、[机器人](../block/robot.md)、[显示屏](../block/screen1.md)，或是通过[适配器](../block/adapter.md)接入的其他模组的方块，都是可以调用的设备。组件的方法与原版模组相同；本手册的提示文本与页面，或者打印设备本身，都能告诉你有哪些方法。

## micropython

`from devices import bus`
`rs = bus.find("redstone")`
`rs.setOutput(1, 15)`
`print(rs)`

`bus.find`返回第一个该类型的设备，`bus.list()`列出所有设备，打印设备则会列出它的方法。返回多个值的方法会以列表形式返回。二进制数据会以bytes形式返回，也可以以bytes形式传入；超过4 KB的数据请传入`bus.blob(data)`。

信号，例如按键、网络消息或红石变化，会以事件的形式到达：
`e = bus.wait_event(5000)`
最多等待五秒并返回下一个事件，其中包含`type`、来源的`deviceId`以及`data`。`bus.wait_event(5000, "redstone_changed")`则只等待某一种事件。

机器本身是名为`computer`的设备，总是排在第一个，提供`beep`、`energy`、`maxEnergy`、`uptime`、`users`、`addUser`、`removeUser`和`pushSignal`。

## 命令行

`component`可以直接在shell里调用组件，通过`ssh`也行。单独运行时列出所有设备；`component redstone`列出某个设备的方法，可以按类型或ID指定；
`component redstone setOutput 1 15`
则调用一个方法。参数能按JSON解析的就按JSON解析，否则当作文本；结果以JSON输出。`component wait`等待下一个信号并输出它。

## 机器人

`import robot`
`robot.forward()`
`robot.turn_left()`
`robot.swing(robot.DOWN)`
`robot`模块可以移动[机器人](../block/robot.md)并让它执行动作：`forward`、`back`、`up`、`down`、`turn_left`、`turn_right`、`detect`、`swing`、`use`、`place`、`drop`、`suck`、`select`、`count`等等。除非指定方向（`robot.UP`或`robot.DOWN`），动作都朝向正前方，成功时返回`True`，失败则返回`None`；`robot.component`包含机器人的全部方法，并能告诉你失败的原因。[机器人](../block/robot.md)移动时，机器会暂停运行。

## Lua与C

Lua的用法类似：
`local bus = require("devices")`
`local rs = bus:find("redstone")`
`rs:setOutput(1, 15)`
`lua /mnt/builtin/bin/lsdev.lua`会列出所有设备。

在C中，包含`/mnt/builtin/include`里的`oc.h`即可直接与组件通信（需要`root`身份）。用`tcc`编译。

## 裸机程序

[无人机](../item/drone.md)和[微控制器](../block/microcontroller.md)不运行Linux。它们运行[EEPROM](../item/eeprom.md)中的一个程序，该程序用C语言基于同一个`oc.h`编写。请在Linux[电脑](computer.md)上构建它，并写入该电脑中的[EEPROM](../item/eeprom.md)：
`ocbuild -o blink.bin blink.c`
`ocflash blink.bin blink`
然后取出[EEPROM](../item/eeprom.md)，放入[微控制器](../block/microcontroller.md)或[无人机](../item/drone.md)。设备启动时程序随之启动，`main`返回时设备关机。如果程序崩溃，[分析器](../item/analyzer.md)会显示崩溃位置。`/mnt/builtin/example`中有让[微控制器](../block/microcontroller.md)闪烁红石的示例（`blink.c`）和让[无人机](../item/drone.md)飞出正方形的示例（`drone.c`）。
