package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saelbeth", "3_25");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6, not an archer; her response is only for the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, strength 8; a maneuver ability used only on purpose
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally; her text needs Freeps' site 3 in play
					put("barliman", "1_70");    // Barliman Butterbur: Man ally; a fellowship ability used only on purpose
					put("shoulder", "1_59");    // Shoulder to Shoulder: [elven] support-area condition, a card of the culture that is no Elf; a maneuver ability only

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; damage +1
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, the race nearest Uruk-hai, strength 7, vitality 2; a skirmish ability used only on purpose
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard, the culture without the race; strength 8;
					// each companion is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SaelbethStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Saelbeth, Elven Councilor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: To play, spot an Elf.<br><b>Skirmish:</b> Exert Saelbeth to make an Uruk-hai strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saelbeth");

		assertEquals("Saelbeth", card.getBlueprint().getTitle());
		assertEquals("Elven Councilor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedThenPlaysToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var shoulder = scn.GetFreepsCard("shoulder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(arwen, saelbeth);

		scn.StartGame();

		// A Man companion, a Man ally and an [elven] condition in play, and no Elf.
		assertInZone(Zone.HAND, saelbeth);
		assertFalse(scn.FreepsPlayAvailable(saelbeth));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(saelbeth));

		scn.FreepsPlayCard(saelbeth);
		assertInZone(Zone.SUPPORT, saelbeth);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyIsEnoughToPlaySaelbeth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(saelbeth);

		scn.StartGame();

		assertInZone(Zone.HAND, saelbeth);
		assertTrue(scn.FreepsPlayAvailable(saelbeth));
		scn.FreepsPlayCard(saelbeth);
		assertInZone(Zone.SUPPORT, saelbeth);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsToMakeAnUrukHaiInOrOutOfTheSkirmishStrengthMinusOneUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(saelbeth);

		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var soldier = scn.GetShadowCard("soldier");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, soldier, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{arwen, soldier});
		// The Guard and Saruman are left over, and the Shadow player leaves them unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(saelbeth));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.FreepsActionAvailable(saelbeth));
		scn.FreepsUseCardAction(saelbeth);
		assertEquals(1, scn.GetWoundsOn(saelbeth));

		// An Uruk-hai of either culture, in this skirmish or in none; not the Orc, not the [uruk-hai] Wizard, and no
		// companion or ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier, saruman, aragorn, arwen, frodo, saelbeth));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertEquals(8, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(saruman));
		scn.ShadowPass();
		// The last decision of the skirmish: still in force.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(guard));
		scn.FreepsPass();

		// Aragorn's 8 beat the Savage's 5. In the next skirmish the Guard is back at 9.
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUrukHaiInPlayHeIsStillExertedAndNothingIsWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(saelbeth);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, soldier);

		// The only minion is an Orc.  The target is no requirement (CR 4.2 "effect"): the ability is on offer, its
		// cost is paid, and nothing is weakened.
		assertEquals(0, scn.GetWoundsOn(saelbeth));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.FreepsActionAvailable(saelbeth));
		scn.FreepsUseCardAction(saelbeth);

		assertEquals(1, scn.GetWoundsOn(saelbeth));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnExhaustedSaelbethCannotUseHisAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(saelbeth);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(saelbeth, 2);
		assertEquals(2, scn.GetWoundsOn(saelbeth));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, savage);

		// Vitality 3 with 2 wounds: he cannot exert, and an Uruk-hai is there to weaken.
		assertEquals(2, scn.GetWoundsOn(saelbeth));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(saelbeth));
		assertEquals(5, scn.GetStrength(savage));
	}
}
