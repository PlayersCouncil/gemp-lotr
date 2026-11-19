package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_005_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bifur", "30_5");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] Dwarf companion, 7/3; a maneuver ability only
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] Dwarf companion, damage +1; a skirmish ability only
					// Dain Ironfoot, Lord of the Iron Hills: [dwarven] Dwarf ally, vitality 3 (he can exert); a
					// bonus for Thorin and a regroup offer that wants a [dwarven] follower to discard
					put("dain", "32_2");
					// Dwarven Axe: [dwarven] possession, hand weapon, strength +2; damage +1 only at a mountain
					put("axe", "31_1");
					// Emeralds of Girion: [dwarven] artifact in the support area; fellowship and regroup abilities only
					put("emeralds", "31_3");
					// Stairs of Khazad-dûm: [dwarven] condition in the support area; +1 to a Dwarf skirmishing a
					// [moria] minion (no [moria] minion is laid). The Hobbit sets print no Free Peoples condition.
					put("stairs", "1_24");

					// Hatred Rekindled: [gundabad] condition in the support area; a Shadow ability only
					put("hatred", "30_34");
					// Caught In A Sack: [troll] condition borne by a [dwarven] companion, who then cannot be assigned
					// to a skirmish; discarded if Bilbo wins a skirmish (he does not fight here)
					put("sack", "31_57");
					// Wicked Spider: [spider] Spider, 7/2, fierce; a skirmish ability that wants an Orc to discard
					put("spider", "31_55");
					put("spider2", "31_55");
					// Old Tomnoddy: [spider] Spider, 8/3, fierce; a text on being played, never reached
					put("tomnoddy", "31_53");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BifurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bifur, Inarticulate
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype: Dwarf
		 * Strength: +1
		 * Aid: Exert a companion.
		 * Game Text: <b>Aid</b> – Exert a companion. <helper>(At the start of the maneuver phase, you may exert a companion to transfer this to a companion.)</helper>
		 * 	Each time bearer wins a fierce skirmish, you may discard a condition from play.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bifur");

		assertEquals("Bifur", card.getBlueprint().getTitle());
		assertEquals("Inarticulate", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AidExertsACompanionOfThePlayersChoiceToTransferBifurToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(bifur);

		scn.StartGame();
		scn.MoveMinionsToTable("spider");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, bifur);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bifur, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// The Ring-bearer, a [shire] Hobbit, and Balin, a [dwarven] Dwarf, may pay; Dain, a Dwarf ally who could
		// exert, may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.FreepsChooseCard(bilbo);
		assertAttachedTo(bifur, bilbo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWhileEveryCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(bifur);
		// Bilbo, vitality 4, and Balin, vitality 3, left with one each: neither can exert. Dain can, but is an ally.
		scn.AddWoundsToChar(bilbo, 3);
		scn.AddWoundsToChar(balin, 2);

		scn.StartGame();
		scn.MoveMinionsToTable("spider");

		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bifur, "Aid"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, bifur);
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dain));
	}

	@Test
	public void WinningHisFierceSkirmishOffersADiscardOfAConditionWhereHisNormalWinAndAnotherCompanionsFierceWinDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var gloin = scn.GetFreepsCard("gloin");
		var axe = scn.GetFreepsCard("axe");
		var emeralds = scn.GetFreepsCard("emeralds");
		var stairs = scn.GetFreepsCard("stairs");
		var hatred = scn.GetShadowCard("hatred");
		var sack = scn.GetShadowCard("sack");
		var spider = scn.GetShadowCard("spider");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		scn.MoveCompanionsToTable(balin, nori, gloin);
		scn.AttachCardsTo(balin, bifur);
		scn.AttachCardsTo(nori, axe);
		scn.MoveCardsToSupportArea(emeralds, stairs, hatred);
		scn.AttachCardsTo(gloin, sack);

		scn.StartGame();
		scn.MoveMinionsToTable(spider, tomnoddy);

		assertAttachedTo(bifur, balin);
		assertAttachedTo(axe, nori);
		assertAttachedTo(sack, gloin);
		assertInZone(Zone.SUPPORT, emeralds);
		assertInZone(Zone.SUPPORT, stairs);
		assertInZone(Zone.SUPPORT, hatred);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, spider}, new PhysicalCardImpl[]{nori, tomnoddy});

		// Balin 7 + 1 for Bifur against the Spider's 7; Nori 7 + 2 for the Axe against Old Tomnoddy's 8.
		scn.FreepsResolveSkirmish(balin);
		assertEquals(8, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(spider));
		scn.PassSkirmishActions();
		// Balin has won a skirmish against a fierce minion, but not a fierce skirmish: nothing is offered.
		assertEquals(1, scn.GetWoundsOn(spider));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bifur));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		scn.FreepsResolveSkirmish(nori);
		assertEquals(9, scn.GetStrength(nori));
		assertEquals(8, scn.GetStrength(tomnoddy));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(tomnoddy));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, spider}, new PhysicalCardImpl[]{nori, tomnoddy});

		// Nori, who does not bear Bifur, wins his fierce skirmish: nothing is offered.
		scn.FreepsResolveSkirmish(nori);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(tomnoddy));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bifur));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		scn.FreepsResolveSkirmish(balin);
		scn.PassFierceSkirmishActions();
		// The Spider's second wound kills it.
		assertInZone(Zone.DISCARD, spider);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bifur));

		scn.FreepsAcceptOptionalTrigger();
		// Conditions of either side, in a support area or borne, are offered; an artifact and a possession are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, hatred, sack));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(emeralds, axe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sack);

		assertInZone(Zone.DISCARD, sack);
		assertInZone(Zone.SUPPORT, stairs);
		assertInZone(Zone.SUPPORT, hatred);
		assertInZone(Zone.SUPPORT, emeralds);
		assertAttachedTo(axe, nori);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningDiscardsNothingAndASecondFierceWinTheSameTurnOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		var hatred = scn.GetShadowCard("hatred");
		var spider = scn.GetShadowCard("spider");
		var spider2 = scn.GetShadowCard("spider2");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, bifur);
		scn.MoveCardsToSupportArea(hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(spider);

		assertAttachedTo(bifur, balin);
		assertInZone(Zone.SUPPORT, hatred);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, spider);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(balin, spider);
		scn.FreepsResolveSkirmish(balin);
		scn.PassFierceSkirmishActions();

		assertInZone(Zone.DISCARD, spider);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bifur));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move, and a second fierce minion: Bifur is still on Balin for the rest of the turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(spider2);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);
		// His Aid is offered again for the follower already borne; declined, he stays where he is.
		scn.FreepsDeclineOptionalTrigger();
		scn.SkipToAssignments();
		assertAttachedTo(bifur, balin);

		scn.FreepsAssignToMinions(balin, spider2);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(balin, spider2);
		scn.FreepsResolveSkirmish(balin);
		scn.PassFierceSkirmishActions();

		assertInZone(Zone.DISCARD, spider2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bifur));
		scn.FreepsAcceptOptionalTrigger();
		// Hatred Rekindled is the only condition: chosen unasked.
		assertInZone(Zone.DISCARD, hatred);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayAFierceWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		var emeralds = scn.GetFreepsCard("emeralds");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, bifur);
		// An artifact in the support area, which is no condition.
		scn.MoveCardsToSupportArea(emeralds);

		scn.StartGame();
		scn.MoveMinionsToTable(spider);

		assertAttachedTo(bifur, balin);
		assertInZone(Zone.SUPPORT, emeralds);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, spider);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(balin, spider);
		scn.FreepsResolveSkirmish(balin);
		scn.PassFierceSkirmishActions();

		assertInZone(Zone.DISCARD, spider);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bifur));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, emeralds);
	}
}
