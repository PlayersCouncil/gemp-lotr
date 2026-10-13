package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;

/**
 * An effect that changes nothing in the game: it remembers something for a later effect, counts a use against a
 * limit, or writes a line in the log.  An action is no more worth taking for having one.
 */
public abstract class BookkeepingAppender extends DelayedAppender {
    @Override
    public final EffectForecast forecast(ActionContext actionContext) {
        return remembersSomething() ? EffectForecast.BOOKKEEPING : EffectForecast.COUNTING;
    }

    /**
     * Whether this effect fills a memory that a later effect may read.  False for one that only counts a use or
     * writes a line in the log (for the rule of Q-0224: see IdleOffers).
     */
    protected boolean remembersSomething() {
        return true;
    }
}
