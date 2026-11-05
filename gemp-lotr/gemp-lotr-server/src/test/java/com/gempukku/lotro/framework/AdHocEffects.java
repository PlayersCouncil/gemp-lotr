package com.gempukku.lotro.framework;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.cards.build.field.effect.filter.FilterFactory;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.ActionProxy;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.virtualtable.Decisions;
import com.gempukku.lotro.virtualtable.TableBase;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Sometimes, there just isn't a card that has the precise action or effect that you want to use (especially when
 * developing new mechanics), so these functions permit the tester to staple actions, effects, and modifiers onto
 * physical cards as needed.  This is also useful when testing fundamental existing mechanics without any
 * confounding variables.
 *
 * Be warned: just because an action or effect has been added to the game does not automatically mean that Gemp will
 * respect it or be aware of it just yet.  In nearly all cases, after you have used any of these functions you will
 * need to execute a decision so that Gemp's game loop will process what's on the table (at which point it will
 * become aware of the changes you have made).  While awaiting a decision Gemp is effectively paused and thus needs
 * that moment to become aware of changes.
 */
public interface AdHocEffects extends TableBase, TestConstants, Decisions {


	FilterFactory FilterFactory = new FilterFactory();
	CardGenerationEnvironment Environment = new LotroCardBlueprintBuilder();


	ActionContext FreepsFilterContext();
	ActionContext ShadowFilterContext();


	/**
	 * Staples a given modifier to the game state for the duration of the game.  This can be a good way to test elusive
	 * game states that would otherwise require a lot of setup.
	 * @param mod The modifier to permanently add to the game.
	 */
	default void ApplyAdHocModifier(Modifier mod)
    {
        game().getModifiersEnvironment().addAlwaysOnModifier(mod);
    }

	/**
	 * Causes the given action to be stapled to the game state for the duration of the game.  The action will be
	 * available to the appropriate player at the appropriate time as if an invisible card were on the board.  The
	 * precise details (player, phase, effect) will need to be set in the action itself.
	 * @param action The action to permanently add to the game.
	 */
	default void ApplyAdHocAction(ActionProxy action)
    {
        game().getActionsEnvironment().addAlwaysOnActionProxy(action);
    }

	/**
	 * Causes the Free Peoples player to execute an arbitrary effect.  The player must be at a choice of
	 * phase actions ("Play ... action or Pass"): the effect is run in place of that player's answer to it, whatever
	 * else the choice offers, and the game then moves on to whoever acts next.  It is useful for altering the game
	 * state in situations where you want e.g. the proper trigger or other side-effects to be respected.
	 * To ensure test integrity, this also asserts that the effect does in fact get carried out.
	 * @param effect The effect to execute.  Details are determined by the effect itself.
	 * executed as part of the current player decision.
	 */
	default void FreepsExecuteAdHocEffect(Effect effect) { ExecuteAdHocEffect(P1(), effect); }
	/**
	 * Causes the Shadow player to execute an arbitrary effect.  The player must be at a choice of
	 * phase actions ("Play ... action or Pass"): the effect is run in place of that player's answer to it, whatever
	 * else the choice offers, and the game then moves on to whoever acts next.  It is useful for altering the game
	 * state in situations where you want e.g. the proper trigger or other side-effects to be respected.
	 * To ensure test integrity, this also asserts that the effect does in fact get carried out.
	 * @param effect The effect to execute.  Details are determined by the effect itself.
	 * executed as part of the current player decision.
	 */
	default void ShadowExecuteAdHocEffect(Effect effect) { ExecuteAdHocEffect(P2(), effect); }

	/**
	 * Causes the given player to execute an arbitrary effect.  The player must be at a choice of
	 * phase actions ("Play ... action or Pass"): the effect is run in place of that player's answer to it, whatever
	 * else the choice offers, and the game then moves on to whoever acts next.  It is useful for altering the game
	 * state in situations where you want e.g. the proper trigger or other side-effects to be respected.
	 * To ensure test integrity, this also asserts that the effect does in fact get carried out.
	 * @param playerId The player who will execute the effect.
	 * @param effect The effect to execute.  Details are determined by the effect itself.
	 * executed as part of the current player decision.
	 */
	default void ExecuteAdHocEffect(String playerId, Effect effect) {
        carryOutEffectInPhaseActionByPlayer(playerId, effect);
        assertTrue("The ad hoc effect was not carried out: something stopped it, or it had nothing to act on."
                + "  Where that is what the test is about, run it with ExecuteAdHocEffectUnchecked.", effect.wasCarriedOut());
    }

