# 开放式电脑手册

开放式电脑（OpenComputers Rescaled²，下简称OC）是一个向Minecraft添加了可持续的、模块化的、可高度定制的[电脑](general/computer.md)、[服务器](item/server1.md)、[机器人](block/robot.md)和[无人机](item/drone.md)的Mod。在此版本中，每台设备都是模拟的64位RISC-V机器：电脑、服务器、机器人和平板电脑运行[Linux](general/linux.md)，而无人机和微控制器则运行其[EEPROM](item/eeprom.md)中的小型裸机程序。你可以用micropython或C为它们编程，参见[编程](general/programming.md)。

你可以通过[手册说明](item/manual.md)来学习这本手册的使用方法（绿色文本为链接，可点击）。

## 目录

### 设备
- [电脑](general/computer.md)
- [服务器](item/server1.md)
- [微控制器](block/microcontroller.md)
- [机器人](block/robot.md)
- [无人机](item/drone.md)
- [平板电脑](item/tablet.md)

### 软件与编程
- [Linux](general/linux.md)
- [编程](general/programming.md)

### 方块与物品
- [物品](item/index.md)
- [方块](block/index.md)

### 教程
- [入门教程](general/quickstart.md)

## 概要

OC模组的电脑是可持续的：当世界保存，或[电脑](general/computer.md)所在区块停止加载时，整台机器连同其内存都会被保存，并在区块再次加载时从中断处继续运行。程序会继续运行，就好像什么都没发生过一样。除[平板电脑](item/tablet.md)以外的所有设备均可持续，平板电脑会重新开始。

所有设备均是模块化的，可用多种组件来组装，正如现实中的[电脑](general/computer.md)一样。硬件没有等级之分：[CPU](item/cpu1.md)、[机箱](block/case1.md)、[显示屏](block/screen1.md)等都只有一种，任何槽位都可以放入任何组件。机器之间的差别在于你往里放了什么：[CPU](item/cpu1.md)的时钟频率、[内存条](item/ram1.md)的容量以及[硬盘](item/hdd1.md)的大小。更快更大的机器耗电也更多。

程序可以通过[编程](general/programming.md)中介绍的设备总线访问所有OC模组的组件，以及通过[适配器](block/adapter.md)访问其他模组的方块。设备可通过多个其他模组供能，包括但不限于红石通量（RF）、工业时代2的EU、通用机械的焦耳（Joules）、应用能源2的能量，以及因式分解/工厂化的Charge（CG）。

[电脑](general/computer.md)是基准。[服务器](item/server1.md)的工作方式相同，但位于[机架](block/rack.md)中。[机器人](block/robot.md)是可移动的[电脑](general/computer.md)，可以与世界交互，[平板电脑](item/tablet.md)则是可以随身携带的电脑；两者都运行[Linux](general/linux.md)并内置[显示屏](block/screen1.md)。[机器人](block/robot.md)组装完成后内部的组件无法拆除；你可以在组装时使用[升级容器](item/upgradeContainer1.md)或[扩展卡容器](item/cardContainer1.md)以便之后更换部件，或者将其[拆解](block/disassembler.md)。[无人机](item/drone.md)和[微控制器](block/microcontroller.md)没有磁盘，也没有足够的空间运行Linux：它们运行写入[EEPROM](item/eeprom.md)的一个裸机程序。

本手册包含了关于模组中所有方块与物品的详细信息、如何搭建各种设备，以及[Linux](general/linux.md)和[编程](general/programming.md)的简单介绍。
