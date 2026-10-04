# Programming

Programs on a [computer](computer.md) reach the world through the device bus: every component the machine can see, such as a [redstone card](../item/redstoneCard1.md), a [robot](../block/robot.md), a [screen](../block/screen1.md), or another mod's block through an [adapter](../block/adapter.md), is a device you can call. Components have the same methods as in the original mod; the tooltips and pages of this manual, or printing a device, tell you which.

## micropython

`from devices import bus`
`rs = bus.find("redstone")`
`rs.setOutput(1, 15)`
`print(rs)`

`bus.find` returns the first device of a kind, `bus.list()` lists them all, and printing a device lists its methods. A method that returns several values returns them as a list. Binary data comes back as bytes and can be passed as bytes; for more than 4 KB, pass `bus.blob(data)`.

Signals, such as key presses, network messages or redstone changes, arrive as events:
`e = bus.wait_event(5000)`
waits up to five seconds and returns the next one, with its `type`, the `deviceId` it came from and its `data`. `bus.wait_event(5000, "redstone_changed")` waits for one kind only.

The machine itself is the device `computer`, always the first one, with `beep`, `energy`, `maxEnergy`, `uptime`, `users`, `addUser`, `removeUser` and `pushSignal`.

## The shell

`component` calls components straight from the shell, also over `ssh`. On its own it lists the devices, `component redstone` lists a device's methods, by type or ID, and
`component redstone setOutput 1 15`
calls one. Arguments are read as JSON where they can be, otherwise as text, and results print as JSON. `component wait` waits for the next signal and prints it.

## Robots

`import robot`
`robot.forward()`
`robot.turn_left()`
`robot.swing(robot.DOWN)`
The `robot` module moves the [robot](../block/robot.md) and lets it act: `forward`, `back`, `up`, `down`, `turn_left`, `turn_right`, `detect`, `swing`, `use`, `place`, `drop`, `suck`, `select`, `count` and more. Actions work towards the front unless given a side (`robot.UP` or `robot.DOWN`) and return `True`, or `None` if they failed; `robot.component` has all the robot's methods and tells why something failed. The machine pauses while the [robot](../block/robot.md) moves.

## Lua and C

Lua works the same way:
`local bus = require("devices")`
`local rs = bus:find("redstone")`
`rs:setOutput(1, 15)`
`lua /mnt/builtin/bin/lsdev.lua` lists all devices.

From C, include `oc.h` from `/mnt/builtin/include`, which talks to components directly (as `root`). Compile with `tcc`.

## Bare-metal programs

[Drones](../item/drone.md) and [microcontrollers](../block/microcontroller.md) do not run Linux. They run one program from their [EEPROM](../item/eeprom.md), written in C against the same `oc.h`. Build it on a Linux [computer](computer.md) and write it to the [EEPROM](../item/eeprom.md) in that computer:
`ocbuild -o blink.bin blink.c`
`ocflash blink.bin blink`
Then take the [EEPROM](../item/eeprom.md) out and put it into the [microcontroller](../block/microcontroller.md) or [drone](../item/drone.md). The program starts when the device does, and the device shuts down when `main` returns. If the program crashes, the [analyzer](../item/analyzer.md) shows where. `/mnt/builtin/example` has a [microcontroller](../block/microcontroller.md) blinking redstone (`blink.c`) and a [drone](../item/drone.md) flying a square (`drone.c`).
