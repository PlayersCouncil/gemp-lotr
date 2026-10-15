package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_4");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Pass of Caradhras 1_348 as site 4: a mountain (its Shadow ability needs an
	// [isengard] minion).  Thrarin's home, site 3, is Rivendell Valley 1_341, a sanctuary and no more.  No Dwarf ally
	// has a home that is a battleground or a mountain: each is of site 3 of the Fellowship path.  Aragorn fights the
	// skirmish; the Dwarves are in none.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("durability", "12_4");
					put("durability2", "12_4");
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5, resistance 6; +2 while skirmishing an Orc
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home 3F, strength 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("spare", "1_177");      // a card to draw
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DurabilityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Durability
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf strength +2. Then, if that Dwarf is at a battleground or mountain site, draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Durability", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void DurabilityTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void AnAllyWhoseHomeIsNoMountainDrawsNothingWithTheFellowshipAtAMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var durability = scn.GetFreepsCard("durability");
		var durability2 = scn.GetFreepsCard("durability2");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(durability, durability2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(durability, durability2);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Pass of Caradhras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertInHand(durability, durability2);
		var spare = scn.GetFreepsCard("spare");
		var spare2 = scn.GetFreepsCard("troop");
		scn.MoveCardsToTopOfDeck(spare2, spare);
		scn.FreepsRefreshDecision();
		assertSame(spare, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, spare, spare2);
		int hand = scn.GetFreepsHandCount();

		// The companion is with the fellowship, at the mountain: 5 + 2, and a card drawn.
		scn.FreepsPlayCard(durability);
		scn.FreepsChooseCard(farin);
		assertEquals(7, scn.GetStrength(farin));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertInHand(spare);

		// Thrarin is at his home site, Rivendell Valley (CR 4.2 "ally"): 4 + 2, and nothing drawn.
		scn.ShadowPass();
		scn.FreepsPlayCard(durability2);
		scn.FreepsChooseCard(thrarin);
		assertEquals(6, scn.GetStrength(thrarin));
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, spare2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
