package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.DelegateActionContext;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Set;

/**
 * Ketura's ruling Q-0034 carried out by the engine (Q-0224: A2, with Q-0231: B).
 *
 * The ruling: an optional trigger, or an ability, with NO COST whose whole effect would do nothing is not offered;
 * an event, and anything with a cost, is offered whatever its effect will do.  The game withholds the offer itself,
 * from what its effects forecast of themselves (EffectForecast, which is what the no-effect flag is judged from)
 * and from what some kinds of effect were taught to say to this rule alone (TaughtForecast).  A kind of effect
 * that cannot say it will do nothing keeps its offer: a card with such an offer still wants a requirement in its
 * definition.
 *
 * What is withheld (see DefaultActionSource.isValid and AddTrigger):
 * <ul>
 * <li>an optional trigger of a card (a "Trigger" with optional: true, a killed or removed-from-play trigger with
 * optional: true, an optional trigger added for a time by "AddTrigger");</li>
 * <li>a special ability of a card in play ("Activated"), a Response ability of a card in play ("Response"), and an
 * ability one card gives another ("AddActivated");</li>
 * </ul>
 * and only where it PAYS NOTHING (paysSomething) and every one of its effects forecasts NOTHING.
 *
 * What is never withheld: the play of an event, a play from hand, an ability used from a discard pile, a draw deck
 * or a stack; anything with a cost; a "you may" inside another action (the effect "Optional": Q-0224, A2); an
 * action flagged for another reason (a limit that is spent, a skirmish action that reaches nobody in the skirmish);
 * an action whose definition names its own "noEffectUnless"; and an effect that does nothing only because a draw
 * deck is empty (an optional draw is still offered then: Q-0029).
 *
 * Four of the kinds taught are exercised by no test (engine pass 27; Ketura, 2026-10-11: no card is invented for
 * a test).  Every printed card that has one of them in an offer that pays nothing carries a requirement written by
 * hand, which answers before this rule is asked, so no printed card shows what the effect says of itself: an ally
 * made to take part in skirmishes (EnableParticipationInSkirmishes: Dunlending Warrior 4_18, Dunlending Ravager
 * 4_15), a restore (RestoreCardsInPlay: Desert Wind Scout 103_48), a card chosen from a discard pile
 * (ChooseCardsFromDiscard: Greatest Kingdom of My People 51_16) and a repeated effect (DoWhile: Race Text 94_4).
 * One card has the first of them with no such requirement, Crown of Erebor 32_1 of the Hobbit sets, which have no
 * recorded game text to test against and are worked last.  The teaching of the four stays; the other kinds have
 * their tests on printed cards (mechanics/AnOfferThatCostsNothingAndWouldDoNothingIsNotMadeTests).
 *
 * The switch (the system property gemp.idleOffers, or the environment variable GEMP_IDLE_OFFERS) is kept for
 * tests and for measuring; the game runs in the mode TAUGHT.
 */
public final class IdleOffers {
    private IdleOffers() {
    }

    public enum Mode {
        /** The rule is off: the game offers what it offered before Q-0224. */
        OFF,
        /**
         * An offer is withheld where the forecast the no-effect flag is judged from says NOTHING: the forecast
         * made while the action is only on offer, in which an effect looks at cards in play and at nothing else.
         */
        FLAG,
        /**
         * As FLAG, and the effects are then asked again as they are when they are about to be carried out
         * (ActionContext.isAtResolution), which is how a prevention and a choice already ask them: with nothing
         * to pay, the board they are offered on is the board they will find.  An effect may then read what the
         * trigger remembered, look through a discard pile, a hand or a draw deck, and say that a site cannot be
         * taken or a threat cannot be added.  Left out where a cost that pays nothing will remember something
         * for the effects (areIdle).  No effect was taught anything for this: it is what each already answers.
         */
        RESOLUTION,
        /**
         * As RESOLUTION, with the effects that were taught something for this rule answering what they were
         * taught (TaughtForecast): a reinforcement with no token to reinforce, a transfer or an assignment
         * with nobody to go to, a stacking with nothing to stack, a Ring already worn, a choice with nothing to
         * choose from, a random discard from a hand that is empty or that the card may not discard from.  Each
         * answers so only to this rule; the no-effect flag is judged as it was.  This is the default, and the
         * mode the game is played in.
         */
        TAUGHT
    }

