package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_269_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_269");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A [sauron] Orc of site number 6 at site 2, and four companions to hold four threats.
	protected VirtualTableScenario GetThreatsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fires", "7_269");
					put("chieftain", "1_266");
					put("aragorn", "1_89");
					put("gimli", "1_13");
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FiresRagedUncheckedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Fires Raged Unchecked
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: The site number of each [sauron] Orc is -1 for each threat.<br>While you can spot 3 threats, each [sauron] Orc that is not roaming is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Fires Raged Unchecked", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "each [sauron] Orc that is not roaming is strength +1": the definition made every [sauron] Orc stronger.
	@Test
	public void ASauronOrcStillRoamingAtThreeThreatsIsNotStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatsScenario();

		scn.MoveCompanionsToTable("aragorn", "gimli", "sam");

		var fires = scn.GetShadowCard("fires");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(fires);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(chieftain));

		// Site number 6 - 3 for the threats is 3, at site 2: the Chieftain is still roaming.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertEquals(7, scn.GetStrength(chieftain));

		// With a fourth threat his site number is 2 and he roams no longer.
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertEquals(8, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void FiresRagedUncheckedTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
