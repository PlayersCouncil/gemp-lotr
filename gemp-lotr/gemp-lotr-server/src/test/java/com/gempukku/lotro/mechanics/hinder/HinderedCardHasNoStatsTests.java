package com.gempukku.lotro.mechanics.hinder;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * A hindered card has no numbers to be found by.
 *
 * Comprehensive Rules 5.0, "hinder": "Numeric stats on hindered cards do not apply.  Do not treat stats such as
 * strength, vitality, or twilight cost as zero, but instead treat them as if the stat doesn't exist."  Its
 * example: "Grond, Hammer of the Underworld, removes X tokens to discard a Free Peoples card with a twilight cost
 * of X.  A hindered Elrond, Lord of Rivendell does not have a twilight cost of 4, neither does he have a twilight
 * cost of 0: he has no twilight cost at all.  Thus Grond is unable to discard a hindered Elrond, even if 0 tokens
 * are removed, as Elrond is not currently a 'Free Peoples card with a twilight cost'."
 *
 * Twilight cost is the one number a printed card reads with no kind of card named beside it, and the rules'
 * example is the first test.  Every printed text that picks a card by its strength, vitality or resistance names
 * its type or race as well ("a companion of strength 6 or less"), and a hindered card has lost those already
 * (HinderedCardCannotBeSpottedTests): no real card can show the three apart from that.  They are kept on a
 * choice of the tests' own making that names the number and nothing else (Ketura, 2026-10-10: "there's engine
 * code that this was used to enforce").
 */
public class HinderedCardHasNoStatsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Grond, Hammer of the Underworld: [sauron] possession, support area.  "Regroup: Remove X
					// [sauron] tokens here to discard a Free Peoples card (except a companion) with a twilight
					// cost of X.  Discard a [sauron] minion or this possession."
					put("grond", "8_103");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai; damage +1 is its whole text

					// Elrond, Lord of Rivendell: ally, cost 4.  At the start of the turn he heals allies of
					// site 3, and nobody here is wounded.
					put("elrond", "1_40");
					put("treebeard", "4_103");  // Treebeard, Earthborn: ally, cost 4; acts only for an unbound Hobbit
					put("guardian", "12_50");   // Guardian: [gondor] condition, cost 4, support area; a skirmish ability
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, cost 1; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: companion, cost 1, no text but a requirement to play
					put("armor1", "1_8");       // Dwarven Armor: possession, cost 0, borne by a Dwarf
					put("armor2", "1_8");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4, resistance 6
					put("boromir", "3_122");    // Boromir, Defender of Minas Tirith: strength 7, vitality 3, resistance 6
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, resistance 6
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4, resistance 5
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Son of Drogo: strength 3 (4 with the Ring), vitality 4, resistance 10
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHinderedCardHasNoTwilightCostNotEvenZero() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToSupportArea(grond);

		var elrond = scn.GetFreepsCard("elrond");
		var treebeard = scn.GetFreepsCard("treebeard");
		var guardian = scn.GetFreepsCard("guardian");
		var thrarin = scn.GetFreepsCard("thrarin");
		var guard = scn.GetFreepsCard("guard");
		var armor1 = scn.GetFreepsCard("armor1");
		var armor2 = scn.GetFreepsCard("armor2");
		scn.MoveAlliesToTable(elrond, treebeard, thrarin);
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, armor1);
		scn.AttachCardsTo(thrarin, armor2);
		scn.MoveCardsToSupportArea(guardian);

		scn.StartGame();
		scn.HinderCard(elrond);
		scn.MoveMinionsToTable(soldier, sentry);
		scn.AddTokensToCard(grond, 8);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(elrond));
		assertEquals(8, scn.GetCultureTokensOn(grond));

		// X is 4.  Treebeard and Guardian cost 4.  Elrond is printed with 4 and, hindered, has no cost.
		// (The armor costs 0 and Thrarin 1.)
		scn.ShadowUseCardAction(grond);
		scn.ShadowChoose("4");
		assertEquals(4, scn.GetCultureTokensOn(grond));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(treebeard, guardian));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(elrond, armor1, armor2, thrarin));
		scn.ShadowChooseCard(treebeard);
		assertInZone(Zone.DISCARD, treebeard);
		// "Discard a [sauron] minion or this possession."
		scn.ShadowChooseCard(soldier);
		assertInZone(Zone.DISCARD, soldier);
		assertInZone(Zone.SUPPORT, elrond, guardian, grond);

		// X is 0.  Nor is the hindered Elrond a card with a twilight cost of 0, as each armor is.
		scn.FreepsPass();
		scn.ShadowUseCardAction(grond);
		scn.ShadowChoose("0");
		assertEquals(4, scn.GetCultureTokensOn(grond));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor1, armor2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(elrond, guardian, thrarin));
		scn.ShadowChooseCard(armor1);
		assertInZone(Zone.DISCARD, armor1);
		scn.ShadowChooseCard(sentry);
		assertInZone(Zone.DISCARD, sentry);
		assertInZone(Zone.SUPPORT, elrond, grond);
		assertTrue(scn.IsHindered(elrond));

		// Restored, he costs 4 again and may be discarded for the four tokens left.  With no [sauron] minion
		// left Grond goes too.
		scn.RestoreCard(elrond);
		scn.FreepsPass();
		scn.ShadowUseCardAction(grond);
		scn.ShadowChoose("4");
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, guardian));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armor2, thrarin));
		scn.ShadowChooseCard(elrond);
		assertInZone(Zone.DISCARD, elrond, grond);
		assertInZone(Zone.SUPPORT, guardian);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHinderedCharacterIsNotFoundByItsStrengthVitalityOrResistance() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam);

		scn.StartGame();

		// Each choice below is of one card in play that the number alone picks out; the card chosen is not
		// changed, and the Free Peoples player is back at the fellowship phase's actions.

		// Strength 6 or more: Aragorn 8, Boromir 7, Gimli 6.  Not Frodo's 4 or Sam's 3.
		scn.FreepsExecuteAdHocEffect(new ChooseActiveCardEffect(null, VirtualTableScenario.P1, "Choose a card of strength 6 or more", scn.GenerateFreepsFilter("minStrength(6)")) { // lint-ok: no printed text picks a card by its strength with no type or race beside it
			@Override
			protected void cardSelected(LotroGame game, PhysicalCard card) { }
		});
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam));
		scn.FreepsChooseCard(aragorn);

		// Vitality 4 or more: Aragorn, Frodo and Sam.  Not Boromir's 3 or Gimli's 3.
		scn.FreepsExecuteAdHocEffect(new ChooseActiveCardEffect(null, VirtualTableScenario.P1, "Choose a card of vitality 4 or more", scn.GenerateFreepsFilter("minVitality(4)")) { // lint-ok: no printed text picks a card by its vitality with no type or race beside it
			@Override
			protected void cardSelected(LotroGame game, PhysicalCard card) { }
		});
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, gimli));
		scn.FreepsChooseCard(aragorn);

		// Resistance 6 or more: Aragorn 6, Boromir 6, Gimli 6, Frodo 10.  Not Sam's 5.
		scn.FreepsExecuteAdHocEffect(new ChooseActiveCardEffect(null, VirtualTableScenario.P1, "Choose a card of resistance 6 or more", scn.GenerateFreepsFilter("minResistance(6)")) { // lint-ok: no printed text picks a card by its resistance with no type or race beside it
			@Override
			protected void cardSelected(LotroGame game, PhysicalCard card) { }
		});
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, gimli, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		scn.FreepsChooseCard(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.HinderCard(aragorn);
		assertTrue(scn.IsHindered(aragorn));

		// Hindered, Aragorn has no strength, vitality or resistance to be found by.  The others are found as
		// before.  (What leaves him out is that a hindered card is not among the cards a choice looks through:
		// the game still reads 8, 4 and 6 for him if asked.  This holds that exclusion for each of the three
		// words, as the old suite did.)
		scn.FreepsExecuteAdHocEffect(new ChooseActiveCardEffect(null, VirtualTableScenario.P1, "Choose a card of strength 6 or more", scn.GenerateFreepsFilter("minStrength(6)")) { // lint-ok: the same choice by strength alone
			@Override
			protected void cardSelected(LotroGame game, PhysicalCard card) { }
		});
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo, sam));
		scn.FreepsChooseCard(boromir);

		scn.FreepsExecuteAdHocEffect(new ChooseActiveCardEffect(null, VirtualTableScenario.P1, "Choose a card of vitality 4 or more", scn.GenerateFreepsFilter("minVitality(4)")) { // lint-ok: the same choice by vitality alone
			@Override
			protected void cardSelected(LotroGame game, PhysicalCard card) { }
		});
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, boromir, gimli));
		scn.FreepsChooseCard(sam);

		scn.FreepsExecuteAdHocEffect(new ChooseActiveCardEffect(null, VirtualTableScenario.P1, "Choose a card of resistance 6 or more", scn.GenerateFreepsFilter("minResistance(6)")) { // lint-ok: the same choice by resistance alone
			@Override
			protected void cardSelected(LotroGame game, PhysicalCard card) { }
		});
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, gimli, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, sam));
		scn.FreepsChooseCard(boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
