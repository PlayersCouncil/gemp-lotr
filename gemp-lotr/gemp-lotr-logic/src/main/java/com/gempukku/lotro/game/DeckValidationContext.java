package com.gempukku.lotro.game;

/**
 * Carries validation overrides for deck checking, typically derived from RTMD meta-site effects.
 * Null fields mean "use the format default."
 */
public class DeckValidationContext {
    private Integer maximumSameNameOverride;
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
     * For numeric overrides, takes the maximum (most permissive).
     * For boolean flags, true overrides false.
     */
    public void merge(DeckValidationContext other) {
        if (other.maximumSameNameOverride != null) {
            if (this.maximumSameNameOverride == null || other.maximumSameNameOverride > this.maximumSameNameOverride) {
                this.maximumSameNameOverride = other.maximumSameNameOverride;
            }
        }
        if (other.skipSiteBlockValidation != null && other.skipSiteBlockValidation) {
            this.skipSiteBlockValidation = true;
        }
    }
}
