package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_50");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDeagolScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "13_50");
					// Déagol, Fateful Finder: the only card of that title, a Free Peoples [gollum] follower
					put("deagol", "13_46");
					// Defiled: an [orc] event that removes [orc] cards in its owner's discard pile from the game,
					// copies of itself among them: a card removed from the game that is not titled Déagol
					put("defiled", "13_105");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Gollum, Her Sneak
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: 
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: While a card titled Déagol is removed from the game, Gollum is strength +2 and <b>fierce</b>.<br><b>Shadow:</b> Remove a card titled Déagol in your discard pile from the game to play this minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Her Sneak", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus2AndFierceWhileACardTitledDeagolIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeagolScenario();

		var gollum = scn.GetShadowCard("gollum");
		var deagol = scn.GetShadowCard("deagol");
		var defiled = scn.GetShadowCard("defiled");
		scn.MoveCardsToDiscard(deagol, defiled);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum);

		assertEquals(5, scn.GetStrength(gollum));
		assertFalse(scn.HasKeyword(gollum, Keyword.FIERCE));

		// A card of his owner's that is removed from the game, and is not titled Déagol.
		scn.MoveCardToZone(defiled, Zone.REMOVED);
		assertEquals(5, scn.GetStrength(gollum));
		assertFalse(scn.HasKeyword(gollum, Keyword.FIERCE));

		scn.MoveCardToZone(deagol, Zone.REMOVED);
		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.HasKeyword(gollum, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromTheDiscardPileByRemovingDeagolFromTheGameHeIsStrengthPlus2AndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeagolScenario();

		var gollum = scn.GetShadowCard("gollum");
		var deagol = scn.GetShadowCard("deagol");
		var defiled = scn.GetShadowCard("defiled");
		scn.MoveCardsToDiscard(gollum, deagol, defiled);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.ShadowActionAvailable(gollum));

		// Déagol is the only card of that title in the discard pile: nothing is asked.
		scn.ShadowUseCardAction(gollum);
		assertInZone(Zone.REMOVED, deagol);
		assertInDiscard(defiled);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		// 2 to play + 2 roaming: his site number is 3 and the fellowship is at site 2
		assertEquals(0, scn.GetTwilight());

		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.HasKeyword(gollum, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOtherPlayersDeagolRemovedFromTheGameMakesGollumStrengthPlus2AndFierceAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeagolScenario();

		// The Free Peoples player's own Déagol, in his own discard pile: on his turns as the Shadow player his
		// own Gollum removes it from the game.
		var theirDeagol = scn.GetFreepsCard("deagol");
		scn.MoveCardsToDiscard(theirDeagol);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum);

		assertEquals(5, scn.GetStrength(gollum));
		assertFalse(scn.HasKeyword(gollum, Keyword.FIERCE));

		// The text names no owner: "a card titled Déagol is removed from the game" (F-0375).
		scn.MoveCardToZone(theirDeagol, Zone.REMOVED);
		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.HasKeyword(gollum, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
