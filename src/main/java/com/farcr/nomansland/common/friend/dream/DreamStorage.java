package com.farcr.nomansland.common.friend.dream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/*
* Generalized Class to allow for mutation & other things
* also for extensibility, since "has experienced dream" will probably
* turn into a list of dreams the player has experienced and information
* associated with them if necessary !!!
 */
public class DreamStorage {
    public static DreamStorage fromCodec(boolean experiencedDream) {
        DreamStorage dreamInfo = new DreamStorage();
        if (experiencedDream)
            dreamInfo.setDreamExperienced();
        return dreamInfo;
    }

    public static final Codec<DreamStorage> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.fieldOf("hasExperiencedDream").forGetter(DreamStorage::getHasExperiencedDream)
        ).apply(instance, DreamStorage::fromCodec)
    );

    private boolean hasExperiencedDream = false;
    public boolean getHasExperiencedDream() { return hasExperiencedDream; }
    public void setDreamExperienced() {
        this.hasExperiencedDream = true;
    }
}
