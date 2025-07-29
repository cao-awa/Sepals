package com.github.cao.awa.sepals.explosion;

import com.github.cao.awa.sepals.threadpool.SepalsThreadPool;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SepalsExplosionStorage {
    public static final SepalsExplosionStorage INSTANCE = new SepalsExplosionStorage();
    private List<ExplosionImpl> explosions = CollectionFactor.arrayList();

    public void add(ExplosionImpl explosion) {
        this.explosions.add(explosion);
    }

    public void doExplosion() throws ExecutionException, InterruptedException {
        if (this.explosions.size() < 50) {
            allExplosion(this.explosions);
        } else {
            List<ExplosionImpl> group1 = CollectionFactor.arrayList();
//            List<ExplosionImpl> group2 = CollectionFactor.arrayList();

            CompletableFuture.runAsync(() -> {
                allExplosion(group1);
            }).thenRunAsync(() -> {
//                allExplosion(group2);
            }).join();
        }

        this.explosions.clear();
    }

    public void allExplosion(List<ExplosionImpl> explosions) {
            for (ExplosionImpl explosion : explosions) {
                explosion.explode();
            }
    }
}
