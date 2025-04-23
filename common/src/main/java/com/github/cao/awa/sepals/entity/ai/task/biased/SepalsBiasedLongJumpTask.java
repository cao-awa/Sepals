package com.github.cao.awa.sepals.entity.ai.task.biased;

import com.github.cao.awa.sepals.entity.ai.task.SepalsLongJumpTask;
import net.minecraft.block.Block;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class SepalsBiasedLongJumpTask<E extends MobEntity> extends SepalsLongJumpTask<E> {
    private final TagKey<Block> favoredBlocks;
    private final float biasChance;
    private final List<Target> unfavoredTargets = new ArrayList<>();
    private boolean useBias;

    public SepalsBiasedLongJumpTask(
            UniformIntProvider cooldownRange,
            int verticalRange,
            int horizontalRange,
            float maxRange,
            Function<E, SoundEvent> entityToSound,
            TagKey<Block> favoredBlocks,
            float biasChance,
            BiPredicate<E, BlockPos> jumpToPredicate
    ) {
        super(cooldownRange, verticalRange, horizontalRange, maxRange, entityToSound, jumpToPredicate);
        this.favoredBlocks = favoredBlocks;
        this.biasChance = biasChance;
    }

    @Override
    protected void run(ServerWorld serverWorld, E mobEntity, long l) {
        super.run(serverWorld, mobEntity, l);
        this.unfavoredTargets.clear();
        this.useBias = mobEntity.getRandom().nextFloat() < this.biasChance;
    }

    @Override
    protected Target getTarget(ServerWorld world) {
        if (this.useBias) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            while (this.targets.isPresent()) {
                Target target = super.getTarget(world);

                if (addUnfavored(world, mutable, target)) {
                    return target;
                }
            }

            return this.unfavoredTargets.isEmpty() ? null : this.unfavoredTargets.removeFirst();
        } else {
            return super.getTarget(world);
        }
    }

    private boolean addUnfavored(ServerWorld world, BlockPos.Mutable mutable, Target target) {
        if (world.getBlockState(mutable.set(target.pos(), Direction.DOWN)).isIn(this.favoredBlocks)) {
            return true;
        }

        this.unfavoredTargets.add(target);
        return false;
    }
}
