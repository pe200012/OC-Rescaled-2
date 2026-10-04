# Redstone I/O

![Hi Red.](oredict:oc:redstone)

The redstone I/O block can be used to remotely read and emit redstone signals. It behaves like a [redstone card](../item/redstoneCard1.md): it can read and emit simple analog as well as bundled signals, but cannot read or emit wireless redstone signals.

When providing a side to the methods of the component exposed by this block, the directions are the global principal directions of the world. The block's texture features subtle indentations corresponding to the numeric value of each side. In micropython, use `rs = bus.find("redstone")` after `from devices import bus`, then for example `rs.setOutput(1, 15)`.

Like the [redstone cards](../item/redstoneCard1.md), this block sends a signal event (read with `bus.wait_event()`) to connected [computers](../general/computer.md) when the state of a redstone signal changes - both for analog as well as for bundled signals. This block can also be configured to wake up connected [computers](../general/computer.md) when a certain input strength is exceeded, allowing automated booting of [computers](../general/computer.md).
