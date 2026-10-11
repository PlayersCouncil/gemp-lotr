package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Two dots: two copies may be in play.
					put("calenhad1", "103_86");
					put("calenhad2", "103_86");
					// War-beacon: a [gondor] beacon, cost 1, to play spot an unbound Man; fellowship abilities never used here
					put("war1", "103_35");
					put("war2", "103_35");
					put("war3", "103_35");
					put("halifirien", "103_87"); // Northern Signal-fire, Flame of Halifirien: a [rohan] beacon; its text is about mounts
					put("catapult", "8_32");     // Catapult: a [gondor] possession in the support area that is no beacon; a maneuver ability never used here

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man, ranger, strength 8, vitality 4; a maneuver ability never used here
					put("heir", "4_109");        // Aragorn, Heir of Elendil: defender +1 and nothing else
					put("prowler", "15_61");     // Gondorian Prowler: a [gondor] Man, ranger and hunter 1, vitality 3; a skirmish ability never used here
					put("armor", "18_39");       // Armor of the White City: [gondor] armor, strength +1. Bearer must be a [gondor] Man. Bearer is a knight
					put("boromir", "1_97");      // Boromir, Son of Denethor: a [gondor] Man with no keyword, strength 7; skirmish: exert him to make a Hobbit stronger
					// Rider of Rohan: a [rohan] Man with no keyword, cost 2, nothing to play him; his skirmish ability needs a mount
					put("rider", "4_286");
					put("rider2", "4_286");
					put("guard", "5_83");        // Household Guard: a [rohan] Man, valiant and nothing else, vitality 3
					put("eowyn", "5_122");       // Eowyn, Daughter of Eomund: valiant; a maneuver ability never used here
					put("merry", "103_85");      // Merry, Master Holbytla: a Hobbit, valiant and enduring, who is also a [rohan] Man beside Eowyn
					put("ranger", "4_122");      // Gondorian Ranger: a Ring-bound Man (and a ranger); his text is about the site number of minions
					put("haldir", "1_48");       // Haldir, Elf of the Golden Wood: an unbound companion who is no Man
					put("barliman", "1_70");     // Barliman Butterbur: a Man who is an ally, no keyword; a fellowship ability never used here
					// The Red Arrow: Assignment: exert a [gondor] Man to play a [rohan] Man (and, if wanted, a possession on him); then it is discarded
					put("redarrow", "9_46");

					put("explorer", "4_250");    // Southron Explorer: a Man who is a minion, strength 5, vitality 3; its regroup ability costs (5)
					put("troop", "1_177");       // Goblin Patrol Troop: strength 13, no text
					// Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Calenhad
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		 * 	Maneuver: Hinder this beacon and exert an unbound Man.  Until the regroup phase, each of your other Men gain each of the unloaded keywords (except race) on that Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("calenhad1");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Calenhad", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoBeaconsCanBeHinderedAndThenCostsTwoAndHindersBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad1");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var halifirien = scn.GetFreepsCard("halifirien");
		var catapult = scn.GetFreepsCard("catapult");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(war1, halifirien, catapult);
		// A hindered beacon is no beacon while it is hindered, and the Catapult never was one.
		scn.HinderCard(halifirien);
		scn.MoveCardsToHand(calenhad, war2);

		scn.StartGame();

		// One beacon that can be hindered: one short.
		assertFalse(scn.FreepsPlayAvailable(calenhad));

		scn.FreepsPlayCard(war2);

		// 1 for the War-beacon
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(calenhad));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(war2));

		scn.FreepsPlayCard(calenhad);

		// 1 + 2 for the Signal-fire. With exactly two beacons to hinder nothing is asked.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, calenhad);
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(halifirien));
		assertFalse(scn.IsHindered(catapult));
		assertFalse(scn.IsHindered(calenhad));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheTwoBeaconsHinderedToPlayAreChosenFromUnhinderedBeaconsOfAnyCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad1");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var halifirien = scn.GetFreepsCard("halifirien");
		var catapult = scn.GetFreepsCard("catapult");
		scn.MoveCardsToSupportArea(war1, war2, war3, halifirien, catapult);
		scn.HinderCard(war3);
		scn.MoveCardsToHand(calenhad);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(calenhad);

		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// Two [gondor] beacons and a [rohan] one
		assertTrue(scn.FreepsHasCardChoiceAvailable(war1, war2, halifirien));
		// A beacon already hindered, a [gondor] support possession that is no beacon, and the card being played
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(war3, catapult, calenhad));

		scn.FreepsChooseCards(war2, halifirien);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, calenhad);
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(halifirien));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(catapult));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityHindersItsOwnCopyAndExertsAnUnboundManWhoseKeywordYourOtherMenGain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad1 = scn.GetFreepsCard("calenhad1");
		var calenhad2 = scn.GetFreepsCard("calenhad2");
		var war1 = scn.GetFreepsCard("war1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var ranger = scn.GetFreepsCard("ranger");
		var haldir = scn.GetFreepsCard("haldir");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCompanionsToTable(aragorn, boromir, rider, guard, ranger, haldir);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(calenhad1, calenhad2, war1);
		// Vitality 3 with 2 wounds: an unbound Man who cannot exert.
		scn.AddWoundsToChar(guard, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);

		assertFalse(scn.FreepsActionAvailable(calenhad1));

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(calenhad1));
		assertTrue(scn.FreepsActionAvailable(calenhad2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.RANGER));
		assertFalse(scn.HasKeyword(boromir, Keyword.RANGER));
		assertFalse(scn.HasKeyword(rider, Keyword.RANGER));
		assertFalse(scn.HasKeyword(guard, Keyword.RANGER));
		assertFalse(scn.HasKeyword(barliman, Keyword.RANGER));
		assertFalse(scn.HasKeyword(haldir, Keyword.RANGER));
		assertFalse(scn.HasKeyword(explorer, Keyword.RANGER));

		scn.FreepsUseCardAction(calenhad1);

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		// Unbound Men of two cultures
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, rider));
		// A Ring-bound Man, an unbound Elf, a Man who is an ally, a Man who is a minion, the Ring-bearer, and an
		// unbound Man who is exhausted
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger, haldir, barliman, explorer, frodo, guard));

		scn.FreepsChooseCard(aragorn);

		assertTrue(scn.IsHindered(calenhad1));
		assertFalse(scn.IsHindered(calenhad2));
		assertFalse(scn.IsHindered(war1));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(rider));
		// My other Men: of [gondor] and of [rohan], an exhausted one, and an ally
		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));
		assertTrue(scn.HasKeyword(rider, Keyword.RANGER));
		assertTrue(scn.HasKeyword(guard, Keyword.RANGER));
		assertTrue(scn.HasKeyword(barliman, Keyword.RANGER));
		// Not an Elf of mine, and not the other player's Man
		assertFalse(scn.HasKeyword(haldir, Keyword.RANGER));
		assertFalse(scn.HasKeyword(explorer, Keyword.RANGER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();

		// The copy that was hindered has no text; the other is still there to use.
		assertFalse(scn.FreepsActionAvailable(calenhad1));
		assertTrue(scn.FreepsActionAvailable(calenhad2));

		scn.FreepsPass();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(calenhad2));
	}

	@Test
	public void WithItsOnlyUnboundManExhaustedTheOtherCopyIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad1 = scn.GetFreepsCard("calenhad1");
		var calenhad2 = scn.GetFreepsCard("calenhad2");
		var guard = scn.GetFreepsCard("guard");
		var ranger = scn.GetFreepsCard("ranger");
		var haldir = scn.GetFreepsCard("haldir");
		var barliman = scn.GetFreepsCard("barliman");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCompanionsToTable(guard, ranger, haldir);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(calenhad1, calenhad2);
		// Vitality 3 with 1 wound: he can exert once more.
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(calenhad1));
		assertTrue(scn.FreepsActionAvailable(calenhad2));
		assertFalse(scn.HasKeyword(ranger, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(barliman, Keyword.VALIANT));

		// The Household Guard is the only unbound Man, so nothing is asked.
		scn.FreepsUseCardAction(calenhad1);

		assertTrue(scn.IsHindered(calenhad1));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(barliman));
		// A Ring-bound Man and an ally are Men of mine all the same.
		assertTrue(scn.HasKeyword(ranger, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(barliman, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(haldir, Keyword.VALIANT));

		scn.ShadowPass();

		// The Ring-bound Man, the Elf and the ally could all exert; the one unbound Man cannot.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.IsHindered(calenhad2));
		assertFalse(scn.FreepsActionAvailable(calenhad2));
	}

	@Test
	public void EveryUnloadedKeywordOnTheManIsGainedAndNoLoadedOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad1 = scn.GetFreepsCard("calenhad1");
		var calenhad2 = scn.GetFreepsCard("calenhad2");
		var prowler = scn.GetFreepsCard("prowler");
		var armor = scn.GetFreepsCard("armor");
		var heir = scn.GetFreepsCard("heir");
		var boromir = scn.GetFreepsCard("boromir");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCompanionsToTable(prowler, heir, boromir);
		// The Prowler is a ranger and hunter 1 in print; the armor he bears makes him a knight as well.
		scn.AttachCardsTo(prowler, armor);
		scn.MoveCardsToSupportArea(calenhad1, calenhad2);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(prowler, Keyword.RANGER));
		assertTrue(scn.HasKeyword(prowler, Keyword.KNIGHT));
		assertTrue(scn.HasKeyword(prowler, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(boromir, Keyword.RANGER));
		assertFalse(scn.HasKeyword(boromir, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(boromir, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(heir, Keyword.RANGER));
		assertFalse(scn.HasKeyword(heir, Keyword.KNIGHT));

		scn.FreepsUseCardAction(calenhad1);
		scn.FreepsChooseCard(prowler);

		assertEquals(1, scn.GetWoundsOn(prowler));
		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));
		assertTrue(scn.HasKeyword(boromir, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(boromir, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(heir, Keyword.RANGER));
		assertTrue(scn.HasKeyword(heir, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(heir, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(explorer, Keyword.RANGER));
		assertFalse(scn.HasKeyword(explorer, Keyword.KNIGHT));

		scn.ShadowPass();

		assertTrue(scn.HasKeyword(heir, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(prowler, Keyword.DEFENDER));

		// Defender +1 is all this Aragorn has: he may be exerted all the same, and nobody gains anything.
		scn.FreepsUseCardAction(calenhad2);
		scn.FreepsChooseCard(heir);

		assertTrue(scn.IsHindered(calenhad2));
		assertEquals(1, scn.GetWoundsOn(heir));
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(prowler, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(explorer, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheKeywordsLastThroughTheSkirmishesAndAreGoneWhenTheRegroupPhaseBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var rider = scn.GetFreepsCard("rider");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn, boromir, rider);
		scn.MoveCardsToSupportArea(calenhad);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(calenhad);
		scn.FreepsChooseCard(aragorn);

		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));
		assertTrue(scn.HasKeyword(rider, Keyword.RANGER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{boromir, runner2});
		// Aragorn's 8 beats a Runner's 5 and that skirmish is over.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(boromir);

		// The last skirmish of the turn, after one has been resolved
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));
		assertTrue(scn.HasKeyword(rider, Keyword.RANGER));

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(boromir, Keyword.RANGER));
		assertFalse(scn.HasKeyword(rider, Keyword.RANGER));
		assertTrue(scn.HasKeyword(aragorn, Keyword.RANGER));
	}

	@Test
	public void AManWhoComesIntoPlayAfterTheAbilityWasUsedGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad1");
		var redarrow = scn.GetFreepsCard("redarrow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCompanionsToTable(aragorn, boromir, rider);
		scn.MoveCardsToSupportArea(calenhad, redarrow);
		scn.MoveCardsToHand(rider2);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(calenhad);
		scn.FreepsChooseCard(aragorn);

		assertTrue(scn.HasKeyword(rider, Keyword.RANGER));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The Red Arrow: exert a [gondor] Man to play a [rohan] Man, here the second Rider of Rohan from hand.
		scn.FreepsUseCardAction(redarrow);
		scn.FreepsChooseCard(boromir);
		// No possession is in hand, so "You may exert that [rohan] Man to play a possession" is not asked (F-0472).

		assertInZone(Zone.FREE_CHARACTERS, rider2);
		assertTrue(scn.IsRace(rider2, Race.MAN));
		// The Rider who was there when the ability was used is a ranger; the one played since is not.
		assertTrue(scn.HasKeyword(rider, Keyword.RANGER));
		assertFalse(scn.HasKeyword(rider2, Keyword.RANGER));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void TheKeywordsStayWhenTheManExertedIsKilledBeforeTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var rider = scn.GetFreepsCard("rider");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(aragorn, boromir, rider);
		scn.MoveCardsToSupportArea(calenhad);
		// Vitality 4: the exertion will be his third wound, and the next wound kills him.
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(calenhad);
		scn.FreepsChooseCard(aragorn);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));
		assertTrue(scn.HasKeyword(rider, Keyword.RANGER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{boromir, runner});
		// Aragorn's 8 loses to the Troop's 13.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();

		assertInZone(Zone.DEAD, aragorn);

		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));
		assertTrue(scn.HasKeyword(rider, Keyword.RANGER));
	}

	@Test
	public void TheKeywordsStayWhenTheManExertedIsHinderedBeforeTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad1");
		var merry = scn.GetFreepsCard("merry");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry, eowyn, rider, boromir);
		scn.MoveCardsToSupportArea(calenhad);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(boromir, Keyword.VALIANT));

		// Beside Eowyn, Merry is an unbound Man, and valiant.
		scn.FreepsUseCardAction(calenhad);
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.HasKeyword(rider, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(boromir, Keyword.VALIANT));

		// Boromir, a [gondor] Man, fights the Runner, and Merry hinders himself by his own skirmish ability:
		// no [rohan] Man is in the skirmish, so nothing is resolved and the skirmish goes on.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(boromir, runner);
		assertFalse(scn.IsHindered(merry));

		scn.FreepsUseCardAction(merry);

		assertTrue(scn.IsHindered(merry));
		assertTrue(scn.IsCharSkirmishing(boromir));
		assertTrue(scn.HasKeyword(rider, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(boromir, Keyword.VALIANT));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ExertingAHobbitWhoIsAlsoAManGivesHisUnloadedKeywordAndNotHisRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad1");
		var merry = scn.GetFreepsCard("merry");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var boromir = scn.GetFreepsCard("boromir");
		var haldir = scn.GetFreepsCard("haldir");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCompanionsToTable(merry, eowyn, rider, boromir, haldir);
		scn.MoveCardsToSupportArea(calenhad);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.MANEUVER);

		// Beside Eowyn, Merry is a Man as well as a Hobbit.
		assertTrue(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertTrue(scn.HasKeyword(merry, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(merry, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(boromir, Keyword.VALIANT));

		scn.FreepsUseCardAction(calenhad);

		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, eowyn, rider, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(haldir));

		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.HasKeyword(rider, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(boromir, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(haldir, Keyword.VALIANT));
		// Enduring is a loaded keyword.
		assertFalse(scn.HasKeyword(rider, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(boromir, Keyword.ENDURING));
		// The other Men are no Hobbits for it.
		assertFalse(scn.IsRace(rider, Race.HOBBIT));
		assertFalse(scn.IsRace(boromir, Race.HOBBIT));
		assertFalse(scn.IsRace(eowyn, Race.HOBBIT));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
