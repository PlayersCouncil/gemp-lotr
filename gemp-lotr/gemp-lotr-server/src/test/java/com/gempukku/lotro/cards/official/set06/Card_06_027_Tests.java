package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_06_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("avenger", "6_27");
					put("avenger2", "6_27");

					put("quickbeam", "6_33");   // Quickbeam, Bregalad: Ent companion, strength 8; only a cost discount of his own
					put("onod", "17_26");       // Woodland Onod: Ent companion, strength 7; a skirmish ability used only on purpose
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: Ent ally, strength 12; a Fellowship ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion who is no Ent
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EntAvengerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Ent Avenger
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 6
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: While you can spot 3 Ents, this companion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("avenger");

		assertEquals("Ent Avenger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void StrengthPlus2OnceAThirdEntIsPlayedCountingItselfAndAnEntAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avenger = scn.GetFreepsCard("avenger");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(avenger, gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(quickbeam);

		scn.StartGame();

		// Two Ents (the Avenger himself and the ally Treebeard) beside Gandalf, who is no Ent.
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(12, scn.GetStrength(treebeard));
		assertTrue(scn.FreepsPlayAvailable(quickbeam));
		scn.FreepsPlayCard(quickbeam);

		// Three Ents now.  The bonus is the Avenger's alone.
		assertEquals(8, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(quickbeam));
		assertEquals(12, scn.GetStrength(treebeard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoCopiesAmongThreeEntsAreEachStrengthPlus2Only() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avenger = scn.GetFreepsCard("avenger");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var onod = scn.GetFreepsCard("onod");
		scn.MoveCompanionsToTable(avenger, avenger2, onod);

		scn.StartGame();

		// 6 + 2 each, not + 4: each copy strengthens only itself.
		assertEquals(8, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(avenger2));
		assertEquals(7, scn.GetStrength(onod));
	}
}
