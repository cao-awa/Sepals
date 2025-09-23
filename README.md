# Sepals

An extremely radical and experimental optimization for Minecraft server performances.

We recommended using Sepals with [lithium](https://modrinth.com/mod/lithium) to make the best performances.

![](https://count.getloli.com/@@cao-awa.sepals?name=%40cao-awa.sepals&theme=rule34&padding=7&offset=0&align=top&scale=1&pixelated=1&darkmode=auto)

## Compatibility

Currently, sepals are compatible with almost all mods (~~if no new issue opened~~).

Here is the verified mod list with the latest Sepals version:

|                                            Target mod | Required target version | 
|------------------------------------------------------:|:-----------------------:|
|             [sodium](https://modrinth.com/mod/sodium) |           all           |  
|                 [iris](https://modrinth.com/mod/iris) |           all           |   
|  [ferritecore](https://modrinth.com/mod/ferrite-core) |           all           |    
|           [krypton](https://modrinth.com/mod/krypton) |           all           | 
|           [lithium](https://modrinth.com/mod/lithium) |        >=0.18.0         |   
|   [c2me-fabric](https://modrinth.com/mod/c2me-fabric) |      \>=0.3.4.0.0       |        
| [moonrise-opt](https://modrinth.com/mod/moonrise-opt) | \>=0.6.0-beta.1+45edfd7 |    
|               [async](https://modrinth.com/mod/async) | \>=0.1.7+alpha.7-1.21.8 | 

If you don't know which version to choose, please use the latest version of all the optimization mods, Sepals will
ensure compatibility with them.

### Specially

When Sepals work with mod [Async](https://modrinth.com/mod/async)
testing on different platforms has different results, please use configurations to disable Sepals features or Async
features to test, find which is more worthwhile if you want to use both together.

Sepals feature related: ```enableSepalsEntitiesCramming```.

## Config

|                 Config name                 | Allowed value | Default value |
|:-------------------------------------------:|:-------------:|:-------------:|
|            forceEnableSepalsPoi             |  bool value   |     false     |    
|            enableSepalsVillager             |  bool value   |     true      |    
|           enableSepalsFrogLookAt            |  bool value   |     true      |    
|      enableSepalsFrogAttackableSensor       |  bool value   |     true      |    
|        enableSepalsLivingTargetCache        |  bool value   |     true      |    
|   nearestLivingEntitiesSensorUseQuickSort   |  bool value   |     true      |    
|       enableSepalsBiasedLongJumpTask        |  bool value   |     true      |    
|        enableSepalsEntitiesCramming         |  bool value   |     true      |    
|            enableSepalsItemMerge            |  bool value   |     true      |
| enableSepalsQuickCanBePushByEntityPredicate |  bool value   |     true      |

## Performance

A test was done in server of Mars provided by feimia, CPU: Intel i7-14700K; Game memory: 4G; OS: Ubuntu 24.04.1 LTS;
Minecraft: 1.21.

### Entities cramming

```
Use box cache to prevent too much 'getOtherEntities' calls

-- Notice --
This feature was ignored scoreboard/team predicates for every sigle entity
may cause unexpected behaviors

This problem will not affects anything in vanilla(survival and creative)
it only things to consider when game servers that using command blocks    

-- Status --
Default enabled
```

1390 villagers cramming in a 7x7 space:

|         Environment | tickCramming | Percent(Avg.) |
|--------------------:|:------------:|:-------------:|
|             Vanilla |   53.6 ms    |     100 %     |
|        With Lithium |   54.4 ms    |     101 %     |
|         With Sepals |   10.2 ms    |     19 %      |
| With Sepals+Lithium |    8.5 ms    |     15 %      |

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

800 frogs cramming in a 7x7 space:

|                   Environment                   | keepRunning | Percent(Avg.) |
|:-----------------------------------------------:|:-----------:|:-------------:|
|          Vanilla <br /> (LongJumpTask)          |   43.1 ms   |     100 %     |
|       With Lithium <br /> (LongJumpTask)        |   7.5 ms    |     17 %      |
|     With Sepals <br /> (SepalsLongJumpTask)     |   0.2 ms    |     0.4 %     |
| With Sepals+Lithium <br /> (SepalsLongJumpTask) |   0.05 ms   |     0.1 %     |

|                   Environment                   | getTarget | Percent(Avg.) | Percent(in ```keepRunning```) |
|:-----------------------------------------------:|:---------:|:-------------:|:-----------------------------:|
|          Vanilla <br /> (LongJumpTask)          |  43.1 ms  |     100 %     |             100 %             |
|       With Lithium <br /> (LongJumpTask)        |  3.6 ms   |      9 %      |             48 %              |
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
|         With Sepals (SepalsFrogBrain#attackable) | 0.1 ms |      1 %      |
| With Sepals+Lithium (SepalsFrogBrain#attackable) | 0.1 ms |      1 %      |

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
|         With Sepals <br /> (SepalsLookAtMobWithIntervalTask$$Lambda#findFirstPlayer) |  0.1 ms   |   3 %   |
| With Sepals+Lithium <br /> (SepalsLookAtMobWithIntervalTask$$Lambda#findFirstPlayer) |  0.1 ms   |   3 %   |

### Villager miscellaneous optimizations

```
Listed what sepals to do here:

1. Used Catheter to replaced java stream, The newest version Catheter has more quite stunning performance and scalability than Stream 
2. Cached tasks, activities, running tasks and memories to improves starting and updating task
3. Use sepals composite task to replaced vanilla composite task
4. Whenever possible, find opportunities to skips more raycast and useless predicates
5. Use 'SepalsLivingTargetCache' to replaced vanilla cache, At the cost in sensors tick make less cost in finding interaction target or look at mob task
6. Rearranged predicates and extra lower cost predicate, The purpose of this is do higher cost predicate later or best don't do that, Skip the remaining high cost predicates in advance
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
|      With lithium       |      12.4 ms       |  68 %   |      4.8 ms      |        51 %         |      5.9 ms       |        113 %         |      1.2 ms       |         40 %         |       0.5 ms       |         100 %         |
|       With Sepals       |       9.7 ms       |  53 %   |      3.6 ms      |        38 %         |      3.7 ms       |         71 %         |       2 ms        |         66 %         |       0.4 ms       |         80 %          |
| With Sepals and lithium |       10 ms        |  55 %   |      3.4 ms      |        36 %         |      3.7 ms       |         71 %         |      2.5 ms       |         83 %         |       0.4 ms       |         80 %          |

800 villagers cramming in a 7x7 space at night:

|       Environment       | Brain#tick (Total) | Percent | Brain#startTasks | Percent(startTasks) | Brain#tickSensors | Percent(tickSensors) | Brain#updateTasks | Percent(updateTasks) | Brain#tickMemories | Percent(tickMemories) |
|:-----------------------:|:------------------:|:-------:|:----------------:|:-------------------:|:-----------------:|:--------------------:|:-----------------:|:--------------------:|:------------------:|:---------------------:|
|         Vanilla         |      16.7 ms       |  100 %  |      8.2 ms      |        100 %        |       6 ms        |        100 %         |       2 ms        |        100 %         |       0.5 ms       |         100 %         |
|      With lithium       |      10.2 ms       |  61 %   |      3.2 ms      |        24 %         |       6 ms        |        113 %         |      0.5 ms       |         25 %         |       0.5 ms       |         100 %         |
|       With Sepals       |        9 ms        |  53 %   |      3.3 ms      |        16 %         |      4.7 ms       |         78 %         |      0.7 ms       |         35 %         |       0.3 ms       |         60 %          |
| With Sepals and lithium |       8.7 ms       |  52 %   |      2.9 ms      |        11 %         |      4.6 ms       |         76 %         |      0.7 ms       |         35 %         |       0.5 ms       |         100 %         |

### Predicate optimization

1172 frogs cramming in a 3x3 space:

|                                                                                                       Environment |   time   | Percent(Avg.) |
|------------------------------------------------------------------------------------------------------------------:|:--------:|:-------------:|
|                                                           Vanilla (java.util.function.Predicate.lambda\$and\$0()) | 49.01 ms |     100 %     |
| With Sepals (com.github.cao.awa.sepals.entity.predicate.SepalsEntityPredicates$$Lambda/0x000002d8f116e000.test()) | 22.6 ms  |     46 %      |
