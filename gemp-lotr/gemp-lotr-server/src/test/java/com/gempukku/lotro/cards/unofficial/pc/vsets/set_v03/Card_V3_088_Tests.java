package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("snowmane", "103_88");
					// Theoden, Rekindled King: a [rohan] Man, strength 7, vitality 3; his cost text and a fellowship ability never used here
					put("theoden", "7_255");
					// Theoden, Last of His House: the title written without its accent; stronger only for valiant companions assigned to skirmishes
					put("theoden2", "102_55");
					// Household Guard: a [rohan] Man, strength 6, vitality 3; valiant and nothing else
					put("guard1", "5_83");
					put("guard2", "5_83");
					put("eowyn", "5_122");       // Eowyn, Daughter of Eomund: a [rohan] Man, strength 6; a maneuver ability never used here
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, strength 3. He is a Man too beside Eowyn or with 3 [rohan] companions
					put("merry", "103_85");
					put("boromir", "1_97");      // Boromir, Son of Denethor: a [gondor] Man, strength 7; skirmish: exert him to make a Hobbit stronger
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("guma", "4_277");        // Guma, Plains Farmer: a [rohan] Man who is an ally, strength 4; a fellowship ability never used here
					// Rider's Mount: a mount, cost 1, for a [rohan] Man; it changes no strength
					put("ridermount1", "4_287");
					put("ridermount2", "4_287");
					// Horse of Rohan: a mount for a Man, an Elf or a Wizard; it changes no strength
					put("horse1", "4_283");
					put("horse2", "4_283");
					put("chain", "4_278");       // Heavy Chain: a [rohan] possession for a Man that is no mount and changes no strength

					put("detachment", "7_191");  // Morgul Detachment: a [wraith] Orc, strength 15, vitality 4; fierce and damage +1 only beside a Nazgul
					put("siege", "8_106");       // Siege Troop: a [sauron] Orc, strength 20, vitality 4; besieger, with a regroup ability
					put("troop", "1_177");       // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("sneak", "17_58");       // Sunland Sneak: strength 14, vitality 3; its text is about its cost
					put("outrider", "13_172");   // Uruk Outrider: strength 15, vitality 2; damage +1 for each character in the dead pile
					put("troll", "1_165");       // Cave Troll of Moria, Scourge of the Black Pit: strength 15, vitality 4, fierce, damage +1
					put("marksman", "1_176");    // Goblin Marksman: an archer and nothing else, strength 7
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SnowmaneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Snowmane, Faithful Servant
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Vitality: +1
		 * Game Text: Bearer must be Theoden.
		 * 	[Rohan] companions are strength +1 (or +2 if mounted) for each wound on Theoden.
		 * 	At the start of the assignment phase, the Shadow player may exhaust a minion of strength 15 or more to hinder Snowmane.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("snowmane");

		assertEquals("Snowmane", card.getBlueprint().getTitle());
		assertEquals("Faithful Servant", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedForOneOnATheodenAndOnNoOneElseAndRaisesHisVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard1");
		var boromir = scn.GetFreepsCard("boromir");
		// Beside Theoden: a [rohan] Man who is not Theoden, a [gondor] Man and the Ring-bearer.
		scn.MoveCompanionsToTable(theoden, guard, boromir);
		scn.MoveCardsToHand(snowmane);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetVitality(theoden));
		assertEquals(3, scn.GetVitality(guard));
		assertTrue(scn.FreepsPlayAvailable(snowmane));

		scn.FreepsPlayCard(snowmane);

		// Theoden is the one bearer there is: nobody is asked which of the four companions is to bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(snowmane, theoden);
		assertEquals(4, scn.GetVitality(theoden));
		assertEquals(3, scn.GetVitality(guard));
		assertEquals(3, scn.GetVitality(boromir));
	}

	@Test
	public void IsPlayedOnTheTheodenWhoseTitleIsWrittenWithoutItsAccent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		// Theoden, Last of His House: the one Theoden to a table, here the one printed "Theoden" and not "Théoden".
		var theoden2 = scn.GetFreepsCard("theoden2");
		var guard = scn.GetFreepsCard("guard1");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(theoden2, guard, boromir);
		scn.MoveCardsToHand(snowmane);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetVitality(theoden2));
		assertTrue(scn.FreepsPlayAvailable(snowmane));

		scn.FreepsPlayCard(snowmane);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(snowmane, theoden2);
		assertEquals(4, scn.GetVitality(theoden2));
		assertEquals(3, scn.GetVitality(guard));
	}

	@Test
	public void CannotBePlayedWithoutATheoden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("eowyn"), scn.GetFreepsCard("boromir"));
		scn.MoveCardsToHand(snowmane);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(snowmane));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RohanCompanionsAreStrongerByOneForEachWoundOnTheodenAndByTwoIfMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var eowyn = scn.GetFreepsCard("eowyn");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(theoden, guard1, guard2, eowyn, boromir);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(theoden, snowmane);
		scn.AttachCardsTo(eowyn, scn.GetFreepsCard("ridermount1"));
		// Mounted, and not [rohan]
		scn.AttachCardsTo(boromir, scn.GetFreepsCard("horse1"));
		// Mounted and [rohan], and no companion
		scn.AttachCardsTo(guma, scn.GetFreepsCard("horse2"));
		// A [rohan] companion bearing a possession that is no mount
		scn.AttachCardsTo(guard2, scn.GetFreepsCard("chain"));
		// A wound on a [rohan] companion who is not Theoden
		scn.AddWoundsToChar(guard1, 1);

		scn.StartGame();

		int frodoStrength = scn.GetStrength(frodo);

		// No wound on Theoden: nobody is stronger.
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));

		scn.AddWoundsToChar(theoden, 1);

		// Theoden bears Snowmane, so he is mounted himself: 7 + 2
		assertEquals(9, scn.GetStrength(theoden));
		assertEquals(7, scn.GetStrength(guard1));
		assertEquals(7, scn.GetStrength(guard2));
		// 6 + 2 on her Rider's Mount
		assertEquals(8, scn.GetStrength(eowyn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(frodoStrength, scn.GetStrength(frodo));

		scn.AddWoundsToChar(theoden, 1);

		// Two wounds on Theoden, and three on [rohan] companions in all: 7 + 2 x 2
		assertEquals(11, scn.GetStrength(theoden));
		// 6 + 2 x 1, whatever wounds he has himself
		assertEquals(8, scn.GetStrength(guard1));
		assertEquals(8, scn.GetStrength(guard2));
		// 6 + 2 x 2
		assertEquals(10, scn.GetStrength(eowyn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanCompanionWhoIsNoManIsStrongerToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(theoden, merry, boromir);
		scn.AttachCardsTo(theoden, snowmane);

		scn.StartGame();

		// Two [rohan] companions and no Eowyn: Merry is a Hobbit and no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(7, scn.GetStrength(boromir));

		scn.AddWoundsToChar(theoden, 2);

		// 3 + 2 for Merry; nothing for Boromir, a Man who is not of [rohan]
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMountPlayedOnACompanionAndAWoundTheodenTakesEachRaiseTheBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard1");
		var ridermount = scn.GetFreepsCard("ridermount1");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.AttachCardsTo(theoden, snowmane);
		scn.MoveCardsToHand(ridermount);
		scn.AddWoundsToChar(theoden, 1);

		scn.StartGame();

		// 6 + 1
		assertEquals(7, scn.GetStrength(guard));

		// Theoden has his mount already, so the Rider's Mount goes on the Household Guard unasked.
		scn.FreepsPlayCard(ridermount);

		assertAttachedTo(ridermount, guard);
		// 6 + 2, now that he is mounted
		assertEquals(8, scn.GetStrength(guard));

		scn.MoveMinionsToTable(marksman);
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(theoden);

		assertEquals(2, scn.GetWoundsOn(theoden));
		// 6 + 2 x 2
		assertEquals(10, scn.GetStrength(guard));
		// 7 + 2 x 2
		assertEquals(11, scn.GetStrength(theoden));
		// The Marksman's strength is 7: the Shadow player is asked nothing.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtTheStartOfTheAssignmentPhaseTheShadowPlayerMayExhaustAMinionOfStrengthFifteenOrMoreToHinderSnowmane() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard1");
		var detachment = scn.GetShadowCard("detachment");
		var siege = scn.GetShadowCard("siege");
		var troop = scn.GetShadowCard("troop");
		var sneak = scn.GetShadowCard("sneak");
		var outrider = scn.GetShadowCard("outrider");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.AttachCardsTo(theoden, snowmane);
		scn.AddWoundsToChar(theoden, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(detachment, siege, troop, sneak, outrider);
		// 13 made 15 by another effect
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 2));
		// Strength 15 with its one spare vitality gone: it cannot be exhausted.
		scn.AddWoundsToChar(outrider, 1);

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowDecisionAvailable("exhaust"));

		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.ShadowDecisionAvailable("exhaust"));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The Shadow player is asked, and I am not.
		assertTrue(scn.ShadowDecisionAvailable("exhaust a minion of strength 15 or more to hinder"));
		assertFalse(scn.FreepsDecisionAvailable("exhaust"));
		assertFalse(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(15, scn.GetStrength(detachment));
		assertEquals(15, scn.GetStrength(troop));
		assertEquals(14, scn.GetStrength(sneak));
		// 7 + 2, mounted; 6 + 1
		assertEquals(9, scn.GetStrength(theoden));
		assertEquals(7, scn.GetStrength(guard));
		// 3 + 1 for Snowmane, less the wound
		assertEquals(3, scn.GetVitality(theoden));

		scn.ShadowChooseYes();

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		// Exactly 15, more than 15, and 15 by another effect; of three cultures
		assertTrue(scn.ShadowHasCardChoiceAvailable(detachment, siege, troop));
		// One short at 14, an exhausted minion of 15, and no companion
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sneak, outrider, theoden, guard));

		scn.ShadowChooseCard(detachment);

		// Vitality 4
		assertEquals(3, scn.GetWoundsOn(detachment));
		assertEquals(0, scn.GetWoundsOn(siege));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(sneak));
		assertEquals(1, scn.GetWoundsOn(outrider));
		assertTrue(scn.IsHindered(snowmane));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(guard));
		// One wound on a vitality of 3 again: he lives.
		assertEquals(2, scn.GetVitality(theoden));
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DeclinedItIsOfferedAgainAtTheStartOfTheFierceAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(theoden, aragorn);
		scn.AttachCardsTo(theoden, snowmane);
		scn.AddWoundsToChar(theoden, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.ShadowDecisionAvailable("exhaust a minion of strength 15 or more to hinder"));

		scn.ShadowChooseNo();

		assertEquals(0, scn.GetWoundsOn(troll));
		assertFalse(scn.IsHindered(snowmane));
		// 7 + 2
		assertEquals(9, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, troll);
		// Aragorn's 8 loses to the Troll's 15, which is fierce and must be faced again.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowDecisionAvailable("exhaust a minion of strength 15 or more to hinder"));

		// The Troll is the only minion: nothing more is asked.
		scn.ShadowChooseYes();

		// Vitality 4
		assertEquals(3, scn.GetWoundsOn(troll));
		assertTrue(scn.IsHindered(snowmane));
		assertEquals(7, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoMinionOfStrengthFifteenTheShadowPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sneak = scn.GetShadowCard("sneak");
		scn.MoveCompanionsToTable(theoden, aragorn);
		scn.AttachCardsTo(theoden, snowmane);

		scn.StartGame();
		scn.MoveMinionsToTable(sneak);
		// A character of strength 15 who is no minion: 8 + 7
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 7));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(14, scn.GetStrength(sneak));
		assertEquals(15, scn.GetStrength(aragorn));
		assertFalse(scn.ShadowDecisionAvailable("exhaust"));
		assertFalse(scn.IsHindered(snowmane));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithOnlyAnExhaustedMinionOfStrengthFifteenTheShadowPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var outrider = scn.GetShadowCard("outrider");
		var sneak = scn.GetShadowCard("sneak");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);

		scn.StartGame();
		scn.MoveMinionsToTable(outrider, sneak);
		// Vitality 2: one wound and it is exhausted, and an exhausted character cannot be exhausted.
		scn.AddWoundsToChar(outrider, 1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(15, scn.GetStrength(outrider));
		assertFalse(scn.ShadowDecisionAvailable("exhaust"));
		assertFalse(scn.IsHindered(snowmane));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void HinderingSnowmaneKillsATheodenWhoWasAliveByItsVitalityAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var detachment = scn.GetShadowCard("detachment");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);
		// Vitality 3 + 1 with 3 wounds
		scn.AddWoundsToChar(theoden, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(detachment);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(1, scn.GetVitality(theoden));
		assertInZone(Zone.FREE_CHARACTERS, theoden);

		// The Morgul Detachment is the only minion: nothing more is asked.
		scn.ShadowChooseYes();

		assertEquals(3, scn.GetWoundsOn(detachment));
		// Hindered, Snowmane gives no vitality: three wounds on a vitality of 3. It goes to the discard pile with him.
		assertInZone(Zone.DEAD, theoden);
		assertInZone(Zone.DISCARD, snowmane);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
