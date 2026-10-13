package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_339_Tests
{

	// The King path with Hall of the Kings as its site 3.  Nothing on the way there asks anything of a fellowship
	// with no wound (Edoras Hall, King's Tent).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Companions, so that threats can be added: a threat cannot be added while there are as many
					// as there are companions.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast; a fellowship ability used only on purpose
					// Pippin, Trollslayer: his requirement to play is about his own play, and he is cheated in;
					// a skirmish ability used only on purpose
					put("pippin", "103_31");

					put("troop", "1_177");      // Goblin Patrol Troop: a card with no text, for the top of the draw deck
				}},
				new HashMap<>(VirtualTableScenario.KingSites)
				{{
					put("site3", "7_339");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HalloftheKingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Hall of the Kings
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 3
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> If you cannot spot 3 threats, add a threat to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Hall of the Kings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void WithTwoThreatsAddsAThreatToDrawACardAndAtThreeItIsNoLongerOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var troop = scn.GetFreepsCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam, pippin);

		scn.StartGame();
		scn.SkipToSite(3);
		var hall = scn.GetCurrentSite();

		scn.MoveCardsToTopOfDeck(troop);
		scn.AddThreats(2);
		// Nearly counting, beside the threats: a burden and a wound on the Ring-bearer.
		scn.AddBurdens(1);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertEquals("Hall of the Kings", hall.getBlueprint().getTitle());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DECK, troop);
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hall));

		scn.FreepsUseCardAction(hall);

		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.HAND, troop);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// Three threats can be spotted now.  With four companions a fourth threat could still be added: it is
		// the spot that withholds the action.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hall));
	}

	@Test
	public void NotOfferedWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		var hall = scn.GetCurrentSite();

		scn.MoveCardsToTopOfDeck(troop);
		// Two threats and two companions: fewer than 3 threats to spot, and no room for another.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		assertEquals("Hall of the Kings", hall.getBlueprint().getTitle());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hall));
	}
}
