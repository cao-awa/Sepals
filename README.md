# Sepals

An extremely radical and experimental optimization for Minecraft server performances.

## Compatibility

Currently, sepals are compatible to almost all mods.

## Performance

A test was done in server of Mars provided by feimia, CPU: Intel i7-14700K; Game memory: 4G; OS: Ubuntu 24.04.1 LTS;
Minecraft: 1.21 .

### Entities cramming

```
Use index to access element to replaced iterator

-- Status --
Default disabled, no recommend to use now

-- Warining -- 
Not tested.
```

The entities cramming optimization of sepals will be auto-disabled when you used lithium and without configurations.

This feature require disable lithium's cramming to enable:

```properties
mixin.entity.collisions.unpushable_cramming=false
```

### Weighted random

```
Use binary search to replaced vanilla weight random

-- Warning --
This feature may not provided good optimization
because it was proved by spark that slower than vanilla when constructing range table
even if the binary search almost close to 0ms

-- Status --
Default disabled

-- Warining -- 
Not tested.
```

### Biased long jump task

```
Use sepals long jump task impletation to replaced vanilla impletation

As mentioned above, the binary search is almost no costs
sepals impletation will construct the range table at the same time as generating targets
and used Catheter to replaced java stream

The same time, rearrange should jump conditions in frog brain, make a good performance

-- Notice --
This feature is unable to change in game runtime
required restart the server to apply changes

-- Status --
Default enabled
```

1366 frogs cramming in a 7x7 space:

|                   Environment                   | keepRunning | Percent(Avg.) |
|:-----------------------------------------------:|:-----------:|:-------------:|
|          Vanilla <br /> (LongJumpTask)          |   43.1 ms   |     100 %     |
|    With Lithium <br /> (SepalsLongJumpTask)     |   7.5 ms    |     17 %      |
|     With Sepals <br /> (SepalsLongJumpTask)     |   0.2 ms    |    0.004 %    |
| With Sepals+Lithium <br /> (SepalsLongJumpTask) |   0.05 ms   |    0.001 %    |

|                   Environment                   | getTarget | Percent(Avg.) | Percent(in ```keepRunning```) |
|:-----------------------------------------------:|:---------:|:-------------:|:-----------------------------:|
|          Vanilla <br /> (LongJumpTask)          |  43.1 ms  |     100 %     |             100 %             |
|     With Sepals <br /> (SepalsLongJumpTask)     |  3.6 ms   |     26 %      |             48 %              |
|     With Sepals <br /> (SepalsLongJumpTask)     |  N/A ms   |      0 %      |              0 %              |
| With Sepals+Lithium <br /> (SepalsLongJumpTask) |  N/A ms   |      0 %      |              0 %              |

### Quick sort in NearestLivingEntitiesSensor

```
Quick sort supports by fastutil to replace java tim sort

-- Status --
Default enabled
```

800 frogs cramming in a 7x7 space:

