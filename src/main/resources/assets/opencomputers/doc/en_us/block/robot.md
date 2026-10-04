# Robot

![His name was Tobor.](block:OpenComputers:robot)

Unlike [computers](../general/computer.md), robots can move around and interact with the world much like a player can. They can *not* interact with external components, however! If you need to communicate with a [computer](../general/computer.md) or other robots, use a [wireless network card](../item/wlanCard1.md), or create some low-level protocol using redstone signals via a [redstone card](../item/redstoneCard1.md), for example.

Robots are built by placing a [computer case](case1.md) in an [assembler](assembler.md). Robots run Linux, like [computers](../general/computer.md). In Linux, `import robot` in micropython gives functions such as `robot.forward()`, `robot.turn_left()`, `robot.swing()` and `robot.place()`; `robot.component` has every robot method. The machine pauses while the robot moves or turns.

Various upgrades can be placed into robots to increase the functionality. These include [inventory](../item/inventoryUpgrade.md) and [inventory controller](../item/inventoryControllerUpgrade.md) upgrades, [tank upgrades](../item/tankUpgrade.md), [navigation upgrade](../item/navigationUpgrade.md), among others. [Upgrade](../item/upgradeContainer1.md) and [card](../item/cardContainer1.md) containers can be placed in the robot for on-the-fly insertion and removal of upgrades and components. A [disk drive](diskDrive.md) can also be placed inside a robot to allow [floppy disks](../item/floppy.md) to be inserted. Put a [hard drive](../item/hdd1.md) in the robot to let it install [Linux](../general/linux.md) on first start. 
