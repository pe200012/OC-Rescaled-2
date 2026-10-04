# 入门

此教程又名《如何组装你的第一台电脑》。为了让你的[电脑](computer.md)开始运行，你首先需要将它正确搭建起来。OC模组中有很多种电脑，但我们从基础的开始：标准电脑。

**声明**：这是手把手的教程，还会告诉你后续如何自己查找问题，因此教程比较长。如果现实中你没有装机经历，并且/或者你是第一次接触此模组，那么推荐你通读全文。

首先，你需要一个[机箱](../block/case1.md)。这个方块会容纳所有组件，也决定了你所搭建电脑的行为。

![一个机箱。](oredict:oc:case3)

打开[机箱](../block/case1.md)的GUI后你会看到右边若干槽位。任何组件都可以放入对应类型的任意槽位，不需要考虑等级问题。

空[机箱](../block/case1.md)基本没什么用。你可以尝试现在启动[电脑](computer.md)，但它会立刻向你的聊天框输出一条报错信息，然后用滴声表达它的不满。好消息是报错信息告诉了你修复方式：电脑需要能量。只需要给你的电脑接通电源，无论直接连接或是通过[能量转换器](../block/powerConverter.md)连接均可。

这个时候再尝试启动，它会告诉你电脑需要安装[CPU](../item/cpu1.md)。[CPU](../item/cpu1.md)只有一种，但它可以以25、50、100或200 MHz运行：潜行使用它即可切换。越快越好，但耗电也越多；50 MHz是个不错的起点。把它装进你的[机箱](../block/case1.md)里。

接下来会要求你安装一些[内存条（RAM）](../item/ram1.md)。你会发现报警音发生了变化：变成了长-短。[内存条](../item/ram1.md)的容量从1 MB到32 MB不等，你安装的内存条容量会累加。Linux至少需要8 MB，16 MB比较宽裕。

看啊，现在将它打开已经不会输出报错信息了！但是，哎呀，它还是干不了什么事情。至少它现在会滴两次了。这代表[电脑](computer.md)启动了但立刻失败了。此时一个非常实用的工具该上场表演了：[分析器](../item/analyzer.md)。它可用来检查OC模组的很多方块，也支持其他模组的一些方块。要对[电脑](computer.md)进行使用，只需手持[分析器](../item/analyzer.md)潜行与机箱交互。

你会看到导致[电脑](computer.md)发生崩溃的错误：
`no bootable EEPROM`
（没有可引导的EEPROM）

电脑会运行其[EEPROM](../item/eeprom.md)中的程序，而它现在没有。合成[EEPROM](../item/eeprom.md)很简单，而对[电脑](computer.md)而言，我们需要一个存有Linux引导程序的：将[EEPROM](../item/eeprom.md)与[手册](../item/manual.md)合成在一起，就能得到“EEPROM (Linux)”。把它放进你的[电脑](computer.md)。

引导程序会在第一块[硬盘](../item/hdd1.md)上寻找Linux。请往机箱里放一块[硬盘](../item/hdd1.md)：电脑第一次启动时，Linux会被安装到其中，你在上面保存的一切内容都会保留。没有[硬盘](../item/hdd1.md)时电脑仍能启动，但Linux会从内存中运行，停止后一切内容都会丢失；用来试玩一下倒也足够。

按下电源键。它活了！或者说应该是。如果它没能启动的话，代表有什么东西出错了，你可以用[分析器](../item/analyzer.md)排查。不过我们先假设它现在已经开始运行了，你已经接近完工了。剩下要做的就是让它接收输入并显示输出。

要查看[电脑](computer.md)在做什么，你需要取一块[显示屏](../block/screen1.md)。不需要显卡：控制台会直接绘制在[显示屏](../block/screen1.md)上。
![这不是纯平显示器。](oredict:oc:screen3)

请将[显示屏](../block/screen1.md)放置于直接相邻[机箱](../block/case1.md)的位置，或者通过[线缆](../block/cable.md)连接。你现在应该能在[显示屏](../block/screen1.md)上看到Linux启动了。最后，将[键盘](../block/keyboard.md)放置在[显示屏](../block/screen1.md)身上，或将其面对[显示屏](../block/screen1.md)放置，以启用[键盘](../block/keyboard.md)输入。

做完这一步，你就完工了。以`root`登录，没有密码。你现在身处Linux shell中。试试`ls /mnt/builtin`看看模组带来了什么，或者输入`micropython`进入Python提示符。[Linux](linux.md)页面会告诉你更多有关这个系统的信息，[编程](programming.md)页面则介绍如何从中控制红石、机器人以及其他一切。

请享受搭建更复杂的[电脑](computer.md)，折腾[服务器](../item/server1.md)以及用[电子装配机](../block/assembler.md)组装[机器人](../block/robot.md)、[无人机](../item/drone.md)、[微控制器](../block/microcontroller.md)和[平板电脑](../item/tablet.md)。

祝编程愉快！
