# 3D打印机

![2D打印太过时了。](oredict:oc:printer)

3D打印机能让你打印出具有任意形状以及任意材质的方块。使用3D打印机的第一步是将3D打印机方块放置在电脑旁边。这样电脑就可以使用`printer3d`组件API了。你可以用其提供的函数创建并打印出[模型](print.md)来。

在Linux中，打印机是设备总线上的一个设备，因此可以用micropython程序操作它：
`from devices import bus`
`p = bus.find("printer3d")`
`print(p)`会列出它的方法。

要打印模型，首先需要用[电脑](../general/computer.md)对3D打印机进行配置。如果设定为不间断打印模式，那么在任务开始后就无需电脑了。你还需要提供[墨盒](../item/inkCartridge.md)以及[变色材料](../item/chamelium.md)作为打印材料。变色材料用量取决于3D打印件的体积，墨水用量则取决于所打印物品的表面积。

用该组件的方法设置形状、材质和标签，然后用`p.commit(1)`开始打印。
