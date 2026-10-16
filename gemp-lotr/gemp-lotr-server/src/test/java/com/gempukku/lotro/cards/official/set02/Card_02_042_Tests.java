package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblinman", "2_42");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetLookScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblinman", "2_42");
					// The Free Peoples player's hand: a companion, a possession and a minion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("armor", "1_8");        // Dwarven Armor
					put("runner", "1_178");     // Goblin Runner
					put("gimli", "1_13");       // Gimli, Son of Glóin: left in the Free Peoples draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUndergroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblinman1", "2_42");
					put("goblinman2", "2_42");  // a second copy, played where the first was discarded
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion with no text, beside him at the same sites
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUndergroundSanctuaryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblinman", "2_42");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion with no text, beside him at the same sites
				}},
				VirtualTableScenario.KingSites,   // site 3, The Dimholt 8_117: underground and a sanctuary
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTwoCopiesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblinman1", "2_42");
					put("goblinman2", "2_42");  // a second copy: "this minion" is the one that exerts
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a card for the Free Peoples hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinManStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Goblin Man
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 2
		 * Game Text: Discard this minion if underground.<br><b>Shadow:</b> Exert this minion to look at the Free Peoples player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goblinman");

		assertEquals("Goblin Man", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsThisMinionAndShowsTheFreePeoplesPlayersHandToItsOwnerAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLookScenario();

		var goblinMan = scn.GetShadowCard("goblinman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(aragorn, armor, runner);
		assertInZone(Zone.HAND, aragorn, armor, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinMan);
		// To Ettenmoors, which is not underground.
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, gimli);
		assertEquals(0, scn.GetWoundsOn(goblinMan));
		assertTrue(scn.ShadowActionAvailable(goblinMan));

		scn.ShadowUseCardAction(goblinMan);
		assertEquals(1, scn.GetWoundsOn(goblinMan));
		// The Shadow player is shown the whole hand, with nothing in it to choose, and no card from the draw deck.
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(gimli, false));
		assertEquals(0, scn.ShadowGetSelectableCount());
		// A look is the looker's alone: the hand's owner is shown nothing and asked nothing.
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		scn.ShadowDismissRevealedCards();
		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, aragorn, armor, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardedWhenTheFellowshipMovesUndergroundAndWhenPlayedThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUndergroundScenario();

		var goblinman1 = scn.GetShadowCard("goblinman1");
		var goblinman2 = scn.GetShadowCard("goblinman2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(goblinman1, goblinman2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Rivendell Valley, site 3, is not underground: the Goblin Man is played and stays.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.ShadowPlayCard(goblinman1);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman1);
		// A minion beside him, which underground sites do not touch.
		scn.MoveMinionsToTable(troop);

		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman1);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		// Balin's Tomb, site 4, is underground.
		scn.FreepsChooseToMove();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertInDiscard(goblinman1);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		// The second copy, played at Balin's Tomb, is discarded as it comes into play, its cost spent.
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(goblinman2);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInDiscard(goblinman1, goblinman2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardedWhenTheFellowshipMovesToAnUndergroundSiteThatIsASanctuaryToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUndergroundSanctuaryScenario();

		var goblinman = scn.GetShadowCard("goblinman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(goblinman, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));

		// The Dimholt, site 3, is underground and a sanctuary.
		scn.FreepsPass();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertInDiscard(goblinman);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExhaustedAGoblinManCannotLookAgainWhileAnotherCopyStillCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoCopiesScenario();

		var goblinman1 = scn.GetShadowCard("goblinman1");
		var goblinman2 = scn.GetShadowCard("goblinman2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinman1, goblinman2);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(goblinman1));
		assertEquals(0, scn.GetWoundsOn(goblinman2));
		assertTrue(scn.ShadowActionAvailable(goblinman1));
		assertTrue(scn.ShadowActionAvailable(goblinman2));

		// The copy used exerts, and the other does not.
		scn.ShadowUseCardAction(goblinman1);
		assertEquals(1, scn.GetWoundsOn(goblinman1));
		assertEquals(0, scn.GetWoundsOn(goblinman2));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		scn.ShadowDismissRevealedCards();

		// Vitality 2 with 1 wound: exhausted, so this copy can no longer pay; the other still can.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(goblinman1));
		assertTrue(scn.ShadowActionAvailable(goblinman2));
	}
}
