package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pallando", "13_79");
					put("alatar", "13_78");     // Alatar Deceived: a second Shadow condition in the support area; its own text answers a Wizard's win only
					put("throne", "17_39");     // Throne of Isengard: each Wizard is strength +3 and damage +1; its trigger answers Saruman being played
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] Wizard minion, strength 8, vitality 4; his texts are about [men] possessions
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text

					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability never used here
					put("radagast", "31_17");   // Radagast, The Brown: a Wizard who is an ally; a fellowship ability never used here
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: a second [gondor] Man; a skirmish ability never used here
					put("defend", "4_115");     // Defend It and Hope: Maneuver, exert a [gondor] Man to wound a minion (twice if an Uruk-hai)
					put("level", "7_92");       // First Level: Maneuver, exert a [gondor] Man to transfer it from the support area to a minion
					put("third", "7_125");      // Third Level: Skirmish, exert a [gondor] Man to transfer it from the support area to a minion skirmishing a [gondor] Man
					put("homestead", "13_42");  // Traveler's Homestead: Maneuver, discard it from play to discard a Shadow condition from play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PallandoDeceivedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Pallando Deceived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: At the start of the maneuver phase, you may exert a Wizard to make this condition a <b>fierce</b> Wizard minion until the start of the regroup phase that has 10 strength and 1 vitality, and cannot take wounds or bear other cards. This card is still a condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pallando");

		assertEquals("Pallando Deceived", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseTheShadowPlayerMayExertAWizardToMakeItAFierceWizardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");

		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(radagast);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, pallando);
		assertFalse(scn.IsType(pallando, CardType.MINION));
		assertFalse(scn.IsRace(pallando, Race.WIZARD));
		assertFalse(scn.HasKeyword(pallando, Keyword.FIERCE));
		assertEquals(0, scn.GetStrength(pallando));
		assertEquals(0, scn.GetVitality(pallando));
		assertEquals(0, scn.GetWoundsOn(saruman));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pallando));
		scn.ShadowAcceptOptionalTrigger();
		// A Wizard of either player, minion, companion or ally: not the Uruk, not the [gandalf] Ent.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, gandalf, radagast));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, avenger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertInZone(Zone.SHADOW_CHARACTERS, pallando);
		assertTrue(scn.IsType(pallando, CardType.MINION));
		assertTrue(scn.IsRace(pallando, Race.WIZARD));
		assertTrue(scn.HasKeyword(pallando, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(pallando, Keyword.DAMAGE));
		assertEquals(10, scn.GetStrength(pallando));
		assertEquals(1, scn.GetVitality(pallando));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheWizardExertedMayBeTheFreePeoplesPlayersGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var saruman = scn.GetShadowCard("saruman");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, gandalf));
		scn.ShadowChooseCard(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.IsType(pallando, CardType.MINION));
		assertEquals(10, scn.GetStrength(pallando));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DeclinedItStaysAConditionNobodyExertsAndItIsNotOfferedAgainAtTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var saruman = scn.GetShadowCard("saruman");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pallando));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SUPPORT, pallando);
		assertFalse(scn.IsType(pallando, CardType.MINION));
		assertEquals(0, scn.GetStrength(pallando));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase is no start of the maneuver phase.
		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.IsType(pallando, CardType.MINION));
	}

	@Test
	public void IsNotOfferedWhileTheOnlyWizardIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var avenger = scn.GetFreepsCard("avenger");

		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCompanionsToTable(avenger);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		// Vitality 4: with 3 wounds Saruman cannot exert. The Uruk and the Ent can, and are no Wizards.
		scn.AddWoundsToChar(saruman, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.IsType(pallando, CardType.MINION));
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(avenger));
	}

	@Test
	public void ItIsAMinionThroughTheFierceSkirmishAndAConditionAgainAtTheRegroupPhaseAndIsOfferedAgainAfterASecondMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Saruman is the only Wizard.
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.IsType(pallando, CardType.MINION));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, pallando);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn 8 against 10
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fierce round: a skirmish after one that is over, and it is a minion still.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, pallando);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(pallando));
		assertInZone(Zone.SHADOW_CHARACTERS, pallando);
		assertTrue(scn.IsType(pallando, CardType.MINION));
		assertTrue(scn.IsRace(pallando, Race.WIZARD));
		assertTrue(scn.HasKeyword(pallando, Keyword.FIERCE));
		assertEquals(10, scn.GetStrength(pallando));
		assertEquals(1, scn.GetVitality(pallando));

		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The regroup phase has started.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, pallando);
		assertFalse(scn.IsType(pallando, CardType.MINION));
		assertTrue(scn.IsType(pallando, CardType.CONDITION));
		assertFalse(scn.IsRace(pallando, Race.WIZARD));
		assertFalse(scn.HasKeyword(pallando, Keyword.FIERCE));
		assertEquals(0, scn.GetStrength(pallando));
		assertEquals(0, scn.GetVitality(pallando));

		// A second move: a second maneuver phase, and the offer is made again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pallando));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertTrue(scn.IsType(pallando, CardType.MINION));
		assertEquals(10, scn.GetStrength(pallando));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AsAMinionItIsNotOfferedToDefendItAndHopeAndLosesASkirmishWithoutAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var defend = scn.GetFreepsCard("defend");

		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		// Saruman is the only Wizard.
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.IsType(pallando, CardType.MINION));

		// Defend It and Hope: Boromir exerts to wound a minion. The other two minions are offered.
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(boromir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pallando));
		scn.FreepsChooseCard(saruman);
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(pallando));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, pallando);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Ettenmoors, twice: Aragorn's 8 + 2 + 2 against 10.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		scn.ShadowPass();
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(pallando));

		scn.ShadowPass();
		scn.FreepsPass();
		// It has lost. With 1 vitality a wound would have killed it; it is there for the fierce round.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, pallando);
		assertTrue(scn.IsType(pallando, CardType.MINION));
		assertEquals(0, scn.GetWoundsOn(pallando));
		assertEquals(1, scn.GetVitality(pallando));
	}

	@Test
	public void AsAMinionItIsNotOfferedFirstLevelWhichGoesToAnotherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var level = scn.GetFreepsCard("level");

		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(level);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		// Saruman is the only Wizard.
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.IsType(pallando, CardType.MINION));

		assertTrue(scn.FreepsActionAvailable(level));
		// No knight is in play, so First Level is paid for by exerting the only [gondor] Man.
		scn.FreepsUseCardAction(level);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pallando));
		scn.FreepsChooseCard(savage);

		assertAttachedTo(level, savage);
		assertEquals(0, scn.GetAttachedCards(pallando).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AsAMinionItIsNotOfferedThirdLevelInTheSkirmishThatFollowsTheManeuverPhaseItWasMadeIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var third = scn.GetFreepsCard("third");

		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(third);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		// Saruman is the only Wizard.
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.IsType(pallando, CardType.MINION));

		// Aragorn, a [gondor] Man, against all three minions.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, pallando);
		scn.ShadowAssignToMinions(aragorn, saruman, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(pallando, aragorn));
		assertTrue(scn.IsCharSkirmishingAgainst(saruman, aragorn));
		assertTrue(scn.IsCharSkirmishingAgainst(savage, aragorn));

		assertTrue(scn.FreepsActionAvailable(third));
		// Aragorn is the only [gondor] Man, so it is paid for by exerting him.
		scn.FreepsUseCardAction(third);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pallando));
		scn.FreepsChooseCard(savage);

		assertAttachedTo(third, savage);
		assertEquals(0, scn.GetAttachedCards(pallando).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AsAMinionItIsStillAConditionAndIsDiscardedAsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var alatar = scn.GetShadowCard("alatar");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var homestead = scn.GetFreepsCard("homestead");

		scn.MoveCardsToSupportArea(pallando, alatar);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(homestead);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Saruman is the only Wizard.
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, pallando);
		assertTrue(scn.IsType(pallando, CardType.MINION));
		assertTrue(scn.IsType(pallando, CardType.CONDITION));

		// Traveler's Homestead has no token, so it is discarded to discard a Shadow condition from play:
		// the minion is offered beside the condition in the support area.
		scn.FreepsUseCardAction(homestead);
		assertInDiscard(homestead);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pallando, alatar));
		scn.FreepsChooseCard(pallando);

		assertInDiscard(pallando);
		assertInZone(Zone.SUPPORT, alatar);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AsAWizardMinionItIsStrengthenedByThroneOfIsengard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pallando = scn.GetShadowCard("pallando");
		var throne = scn.GetShadowCard("throne");
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCardsToSupportArea(pallando, throne);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		// Throne of Isengard: each Wizard is strength +3 and damage +1. The condition is no Wizard yet.
		assertEquals(0, scn.GetStrength(pallando));
		assertEquals(0, scn.GetKeywordCount(pallando, Keyword.DAMAGE));
		// Saruman's 8 + 3
		assertEquals(11, scn.GetStrength(saruman));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pallando));
		scn.ShadowAcceptOptionalTrigger();

		// 10 + 3
		assertEquals(13, scn.GetStrength(pallando));
		assertEquals(1, scn.GetKeywordCount(pallando, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
