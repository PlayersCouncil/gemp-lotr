package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Functions for checking aspects of a card, such as numeric states and other state-based properties.
 *
 * If you are just checking the printed stats, then you can retrieve the blueprint of a card and check those values
 * directly.  These helper functions are more for live cards which may be affected by modifiers sourced from other cards.
 */
public interface CardProperties extends TableBase {

	default int FreepsGetWoundsOn(String cardName) { return GetWoundsOn(GetFreepsCard(cardName)); }
	default int ShadowGetWoundsOn(String cardName) { return GetWoundsOn(GetShadowCard(cardName)); }
	default int GetWoundsOn(PhysicalCardImpl card) { return gameState().getWounds(card); }
	default boolean IsExhausted(PhysicalCardImpl card) { return GetVitality(card) == 1; }

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


	/**
	 * How many culture tokens of the card's own printed culture are on the card: a token of any other culture on
	 * it is not counted, and this cannot read one.
	 * @param card The card to inspect.
	 * @return The number of tokens of that card's culture on it.
	 */
	default int GetCultureTokensOn(PhysicalCardImpl card) { return gameState().getTokenCount(card, Token.findTokenForCulture(card.getBlueprint().getCulture())); }

	/**
	 * Puts culture tokens of the card's own printed culture on the card, the only kind of token this can place.
	 * Nothing is reinforced and nothing is added by a card: no trigger answers it.
	 * @param card The card to put tokens on.
	 * @param count How many.
	 */
	default void AddTokensToCard(PhysicalCardImpl card, int count) {
		gameState().addTokens(card, Token.findTokenForCulture(card.getBlueprint().getCulture()), count);
	}

	/**
	 * Takes culture tokens of the card's own printed culture off the card, the only kind of token this can remove,
	 * and no more of them than are there.
	 * @param card The card to take tokens from.
	 * @param count How many.
	 */
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

	/**
	 * Whether the Free Peoples player's client has the given card before it as a card in the given zone.  See
	 * PlayerSeesCardInZone.
	 */
	default boolean FreepsSeesCardInZone(PhysicalCardImpl card, Zone zone) { return PlayerSeesCardInZone(P1(), card, zone); }
	/**
	 * Whether the Shadow player's client has the given card before it as a card in the given zone.  See
	 * PlayerSeesCardInZone.
	 */
	default boolean ShadowSeesCardInZone(PhysicalCardImpl card, Zone zone) { return PlayerSeesCardInZone(P2(), card, zone); }
	/**
	 * Whether the given player's client has the given card before it as a card in the given zone: true when the
	 * zone that seat was last sent for the card, as it was put into play or moved (the server's own channel for
	 * the seat decides what it is sent), is that zone, and it has not since been sent the card's removal.  This
	 * is what a client draws the card by, and not the table's own idea of where the card is: for that, assertInZone.
	 * A card the seat is not shown (one in a draw deck, one in another player's hand) is in no zone to it.
	 * A site on the adventure path cannot be asked about: the table keeps nothing of how sites are sent as they
	 * are laid, and the reading is refused for one.
	 *
	 * Read as PlayerSeesCardInHand is: from the channel the table keeps for the seat and from a client joined to
	 * the seat as the reading is made, failing if the two were sent different zones, and on a table that keeps no
	 * such channels.
	 * @param player The player whose seat is asked about.
	 * @param card The card, wherever it lies and whoever owns it.
	 * @param zone The zone asked about.
	 * @return True if that seat was last sent the card as a card in that zone.
	 */
	default boolean PlayerSeesCardInZone(String player, PhysicalCardImpl card, Zone zone) {
		Verify(zone != null, "No zone was given to ask whether " + player + " sees a card in it.");
		return zone != null && SeatChannel.zoneSeen(this, player, card) == zone;
	}

	/**
	 * Whether the Free Peoples player's client has the given card before it as borne by the given bearer, or by
	 * no card.  See PlayerSeesCardBorneBy.
	 */
	default boolean FreepsSeesCardBorneBy(PhysicalCardImpl card, PhysicalCardImpl bearer) { return PlayerSeesCardBorneBy(P1(), card, bearer); }
	/**
	 * Whether the Shadow player's client has the given card before it as borne by the given bearer, or by no
	 * card.  See PlayerSeesCardBorneBy.
	 */
	default boolean ShadowSeesCardBorneBy(PhysicalCardImpl card, PhysicalCardImpl bearer) { return PlayerSeesCardBorneBy(P2(), card, bearer); }
	/**
	 * Whether the given player's client has the given card before it as borne by the given bearer: true when that
	 * seat was last sent the card, as it was put into play or moved, as attached to that bearer, and has not since
	 * been sent the card's removal.  With null for the bearer: true when the seat has the card before it and was
	 * last sent it as attached to no card, which is how a client must be told of a card that lies on its own.
	 * A card the seat is not shown at all is borne by nothing and by nobody to it: false either way.
	 *
	 * Read as PlayerSeesCardInHand is: from the channel the table keeps for the seat and from a client joined to
	 * the seat as the reading is made, failing if the two were sent different bearers, and on a table that keeps
	 * no such channels.
	 * @param player The player whose seat is asked about.
	 * @param card The card asked about.
	 * @param bearer The card that is to bear it, or null to ask whether it is shown as borne by no card.
	 * @return True if that seat has the card before it as borne by that bearer (or, for null, by no card).
	 */
	default boolean PlayerSeesCardBorneBy(String player, PhysicalCardImpl card, PhysicalCardImpl bearer) {
		return SeatChannel.zoneSeen(this, player, card) != null && SeatChannel.bearerSeen(this, player, card) == bearer;
	}

