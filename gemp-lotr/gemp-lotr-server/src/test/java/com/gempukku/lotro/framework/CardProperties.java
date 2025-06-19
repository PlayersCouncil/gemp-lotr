package com.gempukku.lotro.framework;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCardImpl;

import java.util.Collections;
import java.util.List;

/**
 * Functions for checking aspects of a card, such as numeric states and other state-based properties.
 *
 * If you are just checking the printed stats, then you can retrieve the blueprint of a card and check those values
 * directly.  These helper functions are more for live cards which may be affected by modifiers sourced from other cards.
 */
public interface CardProperties extends TestBase {

	default int FreepsGetWoundsOn(String cardName) { return GetWoundsOn(GetFreepsCard(cardName)); }
	default int ShadowGetWoundsOn(String cardName) { return GetWoundsOn(GetShadowCard(cardName)); }
	default int GetWoundsOn(PhysicalCardImpl card) { return gameState().getWounds(card); }

	default void AddWoundsToChar(PhysicalCardImpl card, int count) {
		for(int i = 0; i < count; i++) {
			gameState().addWound(card);
		}
	}

	default void RemoveWoundsFromChar(PhysicalCardImpl card, int count) {
		for(int i = 0; i < count; i++) {
			gameState().removeWound(card);
		}
	}


	default int GetCultureTokensOn(PhysicalCardImpl card) { return gameState().getTokenCount(card, Token.findTokenForCulture(card.getBlueprint().getCulture())); }

	default void AddTokensToCard(PhysicalCardImpl card, int count) {
		gameState().addTokens(card, Token.findTokenForCulture(card.getBlueprint().getCulture()), count);
	}

	default void RemoveTokensFromCard(PhysicalCardImpl card, int count) {
		gameState().removeTokens(card, Token.findTokenForCulture(card.getBlueprint().getCulture()), count);
	}


	/**
	 * @param card The target card
	 * @return All cards currently attached to the given card.
	 */
    default List<PhysicalCardImpl> GetAttachedCards(PhysicalCardImpl card) {
        return (List<PhysicalCardImpl>)(List<?>)gameState().getAttachedCards(card);
    }

	/**
	 * @param card The target card
	 * @return All cards currently stacked on the given card.
	 */
    default List<PhysicalCardImpl> GetStackedCards(PhysicalCardImpl card) {
        return (List<PhysicalCardImpl>)(List<?>)gameState().getStackedCards(card);
    }

	default Boolean IsSiteControlled(PhysicalCardImpl card) {
		return card.getCardController() != null;
	}

	default Boolean FreepsControls(PhysicalCardImpl card) {
		return card.getCardController() != null && card.getCardController().equals(P1);
	}

	default Boolean ShadowControls(PhysicalCardImpl card) {
		return card.getCardController() != null && card.getCardController().equals(P2);
	}

	/**
	 * @param card The card to inspect.
	 * @return The modified current twilight cost of the card, as altered by all current in-game effects.
	 */
	default int GetTwilightCost(PhysicalCardImpl card)
	{
		return game().getModifiersQuerying().getCurrentTwilightCost(game(), card);
	}

	/**
	 * @param card The card to inspect.
	 * @return The modified current strength of the card, as altered by all current in-game effects.
	 */
	default int GetStrength(PhysicalCardImpl card)
	{
		return game().getModifiersQuerying().getStrength(game(), card);
	}

	default int GetOverwhelmMultiplier(PhysicalCardImpl card) { return game().getModifiersQuerying().getOverwhelmMultiplier(game(), card); }

	/**
	 * @param card The card to inspect.
	 * @return The modified current vitality of the card, as altered by all current in-game effects.
	 */
	default int GetVitality(PhysicalCardImpl card) { return game().getModifiersQuerying().getVitality(game(), card); }

	/**
	 * @param card The card to inspect.
	 * @return The modified current resistance of the card, as altered by all current in-game effects.
	 */
	default int GetResistance(PhysicalCardImpl card) { return game().getModifiersQuerying().getResistance(game(), card); }

	/**
	 * @param card The card to inspect.
	 * @return The modified current site number of the card, as altered by all current in-game effects.
	 */
	default int GetMinionSiteNumber(PhysicalCardImpl card){ return game().getModifiersQuerying().getMinionSiteNumber(game(), card); }

	/**
	 * @param card The card to inspect.
	 * @param keyword The keyword to check for.
	 * @return Whether the current card has the given keyword, either printed on it or added (or removed) by a game effect.
	 */
    default boolean HasKeyword(PhysicalCardImpl card, Keyword keyword)
    {
        return game().getModifiersQuerying().hasKeyword(game(), card, keyword);
    }

	/**
	 * @param card The card to inspect.
	 * @param keyword The keyword to count.
	 * @return How many of a numeric keyword this card currently has.
	 */
	default int GetKeywordCount(PhysicalCardImpl card, Keyword keyword)
	{
		return game().getModifiersQuerying().getKeywordCount(game(), card, keyword);
	}

	/**
	 * @param card The card to inspect.
	 * @param timeword The time-word (phase or response) to check for.
	 * @return Whether the current card has the given timeword, either printed on it or added (or removed) by a game effect.
	 */
	default boolean HasTimeword(PhysicalCardImpl card, Timeword timeword) {
		return card.getBlueprint().hasTimeword(timeword);
	}

	/**
	 * @param card The card to inspect.
	 * @param type The card type to check for.
	 * @return Whether the current card has the given type, either printed on it or added (or removed) by a game effect.
	 */
    default boolean IsType(PhysicalCardImpl card, CardType type)
    {
        return game().getModifiersQuerying().isCardType(game(), card, type);
    }

	default boolean IsRace(PhysicalCardImpl card, Race race)
	{
		return  game().getModifiersQuerying().isRace(game(), card, race);
	}

	default boolean IsHindered(PhysicalCardImpl card) { return HasKeyword(card, Keyword.HINDERED); }
	default void HinderCard(PhysicalCardImpl card) {  game().getGameState().hinder(Collections.singletonList(card)); }
	default void RestoreCard(PhysicalCardImpl card) {  game().getGameState().restore(Collections.singletonList(card)); }

}
