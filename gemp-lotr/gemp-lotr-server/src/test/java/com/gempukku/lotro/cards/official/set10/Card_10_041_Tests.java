package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "10_41");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "10_41");
					// Easterling Axeman: the other Easterling; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Dwarf Guard: a companion with no text once in play
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingPillagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Easterling Pillager
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>. <b>Fierce</b>.<br>When you play this minion, you may spot another Easterling to add a burden for each companion over 5.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Easterling Pillager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered (a spot is no
	// cost).  "A burden for each companion over 5" is no burden with five companions.
	@Test
	public void TheBurdenIsNotOfferedWithFiveCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		// Frodo and four Dwarf Guards
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"));

		var pillager = scn.GetShadowCard("pillager");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(pillager);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Another Easterling is there to spot.
		scn.ShadowPlayCard(pillager);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pillager));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheBurdenIsOfferedWithSixCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		// Frodo and five Dwarf Guards
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"));

		var pillager = scn.GetShadowCard("pillager");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(pillager);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(pillager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pillager));
		scn.ShadowAcceptOptionalTrigger();
		// How many companions to spot: all six
		scn.ShadowChoose("6");
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void EasterlingPillagerTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(5, scn.GetTwilight());
	}
}
