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

public class Card_02_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_102");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo, Reluctant Adventurer is the Ring-bearer of this table.
	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Hobbit Sword: a possession of cost 1, a hand weapon.  "Bearer must be a Hobbit."
					put("sword1", "1_299");
					put("sword2", "1_299");
					// Sam, Son of Hamfast: the other Hobbit; his texts are used only on purpose.
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				"2_102",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Frodo, Reluctant Adventurer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Aragorn
		 * Game Text: <b>Ring-bearer (resistance 10).</b><br>The cost of each artifact, possession, and [shire] tale played on Frodo is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Reluctant Adventurer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void APossessionPlayedOnFrodoCostsOneLessAndOnAnotherHobbitItsFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(sword1, sword2);

		scn.StartGame();

		assertEquals("Reluctant Adventurer", frodo.getBlueprint().getSubtitle());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(sword1));

		scn.FreepsPlayCard(sword1);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(sword1, frodo);
		// 1 for the Sword - 1 on Frodo
		assertEquals(0, scn.GetTwilight());

		// Frodo has his hand weapon: the second Sword can only go to Sam, at its printed cost.
		assertTrue(scn.FreepsPlayAvailable(sword2));
		scn.FreepsPlayCard(sword2);
		assertAttachedTo(sword2, sam);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
