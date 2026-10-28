package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "7_113");
					put("pippin", "1_306");       // Pippin, Friend to Frodo: [shire] Hobbit companion, strength 3, vitality 4; his text is about [shire] tales
					put("trollslayer", "103_31"); // Pippin, Trollslayer: the [gondor] Pippin; cheated in past his "To play", a skirmish ability only
					put("merry", "1_302");        // Merry, Friend to Sam: a Hobbit companion who is no Pippin; a skirmish ability only

					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("merchant", "7_97");      // Gondorian Merchant: [gondor] Man ally; a maneuver ability only, needing initiative
					put("deadman", "10_27");      // Dead Man of Dunharrow: a [gondor] companion who is a Wraith (cheated in: no threat, no exertions)
					put("guard", "5_83");         // Household Guard: a [rohan] Man companion; Valiant is its whole text

					put("party1", "1_158");       // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1 and nothing else
					put("party2", "1_158");
					put("nelya", "2_84");         // Úlairë Nelya: strength 10; fierce and damage +1 while 2 burdens can be spotted
					put("marksman1", "1_176");    // Goblin Marksman: strength 7, archer and nothing else
					put("marksman2", "1_176");
					put("skirmisher1", "18_90");  // Orkish Skirmisher: strength 9; each time it wins, may exert twice to wound a companion
					put("skirmisher2", "18_90");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinsArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Pippin's Armor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Armor
		 * Strength: +2
		 * Vitality: -1
		 * Game Text: Bearer must be Pippin.
		 * While you can spot 3 [gondor] Men, Pippin may take no more than 1 wound in a skirmish.
		*/

		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");

		assertEquals("Pippin's Armor", armor.getBlueprint().getTitle());
		assertNull(armor.getBlueprint().getSubtitle());
		assertTrue(armor.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, armor.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, armor.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, armor.getBlueprint().getCardType());
		assertTrue(armor.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, armor.getBlueprint().getPossessionClasses().size());
		assertEquals(0, armor.getBlueprint().getTwilightCost());
		assertEquals(2, armor.getBlueprint().getStrength());
		assertEquals(-1, armor.getBlueprint().getVitality());
	}

	@Test
	public void IsNotPlayableWithoutPippinAndGoesOnPippinOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(armor);
		assertInHand(armor);

		scn.StartGame();

		// A Hobbit companion of Pippin's culture who is no Pippin, and the Ring-bearer: not playable.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(armor));

		scn.MoveCompanionsToTable(pippin);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);

		// Pippin is the one bearer it can go on: nothing is asked.
		assertAttachedTo(armor, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedOnTheGondorPippin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(trollslayer, merry);
		scn.MoveCardsToHand(armor);
		assertInHand(armor);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);

		assertAttachedTo(armor, trollslayer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithThreeGondorMenSpottedPippinLosingToADamagePlusOneMinionTakesOneWoundAndBoromirTakesTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		// Exactly 3 [gondor] Men: two companions and the ally in the support area.
		scn.MoveCompanionsToTable(pippin, boromir, aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(pippin, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2);
		assertAttachedTo(armor, pippin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, party1}, new PhysicalCardImpl[]{boromir, party2});

		// Strength 9 against Pippin's 5 (3 + 2): he loses, not overwhelmed. Damage +1 makes it 2 wounds; 1 is taken.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pippin));

		// Strength 9 against 7: Boromir, a [gondor] Man beside the bearer, takes both.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoGondorMenAGondorWraithAndARohanManPippinTakesBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var deadman = scn.GetFreepsCard("deadman");
		var guard = scn.GetFreepsCard("guard");
		var party1 = scn.GetShadowCard("party1");
		// Two [gondor] Men, and beside them a [gondor] companion who is no Man and a Man who is not [gondor].
		scn.MoveCompanionsToTable(pippin, boromir, deadman, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(pippin, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		assertAttachedTo(armor, pippin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, party1);

		scn.FreepsResolveSkirmish(pippin);
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.PassSkirmishActions();

		// 9 against 5 with damage +1: both wounds land. Vitality 3 (4 - 1): he lives.
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertInPlay(pippin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheGondorPippinTakesOneWoundInEachSkirmishOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var pippin = scn.GetFreepsCard("trollslayer");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(pippin, boromir, aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(pippin, armor);

		scn.StartGame();
		// The [gondor] Pippin bears it.  Two burdens make Úlairë Nelya fierce and damage +1 by her own text, so
		// that Pippin fights her in two skirmishes of one turn and loses each for 2 wounds.  His strength is raised
		// to 6 only so that her 10 does not overwhelm him (he is enduring: +2 for each wound after that).
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(nelya);
		scn.ApplyAdHocModifier(new StrengthModifier(null, pippin, null, 1));
		assertAttachedTo(armor, pippin);
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, nelya);
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(6, scn.GetStrength(pippin));
		scn.PassSkirmishActions();

		// 2 wounds, 1 taken.
		assertEquals(1, scn.GetWoundsOn(pippin));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(pippin, nelya);
		scn.FreepsResolveSkirmish(pippin);
		scn.PassFierceSkirmishActions();

		// A new skirmish: 2 more wounds, 1 taken again (one cap for the turn would leave him at 1; none, at 4
		// wounds and dead, vitality 3).
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertInPlay(pippin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PippinTakesTwoArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(pippin, boromir, aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(pippin, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		assertAttachedTo(armor, pippin);
		scn.SkipToArcheryWounds();

		// The archery phase is no skirmish: both arrows land, the 3 [gondor] Men spotted all the while.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.FreepsChooseCard(pippin);
		scn.FreepsChooseCard(pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void PippinAssignedToNoSkirmishTakesOneWoundOfTwoInAnotherCompanionsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var skirmisher1 = scn.GetShadowCard("skirmisher1");
		var skirmisher2 = scn.GetShadowCard("skirmisher2");
		scn.MoveCompanionsToTable(pippin, boromir, aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(pippin, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher1, skirmisher2);
		// Boromir faces both Skirmishers (defender +1) and loses without being overwhelmed: 10 against 18.
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 3));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, boromir, null, Keyword.DEFENDER, 1));
		assertAttachedTo(armor, pippin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, skirmisher1, skirmisher2);
		scn.FreepsResolveSkirmish(boromir);

		assertFalse(scn.IsCharAssigned(pippin));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.PassSkirmishActions();

		// Each Skirmisher has won, and wounds a companion of the Shadow player's choice in this skirmish phase.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher1));
		scn.ShadowChooseOptionalTrigger(skirmisher1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(pippin));
		scn.ShadowChooseCard(pippin);
		assertEquals(2, scn.GetWoundsOn(skirmisher1));
		assertEquals(1, scn.GetWoundsOn(pippin));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher2));
		scn.ShadowAcceptOptionalTrigger();

		// Pippin, in none of this skirmish, has taken his one wound of it: he can take no second, and so cannot be
		// chosen to be wounded (CR 4.2 "wound"); Aragorn can.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pippin));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(skirmisher2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
