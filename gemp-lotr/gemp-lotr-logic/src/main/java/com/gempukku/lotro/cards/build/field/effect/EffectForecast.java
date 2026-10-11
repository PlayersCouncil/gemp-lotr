package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.DelegateActionContext;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * What can be told of an effect before the action it belongs to is taken: whether it will do nothing, and, where
 * it gives something to cards in play, which cards those could be.  It is what the no-effect flag on an action is
 * judged from (see DefaultActionSource).
 *
 * A forecast is made while the action is only on offer: none of its costs has been paid and nothing has been
 * chosen.  It never works out an amount, since some amounts spend a limit as they are worked out.  Where an
 * effect cannot tell, it says UNKNOWN, and an action with an UNKNOWN part is not flagged for doing nothing.
 */
public final class EffectForecast {
    public enum Verdict {
        /** The effect will do nothing: there is nobody to give it to, or nothing for it to act on. */
        NOTHING,
        /** The effect will do nothing because what it gives is capped and the cap is spent. */
        AT_LIMIT,
        /** The effect may do something, or cannot tell. */
        UNKNOWN
    }

    /** Cannot tell, and cannot say whom it reaches: the answer of every effect that has not been taught better. */
    public static final EffectForecast UNKNOWN = new EffectForecast(Verdict.UNKNOWN, false, null, false);
    /** Will do nothing. */
    public static final EffectForecast NOTHING = new EffectForecast(Verdict.NOTHING, true, Collections.emptySet(), false);
    /**
     * Will do nothing because a draw deck has no card to give, or its player may draw none: a draw from an empty
     * draw deck, a reveal of its top cards.  It is flagged as any other effect that does nothing is.  It is told
     * apart for the rule of Q-0224 (IdleOffers): an optional draw is still offered with an empty
     * draw deck (Ketura on Q-0029), so an offer is never withheld on this ground.
     */
    public static final EffectForecast NOTHING_FROM_A_DRAW_DECK = new EffectForecast(Verdict.NOTHING, true, Collections.emptySet(), false, true);
    /** Will do nothing, a cap being spent. */
    public static final EffectForecast AT_LIMIT = new EffectForecast(Verdict.AT_LIMIT, true, Collections.emptySet(), false);
    /**
     * Does nothing to the game by nature (it remembers something, counts a use, writes a line in the log), so it
     * neither makes an action worth taking nor, when it is all an action has, shows that the action is idle.
     */
    public static final EffectForecast BOOKKEEPING = new EffectForecast(Verdict.NOTHING, true, Collections.emptySet(), true, false, true);
    /**
     * Bookkeeping that fills no memory: a use counted against a limit, a line in the log.  For the no-effect flag
     * it is bookkeeping like any other.  It is told apart for the rule of Q-0224 (IdleOffers).
     */
    public static final EffectForecast COUNTING = new EffectForecast(Verdict.NOTHING, true, Collections.emptySet(), true, false, false);

    private final Verdict verdict;
    private final boolean skirmishBound;
    private final Collection<PhysicalCard> reach;
    private final boolean bookkeeping;
    private final boolean restingOnADrawDeck;
    private final boolean remembering;

    private EffectForecast(Verdict verdict, boolean skirmishBound, Collection<PhysicalCard> reach, boolean bookkeeping) {
        this(verdict, skirmishBound, reach, bookkeeping, false);
    }

    private EffectForecast(Verdict verdict, boolean skirmishBound, Collection<PhysicalCard> reach, boolean bookkeeping,
            boolean restingOnADrawDeck) {
        this(verdict, skirmishBound, reach, bookkeeping, restingOnADrawDeck, false);
    }

    private EffectForecast(Verdict verdict, boolean skirmishBound, Collection<PhysicalCard> reach, boolean bookkeeping,
            boolean restingOnADrawDeck, boolean remembering) {
        this.verdict = verdict;
        this.skirmishBound = skirmishBound;
        this.reach = reach;
        this.bookkeeping = bookkeeping;
        this.restingOnADrawDeck = restingOnADrawDeck;
        this.remembering = remembering;
    }

    /**
     * @return True if this effect, or one it holds, fills a memory for a later effect to read (a "memorize").  An
     * effect that reads such a memory cannot be asked what it will do until the memory is filled.
     */
    public boolean isRemembering() {
        return remembering;
    }

