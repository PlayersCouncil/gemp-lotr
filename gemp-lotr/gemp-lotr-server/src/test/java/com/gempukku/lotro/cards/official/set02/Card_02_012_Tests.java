package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_12");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two Dwarves at an underground site, and a draw deck deep enough for four uses.
	protected VirtualTableScenario GetTwoDwarvesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("realm", "2_12");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, no text in play
					put("troop", "1_177");      // Goblin Patrol Troop: minion, strength 13, no text
					for (int i = 1; i <= 20; i++) {
						put("filler" + i, "1_177");
					}
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RealmofDwarrowdelfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Realm of Dwarrowdelf
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br><b>Skirmish:</b> Discard the top 3 cards from your draw deck to make a Dwarf strength +1 (limit +3) and, if underground, <b>damage +1</b> (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Realm of Dwarrowdelf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "... and, if underground, damage +1 (limit +3)": the limit is the card's for the whole phase, whichever
	// Dwarf is chosen (F-0135, F-0125; the shape of F-0213 on the erratum 52_12).  The definition counted the
	// damage bonus for each Dwarf apart, so a second Dwarf got damage +1 after the first had had +3.
	// F-0456.
	@Test
	public void TheDamageLimitIsTheCardsSoNoDwarfGetsMoreOnceItIsReached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoDwarvesScenario();

		var realm = scn.GetFreepsCard("realm");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(realm);
		scn.MoveCompanionsToTable(gimli, guard);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		// Site 4, Balin's Tomb, is underground.
		assertEquals(4, scn.GetCurrentSiteNumber());
		for (int i = 0; i < 3; i++) {
			scn.FreepsUseCardAction(realm);
			scn.FreepsChooseCard(gimli);
			scn.ShadowPass();
		}
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(4, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		// A fourth use, on the other Dwarf: the cost is paid and neither limit has anything left to give.
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsUseCardAction(realm);
		scn.FreepsChooseCard(guard);

		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void RealmofDwarrowdelfTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
