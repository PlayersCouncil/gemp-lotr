package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_236_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herugrim", "57_236");
					put("theoden", "4_292");    // Theoden, Son of Thengel: strength 6, vitality 2; a regroup ability used only on purpose
					put("lord", "4_365");       // Theoden, Lord of the Mark: another Theoden, never on the same board
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("guard2", "5_83");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("troop2", "1_177");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HerugrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Herugrim, Sword of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Vitality: +1
		 * Game Text: Bearer must be Théoden.
		 * He is damage +1.
		 * Response: If Théoden is about to take a wound and you cannot spot 3 threats, add 2 threats to prevent
		 * that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("herugrim");

		assertEquals("Herugrim", card.getBlueprint().getTitle());
		assertEquals("Sword of the Mark", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnTheodenOnlyAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herugrim = scn.GetFreepsCard("herugrim");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(theoden, guard, aragorn);
		scn.MoveCardsToHand(herugrim);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(herugrim));

		scn.FreepsPlayCard(herugrim);

		// Theoden is the one bearer it can go on; the [rohan] Man and the [gondor] Man are not asked about.
		assertAttachedTo(herugrim, theoden);
		assertNotAttachedTo(herugrim, guard);
		assertNotAttachedTo(herugrim, aragorn);
		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutTheodenAndAnotherTheodenMayBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herugrim = scn.GetFreepsCard("herugrim");
		var lord = scn.GetFreepsCard("lord");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(herugrim, lord);

		scn.StartGame();

		// A [rohan] Man companion and the Ring-bearer, and no Theoden.
		assertFalse(scn.FreepsPlayAvailable(herugrim));

		scn.FreepsPlayCard(lord);
		assertTrue(scn.FreepsPlayAvailable(herugrim));
		scn.FreepsPlayCard(herugrim);

		assertAttachedTo(herugrim, lord);
		assertEquals(1, scn.GetKeywordCount(lord, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseAddsTwoThreatsToPreventAWoundToTheodenButNotToAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herugrim = scn.GetFreepsCard("herugrim");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(theoden, guard, aragorn);
		scn.AttachCardsTo(theoden, herugrim);

		var troop = scn.GetShadowCard("troop");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{theoden, troop}, new PhysicalCardImpl[]{guard, raiders});

		// A [rohan] Man companion who is not Theoden loses his skirmish, 6 against 9 with damage +1: no offer.
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();
		scn.ShadowPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(herugrim));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetThreats());

		// Theoden, 8 against 13, is about to take a wound.
		scn.FreepsResolveSkirmish(theoden);
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(herugrim));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTwoThreatsItMayBeUsedAgainOnTheSameSkirmishesSecondWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herugrim = scn.GetFreepsCard("herugrim");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Five companions: the threat limit is 5.
		scn.MoveCompanionsToTable(theoden, guard, guard2, aragorn);
		scn.AttachCardsTo(theoden, herugrim);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, raiders);
		scn.FreepsResolveSkirmish(theoden);
		scn.FreepsPass();
		scn.ShadowPass();

		// 9 against 8, and damage +1: two wounds, each about to be taken in turn.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(herugrim));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(theoden));

		// Two threats: 3 cannot be spotted yet.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(herugrim));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(4, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnceThreeThreatsCanBeSpottedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herugrim = scn.GetFreepsCard("herugrim");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Five companions: the threat limit is 5, so 2 more could still be added at 3.
		scn.MoveCompanionsToTable(theoden, guard, guard2, aragorn);
		scn.AttachCardsTo(theoden, herugrim);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.AddThreats(1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, raiders);
		scn.FreepsResolveSkirmish(theoden);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(herugrim));
		scn.FreepsAcceptOptionalTrigger();

		// The first wound is prevented; for the damage +1 wound 3 threats are spotted, so it is not offered
		// and the wound is taken.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(herugrim));
		assertEquals(3, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTwoThreatsCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herugrim = scn.GetFreepsCard("herugrim");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		// Three companions: the threat limit is 3.
		scn.MoveCompanionsToTable(theoden, guard);
		scn.AttachCardsTo(theoden, herugrim);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, troop);
		scn.FreepsResolveSkirmish(theoden);
		scn.FreepsPass();
		scn.ShadowPass();

		// 3 threats cannot be spotted, but only 1 more can be added.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(herugrim));
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
