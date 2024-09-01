# Sepals

An extremely radical and experimental optimization for Minecraft server performances.

## Compatibility

Currently, sepals are compatible to almost all mods.

## Performance

A test was done in cao-awa personal computer, CPU: 5950x; Memory: 128G DDR4-3200MHz; OS: Windows 11; Minecraft: 1.21.

### Entities cramming

```
Use index to access element to replaced iterator

-- Status --
Default disabled, no recommend to use now
```

496 large slime cramming in a 20x20 space:

|               Environment               | tickCramming | Percent |
|:---------------------------------------:|:------------:|:-------:|
|                 Vanilla                 |   45.3 ms    |  100 %  |
|               With Sepals               |    44 ms     |  97 %   |
|              With Lithium               |   43.1 ms    |  95 %   |
| With Sepals<br/> and configured Lithium |   40.8 ms    |  90 %   |

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
```

900 frogs cramming in a 3x3 space:

| Environment | Weighting#getRandom | Percent(Avg.) |
|:-----------:|:-------------------:|:-------------:|
|   Vanilla   |       8.4 ms        |     100 %     |
| With Sepals |       9.2 ms        |     109 %     |

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

1366 frogs cramming in a 3x3 space:

|                     Environment                     | keepRunning | Percent(Avg.) |
|:---------------------------------------------------:|:-----------:|:-------------:|
|      Vanilla <br /> (LongJumpTask#keepRunning)      |   15.6 ms   |     100 %     |
| With Sepals <br /> (SepalsLongJumpTask#keepRunning) |   0.3 ms    |    0.02 %     |

|                     Environment                     | getTarget | Percent(Avg.) | Percent(in ```keepRunning```) |
|:---------------------------------------------------:|:---------:|:-------------:|:-----------------------------:|
|      Vanilla <br /> (LongJumpTask#keepRunning)      |  13.4 ms  |     100 %     |             85 %              |
| With Sepals <br /> (SepalsLongJumpTask#keepRunning) |  0.02 ms  |    0.001 %    |            0.06 %             |

### Quick sort in NearestLivingEntitiesSensor

```
Quick sort supports by fastutil to replace java tim sort

-- Status --
Default enabled
```

900 frogs cramming in a 3x3 space:

| Environment | sort (NearestLivingEntitiesSensor#sense) | Percent(Avg.) |
|------------:|:----------------------------------------:|:-------------:|
|     Vanilla |                  6.5 ms                  |     100 %     |
| With Sepals |                   3 ms                   |     46 %      |

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

the 'TargetPredicate#test' in this case (1366 frogs) has make 44.3ms costs in once game tick
among them, 'BlockView.raycast' contributed 39.7ms

then i make it be:

FrogEntity.isValidFrogFood(target) && 
 target.isInRange(entity, 10.0) && 
 entity.getBrain().hasMemoryModule(MemoryModuleType.HAS
 isTargetUnreachable(entity, target) && 
 Sensor.testAttackableTargetPredicate(entity, target);
 
the 'isValidFrogFood' is simple conditions, check the entity's tag has in 'frog_food'
and a extra check when entity is slime then skip it when it size not 1

the 'isInRange' also simple, it only a few math calculates

the 'hasMemoryModule' simple but it still can be said that high-cost
this method getting element from map, but the key type 'MemoryModuleType' are not hash-able or comparable
it cause getting in this 1366 frogs case make 5ms costs in once game tick

-- Status --
Default enabled
```

1366 frogs cramming in a 3x3 space:

| Environment | sort (NearestLivingEntitiesSensor#sense) | Percent(Avg.) |
|------------:|:----------------------------------------:|:-------------:|
|     Vanilla |                  6.5 ms                  |     100 %     |
| With Sepals |                   3 ms                   |     46 %      |

### Frog look-at target filter

```
Use 'SepalsLivingTargetCache' to improves target search performance
Required enable the SepalsLivingTargetCache to apply this feature, otherwise not difference to vanilla

-- Notice --
The raycast is in TargetPredicate test
at the findFirst in LivingTargetCache when input predicate is success

but if subsequent conditions is failures, then to calculate this anymore is ueless
because even if the findFirst has found (raycast success)
but we don't used this result in subsequent contexts  

-- Status --
Default enabled
```

1366 frogs cramming in a 3x3 space:

|                                                                  Environment | findFirst | Percent | The ```raycast``` time | The ```raycast``` percent |
|-----------------------------------------------------------------------------:|:---------:|:-------:|:----------------------:|:-------------------------:|
|                 Vanilla <br /> (LookAtMobWithIntervalTask$$Lambda#findFirst) |  26.5 ms  |  100 %  |        25.1 ms         |           94 %            |
| With Sepals <br /> (SepalsLookAtMobWithIntervalTask$$Lambda#findFirstPlayer) |  3.4 ms   |  13 %   |         3.3 ms         |           97 %            |

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
Not long-term stability tested
this feature does not be proved 100% vanilla
also it does not be proved has not vanilla in statistical significance 

-- Status --
Default enabled
```

591 villagers cramming in a 1x1 space:

|               Environment                | Brain#tick (Total) | Percent | Brain#startTasks | Percent(startTasks) | Brain#tickSensors | Percent(tickSensors) | Brain#updateTasks | Percent(updateTasks) | Brain#tickMemories | Percent(tickMemories) |
|:----------------------------------------:|:------------------:|:-------:|:----------------:|:-------------------:|:-----------------:|:--------------------:|:-----------------:|:--------------------:|:------------------:|:---------------------:|
|  Vanilla <br /> (No any sepals feature)  |       33 ms        |  100 %  |      22 ms       |        100 %        |       3 ms        |        100 %         |       7 ms        |        100 %         |       0.8 ms       |         100 %         |
|   Vanilla <br /> (No sepals villager)    |      32.8 ms       |  99 %   |     22.3 ms      |        101 %        |      3.3 ms       |        110 %         |      6.1 ms       |         87 %         |       1.1 ms       |         137 %         |
| With lithium <br /> (No sepals villager) |      11.7 ms       |  35 %   |      5.4 ms      |        24 %         |      3.4 ms       |        113 %         |      2.1 ms       |         30 %         |       0.9 ms       |         112 %         |
|               With Sepals                |       8.3 ms       |  25 %   |      3.7 ms      |        16 %         |      3.6 ms       |        120 %         |      0.3 ms       |        0.04 %        |       0.8 ms       |         100 %         |
|         With Sepals and lithium          |       7.3 ms       |  22 %   |      2.6 ms      |        11 %         |      3.5 ms       |        116 %         |      0.6 ms       |        0.08 %        |       0.6 ms       |         75 %          |
