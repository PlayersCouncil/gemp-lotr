package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "18_97");
					// Erkenbrand, Master of Westfold: [rohan] Man companion, strength 7, vitality 3; his texts are about
					// his own play, and he is cheated in
					put("erkenbrand", "0_59");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, vitality 3; valiant is its whole text
					// Rohirrim Herdsman: a [rohan] Man ally, vitality 2; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions or Éowyn can be
					// spotted; "each time another [rohan] Man exerts, you may exert Merry to heal that Man"
					put("merry", "103_85");
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer; his archery ability is used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf

					put("marksman1", "1_176");  // Goblin Marksman: a [moria] archer minion; Archer is its whole text
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("marksman4", "1_176");
					// Uruk Pursuer: printed fierce, strength 5, vitality 3; its texts are about unbound Hobbits discarded or killed
					put("pursuer", "4_190");
					put("pursuer2", "4_190");
					put("assassin", "2_93");    // Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErkenbrandsShieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Erkenbrand's Shield
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Shield
		 * Game Text: Bearer must be a [rohan] Man.<br>The minion archery total is -1.<br><b>Skirmish:</b> If bearer is Erkenbrand, you may add (2) to cancel a fierce skirmish involving him.<br><b>Response:</b> If a [rohan] Man is about to take a wound, spot Gandalf and exert bearer to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shield");

		assertEquals("Erkenbrand's Shield", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAllyAndMakesTheMinionArcheryTotalMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var legolas = scn.GetFreepsCard("legolas");
		// Two [rohan] companions, the Guard and Merry, and no Éowyn: Merry is no Man.
		scn.MoveCompanionsToTable(guard, aragorn, merry, legolas);
		scn.MoveAlliesToTable(herdsman);

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");

		scn.StartGame();
		scn.MoveCardsToHand(shield);
		assertInHand(shield);
		// The minions stand on the table later in the turn; laid now, the totals are read before and after the play.
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.FreepsRefreshDecision();

		// Two Shadow archers, one Free Peoples archer.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(shield);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, not a [rohan] Hobbit, not an Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merry, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(herdsman);

		assertAttachedTo(shield, herdsman);
		// The minion archery total goes down by one; the fellowship's does not.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// And so it stands when the archery phase comes.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void SkirmishAbilityAddsTwoToCancelErkenbrandsFierceSkirmishAndCancelsNoRegularOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		scn.MoveCompanionsToTable(erkenbrand);
		scn.AttachCardsTo(erkenbrand, shield);
		assertAttachedTo(shield, erkenbrand);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(erkenbrand, pursuer);
		scn.FreepsResolveSkirmish(erkenbrand);
		assertFalse(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(erkenbrand, pursuer));

		// The regular skirmish against the fierce minion: offered, its (2) added, and nothing cancelled (what
		// stands after the "to" is no requirement).
		assertTrue(scn.FreepsActionAvailable(shield));
		int twilight = scn.GetTwilight();
		scn.FreepsUseCardAction(shield);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(erkenbrand, pursuer));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.PassSkirmishActions();
		// 7 against 5: the Pursuer loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(erkenbrand));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(erkenbrand, pursuer);
		scn.FreepsResolveSkirmish(erkenbrand);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(erkenbrand, pursuer));
		assertTrue(scn.FreepsActionAvailable(shield));

		twilight = scn.GetTwilight();
		scn.FreepsUseCardAction(shield);
		assertEquals(twilight + 2, scn.GetTwilight());
		// Cancelled: Erkenbrand would have won again, and the Pursuer has no second wound.
		assertFalse(scn.IsCharSkirmishing(erkenbrand));
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(erkenbrand));
		assertAttachedTo(shield, erkenbrand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsFierceSkirmishItIsPaidForAndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(erkenbrand, guard);
		scn.AttachCardsTo(erkenbrand, shield);
		assertAttachedTo(shield, erkenbrand);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		// No regular skirmish; in the fierce round the Guard alone is assigned.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, pursuer);
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(guard, pursuer));

		// Its bearer is Erkenbrand and its cost can be paid, so it is offered; his is no skirmish to cancel.
		assertTrue(scn.FreepsActionAvailable(shield));
		int twilight = scn.GetTwilight();
		scn.FreepsUseCardAction(shield);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(guard, pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void BorneByARohanManWhoIsNotErkenbrandTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(erkenbrand, guard);
		scn.AttachCardsTo(guard, shield);
		assertAttachedTo(shield, guard);

		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, pursuer2);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, pursuer},
				new PhysicalCardImpl[]{erkenbrand, pursuer2});

		// Erkenbrand is on the table and the Guard bears the Shield: in either fierce skirmish, it is not offered.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shield));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(erkenbrand);
		assertTrue(scn.IsCharSkirmishingAgainst(erkenbrand, pursuer2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shield));
		assertAttachedTo(shield, guard);
	}

	@Test
	public void ResponseExertsAnAllyBearerToPreventAnArcheryWoundToARohanManAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var herdsman = scn.GetFreepsCard("herdsman");
		// Two [rohan] companions, the Guard and Merry, and no Éowyn: Merry is no Man.
		scn.MoveCompanionsToTable(guard, merry, aragorn, gandalf);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(herdsman, shield);
		assertAttachedTo(shield, herdsman);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3", "marksman4");

		// Four archers, -1 for the Shield: three arrows.
		scn.SkipToArcheryWounds();
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(herdsman));

		// The first is for the Guard: offered, the Herdsman exerts, and the wound is not placed.
		scn.FreepsChooseCard(guard);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shield));
		scn.FreepsChooseOptionalTrigger(shield);
		assertEquals(1, scn.GetWoundsOn(herdsman));
		// Merry answers a [rohan] Man exerting; declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(guard));

		// The next for Merry, a [rohan] Hobbit, and the last for Aragorn, a [gondor] Man: not offered.
		scn.FreepsChooseCard(merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shield));
		assertEquals(1, scn.GetWoundsOn(merry));
		scn.FreepsChooseCard(aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shield));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ResponseExertsACompanionBearerToPreventAWoundToARohanManWhoIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard, gandalf);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, shield);
		assertAttachedTo(shield, guard);

		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(0, scn.GetWoundsOn(guard));

		// The one ally is wounded: offered, the Guard exerts, and the wound is not placed.
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shield));
		scn.FreepsChooseOptionalTrigger(shield);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWithAGandalfCompanionWhoIsNotGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(guard, avenger);
		scn.AttachCardsTo(guard, shield);
		assertAttachedTo(shield, guard);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2");

		// Two archers, -1 for the Shield: one arrow.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsChooseCard(guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shield));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhileTheBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard, gandalf);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(herdsman, shield);
		assertAttachedTo(shield, herdsman);
		// Vitality 2, 1 wound: the Herdsman cannot exert.  The Guard, who could, is not the bearer.
		scn.AddWoundsToChar(herdsman, 1);
		assertEquals(1, scn.GetWoundsOn(herdsman));

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2");

		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsChooseCard(guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shield));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
