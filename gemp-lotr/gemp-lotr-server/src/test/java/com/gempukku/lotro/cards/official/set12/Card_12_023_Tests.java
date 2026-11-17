package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seclusion", "12_23");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgûl
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: an Elf companion of another title; +2 at sites 6 to 8
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: Elf ally; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] companion who is no Elf
					// Betrayal of Isengard: the player's own [gandalf] support-area condition; its texts answer a
					// Shadow-phase draw and are used by Gandalf, neither of which comes up here.
					put("betrayal", "3_29");
					put("cloak", "1_42");       // Elven Cloak: a Free Peoples possession on a companion; minion archery total -1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("spear", "1_182");      // Goblin Spear: a Shadow possession on a [moria] minion; a kill trigger only
					// Goblin Swarms: [moria] support-area condition; a response to a [moria] Orc's win and a Shadow play
					put("swarms", "1_183");
					// Weary: [isengard] condition borne by a companion; its trigger answers an assignment to a tracker
					put("weary", "4_212");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeclusionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Seclusion
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Toil 2</b>. <helper>(For each [elven] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot an Elf companion to discard a condition from play.
		*/

		var scn = GetScenario();

		var seclusion = scn.GetFreepsCard("seclusion");

		assertEquals("Seclusion", seclusion.getBlueprint().getTitle());
		assertNull(seclusion.getBlueprint().getSubtitle());
		assertFalse(seclusion.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, seclusion.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, seclusion.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, seclusion.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(seclusion, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(seclusion, Keyword.TOIL));
		assertTrue(scn.HasTimeword(seclusion, Timeword.MANEUVER));
		assertEquals(3, seclusion.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedByToilExertingOneElvenCharacterItAddsOneAndDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seclusion = scn.GetFreepsCard("seclusion");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var swarms = scn.GetShadowCard("swarms");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(seclusion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(swarms);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(seclusion);
		assertInZone(Zone.SUPPORT, swarms);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(seclusion));

		scn.FreepsPlayCard(seclusion);
		// Toil: any number of [elven] characters, the ally among them; nothing is owed, the cost being added.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		scn.FreepsChooseCards(celeborn);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		// 3 - 2 for the one exertion
		assertEquals(twilight + 1, scn.GetTwilight());
		// The one condition in play is discarded.
		assertInDiscard(swarms, seclusion);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardsAConditionOfEitherPlayerWhereverItIsAndNoPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seclusion = scn.GetFreepsCard("seclusion");
		var arwen = scn.GetFreepsCard("arwen");
		var betrayal = scn.GetFreepsCard("betrayal");
		var cloak = scn.GetFreepsCard("cloak");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var swarms = scn.GetShadowCard("swarms");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, cloak);
		scn.MoveCardsToSupportArea(betrayal);
		scn.MoveCardsToHand(seclusion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);
		scn.AttachCardsTo(arwen, weary);
		scn.MoveCardsToSupportArea(swarms);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(seclusion);
		assertAttachedTo(cloak, arwen);
		assertAttachedTo(weary, arwen);
		assertAttachedTo(spear, troop);
		assertInZone(Zone.SUPPORT, betrayal, swarms);
		assertTrue(scn.FreepsPlayAvailable(seclusion));

		scn.FreepsPlayCard(seclusion);
		// No exertion for toil.
		scn.FreepsDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(arwen));

		// A Shadow condition in a support area, a Shadow condition a companion bears, and the player's own condition:
		// one of them, and no possession.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(swarms, weary, betrayal));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(cloak, spear));
		scn.FreepsChooseCard(weary);

		assertInDiscard(weary, seclusion);
		assertInZone(Zone.SUPPORT, betrayal, swarms);
		assertAttachedTo(cloak, arwen);
		assertAttachedTo(spear, troop);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAnElfAllyAndNoElfCompanionUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seclusion = scn.GetFreepsCard("seclusion");
		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		var swarms = scn.GetShadowCard("swarms");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(seclusion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(swarms);
		scn.SkipToPhase(Phase.MANEUVER);

		// An Elf ally, and a companion who is no Elf: no Elf companion to spot.
		assertInHand(seclusion);
		assertInZone(Zone.SUPPORT, swarms);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(seclusion));

		// Haldir, an Elf companion of another title than the other tests', is spotted.
		scn.MoveCompanionsToTable(haldir);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(seclusion));

		scn.FreepsPlayCard(seclusion);
		scn.FreepsDeclineChoosing();
		assertInDiscard(swarms, seclusion);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seclusion = scn.GetFreepsCard("seclusion");
		var arwen = scn.GetFreepsCard("arwen");
		var cloak = scn.GetFreepsCard("cloak");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, cloak);
		scn.MoveCardsToHand(seclusion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(seclusion);
		assertAttachedTo(cloak, arwen);
		assertAttachedTo(spear, troop);
		assertTrue(scn.FreepsPlayAvailable(seclusion));

		scn.FreepsPlayCard(seclusion);
		scn.FreepsDeclineChoosing();

		// The target is no requirement: played, and nothing discarded.
		assertInDiscard(seclusion);
		assertAttachedTo(cloak, arwen);
		assertAttachedTo(spear, troop);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
