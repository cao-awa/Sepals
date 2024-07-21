# Sepals
An extremely radical and experimental optimization for Minecraft server performances.

## Compatibility
Currently, sepals are compatible to almost all mods.

### Lithium
The entities cramming optimization of sepals will be auto-disabled when you used lithium and without configurations.

This feature require disable lithium's cramming to enable:

```properties
mixin.entity.collisions.unpushable_cramming=false
```
## Performance
A test was done in cao-awa personal computer, CPU: 5950x; Memory: 128G DDR4-3200MHz; OS: Windows 11; Minecraft: 1.21.

### Entities cramming
2993 large slime cramming in a 20x20 space:

|    Environment   |  MSPT(Min)   | MSPT(Avg.) | MSPT(Max) |
|:------------------:|:---------:|:--------------------------:|:----------:|
|   Vanilla   |   160 ms   |         183 ms         |  241 ms  |
| With Sepals |   128 ms   |         157 ms         | 191 ms |
| With Lithium |   111 ms   |         134 ms         | 205 ms |
| With Sepals<br/> and configured Lithium |   72 ms   |         88 ms         | 115 ms |
