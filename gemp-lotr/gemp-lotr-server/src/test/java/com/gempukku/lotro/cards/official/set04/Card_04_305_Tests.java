package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_305_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goodwork", "4_305");
					// Kept Safe: a unique [shire] condition that carries [shire] tokens of its own; its response
					// answers a wound on an unbound Hobbit, and no Hobbit is wounded where it is laid
					put("keptsafe", "6_111");
					// Merry, From O'er the Brandywine: a [shire] Hobbit, strength 3, +2 while he bears a weapon
					put("merry", "1_303");
					put("sword", "1_299");      // Hobbit Sword: hand weapon, strength +2, bearer must be a Hobbit
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit, strength 3; abilities used only on purpose
					// Pippin, Trollslayer: a [gondor] Hobbit, strength 3; enduring, and a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit, strength 3; an assignment ability
					// used only on purpose
					put("smeagol", "103_116");
					// Bounder: a [shire] Hobbit ally, home site 2F, strength 2; a skirmish ability used only on purpose
					put("bounder", "1_286");

					// Uruk Seeker: [isengard] Uruk-hai, strength 6, vitality 2, printed fierce; +2 only against a
					// bearer of a search card
					put("seeker", "4_195");
					// Crebain Flock: [isengard] Crow, strength 2, vitality 3; its Shadow ability is used only on purpose
					put("crebain1", "101_25");
					put("crebain2", "101_25");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3, vitality 4; his
					// abilities are used only on purpose
					put("ted", "18_75");
					// Mordor Soldier: [sauron] Orc, strength 3, vitality 2; a skirmish ability that removes a threat
					put("soldier", "7_290");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRedhornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goodwork", "4_305");
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit, strength 3; abilities used only on purpose
					// Crebain Flock: [isengard] Crow, strength 2, vitality 3; its Shadow ability is used only on purpose
					put("crebain1", "101_25");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Redhorn Pass: "Culture tokens cannot be added, removed, or reinforced." It stands in place of
					// site 9, which no test reaches, and a test lays it as the site after the first.
					remove("site9");
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoodWorkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Good Work
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time a Hobbit wins a skirmish, you may place a [shire] token here.<br><b>Fellowship:</b> Remove a burden for each [shire] token here (limit 3). Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goodwork");

		assertEquals("Good Work", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ATokenIsOfferedEachTimeAHobbitWinsAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goodwork = scn.GetFreepsCard("goodwork");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCardsToSupportArea(goodwork, keptsafe);
		scn.AddTokensToCard(keptsafe, 2);
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, goodwork, keptsafe);
		assertInZone(Zone.ATTACHED, sword);
		assertEquals(0, scn.GetCultureTokensOn(goodwork));
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		// The Seeker is fierce, so that Merry beats it twice in the turn.
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, seeker);

		// Merry, 3 + 2 for the Sword + 2 while he bears a weapon, beats the Seeker, 6: offered, and taken.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(7, scn.GetStrength(merry));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(goodwork));
		scn.FreepsAcceptOptionalTrigger();
		// The token goes here, and not on Kept Safe.
		assertEquals(1, scn.GetCultureTokensOn(goodwork));
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));

		// The same Hobbit wins the fierce skirmish: offered again, and declined.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(merry, seeker);
		scn.FreepsResolveSkirmish(merry);
		scn.PassFierceSkirmishActions();
		assertInDiscard(seeker);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(goodwork));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(goodwork));
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHobbitOfAnotherCultureWinningOffersATokenAndACompanionWhoIsNoHobbitDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goodwork = scn.GetFreepsCard("goodwork");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var crebain1 = scn.GetShadowCard("crebain1");
		var crebain2 = scn.GetShadowCard("crebain2");
		scn.MoveCardsToSupportArea(goodwork);
		scn.MoveCompanionsToTable(pippin, smeagol);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, goodwork);
		scn.MoveMinionsToTable(crebain1, crebain2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{smeagol, crebain1},
				new PhysicalCardImpl[]{pippin, crebain2});

		// Smeagol, 3, a [shire] companion who is no Hobbit, beats a Crebain Flock, 2: nothing offered.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(crebain1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(0, scn.GetCultureTokensOn(goodwork));

		// Pippin, 3, a [gondor] Hobbit, beats the other: offered.
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(crebain2));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(goodwork));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(goodwork));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHobbitAllyAndTheOpponentsHobbitWinningEachOfferAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goodwork = scn.GetFreepsCard("goodwork");
		var bounder = scn.GetFreepsCard("bounder");
		var sam = scn.GetFreepsCard("sam");
		var crebain1 = scn.GetShadowCard("crebain1");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToSupportArea(goodwork);
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, goodwork);
		scn.MoveMinionsToTable(crebain1, ted);
		// Strength 2 by print: +1 by hand so that he beats a Crebain Flock, 2.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bounder, null, 1));
		// The fellowship moves to site 2, Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{bounder, crebain1},
				new PhysicalCardImpl[]{sam, ted});

		// Bounder, 3, beats the Crebain, 2: offered.
		scn.FreepsResolveSkirmish(bounder);
		assertEquals(3, scn.GetStrength(bounder));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(crebain1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(goodwork));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(goodwork));

		// Ted Sandyman, 3, beats Sam, 3, on a tie: the opponent's Hobbit wins, and the offer is made again.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(goodwork));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(goodwork));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAHobbitLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goodwork = scn.GetFreepsCard("goodwork");
		var sam = scn.GetFreepsCard("sam");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(goodwork);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, goodwork);
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, soldier);

		// Sam, 3, loses to the Mordor Soldier, 3, and lives.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(goodwork));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhereNoCultureTokenCanBePlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornScenario();

		var goodwork = scn.GetFreepsCard("goodwork");
		var sam = scn.GetFreepsCard("sam");
		var crebain1 = scn.GetShadowCard("crebain1");
		scn.MoveCardsToSupportArea(goodwork);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, goodwork);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("redhorn"));
		scn.MoveMinionsToTable(crebain1);
		scn.SkipToAssignments();
		assertEquals("Redhorn Pass", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsAssignToMinions(sam, crebain1);

		// Sam, 3, beats the Crebain, 2, at Redhorn Pass: the token could not be placed, and nothing is offered.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(crebain1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(goodwork));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipAbilityRemovesABurdenForEachTokenHereAndDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goodwork = scn.GetFreepsCard("goodwork");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		scn.MoveCardsToSupportArea(goodwork, keptsafe);
		scn.AddTokensToCard(goodwork, 2);
		scn.AddTokensToCard(keptsafe, 2);

		scn.StartGame();
		scn.AddBurdens(3);
		assertInZone(Zone.SUPPORT, goodwork, keptsafe);
		assertEquals(2, scn.GetCultureTokensOn(goodwork));
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertEquals(3, scn.GetBurdens());
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(goodwork));

		scn.FreepsUseCardAction(goodwork);

		// Two tokens here: two burdens. Kept Safe's two [shire] tokens are not counted, and stay.
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(goodwork);
		assertInZone(Zone.SUPPORT, keptsafe);
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithFourTokensNoMoreThanThreeBurdensAreRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goodwork = scn.GetFreepsCard("goodwork");
		scn.MoveCardsToSupportArea(goodwork);
		scn.AddTokensToCard(goodwork, 4);

		scn.StartGame();
		scn.AddBurdens(5);
		assertInZone(Zone.SUPPORT, goodwork);
		assertEquals(4, scn.GetCultureTokensOn(goodwork));
		assertEquals(5, scn.GetBurdens());
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(goodwork);

		// Limit 3: 5 - 3.
		assertEquals(2, scn.GetBurdens());
		assertInDiscard(goodwork);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoTokensItRemovesNoBurdenAndStillDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goodwork = scn.GetFreepsCard("goodwork");
		scn.MoveCardsToSupportArea(goodwork);

		scn.StartGame();
		scn.AddBurdens(2);
		assertInZone(Zone.SUPPORT, goodwork);
		assertEquals(0, scn.GetCultureTokensOn(goodwork));
		assertEquals(2, scn.GetBurdens());
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(goodwork));

		scn.FreepsUseCardAction(goodwork);

		assertEquals(2, scn.GetBurdens());
		assertInDiscard(goodwork);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goodwork = scn.GetFreepsCard("goodwork");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(goodwork);
		scn.AddTokensToCard(goodwork, 2);

		scn.StartGame();
		scn.AddBurdens(2);
		assertInZone(Zone.SUPPORT, goodwork);
		assertEquals(2, scn.GetCultureTokensOn(goodwork));
		assertEquals(2, scn.GetBurdens());
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(goodwork));

		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(goodwork));
		assertInZone(Zone.SUPPORT, goodwork);
		assertEquals(2, scn.GetCultureTokensOn(goodwork));
		assertEquals(2, scn.GetBurdens());
	}
}
