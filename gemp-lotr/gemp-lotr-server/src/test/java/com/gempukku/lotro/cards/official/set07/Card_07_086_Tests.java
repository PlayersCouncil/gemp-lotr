package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denethor", "7_86");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, strength 6; "Knight." is its whole text
					put("knight2", "5_35");
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man who is an ally; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man companion; his skirmish ability needs him mounted
					put("nomad", "7_132");      // Desert Nomad: a [raider] Man minion; damage +1 at 2 threats and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DenethorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Denethor, Wizened Steward
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Denethor is strength +1 for each other [gondor] Man you can spot (limit +4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("denethor");

		assertEquals("Denethor", card.getBlueprint().getTitle());
		assertEquals("Wizened Steward", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void IsStrengthPlus1ForEachOtherGondorManAndForNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(denethor, pippin, rider);
		scn.MoveCardsToHand(knight);

		var nomad = scn.GetShadowCard("nomad");

		scn.StartGame();
		scn.MoveMinionsToTable(nomad);

		// Himself, a [gondor] Hobbit, a [rohan] Man, the opponent's [raider] Man and Frodo: none counts.
		assertEquals(5, scn.GetStrength(denethor));

		// The [gondor] Man ally counts.
		scn.MoveAlliesToTable(merchant);
		scn.FreepsRefreshDecision();
		assertEquals(6, scn.GetStrength(denethor));
		assertEquals(6, scn.GetStrength(merchant));

		// A [gondor] Man companion played from hand: one more.
		scn.FreepsPlayCard(knight);
		assertEquals(7, scn.GetStrength(denethor));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(5, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusStopsAtPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var knight2 = scn.GetFreepsCard("knight2");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(denethor, aragorn, boromir, knight);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(knight2);

		scn.StartGame();

		// Four other [gondor] Men: 5 + 4.
		assertEquals(9, scn.GetStrength(denethor));

		// A fifth: still 9.
		scn.FreepsPlayCard(knight2);
		assertEquals(9, scn.GetStrength(denethor));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
