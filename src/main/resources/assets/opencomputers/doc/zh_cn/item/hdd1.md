# 硬盘

![空——间——](oredict:oc:hdd1)

硬盘是OC模组中主要的存储媒介。它有16、32和64 MB三种容量。在Linux中每块硬盘都是一块裸盘：`/dev/vda`是第一块，`/dev/vdb`是下一块，依此类推。第一块硬盘会在[电脑](../general/computer.md)首次启动时自动安装Linux。其他硬盘一开始是空白的，请用`mke2fs`和`mount`准备它们。请只在机器关闭时更换硬盘。

硬盘还可放入[硬盘阵列柜](../block/raid.md)中，与其他硬盘组合为同一个OpenComputers文件系统。Linux只能把它当作设备总线上的`filesystem`设备，而不是磁盘。请注意硬盘在放入[硬盘阵列柜](../block/raid.md)后会被抹除所有数据。
