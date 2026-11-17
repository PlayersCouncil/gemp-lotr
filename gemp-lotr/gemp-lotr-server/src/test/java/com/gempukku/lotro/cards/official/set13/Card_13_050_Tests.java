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
					put("gollum", "13_50");
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

	protected VirtualTableScenario GetAbilityScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "13_50");
					// Déagol, Fateful Finder: the only card of that title, a Free Peoples [gollum] follower
					put("deagol", "13_46");
					// Shelob, Last Child of Ungoliant: another [gollum] minion, left in the discard pile
					put("shelob", "8_26");
					put("runner", "1_178");     // Goblin Runner: another minion in play, strength 5
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

		var gollum = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", gollum.getBlueprint().getTitle());
		assertEquals("Her Sneak", gollum.getBlueprint().getSubtitle());
		assertTrue(gollum.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, gollum.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, gollum.getBlueprint().getCulture());
		assertEquals(CardType.MINION, gollum.getBlueprint().getCardType());
		assertEquals(2, gollum.getBlueprint().getTwilightCost());
		assertEquals(5, gollum.getBlueprint().getStrength());
		assertEquals(4, gollum.getBlueprint().getVitality());
		assertEquals(3, gollum.getBlueprint().getSiteNumber());
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
		assertInDiscard(gollum, deagol, defiled);
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

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheOnlyDeagolInADiscardPileIsTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAbilityScenario();

		var gollum = scn.GetShadowCard("gollum");
		var deagol = scn.GetShadowCard("deagol");
		var theirDeagol = scn.GetFreepsCard("deagol");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToDiscard(gollum, shelob);
		scn.MoveCardsToHand(deagol);
		scn.MoveCardsToDiscard(theirDeagol);

		scn.StartGame();
		assertInHand(deagol);
		assertInDiscard(gollum, shelob, theirDeagol);
		scn.FreepsPass();
		// 2 to play + 2 roaming: his site number is 3 and the fellowship is at site 2
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		// His own Déagol is in his hand, and the other player's in the other player's discard pile.
		assertInHand(deagol);
		assertInDiscard(gollum, shelob, theirDeagol);
		assertFalse(scn.ShadowActionAvailable(gollum));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveCardsToDiscard(deagol);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(gollum));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithThePoolOneShortOfGollumAndPlaysOnlyGollumFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAbilityScenario();

		var gollum = scn.GetShadowCard("gollum");
		var deagol = scn.GetShadowCard("deagol");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(gollum, deagol, shelob);

		scn.StartGame();
		assertInDiscard(gollum, deagol, shelob);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		// 2 to play + 2 roaming: his site number is 3 and the fellowship is at site 2
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertInDiscard(gollum, deagol, shelob);
		assertFalse(scn.ShadowActionAvailable(gollum));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(gollum));
		assertEquals(5, scn.GetStrength(runner));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));

		scn.ShadowUseCardAction(gollum);
		assertInZone(Zone.REMOVED, deagol);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		// The other minion in the discard pile stays there, and the minion in play is not strengthened.
		assertInDiscard(shelob);
		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.HasKeyword(gollum, Keyword.FIERCE));
		assertEquals(5, scn.GetStrength(runner));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
