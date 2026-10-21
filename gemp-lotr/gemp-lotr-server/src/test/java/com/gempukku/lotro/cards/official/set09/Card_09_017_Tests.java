package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knife", "9_17");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man, strength 8
					put("rangersword", "1_112");  // Ranger's Sword: a hand weapon, strength +2; "Bearer must be Aragorn."
					put("boromir", "1_96");       // Boromir, Lord of Gondor: a [gondor] Man who is not Aragorn
					// Sword of Gondor: a hand weapon, "Bearer must be a [gondor] Man"; its damage +1 is for a skirmish
					put("gondorsword", "4_134");
					put("knight", "5_35");        // Gondorian Knight: a [gondor] Man with no text, who bears nothing
					put("merchant", "7_97");      // Gondorian Merchant: the [gondor] Man ally; a maneuver ability only
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit, no Man (cheated in); a skirmish ability only
					put("guard", "5_83");         // Household Guard: a [rohan] Man companion; Valiant only

					put("slayer", "3_93");        // Morgul Slayer: [sauron] Orc, strength 7, vitality 2; a regroup ability only
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KnifeoftheGaladhrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Knife of the Galadhrim
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be a [gondor] Man. If bearer is Aragorn, he may bear this hand weapon in addition to 1 other hand weapon.<br><b>Skirmish:</b> Discard this weapon to wound a minion bearer is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("knife");

		assertEquals("Knife of the Galadhrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AragornMayBearItBesideOneOtherHandWeaponAndAnotherGondorManMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knife = scn.GetFreepsCard("knife");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var boromir = scn.GetFreepsCard("boromir");
		var gondorsword = scn.GetFreepsCard("gondorsword");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.AttachCardsTo(boromir, gondorsword);
		scn.MoveCardsToHand(knife);

		scn.StartGame();

		assertAttachedTo(rangersword, aragorn);
		assertAttachedTo(gondorsword, boromir);
		assertTrue(scn.FreepsPlayAvailable(knife));
		scn.FreepsPlayCard(knife);

		// All three are [gondor] Men.  Aragorn has a hand weapon and may bear the Knife beside it; the Knight has
		// none; Boromir has one and is not Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(knife, aragorn);
		assertAttachedTo(rangersword, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBeBorneByAGondorManCompanionOrAllyAndByNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knife = scn.GetFreepsCard("knife");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(knife);

		scn.StartGame();

		scn.FreepsPlayCard(knife);
		// Two [gondor] Man companions and the [gondor] Man ally; a [gondor] Hobbit, a [rohan] Man and Frodo are not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(knife, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AragornBearingTheKnifeMayBeGivenOneOtherHandWeaponAndNotASecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knife = scn.GetFreepsCard("knife");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var boromir = scn.GetFreepsCard("boromir");
		var gondorsword = scn.GetFreepsCard("gondorsword");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		scn.AttachCardsTo(aragorn, knife);
		scn.MoveCardsToHand(rangersword, gondorsword);

		scn.StartGame();

		// The Knife first, then the other hand weapon.
		assertAttachedTo(knife, aragorn);
		assertTrue(scn.FreepsPlayAvailable(rangersword));
		scn.FreepsPlayCard(rangersword);
		assertAttachedTo(rangersword, aragorn);
		assertAttachedTo(knife, aragorn);

		// With the Knife and one other, Aragorn has no room for a third; Boromir and the Knight, unarmed, have.
		scn.FreepsPlayCard(gondorsword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(boromir);

		assertAttachedTo(gondorsword, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherGondorManBearingTheKnifeMayNotBeGivenAnotherHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knife = scn.GetFreepsCard("knife");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gondorsword = scn.GetFreepsCard("gondorsword");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		scn.AttachCardsTo(boromir, knife);
		scn.MoveCardsToHand(gondorsword);

		scn.StartGame();

		assertAttachedTo(knife, boromir);
		scn.FreepsPlayCard(gondorsword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		scn.FreepsChooseCard(knight);

		assertAttachedTo(gondorsword, knight);
		assertAttachedTo(knife, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsTheKnifeToWoundAMinionBearerIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knife = scn.GetFreepsCard("knife");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, knife);

		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, savage);
		// Vitality 3 with 2 wounds: exhausted, so a wound kills it where an exertion could not be made.
		scn.AddWoundsToChar(savage, 2);

		assertAttachedTo(knife, aragorn);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(knife));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, slayer);
		// The Shadow player puts his other minion on Aragorn too.
		scn.ShadowAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(slayer));
		assertTrue(scn.FreepsActionAvailable(knife));
		scn.FreepsUseCardAction(knife);

		assertInDiscard(knife);
		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInDiscard(savage);
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItWoundsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knife = scn.GetFreepsCard("knife");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, knife);

		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, savage);

		assertAttachedTo(knife, aragorn);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, slayer}, new PhysicalCardImpl[]{aragorn, savage});
		// Boromir's skirmish first; the Savage waits on Aragorn, whose own skirmish has not begun.
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(knife));
		scn.FreepsUseCardAction(knife);

		assertInDiscard(knife);
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
