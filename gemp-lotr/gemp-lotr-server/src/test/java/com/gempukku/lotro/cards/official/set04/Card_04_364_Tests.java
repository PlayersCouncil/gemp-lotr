package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_364_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "4_364");
					put("sam", "1_311");
					put("merry", "1_303");
					put("pippin", "1_306");
					put("gaffer", "1_291");   // Hobbit ally: must never count as an unbound Hobbit
					put("helpless", "2_76");  // "Sam's game text does not apply"

					put("troop1", "1_177");
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Aragorn, Wingfoot
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Ranger</b>.<br>Each time the fellowship moves, you may wound a minion for each unbound Hobbit you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Wingfoot", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void MovingWoundsOneMinionPerUnboundHobbitSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(aragorn, sam, merry, pippin);
		scn.MoveCardsToSupportArea(gaffer);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveMinionsToTable(troop1, troop2);

		scn.StartGame();

		// Frodo (Ring-bearer) and Sam are Ring-bound by rule; the Gaffer is an ally, not a companion.
		assertTrue(scn.HasKeyword(scn.GetRingBearer(), Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertFalse(scn.HasKeyword(merry, Keyword.RING_BOUND));
		assertFalse(scn.HasKeyword(pippin, Keyword.RING_BOUND));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));

		// Passing the fellowship phase at site 1 forces the move to site 2
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Only Merry and Pippin are unbound Hobbits
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsDecided(2);

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2));
		scn.FreepsChooseCard(troop1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		scn.FreepsChooseCard(troop2);
		assertEquals(1, scn.GetWoundsOn(troop2));

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingTriggerIsOptional() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveMinionsToTable(troop1);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SamIsStillRingBoundWhenHisGameTextIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1061: Helpless ("Sam's game text does not apply") was stripping Sam's rule-granted
		// Ring-bound status, so he was being counted as an unbound Hobbit.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var helpless = scn.GetShadowCard("helpless");
		scn.MoveCompanionsToTable(aragorn, sam, merry, pippin);
		scn.AttachCardsTo(sam, helpless);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveMinionsToTable(troop1, troop2);

		scn.StartGame();

		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsDecided(2);
		scn.FreepsChooseCard(troop1);
		scn.FreepsChooseCard(troop2);

		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
