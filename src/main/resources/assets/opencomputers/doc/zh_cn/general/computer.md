# 电脑

电脑是由多种[方块](../block/index.md)与组件搭建而成的。搭建一台可用的电脑至少需要一个[机箱](../block/case1.md)、一个[显示屏](../block/screen1.md)和一个[键盘](../block/keyboard.md)。[键盘](../block/keyboard.md)必须放在[显示屏](../block/screen1.md)上（无论附着于显示屏的某一面或直接放在其正前方均可）。电脑会使用与其[机箱](../block/case1.md)相接的[显示屏](../block/screen1.md)，没有的话则使用网络中找到的第一块，并且只读取连接到该[显示屏](../block/screen1.md)的[键盘](../block/keyboard.md)。因此用[线缆](../block/cable.md)连在一起的多台电脑不会在彼此的[显示屏](../block/screen1.md)上绘制内容。

[机箱](../block/case1.md)中安装的是各种组件：[CPU](../item/cpu1.md)、[内存条（RAM）](../item/ram1.md)、带有Linux引导程序的[EEPROM](../item/eeprom.md)，通常还有[硬盘（HDD）](../item/hdd1.md)。其他组件则提供更多功能：[网卡](../item/lanCard.md)和[因特网卡](../item/internetCard.md)在Linux中表现为网络接口，[红石卡](../item/redstoneCard1.md)让程序可以读取和设置红石，等等。控制台不需要[显卡](../item/graphicsCard1.md)。分步搭建说明请参阅[入门指南](quickstart.md)。

电脑在模拟的64位RISC-V处理器上运行[Linux](linux.md)。带有[硬盘](../item/hdd1.md)的电脑首次启动时，Linux会被安装到该硬盘上。没有[硬盘](../item/hdd1.md)时，电脑会从内存中运行临时系统，电脑停止后一切内容都会丢失。额外的[硬盘](../item/hdd1.md)和[软盘](../item/floppy.md)在Linux中都是普通磁盘，参见[Linux](linux.md)。

运行中的电脑会连同内存一起随世界保存，并在其所在区块再次加载时从中断处继续运行。可在Linux中用`poweroff`关闭电脑，或用`reboot`重启。对[内存条](../item/ram1.md)、[硬盘](../item/hdd1.md)、网卡或[EEPROM](../item/eeprom.md)的更改会在电脑重启时生效；其他组件可以在运行时添加和移除。

最后一个必要步骤是给电脑提供电源。OC模组兼容大多数主流的提供了能源的模组，且很多方块都可被直接供能。你可以查看方块的提示文本，若文本中含有方块的能量转换率条目，那么这个方块就能连接到外部能源。电脑的耗电量取决于其[CPU](../item/cpu1.md)的时钟频率与[内存条](../item/ram1.md)的容量。
对于包含多台电脑的大型网络，可以使用[能量转换器](../block/powerConverter.md)（将其他模组的能量转换为OC的内部能量形式）、[能量分配器](../block/powerDistributor.md)（将能量分配给网络中的不同电脑）、[电容器](../block/capacitor.md)（为网络存储能量）配合[线缆](../block/cable.md)来连接网络中的不同电脑。
