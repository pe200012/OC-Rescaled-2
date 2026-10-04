# 软盘驱动器

![轉啊轉啊...](oredict:oc:diskDrive)

软盘驱动器与[电脑](../general/computer.md)连接后可读取[软盘](../item/floppy.md)。在[Linux](../general/linux.md)中，软盘是一块512 KB的裸盘。机器能看到的每个软盘驱动器（最多4个）都是一个驱动器，编号排在硬盘之后。请用`mke2fs`格式化软盘，然后用`mount`挂载。电脑运行时可以插入或取出软盘，但新接入的软盘驱动器需要重启才能识别。

软盘驱动器也可以装进[机器人](robot.md)中，这样机器人就能插[软盘](../item/floppy.md)了。这一功能作用很大，因为这是不使用网络（例如通过[网卡](../item/lanCard.md)通信）的前提下，机器人唯一一种与外界交换数据的手段。

手持[软盘](../item/floppy.md)潜行与软盘驱动器交互（按住shift右键单击）即可在不打开其GUI的前提下插入或取出[软盘](../item/floppy.md)。