|         Environment | sort (NearestLivingEntitiesSensor#sense) | Percent(Avg.) |
|--------------------:|:----------------------------------------:|:-------------:|
|             Vanilla |                  3.8 ms                  |     100 %     |
|        With Lithium |                  3.6 ms                  |     94 %      |
|         With Sepals |                  2.2 ms                  |     57 %      |
| With Sepals+Lithium |                  2.2 ms                  |     57 %      |

### Frog attackable target filter

```
Rearrange the attackable conditions
let less costs predicate running first
reduce the probability of high-costs calculating

-- The complaints --
Mojang's attackable predicate is:

!entity.getBrain().hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN)
 && Sensor.testAttackableTargetPredicate(entity, target)
 && FrogEntity.isValidFrogFood(target)
 && !this.isTargetUnreachable(entity, target)
 && target.isInRange(entity, 10.0)

in this case, 'Sensor#testAttackableTargetPredicate' has calls 'TargetPredicate#test'
that cause a very lots raycast calculate when entities too much in the area
but... minecraft's raycast is absolutely bad, very slow

the 'TargetPredicate#test' in this case (800 frogs) has make 9.8ms costs in once game tick
among them, 'BlockView.raycast' contributed 7.3ms

then i make it be:

FrogEntity.isValidFrogFood(target) &&
 entity.getBrain().hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN) && 
 target.isInRange(entity, 10.0) && 
 Sensor.testAttackableTargetPredicate(entity, target) && 
 isTargetUnreachable(entity, target);
 
the 'isValidFrogFood' is simple conditions, check the entity's tag has in 'frog_food'
and a extra check when entity is slime then skip it when it size not 1

the 'isInRange' and 'hasMemoryModule' also simple, it only a few math calculates

-- Status --
Default enabled
```

800 frogs cramming in a 7x7 space:

|                                      Environment |  time  | Percent(Avg.) |
|-------------------------------------------------:|:------:|:-------------:|
|          Vanilla (FrogAttackablesSensor#matches) | 10 ms  |     100 %     |
|     With Lithium (FrogAttackablesSensor#matches) | 5.7 ms |     57 %      |
|         With Sepals (SepalsFrogBrain#attackable) | 0.1 ms |    0.01 %     |
| With Sepals+Lithium (SepalsFrogBrain#attackable) | 0.1 ms |    0.01 %     |

### Frog look-at target filter

```
Use 'SepalsLivingTargetCache' to improves target search performance
Required enable the SepalsLivingTargetCache to apply this feature, otherwise not difference to vanilla

-- Notice --
The raycast is in TargetPredicate test
at the 'findFirst' in LivingTargetCache when input predicate is success

but if subsequent conditions is failures, then to calculate this anymore is ueless
because even if the findFirst has found (raycast success)
but we don't used this result in subsequent contexts  

-- Status --
Default enabled
```

800 frogs cramming in a 7x7 space:

|                                                                          Environment | findFirst | Percent |
|-------------------------------------------------------------------------------------:|:---------:|:-------:|
|                         Vanilla <br /> (LookAtMobWithIntervalTask$$Lambda#findFirst) |  2.7 ms   |  100 %  |
|                    With Lithium <br /> (LookAtMobWithIntervalTask$$Lambda#findFirst) |  2.5 ms   |  92 %   |
|         With Sepals <br /> (SepalsLookAtMobWithIntervalTask$$Lambda#findFirstPlayer) |  0.1 ms   | 0.03 %  |
| With Sepals+Lithium <br /> (SepalsLookAtMobWithIntervalTask$$Lambda#findFirstPlayer) |  0.1 ms   | 0.03 %  |

### Villager miscellaneous optimizations

```
Listed what sepals to do here:

1. Used Catheter to replaced java stream
   The newest version Catheter has more quite stunning performance and scalability than Stream 
2. Cached tasks, activities, running tasks and memories to improves starting and updating task
3. Use sepals composite task to replaced vanilla composite task
4. Whenever possible, find opportunities to skips more raycast and useless predicates
5. Use 'SepalsLivingTargetCache' to replaced vanilla cache
   At the cost in sensors tick make less cost in finding interaction target or look at mob task
6. Rearranged predicates and extra lower cost predicate
   The purpose of this is do higher cost predicate later or best don't do that
   Skip the remaining high cost predicates in advance
7. Copied and modified 'SerializingRegionBasedStorage' optimizations from lithium
8. With more targeted task, don't use the generics to reduce useless operations
9. Used binary search list to replaced hashset search

-- Notice --
Recommend use this feature with lithium and c2me to make best performance

-- Warning --
Not long-term stability tested, only a month running shown it's ok currently
this feature does not be proved 100% vanilla
also it does not be proved has not vanilla in statistical significance 

-- Status --
Default enabled
```

800 villagers cramming in a 7x7 space at noon:

|       Environment       | Brain#tick (Total) | Percent | Brain#startTasks | Percent(startTasks) | Brain#tickSensors | Percent(tickSensors) | Brain#updateTasks | Percent(updateTasks) | Brain#tickMemories | Percent(tickMemories) |
|:-----------------------:|:------------------:|:-------:|:----------------:|:-------------------:|:-----------------:|:--------------------:|:-----------------:|:--------------------:|:------------------:|:---------------------:|
|         Vanilla         |       18 ms        |  100 %  |      9.3 ms      |        100 %        |      5.2 ms       |        100 %         |       3 ms        |        100 %         |       0.5 ms       |         100 %         |
|      With lithium       |      12.4 ms       |  35 %   |      4.8 ms      |        24 %         |      5.9 ms       |        113 %         |      1.2 ms       |         30 %         |       0.5 ms       |         112 %         |
|       With Sepals       |       9.7 ms       |  25 %   |      3.6 ms      |        16 %         |      3.7 ms       |        120 %         |       2 ms        |        0.04 %        |       0.4 ms       |         100 %         |
| With Sepals and lithium |       10 ms        |  22 %   |      3.4 ms      |        11 %         |      3.7 ms       |        116 %         |      2.5 ms       |        0.08 %        |       0.4 ms       |         75 %          |

800 villagers cramming in a 7x7 space at night:

|       Environment       | Brain#tick (Total) | Percent | Brain#startTasks | Percent(startTasks) | Brain#tickSensors | Percent(tickSensors) | Brain#updateTasks | Percent(updateTasks) | Brain#tickMemories | Percent(tickMemories) |
|:-----------------------:|:------------------:|:-------:|:----------------:|:-------------------:|:-----------------:|:--------------------:|:-----------------:|:--------------------:|:------------------:|:---------------------:|
|         Vanilla         |      16.7 ms       |  100 %  |      8.2 ms      |        100 %        |       6 ms        |        100 %         |       2 ms        |        100 %         |       0.5 ms       |         100 %         |
|      With lithium       |      10.2 ms       |  35 %   |      3.2 ms      |        24 %         |       6 ms        |        113 %         |      0.5 ms       |         30 %         |       0.5 ms       |         112 %         |
|       With Sepals       |        9 ms        |  25 %   |      3.3 ms      |        16 %         |      4.7 ms       |        120 %         |      0.7 ms       |        0.04 %        |       0.3 ms       |         100 %         |
| With Sepals and lithium |       8.7 ms       |  22 %   |      2.9 ms      |        11 %         |      4.6 ms       |        116 %         |      0.7 ms       |        0.08 %        |       0.5 ms       |         75 %          |