    /**
     * @return True if this forecast of nothing, or any part of it, rests on a draw deck having no card to give
     * (NOTHING_FROM_A_DRAW_DECK).
     */
    public boolean isRestingOnADrawDeck() {
        return restingOnADrawDeck;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    /**
     * @return True if everything this effect may do matters only to a skirmish being fought as it is used and is
     * gone when that skirmish ends: strength, damage or protection from being overwhelmed, for the current phase.
     * Asked only of a forecast whose verdict is UNKNOWN.
     */
    public boolean isSkirmishBound() {
        return skirmishBound;
    }

    /**
     * @return The cards this effect could be given to, or null if that is not known.  A forecast of NOTHING
     * reaches no card.
     */
    public Collection<PhysicalCard> getReach() {
        return reach;
    }

    public boolean isBookkeeping() {
        return bookkeeping;
    }

    /**
     * The answer of a card resolver: these are the cards that could be chosen or would be taken, and what is
     * then done to them is for the effect that owns the resolver to say.
     */
    public static EffectForecast candidates(Collection<? extends PhysicalCard> cards) {
        return new EffectForecast(Verdict.UNKNOWN, false, Collections.unmodifiableSet(new LinkedHashSet<>(cards)), false);
    }

    /**
     * The forecast of an effect that is given to cards in play, from its card resolver's forecast: with nobody to
     * be given to it does nothing; otherwise it reaches the resolver's candidates.
     * @param candidates What the effect's card resolver forecast.
     * @param skirmishBound Whether what is given matters only to the skirmish being fought and ends with it.
     */
    public static EffectForecast givenTo(EffectForecast candidates, boolean skirmishBound) {
        if (candidates.reach == null)
            return UNKNOWN;
        return givenTo(candidates.reach, skirmishBound);
    }

    public static EffectForecast givenTo(Collection<? extends PhysicalCard> cards, boolean skirmishBound) {
        if (cards.isEmpty())
            return NOTHING;
        return new EffectForecast(Verdict.UNKNOWN, skirmishBound, Collections.unmodifiableSet(new LinkedHashSet<>(cards)), false);
    }

    /**
     * Whether an amount is capped and its cap is spent for the given card (see Evaluator.isSpent).  The amount
     * is not worked out.  An amount that cannot be asked, for want of something not yet chosen or remembered,
     * is not known to be spent.
     */
    public static boolean isSpent(ValueSource amount, ActionContext actionContext, PhysicalCard cardAffected) {
        try {
            return amount.getEvaluator(actionContext).isSpent(actionContext.getGame(), cardAffected);
        } catch (RuntimeException exp) {
            return false;
        }
    }

    /**
     * The forecast of several effects taken together, whether they all happen (the effects of an action, of a
     * "multiple") or only some of them will (the branches of an "if", the options of a "choice"): together they do
     * nothing only if each does nothing, and they reach every card that any of them may reach.  A part that does
     * nothing reaches nobody and takes no part in the rest.
     */
    public static EffectForecast together(List<EffectForecast> parts) {
        boolean nothing = true;
        boolean atLimit = false;
        boolean allBookkeeping = true;
        boolean skirmishBound = true;
        boolean reachKnown = true;
        boolean onADrawDeck = false;
        boolean remembering = false;
        Set<PhysicalCard> reach = new LinkedHashSet<>();
        for (EffectForecast part : parts) {
            if (!part.bookkeeping)
                allBookkeeping = false;
            if (part.restingOnADrawDeck)
                onADrawDeck = true;
            if (part.remembering)
                remembering = true;
            if (part.verdict == Verdict.UNKNOWN) {
                nothing = false;
                if (!part.skirmishBound)
                    skirmishBound = false;
                if (part.reach == null)
                    reachKnown = false;
                else
                    reach.addAll(part.reach);
            } else if (part.verdict == Verdict.AT_LIMIT) {
                atLimit = true;
            }
        }
        if (nothing) {
            if (allBookkeeping)
                return remembering ? BOOKKEEPING : COUNTING;
            if (atLimit)
                return AT_LIMIT;
            if (remembering)
                return new EffectForecast(Verdict.NOTHING, true, Collections.emptySet(), false, onADrawDeck, true);
            return onADrawDeck ? NOTHING_FROM_A_DRAW_DECK : NOTHING;
        }
        return new EffectForecast(Verdict.UNKNOWN, skirmishBound, reachKnown ? Collections.unmodifiableSet(reach) : null, false);
    }

    public static EffectForecast together(EffectForecast... parts) {
        return together(Arrays.asList(parts));
    }

    /**
     * Whether the given effects, about to be carried out one after another, are certain to do nothing: nothing to
     * draw, nobody to give it to, nothing left for it to act on.  Unlike a forecast made for the no-effect flag,
     * this is asked at the moment the effects come up, so they are asked in a context that says so
     * (ActionContext.isAtResolution) and may read what has been chosen and remembered.  It is what a prevention
     * asks before it is put to a player (PreventableEffectAppender): nobody is asked to pay to prevent what is
     * not going to happen.  A choice asks it of its branches when none of them can be carried out in full
     * (Choice): nobody is asked to pick between things that will not happen.
     *
     * Each effect has to say for itself that it will do nothing.  An effect that cannot tell, and one that only
     * remembers something for the next (whose forecast is then made before that memory is filled), leave the
     * answer at false.  An effect that will happen in part (three cards to draw and one in the deck) is not
     * doing nothing.
     */
    public static boolean isCertainToDoNothing(ActionContext actionContext, EffectAppender[] effectAppenders) {
        if (effectAppenders == null || effectAppenders.length == 0)
            return false;
        final ActionContext atResolution = new DelegateActionContext(actionContext, actionContext.getPerformingPlayer()) {
            @Override
            public boolean isAtResolution() {
                return true;
            }
        };
        try {
            for (EffectAppender effectAppender : effectAppenders) {
                final EffectForecast forecast = effectAppender.forecast(atResolution);
                if (forecast.verdict == Verdict.UNKNOWN || forecast.bookkeeping)
                    return false;
            }
        } catch (RuntimeException exp) {
            return false;
        }
        return true;
    }

    /**
     * The forecast, made only at resolution (ActionContext.isAtResolution), of an effect that acts on the cards
     * its card resolver finds and does nothing else: with none found it does nothing.  While the action is only
     * on offer it says UNKNOWN, as such an effect did before it was asked at resolution: the no-effect flag is
     * not judged from it.
     */
    public static EffectForecast foundAtResolution(ActionContext actionContext, EffectAppender cardResolver) {
        if (!actionContext.isAtResolution())
            return UNKNOWN;
        return givenTo(cardResolver.forecast(actionContext), false);
    }

    /**
     * @return The forecast of the given effects taken together, each asked in the given context.
     */
    public static EffectForecast together(ActionContext actionContext, EffectAppender[]... effectAppenders) {
        List<EffectForecast> parts = new java.util.ArrayList<>();
        for (EffectAppender[] appenders : effectAppenders) {
            if (appenders == null)
                continue;
            for (EffectAppender appender : appenders)
                parts.add(appender.forecast(actionContext));
        }
        return together(parts);
    }
}
