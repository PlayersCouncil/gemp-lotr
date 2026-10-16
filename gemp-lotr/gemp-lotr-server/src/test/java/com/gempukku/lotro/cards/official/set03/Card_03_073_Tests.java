package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trees", "3_73");

					put("worker", "3_62");      // Isengard Worker: [isengard] Orc; a regroup ability used only on purpose
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc; a regroup ability used only on purpose
					put("retainer", "3_57");    // Isengard Retainer: [isengard] Orc; a regroup ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, damage +1 only
					put("scout", "1_191");      // Moria Scout: a [moria] Orc; a trigger on its own play only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3; a skirmish ability only
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, vitality 2; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheTreesAreStrongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Trees Are Strong
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Discard X [isengard] Orcs to make the Free Peoples player wound X companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trees");

		assertEquals("The Trees Are Strong", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardingTwoIsengardOrcsMakesTheFreePeoplesPlayerWoundTwoCompanionsOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);

		var trees = scn.GetShadowCard("trees");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var retainer = scn.GetShadowCard("retainer");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToHand(trees);

		scn.StartGame();
		scn.MoveMinionsToTable(worker, servant, retainer, savage, scout);
		assertInZone(Zone.SHADOW_CHARACTERS, worker, servant, retainer, savage, scout);
		// Vitality 3 with 2 wounds: Gimli is exhausted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(trees));

		scn.ShadowPlayCard(trees);
		// [isengard] Orcs of three titles; not an [isengard] Uruk-hai, nor a [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, servant, retainer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, scout));
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(worker, servant);

		assertInDiscard(worker, servant);
		assertInZone(Zone.SHADOW_CHARACTERS, retainer, savage, scout);

		// X is 2: the Free Peoples player chooses exactly two companions, the exhausted one among them; no ally.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, retainer));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, gimli);

		// A wound and not an exertion: the exhausted Gimli is killed.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertInDiscard(trees);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DiscardingOneIsengardOrcWoundsOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var trees = scn.GetShadowCard("trees");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(trees);

		scn.StartGame();
		scn.MoveMinionsToTable(worker, servant);
		assertInZone(Zone.SHADOW_CHARACTERS, worker, servant);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(trees);
		scn.ShadowChooseCards(worker);

		assertInDiscard(worker);
		assertInZone(Zone.SHADOW_CHARACTERS, servant);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(trees);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void XIsNotHeldToTheCompanionsAndNoCompanionIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var trees = scn.GetShadowCard("trees");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var retainer = scn.GetShadowCard("retainer");
		scn.MoveCardsToHand(trees);

		scn.StartGame();
		scn.MoveMinionsToTable(worker, servant, retainer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Three Orcs may be discarded with two companions to wound.
		scn.ShadowPlayCard(trees);
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(worker, servant, retainer);

		// Wound 3 companions with two there: each is wounded once (CR 4.2 "wound").
		assertInDiscard(worker, servant, retainer);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(trees);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var trees = scn.GetShadowCard("trees");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(trees);

		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		assertInHand(trees);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(trees));
		assertInHand(trees);

		// The same board in the regroup phase: playable.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(trees));
	}
}
