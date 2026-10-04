# Hard Disk Drive

![Spaaaace.](oredict:oc:hdd1)

The hard disk drives are the main storage medium in OpenComputers. They come in 16, 32 and 64 MB. Each one is a raw disk in Linux: `/dev/vda` is the first, `/dev/vdb` the next, and so on. The first hard drive gets Linux installed automatically when the [computer](../general/computer.md) starts for the first time. Other drives start blank; use `mke2fs` and `mount` to prepare them. Only swap hard drives while the machine is off.

Hard drives can be placed inside a [raid](../block/raid.md), which combines them into one OpenComputers file system. Linux sees it only as a `filesystem` device on the device bus, not as a disk. Note that placing a hard drive in a [raid](../block/raid.md) wipes the drive of its contents.
