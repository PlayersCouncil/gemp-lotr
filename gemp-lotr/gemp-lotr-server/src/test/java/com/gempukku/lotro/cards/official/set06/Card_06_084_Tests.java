package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spied", "6_84");
					// Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl of vitality 3; fierce and damage +1 only at
					// 2 burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl of vitality 3; fierce, and his texts read burdens only
					put("khamul", "103_61");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl of vitality 2, who cannot exert twice; his
					// text is about [wraith] conditions borne
					put("lemenya", "1_232");
					// Morgul Predator: a [wraith] Orc of vitality 3, no Nazgûl; a skirmish ability used only on purpose
					put("predator", "7_196");
					// Morgul Hound: a [wraith] Orc, cost 2; stronger only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Troll of Udûn: a [sauron] Troll, cost 10, vitality 5, site 6; cost -4 only while a Troll can be
					// spotted (none is); a skirmish ability used only on purpose
					put("troll", "6_106");
					// Goblin Patrol Troop: a [moria] Orc, cost 6, no text
					put("troop", "1_177");
					// Orc Banner: a [sauron] condition, cost 1; a trigger on a skirmish lost only
					put("banner", "1_263");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNerteaScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spied", "6_84");
					put("nelya", "2_84");
					put("troll", "6_106");
					// Uruk Axe: [sauron] possession, cost 1, bearer must be a [sauron] minion
					put("axe", "10_102");
					// Úlairë Nertëa, Sanctified for Cruelty: "Each time you play a minion, you may exert it to play a
					// possession on it from your discard pile"; vitality 2, so she cannot pay Spied From Above
					put("nertea", "103_77");
					// Isengard Informant: "Regroup: Exert this minion to play an [orc] Troll from your discard pile."
					put("informant", "13_110");
					// Beastly Olog-hai: [orc] Troll, cost 6, site 5; to play, spot an [orc] minion
					put("olog", "11_108");
					// Orc Spear: [orc] possession, cost 1, bearer must be an [orc] minion
					put("spear", "12_96");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpiedFromAboveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Spied From Above
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * <b>Regroup:</b> Exert a Nazgûl twice to play a [wraith] or [sauron] minion. That minion's twilight cost is -8 and it comes into play exhausted.
		*/

		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied");

		assertEquals("Spied From Above", spied.getBlueprint().getTitle());
		assertNull(spied.getBlueprint().getSubtitle());
		assertFalse(spied.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, spied.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, spied.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, spied.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(spied, Keyword.SUPPORT_AREA));
		assertEquals(1, spied.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityExertsANazgulTwiceToPlayAWraithOrSauronMinionFromHandExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetShadowCard("spied");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var lemenya = scn.GetShadowCard("lemenya");
		var predator = scn.GetShadowCard("predator");
		var hound = scn.GetShadowCard("hound");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToSupportArea(spied);
		scn.MoveCardsToHand(hound, troll, troop, banner);
		assertInHand(hound, troll, troop, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, khamul, lemenya, predator);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Khamul is fierce: a fierce assignment phase, with nobody assigned again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// Enough for the Goblin Patrol Troop at its full cost, 6 + 2 roaming at site 2.
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertTrue(scn.ShadowActionAvailable(spied));
		scn.ShadowUseCardAction(spied);

		// Nazgûl of two cultures who can exert twice; not Lemenya, a Nazgûl of vitality 2, nor the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, predator));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(khamul));

		// A [wraith] Orc and a [sauron] Troll; not a [moria] Orc the pool could pay for, nor a [sauron] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, banner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);

		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		// Vitality 5: exhausted is 4 wounds.
		assertEquals(4, scn.GetWoundsOn(troll));
		assertTrue(scn.IsExhausted(troll));
		// 10 - (10 - 8 + 2 roaming)
		assertEquals(6, scn.GetTwilight());
		// The other minions are untouched, and the cards not chosen stay in hand.
		assertEquals(0, scn.GetWoundsOn(predator));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertInHand(hound, troop, banner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheMinionsTwilightCostIsEightLessSoTheTrollOfUdunCostsTwoAndItsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetShadowCard("spied");
		var nelya = scn.GetShadowCard("nelya");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(spied);
		scn.MoveCardsToHand(troll);
		assertInHand(troll);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// The Troll costs 10 - 8 = 2, and 2 more roaming at site 2: 4.  One short:
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(spied));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(spied));
		scn.ShadowUseCardAction(spied);

		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(4, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoNazgulAbleToExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetShadowCard("spied");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var predator = scn.GetShadowCard("predator");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(spied);
		scn.MoveCardsToHand(troll);
		assertInHand(troll);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya, predator);
		// Nelya, vitality 3, with a wound: she can exert once, not twice.  Lemenya has vitality 2.
		scn.AddWoundsToChar(nelya, 1);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(predator));
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The [wraith] Orc could exert twice, and the Troll could be paid for: only no Nazgûl can pay.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(spied));
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertInHand(troll);
	}

	@Test
	public void NotOfferedUntilAWraithOrSauronMinionIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetShadowCard("spied");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToSupportArea(spied);
		scn.MoveCardsToHand(troop, banner);
		assertInHand(troop, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The play is part of the cost: a [moria] minion and a [sauron] condition in hand give it nothing to play.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(spied));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.MoveCardsToHand(hound);
		scn.ShadowRefreshDecision();
		assertInHand(hound);
		assertTrue(scn.ShadowActionAvailable(spied));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheMinionPlayedIsExhaustedAsItComesIntoPlaySoNerteaCannotExertIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNerteaScenario();

		var spied = scn.GetShadowCard("spied");
		var nelya = scn.GetShadowCard("nelya");
		var troll = scn.GetShadowCard("troll");
		var axe = scn.GetShadowCard("axe");
		var nertea = scn.GetShadowCard("nertea");
		var informant = scn.GetShadowCard("informant");
		var olog = scn.GetShadowCard("olog");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToSupportArea(spied);
		scn.MoveCardsToHand(troll);
		scn.MoveCardsToDiscard(olog, spear, axe);
		assertInHand(troll);
		assertInDiscard(olog, spear, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, nertea, informant);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Nertëa is fierce: a fierce assignment phase, with nobody assigned again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The control: an [orc] Troll played from the discard pile by Isengard Informant comes into play fresh, and
		// Nertëa offers to exert it to play the Orc Spear on it.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowUseCardAction(informant);
		assertInZone(Zone.SHADOW_CHARACTERS, olog);
		assertEquals(0, scn.GetWoundsOn(olog));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(spear, axe);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(spied));
		scn.ShadowUseCardAction(spied);

		// "Comes into play exhausted": by the time its play is answered the Troll cannot exert, so Nertëa has
		// nothing to offer, though the Uruk Axe could go on a [sauron] minion.
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(4, scn.GetWoundsOn(troll));
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertInDiscard(axe);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