	/**
	 * Causes the Free Peoples player to execute an arbitrary effect whose outcome the test is about, without
	 * asserting that it was carried out: a wound aimed at a character who may turn out to be protected from it.
	 * The test reads what became of it from the board.  See {@link #ExecuteAdHocEffectUnchecked}.
	 * @param effect The effect to execute.  Details are determined by the effect itself.
	 */
	default void FreepsExecuteAdHocEffectUnchecked(Effect effect) { ExecuteAdHocEffectUnchecked(P1(), effect); }
	/**
	 * Causes the Shadow player to execute an arbitrary effect whose outcome the test is about, without asserting
	 * that it was carried out: a wound aimed at a character who may turn out to be protected from it.  The test
	 * reads what became of it from the board.  See {@link #ExecuteAdHocEffectUnchecked}.
	 * @param effect The effect to execute.  Details are determined by the effect itself.
	 */
	default void ShadowExecuteAdHocEffectUnchecked(Effect effect) { ExecuteAdHocEffectUnchecked(P2(), effect); }
	/**
	 * Causes the given player to execute an arbitrary effect, as ExecuteAdHocEffect does, but without asserting
	 * that the effect was carried out.  It is for an effect that something on the board may stop (a wound that a
	 * prevention or a "cannot take wounds" may turn aside), where what happened is the thing under test and is
	 * read from the board afterwards.  Where the effect is only a means of arranging the board, use
	 * ExecuteAdHocEffect, which fails the test if the effect did not happen.
	 * The effect is run in place of the player's answer to a choice of phase actions, so that choice is used up:
	 * the game moves on to whoever acts next.
	 * @param playerId The player who will execute the effect, who must be at a choice of phase actions.
	 * @param effect The effect to execute.  Details are determined by the effect itself.
	 */
	default void ExecuteAdHocEffectUnchecked(String playerId, Effect effect) {
		carryOutEffectInPhaseActionByPlayer(playerId, effect);
	}

	/**
	 * Takes a filter string and produces the proper Java code that the filter represents.  This is used to produce
	 * other ad-hoc effects in a manner that more closely mimicks regular hjson card definitions.
	 * The action context used will assume that "you" is the Free Peoples player.
	 * @param filter Filter string to convert
	 * @return A filterable code construct to be used on other effects
	 * @throws InvalidCardDefinitionException Throws an error if your filter was malformed.
	 */
	default Filterable GenerateFreepsFilter(String filter) throws InvalidCardDefinitionException {
		return FilterFactory.generateFilter(filter, Environment).getFilterable(FreepsFilterContext());
	}
	/**
	 * Takes a filter string and produces the proper Java code that the filter represents.  This is used to produce
	 * other ad-hoc effects in a manner that more closely mimicks regular hjson card definitions.
	 * The action context used will assume that "you" is the Shadow player.
	 * @param filter Filter string to convert
	 * @return A filterable code construct to be used on other effects
	 * @throws InvalidCardDefinitionException Throws an error if your filter was malformed.
	 */
	default Filterable GenerateShadowFilter(String filter) throws InvalidCardDefinitionException {
		return FilterFactory.generateFilter(filter, Environment).getFilterable(ShadowFilterContext());
	}

	/**
	 * Adds a permanent effect that automatically discards cards that match the provided filter.
	 * "You" and "Your" in filters will mean the Free Peoples player.
	 * @param filter The hjson filter string representing which cards should be auto discarded.
	 */
	default void ApplyAdHocFreepsAutoDiscard(String filter)  {
		try{
			ApplyAdHocAutoDiscard(GenerateFreepsFilter(filter));
		}
		catch(InvalidCardDefinitionException ignored) {}

	}

	/**
	 * Adds a permanent effect that automatically discards cards that match the provided filter.
	 * "You" and "Your" in filters will mean the Shadow player.
	 * @param filter The hjson filter string representing which cards should be auto discarded.
	 */
	default void ApplyAdHocShadowAutoDiscard(String filter)  {
		try {
			ApplyAdHocAutoDiscard(GenerateShadowFilter(filter));
		}
		catch(InvalidCardDefinitionException ignored) {}
	}

	/**
	 * Adds a permanent effect that automatically discards cards that match the provided filterables.
	 * You probably aren't using this in normal unit tests.  Use ApplyAdHocFreepsAutoDiscard or ApplyAdHocShadowAutoDiscard
	 * instead.
	 * @param filterables The Java card filters representing which cards should be auto discarded.
	 */
	default void ApplyAdHocAutoDiscard(Filterable...filterables)  {

		ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(
					LotroGame game, EffectResult effectResult)  {
				RequiredTriggerAction action = new RequiredTriggerAction(null);
				action.appendEffect(
						new DiscardCardsFromPlayEffect(P2(), null, filterables));
				return Collections.singletonList(action);
			}
		});
	}

	default void carryOutEffectInPhaseActionByPlayer(String playerId, Effect effect) {
		var action = new SystemQueueAction();
		action.appendEffect(effect);
		carryOutEffectInPhaseActionByPlayer(playerId, action);
	}

	/**
	 * Has the given player take the given action in answer to the choice of actions that player is facing, as if
	 * it had been among the actions on offer.  Fails unless the player is at a choice of actions.
	 */
	default void carryOutEffectInPhaseActionByPlayer(String playerId, Action action) {
		var pending = userFeedback().getAwaitingDecision(playerId);
		assertTrue(playerId + " is not at a choice of actions, so an ad hoc effect cannot be run for that player"
				+ (pending == null ? " (nothing is being asked)." : " (the decision pending is '" + pending.getText() + "')."),
				pending instanceof CardActionSelectionDecision);
		var awaitingDecision = (CardActionSelectionDecision) pending;
		// The action is added after those the decision already offers, and is taken by its place among them.
		int place = awaitingDecision.getDecisionParameters().get("actionId").length;
		awaitingDecision.addAction(action);

		PlayerDecided(playerId, String.valueOf(place));
	}


}
