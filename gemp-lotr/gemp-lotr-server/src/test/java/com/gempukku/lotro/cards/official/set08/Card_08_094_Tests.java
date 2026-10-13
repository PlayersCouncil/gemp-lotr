package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_94");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The first is cheated into play: the [sauron] Orc to spot, and the other besieger
					put("agitator1", "8_94");
					put("agitator2", "8_94");
					put("agitator3", "8_94");
					// Dwarf Guard: a companion with no text once in play
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothAgitatorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gorgoroth Agitator
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>. To play, spot a [sauron] Orc.<br>When you play this minion, if you can spot another besieger, you may add a threat for each companion over 4.<br><b>Shadow:</b> Remove a threat to play a [sauron] engine from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gorgoroth Agitator", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  "A threat for each
	// companion over 4" is no threat with four companions.
	@Test
	public void TheThreatsAreOfferedWithFiveCompanionsAndNotWithFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var frodo = scn.GetRingBearer();
		var guard4 = scn.GetFreepsCard("guard4");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"));

		var agitator1 = scn.GetShadowCard("agitator1");
		var agitator2 = scn.GetShadowCard("agitator2");
		var agitator3 = scn.GetShadowCard("agitator3");
		scn.MoveCardsToHand(agitator2, agitator3);

		scn.StartGame();
		scn.MoveMinionsToTable(agitator1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Frodo and three Dwarf Guards; another besieger is there to spot.
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		scn.ShadowPlayCard(agitator2);
		assertInZone(Zone.SHADOW_CHARACTERS, agitator2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(agitator2));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A fifth companion: one over 4.
		scn.MoveCompanionsToTable(guard4);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(agitator3);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(agitator3));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GorgorothAgitatorTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
