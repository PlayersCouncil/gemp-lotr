package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "8_92");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, cost 2; Valiant is its whole text
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, cost 2; a skirmish ability only when mounted
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] companion who is a Hobbit, cost 2
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man who is an ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man and a companion, not [rohan]
					put("spear", "4_288");      // Rider's Spear: a [rohan] card that is no companion

					put("marksman", "1_176");   // Goblin Marksman: Archer is its whole text
					put("despair", "7_148");    // Fierce in Despair: "Spot 2 [raider] Men to discard a companion (except the Ring-bearer)"
					put("axeman1", "4_224");    // Easterling Axeman: [raider] Man; his text needs 2 burdens
					put("axeman2", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOtherTheodenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "8_92");
					put("sonofthengel", "4_292"); // Théoden, Son of Thengel: another Théoden, cost 2, held in hand
					put("guard", "5_83");         // Household Guard: [rohan] Man companion, cost 2
					put("merry", "103_85");       // Merry, Master Holbytla: [rohan], and a Hobbit
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, not [rohan]
					put("marksman", "1_176");     // Goblin Marksman: Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Théoden, Tall and Proud
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Valiant</b>.
		 * While you can spot a [rohan] Man, Théoden's twilight cost is -1.
		 * When Théoden is killed, you may play a [rohan] companion from your discard pile or draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("Tall and Proud", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForThreeBesideAManOfAnotherCultureAndARohanHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherTheodenScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(theoden);
		// Merry is a Man only beside three [rohan] companions or Éowyn: here he is a [rohan] Hobbit.
		scn.MoveCompanionsToTable(aragorn, merry);

		scn.StartGame();

		assertEquals(3, scn.GetTwilightCost(theoden));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(theoden));
		scn.FreepsPlayCard(theoden);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CostsOneLessOnceARohanManIsPlayedAndAnotherTheodenInHandDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherTheodenScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var sonOfThengel = scn.GetFreepsCard("sonofthengel");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(theoden, sonOfThengel, guard);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertEquals(3, scn.GetTwilightCost(theoden));
		assertEquals(2, scn.GetTwilightCost(sonOfThengel));

		scn.FreepsPlayCard(guard);
		assertEquals(2, scn.GetTwilight());

		// The text names this card: the other Théoden keeps his printed 2.
		assertEquals(2, scn.GetTwilightCost(theoden));
		assertEquals(2, scn.GetTwilightCost(sonOfThengel));

		scn.FreepsPlayCard(theoden);

		// 2 for the Guard + 3 - 1 for Théoden
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		// In play his cost is the number printed on him, though he is a [rohan] Man himself beside another.
		assertEquals(3, scn.GetTwilightCost(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManWhoIsAnAllyIsSpottedForTheLowerCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCardsToHand(theoden);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(theoden));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(theoden);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void KilledWithRohanCompanionsInTheDiscardPileOnlyOneOfThemMayBePlayedFromThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var rider = scn.GetFreepsCard("rider");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveCardsToDiscard(guard, merry, guma, aragorn, spear);
		// The third [rohan] companion is in hand: the draw deck holds none.
		scn.MoveCardsToHand(rider);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		// The Marksman's arrow is his third wound.
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		scn.FreepsChooseCard(theoden);
		assertInZone(Zone.DEAD, theoden);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		int pool = scn.GetTwilight();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsAcceptOptionalTrigger();

		// Only the discard pile has a [rohan] companion, so there is no question of piles.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, aragorn, spear, rider));
		scn.FreepsChooseCardBPFromSelection(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertInZone(Zone.DISCARD, merry, guma, aragorn, spear);
		assertInZone(Zone.HAND, rider);
		// The Guard's own cost
		assertEquals(pool + 2, scn.GetTwilight());
		// The draw deck was not looked through.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void KilledWithRohanCompanionsInTheDrawDeckOnlyOneOfThemMayBePlayedFromThereAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var rider = scn.GetFreepsCard("rider");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(theoden);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		scn.FreepsChooseCard(theoden);
		assertInZone(Zone.DEAD, theoden);

		// Every card of the scenario is still in the draw deck, and the discard pile is empty.
		assertInZone(Zone.DECK, guard, merry, rider, guma, aragorn, spear);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		int pool = scn.GetTwilight();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsAcceptOptionalTrigger();

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, aragorn, spear));
		scn.FreepsChooseCardBPFromSelection(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInZone(Zone.DECK, guard, rider, guma, aragorn, spear);
		// Merry's own cost
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithARohanCompanionInEachPileThePlayerChoosesThePile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(theoden);
		// The Guard and the Rider in the discard pile; Merry stays in the draw deck.
		scn.MoveCardsToDiscard(guard, rider);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		scn.FreepsChooseCard(theoden);
		assertInZone(Zone.DEAD, theoden);

		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("discard pile"));
		assertTrue(scn.FreepsChoiceAvailable("draw deck"));
		scn.FreepsChooseOption("discard pile");

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		scn.FreepsChooseCardBPFromSelection(rider);

		assertInZone(Zone.FREE_CHARACTERS, rider);
		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.DECK, merry);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningPlaysNothingAndLeavesTheDeckUnshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveCardsToDiscard(guard);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		scn.FreepsChooseCard(theoden);
		assertInZone(Zone.DEAD, theoden);

		int pool = scn.GetTwilight();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.DECK, merry);
		assertEquals(pool, scn.GetTwilight());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ARohanCompanionFoundInTheDrawDeckNeedNotBePlayedAndTheDeckIsStillShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(theoden);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		scn.FreepsChooseCard(theoden);
		assertInZone(Zone.DEAD, theoden);

		int pool = scn.GetTwilight();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry, rider));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, guard, merry, rider);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionInEitherPileNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var rider = scn.GetFreepsCard("rider");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		// A [rohan] companion in play and two in hand; a [rohan] ally and a companion of another
		// culture in the discard pile; a [rohan] possession in the draw deck.
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveCardsToHand(merry, rider);
		scn.MoveCardsToDiscard(guma, aragorn);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DECK, spear);
		scn.FreepsChooseCard(theoden);

		assertInZone(Zone.DEAD, theoden);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.HAND, merry, rider);
		assertInZone(Zone.DISCARD, guma, aragorn);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnotherTheodenInTheDiscardPileCannotBePlayedSoNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherTheodenScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var sonOfThengel = scn.GetFreepsCard("sonofthengel");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(theoden);
		// The one [rohan] companion in either pile is a Théoden, and no Théoden can be played once
		// this one lies in the dead pile.
		scn.MoveCardsToDiscard(sonOfThengel);
		scn.MoveCardsToHand(guard, merry);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		scn.FreepsChooseCard(theoden);

		assertInZone(Zone.DEAD, theoden);
		assertInZone(Zone.DISCARD, sonOfThengel);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void KilledByAThreatWoundHeMakesTheOfferToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(theoden, rider);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddThreats(1);
		scn.AddWoundsToChar(theoden, 2);
		scn.AddWoundsToChar(rider, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		// The arrow kills the Rider, and the threat becomes a wound that the Free Peoples player places.
		scn.FreepsChooseCard(rider);
		assertInZone(Zone.DEAD, rider);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, theoden));
		scn.FreepsChooseCard(theoden);

		assertInZone(Zone.DEAD, theoden);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		scn.FreepsChooseCardBPFromSelection(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertInZone(Zone.DECK, merry);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnotherCompanionKilledBesideHimBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(theoden, guard);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(guard, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		scn.FreepsChooseCard(guard);

		// A [rohan] companion he could have played lies in the draw deck.
		assertInZone(Zone.DEAD, guard);
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertInZone(Zone.DECK, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DiscardedFromPlayAndNotKilledHeMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(theoden);

		var despair = scn.GetShadowCard("despair");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCardsToHand(despair);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Théoden is the only companion who is not the Ring-bearer, so he is not chosen by anyone.
		assertTrue(scn.ShadowPlayAvailable(despair));
		scn.ShadowPlayCard(despair);

		assertInZone(Zone.DISCARD, theoden);
		assertInZone(Zone.DECK, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
