# OpenComputers Manual

OpenComputers Rescaled² is a mod that adds persistent, modular, and highly configurable [computers](general/computer.md), [servers](item/server1.md), [robots](block/robot.md), and [drones](item/drone.md) to the game. In this version, every device is an emulated 64-bit RISC-V machine: computers, servers, robots and tablets run [Linux](general/linux.md), while drones and microcontrollers run small bare-metal programs from their [EEPROM](item/eeprom.md). You program them in micropython or C, see [programming](general/programming.md).

To learn about how to use the manual, check out [the page about the manual](item/manual.md) (that green text is a link, you can click it).

## Table of Contents

### Devices
- [Computers](general/computer.md)
- [Servers](item/server1.md)
- [Microcontrollers](block/microcontroller.md)
- [Robots](block/robot.md)
- [Drones](item/drone.md)
- [Tablets](item/tablet.md)

### Software and Programming
- [Linux](general/linux.md)
- [Programming](general/programming.md)

### Blocks and Items
- [Items](item/index.md)
- [Blocks](block/index.md)

### Guides
- [Getting Started](general/quickstart.md)

## Overview

Computers in OpenComputers are persistent: when the world is saved or the chunk a [computer](general/computer.md) is in is unloaded, the whole machine, its memory included, is saved, and it continues where it left off when the chunk is loaded again. Programs keep running as if nothing happened. This works for all devices except [tablets](item/tablet.md), which start fresh.

All devices are modular and can be assembled with a wide range of components, just like [computers](general/computer.md) in real life. Hardware is not tiered: there is one kind of [CPU](item/cpu1.md), [computer case](block/case1.md), [screen](block/screen1.md) and so on, and any slot takes any component. Machines differ in what you put into them instead: the [CPU](item/cpu1.md)'s clock rate, the amount of [memory](item/ram1.md) and the size of the [hard drives](item/hdd1.md). Faster and bigger machines draw more power.

Programs reach every OpenComputers component, and the blocks of other mods through the [adapter](block/adapter.md), through the device bus described in [programming](general/programming.md). Power can be supplied using a large range of other mods, including, but not limited to, Redstone Flux, IndustrialCraft2 EU, Mekanism Joules, Applied Energistics 2 energy as well as Factorization Charge.

[Computers](general/computer.md) are the base-line. [Servers](item/server1.md) work the same way but live in a [rack](block/rack.md). [Robots](block/robot.md) are moving [computers](general/computer.md) that can interact with the world, and [tablets](item/tablet.md) are computers you carry around; both run [Linux](general/linux.md) with a built-in [screen](block/screen1.md). Once a [robot](block/robot.md) is built, the components inside it cannot be removed; build it with [upgrade](item/upgradeContainer1.md) or [card](item/cardContainer1.md) containers to swap parts later, or [disassemble](block/disassembler.md) it. [Drones](item/drone.md) and [microcontrollers](block/microcontroller.md) have no disk and too little room for Linux: they run one bare-metal program written to their [EEPROM](item/eeprom.md).

This manual contains information about all blocks and items, how to set up different types of devices, and an introduction to [Linux](general/linux.md) and [programming](general/programming.md) them.
