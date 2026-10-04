# Getting Started

Also know as "how to build your first computer". To get your first [computer](computer.md) to run, you will need to first set it up correctly. There are many different types of computers in OpenComputers, but let's start with the basic one: the standard computer.

**Disclaimer**: this will be step-by-step, and also provide some information on how to look for issues yourself later on, so this is quite long. If you have never built a computer in real life, and/or are completely new to the mod, it is highly recommended you read through it all.

First off, you will need a [computer case](../block/case1.md). This is the block which will contain all of the components, defining the behavior of the computer you are building.

![A computer case.](oredict:oc:case3)

When you open the [computer case](../block/case1.md)'s GUI, you will see a few slots to the right. Any component goes into any slot of its kind: there are no tiers to worry about.

In their empty state, [computer cases](../block/case1.md) are pretty useless. You can try to power up your [computer](computer.md) now, but it'll immediately print an error message to your chat log, and make its dissatisfaction heard by beeping at you. Good thing the error message is telling you what you can do to fix this situation: it requires energy. Connect your [computer](computer.md) to some power, either directly or via a [power converter](../block/powerConverter.md).

When you try to start it now, it will tell you that you need a [CPU](../item/cpu1.md). There is one kind of [CPU](../item/cpu1.md), but it can run at 25, 50, 100 or 200 MHz: use it while sneaking to switch. Faster is nicer, but draws more power; 50 MHz is a fine start. Put it in your [computer case](../block/case1.md).

Next up you will be asked to insert some [memory (RAM)](../item/ram1.md). Notice that the beep code is different now: long-short. [Memory](../item/ram1.md) comes in sizes from 1 MB to 32 MB, and the sticks you install add up. Linux needs at least 8 MB; 16 MB is comfortable.

And behold, turning it on now does not print any more error messages! But alas, it still doesn't do much. At least it beeps twice now. That means the [computer](computer.md) started but failed right away. This is where a very useful tool comes into play: the [analyzer](../item/analyzer.md). This tool allows inspecting many of OpenComputers' blocks, as well as some blocks from other mods. To use it on the [computer](computer.md), use the [analyzer](../item/analyzer.md) on the case while sneaking.

You should now see the error that caused the [computer](computer.md) to crash:
`no bootable EEPROM`

The computer runs whatever program is on its [EEPROM](../item/eeprom.md), and it has none. Crafting an [EEPROM](../item/eeprom.md) is pretty simple, and for a [computer](computer.md) we want one holding the Linux boot loader: craft an [EEPROM](../item/eeprom.md) together with a [manual](../item/manual.md) to get an "EEPROM (Linux)". Put it into your [computer](computer.md).

The boot loader looks for Linux on the first [hard drive](../item/hdd1.md). Put a [hard drive](../item/hdd1.md) into the case: the first time the computer starts, Linux is installed onto it, and everything you save there stays. Without a [hard drive](../item/hdd1.md) the computer still starts, but runs Linux from memory and forgets everything when it stops; fine for trying things out.

Press the power button. It lives! Or should, anyway. If it doesn't, something went wrong, and you'll want to investigate using the [analyzer](../item/analyzer.md). But assuming it's running now, you're pretty much done. All that's left is to make it take input and show some output.

To see what the [computer](computer.md) is doing, you'll want to grab a [screen](../block/screen1.md). No graphics card is needed: the console is drawn onto the [screen](../block/screen1.md) directly.
![No, it's not a flatscreen.](oredict:oc:screen3)

Place the [screen](../block/screen1.md) adjacent to your [computer case](../block/case1.md), or connect it using some [cable](../block/cable.md). You should now see Linux booting on the [screen](../block/screen1.md). Finally, place a [keyboard](../block/keyboard.md) either on the [screen](../block/screen1.md) itself, or in a way so that it faces the [screen](../block/screen1.md), to enable [keyboard](../block/keyboard.md) input.

And with that, you're done. Log in as `root`; there is no password. You are now in a Linux shell. Try `ls /mnt/builtin` to see what the mod brings along, or `micropython` to get a Python prompt. The [Linux](linux.md) page tells you more about the system, and [programming](programming.md) how to control redstone, robots and everything else from it.

Have fun building more complex [computers](computer.md), messing with [servers](../item/server1.md) and assembling [robots](../block/robot.md), [drones](../item/drone.md), [microcontrollers](../block/microcontroller.md) and [tablets](../item/tablet.md) in the [assembler](../block/assembler.md).

Happy coding!
