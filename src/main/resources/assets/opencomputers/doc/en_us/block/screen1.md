# Screens

![See this?](oredict:oc:screen1)

A screen shows the output of [computers](../general/computer.md). The Linux console draws onto the screen directly, so no [graphics card](../item/graphicsCard1.md) is needed for it; a graphics card is still a component that programs can call. Screens have a resolution of up to 160x50 characters and 256 colors, and the console is 80x24 characters. This includes the screens built into [robots](robot.md) and [tablets](../item/tablet.md).

The console uses the screen touching the computer case if there is one, otherwise the first screen on the network. Only [keyboards](keyboard.md) attached to that screen type on it. Several computers cabled together do not draw on each other's screens.

Screens can be placed next to each other to form multi-block screens, as long as they are facing the same way. When placed facing up or down they must also be rotated the same way. Their orientation is indicated by an arrow overlay shown while holding a screen in hand.

The size of a screen has no impact on the available resolution. To control how adjacent screens connect, screens can also be dyed using any dye. Simply right-click the screen with a dye in hand. The dye will not be consumed, but screens will not retain this color when broken. Screens with different colors will not connect.

Screens also support mouse input. Clicks can either be performed in a screen's GUI (which can only be opened if a [keyboard](keyboard.md) is connected to the screen), or by sneak-right-clicking a screen in the world. While a program draws on the framebuffer, the mouse position and buttons are reported in framebuffer pixels (/dev/input/event1). In the screen's GUI, moving the mouse without a button also moves the pointer.

Linux also has a framebuffer, /dev/fb0, with 320x192 pixels. As soon as a program draws on it, the screen shows the pixels. When the drawing stops for a second and the console prints again, the screen goes back to text. Try `micropython /mnt/builtin/example/framebuffer.py`.
