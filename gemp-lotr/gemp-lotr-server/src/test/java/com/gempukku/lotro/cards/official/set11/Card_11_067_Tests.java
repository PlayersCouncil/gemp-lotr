package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archer", "11_67");
					// Sam, Son of Hamfast: [shire] Hobbit companion, resistance 5.  "Fellowship: Exert Sam to remove a burden."
					put("sam", "1_311");
					// Aragorn, Ranger of the North: [gondor] Man companion, resistance 6; a maneuver ability only
					put("aragorn", "1_89");
					// Barliman Butterbur: [gandalf] ally, which has resistance 0 (CR 4.2 "resistance")
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArcherofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Archer of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.<br>While you can spot a companion who has resistance 4 or less, the minion archery total is +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("archer");

		assertEquals("Archer of Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileSamHasResistance4TheMinionArcheryTotalIsPlusOneAndNotOnceHisBurdenIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(archer);

		assertEquals(1, scn.GetBurdens());
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(4, scn.GetResistance(sam));
		// 1 for the Archer's own keyword, +1 while Sam is at resistance 4
		assertEquals(2, scn.GetShadowArcheryTotal());

		// Sam's own ability takes the burden off, and him to resistance 5.
		scn.FreepsUseCardAction(sam);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AManCompanionAtResistance4CountsWhereAnAllyAndTheMinionItselfDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(archer);

		// No companion is at 4 or less: the ally at 0 and the minion are no companions.
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetResistance(barliman));
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.AddBurdens(1);

		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