	default Boolean IsSiteControlled(PhysicalCardImpl card) {
		return card.getCardController() != null;
	}

	default Boolean FreepsControls(PhysicalCardImpl card) {
		return card.getCardController() != null && card.getCardController().equals(P1());
	}

	default Boolean ShadowControls(PhysicalCardImpl card) {
		return card.getCardController() != null && card.getCardController().equals(P2());
	}

	/**
	 * @param card The card to inspect.
	 * @return The twilight cost of the card where it is.  For a card in play that is the number printed on it,
	 * whatever another card or its own text says about its cost (CR 4.2 "twilight cost").  For a card in a hand
	 * or a pile it is what the card would cost to play, as altered by all current in-game effects, the roaming
	 * penalty apart.  For a site it is the Shadow number as it stands.
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

	/**
	 * The N of a follower's printed "Aid - (N)": the twilight its Aid costs, as the card's definition has it.  A
	 * Free Peoples follower's Aid adds that much to the twilight pool and a Shadow follower's removes it.  This
	 * reads the card, wherever it lies, and takes no Aid: the pool read before and after an Aid is taken shows
	 * what was paid.
	 * Only for an Aid that is a plain amount of twilight and nothing else.  Fails, saying so, for a follower whose
	 * Aid is paid in anything else ("Aid - Add a burden.", "Aid - Exert a [gondor] companion.") and for a card
	 * that is no follower.
	 * @param follower The follower to inspect.
	 * @return The twilight cost of its Aid.
	 */
	default int GetAidTwilightCost(PhysicalCardImpl follower) {
		Verify(follower != null && follower.getBlueprint().getCardType() == CardType.FOLLOWER,
				(follower == null ? "Nothing" : GameUtils.getFullName(follower))
						+ " is not a follower, so it has no Aid whose twilight cost could be read.");
		Integer cost = follower.getBlueprint().getAidTwilightCost();
		Verify(cost != null, "The Aid of " + GameUtils.getFullName(follower)
				+ " is not a plain amount of twilight, so it has no twilight cost to read.");
		return cost;
	}

	default int GetOverwhelmMultiplier(PhysicalCardImpl card) { return game().getModifiersQuerying().getOverwhelmMultiplier(game(), card); }

	/**
	 * The vitality the card has left: its printed vitality and whatever cards add or take away, less the wounds
	 * on it, and never less than 0.  A character of printed vitality 4 with 3 wounds reads 1.  The printed
	 * number is the blueprint's.
	 * @param card The card to inspect.
	 * @return The modified current vitality of the card, as altered by all current in-game effects and by its wounds.
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

	/**
	 * What a card adds of its own to what the client shows of it, under "Extra information" in its card info: the
	 * text of its definition's displayableInformation, with the value the card has stored on itself (a race, a
	 * culture or a keyword named as it was played) in place of {stored}.
	 * @param card The card to inspect.
	 * @return That text; null if the card's definition has none.
	 */
	default String GetDisplayableInformation(PhysicalCardImpl card) { return card.getBlueprint().getDisplayableInformation(card); }

	/**
	 * The "Affecting card" list of a card's info as the client shows it, one line for each modifier, as
	 * "&lt;source's title&gt;: &lt;modifier's text&gt;" ("Glamdring: Strength +2"), and "System: &lt;text&gt;" for a
	 * modifier with no source card (a rule's).  The lines are what the card info reads
	 * (ModifiersQuerying.getModifiersAffecting), in alphabetical order; the card info itself shows them unordered and
	 * links each source.
	 * @param card The card to inspect.
	 * @return The lines; empty when nothing affects the card.
	 */
	default List<String> GetAffectingCard(PhysicalCardImpl card) {
		List<String> lines = new ArrayList<>();
		for (var modifier : game().getModifiersQuerying().getModifiersAffecting(game(), card)) {
			var source = modifier.getSource();
			lines.add((source == null ? "System" : source.getBlueprint().getTitle()) + ": " + modifier.getText(game(), card));
		}
		Collections.sort(lines);
		return lines;
	}

	default boolean IsHindered(PhysicalCardImpl card) { return HasKeyword(card, Keyword.HINDERED); }
	default void HinderCard(PhysicalCardImpl...cards) {
		for(var card : cards) {
			game().getGameState().hinder(Collections.singletonList(card));
		}
	}
	default void RestoreCard(PhysicalCardImpl...cards) {
		for(var card: cards) {
			game().getGameState().restore(game(), Collections.singletonList(card));
		}
	}

}