    private static final ThreadLocal<Boolean> ASKING_TAUGHT = ThreadLocal.withInitial(() -> false);

    /** Whether the rule is asking, in the mode TAUGHT: an effect that was taught something for the rule says it. */
    public static boolean isAskingTaught() {
        return ASKING_TAUGHT.get();
    }

    private static volatile Mode mode = readMode();

    private static Mode readMode() {
        String word = System.getProperty("gemp.idleOffers");
        if (word == null)
            word = System.getenv("GEMP_IDLE_OFFERS");
        if (word == null)
            return Mode.TAUGHT;
        try {
            return Mode.valueOf(word.trim().toUpperCase());
        } catch (IllegalArgumentException exp) {
            return Mode.TAUGHT;
        }
    }

    public static Mode getMode() {
        return mode;
    }

    /** For a test or a probe that reads an offer with the rule off and on at one table. */
    public static void setMode(Mode newMode) {
        mode = newMode;
    }

    // Costs that pay nothing: a spot (which only looks), a choice or a number that is only remembered, a line in
    // the log.  "Spot X to <effect>" is no cost for Q-0034 (shapes.md, F-0167).
    private static final Set<String> PAYS_NOTHING = Set.of(
            "spot", "chooseactivecards", "chooseanumber", "choosehowmanytospot", "choosehowmanyburdenstospot",
            "choosehowmanythreatstospot", "choosehowmanytwilighttokenstospot", "choosehowmanyfpculturestospot",
            "memorize", "memorizeactive", "memorizenumber", "memorizeinfo", "memorizetitle", "memorizestacked",
            "memorizediscard", "memorizetopofdeck", "chooseyesorno", "sendmessage");

    /**
     * Whether an action, as its definition writes it, has a cost for the purposes of Q-0034: anything under its
     * "cost" that is not of a kind that pays nothing, and any cost written inside one of its effects ("you may
     * exert X to ...", a cost-to-effect, a choice between two of them).  The price another player pays to prevent
     * an effect ("Preventable") is no cost of the action's.  A limit on uses is no cost.
     */
    public static boolean paysSomething(JSONObject[] costs, JSONObject[] effects) {
        for (JSONObject cost : costs) {
            if (!PAYS_NOTHING.contains(String.valueOf(cost.get("type")).toLowerCase()))
                return true;
        }
        for (JSONObject effect : effects) {
            if (holdsACost(effect))
                return true;
        }
        return false;
    }

