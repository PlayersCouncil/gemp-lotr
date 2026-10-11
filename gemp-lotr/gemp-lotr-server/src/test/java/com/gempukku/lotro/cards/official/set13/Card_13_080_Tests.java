package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("radagast", "13_80");
					put("alatar", "13_78");     // Alatar Deceived: a second Shadow condition in the support area; its own text answers a Wizard's win only
					put("saruman", "12_144");   // Saruman, Agent of the Dark Lord: an [uruk-hai] Wizard minion, vitality 4, lurker; each time a companion is killed, you may heal Saruman
					put("staff", "13_81");      // Staff of Saruman: [isengard] artifact, "Bearer must be a Wizard"; its regroup ability is never used here
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text

					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: vitality 4; a fellowship ability never used here
					put("mithrandir", "6_30");  // Gandalf, Mithrandir: each time the fellowship moves, wound Gandalf
					put("brown", "31_17");      // Radagast, The Brown: a Wizard who is an ally, vitality 4; a fellowship ability never used here
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard, vitality 4
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally; there for Master of Healing to spot
					put("healing1", "3_22");    // Master of Healing: Fellowship or Regroup, spot Elrond to heal a companion or ally
					put("healing2", "3_22");
					put("healing3", "3_22");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: a second [gondor] Man; a skirmish ability never used here
					put("defend", "4_115");     // Defend It and Hope: Maneuver, exert a [gondor] Man to wound a minion (twice if an Uruk-hai)
					put("level", "7_92");       // First Level: Maneuver, exert a [gondor] Man to transfer it from the support area to a minion
					put("homestead", "13_42");  // Traveler's Homestead: Maneuver, discard it from play to discard a Shadow condition from play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RadagastDeceivedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Radagast Deceived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a Wizard heals, this condition becomes a <b>fierce</b> Wizard minion until the start of the regroup phase that has 12 strength and 1 vitality, and cannot take wounds or bear other cards. This card is still a condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("radagast");

		assertEquals("Radagast Deceived", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromHandForThreeToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetShadowCard("radagast");
		scn.MoveCardsToHand(radagast);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, radagast);
		assertTrue(scn.ShadowPlayAvailable(radagast));

		scn.ShadowPlayCard(radagast);

		assertEquals(twilight - 3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, radagast);
		assertNotAttachedTo(radagast, scn.GetRingBearer());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHealedGandalfMakesItAFierceWizardMinionAndAHealedEntDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetShadowCard("radagast");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var healing2 = scn.GetFreepsCard("healing2");
		var healing3 = scn.GetFreepsCard("healing3");

		scn.MoveCardsToSupportArea(radagast);
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1, healing2, healing3);

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(avenger, 1);

		// A [gandalf] companion who is no Wizard heals.
		scn.FreepsPlayCard(healing1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger));
		scn.FreepsChooseCard(avenger);
		assertEquals(0, scn.GetWoundsOn(avenger));

		assertInZone(Zone.SUPPORT, radagast);
		assertFalse(scn.IsType(radagast, CardType.MINION));
		assertFalse(scn.IsRace(radagast, Race.WIZARD));
		assertFalse(scn.HasKeyword(radagast, Keyword.FIERCE));
		assertEquals(0, scn.GetStrength(radagast));
		assertEquals(0, scn.GetVitality(radagast));

		// Gandalf, the only one still wounded, heals one of his two wounds.
		scn.FreepsPlayCard(healing2);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		assertInZone(Zone.SHADOW_CHARACTERS, radagast);
		assertTrue(scn.IsType(radagast, CardType.MINION));
		assertTrue(scn.IsRace(radagast, Race.WIZARD));
		assertTrue(scn.HasKeyword(radagast, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(radagast, Keyword.DAMAGE));
		assertEquals(12, scn.GetStrength(radagast));
		assertEquals(1, scn.GetVitality(radagast));

		// Gandalf heals again while it is a minion: it is what it was, and not strength 24 and vitality 2.
		scn.FreepsPlayCard(healing3);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SHADOW_CHARACTERS, radagast);
		assertEquals(12, scn.GetStrength(radagast));
		assertEquals(1, scn.GetVitality(radagast));
		assertEquals(1, scn.GetKeywordCount(radagast, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHealedWizardWhoIsAnAllyMakesItAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetShadowCard("radagast");
		var brown = scn.GetFreepsCard("brown");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");

		scn.MoveCardsToSupportArea(radagast);
		scn.MoveAlliesToTable(brown, elrond);
		scn.MoveCardsToHand(healing1);

		scn.StartGame();
		scn.AddWoundsToChar(brown, 2);
		assertFalse(scn.IsType(radagast, CardType.MINION));

		// Radagast, the ally, is the only one wounded.
		scn.FreepsPlayCard(healing1);
		assertEquals(1, scn.GetWoundsOn(brown));

		assertInZone(Zone.SHADOW_CHARACTERS, radagast);
		assertTrue(scn.IsType(radagast, CardType.MINION));
		assertEquals(12, scn.GetStrength(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASarumanWhoHealsMakesItAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetShadowCard("radagast");
		var saruman = scn.GetShadowCard("saruman");
		var mithrandir = scn.GetFreepsCard("mithrandir");

		scn.MoveCardsToSupportArea(radagast);
		scn.MoveCompanionsToTable(mithrandir);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.AddWoundsToChar(saruman, 2);
		scn.AddWoundsToChar(mithrandir, 3);

		assertEquals(0, scn.GetVitality(radagast));
		assertEquals(0, scn.GetStrength(radagast));
		assertFalse(scn.HasKeyword(radagast, Keyword.FIERCE));
		assertFalse(scn.IsType(radagast, CardType.MINION));

		// The fellowship moves and this Gandalf wounds himself: his fourth wound, and he dies. Each time a
		// companion is killed, the Shadow player may heal this Saruman, an [uruk-hai] Wizard.
		scn.FreepsPass();
		assertInZone(Zone.DEAD, mithrandir);
		assertFalse(scn.IsType(radagast, CardType.MINION));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(saruman));

		assertEquals(1, scn.GetVitality(radagast));
		assertEquals(12, scn.GetStrength(radagast));
		assertTrue(scn.HasKeyword(radagast, Keyword.FIERCE));
		assertTrue(scn.IsType(radagast, CardType.MINION));
		assertTrue(scn.IsType(radagast, CardType.CONDITION));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ItIsAConditionAgainAtTheRegroupPhaseAndHealedInThatPhaseItIsAMinionUntilTheNextOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetShadowCard("radagast");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var healing2 = scn.GetFreepsCard("healing2");

		scn.MoveCardsToSupportArea(radagast);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1, healing2);

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 2);

		// Healed in the fellowship phase
		scn.FreepsPlayCard(healing1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsType(radagast, CardType.MINION));

		// It is the only minion. Nobody is assigned, and the fierce round follows: the last moment before the
		// regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, radagast);
		assertTrue(scn.IsType(radagast, CardType.MINION));
		assertTrue(scn.IsRace(radagast, Race.WIZARD));
		assertTrue(scn.HasKeyword(radagast, Keyword.FIERCE));
		assertEquals(12, scn.GetStrength(radagast));
		assertEquals(1, scn.GetVitality(radagast));

		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// The regroup phase has started.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, radagast);
		assertFalse(scn.IsType(radagast, CardType.MINION));
		assertTrue(scn.IsType(radagast, CardType.CONDITION));
		assertFalse(scn.IsRace(radagast, Race.WIZARD));
		assertFalse(scn.HasKeyword(radagast, Keyword.FIERCE));
		assertEquals(0, scn.GetStrength(radagast));
		assertEquals(0, scn.GetVitality(radagast));

		// Gandalf heals again, in the regroup phase: a minion again, and at once.
		scn.FreepsPlayCard(healing2);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SHADOW_CHARACTERS, radagast);
		assertTrue(scn.IsType(radagast, CardType.MINION));
		assertEquals(12, scn.GetStrength(radagast));

		// "Until the start of the regroup phase", begun in a regroup phase, is until the next one starts: it is
		// a minion through the whole of a second move.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, radagast);
		assertTrue(scn.IsType(radagast, CardType.MINION));
		assertTrue(scn.HasKeyword(radagast, Keyword.FIERCE));
		assertEquals(12, scn.GetStrength(radagast));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, radagast);
		assertFalse(scn.IsType(radagast, CardType.MINION));
		assertEquals(0, scn.GetStrength(radagast));
	}

	@Test
	public void HealedInTheRegroupPhaseWithNoSecondMoveItIsDiscardedWithTheMinionsAsTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetShadowCard("radagast");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");

		scn.MoveCardsToSupportArea(radagast);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1);

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 1);
		// No minion: straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SUPPORT, radagast);

		scn.FreepsPlayCard(healing1);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SHADOW_CHARACTERS, radagast);
		assertTrue(scn.IsType(radagast, CardType.MINION));

		scn.SkipToMovementDecision();
		assertTrue(scn.IsType(radagast, CardType.MINION));
		scn.FreepsChooseToStay();

		// The turn has ended and the Shadow player's minions are discarded: this one with them.
		assertInDiscard(radagast);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AsAMinionItIsNotOfferedToDefendItAndHopeAndLosesASkirmishWithoutAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetShadowCard("radagast");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var defend = scn.GetFreepsCard("defend");

		scn.MoveCardsToSupportArea(radagast);
		scn.MoveCompanionsToTable(gandalf, aragorn, boromir);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1, defend);

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 1);
		scn.FreepsPlayCard(healing1);
		assertTrue(scn.IsType(radagast, CardType.MINION));

		scn.MoveMinionsToTable(saruman, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Defend It and Hope: Boromir exerts to wound a minion. The other two minions are offered.
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(boromir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(radagast));
		scn.FreepsChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(radagast));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, radagast);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Ettenmoors, three times: Aragorn's 8 + 2 + 2 + 2 against 12.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		scn.ShadowPass();
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		scn.ShadowPass();
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		assertEquals(14, scn.GetStrength(aragorn));
		assertEquals(12, scn.GetStrength(radagast));

		scn.ShadowPass();
		scn.FreepsPass();
		// It has lost. With 1 vitality a wound would have killed it; it is there for the fierce round.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, radagast);
		assertTrue(scn.IsType(radagast, CardType.MINION));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetVitality(radagast));
	}

	@Test
	public void AsAMinionItIsNotOfferedStaffOfSarumanOrFirstLevelWhichGoToOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetShadowCard("radagast");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var staff = scn.GetShadowCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var level = scn.GetFreepsCard("level");

		scn.MoveCardsToSupportArea(radagast);
		scn.MoveCardsToHand(staff);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1);
		scn.MoveCardsToSupportArea(level);

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 1);
		scn.FreepsPlayCard(healing1);
		assertTrue(scn.IsType(radagast, CardType.MINION));
		assertTrue(scn.IsRace(radagast, Race.WIZARD));

		scn.MoveMinionsToTable(saruman, savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Staff of Saruman, "Bearer must be a Wizard": it is a Wizard, and is not among those offered.
		assertTrue(scn.ShadowPlayAvailable(staff));
		scn.ShadowPlayCard(staff);
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(radagast));
		scn.ShadowChooseCard(saruman);
		assertAttachedTo(staff, saruman);
		assertEquals(0, scn.GetAttachedCards(radagast).size());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(level));
		// No knight is in play, so First Level is paid for by exerting the only [gondor] Man.
		scn.FreepsUseCardAction(level);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(radagast));
		scn.FreepsChooseCard(savage);

		assertAttachedTo(level, savage);
		assertEquals(0, scn.GetAttachedCards(radagast).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AsAMinionItIsStillAConditionAndIsDiscardedAsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetShadowCard("radagast");
		var alatar = scn.GetShadowCard("alatar");
		var savage = scn.GetShadowCard("savage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var homestead = scn.GetFreepsCard("homestead");

		scn.MoveCardsToSupportArea(radagast, alatar);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1);
		scn.MoveCardsToSupportArea(homestead);

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 1);
		scn.FreepsPlayCard(healing1);

		assertInZone(Zone.SHADOW_CHARACTERS, radagast);
		assertTrue(scn.IsType(radagast, CardType.MINION));
		assertTrue(scn.IsType(radagast, CardType.CONDITION));

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		// Traveler's Homestead has no token, so it is discarded to discard a Shadow condition from play:
		// the minion is offered beside the condition in the support area.
		scn.FreepsUseCardAction(homestead);
		assertInDiscard(homestead);
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, alatar));
		scn.FreepsChooseCard(radagast);

		assertInDiscard(radagast);
		assertInZone(Zone.SUPPORT, alatar);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
