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

public class Card_03_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("golradir", "3_20");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6, not an archer; her response is only for the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, strength 8; a maneuver ability used only on purpose
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally; her text needs Freeps' site 3 in play
					put("barliman", "1_70");    // Barliman Butterbur: Man ally; a fellowship ability used only on purpose
					put("shoulder", "1_59");    // Shoulder to Shoulder: [elven] support-area condition, a card of the culture that is no Elf; a maneuver ability only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai (not an Orc), strength 5, vitality 3; damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GolradirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Golradir, Councilor of Imladris
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: To play, spot an Elf.<br><b>Skirmish:</b> Exert Golradir to make an Orc strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("golradir");

		assertEquals("Golradir", card.getBlueprint().getTitle());
		assertEquals("Councilor of Imladris", card.getBlueprint().getSubtitle());
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

		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var shoulder = scn.GetFreepsCard("shoulder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(arwen, golradir);

		scn.StartGame();

		// A Man companion, a Man ally and an [elven] condition in play, and no Elf.
		assertInZone(Zone.HAND, golradir);
		assertFalse(scn.FreepsPlayAvailable(golradir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(golradir));

		scn.FreepsPlayCard(golradir);
		assertInZone(Zone.SUPPORT, golradir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyIsEnoughToPlayGolradir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(golradir);

		scn.StartGame();

		assertInZone(Zone.HAND, golradir);
		assertTrue(scn.FreepsPlayAvailable(golradir));
		scn.FreepsPlayCard(golradir);
		assertInZone(Zone.SUPPORT, golradir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsToMakeAnOrcInOrOutOfTheSkirmishStrengthMinusOneUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(golradir);

		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, soldier, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, soldier},
				new PhysicalCardImpl[]{arwen, savage});
		// The Troop is left over, and the Shadow player leaves it unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(golradir));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(golradir));
		scn.FreepsUseCardAction(golradir);
		assertEquals(1, scn.GetWoundsOn(golradir));

		// An Orc of either culture, in this skirmish or in none; not the Uruk-hai, and no companion or ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, aragorn, arwen, frodo, golradir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(12, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(savage));
		scn.ShadowPass();
		// The last decision of the skirmish: still in force.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(12, scn.GetStrength(troop));
		scn.FreepsPass();

		// Aragorn's 8 beat the Soldier's 7. In the next skirmish the Troop is back at 13.
		assertEquals(1, scn.GetWoundsOn(soldier));
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnExhaustedGolradirCannotUseHisAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(golradir);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(golradir, 2);
		assertEquals(2, scn.GetWoundsOn(golradir));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, troop);

		// Vitality 3 with 2 wounds: he cannot exert, and an Orc is there to weaken.
		assertEquals(2, scn.GetWoundsOn(golradir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(golradir));
		assertEquals(13, scn.GetStrength(troop));
	}
}
