package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_045_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope1", "102_45");
					put("hope2", "102_45");

					// Eomer, Third Marshal of Riddermark: a unique [rohan] Man, strength 7, vitality 3, not
					// valiant.  He is stronger for each wound on a minion he is skirmishing; no minion here
					// is ever wounded.
					put("eomer", "4_267");
					// Elite Rider: a [rohan] Man who is not unique and not valiant, strength 6.  He is
					// stronger while skirmishing a wounded minion, and never skirmishes here.
					put("rider", "7_225");
					// Household Guard: a [rohan] Man; "Valiant" is his whole text.
					put("guard", "5_83");
					// Aragorn, Ranger of the North: a Man and a companion, of the [gondor] culture.
					put("aragorn", "1_89");
					// Guma, Plains Farmer: a [rohan] Man who is an ally.
					put("guma", "4_277");
					// Merry, Master Holbytla: the one [rohan] companion who is not a Man.  A Hobbit, strength
					// 3, valiant and enduring; he counts as a Man while 3 [rohan] companions (or Eowyn) can be
					// spotted, so the test he is in has two.  His other text needs a [rohan] Man to exert, or
					// a skirmish.
					put("merry", "103_85");
					// Theoden, Son of Thengel: a [rohan] Man, vitality 2.  His regroup ability needs a [rohan]
					// ally to spot.
					put("theoden", "4_292");
					// King's Mail: a [rohan] armour.  "If bearer is Theoden, he may not take wounds except
					// during a skirmish involving him."
					put("mail", "4_284");

					// Goblin Runner: strength 5, vitality 1.
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DoNotTrusttoHopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Do Not Trust to Hope
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Strength: +2
		 * Game Text: Bearer must be a [rohan] companion.
		 * Bearer is <b>valiant</b>.
		 * At the start of the regroup phase, wound bearer or discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope1");

		assertEquals("Do Not Trust to Hope", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		// The card makes its bearer valiant; it is not valiant itself.
		assertFalse(scn.HasKeyword(card, Keyword.VALIANT));
	}

	@Test
	public void IsPlayedForOneOnARohanCompanionWhoBecomesValiantAndStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hope = scn.GetFreepsCard("hope1");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCardsToHand(hope);
		scn.MoveCompanionsToTable(eomer, rider, guard, aragorn);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(eomer));
		assertFalse(scn.HasKeyword(eomer, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertTrue(scn.FreepsPlayAvailable(hope));

		scn.FreepsPlayCard(hope);
		// A [rohan] companion, unique or not, valiant already or not.  Not a companion of another culture,
		// though a Man as they are; not a [rohan] Man who is an ally; not the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, rider, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guma, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(eomer);
		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(hope, eomer);
		assertTrue(scn.HasKeyword(eomer, Keyword.VALIANT));
		// 7 + 2 for the condition.
		assertEquals(9, scn.GetStrength(eomer));
		// Only its bearer.
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertEquals(6, scn.GetStrength(rider));
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanCompanionWhoIsNotAManMayBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hope = scn.GetFreepsCard("hope1");
		var eomer = scn.GetFreepsCard("eomer");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(hope);
		scn.MoveCompanionsToTable(eomer, merry);

		scn.StartGame();

		// Two [rohan] companions, one short of the three that would have Merry count as a Man.
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsPlayAvailable(hope));

		scn.FreepsPlayCard(hope);
		// The [rohan] Hobbit as well as the [rohan] Man, and not the [shire] Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));

		scn.FreepsChooseCard(merry);
		assertAttachedTo(hope, merry);
		// 3 + 2 for the condition.
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(7, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfEachRegroupPhaseAndNoSoonerBearerIsWoundedOrTheConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope1");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eomer, rider);
		scn.AttachCardsTo(eomer, hope);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// Through the maneuver and archery phases and into the assignment phase, nothing is asked.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertAttachedTo(hope, eomer);
		scn.FreepsPass();
		scn.ShadowPass();

		// Nor at the start of the skirmish phase.
		scn.FreepsAssignAndResolve(eomer, runner);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertAttachedTo(hope, eomer);

		// Eomer at 9 beats the Runner at 5 and takes no wound.  The regroup phase begins.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInZone(Zone.DISCARD, runner);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Wound"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertAttachedTo(hope, eomer);

		scn.FreepsChooseOption("Wound");
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertAttachedTo(hope, eomer);
		assertTrue(scn.HasKeyword(eomer, Keyword.VALIANT));
		assertEquals(9, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The fellowship moves again.  With no minion there is no fighting, and the second regroup phase of
		// the turn asks again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertAttachedTo(hope, eomer);

		scn.FreepsChooseOption("Discard");
		assertInZone(Zone.DISCARD, hope);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertFalse(scn.HasKeyword(eomer, Keyword.VALIANT));
		assertEquals(7, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachCopyAsksForItsOwnBearerAndDiscardsOnlyItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetFreepsCard("hope1");
		var hope2 = scn.GetFreepsCard("hope2");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, hope1);
		scn.AttachCardsTo(guard, hope2);

		scn.StartGame();
		// 6 + 2 for the condition.  The Guard is valiant with or without it.
		assertEquals(8, scn.GetStrength(guard));

		// No minion is in play, so the regroup phase follows the Shadow phase.
		scn.SkipToPhase(Phase.REGROUP);

		// The copies ask one after the other, each naming its own bearer.  Eomer's is first.
		assertTrue(scn.FreepsChoiceAvailable("Third Marshal"));
		assertFalse(scn.FreepsChoiceAvailable("Household Guard"));
		scn.FreepsChooseOption("Wound");
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertAttachedTo(hope1, eomer);
		assertAttachedTo(hope2, guard);

		assertTrue(scn.FreepsChoiceAvailable("Household Guard"));
		assertFalse(scn.FreepsChoiceAvailable("Third Marshal"));
		scn.FreepsChooseOption("Discard");
		assertInZone(Zone.DISCARD, hope2);
		assertAttachedTo(hope1, eomer);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(guard));
		// The Guard is back to his own strength and is valiant still, by his own text; Eomer is as he was.
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(eomer, Keyword.VALIANT));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABearerWhoCannotTakeWoundsIsNotAskedAndTheConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope1 = scn.GetFreepsCard("hope1");
		var hope2 = scn.GetFreepsCard("hope2");
		var theoden = scn.GetFreepsCard("theoden");
		var mail = scn.GetFreepsCard("mail");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(theoden, eomer);
		// In King's Mail Theoden may not take wounds outside a skirmish of his own.  Eomer, who may, bears
		// the other copy.
		scn.AttachCardsTo(theoden, mail, hope1);
		scn.AttachCardsTo(eomer, hope2);

		scn.StartGame();
		assertAttachedTo(hope1, theoden);
		assertTrue(scn.HasKeyword(theoden, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(theoden));

		// No minion is in play, so the regroup phase follows the Shadow phase.
		scn.SkipToPhase(Phase.REGROUP);

		// One question is put, and it is Eomer's.  A player must choose what he can fully do: Theoden cannot
		// be wounded, so his copy has nothing to ask.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Third Marshal"));
		assertFalse(scn.FreepsChoiceAvailable("Son of Thengel"));
		scn.FreepsChooseOption("Wound");

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertAttachedTo(hope2, eomer);
		// Theoden's copy is discarded, he has no wound, and the Mail is still on him.
		assertInZone(Zone.DISCARD, hope1);
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertFalse(scn.HasKeyword(theoden, Keyword.VALIANT));
		assertAttachedTo(mail, theoden);
	}

	@Test
	public void WoundingAnExhaustedBearerKillsHimAndTheConditionGoesWithHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope1");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, hope);

		scn.StartGame();
		// Two wounds of his three vitality: he could not exert, and can still be wounded.
		scn.AddWoundsToChar(eomer, 2);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Wound"));
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertAttachedTo(hope, eomer);

		scn.FreepsChooseOption("Wound");
		assertInZone(Zone.DEAD, eomer);
		assertInZone(Zone.DISCARD, hope);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
