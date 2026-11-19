package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "17_37");
					// Saruman's Staff, Wizard's Device: an [isengard] artifact that plays on Saruman (strength +2, fierce,
					// damage +1).  "Maneuver: Make the first sentence of Saruman's game text not apply until the regroup
					// phase."
					put("staff", "4_174");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
					put("tracker", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3; Hunter 1 is its whole text
					put("tracker2", "15_171");

					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability only
					put("elf", "1_53");         // Lorien Elf: an Elf companion with no keyword; "To play, spot an Elf"
					// Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is its whole text
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, cost 2; Knight is its whole text
					// Faramir, Captain of Ithilien: a companion printed Hunter 2, strength 7, vitality 3; a maneuver
					// heal used only on purpose
					put("faramir", "18_48");
					// Gondorian Merchant: a [gondor] Man ally; his maneuver ability is used only on purpose
					put("merchant", "7_97");
					// Quick As May Be: Maneuver event, cost 1.  "Spot a Dwarf companion and an Elf companion to wound
					// a minion."
					put("quick1", "4_53");
					put("quick2", "4_53");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Saruman, Instigator of Insurrection
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Saruman cannot be assigned to a skirmish.<br>While you can spot 6 companions, each minion gains <b>hunter 2</b>.<br><b>Response:</b> If a hunter minion is about to take a wound, exert Saruman to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Instigator of Insurrection", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SarumanCannotBeAssignedToASkirmishByEitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToAssignments();

		// The Troop on the same board is offered to each player in turn; Saruman to neither.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.FreepsCanAssign(troop));
		assertFalse(scn.FreepsCanAssign(saruman));

		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.ShadowCanAssign(troop));
		assertFalse(scn.ShadowCanAssign(saruman));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyBySarumansStaffHeIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		// Until the Staff's ability is used the sentence is in force.
		assertFalse(scn.FreepsCanAssign(saruman));
		assertFalse(scn.ShadowCanAssign(saruman));
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		assertTrue(scn.FreepsCanAssign(saruman));
		assertTrue(scn.ShadowCanAssign(saruman));

		// The Free Peoples player is offered him beside the Troop, and takes the Troop; the Shadow player then
		// assigns Saruman.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		scn.FreepsAssignToMinions(aragorn, troop);
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(saruman));
		scn.ShadowAssignToMinions(aragorn, saruman);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, saruman));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyEachMinionStillGainsHunterAndHisResponseIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var quick1 = scn.GetFreepsCard("quick1");
		// Six companions, Frodo among them.
		scn.MoveCompanionsToTable(gimli, elf, aragorn, guard, knight);
		scn.MoveCardsToHand(quick1);
		assertInHand(quick1);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetKeywordCount(troop, Keyword.HUNTER));
		assertFalse(scn.FreepsCanAssign(saruman));
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);

		// Only the first sentence is switched off: Saruman can now be assigned, and the second and third
		// sentences go on working.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsCanAssign(saruman));
		assertEquals(2, scn.GetKeywordCount(troop, Keyword.HUNTER));

		scn.FreepsPlayCard(quick1);
		scn.FreepsChooseCard(troop);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);

		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void FiveCompanionsAndAnAllyGiveNoMinionHunterAndASixthCompanionPlayedGivesEachMinionHunter2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var knight = scn.GetFreepsCard("knight");
		// Five companions, Frodo among them, and an ally: six characters.
		scn.MoveCompanionsToTable(gimli, elf, aragorn, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(knight);
		assertInHand(knight);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Minions that will stand in play later in the turn, laid early.
		scn.MoveMinionsToTable(saruman, troop, savage);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The sixth companion.
		scn.FreepsPlayCard(knight);
		assertInZone(Zone.FREE_CHARACTERS, knight);

		// Every minion, of three cultures and races, Saruman himself among them; no companion or ally.
		assertEquals(2, scn.GetKeywordCount(saruman, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(troop, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSixCompanionsTheResponseExertsSarumanToPreventAWoundToAMinionMadeAHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var quick1 = scn.GetFreepsCard("quick1");
		scn.MoveCompanionsToTable(gimli, elf, aragorn, guard, knight);
		scn.MoveCardsToHand(quick1);
		assertInHand(quick1);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Troop has no hunter of its own: it is a hunter by the second sentence.
		assertEquals(2, scn.GetKeywordCount(troop, Keyword.HUNTER));
		scn.FreepsPlayCard(quick1);
		scn.FreepsChooseCard(troop);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.ShadowChooseOptionalTrigger(saruman);

		// Saruman exerts and the Troop's wound is prevented; the Savage, another hunter minion, is not touched.
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithFiveCompanionsTheResponseIsNotOfferedForAPlainMinionAndIsForAPrintedHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var quick1 = scn.GetFreepsCard("quick1");
		var quick2 = scn.GetFreepsCard("quick2");
		scn.MoveCompanionsToTable(gimli, elf, aragorn, guard);
		scn.MoveCardsToHand(quick1, quick2);
		assertInHand(quick1, quick2);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var tracker = scn.GetShadowCard("tracker");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop, tracker);
		scn.SkipToPhase(Phase.MANEUVER);

		// Five companions: the Troop is no hunter, and its wound is not answered.
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(tracker, Keyword.HUNTER));
		scn.FreepsPlayCard(quick1);
		scn.FreepsChooseCard(troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// The Tracking Uruk is printed Hunter 1: answered.  Declined, it takes the wound.
		scn.FreepsPlayCard(quick2);
		scn.FreepsChooseCard(tracker);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(tracker));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhenSeveralMinionsAreWoundedTogetherInALostSkirmishTheResponseSavesOnlyAHunterMinionChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		// Five companions, Frodo among them: no minion is a hunter by Saruman, so the Troop is none.
		scn.MoveCompanionsToTable(gimli, elf, aragorn, guard);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var tracker = scn.GetShadowCard("tracker");
		var tracker2 = scn.GetShadowCard("tracker2");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop, tracker, tracker2);
		// Aragorn, 8 + 40, beats the three together, 15 + 15 + 13 (each Tracking Uruk is Hunter 1 against a
		// companion who is no hunter), and is far from doubling them: nobody is overwhelmed, and the three lose
		// together and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 40));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowAssignToMinions(aragorn, tracker, tracker2);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(48, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(tracker, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(tracker2, Keyword.HUNTER));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(tracker));
		assertEquals(0, scn.GetWoundsOn(tracker2));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.PassSkirmishActions();

		// Two hunters are about to take a wound, beside a minion who is none.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowChooseOptionalTrigger(saruman);
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracker, tracker2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tracker);
		assertEquals(1, scn.GetWoundsOn(saruman));

		// That wound alone is prevented: the other Tracking Uruk and the Troop take theirs.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(tracker));
		assertEquals(1, scn.GetWoundsOn(tracker2));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundToAHunterCompanionIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var faramir = scn.GetFreepsCard("faramir");
		// Six companions, so that every minion is a hunter too.
		scn.MoveCompanionsToTable(gimli, elf, aragorn, guard, faramir);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, troop);
		scn.FreepsResolveSkirmish(faramir);

		// Both are hunters, so neither has a bonus: 13 against 7, and Faramir takes the wound.
		assertEquals(2, scn.GetKeywordCount(faramir, Keyword.HUNTER));
		assertEquals(0, scn.GetWoundsOn(faramir));
		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhileSarumanIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var quick1 = scn.GetFreepsCard("quick1");
		scn.MoveCompanionsToTable(gimli, elf);
		scn.MoveCardsToHand(quick1);
		assertInHand(quick1);

		var saruman = scn.GetShadowCard("saruman");
		var tracker = scn.GetShadowCard("tracker");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, tracker);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(saruman, 3);
		assertEquals(3, scn.GetWoundsOn(saruman));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(quick1);
		scn.FreepsChooseCard(tracker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(1, scn.GetWoundsOn(tracker));
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
