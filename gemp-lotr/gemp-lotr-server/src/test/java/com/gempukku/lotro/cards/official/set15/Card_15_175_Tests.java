package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_175_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("infantry", "15_175");
					put("infantry2", "15_175");

					// Tyrannical Uruk: an [uruk-hai] Uruk-hai, strength 14, vitality 3; damage +1 and toil 1 (a play
					// matter) only.  A minion well above strength 10.
					put("tyrannical1", "11_204");
					put("tyrannical2", "11_204");
					// Army of Uruk-hai: an [uruk-hai] Uruk-hai, strength 13, vitality 3; its text is on archers only.
					put("army", "11_177");
					// Saruman, Agent of the Dark Lord: an [uruk-hai] minion who is no Uruk-hai (a Wizard), vitality 4.
					// His skirmish ability and his heal on a companion's death are never used.
					put("saruman", "12_144");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2, damage +1 only.
					put("guard", "10_81");
					// Weapons of Control: an [uruk-hai] support-area condition, no minion.  Its ability is never used.
					put("weapons", "13_177");

					// Aragorn, Ranger of the North: a [gondor] companion of strength 8; Ranger's Sword (bearer must be
					// Aragorn) makes him 10.  His maneuver ability is never used.
					put("aragorn", "1_89");
					put("sword", "1_112");
					// Gimli, Son of Gloin: a [dwarven] companion of strength 6.  His skirmish ability is never used.
					put("gimli", "1_13");
					// Thrarin, Dwarven Smith: a Dwarf ally of strength 4.  His ability is never used.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukInfantryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Uruk Infantry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While you can spot a companion of strength 10 or more, this minion is strength +2.<br><b>Regroup:</b> Exert 4 [uruk-hai] minions (or discard 3 [uruk-hai] minions) to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("infantry");

		assertEquals("Uruk Infantry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus2OnceACompanionReachesStrength10() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sword);

		var infantry = scn.GetShadowCard("infantry");
		var infantry2 = scn.GetShadowCard("infantry2");
		var tyrannical = scn.GetShadowCard("tyrannical1");

		scn.StartGame();
		// Minions laid in the fellowship phase stand as they will later in the turn (Q-0228).
		scn.MoveMinionsToTable(infantry, infantry2, tyrannical);
		scn.FreepsRefreshDecision();

		// A minion of strength 14 is no companion.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(14, scn.GetStrength(tyrannical));
		assertEquals(8, scn.GetStrength(infantry));
		assertEquals(8, scn.GetStrength(infantry2));

		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);

		// 8 + 2 for the Sword: a companion of exactly 10.
		assertEquals(10, scn.GetStrength(aragorn));
		// Each copy +2 for itself, not +2 for each copy of the title.
		assertEquals(10, scn.GetStrength(infantry));
		assertEquals(10, scn.GetStrength(infantry2));
		// The other minion is not made stronger.
		assertEquals(14, scn.GetStrength(tyrannical));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotStrongerBesideACompanionOfStrength9OrAnAllyOfStrength10() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var infantry = scn.GetShadowCard("infantry");

		scn.StartGame();
		scn.MoveMinionsToTable(infantry);
		// Numbers given by hand only to make the board: Aragorn one short of 10, the ally at 10.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 1));
		scn.ApplyAdHocModifier(new StrengthModifier(null, thrarin, null, 6));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(infantry));

		// A [dwarven] companion brought to 10 switches it on.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 4));
		assertEquals(10, scn.GetStrength(gimli));
		assertEquals(10, scn.GetStrength(infantry));
	}

	@Test
	public void BothCostsPayableAsksWhichAndExerting4UrukHaiMinionsTakesControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var tyrannical1 = scn.GetShadowCard("tyrannical1");
		var tyrannical2 = scn.GetShadowCard("tyrannical2");
		var army = scn.GetShadowCard("army");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var weapons = scn.GetShadowCard("weapons");

		scn.StartGame();
		// Player 1's second turn: both markers on site 2, and site 1 behind them.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(weapons);
		scn.MoveMinionsToTable(infantry, tyrannical1, tyrannical2, army, saruman, guard);
		assertInZone(Zone.SUPPORT, weapons);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.ShadowControls(site1));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(infantry));
		scn.ShadowUseCardAction(infantry);

		// The parenthesis has a price of its own: the player chooses (Q-0089, Q-0308).
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Exert");

		assertEquals(4, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, tyrannical1, tyrannical2, army, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		scn.ShadowChooseCards(infantry, tyrannical1, army, saruman);

		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(1, scn.GetWoundsOn(tyrannical1));
		assertEquals(1, scn.GetWoundsOn(army));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(tyrannical2));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void Discarding3UrukHaiMinionsTakesControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var tyrannical1 = scn.GetShadowCard("tyrannical1");
		var tyrannical2 = scn.GetShadowCard("tyrannical2");
		var army = scn.GetShadowCard("army");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var weapons = scn.GetShadowCard("weapons");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(weapons);
		scn.MoveMinionsToTable(infantry, tyrannical1, tyrannical2, army, saruman, guard);
		assertInZone(Zone.SUPPORT, weapons);
		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.ShadowControls(site1));
		scn.FreepsPass();
		scn.ShadowUseCardAction(infantry);
		scn.ShadowChooseOption("Discard");

		assertEquals(3, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, tyrannical1, tyrannical2, army, saruman));
		// Neither the [sauron] Uruk-hai nor the [uruk-hai] condition.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, weapons));
		scn.ShadowChooseCards(tyrannical1, army, saruman);

		assertInZone(Zone.DISCARD, tyrannical1, army, saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, infantry, tyrannical2, guard);
		assertInZone(Zone.SUPPORT, weapons);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnly3UrukHaiMinionsAbleToExertTheDiscardIsMadeWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var tyrannical1 = scn.GetShadowCard("tyrannical1");
		var tyrannical2 = scn.GetShadowCard("tyrannical2");
		var army = scn.GetShadowCard("army");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(infantry, tyrannical1, tyrannical2, army);
		// Vitality 3 with 2 wounds: exhausted, so only three of the four can exert.
		scn.AddWoundsToChar(army, 2);
		assertEquals(2, scn.GetWoundsOn(army));
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowUseCardAction(infantry);

		// No choice of costs: the discard is the one that can be paid.
		assertTrue(scn.ShadowDecisionAvailable("discard"));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, tyrannical1, tyrannical2, army));
		scn.ShadowChooseCards(tyrannical1, tyrannical2, army);

		assertInZone(Zone.DISCARD, tyrannical1, tyrannical2, army);
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoUrukHaiMinionsBesideASauronUrukHaiAndAnUrukHaiCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var tyrannical1 = scn.GetShadowCard("tyrannical1");
		var guard = scn.GetShadowCard("guard");
		var weapons = scn.GetShadowCard("weapons");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(weapons);
		scn.MoveMinionsToTable(infantry, tyrannical1, guard);
		assertInZone(Zone.SUPPORT, weapons);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		assertInZone(Zone.SHADOW_CHARACTERS, infantry, tyrannical1, guard);
		assertInZone(Zone.SUPPORT, weapons);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(infantry));
	}

	@Test
	public void OnTheFirstTurnNoSiteCanBeTakenAndTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var tyrannical1 = scn.GetShadowCard("tyrannical1");
		var tyrannical2 = scn.GetShadowCard("tyrannical2");

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, tyrannical1, tyrannical2);
		scn.SkipToPhase(Phase.REGROUP);

		// The Shadow player's marker is still on site 1: no site is behind every marker.
		assertEquals(1, scn.ShadowGetSiteNumber());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(infantry));
		scn.ShadowUseCardAction(infantry);

		assertInZone(Zone.DISCARD, infantry, tyrannical1, tyrannical2);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
