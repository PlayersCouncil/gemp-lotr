package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_303_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "51_303");

					put("sword", "1_299");      // Hobbit Sword: hand weapon, strength +2, bearer must be a Hobbit, no other text
					put("pan", "3_108");        // Frying Pan: hand weapon for a Hobbit; here it stays in hand
					put("pipe", "1_285");       // Bilbo's Pipe: a pipe, not a weapon, bearer must be a Hobbit; a fellowship ability used only on purpose
					put("bow", "1_41");         // Elven Bow: ranged weapon, bearer is an archer; Elf only, so it is cheated onto Merry
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("rsword", "1_112");     // Ranger's Sword: hand weapon, strength +2, Aragorn is damage +1
					put("rbow", "4_131");       // Ranger's Bow: ranged weapon that makes nobody an archer; a skirmish ability used only on purpose

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier2", "1_271");
					put("scimitar", "1_269");   // Orc Scimitar: Shadow hand weapon, strength +2; a skirmish ability used only on purpose
					put("scimitar2", "1_269");
					put("smith", "3_60");       // Isengard Smith: Orc; "Regroup: Exert this minion to discard a weapon (or all Free Peoples possessions if you can spot 5 companions)."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Merry, From O'er the Brandywine
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: While Merry bears a weapon, he is strength +2.
		 * Skirmish: Exert Merry to make him strength +1 for each hand weapon you can spot (limit +3).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("From O'er the Brandywine", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void PlaysForOneAndIsStrength2MoreOnlyOnceHeBearsAWeaponHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var pipe = scn.GetFreepsCard("pipe");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		var bow = scn.GetFreepsCard("bow");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(merry, pipe, sword);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rsword);
		// Frodo, another Hobbit, bears a weapon all the while: Merry's bonus is not every Hobbit's. The Elven Bow
		// is cheated on (no ranged weapon may be played on a Hobbit) and changes no strength.
		scn.AttachCardsTo(frodo, bow);

		scn.StartGame();

		// Frodo is strength 3, +1 from The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(merry);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, merry);
		// Aragorn's weapon is not Merry's.
		assertEquals(3, scn.GetStrength(merry));

		scn.FreepsPlayCard(pipe);
		scn.FreepsChooseCard(merry);
		assertAttachedTo(pipe, merry);
		// A pipe is not a weapon.
		assertEquals(3, scn.GetStrength(merry));

		scn.FreepsPlayCard(sword);
		scn.FreepsChooseCard(merry);
		assertAttachedTo(sword, merry);
		// 3 + 2 from the sword + 2 for bearing a weapon
		assertEquals(7, scn.GetStrength(merry));
		// Frodo is strength 3, +1 from The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARangedWeaponAloneMakesHimStrength2MoreAndAHandWeaponBesideItAddsOnlyItsOwnBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var bow = scn.GetFreepsCard("bow");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(merry);
		// No ranged weapon may be played on a Hobbit, so the Elven Bow is cheated on. It changes no strength.
		scn.AttachCardsTo(merry, bow);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		assertEquals(5, scn.GetStrength(merry));

		scn.FreepsPlayCard(sword);
		scn.FreepsChooseCard(merry);
		// 3 + 2 from the sword + 2 for bearing a weapon, once for two weapons
		assertEquals(7, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenHisWeaponIsDiscardedTheBonusIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		var smith = scn.GetShadowCard("smith");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, sword);
		scn.AttachCardsTo(aragorn, rsword);

		scn.StartGame();
		scn.MoveMinionsToTable(smith);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(merry));
		assertTrue(scn.ShadowActionAvailable(smith));

		scn.ShadowUseCardAction(smith);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, rsword));
		scn.ShadowChooseCard(sword);
		assertInZone(Zone.DISCARD, sword);
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsMerryToMakeHimStrength1MoreForEachHandWeaponInPlayWhoeverBearsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var pipe = scn.GetFreepsCard("pipe");
		var pan = scn.GetFreepsCard("pan");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rbow = scn.GetFreepsCard("rbow");
		var soldier = scn.GetShadowCard("soldier");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, sword, pipe);
		// Aragorn is not Ring-bound, so the bow is cheated on: a ranged weapon, not a hand weapon.
		scn.AttachCardsTo(aragorn, rbow);
		scn.MoveCardsToHand(pan);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, scimitar);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		// Two hand weapons, his own and the Orc's: 7 + 2
		assertEquals(9, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyAnotherCompanionsHandWeaponInPlayHeIsMadeStrength1More() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(aragorn, rsword);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(10, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(4, scn.GetStrength(merry));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThePlus3IsForTheWholePhaseAndAtItTheAbilityStillExertsMerryForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, sword);
		scn.AttachCardsTo(aragorn, rsword);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(7, scn.GetStrength(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(9, scn.GetStrength(merry));
		scn.ShadowPass();

		// Two hand weapons again, but only +1 is left of the +3.
		scn.FreepsUseCardAction(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(10, scn.GetStrength(merry));
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(merry));
		scn.FreepsUseCardAction(merry);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(10, scn.GetStrength(merry));
		assertEquals(10, scn.GetStrength(aragorn));
		scn.ShadowPass();

		// Vitality 4 with 3 wounds: exhausted, so he can no longer exert.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void TheBonusLastsOnlyItsSkirmishPhaseAndThePlus3IsThereAgainInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var scimitar = scn.GetShadowCard("scimitar");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, sword);
		scn.AttachCardsTo(aragorn, rsword);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, soldier2);
		scn.AttachCardsTo(soldier, scimitar);
		scn.AttachCardsTo(soldier2, scimitar2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, soldier}, new PhysicalCardImpl[]{aragorn, soldier2});
		scn.FreepsResolveSkirmish(merry);

		assertEquals(7, scn.GetStrength(merry));
		scn.FreepsUseCardAction(merry);
		// Four hand weapons in play: +3 at most.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(10, scn.GetStrength(merry));

		// Merry 10 against the Orc's 9.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(soldier));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(merry));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(10, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
