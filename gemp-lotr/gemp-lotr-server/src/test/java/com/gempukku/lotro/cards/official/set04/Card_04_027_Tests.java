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

public class Card_04_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rock", "4_27");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					// Dunlending Ravager: [dunland] Man, strength 5, vitality 1; +2 while skirmishing a [rohan] Man,
					// and an assignment ability that needs an ally (none where he is laid).
					put("ravager", "4_15");
					// Easterling Axeman: a [raider] Man, strength 6, vitality 2; strength +3 and fierce only at 2 burdens.
					put("axeman", "4_224");
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man, strength 8.  "Saruman may
					// not be assigned to a skirmish" until the Staff's maneuver ability; his move trigger has him
					// laid after the move, and his skirmish ability is used only on purpose.
					put("saruman", "4_33");
					put("staff", "4_174");      // Saruman's Staff: on Saruman, strength +2, fierce, damage +1
					// Hides: a [dunland] possession in the support area.  "Response: If a [dunland] Man is about to
					// take a wound, remove (2) or discard this possession to prevent that wound."
					put("hides", "4_19");

					// Éomer, Skilled Tactician: a [rohan] Man companion, strength 8, vitality 3; a fellowship
					// ability used only on purpose.
					put("eomer", "7_227");
					// Rider of Rohan: a [rohan] Man companion, strength 5; a skirmish ability only while mounted.
					put("rider", "4_286");
					// Merry, Master Holbytla: a [rohan] Hobbit, strength 3, vitality 4; a Man only while 3 [rohan]
					// companions or Éowyn can be spotted (neither here).
					put("merry", "103_85");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man, strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rock", "4_27");
					// Dunlending Elder: [dunland] Man, strength 7, vitality 1; its bonus needs 2 controlled sites.
					put("elder", "6_2");
					// Rohirrim Herdsman: a [rohan] Man ally, strength 4, vitality 2, home site 3 of the Towers path;
					// a regroup ability used only on purpose.
					put("herdsman", "7_247");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LivingOffRockStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Living Off Rock
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time a [dunland] Man wins a skirmish involving a [rohan] Man, you may make that minion strength +2 and <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rock");

		assertEquals("Living Off Rock", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AWinOverARohanManMakesThatMinionStrengthPlus2AndFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetShadowCard("rock");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");
		var savage = scn.GetShadowCard("savage");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToSupportArea(rock);
		assertInZone(Zone.SUPPORT, rock);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, axeman, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, brigand);
		// The Shadow player puts the Axeman, a Man of another culture, into the same skirmish; the Savage, a second
		// [dunland] Man, is left out of every skirmish.
		scn.ShadowAssignToMinions(eomer, axeman);
		scn.FreepsResolveSkirmish(eomer);

		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(6, scn.GetStrength(axeman));
		assertEquals(11, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		// 8 + 6 against 8: the minions win, without overwhelming Éomer (16).
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rock));
		assertFalse(scn.InFierceRound());

		scn.ShadowAcceptOptionalTrigger();
		// "That minion": the [dunland] Man who won, 8 + 2 and fierce; the Axeman who won beside him and the Savage
		// as printed.
		assertEquals(10, scn.GetStrength(brigand));
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertEquals(6, scn.GetStrength(axeman));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertEquals(11, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		// Fierce, he is defended against a second time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(axeman));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(savage));
		scn.FreepsAssignToMinions(eomer, brigand);

		// The fierce skirmish is the last thing before the regroup phase: both halves hold through it.
		scn.FreepsResolveSkirmish(eomer);
		assertEquals(10, scn.GetStrength(brigand));
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(eomer));

		// "Each time": he has won a second skirmish against a [rohan] Man and is asked again.  Declined, so that
		// what is read in the regroup phase is the end of the first win's bonus.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rock));
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(brigand));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
	}

	@Test
	public void AWinOverARohanAllyAtHisHomeSiteIsAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var rock = scn.GetShadowCard("rock");
		var elder = scn.GetShadowCard("elder");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(rock);
		assertInZone(Zone.SUPPORT, rock);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(elder);
		// The fellowship moves to site 3, the Herdsman's home, where he may be assigned to a skirmish.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(herdsman, elder);
		scn.FreepsResolveSkirmish(herdsman);

		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(7, scn.GetStrength(elder));
		assertFalse(scn.HasKeyword(elder, Keyword.FIERCE));

		// 7 against 4: the Elder wins, without overwhelming the Herdsman (8).
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rock));
		scn.ShadowAcceptOptionalTrigger();

		// 7 + 2
		assertEquals(9, scn.GetStrength(elder));
		assertTrue(scn.HasKeyword(elder, Keyword.FIERCE));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningLeavesThatMinionAsPrintedAndNoFierceRoundFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetShadowCard("rock");
		var savage = scn.GetShadowCard("savage");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToSupportArea(rock);
		assertInZone(Zone.SUPPORT, rock);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, savage);
		scn.FreepsResolveSkirmish(eomer);
		assertEquals(0, scn.GetWoundsOn(eomer));

		// 11 against 8: the Savage wins, without overwhelming Éomer (16).
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rock));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(11, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADunlandManWhoLosesToARohanManAndLivesIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetShadowCard("rock");
		var brigand = scn.GetShadowCard("brigand");
		var hides = scn.GetShadowCard("hides");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToSupportArea(rock, hides);
		assertInZone(Zone.SUPPORT, rock, hides);
		// Éomer at 9 beats the Brigand's 8 without overwhelming him (16).
		scn.ApplyAdHocModifier(new StrengthModifier(null, eomer, null, 1));

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, brigand);
		scn.FreepsResolveSkirmish(eomer);

		// An empty pool leaves discarding Hides as its one cost.
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(brigand));

		scn.PassCurrentPhaseActions();
		// Hides saves the Brigand from the wound for losing, so that he is still in play to be answered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rock));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(hides);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rock));
		assertEquals(8, scn.GetStrength(brigand));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotAnsweredWhenAManOfAnotherCultureOrADunlandMinionWhoIsNoManWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetShadowCard("rock");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(eomer, rider);
		scn.MoveCardsToSupportArea(rock);
		assertInZone(Zone.SUPPORT, rock);

		scn.StartGame();
		scn.FreepsPass();
		// Laid once the fellowship has moved, so that Saruman's move trigger has nothing to answer.
		scn.MoveMinionsToTable(axeman, saruman);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);
		// The Staff's maneuver ability makes "Saruman may not be assigned to a skirmish" not apply until the
		// regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, axeman}, new PhysicalCardImpl[]{eomer, saruman});

		// 6 against 5: the Axeman, a Man of another culture, beats a [rohan] Man.
		scn.FreepsResolveSkirmish(rider);
		assertEquals(0, scn.GetWoundsOn(rider));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(rider));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rock));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// 8 + 2 for the Staff against 8: Saruman, a [dunland] minion who is no Man, beats a [rohan] Man.
		scn.FreepsResolveSkirmish(eomer);
		assertEquals(0, scn.GetWoundsOn(eomer));
		scn.PassCurrentPhaseActions();
		// 1 for losing + 1 for the Staff's damage +1
		assertEquals(2, scn.GetWoundsOn(eomer));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rock));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		// The Staff makes Saruman fierce, so the game goes on to the fierce assignments.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotAnsweredWhenTheBeatenCharacterIsARohanHobbitOrAManOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetShadowCard("rock");
		var ravager = scn.GetShadowCard("ravager");
		var savage = scn.GetShadowCard("savage");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(merry, knight);
		scn.MoveCardsToSupportArea(rock);
		assertInZone(Zone.SUPPORT, rock);

		scn.StartGame();
		scn.MoveMinionsToTable(ravager, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, ravager}, new PhysicalCardImpl[]{knight, savage});

		// The Ravager is 5, not 7: Merry, a [rohan] Hobbit, is no Man here.  5 against 3 wins without overwhelming.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(5, scn.GetStrength(ravager));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rock));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// 11 against 6: the Savage beats a [gondor] Man, without overwhelming him (12).
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rock));
		assertEquals(5, scn.GetStrength(ravager));
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
