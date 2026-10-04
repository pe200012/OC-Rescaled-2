# EEPROM

![派对，启动！](oredict:oc:eeprom)

EEPROM容量为64 KB，其中包含了用于在电脑引导启动过程中进行初始化的代码。电脑会启动其EEPROM中的程序，该程序必须是RISC-V程序。将EEPROM与[手册](manual.md)合成可得到“EEPROM (Linux)”，其中存有Linux引导程序。

EEPROM可被编程为裸机程序用于特殊用途，例如[无人机](drone.md)与[微控制器](../block/microcontroller.md)。如果EEPROM中存的是文本，电脑会报告“EEPROM holds text, not a RISC-V program”（EEPROM中是文本，不是RISC-V程序）。如果没有EEPROM或其内容为空，电脑会报告“no bootable EEPROM”（没有可引导的EEPROM）。对机器使用[分析器](analyzer.md)可查看引导错误。
