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
        return EffectForecast.BOOKKEEPING;
    }
}
