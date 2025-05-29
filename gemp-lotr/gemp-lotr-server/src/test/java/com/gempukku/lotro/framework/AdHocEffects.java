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
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.TakeControlOfASiteEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

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
public interface AdHocEffects extends TestBase, Decisions {


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

	default void ShadowTakeControlOfSite() {
		ShadowExecuteAdHocEffect(new TakeControlOfASiteEffect(null, P2));
	}

	/**
	 * Causes the Free Peoples player to execute an arbitrary effect.  Note that there are nuances to how and whether this
	 * ever works; in particular it seems to only work if there are 0 legal actions to take on the current decision.
	 * When it works, it can be useful for altering the game state in situations where you want e.g. the proper trigger
	 * or other side-effects to be respected.  It is finicky tho.
	 * To ensure test integrity, this also asserts that the effect does in fact get carried out.
	 * @param effect The effect to execute.  Details are determined by the effect itself.
	 * executed as part of the current player decision.
	 */
	default void FreepsExecuteAdHocEffect(Effect effect) { ExecuteAdHocEffect(P1, effect); }
	/**
	 * Causes the Shadow player to execute an arbitrary effect.  Note that there are nuances to how and whether this
	 * ever works; in particular it seems to only work if there are 0 legal actions to take on the current decision.
	 * When it works, it can be useful for altering the game state in situations where you want e.g. the proper trigger
	 * or other side-effects to be respected.  It is finicky tho.
	 * To ensure test integrity, this also asserts that the effect does in fact get carried out.
	 * @param effect The effect to execute.  Details are determined by the effect itself.
	 * executed as part of the current player decision.
	 */
	default void ShadowExecuteAdHocEffect(Effect effect) { ExecuteAdHocEffect(P2, effect); }

	/**
	 * Causes the given player to execute an arbitrary effect.  Note that there are nuances to how and whether this
	 * ever works; in particular it seems to only work if there are 0 legal actions to take on the current decision.
	 * When it works, it can be useful for altering the game state in situations where you want e.g. the proper trigger
	 * or other side-effects to be respected.  It is finicky tho.
	 * To ensure test integrity, this also asserts that the effect does in fact get carried out.
	 * @param playerId The player who will execute the effect.
	 * @param effect The effect to execute.  Details are determined by the effect itself.
	 * executed as part of the current player decision.
	 */
	default void ExecuteAdHocEffect(String playerId, Effect effect) {
        carryOutEffectInPhaseActionByPlayer(playerId, effect);
        assertTrue(effect.wasCarriedOut());
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
						new DiscardCardsFromPlayEffect(P2, null, filterables));
				return Collections.singletonList(action);
			}
		});
	}

	default void carryOutEffectInPhaseActionByPlayer(String playerId, Effect effect) {
		var action = new SystemQueueAction();
		action.appendEffect(effect);
		carryOutEffectInPhaseActionByPlayer(playerId, action);
	}

	default void carryOutEffectInPhaseActionByPlayer(String playerId, Action action) {
		var awaitingDecision = (CardActionSelectionDecision) userFeedback().getAwaitingDecision(playerId);
		awaitingDecision.addAction(action);

		PlayerDecided(playerId, "0");
	}


}
