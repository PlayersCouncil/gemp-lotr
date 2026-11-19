package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_284_Tests
{

	// The Fellowship path, so that Edoras Hall 7_330 (each [rohan] possession costs 1 less) is not under the play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mail", "4_284");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3, valiant only
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T; a regroup ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit; a Man only beside 3 [rohan] companions or Eowyn
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4
					put("renowned", "13_137");  // Théoden, The Renowned: strength 7, vitality 3; his texts need Éowyn, Éomer or Théodred
					put("thengel", "4_292");    // Théoden, Son of Thengel: strength 6, vitality 2; a regroup ability used only on purpose

					put("party1", "1_158");     // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1 and nothing else
					put("party2", "1_158");
					put("nelya", "2_84");       // Úlairë Nelya: strength 10; fierce and damage +1 while 2 burdens can be spotted
					put("marksman1", "1_176");  // Goblin Marksman: strength 7, archer and nothing else
					put("marksman2", "1_176");
					put("skirmisher", "18_90"); // Orkish Skirmisher: strength 9; each time he wins, may exert twice to wound a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path, where the Herdsman's home is site 3, Barrows of Edoras 4_337 (a sanctuary whose fellowship
	// ability is never used); site 2 is Eastfold 4_331, whose Shadow ability needs 3 [isengard] trackers.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mail", "4_284");
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, strength 4, vitality 2
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingsMailStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: King's Mail
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	Bearer takes no more than 1 wound during each skirmish phase. If bearer is Théoden, he may not take wounds except during a skirmish involving him.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mail");

		assertEquals("King's Mail", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mail = scn.GetFreepsCard("mail");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(mail);
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(mail));

		scn.FreepsPlayCard(mail);
		// A [rohan] Man companion and a [rohan] Man ally; not Merry, a [rohan] Hobbit (two [rohan] companions only),
		// Aragorn, a [gondor] Man, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(herdsman);

		assertAttachedTo(mail, herdsman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerLosingToADamagePlusOneMinionTakesOneWoundAndAnotherManTakesTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mail = scn.GetFreepsCard("mail");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.AttachCardsTo(guard, mail);
		assertAttachedTo(mail, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, party1}, new PhysicalCardImpl[]{aragorn, party2});

		// Strength 9 against 6: the Guard loses, but is not overwhelmed. Damage +1 makes it 2 wounds, and 1 gets through.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));

		// Strength 9 against 8: Aragorn, a Man companion who does not bear it, takes both.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerTakesOneWoundInEachSkirmishPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mail = scn.GetFreepsCard("mail");
		var guard = scn.GetFreepsCard("guard");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mail);
		assertAttachedTo(mail, guard);

		scn.StartGame();
		// Two burdens make Úlairë Nelya fierce and damage +1 by her own text, so that the Guard fights her in two
		// skirmish phases of one turn and loses each for 2 wounds.
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, nelya);
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.PassSkirmishActions();

		// Strength 10 against 6, 2 wounds: 1 taken.
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, nelya);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();

		// A new skirmish phase: 2 more wounds, 1 taken again. Vitality 3: he lives.
		assertEquals(2, scn.GetWoundsOn(guard));
		assertInPlay(guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerTakesTwoArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mail = scn.GetFreepsCard("mail");
		var guard = scn.GetFreepsCard("guard");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mail);
		assertAttachedTo(mail, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToArcheryWounds();

		// The archery phase is not a skirmish phase: both arrows land.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsChooseCard(guard);
		scn.FreepsChooseCard(guard);
		assertEquals(2, scn.GetWoundsOn(guard));

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAllyBearerAtHisHomeSiteTakesOneWoundOfTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var mail = scn.GetFreepsCard("mail");
		var herdsman = scn.GetFreepsCard("herdsman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(herdsman, mail);
		assertAttachedTo(mail, herdsman);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);
		// The fellowship moves on to site 3[T], the Herdsman's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(herdsman, savage);

		// Strength 5 against 4: he loses and is not overwhelmed. Damage +1 makes it 2 wounds, which would kill him at
		// vitality 2; 1 gets through.
		scn.FreepsResolveSkirmish(herdsman);
		assertEquals(0, scn.GetWoundsOn(herdsman));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertInZone(Zone.SUPPORT, herdsman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BorneByTheodenHeIsNotOfferedAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mail = scn.GetFreepsCard("mail");
		var thengel = scn.GetFreepsCard("thengel");
		var guard = scn.GetFreepsCard("guard");
		var marksman1 = scn.GetShadowCard("marksman1");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thengel, guard);
		scn.AttachCardsTo(thengel, mail);
		assertAttachedTo(mail, thengel);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1);
		scn.SkipToArcheryWounds();

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thengel));
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(thengel));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void BorneByAnotherRohanManItLeavesHimAndTheodenOpenToArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mail = scn.GetFreepsCard("mail");
		var thengel = scn.GetFreepsCard("thengel");
		var guard = scn.GetFreepsCard("guard");
		var marksman1 = scn.GetShadowCard("marksman1");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thengel, guard);
		scn.AttachCardsTo(guard, mail);
		assertAttachedTo(mail, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1);
		scn.SkipToArcheryWounds();

		// The bearer is no Théoden, and the Théoden bears no King's Mail: both may take the wound.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, thengel, frodo));
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(thengel));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void BorneByTheodenHeTakesNoWoundInAnothersSkirmishAndOneInHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mail = scn.GetFreepsCard("mail");
		var renowned = scn.GetFreepsCard("renowned");
		var guard = scn.GetFreepsCard("guard");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var party1 = scn.GetShadowCard("party1");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(renowned, guard);
		scn.AttachCardsTo(renowned, mail);
		assertAttachedTo(mail, renowned);

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher, party1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, skirmisher}, new PhysicalCardImpl[]{renowned, party1});

		// The Guard's skirmish first: 9 against 6, he loses and takes a wound.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));

		// The Skirmisher's wound for his win: Théoden, waiting for his own skirmish, is not offered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(renowned));
		scn.ShadowChooseCard(guard);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(renowned));

		// His own skirmish: 9 against 7, damage +1, 2 wounds; he may take wounds here, and takes no more than 1.
		scn.FreepsResolveSkirmish(renowned);
		assertEquals(0, scn.GetWoundsOn(renowned));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(renowned));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
