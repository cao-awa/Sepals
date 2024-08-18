package com.github.cao.awa.sepals.entity.task;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.catheter.Catheter;
import com.github.cao.awa.sepals.weight.SepalsWeighting;
import com.github.cao.awa.sepals.weight.WeightTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LongJumpTask;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.LongJumpUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SepalsLongJumpTask<E extends MobEntity> extends MultiTickTask<E> {
    private static final List<Integer> RAM_RANGES = Lists.newArrayList(65, 70, 75, 80);
    private final UniformIntProvider cooldownRange;
    protected final int verticalRange;
    protected final int horizontalRange;
    protected final float maxRange;
    protected Catheter<BlockPos> targets = null;
    protected Target[] precalculatedTargets = null;
    private int precalculatedRange = 0;
    private int precalculatingIndex = 0;
    protected Vec3d lastPos = null;
    protected boolean isNoRange = false;
    @Nullable
    protected Vec3d lastTarget;
    protected int cooldown;
    protected long targetTime;
    private final Function<E, SoundEvent> entityToSound;
    private final BiPredicate<E, BlockPos> jumpToPredicate;

    public SepalsLongJumpTask(UniformIntProvider cooldownRange, int verticalRange, int horizontalRange, float maxRange, Function<E, SoundEvent> entityToSound) {
        this(cooldownRange, verticalRange, horizontalRange, maxRange, entityToSound, LongJumpTask::shouldJumpTo);
    }

    public static <E extends MobEntity> boolean shouldJumpTo(E entity, BlockPos pos) {
        World world = entity.getWorld();
        BlockPos blockPos = pos.down();
        return world.getBlockState(blockPos).isOpaqueFullCube(world, blockPos)
                && entity.getPathfindingPenalty(LandPathNodeMaker.getLandNodeType(entity, pos)) == 0.0F;
    }

    public SepalsLongJumpTask(
            UniformIntProvider cooldownRange,
            int verticalRange,
            int horizontalRange,
            float maxRange,
            Function<E, SoundEvent> entityToSound,
            BiPredicate<E, BlockPos> jumpToPredicate
    ) {
        super(
                ImmutableMap.of(
                        MemoryModuleType.LOOK_TARGET,
                        MemoryModuleState.REGISTERED,
                        MemoryModuleType.LONG_JUMP_COOLING_DOWN,
                        MemoryModuleState.VALUE_ABSENT,
                        MemoryModuleType.LONG_JUMP_MID_JUMP,
                        MemoryModuleState.VALUE_ABSENT
                ),
                200
        );
        this.cooldownRange = cooldownRange;
        this.verticalRange = verticalRange;
        this.horizontalRange = horizontalRange;
        this.maxRange = maxRange;
        this.entityToSound = entityToSound;
        this.jumpToPredicate = jumpToPredicate;
    }

    protected boolean shouldRun(ServerWorld serverWorld, MobEntity mobEntity) {
        boolean bl = mobEntity.isOnGround()
                && !mobEntity.isTouchingWater()
                && !mobEntity.isInLava()
                && !serverWorld.getBlockState(mobEntity.getBlockPos()).isOf(Blocks.HONEY_BLOCK);
        if (!bl) {
            mobEntity.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, this.cooldownRange.get(serverWorld.random) / 2);
        }

        return bl;
    }

    protected boolean shouldKeepRunning(ServerWorld serverWorld, MobEntity mobEntity, long l) {
        boolean bl = this.lastPos != null
                && this.lastPos.equals(mobEntity.getPos())
                && this.cooldown > 0
                && !mobEntity.isInsideWaterOrBubbleColumn()
                && (this.lastTarget != null || this.targets.isPresent());
        if (!bl && mobEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
            mobEntity.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, this.cooldownRange.get(serverWorld.random) / 2);
            mobEntity.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        }

        return bl;
    }

    protected void run(ServerWorld serverWorld, E mobEntity, long l) {
        this.lastTarget = null;
        this.cooldown = 20;
        this.lastPos = mobEntity.getPos();
        BlockPos blockPos = mobEntity.getBlockPos();
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        this.precalculatedRange = 0;
        this.precalculatingIndex = 0;
        this.isNoRange = true;
        this.targets = Catheter.of(makeBlockPos(
                        i - this.horizontalRange,
                        j - this.verticalRange,
                        k - this.horizontalRange,
                        i + this.horizontalRange,
                        j + this.verticalRange,
                        k + this.horizontalRange
                ))
                .discard(blockPos::equals)
                .ifPresent(cather -> this.precalculatedTargets = new Target[cather.count()])
                .whenAlternate(0, (min, blockPos2) -> {
                    int weight = MathHelper.ceil(blockPos.getSquaredDistance(blockPos2));
                    int max = min + weight;
                    this.precalculatedTargets[this.precalculatingIndex++] = new Target(
                            blockPos2.toImmutable(),
                            weight,
                            min,
                            max
                    );
                    return max;
                }, precalculatedRange -> this.precalculatedRange = precalculatedRange);

        if (this.targets.isPresent()) {
            if (this.precalculatedRange / this.precalculatedTargets[0].weightValue() == this.precalculatedTargets.length) {
                this.isNoRange = true;
            }
        }
    }

    private static List<BlockPos> makeBlockPos(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        List<BlockPos> blockPosList = ApricotCollectionFactor.arrayList();
        BlockPos.iterate(startX, startY, startZ, endX, endY, endZ).forEach(blockPosList::add);
        return blockPosList;
    }

    protected void keepRunning(ServerWorld serverWorld, E mobEntity, long l) {
        if (this.lastTarget == null) {
            --this.cooldown;
            findTarget(serverWorld, mobEntity, l);
        } else {
            if (l - this.targetTime >= 40L) {
                mobEntity.setYaw(mobEntity.bodyYaw);
                mobEntity.setNoDrag(true);
                double d = this.lastTarget.length();
                double e = d + (double) mobEntity.getJumpBoostVelocityModifier();
                mobEntity.setVelocity(this.lastTarget.multiply(e / d));
                mobEntity.getBrain().remember(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
                serverWorld.playSoundFromEntity(null, mobEntity, this.entityToSound.apply(mobEntity), SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
        }
    }

    protected void findTarget(ServerWorld world, E entity, long time) {
        while (this.targets.isPresent()) {
            Target target = getTarget(world);

            BlockPos blockPos = target.getPos();
            if (canJumpTo(world, entity, blockPos)) {
                Vec3d vec3d2 = getJumpingVelocity(entity, Vec3d.ofCenter(blockPos));
                if (vec3d2 == null) {
                    continue;
                }

                entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(blockPos));
                Path path = entity.getNavigation().findPathTo(blockPos, 0, 8);
                if (path == null || !path.reachesTarget()) {
                    this.lastTarget = vec3d2;
                    this.targetTime = time;
                    return;
                }
            }
        }
    }

    protected Target getTarget(ServerWorld world) {
        if (this.targets.isEmpty()) {
            return null;
        }

        Target target;
        if (this.isNoRange) {
            target = this.precalculatedTargets[0];
        } else {
            target = SepalsWeighting.getRandom(world.random, this.precalculatedTargets, this.precalculatedRange);
        }

        if (target != null) {
            this.targets.remove(target.getPos());
        }
        return target;
    }

    private boolean canJumpTo(ServerWorld world, E entity, BlockPos pos) {
        BlockPos blockPos = entity.getBlockPos();
        int i = blockPos.getX();
        int j = blockPos.getZ();
        return (i != pos.getX() || j != pos.getZ()) && this.jumpToPredicate.test(entity, pos);
    }

    @Nullable
    protected Vec3d getJumpingVelocity(MobEntity entity, Vec3d targetPos) {
        List<Integer> list = Lists.newArrayList(RAM_RANGES);
        Collections.shuffle(list);
        float f = (float) (entity.getAttributeValue(EntityAttributes.GENERIC_JUMP_STRENGTH) * (double) this.maxRange);

        for (int i : list) {
            Optional<Vec3d> optional = LongJumpUtil.getJumpingVelocity(entity, targetPos, f, i, true);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        return null;
    }

    public static class Target extends Weighted.Absent implements WeightTable.Ranged<Target> {
        private final BlockPos pos;
        private final int weight;
        private final int min;
        private final int max;

        public Target(BlockPos pos, int weight, int min, int max) {
            super(weight);
            this.pos = pos;
            this.weight = weight;
            this.min = min;
            this.max = max;
        }

        public int weightValue() {
            return this.weight;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        @Override
        public int min() {
            return this.min;
        }

        @Override
        public int max() {
            return this.max;
        }

        @Override
        public Target element() {
            return this;
        }
    }
}
