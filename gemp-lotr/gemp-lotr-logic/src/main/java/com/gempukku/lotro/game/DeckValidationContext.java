package com.gempukku.lotro.game;

/**
 * Carries validation overrides for deck checking, typically derived from RTMD meta-site effects.
 * Null fields mean "use the format default."
 */
public class DeckValidationContext {
    private Integer maximumSameNameOverride;
    private Integer minimumDeckSizeOverride;
    private Boolean skipSiteBlockValidation;

    public DeckValidationContext() {}

    public Integer getMaximumSameNameOverride() { return maximumSameNameOverride; }

    public DeckValidationContext setMaximumSameNameOverride(Integer value) {
        this.maximumSameNameOverride = value;
        return this;
    }

    /**
     * Returns the effective maximum same-name limit, using the override if set,
     * otherwise falling back to the provided format default.
     */
    public int getMaximumSameName(int formatDefault) {
        return maximumSameNameOverride != null ? maximumSameNameOverride : formatDefault;
    }

    public Integer getMinimumDeckSizeOverride() { return minimumDeckSizeOverride; }

    public DeckValidationContext setMinimumDeckSizeOverride(Integer value) {
        this.minimumDeckSizeOverride = value;
        return this;
    }

    /**
     * Returns the effective minimum draw-deck size, using the override if set,
     * otherwise falling back to the provided format default.
     */
    public int getMinimumDeckSize(int formatDefault) {
        return minimumDeckSizeOverride != null ? minimumDeckSizeOverride : formatDefault;
    }

    public Boolean getSkipSiteBlockValidation() { return skipSiteBlockValidation; }

    public DeckValidationContext setSkipSiteBlockValidation(Boolean value) {
        this.skipSiteBlockValidation = value;
        return this;
    }

    public boolean shouldSkipSiteBlockValidation() {
        return skipSiteBlockValidation != null && skipSiteBlockValidation;
    }

    /**
     * Merges another context's overrides into this one.
     * Each override merges towards the value that is least likely to invalidate a deck a
     * single modifier already allows: the same-name cap keeps the highest (most permissive)
     * value, the minimum deck size keeps the highest (most restrictive) one, because a
     * "at least N cards" modifier is a floor that a second modifier can only raise.
     * For boolean flags, true overrides false.
     */
    public void merge(DeckValidationContext other) {
        if (other.maximumSameNameOverride != null) {
            if (this.maximumSameNameOverride == null || other.maximumSameNameOverride > this.maximumSameNameOverride) {
                this.maximumSameNameOverride = other.maximumSameNameOverride;
            }
        }
        if (other.minimumDeckSizeOverride != null) {
            if (this.minimumDeckSizeOverride == null || other.minimumDeckSizeOverride > this.minimumDeckSizeOverride) {
                this.minimumDeckSizeOverride = other.minimumDeckSizeOverride;
            }
        }
        if (other.skipSiteBlockValidation != null && other.skipSiteBlockValidation) {
            this.skipSiteBlockValidation = true;
        }
    }
}