    private static boolean holdsACost(Object definition) {
        if (definition instanceof JSONObject object) {
            final boolean prevention = String.valueOf(object.get("type")).equalsIgnoreCase("preventable");
            for (Object entryObject : object.entrySet()) {
                final java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry<?, ?>) entryObject;
                final String key = String.valueOf(entry.getKey());
                if (key.equalsIgnoreCase("cost")) {
                    if (prevention)
                        continue;
                    if (holdsARealCost(entry.getValue()))
                        return true;
                } else if (holdsACost(entry.getValue())) {
                    return true;
                }
            }
        } else if (definition instanceof Collection<?> collection) {
            for (Object element : collection) {
                if (holdsACost(element))
                    return true;
            }
        } else if (definition instanceof Object[] array) {
            for (Object element : array) {
                if (holdsACost(element))
                    return true;
            }
        }
        return false;
    }

    private static boolean holdsARealCost(Object costs) {
        if (costs instanceof JSONObject object)
            return !PAYS_NOTHING.contains(String.valueOf(object.get("type")).toLowerCase());
        if (costs instanceof Collection<?> collection) {
            for (Object element : collection) {
                if (holdsARealCost(element))
                    return true;
            }
        } else if (costs instanceof Object[] array) {
            for (Object element : array) {
                if (holdsARealCost(element))
                    return true;
            }
        }
        return false;
    }

    /**
     * Whether any of the given costs, which pay nothing, remembers something for the effects ("spot a companion
     * to heal that companion" remembers the companion): a key that begins with "memor", anywhere inside.
     */
    public static boolean remember(JSONObject[] costs) {
        for (JSONObject cost : costs) {
            if (remembers(cost))
                return true;
        }
        return false;
    }

    private static boolean remembers(Object definition) {
        if (definition instanceof JSONObject object) {
            for (Object entryObject : object.entrySet()) {
                final java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry<?, ?>) entryObject;
                if (String.valueOf(entry.getKey()).toLowerCase().startsWith("memor") || remembers(entry.getValue()))
                    return true;
            }
        } else if (definition instanceof Collection<?> collection) {
            for (Object element : collection) {
                if (remembers(element))
                    return true;
            }
        } else if (definition instanceof Object[] array) {
            for (Object element : array) {
                if (remembers(element))
                    return true;
            }
        }
        return false;
    }

    /**
     * Whether the given effects, which nothing is paid for, are certain to do nothing on the board as it stands.
     * They are asked twice.  First as they are for the no-effect flag, while the action is only on offer: an
     * effect then looks at cards in play and at nothing else, and one that reads memory says it cannot tell.
     * Then (in the mode RESOLUTION) as they are asked when they are about to be carried out, which with nothing
     * to pay is the same board: an effect may then read memory and look through any zone.
     * @param memoryStillToBeFilled True where something that pays nothing stands before the effects and will
     *                              remember a card or a number for them ("spot a companion to heal that
     *                              companion"): an effect asked now as if it were about to be carried out would
     *                              find that memory empty, so the second asking is left out.  False where
     *                              nothing that remembers stands before them.
     */
    public static boolean areIdle(ActionContext actionContext, Iterable<? extends EffectAppender> effects,
            boolean memoryStillToBeFilled) {
        final Mode asked = mode;
        if (asked == Mode.OFF)
            return false;
        final boolean before = ASKING_TAUGHT.get();
        ASKING_TAUGHT.set(asked == Mode.TAUGHT);
        try {
            if (areIdle(actionContext, effects))
                return true;
            if (asked == Mode.FLAG || memoryStillToBeFilled)
                return false;
            return areIdle(asAtResolution(actionContext), effects);
        } finally {
            ASKING_TAUGHT.set(before);
        }
    }

    private static ActionContext asAtResolution(ActionContext actionContext) {
        return new DelegateActionContext(actionContext, actionContext.getPerformingPlayer()) {
            @Override
            public boolean isAtResolution() {
                return true;
            }
        };
    }

    /**
     * In the mode TAUGHT: whether none of the given costs, which pay nothing, will remember anything, each being
     * a choice with nothing to choose from ("spot an ally to heal that ally" with no such ally, where spotting
     * none is allowed).  The effects may then be asked as they are when about to be carried out.
     */
    public static boolean rememberNothing(ActionContext actionContext, Iterable<? extends EffectAppender> costs) {
        if (mode != Mode.TAUGHT)
            return false;
        final boolean before = ASKING_TAUGHT.get();
        ASKING_TAUGHT.set(true);
        try {
            final ActionContext context = asAtResolution(actionContext);
            for (EffectAppender cost : costs) {
                final EffectForecast forecast = cost.forecast(context);
                if (forecast.getVerdict() != EffectForecast.Verdict.NOTHING || forecast.isRemembering())
                    return false;
            }
            return true;
        } catch (RuntimeException exp) {
            return false;
        } finally {
            ASKING_TAUGHT.set(before);
        }
    }

    private static boolean areIdle(ActionContext context, Iterable<? extends EffectAppender> effects) {
        try {
            boolean anythingButBookkeeping = false;
            for (EffectAppender effect : effects) {
                final EffectForecast forecast = effect.forecast(context);
                // It may do something, it cannot tell, or its cap is spent (a limit: offered and flagged).
                if (forecast.getVerdict() != EffectForecast.Verdict.NOTHING)
                    return false;
                // An optional draw is still offered with an empty draw deck (Q-0029).
                if (forecast.isRestingOnADrawDeck())
                    return false;
                // What it remembers may be what a later effect acts on, and that effect, asked now as if it
                // were about to be carried out, would find its memory empty.
                if (forecast.isRemembering() && context.isAtResolution())
                    return false;
                if (!forecast.isBookkeeping())
                    anythingButBookkeeping = true;
            }
            return anythingButBookkeeping;
        } catch (RuntimeException exp) {
            // An effect that cannot be asked has not been found to do nothing.
            return false;
        }
    }
}
