package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("asunder", "2_27");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, vitality 4
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose

					put("staff", "2_29");       // Wizard Staff: [gandalf] possession, staff, on a Wizard; changes no vitality
					put("focus", "7_38");       // Gandalf's Staff, Focus of Power: [gandalf] artifact, staff, vitality +1
					put("glamdring", "1_75");   // Glamdring: a hand weapon Gandalf bears, no staff

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man; strength +3 and fierce only at 2 burdens
					put("saruman", "4_173");    // Saruman, Black Traitor: [isengard] Wizard minion who may not be assigned to a skirmish
					put("sarustaff", "4_174");  // Saruman's Staff, Wizard's Device: [isengard] artifact staff borne by Saruman
					put("stave", "13_81");      // Staff of Saruman, Fallen Istar's Stave: [isengard] artifact staff on "a Wizard", vitality +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StaffAsunderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Staff Asunder
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>.
		 * <b>Skirmish:</b> Discard a staff Gandalf is bearing and then exert him twice to discard a minion he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("asunder");

		assertEquals("Staff Asunder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsGandalfsStaffAndExertsHimTwiceToDiscardAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asunder = scn.GetFreepsCard("asunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var staff = scn.GetFreepsCard("staff");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AttachCardsTo(gandalf, staff, glamdring);
		scn.MoveCardsToHand(asunder);

		var troop = scn.GetShadowCard("troop");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		var sarustaff = scn.GetShadowCard("sarustaff");

		assertAttachedTo(staff, gandalf);
		assertAttachedTo(glamdring, gandalf);
		
		scn.StartGame();
		scn.MoveMinionsToTable(troop, axeman, saruman);
		scn.AttachCardsTo(saruman, sarustaff);
		// Vitality 4 and a staff that gives none: with 1 wound he has 3 left, enough to exert twice.
		scn.AddWoundsToChar(gandalf, 1);

		assertAttachedTo(sarustaff, saruman);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		// Saruman may not be assigned: the Axeman joins the Troop against Gandalf.
		scn.ShadowAssignToMinions(gandalf, axeman);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, axeman));
		assertAttachedTo(staff, gandalf);
		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(sarustaff, saruman);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(3, scn.GetVitality(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsPlayAvailable(asunder));
		scn.FreepsPlayCard(asunder);

		// His one staff goes unasked; his hand weapon and the staff Saruman bears stay where they are.
		assertInDiscard(staff);
		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(sarustaff, saruman);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// Either minion he is skirmishing; not Saruman, who is in no skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, axeman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman, gandalf, avenger));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axeman);

		assertInDiscard(axeman, asunder);
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOtherPlayersStaffGandalfBearsPaysTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asunder = scn.GetFreepsCard("asunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(asunder);

		var troop = scn.GetShadowCard("troop");
		var stave = scn.GetShadowCard("stave");
		// "Bearer must be a Wizard": the Shadow player may play his staff on Gandalf.
		scn.AttachCardsTo(gandalf, stave);

		assertAttachedTo(stave, gandalf);
		
		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertAttachedTo(stave, gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(asunder));
		scn.FreepsPlayCard(asunder);

		assertInDiscard(stave);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		// The only minion he is skirmishing is discarded unasked, and with it gone the skirmish is over.
		assertInDiscard(troop, asunder);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayedInAnotherCompanionsSkirmishTheCostIsPaidAndNoMinionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asunder = scn.GetFreepsCard("asunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(asunder);

		var troop = scn.GetShadowCard("troop");
		var axeman = scn.GetShadowCard("axeman");

		assertAttachedTo(staff, gandalf);
		
		scn.StartGame();
		scn.MoveMinionsToTable(troop, axeman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{aragorn, axeman});
		scn.FreepsResolveSkirmish(aragorn);

		// The Axeman fights another companion; the Troop is assigned to Gandalf, whose skirmish has not begun.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, axeman));
		assertFalse(scn.IsCharSkirmishing(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(asunder));
		scn.FreepsPlayCard(asunder);

		assertInDiscard(staff, asunder);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		// Gandalf is skirmishing nobody: neither minion is discarded.
		assertInZone(Zone.SHADOW_CHARACTERS, troop, axeman);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, axeman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileGandalfBearsNoStaff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asunder = scn.GetFreepsCard("asunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(asunder);

		var troop = scn.GetShadowCard("troop");
		var saruman = scn.GetShadowCard("saruman");
		var sarustaff = scn.GetShadowCard("sarustaff");

		assertAttachedTo(glamdring, gandalf);
		
		scn.StartGame();
		scn.MoveMinionsToTable(troop, saruman);
		scn.AttachCardsTo(saruman, sarustaff);

		assertAttachedTo(sarustaff, saruman);
		
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		// A staff is in play, but Saruman bears it; Gandalf bears only a hand weapon.
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(sarustaff, saruman);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(asunder));
	}

	@Test
	public void CannotBePlayedWhileGandalfCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asunder = scn.GetFreepsCard("asunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(asunder);

		var troop = scn.GetShadowCard("troop");

		assertAttachedTo(staff, gandalf);
		
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 4: with 2 wounds he has 2 left and can exert only once. The Ent Avenger could exert twice.
		scn.AddWoundsToChar(gandalf, 2);

		assertEquals(2, scn.GetWoundsOn(gandalf));
		
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(asunder));
	}

	@Test
	public void PlayedWhereGandalfKeepsEnoughVitalityOnceAStaffThatGivesVitalityIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asunder = scn.GetFreepsCard("asunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var focus = scn.GetFreepsCard("focus");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, focus);
		scn.MoveCardsToHand(asunder);

		var troop = scn.GetShadowCard("troop");

		assertAttachedTo(focus, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 4 + 1 from the staff: with 1 wound he has 4 left. Once the staff is discarded he has 3, which is
		// enough to exert twice.
		scn.AddWoundsToChar(gandalf, 1);

		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertEquals(4, scn.GetVitality(gandalf));
		assertAttachedTo(focus, gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(asunder));
		scn.FreepsPlayCard(asunder);

		assertInDiscard(focus);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		// The only minion he is skirmishing is discarded unasked, and with it gone the skirmish is over.
		assertInDiscard(troop, asunder);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhereOnlyTheStaffsOwnVitalityLetsGandalfExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asunder = scn.GetFreepsCard("asunder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var focus = scn.GetFreepsCard("focus");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, focus);
		scn.MoveCardsToHand(asunder);

		var troop = scn.GetShadowCard("troop");

		assertAttachedTo(focus, gandalf);
		
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 4 + 1 from the staff: with 2 wounds he has 3 left. Once the staff is discarded he has 2, and can
		// exert only once.
		scn.AddWoundsToChar(gandalf, 2);

		assertEquals(2, scn.GetWoundsOn(gandalf));
		
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(3, scn.GetVitality(gandalf));
		assertAttachedTo(focus, gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(asunder));
	}
}
