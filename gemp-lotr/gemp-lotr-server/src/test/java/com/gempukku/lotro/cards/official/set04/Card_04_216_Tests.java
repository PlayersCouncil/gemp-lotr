package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_216_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrow", "4_216");
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession that holds [raider] tokens; abilities used only on purpose

					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2; strength +3 and fierce at 2 burdens
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability used only on purpose
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] artifact; Maneuver: remove (5) to become a fierce Southron minion, no Man, strength 5, vitality 5
					put("archer", "4_245");     // Southron Archer: [raider] archer Man, vitality 2; its archery-total text needs a site controlled
					put("lord", "4_219");       // Desert Lord: unique [raider] archer Man, vitality 3; an archery ability used only on purpose
					put("rider", "12_74");      // Mumak Rider: [men] archer Man, vitality 2; a play trigger only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength 3, vitality 4, bearing nothing
					put("bounder", "1_286");    // Bounder: Hobbit ally whose home is site 2 of this path; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Redhorn Pass 13_194 ("Culture tokens cannot be added, removed, or reinforced") in
	// place of a site no test reaches; the test lays it as the site after the first.
	protected VirtualTableScenario GetRedhornPassScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrow", "4_216");
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength 3, vitality 4, bearing nothing
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArrowFromTheSouthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Arrow From the South
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time a companion or ally loses a skirmish involving a [raider] Man, you may place a [raider] token here.<br><b>Archery:</b> Heal a [raider] archer for each [raider] token here. Discard this condition.
		*/

		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");

		assertEquals("Arrow From the South", arrow.getBlueprint().getTitle());
		assertNull(arrow.getBlueprint().getSubtitle());
		assertTrue(arrow.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, arrow.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, arrow.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, arrow.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(arrow, Keyword.SUPPORT_AREA));
		assertEquals(2, arrow.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeACompanionLosesToARaiderManATokenMayBePlacedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetShadowCard("arrow");
		var ships = scn.GetShadowCard("ships");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(arrow, ships);
		// Another [raider] card holding a [raider] token: the token goes on this card and no other.
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(0, scn.GetCultureTokensOn(arrow));

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// At 2 burdens the Axeman is strength 9 and fierce.
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, axeman);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(9, scn.GetStrength(axeman));
		// Aragorn (8) loses to the Axeman (9) and lives.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(arrow));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(arrow));
		assertEquals(1, scn.GetCultureTokensOn(ships));

		// The fierce round: the same companion loses to the same Man again, and it is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, axeman);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(arrow));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(arrow));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyLosingToARaiderManIsAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetShadowCard("arrow");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToSupportArea(arrow);

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		// Bounder is made strong enough not to be overwhelmed; only the board needs it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bounder, null, 2));
		scn.MoveMinionsToTable(explorer);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, explorer);
		scn.FreepsResolveSkirmish(bounder);
		// 2 + 2 against 5: he loses and lives.
		assertEquals(4, scn.GetStrength(bounder));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(arrow));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(arrow));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingToARaiderMinionWhoIsNoManOrToAManOfAnotherCultureIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetShadowCard("arrow");
		var rearguard = scn.GetShadowCard("rearguard");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(arrow, rearguard);

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		assertInZone(Zone.SUPPORT, rearguard);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		// A [raider] minion now, and no Man.
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, rearguard}, new PhysicalCardImpl[]{aragorn, savage});

		// Merry (3) loses to the Rearguard (5) and lives.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(arrow));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Aragorn (8) loses to the [dunland] Savage (11) and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(arrow));
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		// The fierce Rearguard is left for the fierce round.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ACompanionWinningAgainstARaiderManIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetShadowCard("arrow");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToSupportArea(arrow);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn (8) beats the Explorer (5), who lives: the [raider] Man is the one who loses.
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(arrow));
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferPlacesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetShadowCard("arrow");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToSupportArea(arrow);

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, arrow);
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, explorer);
		scn.FreepsResolveSkirmish(merry);
		// Merry (3) loses to the Explorer (5) and lives.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(arrow));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		assertInZone(Zone.SUPPORT, arrow);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtRedhornPassWhereNoTokenCanBePlacedTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornPassScenario();

		var arrow = scn.GetShadowCard("arrow");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToSupportArea(arrow);

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		scn.MoveCardToAdventurePath(scn.GetShadowSite("redhorn"));
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		assertEquals("Redhorn Pass", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsAssignToMinions(merry, explorer);
		scn.FreepsResolveSkirmish(merry);
		// Merry (3) loses to the Explorer (5), as where the token is offered.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(arrow));
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoTokensOneArcherMayBeHealedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetShadowCard("arrow");
		var archer = scn.GetShadowCard("archer");
		var lord = scn.GetShadowCard("lord");
		scn.MoveCardsToSupportArea(arrow);
		scn.AddTokensToCard(arrow, 2);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, arrow);
		scn.MoveMinionsToTable(archer, lord);
		scn.AddWoundsToChar(lord, 2);
		assertEquals(2, scn.GetCultureTokensOn(arrow));
		assertEquals(2, scn.GetWoundsOn(lord));
		assertEquals(0, scn.GetWoundsOn(archer));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(arrow));
		scn.ShadowUseCardAction(arrow);

		// CR 4.2 "for each": a single character may be affected more than once, healing included. The Desert Lord
		// is the one wounded [raider] archer, so each heal goes to him.
		assertEquals(0, scn.GetWoundsOn(lord));
		assertEquals(0, scn.GetWoundsOn(archer));
		assertInDiscard(arrow);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityHealsARaiderArcherForEachTokenHereAndDiscardsThisCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetShadowCard("arrow");
		var ships = scn.GetShadowCard("ships");
		var archer = scn.GetShadowCard("archer");
		var lord = scn.GetShadowCard("lord");
		var explorer = scn.GetShadowCard("explorer");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToSupportArea(arrow, ships);
		scn.AddTokensToCard(arrow, 1);
		// Tokens on another [raider] card are not counted.
		scn.AddTokensToCard(ships, 2);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, arrow);
		scn.MoveMinionsToTable(archer, lord, explorer, rider);
		scn.AddWoundsToChar(archer, 1);
		scn.AddWoundsToChar(lord, 2);
		scn.AddWoundsToChar(explorer, 1);
		scn.AddWoundsToChar(rider, 1);
		assertEquals(1, scn.GetCultureTokensOn(arrow));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetWoundsOn(archer));
		assertEquals(2, scn.GetWoundsOn(lord));
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(rider));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(arrow));
		scn.ShadowUseCardAction(arrow);

		// A [raider] Man who is no archer, and an archer of the [men] culture, are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(archer, lord));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(explorer, rider));
		scn.ShadowChooseCard(lord);

		// One token here, one heal: the Ships' tokens heal nothing more.
		assertEquals(1, scn.GetWoundsOn(lord));
		assertEquals(1, scn.GetWoundsOn(archer));
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertInDiscard(arrow);
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithNoTokenHereItHealsNothingAndIsStillDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetShadowCard("arrow");
		var ships = scn.GetShadowCard("ships");
		var archer = scn.GetShadowCard("archer");
		scn.MoveCardsToSupportArea(arrow, ships);
		scn.AddTokensToCard(ships, 2);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, arrow);
		assertEquals(2, scn.GetCultureTokensOn(ships));
		scn.MoveMinionsToTable(archer);
		scn.AddWoundsToChar(archer, 1);
		assertEquals(0, scn.GetCultureTokensOn(arrow));
		assertEquals(1, scn.GetWoundsOn(archer));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(arrow));
		scn.ShadowUseCardAction(arrow);

		assertEquals(1, scn.GetWoundsOn(archer));
		assertInDiscard(arrow);
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetShadowCard("arrow");
		var archer = scn.GetShadowCard("archer");
		scn.MoveCardsToSupportArea(arrow);
		scn.AddTokensToCard(arrow, 1);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(archer);
		scn.AddWoundsToChar(archer, 1);
		assertEquals(1, scn.GetCultureTokensOn(arrow));
		assertEquals(1, scn.GetWoundsOn(archer));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(arrow));
	}
}
