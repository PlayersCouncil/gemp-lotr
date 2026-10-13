package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("takeitdown", "103_89");
					put("theoden", "7_255");       // Théoden, Rekindled King: vitality 3; a fellowship ability never used here
					put("king", "102_55");         // Theoden, Last of His House: his title is written without the accent; vitality 3
					put("eomer", "7_227");         // Éomer, Skilled Tactician: vitality 3; a fellowship ability never used here
					put("guard", "5_83");          // Household Guard: [rohan] Man companion, vitality 3, valiant and nothing else
					put("guard2", "5_83");
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: a [gondor] Man
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, vitality 4; he is a Man too with 3 [rohan] companions
					// or Eowyn to spot, and each time another [rohan] Man exerts he may exert to heal that Man
					put("merry", "103_85");
					put("guma", "4_277");          // Guma, Plains Farmer: [rohan] Man ally, vitality 2; a fellowship ability
					put("mount", "4_287");         // Rider's Mount: makes a companion mounted; its text needs its bearer in a skirmish
					put("delvings", "101_2");      // Deepest Delvings: a Free Peoples condition, here only to have cards stacked on it

					put("rider", "5_53");          // Isengard Rider: [isengard] warg-rider, strength 8, vitality 3, fierce while not exhausted
					put("rider2", "5_53");
					put("hound", "102_24");        // Hound of Isengard: mount for a warg-rider, strength +1, vitality +1
					put("scimitar", "5_54");       // Isengard Scimitar: a hand weapon for an [isengard] Orc
					put("witchking", "1_237");     // The Witch-king, Lord of Angmar: [wraith], vitality 4, fierce
					put("fellbeast", "6_83");      // Fell Beast: mount for a Nazgul, strength +2, bearer is fierce
					put("lemenya", "1_232");       // Ulaire Lemenya: Nazgul of vitality 2; his text needs a [wraith] condition
					put("rearguard", "103_43");    // Bladetusk Rearguard: an artifact that makes itself a mounted Southron minion, 5 and 5
					put("southron1", "4_222");     // Desert Warrior: Southron Man; may remove (3) to wound at the start of his own skirmish
					put("southron2", "4_222");
					put("southron3", "4_222");
					put("swarms", "1_183");        // Goblin Swarms: a Shadow condition, here only to have cards stacked on it
					put("runner", "1_178");        // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TakeitDownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Take it Down!
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert Theoden or Eomer to wound a mounted minion twice.  You may then exert X [rohan] companions to discard X stacked Shadow cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("takeitdown");

		assertEquals("Take it Down!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheodenToWoundTwiceAMountedMinionWhoNeedNotBeInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(theoden, eomer, guard, aragorn);
		// Éomer is mounted, and no minion.
		scn.AttachCardsTo(eomer, mount);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var rider2 = scn.GetShadowCard("rider2");
		var hound = scn.GetShadowCard("hound");
		var scimitar = scn.GetShadowCard("scimitar");
		var witchking = scn.GetShadowCard("witchking");
		var fellbeast = scn.GetShadowCard("fellbeast");

		scn.StartGame();
		scn.MoveMinionsToTable(rider, rider2, witchking);
		// Two mounted minions of two cultures, and a warg-rider who bears a weapon and no mount.
		scn.AttachCardsTo(rider, hound);
		scn.AttachCardsTo(witchking, fellbeast);
		scn.AttachCardsTo(rider2, scimitar);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertFalse(scn.FreepsPlayAvailable(takeitdown));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(theoden, rider);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(theoden);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.FreepsPlayAvailable(takeitdown));

		scn.FreepsPlayCard(takeitdown);

		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, eomer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(theoden);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eomer));
		// The Rider fighting Théoden and the Witch-king, whom nobody is fighting.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, witchking));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rider2, eomer, theoden));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(witchking);

		assertEquals(2, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(rider2));

		// No Shadow card is stacked anywhere; the exertion is offered all the same, and may be made for nothing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, eomer, guard));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertInZone(Zone.DISCARD, takeitdown);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void EomerMayExertInsteadAndTheSecondExertionMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var king = scn.GetFreepsCard("king");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(king, eomer, guard);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, hound);
		scn.SkipToAssignments();
		// Neither Theoden nor Éomer is in the skirmish.
		scn.FreepsAssignToMinions(guard, rider);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(0, scn.GetWoundsOn(king));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(rider));

		scn.FreepsPlayCard(takeitdown);

		assertTrue(scn.FreepsHasCardChoiceAvailable(king, eomer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard));
		scn.FreepsChooseCard(eomer);

		// The Rider is the only mounted minion.
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(king));
		assertEquals(2, scn.GetWoundsOn(rider));

		assertTrue(scn.FreepsHasCardChoiceAvailable(king, eomer, guard));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(king));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInZone(Zone.DISCARD, takeitdown);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheodenIsExhaustedAndEomerIsNotInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(theoden, guard, aragorn);
		// Vitality 3
		scn.AddWoundsToChar(theoden, 2);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, rider);
		scn.FreepsResolveSkirmish(guard);

		// A [rohan] Man and a [gondor] Man could each exert, and a mounted minion is in the skirmish.
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsPlayAvailable(takeitdown));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMountedMinionNobodyIsWoundedAndTheSecondExertionIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveCardsToHand(takeitdown);

		var rider2 = scn.GetShadowCard("rider2");
		var scimitar = scn.GetShadowCard("scimitar");
		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner);

		scn.StartGame();
		// A companion who is mounted, and a warg-rider who bears a weapon and no mount.
		scn.AttachCardsTo(guard, mount);
		scn.MoveMinionsToTable(rider2);
		scn.AttachCardsTo(rider2, scimitar);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, rider2);
		scn.FreepsResolveSkirmish(theoden);

		assertTrue(scn.FreepsPlayAvailable(takeitdown));

		scn.FreepsPlayCard(takeitdown);

		// Théoden is the only one who can pay, and there is nobody to wound.
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(rider2));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, guard));
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetStackedCards(swarms).size());
		assertEquals(0, scn.GetWoundsOn(rider2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThenExertsTwoRohanCompanionsToDiscardTwoStackedShadowCardsOfThePlayersChoosing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		var delvings = scn.GetFreepsCard("delvings");
		var myRunner = scn.GetFreepsCard("runner");
		var myKing = scn.GetFreepsCard("king");
		scn.MoveCompanionsToTable(theoden, eomer, guard, guard2, aragorn);
		scn.MoveAlliesToTable(guma);
		// Vitality 3: exhausted.
		scn.AddWoundsToChar(guard2, 2);
		// A Shadow card and a Free Peoples card of my own, stacked on a Free Peoples card of mine.
		scn.MoveCardsToSupportArea(delvings);
		scn.StackCardsOn(delvings, myRunner, myKing);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");
		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var hisAragorn = scn.GetShadowCard("aragorn");
		// A minion, a possession and a Free Peoples card, stacked on a Shadow condition.
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner, scimitar, hisAragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, rider);
		scn.FreepsResolveSkirmish(theoden);

		scn.FreepsPlayCard(takeitdown);
		scn.FreepsChooseCard(theoden);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(guard));
		// Théoden has exerted once and can again.  Not the exhausted Guard, the [gondor] Man, the [rohan] ally
		// or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, eomer, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard2, aragorn, guma, frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(eomer, guard);

		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(guma));
		// Two of the three stacked Shadow cards; not a stacked Free Peoples card, nor a Shadow card in play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, scimitar, myRunner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hisAragorn, myKing, swarms, delvings, rider, hound));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(scimitar, myRunner);

		// Each goes to its owner's discard pile.
		assertInZone(Zone.DISCARD, scimitar, myRunner);
		assertEquals(2, scn.GetStackedCards(swarms).size());
		assertTrue(scn.GetStackedCards(swarms).contains(runner));
		assertTrue(scn.GetStackedCards(swarms).contains(hisAragorn));
		assertEquals(1, scn.GetStackedCards(delvings).size());
		assertTrue(scn.GetStackedCards(delvings).contains(myKing));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARohanCompanionWhoIsNoManMayExertForItAndARohanManWhoIsAnAllyMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		// Two [rohan] companions and no Eowyn: Merry, Master Holbytla is a Hobbit and no Man. Guma is a [rohan]
		// Man who is an ally.
		scn.MoveCompanionsToTable(theoden, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");
		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner, scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, rider);
		scn.FreepsResolveSkirmish(theoden);

		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));

		// Théoden is the only one who can pay for the event, and the Rider the only mounted minion.
		scn.FreepsPlayCard(takeitdown);

		// Merry's own text answers the exertion of another [rohan] Man, and is not taken up.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(merry));
		// Théoden, who can exert again, and the [rohan] Hobbit.  Not the [gondor] Man, the [rohan] Man who is an
		// ally, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guma, frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// One companion exerted: one of the two stacked Shadow cards.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, scimitar));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(scimitar);

		assertInZone(Zone.DISCARD, scimitar);
		assertEquals(1, scn.GetStackedCards(swarms).size());
		assertTrue(scn.GetStackedCards(swarms).contains(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OneCompanionExertedDiscardsOneStackedShadowCardAndLeavesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(theoden, eomer, guard);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");
		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner, scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, rider);
		scn.FreepsResolveSkirmish(theoden);

		scn.FreepsPlayCard(takeitdown);
		scn.FreepsChooseCard(theoden);

		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, scimitar));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(runner);

		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetStackedCards(swarms).size());
		assertTrue(scn.GetStackedCards(swarms).contains(scimitar));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MoreCompanionsMayExertThanThereAreStackedShadowCardsAndEomerPaysWhenTheodenCannot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var king = scn.GetFreepsCard("king");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(king, eomer, guard, guard2);
		// Vitality 3: Theoden cannot exert.
		scn.AddWoundsToChar(king, 2);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");
		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, rider);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.FreepsPlayAvailable(takeitdown));

		scn.FreepsPlayCard(takeitdown);

		// Nobody is asked who pays: only Éomer can.
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(2, scn.GetWoundsOn(king));
		assertEquals(2, scn.GetWoundsOn(rider));
		// One stacked Shadow card, and three who can exert: X is the player's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guard, guard2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(king));
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard, guard2);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetStackedCards(swarms).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionAbleToExertNothingMoreIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(theoden, guard, aragorn);
		scn.MoveAlliesToTable(guma);
		// Vitality 3 each: Théoden can exert once more, the Guard not at all.
		scn.AddWoundsToChar(theoden, 1);
		scn.AddWoundsToChar(guard, 2);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsPlayAvailable(takeitdown));

		scn.FreepsPlayCard(takeitdown);

		// Paying for the event has exhausted Théoden; a [gondor] Man and a [rohan] ally could still exert.
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertInZone(Zone.DISCARD, takeitdown);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ABladetuskMadeAMountedMinionIsWoundedTwiceAndSouthronsStackedOnItAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(theoden, guard, eomer);
		scn.MoveCardsToHand(takeitdown);

		var rearguard = scn.GetShadowCard("rearguard");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(rearguard);
		scn.StackCardsOn(rearguard, southron1, southron2, southron3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// The Rearguard removes (5) and is a fierce mounted Southron minion until the end of the turn.
		scn.ShadowUseCardAction(rearguard);

		assertInZone(Zone.SHADOW_CHARACTERS, rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.MOUNTED));
		assertEquals(0, scn.GetWoundsOn(rearguard));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, rearguard);
		// Its ambush
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(theoden);

		assertEquals(3, scn.GetStackedCards(rearguard).size());

		scn.FreepsPlayCard(takeitdown);
		scn.FreepsChooseCard(theoden);

		// The Rearguard is the only mounted minion: the Goblin Runner is not asked about.
		assertEquals(2, scn.GetWoundsOn(rearguard));
		assertEquals(0, scn.GetWoundsOn(runner));

		scn.FreepsChooseCards(guard, eomer);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertTrue(scn.FreepsHasCardChoiceAvailable(southron1, southron2, southron3));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(southron1, southron2);

		assertInZone(Zone.DISCARD, southron1, southron2);
		assertEquals(1, scn.GetStackedCards(rearguard).size());
		assertTrue(scn.GetStackedCards(rearguard).contains(southron3));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMountedMinionOfVitalityTwoIsKilledAndAnExhaustedOneMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveCardsToHand(takeitdown);

		var lemenya = scn.GetShadowCard("lemenya");
		var fellbeast = scn.GetShadowCard("fellbeast");
		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, rider);
		scn.AttachCardsTo(lemenya, fellbeast);
		scn.AttachCardsTo(rider, hound);
		// Vitality 3 + 1 for the Hound: exhausted, and still a minion that can be wounded.
		scn.AddWoundsToChar(rider, 3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(theoden);

		assertEquals(0, scn.GetWoundsOn(lemenya));

		scn.FreepsPlayCard(takeitdown);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, rider));
		scn.FreepsChooseCard(lemenya);

		// Vitality 2 and two wounds: he is killed, and his mount goes with him.
		assertInZone(Zone.DISCARD, lemenya, fellbeast);
		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertEquals(3, scn.GetWoundsOn(rider));

		scn.FreepsDeclineChoosing();

		// No minion is left in the skirmish, so it is over; the exhausted Rider is not fierce.
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheSecondExertionAsksNothingMoreAndLeavesTheStackedCardsWhereTheyAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");
		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner, scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, rider);
		scn.FreepsResolveSkirmish(theoden);

		scn.FreepsPlayCard(takeitdown);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(rider));
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, guard));
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetStackedCards(swarms).size());
		assertInZone(Zone.DISCARD, takeitdown);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionAbleToExertAStackedShadowCardStaysAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var takeitdown = scn.GetFreepsCard("takeitdown");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(theoden, guard, aragorn);
		// Vitality 3 each: Théoden can exert once more, the Guard not at all.
		scn.AddWoundsToChar(theoden, 1);
		scn.AddWoundsToChar(guard, 2);
		scn.MoveCardsToHand(takeitdown);

		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");
		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(takeitdown);

		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetStackedCards(swarms).size());
		assertInZone(Zone.DISCARD, takeitdown);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
