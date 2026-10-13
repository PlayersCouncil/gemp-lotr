package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "14_10");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Hillman beside a [men] minion who is no Man, with a [men] Man to put down later.
	protected VirtualTableScenario GetAnotherMenManScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman", "14_10");
					put("ted", "18_75");
					put("corps", "11_73");
					put("gimli", "1_13");
					put("disquiet1", "2_2");
					put("disquiet2", "2_2");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FuriousHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Furious Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Response:</b> If the Free Peoples player plays an event, spot another [men] Man and discard this minion from play to cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Furious Hillman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// "spot another [men] Man": the definition spotted any other [men] card.
	@Test
	public void TheResponseWantsAnotherMenManAndNotJustAnotherMenCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAnotherMenManScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var disquiet2 = scn.GetFreepsCard("disquiet2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(disquiet1, disquiet2);

		var hillman = scn.GetShadowCard("hillman");
		var ted = scn.GetShadowCard("ted");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, ted);
		scn.SkipToPhase(Phase.MANEUVER);

		// Ted Sandyman is a [men] minion and a Hobbit: there is no other [men] Man, and the event goes through.
		scn.FreepsPlayCard(disquiet1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// With a [men] Man beside him the Hillman may answer the next event.
		scn.MoveMinionsToTable(corps);
		scn.ShadowPass();
		scn.FreepsPlayCard(disquiet2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, hillman);
		// Cancelled: Gimli exerted for it and is no more of a defender than he was.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void FuriousHillmanTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
