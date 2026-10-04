# 3D Printer

![2D printing is so yesteryear.](oredict:oc:printer)

3D printers allow you to print any block of any shape, with any type of texture. To get started with 3D printers, you will need to place down a 3D printer block next to a computer. This will give access to the `printer3d` component API, allowing you to set up and print [models](print.md) using the provided functions.

In Linux, the printer is a device on the device bus, so a micropython program can use it:
`from devices import bus`
`p = bus.find("printer3d")`
`print(p)` lists its methods.

In order to be able to print the models, a 3D printer needs to be configured via a [computer](../general/computer.md). If set to print non-stop, the computer will no longer be required thereafter. You will also need to provide an [ink cartridge](../item/inkCartridge.md) and some [chamelium](../item/chamelium.md) as input materials. The amount of chamelium used depends on the volume of the 3D print, while the amount of ink used depends on the surface area of the printed item.

Set up the shapes, textures and label with the component's methods, then start the print with `p.commit(1)`.
