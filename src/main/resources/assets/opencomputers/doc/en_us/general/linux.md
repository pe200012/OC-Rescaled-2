# Linux

[Computers](computer.md), [servers](../item/server1.md), [robots](../block/robot.md) and [tablets](../item/tablet.md) run Linux on an emulated 64-bit RISC-V processor. It is a small but real Linux: a BusyBox shell with the usual tools, the editors `nano` and `vi`, `micropython`, Lua 5.4, the C compiler `tcc`, and `ssh`/`scp` (dropbear). Log in as `root`; there is no password.

## Starting up

The [EEPROM](../item/eeprom.md) "EEPROM (Linux)", crafted from an [EEPROM](../item/eeprom.md) and a [manual](../item/manual.md), holds the boot loader. It starts Linux from the first [hard drive](../item/hdd1.md). A new first [hard drive](../item/hdd1.md) gets Linux installed the first time the machine starts. Without a [hard drive](../item/hdd1.md), Linux runs as a live system from memory, which forgets everything when the machine stops. Linux needs at least 8 MB of [memory](../item/ram1.md).

If a machine does not start, sneak-use an [analyzer](../item/analyzer.md) on it to see why.

## Disks

[Hard drives](../item/hdd1.md) and [floppy disks](../item/floppy.md) are plain disks: `/dev/vda` is the first [hard drive](../item/hdd1.md), `/dev/vdb` the next, and the floppy drives come after the [hard drives](../item/hdd1.md). The floppy slot of the case and every [disk drive](../block/diskDrive.md) the machine can reach is one drive. New disks are blank; give them a file system and mount them:
`mke2fs /dev/vdb`
`mount /dev/vdb /mnt`
[Floppy disks](../item/floppy.md) can be inserted and removed while Linux runs; `umount` them first. [Hard drives](../item/hdd1.md), and newly connected [disk drives](../block/diskDrive.md), need the machine to restart. The disk images are kept in the world save, in `opencomputers-riscv/disks`, where you can also open them from outside the game.

## Screen, keyboard and mouse

The console is 80x24 characters. Paste text with the middle mouse button or the paste key (Insert by default).

Programs can also draw pixels: `/dev/fb0` is a 320x192 framebuffer with 32-bit pixels. As soon as a program draws, the [screen](../block/screen1.md) shows the pixels; once it stops for a second and the console prints again, the [screen](../block/screen1.md) shows text again. Keys also arrive at `/dev/input/event0`, and while the [screen](../block/screen1.md) shows pixels, `/dev/input/event1` reports where it is clicked, dragged and scrolled, in pixels. In the world, sneak-use a [screen](../block/screen1.md) to click it. Try `micropython /mnt/builtin/example/framebuffer.py`.

## Networking

[Network cards](../item/lanCard.md) and [wireless network cards](../item/wlanCard1.md) are network interfaces, `eth0`, `eth1` and so on, and Linux talks TCP/IP over OpenComputers' [cables](../block/cable.md), [relays](../block/relay.md) and wireless network. Interfaces start unconfigured:
`ip addr add 10.0.0.1/24 dev eth0`
`ip link set eth0 up`
Give the next machine `10.0.0.2`, and `ping 10.0.0.1` reaches the first. `lua /mnt/builtin/bin/setup-network.lua` asks for the settings and keeps them.

An [internet card](../item/internetCard.md) is the next interface after the network cards, with a gateway at `10.0.2.2`:
`ip addr add 10.0.2.15/24 dev eth1`
`ip link set eth1 up`
`ip route add default via 10.0.2.2`
`echo nameserver 1.1.1.1 > /etc/resolv.conf`
Then `wget` and `ssh` reach the real internet. Connections can only be made outwards.

## Saving and stopping

A running machine is saved with the world and continues where it left off; [tablets](../item/tablet.md) start fresh instead. `poweroff` turns the machine off, `reboot` restarts it, and a restart is needed after changing [memory](../item/ram1.md), [hard drives](../item/hdd1.md), network cards or the [EEPROM](../item/eeprom.md). Turning a machine off with its power button, or letting it run out of energy, is like pulling the plug: files that were not written yet are lost, but the system's file system stays intact.

## The mod's own files

`/mnt/builtin` holds what the mod brings along: the device bus libraries, examples, and the tools and headers for C. How to use them is described in [programming](programming.md).
