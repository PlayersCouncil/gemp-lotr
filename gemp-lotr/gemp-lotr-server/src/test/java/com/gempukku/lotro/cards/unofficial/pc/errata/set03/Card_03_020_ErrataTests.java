package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_020_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("golradir", "53_20");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6, not an archer; her response is only for the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally; her text needs Freeps' site 3 in play
					put("barliman", "1_70");    // Barliman Butterbur: Man ally
					put("eyes", "1_43");        // Far-seeing Eyes: [elven] condition that is not an Elf; acts only when an Elf is played
					put("shoulder", "1_59");    // Shoulder to Shoulder: [elven] support-area condition, a card of the culture that is no Elf; a maneuver ability only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai (not an Orc), strength 5; damage +1
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
		 * Vitality: 2
		 * Home: 3
		 * Game Text: To play, spot an Elf.
		 * Skirmish: Exert Golradir to make a minion strength -1 (or -1 for each Elf you can spot if that minion
		 * is an Orc).
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
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedThenPlaysForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertFalse(scn.FreepsPlayAvailable(golradir));

		scn.FreepsPlayCard(arwen);
		assertEquals(2, scn.GetTwilight());
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

		assertTrue(scn.FreepsPlayAvailable(golradir));
		scn.FreepsPlayCard(golradir);
		assertInZone(Zone.SUPPORT, golradir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsToMakeTheOrcHeIsSkirmishingStrengthMinusOneForEachOfThreeElves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var eyes = scn.GetFreepsCard("eyes");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(golradir, silinde);
		scn.MoveCardsToSupportArea(eyes);

		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, soldier, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, troop);

		assertEquals(0, scn.GetWoundsOn(golradir));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(golradir));
		scn.FreepsUseCardAction(golradir);
		assertEquals(1, scn.GetWoundsOn(golradir));

		// Any minion, in this skirmish or not; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, soldier, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Three Elves: Arwen, Golradir and Silinde. Not 2 companions, 2 allies or 4 [elven] cards.
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionThatIsNotAnOrcIsOnlyStrengthMinusOneWhateverTheElves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var eyes = scn.GetFreepsCard("eyes");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(golradir, silinde);
		scn.MoveCardsToSupportArea(eyes);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, troop);

		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsUseCardAction(golradir);
		// The Uruk-hai is not in this skirmish, nor assigned to any.
		scn.FreepsChooseCard(savage);

		assertEquals(4, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithTwoElvesAnOrcIsStrengthMinusTwoUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eyes = scn.GetFreepsCard("eyes");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToSupportArea(eyes);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, soldier);

		assertEquals(7, scn.GetStrength(soldier));
		scn.FreepsUseCardAction(golradir);

		// Two Elves: Arwen and Golradir. Not 3 companions, 1 ally or 3 [elven] cards.
		assertEquals(5, scn.GetStrength(soldier));
		scn.ShadowPass();
		// The last decision of the skirmish: still in force.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(soldier));
		scn.FreepsPass();

		// Arwen's 6 beat the Orc's 5; it survives its wound and is back to 7 in the regroup phase.
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOrcStaysAtTheStrengthGivenWhenAnElfLeavesPlayAfterwards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(golradir, silinde);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, troop);

		scn.FreepsUseCardAction(golradir);
		// Three Elves
		assertEquals(10, scn.GetStrength(troop));

		scn.MoveCardsToDiscard(silinde);
		// Two Elves now, but the -3 was fixed when Golradir's ability was used.
		assertEquals(10, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnExhaustedGolradirCannotUseHisAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(golradir);
		scn.AddWoundsToChar(golradir, 1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, troop);

		assertFalse(scn.FreepsActionAvailable(golradir));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
