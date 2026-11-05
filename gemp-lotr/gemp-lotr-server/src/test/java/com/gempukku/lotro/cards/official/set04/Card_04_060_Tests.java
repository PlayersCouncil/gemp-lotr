package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blades", "4_60");

					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					put("haldir", "1_48");     // Haldir, Elf of the Golden Wood: Elf companion; +2 only at sites 6 to 8
					put("celeborn", "1_34");   // Celeborn, Lord of Lorien: Elf ally, cost 2; a fellowship ability used only on purpose
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("defend", "4_115");    // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion once (twice if an Uruk-hai)
					put("stairs", "1_24");     // Stairs of Khazad-dum: a Free Peoples support condition; +1 only to a Dwarf against a [moria] minion

					put("troop", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("scout", "1_191");     // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladesDrawnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Blades Drawn
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 3 Elves. Plays to your support area.<br>For each wound on each minion, that minion is strength -2. Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blades");

		assertEquals("Blades Drawn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithTwoElvesButCanOnceAnElfAllyIsPlayedAsTheThird() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blades = scn.GetFreepsCard("blades");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Two Elf companions, and Aragorn and Frodo, who are no Elves.
		scn.MoveCompanionsToTable(arwen, haldir, aragorn);
		scn.MoveCardsToHand(blades, celeborn);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(blades));

		// An Elf ally is the third Elf.
		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.FreepsPlayAvailable(blades));
		scn.FreepsPlayCard(blades);
		assertInZone(Zone.SUPPORT, blades);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionIsStrengthMinus2ForEachWoundOnItAndAWoundedCompanionIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blades = scn.GetFreepsCard("blades");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(blades);
		scn.MoveCardsToHand(defend);
		scn.AddWoundsToChar(aragorn, 1);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, scout);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(savage, 2);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(scout));
		// The [moria] Orc: 13 - 2 for its one wound.  The [isengard] Uruk-hai: 5 - 4 for its two.
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(1, scn.GetStrength(savage));
		// An unwounded minion and a wounded companion are as printed.
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(8, scn.GetStrength(aragorn));

		// A second wound on the Troop, dealt by play: 13 - 4 at once.
		scn.FreepsPlayCard(defend);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(1, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void StaysThroughTheSkirmishAndIsDiscardedAsTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blades = scn.GetFreepsCard("blades");
		var stairs = scn.GetFreepsCard("stairs");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(blades, stairs);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, blades);
		assertInZone(Zone.SUPPORT, stairs);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);

		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// In the skirmish, the phase before regroup: still in play, the Troop 13 - 2.
		assertInZone(Zone.SUPPORT, blades);
		assertEquals(11, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// The Troop (11) beats Aragorn (8) and keeps its wound.  As the regroup phase starts the condition is
		// discarded, and the Troop is as printed; the other Free Peoples support condition stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(blades);
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(13, scn.GetStrength(troop));
	}
}
