package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alatar", "13_78");
					put("radagastd", "13_80");  // Radagast Deceived: a second Shadow condition in the support area; its own text answers a heal only
					put("pallandod", "13_79");  // Pallando Deceived: at the start of the maneuver phase its owner may exert a Wizard to make it a fierce Wizard minion of strength 10
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] Wizard minion, strength 8, vitality 4; his texts are about [men] possessions
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("shaman", "1_152");     // Uruk Shaman: [isengard] Uruk-hai, strength 8, vitality 2, damage +1; a maneuver ability never used here
					put("pursuer1", "4_190");   // Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3; its texts are about unbound Hobbits
					put("pursuer2", "4_190");

					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability never used here
					put("radagast", "31_17");   // Radagast, The Brown: a Wizard who is an ally, strength 8; a fellowship ability never used here
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard, strength 6
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					put("follow", "1_109");     // One Whom Men Would Follow: Maneuver, exert Aragorn; an ally is strength +2 and skirmishes until the regroup phase
					put("level", "7_125");      // Third Level: Skirmish, exert a [gondor] Man to transfer it from the support area to a minion skirmishing a [gondor] Man
					put("pallando", "13_37");   // Pallando, Far-travelling One: follower, bearer strength +1; each time bearer wins a skirmish, you may discard a condition from play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AlatarDeceivedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Alatar Deceived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a Wizard wins a skirmish, this condition becomes a <b>fierce</b>, <b>damage +1</b> Wizard minion until the start of the regroup phase that has 11 strength and 1 vitality, and cannot take wounds or bear other cards. This card is still a condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alatar");

		assertEquals("Alatar Deceived", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AGandalfWhoWinsASkirmishMakesItAFierceWizardMinionUntilTheRegroupPhaseAndAgainAfterASecondMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetShadowCard("alatar");
		var savage = scn.GetShadowCard("savage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(alatar);
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);

		assertInZone(Zone.SUPPORT, alatar);
		assertFalse(scn.IsType(alatar, CardType.MINION));
		assertFalse(scn.IsRace(alatar, Race.WIZARD));
		assertFalse(scn.HasKeyword(alatar, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(alatar, Keyword.DAMAGE));
		assertEquals(0, scn.GetStrength(alatar));
		assertEquals(0, scn.GetVitality(alatar));

		// Gandalf 7 against 5: he wins.
		scn.PassSkirmishActions();
		// The Uruk's wound for losing and this card's trigger come at one moment.
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(savage));

		assertInZone(Zone.SHADOW_CHARACTERS, alatar);
		assertTrue(scn.IsType(alatar, CardType.MINION));
		assertTrue(scn.IsRace(alatar, Race.WIZARD));
		assertTrue(scn.HasKeyword(alatar, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(alatar, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(alatar));
		assertEquals(1, scn.GetVitality(alatar));

		// It is fierce, and is fought in the fierce round.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, alatar);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(alatar));
		assertTrue(scn.IsType(alatar, CardType.MINION));
		assertEquals(11, scn.GetStrength(alatar));

		// Aragorn 8 against 11: he loses and takes 1 wound and 1 more for damage +1. A Wizard has won again, and
		// the minion is what it was: not damage +2, not strength 22.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The regroup phase has started.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, alatar);
		assertFalse(scn.IsType(alatar, CardType.MINION));
		assertTrue(scn.IsType(alatar, CardType.CONDITION));
		assertFalse(scn.IsRace(alatar, Race.WIZARD));
		assertFalse(scn.HasKeyword(alatar, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(alatar, Keyword.DAMAGE));
		assertEquals(0, scn.GetStrength(alatar));
		assertEquals(0, scn.GetVitality(alatar));

		// A second move, and Gandalf beats the Uruk again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertFalse(scn.IsType(alatar, CardType.MINION));
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(savage));

		assertInZone(Zone.SHADOW_CHARACTERS, alatar);
		assertTrue(scn.IsType(alatar, CardType.MINION));
		assertEquals(11, scn.GetStrength(alatar));
		assertEquals(1, scn.GetKeywordCount(alatar, Keyword.DAMAGE));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ASarumanWhoWinsASkirmishMakesItAMinionAndAnEntWhoWinsOrAnUrukWhoBeatsGandalfDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetShadowCard("alatar");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var shaman = scn.GetShadowCard("shaman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(alatar);
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage, shaman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{avenger, savage},
				new PhysicalCardImpl[]{gandalf, shaman},
				new PhysicalCardImpl[]{aragorn, saruman});

		// A [gandalf] companion who is no Wizard wins: the Ent's 6 against 5.
		scn.FreepsResolveSkirmish(avenger);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertInZone(Zone.SUPPORT, alatar);
		assertFalse(scn.IsType(alatar, CardType.MINION));

		// A Wizard loses, to an [isengard] minion who is no Wizard: Gandalf's 7 against 8, and damage +1.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertInZone(Zone.SUPPORT, alatar);
		assertFalse(scn.IsType(alatar, CardType.MINION));

		// The Shadow player's own Wizard wins: Saruman's 8 against Aragorn's 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertInZone(Zone.SHADOW_CHARACTERS, alatar);
		assertTrue(scn.IsType(alatar, CardType.MINION));
		assertTrue(scn.IsType(alatar, CardType.CONDITION));
		assertTrue(scn.IsRace(alatar, Race.WIZARD));
		assertTrue(scn.HasKeyword(alatar, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(alatar, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(alatar));
		assertEquals(1, scn.GetVitality(alatar));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWizardWhoIsAnAllyAndWinsASkirmishMakesItAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetShadowCard("alatar");
		var shaman = scn.GetShadowCard("shaman");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");

		scn.MoveCardsToSupportArea(alatar);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(follow);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Aragorn exerts, and the only ally skirmishes until the regroup phase at strength 8 + 2.
		scn.FreepsPlayCard(follow);
		assertEquals(10, scn.GetStrength(radagast));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(radagast, shaman);
		scn.FreepsResolveSkirmish(radagast);
		assertFalse(scn.IsType(alatar, CardType.MINION));

		// 10 against 8
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(shaman));

		assertInZone(Zone.SHADOW_CHARACTERS, alatar);
		assertTrue(scn.IsType(alatar, CardType.MINION));
		assertEquals(11, scn.GetStrength(alatar));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TwoWizardsWhoWinOneSkirmishMakeItAMinionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetShadowCard("alatar");
		var pallandod = scn.GetShadowCard("pallandod");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(alatar, pallandod);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Pallando Deceived: Saruman, the only Wizard, exerts, and it is a second Wizard minion, of strength 10.
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.IsRace(pallandod, Race.WIZARD));
		assertTrue(scn.IsType(pallandod, CardType.MINION));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, saruman);
		scn.ShadowAssignToMinions(aragorn, pallandod);
		scn.FreepsResolveSkirmish(aragorn);
		// Ettenmoors: Aragorn exerts to be strength 8 + 2, so that 8 + 10 does not overwhelm him.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertFalse(scn.IsType(alatar, CardType.MINION));

		scn.ShadowPass();
		scn.FreepsPass();
		// Each of the two Wizards has won, and the trigger is there for each beside the wound for losing.
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveActionOrder("Alatar Deceived");
		assertTrue(scn.IsType(alatar, CardType.MINION));
		assertEquals(11, scn.GetStrength(alatar));

		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveActionOrder("Alatar Deceived");
		// 1 for Ettenmoors and 1 for losing
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertInZone(Zone.SHADOW_CHARACTERS, alatar);
		assertEquals(11, scn.GetStrength(alatar));
		assertEquals(1, scn.GetVitality(alatar));
		assertEquals(1, scn.GetKeywordCount(alatar, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(alatar, Keyword.FIERCE));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AsAMinionItLosesASkirmishAndTakesNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetShadowCard("alatar");
		var pursuer = scn.GetShadowCard("pursuer1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(alatar);
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.FreepsResolveSkirmish(gandalf);
		// Gandalf 7 against 5: the Uruk loses and takes a wound, as a minion does.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.IsType(alatar, CardType.MINION));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, alatar},
				new PhysicalCardImpl[]{gandalf, pursuer});
		scn.FreepsResolveSkirmish(aragorn);
		// Ettenmoors, twice: Aragorn's 8 + 2 + 2 against 11.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		scn.ShadowPass();
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(11, scn.GetStrength(alatar));
		assertEquals(1, scn.GetVitality(alatar));

		scn.ShadowPass();
		scn.FreepsPass();
		// It has lost. With 1 vitality a wound would have killed it.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertInZone(Zone.SHADOW_CHARACTERS, alatar);
		assertTrue(scn.IsType(alatar, CardType.MINION));
		assertEquals(0, scn.GetWoundsOn(alatar));
		assertEquals(1, scn.GetVitality(alatar));
	}

	@Test
	public void AsAMinionItIsNotOfferedThirdLevelWhichGoesToAnotherMinionInItsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetShadowCard("alatar");
		var savage = scn.GetShadowCard("savage");
		var pursuer1 = scn.GetShadowCard("pursuer1");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var level = scn.GetFreepsCard("level");

		scn.MoveCardsToSupportArea(alatar);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(level);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, pursuer1, pursuer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		// The two fierce Uruks are kept for the fierce round.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.IsType(alatar, CardType.MINION));

		// The fierce round: Aragorn, a [gondor] Man, against it and both Uruks.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, alatar);
		scn.ShadowAssignToMinions(aragorn, pursuer1, pursuer2);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(alatar, aragorn));
		assertTrue(scn.IsCharSkirmishingAgainst(pursuer1, aragorn));

		assertTrue(scn.FreepsActionAvailable(level));
		// Aragorn is the only ranger, so it is paid for by exerting the only [gondor] Man.
		scn.FreepsUseCardAction(level);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer1, pursuer2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(alatar));
		scn.FreepsChooseCard(pursuer1);

		assertAttachedTo(level, pursuer1);
		assertEquals(0, scn.GetAttachedCards(alatar).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AsAMinionItIsStillAConditionAndIsDiscardedAsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetShadowCard("alatar");
		var radagastd = scn.GetShadowCard("radagastd");
		var savage = scn.GetShadowCard("savage");
		var shaman = scn.GetShadowCard("shaman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pallando = scn.GetFreepsCard("pallando");

		scn.MoveCardsToSupportArea(alatar, radagastd);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(aragorn, pallando);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, shaman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gandalf, savage},
				new PhysicalCardImpl[]{aragorn, shaman});
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.SHADOW_CHARACTERS, alatar);
		assertTrue(scn.IsType(alatar, CardType.MINION));
		assertTrue(scn.IsType(alatar, CardType.CONDITION));

		// Aragorn bears Pallando: 8 + 1 against 8. He wins and may discard a condition from play.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsAcceptOptionalTrigger();
		// The minion is offered beside the condition in the support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(alatar, radagastd));
		scn.FreepsChooseCard(alatar);

		assertInDiscard(alatar);
		assertInZone(Zone.SUPPORT, radagastd);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
