package com.farcr.nomansland.common.entity.cervidae;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;

public interface IAntlers {

    int MIN_ANTLER_GROWTH_TIME = 72000;
    int MAX_ANTLER_GROWTH_TIME = 90000;

    boolean hasAntlers();

    void setHasAntlers(boolean hasAntlers);

    int getAntlerTimer();

    void setAntlerTimer(int antlerTimer);

    default void readAntlerData(CompoundTag compound) {
        setHasAntlers(compound.getBoolean("HasAntlers"));
        setAntlerTimer(compound.getInt("AntlerTimer"));
    }

    default void saveAntlerData(CompoundTag compound) {
        compound.putBoolean("HasAntlers", hasAntlers());
        compound.putInt("AntlerTimer", getAntlerTimer());
    }

    //TODO: Antler Growth SFX & Particles
    default void regrowLostAntlers(Mob mob) {
        int value = getAntlerTimer();
        if (value > 0) {
            value--;
            if (value == 0 && !hasAntlers()) {
                setHasAntlers(true);
            }
            setAntlerTimer(value);
        }
    }

    default boolean shouldShedAntlers() {
        return hasAntlers() && getAntlerTimer() == 0;
    }

    void onShedAntlers();

    default void addAntlersUponSpawning(RandomSource random) {
        setHasAntlers(true);
//        setAntlerTimer(10);
        setAntlerTimer(random.nextInt(0, MIN_ANTLER_GROWTH_TIME/2));
    }

    default void removeAntlers(RandomSource random) {
        setHasAntlers(false);
        setAntlerTimer(random.nextInt(MIN_ANTLER_GROWTH_TIME, MAX_ANTLER_GROWTH_TIME));
    }
}
