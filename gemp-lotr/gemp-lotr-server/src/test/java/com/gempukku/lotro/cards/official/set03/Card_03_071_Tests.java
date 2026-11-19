package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tower", "3_71");

					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability only
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, damage +1 only
					put("scout", "1_191");      // Moria Scout: a [moria] Orc; a trigger on its own play only
					put("armory", "1_157");     // Uruk-hai Armory: an [isengard] condition (support area); lowers the archery total
					put("warrior", "1_185");    // Goblin Warrior: [moria] Orc, vitality 1 (always exhausted); a trigger on its own play only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, vitality 2; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TowerofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Tower of Orthanc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [isengard] minion. Plays to your support area.<br>Each time the fellowship moves, you may spot an exhausted companion to add (3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tower");

		assertEquals("Tower of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnIsengardMinionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var worker = scn.GetShadowCard("worker");
		var scout = scn.GetShadowCard("scout");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(tower, worker);
		scn.MoveCardsToSupportArea(armory);

		scn.StartGame();
		scn.SetTwilight(10);
		// Laid ahead of the Shadow phase, so that its first decision is made with it on the table.
		scn.MoveMinionsToTable(scout);
		assertInZone(Zone.SUPPORT, armory);
		assertInZone(Zone.SHADOW_CHARACTERS, scout);
		scn.FreepsPass();

		// A [moria] minion and an [isengard] card that is no minion: not enough.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(tower));

		scn.ShadowPlayCard(worker);
		assertTrue(scn.ShadowPlayAvailable(tower));
		scn.ShadowPlayCard(tower);

		assertInZone(Zone.SUPPORT, tower);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnIsengardUrukHaiMayBeSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(tower);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertTrue(scn.ShadowPlayAvailable(tower));
		scn.ShadowPlayCard(tower);

		assertInZone(Zone.SUPPORT, tower);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingWithAnExhaustedCompanionAddsThreeBeforeTheMovesOwnTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tower = scn.GetShadowCard("tower");
		scn.MoveCardsToSupportArea(tower);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tower));
		// The move's own twilight is not in yet, and the marker is still on the site left.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetCurrentSiteNumber());
		scn.ShadowAcceptOptionalTrigger();

		// 3 from the Tower + 2 for Ettenmoors + 2 companions.
		assertEquals(7, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheAddIsOfferedAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tower = scn.GetShadowCard("tower");
		scn.MoveCardsToSupportArea(tower);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tower));
		scn.ShadowDeclineOptionalTrigger();

		// 2 for Ettenmoors + 2 companions, and nothing from the Tower.
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedForAWoundedCompanionOrAnExhaustedAllyOrMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		var tower = scn.GetShadowCard("tower");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToSupportArea(tower);

		scn.StartGame();
		// Aragorn has 2 vitality left, one more than exhausted; Barliman, vitality 2 with 1 wound, is exhausted.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(barliman, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tower));
		// 2 for Ettenmoors + 2 companions.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Goblin Warrior, of vitality 1, is an exhausted minion at the regroup move.
		scn.MoveMinionsToTable(warrior);
		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertEquals(4, scn.GetTwilight());

		scn.FreepsChooseToMove();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tower));
		// 4 + 0 for Rivendell Valley + 2 companions.
		assertEquals(6, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tower = scn.GetShadowCard("tower");
		scn.MoveCardsToSupportArea(tower);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// The fellowship stays where it is: the turn ends with nothing offered, and the other player's begins.
		scn.FreepsChooseToStay();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tower));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AnExhaustedRingBearerIsSpottedAtEachMoveOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var tower = scn.GetShadowCard("tower");
		scn.MoveCardsToSupportArea(tower);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tower));
		scn.ShadowAcceptOptionalTrigger();
		// 3 from the Tower + 2 for Ettenmoors + 1 companion.
		assertEquals(6, scn.GetTwilight());

		scn.SkipToMovementDecision();
		assertEquals(6, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tower));
		assertEquals(6, scn.GetTwilight());
		scn.ShadowAcceptOptionalTrigger();

		// 6 + 3 from the Tower + 0 for Rivendell Valley + 1 companion.
		assertEquals(10, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
