package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oath", "17_66");
					put("oath2", "17_66");

					// Man of Bree: a [men] Man, vitality 1, site 4 (roaming at site 2); strength 4 and +1 for each card in
					// the Free Peoples hand
					put("bree", "11_90");
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard), vitality 4, site 4; his
					// strength bonus wants a [men] minion bearing a possession, his Skirmish ability is not used
					put("saruman", "17_48");
					// Dunlending Brigand: a [dunland] Man with no text, site 3
					put("brigand", "4_10");
					// Henchman's Dagger: a [men] possession, no minion; no text in play here
					put("dagger", "18_69");
					// Squad of Haradrim: a [men] Man, strength 9, vitality 2; its Regroup ability is not reached
					put("squad", "11_99");
					// Column of Easterlings: a [men] Man, lurker.  "Skirmish: Discard this minion to make a [men]
					// minion strength +4."
					put("column", "11_72");

					// Aragorn, Ranger of the North: a ranger and a [gondor] Man, strength 8, vitality 4
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: strength 6, vitality 3; his Skirmish ability is not used
					put("gimli", "1_13");
					// What Are They?: [gondor] Maneuver event.  "Spot a ranger to discard a roaming minion."
					put("what1", "1_119");
					put("what2", "1_119");
					put("what3", "1_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Lone tests of F-0444, on a board of their own: a [men] minion to kill and to discard in the maneuver phase.
	protected VirtualTableScenario GetKilledOrDiscardedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oath", "17_66");
					// Man of Bree: [men] Man, cost 2, vitality 1, site 4; strength 4 and +1 for each card in the
					// Free Peoples player's hand
					put("bree", "11_90");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man and a ranger, vitality 4
					// Defend It and Hope: [gondor] Maneuver event, cost 0.  "Exert a [gondor] Man to wound a
					// minion once (or twice if that minion is an Uruk-hai)."
					put("defend", "4_115");
					// What Are They?: [gondor] Maneuver event, cost 1.  "Spot a ranger to discard a roaming minion."
					put("whatAreThey", "1_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WildmansOathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Wildman's Oath
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Each time a [men] minion is discarded during the maneuver phase, you may stack it here.<br><b>Skirmish:</b> Discard this possession to make a [men] Man strength +2.
		*/

		var scn = GetScenario();

		var oath = scn.GetFreepsCard("oath");

		assertEquals("Wildman's Oath", oath.getBlueprint().getTitle());
		assertNull(oath.getBlueprint().getSubtitle());
		assertFalse(oath.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, oath.getBlueprint().getSide());
		assertEquals(Culture.MEN, oath.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, oath.getBlueprint().getCardType());
		assertNull(oath.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(oath, Keyword.SUPPORT_AREA));
		assertEquals(2, oath.getBlueprint().getTwilightCost());
	}

	// "Each time a [men] minion is discarded during the maneuver phase": a minion that is killed was not
	// discarded, though it is placed in the discard pile (CR 4.2 "killed": "Cards that trigger when a character is
	// killed will not trigger when a character is discarded, and vice-versa"; F-0444).
	@Test
	public void AMenMinionKilledInTheManeuverPhaseWasNotDiscardedAndIsNotOfferedToStack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledOrDiscardedScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var oath = scn.GetShadowCard("oath");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SkipToPhase(Phase.MANEUVER);

		// The wound takes the last of Man of Bree's vitality: he is killed.
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, bree);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMenMinionDiscardedInTheManeuverPhaseMayBeStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledOrDiscardedScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatAreThey = scn.GetFreepsCard("whatAreThey");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatAreThey);

		var oath = scn.GetShadowCard("oath");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SkipToPhase(Phase.MANEUVER);

		// Man of Bree, site number 4, is roaming at site 2: the one minion to discard.
		scn.FreepsPlayCard(whatAreThey);
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, bree);
		assertEquals(1, scn.GetStackedCards(oath).size());
		assertSame(bree, scn.GetStackedCards(oath).get(0));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachMenMinionDiscardedInTheManeuverPhaseMayBeStackedHereAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var what1 = scn.GetFreepsCard("what1");
		var what2 = scn.GetFreepsCard("what2");
		var what3 = scn.GetFreepsCard("what3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(what1, what2, what3);

		var oath = scn.GetShadowCard("oath");
		var bree = scn.GetShadowCard("bree");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(bree, saruman, brigand);
		scn.AttachCardsTo(bree, dagger);
		assertInZone(Zone.SUPPORT, oath);
		assertAttachedTo(dagger, bree);
		assertEquals(0, scn.GetStackedCards(oath).size());
		scn.SkipToPhase(Phase.MANEUVER);

		// All three are roaming at site 2.  Man of Bree goes, and his Dagger, a [men] card that is no minion, with
		// him: one offer, for the Man.
		scn.FreepsPlayCard(what1);
		scn.FreepsChooseCard(bree);
		assertInZone(Zone.DISCARD, bree, dagger);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, bree);
		assertEquals(1, scn.GetStackedCards(oath).size());
		assertInZone(Zone.DISCARD, dagger);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A second time, for a [men] minion who is no Man.
		scn.ShadowPass();
		scn.FreepsPlayCard(what2);
		scn.FreepsChooseCard(saruman);
		assertInZone(Zone.DISCARD, saruman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, saruman);
		assertEquals(2, scn.GetStackedCards(oath).size());

		// A Man of [dunland]: no offer.
		scn.ShadowPass();
		scn.FreepsPlayCard(what3);
		assertInZone(Zone.DISCARD, brigand);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertEquals(2, scn.GetStackedCards(oath).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTwoCopiesTheMinionGoesToTheCopyTakenAndIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var what1 = scn.GetFreepsCard("what1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(what1);

		var oath = scn.GetShadowCard("oath");
		var oath2 = scn.GetShadowCard("oath2");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(oath, oath2);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.SUPPORT, oath, oath2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(what1);
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath2));
		scn.ShadowChooseOptionalTrigger(oath2);

		assertInZone(Zone.STACKED, bree);
		assertEquals(1, scn.GetStackedCards(oath2).size());
		assertEquals(0, scn.GetStackedCards(oath).size());
		// The Man is stacked: the other copy has nothing left to stack.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningLeavesTheMinionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var what1 = scn.GetFreepsCard("what1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(what1);

		var oath = scn.GetShadowCard("oath");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.SUPPORT, oath);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(what1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, bree);
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMenMinionDiscardedInASkirmishIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var oath = scn.GetShadowCard("oath");
		var column = scn.GetShadowCard("column");
		var squad = scn.GetShadowCard("squad");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(column, squad);
		assertInZone(Zone.SUPPORT, oath);
		scn.SkipToAssignments();
		// Both minions on Aragorn, so that his skirmish goes on once the Column has gone.
		scn.FreepsAssignToMinions(aragorn, column);
		scn.ShadowAssignToMinions(aragorn, squad);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(column));
		scn.ShadowUseCardAction(column);
		assertInZone(Zone.DISCARD, column);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThisPossessionToMakeAMenManStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var oath = scn.GetShadowCard("oath");
		var oath2 = scn.GetShadowCard("oath2");
		var squad = scn.GetShadowCard("squad");
		var bree = scn.GetShadowCard("bree");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(oath, oath2);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, bree, saruman, brigand);
		assertInZone(Zone.SUPPORT, oath, oath2);
		scn.SkipToAssignments();
		// Aragorn takes the Squad; the other three are left unassigned.
		scn.FreepsAssignToMinions(aragorn, squad);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int squadStrength = scn.GetStrength(squad);
		int breeStrength = scn.GetStrength(bree);
		assertTrue(scn.ShadowActionAvailable(oath));
		scn.ShadowUseCardAction(oath);
		// A [men] Man in the skirmish and one in none; not a [men] minion who is no Man, nor a Man of [dunland].
		assertTrue(scn.ShadowHasCardChoiceAvailable(squad, bree));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, brigand));
		scn.ShadowChooseCard(squad);

		assertInZone(Zone.DISCARD, oath);
		assertInZone(Zone.SUPPORT, oath2);
		assertEquals(squadStrength + 2, scn.GetStrength(squad));
		assertEquals(breeStrength, scn.GetStrength(bree));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheStrengthBonusIsGoneInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var oath = scn.GetShadowCard("oath");
		var squad = scn.GetShadowCard("squad");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, saruman);
		assertInZone(Zone.SUPPORT, oath);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, squad}, new PhysicalCardImpl[]{gimli, saruman});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(squad));
		scn.ShadowUseCardAction(oath);
		assertEquals(11, scn.GetStrength(squad));
		assertInZone(Zone.DISCARD, oath);

		// Still in force at the next decision of the same skirmish.
		scn.FreepsPass();
		assertEquals(11, scn.GetStrength(squad));
		// 11 against 8: Aragorn loses and the Squad lives on into Gimli's skirmish, where the bonus is gone.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, saruman));
		assertEquals(9, scn.GetStrength(squad));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMenManInPlayTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var oath = scn.GetShadowCard("oath");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, brigand);
		assertInZone(Zone.SUPPORT, oath);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int sarumanStrength = scn.GetStrength(saruman);
		int brigandStrength = scn.GetStrength(brigand);
		// A [men] minion who is no Man and a Man of [dunland]: the possession is still offered, and goes for nothing.
		assertTrue(scn.ShadowActionAvailable(oath));
		scn.ShadowUseCardAction(oath);

		assertInZone(Zone.DISCARD, oath);
		assertEquals(sarumanStrength, scn.GetStrength(saruman));
		assertEquals(brigandStrength, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var oath = scn.GetShadowCard("oath");
		var squad = scn.GetShadowCard("squad");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(squad);
		assertInZone(Zone.SUPPORT, oath);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(oath));
	}
}
