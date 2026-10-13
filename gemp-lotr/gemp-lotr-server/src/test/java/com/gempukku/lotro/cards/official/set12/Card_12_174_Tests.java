package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_174_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_174");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKillScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "12_174");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: the other [wraith] card; strength 9
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3
					// Dwarf Guard: a companion with no text once in play
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Úlairë Cantëa, Black Assassin
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br><b>Assignment:</b> Assign Úlairë Cantëa to a companion who has resistance 0.<br><b>Skirmish:</b> Spot 6 companions and another [wraith] card to kill a companion Úlairë Cantëa is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Úlairë Cantëa", card.getBlueprint().getTitle());
		assertEquals("Black Assassin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered (a spot is no
	// cost).  In a skirmish Úlairë Cantëa is not part of there is no "companion Úlairë Cantëa is skirmishing".
	@Test
	public void TheKillIsOfferedInCanteasOwnSkirmishAndNotInAnothers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKillScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// Six companions with Frodo
		scn.MoveCompanionsToTable(aragorn, gimli, scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"));

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, cantea});

		// Aragorn's skirmish is against Úlairë Lemenya: the six companions and the other [wraith] card are there
		// to spot, and Úlairë Cantëa is skirmishing nobody.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));
		scn.ShadowPass();

		// Gimli's is against her.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, cantea));
		assertTrue(scn.ShadowActionAvailable(cantea));

		scn.ShadowUseCardAction(cantea);
		assertInZone(Zone.DEAD, gimli);
		// That was the last skirmish; she is fierce, and the fierce round begins with its assignment actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void UlaireCanteaTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(5, scn.GetTwilight());
	}
}
