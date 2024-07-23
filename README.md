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

|               Environment               |  MSPT(Min)  | MSPT(Avg.) | MSPT(Max) | Percent(Avg.) |
|:---------------------------------------:|:-----------:|:----------:|:---------:|:-------------:|
|                 Vanilla                 |   160 ms    |   183 ms   |  241 ms   |     100 %     |
|               With Sepals               |   128 ms    |   157 ms   |  191 ms   |     85 %      |
|              With Lithium               |   111 ms    |   134 ms   |  205 ms   |     73 %      |
| With Sepals<br/> and configured Lithium |    72 ms    |   88 ms    |  115 ms   |     48 %      |

### Weighted random
997 frogs cramming in a 10x10 space:

|                  Environment                   | MSPT(Min) | MSPT(Avg.) | MSPT(Max) | Percent(Avg.) |
|:----------------------------------------------:|:---------:|:----------:|:---------:|:-------------:|
|                    Vanilla                     |  105 ms   |   135 ms   |  206 ms   |     100 %     |
|                  With Sepals                   |   43 ms   |   96 ms    |  163 ms   |     71 %      |
| Direct random <br/> (Not vanilla, no approved) |   45 ms   |   73 ms    |  111 ms   |     54 %      |
