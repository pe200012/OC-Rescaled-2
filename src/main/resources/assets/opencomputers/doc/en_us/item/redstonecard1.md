# Redstone Card

![Seeing red.](oredict:oc:redstoneCard1)

The redstone card allows [computers](../general/computer.md) to read and emit analog redstone signal in adjacent blocks. When an incoming signal strength changes, an event is injected into the [computer](../general/computer.md). Programs use the card through the device bus:

`from devices import bus`
`rs = bus.find("redstone")`
`rs.setOutput(1, 15)`
`e = bus.wait_event(5000)`

If there are any supported mods present that provide bundled redstone facilities, such as RedLogic, Project Red or MineFactory Reloaded; or mods that provide wireless redstone facilities such as WR-CBE and Slimevoid's Wireless mod, the card also allows interacting with these systems.

The side provided to the several methods are relative to the orientation of the [computer case](../block/case1.md) / [robot](../block/robot.md) / [rack](../block/rack.md). That means when looking at the front of the computer, the right side is at your left and vice versa.
