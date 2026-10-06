# OpenComputers Rescaled²

A fork of [OpenComputers Rescaled][ocr] for Minecraft 1.12.2 on [Cleanroom][cleanroom]. Lua is gone. Every device is an emulated 64-bit RISC-V machine (RV64GC, using [Sedna][sedna]). Computers, servers, robots and tablets run Linux. Drones and microcontrollers run bare-metal programs from their EEPROM.

The mod id and the API (`li.cil.oc.api`) are still OpenComputers', so other mods' OpenComputers drivers keep working. For the same reason, this mod cannot be installed alongside OpenComputers.

## What changed

- **Linux.** Machines boot a small [Buildroot][buildroot] Linux from the first hard drive, or run it live from memory when there is no drive. The image has BusyBox, `nano` and `vi`, MicroPython, Lua 5.4, the C compiler `tcc`, and Dropbear for `ssh`, `scp` and `sftp`.
- **Persistence.** A running machine is saved with the world, memory included, and continues where it left off when its chunk loads again.
- **Components.** Programs reach components through OpenComputers II's device bus protocol. The bus has libraries for MicroPython and Lua, a `component` shell command, and an `oc.h` header for C. Components keep their original methods. Robots have a MicroPython `robot` module.
- **Disks.** Hard drives and floppy disks are virtio block devices (`/dev/vda`, …). Their images are stored in the world save under `opencomputers-riscv/disks`. The root file system is journaled. Floppies can be swapped while Linux runs.
- **Screens.**
  - The console is 80x24 characters.
  - `/dev/fb0` is a 320×192 framebuffer.
  - Keyboard and mouse input arrives through `/dev/input`.
- **Networking.**
  - Network cards are Ethernet interfaces that carry TCP/IP over cables, relays and wireless.
  - The internet card is a NAT gateway to the real internet.
  - Ports listed in the config are forwarded from the host, so you can `ssh` into a computer from outside the game.
- **Drones and microcontrollers.** They run one C program from their EEPROM. You build it with `ocbuild` on an in-game Linux computer and write it to the EEPROM with `ocflash`.
- **Flat hardware.** There is one kind of each CPU, case, screen and so on, and any slot takes any component. Machines differ in clock rate, memory and disk size. Power draw follows those.
- **Manual.** The in-game manual is rewritten for all of the above, in English, German, French, Russian and Chinese.

Status: beta. It has been tested in the single-player client only, not on a dedicated server.

## Requirements

- Minecraft 1.12.2 with Cleanroom
- [Scalar][scalar], for the Scala 3 runtime

## Building

Building needs JDK 25 and Docker. Buildroot compiles the Linux image inside Buildroot's CI Docker image. The first build takes a while; after that, the image is rebuilt only when its configuration changes.

```sh
git clone --recursive <this repository>
./gradlew build        # mod jar in build/libs
./gradlew runClient    # development client
```

The code is in a few places:

- `riscv/` is the Minecraft-independent machine: board setup, devices, the terminal and the device bus. It is shaded into the mod jar.
- `src/main/scala/ocsquared` is the mod itself.
- `riscv/src/main/scripts` holds the guest-side libraries and tools, which are mounted in the machine at `/mnt/builtin`.
- `deps/` holds Sedna, Ceres and Buildroot as submodules.

## Credits

- **[OpenComputers][oc]** by Florian "Sangar" Nücke, payonel, Vexatos, asie, magik6k and [all its contributors][oc-contributors]. Nearly everything here except the machine is their work.
- **[OpenComputers Rescaled][ocr]** by kappa-maintainer, which ports OpenComputers to Cleanroom and Scala 3. This fork starts from it.
- **Florian Nücke's [OpenComputers II][oc2]**, which provides several pieces:
  - [Sedna][sedna], the RISC-V emulator;
  - [Ceres][ceres], its serialization library;
  - the [Buildroot tree][oc2-buildroot] the Linux image is built from;
  - the device bus protocol and its guest libraries;
  - the VT100 terminal.
- **[CleanroomMC][cleanroom]** for Cleanroom and Scalar.
- **The Linux image** is made from [Buildroot][buildroot], Linux, BusyBox, MicroPython, Lua, TinyCC, Dropbear and the other packages Buildroot builds. Each keeps its own license.

## AI use

This fork was written with AI. Claude (Anthropic's Claude Opus 5.5, in Claude Code) wrote the following:

- the changes to OpenComputers Rescaled;
- the `oc-riscv` branch of the Buildroot fork;
- the manual in all five languages;
- this README.

pe200012 directed the work, made the design decisions, and tested the changes in game before committing them. The manual's translations have not been proofread by native speakers. Code from OpenComputers, OpenComputers Rescaled and OpenComputers II is by those projects' authors, although some of it has been changed here.

## License

The code is under the MIT license (see [LICENSE](LICENSE)). The assets are public domain unless stated otherwise. The licenses of the APIs used from other mods are in [LICENSE-mods](LICENSE-mods). The scripts taken from OpenComputers II are under the MIT license as well.

[oc]: https://github.com/MightyPirates/OpenComputers
[oc-contributors]: https://github.com/MightyPirates/OpenComputers/graphs/contributors
[ocr]: https://github.com/kappa-maintainer/OpenComputers
[oc2]: https://github.com/fnuecke/oc2
[sedna]: https://github.com/fnuecke/sedna
[ceres]: https://github.com/fnuecke/ceres
[oc2-buildroot]: https://github.com/fnuecke/buildroot
[buildroot]: https://buildroot.org
[cleanroom]: https://github.com/CleanroomMC
[scalar]: https://github.com/CleanroomMC/Scalar
