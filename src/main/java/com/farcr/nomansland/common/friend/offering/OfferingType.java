package com.farcr.nomansland.common.friend.offering;

public enum OfferingType {
    REGULAR(true),
    SPECIAL(true),
    MAP(true),
    BAD_OMEN(false);

    final boolean continueRegularInteraction;
    public boolean shouldContinueRegularInteraction() {
        return this.continueRegularInteraction;
    }
    OfferingType(final boolean continueRegularInteraction) {
        this.continueRegularInteraction = continueRegularInteraction;
    }
}
