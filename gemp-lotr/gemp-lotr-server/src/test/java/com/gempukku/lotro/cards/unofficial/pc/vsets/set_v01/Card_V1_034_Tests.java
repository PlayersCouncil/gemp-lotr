package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("waters", "101_34");
					put("waters2", "101_34");

					// Tentacles: [moria] minions of site 4.  All but the Vile Tentacle are discarded away from a marsh.
					// Vile Tentacle: cost 2, vitality 2.  Its Shadow ability is never used here.
					put("vile", "101_36");
					// Reaching Tentacle: cost 2, vitality 2.  Stronger for each tentacle; nothing when played.
					put("reaching", "19_19");
					// Strong Tentacle: cost 3, vitality 2.  The same text as Reaching Tentacle.
					put("strong", "19_20");
					// Foul Tentacle: vitality 1.  May fetch a tentacle when played, so it is never played here.
					put("foul", "2_58");
					// Huge Tentacle: vitality 2, damage +1.  May fetch the Watcher when played, so never played here.
					put("huge", "2_66");

					// Goblin Patrol Troop: a [moria] minion with no text, vitality 3, and not a tentacle.
					put("troop", "1_177");

					// Legolas, Greenleaf: an archer.  "Archery: Exert Legolas to wound a minion; Legolas does not add
					// to the fellowship archery total."  Vitality 3, so he can do that twice.
					put("legolas", "1_50");
					// Aragorn, Ranger of the North: strength 8, vitality 4, and not an archer.
					put("aragorn", "1_89");
					// Song of Durin: "Fellowship: Discard this condition to discard up to 2 [moria] conditions."
					put("song", "3_5");
					put("song2", "3_5");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OutofDarkWatersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Out of Dark Waters
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a tentacle takes a wound, you may stack that tentacle here.
		 * Shadow: Play a tentacle from here as if from hand.
		 * Response: If this condition is about to be discarded, discard 2 tentacles stacked here to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("waters");

		assertEquals("Out of Dark Waters", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waters = scn.GetShadowCard("waters");
		scn.MoveCardsToHand(waters);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		// 1 companion + 2 for site 2
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.HAND, waters);
		assertTrue(scn.ShadowPlayAvailable(waters));

		scn.ShadowPlayCard(waters);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, waters);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WoundedTentacleMayBeStackedHereAndAWoundedOrcIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waters = scn.GetShadowCard("waters");
		var waters2 = scn.GetShadowCard("waters2");
		var vile = scn.GetShadowCard("vile");
		var reaching = scn.GetShadowCard("reaching");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		// Two copies of the condition, so that "here" has another copy to be told from.
		scn.MoveCardsToSupportArea(waters, waters2);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		// Every site is a marsh, so that the tentacles which are discarded away from one stay in play.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));
		scn.MoveMinionsToTable(vile, reaching, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(vile));

		// A wound on a [moria] minion that is not a tentacle.
		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoiceAvailable(vile, reaching, troop));
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPassCurrentPhaseAction();

		// The same wound on a tentacle.
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(vile);

		assertEquals(1, scn.GetWoundsOn(vile));
		// Each copy makes its own offer for the one wound.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters2));
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertEquals(0, scn.GetStackedCards(waters).size());
		assertEquals(0, scn.GetStackedCards(waters2).size());
		scn.ShadowChooseOptionalTrigger(waters2);

		// "Here" is the copy whose offer was taken, and not the other.
		assertInZone(Zone.STACKED, vile);
		assertEquals(1, scn.GetStackedCards(waters2).size());
		assertTrue(scn.GetStackedCards(waters2).contains(vile));
		assertEquals(0, scn.GetStackedCards(waters).size());

		// The first copy's offer for the same wound is still to be answered.  Declined, it changes nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(waters2));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.STACKED, vile);
		assertEquals(1, scn.GetStackedCards(waters2).size());
		assertTrue(scn.GetStackedCards(waters2).contains(vile));
		assertEquals(0, scn.GetStackedCards(waters).size());
		// The tentacle that took no wound, and the Orc that took one, are where they were.
		assertInZone(Zone.SHADOW_CHARACTERS, reaching, troop);
		assertEquals(0, scn.GetWoundsOn(reaching));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryWoundOnATentacleMayBeStackedHereToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waters = scn.GetShadowCard("waters");
		var huge = scn.GetShadowCard("huge");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(waters);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		// Every site is a marsh, so that the tentacles which are discarded away from one stay in play.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));
		scn.MoveMinionsToTable(huge);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(huge));
		assertInZone(Zone.SHADOW_CHARACTERS, huge);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// Legolas is the fellowship's one archer and the Huge Tentacle the only minion, so it takes the wound.
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(huge));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, huge);
		assertEquals(1, scn.GetStackedCards(waters).size());
		assertTrue(scn.GetStackedCards(waters).contains(huge));
		// No minion is left, so the turn goes on to the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TentacleKilledByTheWoundMayStillBeStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waters = scn.GetShadowCard("waters");
		var waters2 = scn.GetShadowCard("waters2");
		var foul = scn.GetShadowCard("foul");
		var legolas = scn.GetFreepsCard("legolas");
		// Two copies of the condition, so that "here" has another copy to be told from.
		scn.MoveCardsToSupportArea(waters, waters2);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		// Every site is a marsh, so that the tentacles which are discarded away from one stay in play.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));
		scn.MoveMinionsToTable(foul);
		scn.SkipToPhase(Phase.ARCHERY);

		// The Foul Tentacle has 1 vitality: the fellowship's archery wound is its last.
		assertEquals(1, scn.GetVitality(foul));
		assertInZone(Zone.SHADOW_CHARACTERS, foul);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetStackedCards(waters).size());
		assertEquals(0, scn.GetStackedCards(waters2).size());

		scn.PassCurrentPhaseActions();

		// Each copy makes its own offer for the one wound.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters2));
		scn.ShadowChooseOptionalTrigger(waters2);

		// "Here" is the copy whose offer was taken, and not the other.
		assertInZone(Zone.STACKED, foul);
		assertEquals(1, scn.GetStackedCards(waters2).size());
		assertTrue(scn.GetStackedCards(waters2).contains(foul));
		assertEquals(0, scn.GetStackedCards(waters).size());
		assertEquals(0, scn.GetShadowDiscardCount());

		// The first copy's offer for the same wound is still to be answered.  Taken as well, it finds the
		// tentacle already stacked and leaves it on the copy it went to.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(waters2));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, foul);
		assertEquals(1, scn.GetStackedCards(waters2).size());
		assertTrue(scn.GetStackedCards(waters2).contains(foul));
		assertEquals(0, scn.GetStackedCards(waters).size());
		assertEquals(0, scn.GetShadowDiscardCount());
		// No minion is left, so the turn goes on to the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWoundOnTheSameTentacleOffersTheStackAgainAndDecliningLeavesItInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waters = scn.GetShadowCard("waters");
		var vile = scn.GetShadowCard("vile");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(waters);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		// The Goblin Patrol Troop is on the table so that Legolas's ability has a choice to make: with one minion
		// the game would pick the tentacle unasked.
		scn.MoveMinionsToTable(vile, troop);
		// No tentacle has more than 2 vitality.  One more, so that a second wound is not this one's last.
		scn.ApplyAdHocModifier(new VitalityModifier(null, vile, null, 1));
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(3, scn.GetVitality(vile));
		assertEquals(0, scn.GetWoundsOn(vile));

		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(vile);

		assertEquals(1, scn.GetWoundsOn(vile));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters));
		scn.ShadowDeclineOptionalTrigger();

		// Declined: the tentacle is still in play, wound and all, and nothing is stacked.
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertEquals(1, scn.GetWoundsOn(vile));
		assertEquals(0, scn.GetStackedCards(waters).size());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPassCurrentPhaseAction();

		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(vile);

		assertEquals(2, scn.GetWoundsOn(vile));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, vile);
		assertEquals(1, scn.GetStackedCards(waters).size());
		assertTrue(scn.GetStackedCards(waters).contains(vile));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void TentacleThatExertsIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waters = scn.GetShadowCard("waters");
		var vile = scn.GetShadowCard("vile");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(waters);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(vile);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, vile);
		scn.FreepsResolveSkirmish(aragorn);

		// Site 2 is Ettenmoors: "Skirmish: Exert your companion or minion to make that character strength +2."
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(vile));
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(scn.GetCurrentSite()));

		scn.ShadowUseCardAction(scn.GetCurrentSite());

		// The exertion is on the tentacle, and it is not a wound taken.
		assertEquals(1, scn.GetWoundsOn(vile));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, vile);
		assertEquals(0, scn.GetStackedCards(waters).size());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ShadowActionPlaysAChosenTentacleFromHereForItsCostAndRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waters = scn.GetShadowCard("waters");
		var waters2 = scn.GetShadowCard("waters2");
		var vile = scn.GetShadowCard("vile");
		var reaching = scn.GetShadowCard("reaching");
		var strong = scn.GetShadowCard("strong");
		var huge = scn.GetShadowCard("huge");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(waters, waters2);
		// Here: two tentacles, and a minion that is not a tentacle.  The text only ever stacks tentacles here, so
		// the Orc is the nearest thing to a wrong card that can be put on the stack.
		scn.StackCardsOn(waters, vile, reaching, troop);
		// A tentacle on the other copy, and one in the discard pile.
		scn.StackCardsOn(waters2, strong);
		scn.MoveCardsToDiscard(huge);

		scn.StartGame();
		// Every site is a marsh, so that the tentacles which are discarded away from one stay in play.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));
		// Enough that every card kept out of the choice below could be paid for, and is kept out only for what
		// or where it is: the 8 this comes to is exactly the Goblin Patrol Troop's 6 + 2 roaming, and covers the
		// Strong Tentacle's 3 + 2 and the Huge Tentacle's 2 + 2 as well.  With less, the Orc would be out of
		// reach anyway and its absence from the choice would show nothing.
		scn.SetTwilight(5);
		scn.FreepsPassCurrentPhaseAction();

		// 5 set + 1 companion + 2 for site 2
		assertEquals(8, scn.GetTwilight());
		assertEquals(3, scn.GetStackedCards(waters).size());
		assertEquals(1, scn.GetStackedCards(waters2).size());
		assertTrue(scn.ShadowActionAvailable(waters));

		scn.ShadowUseCardAction(waters);

		assertTrue(scn.ShadowHasCardChoiceAvailable(vile, reaching));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, strong, huge));
		scn.ShadowChooseCard(reaching);

		// 2 to play + 2 roaming: a minion of site 4 at site 2 costs 2 more, as it would from hand.
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, reaching);
		assertEquals(0, scn.GetWoundsOn(reaching));
		assertInZone(Zone.STACKED, vile, troop, strong);
		assertEquals(2, scn.GetStackedCards(waters).size());
		assertEquals(1, scn.GetStackedCards(waters2).size());
		assertInZone(Zone.DISCARD, huge);
		// The Vile Tentacle is still here and the pool still covers it.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(waters));

		// In the maneuver phase, on the same board, it is not offered.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(waters));
	}

	@Test
	@Ignore("F-0036: the Shadow action is offered, and does nothing, when no tentacle stacked here can be played")
	public void ShadowActionIsNotOfferedWhenNoTentacleHereCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waters = scn.GetShadowCard("waters");
		var waters2 = scn.GetShadowCard("waters2");
		var vile = scn.GetShadowCard("vile");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(waters, waters2);
		// One copy holds a tentacle; the other holds only a minion that is not one.
		scn.StackCardsOn(waters, vile);
		scn.StackCardsOn(waters2, troop);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		// 1 companion + 2 for site 2.  The Vile Tentacle costs 2 + 2 roaming, one more than the pool holds, and
		// the Goblin Patrol Troop, at 6 + 2, is not a tentacle.
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetStackedCards(waters).size());
		assertEquals(1, scn.GetStackedCards(waters2).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(waters));
		assertFalse(scn.ShadowActionAvailable(waters2));
	}

	@Test
	public void ResponseDiscardsTwoChosenTentaclesStackedHereToKeepTheConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		scn.MoveCardsToSupportArea(song);

		var waters = scn.GetShadowCard("waters");
		var waters2 = scn.GetShadowCard("waters2");
		var foul = scn.GetShadowCard("foul");
		var huge = scn.GetShadowCard("huge");
		var vile = scn.GetShadowCard("vile");
		var strong = scn.GetShadowCard("strong");
		var reaching = scn.GetShadowCard("reaching");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(waters, waters2);
		// Here: three tentacles, one more than the cost, and a minion that is not a tentacle.
		scn.StackCardsOn(waters, foul, huge, vile, troop);
		// A tentacle stacked on the other copy.
		scn.StackCardsOn(waters2, strong);

		scn.StartGame();
		// Every site is a marsh, so that the tentacles which are discarded away from one stay in play.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));
		// A tentacle in play.
		scn.MoveMinionsToTable(reaching);

		assertEquals(4, scn.GetStackedCards(waters).size());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsUseCardAction(song);
		assertTrue(scn.FreepsHasCardChoiceAvailable(waters, waters2));
		scn.FreepsChooseCard(waters);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters));
		assertInZone(Zone.SUPPORT, waters);
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(foul, huge, vile));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, strong, reaching));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(foul, huge);

		assertInZone(Zone.DISCARD, foul, huge);
		assertEquals(2, scn.GetShadowDiscardCount());
		// The condition is still in play, with what was not discarded still on it.
		assertInZone(Zone.SUPPORT, waters, waters2);
		assertInZone(Zone.STACKED, vile, troop, strong);
		assertEquals(2, scn.GetStackedCards(waters).size());
		assertEquals(1, scn.GetStackedCards(waters2).size());
		assertInZone(Zone.SHADOW_CHARACTERS, reaching);
		// The Free Peoples player's cost is paid all the same.
		assertInZone(Zone.DISCARD, song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWithOneTentacleStackedHereNorForAnotherCopyAndIsWithExactlyTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var song2 = scn.GetFreepsCard("song2");
		scn.MoveCardsToSupportArea(song, song2);

		var waters = scn.GetShadowCard("waters");
		var waters2 = scn.GetShadowCard("waters2");
		var foul = scn.GetShadowCard("foul");
		var huge = scn.GetShadowCard("huge");
		var vile = scn.GetShadowCard("vile");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(waters, waters2);
		// The copy that is discarded first holds one tentacle and a minion that is not one.
		scn.StackCardsOn(waters2, vile, troop);
		// The other copy holds exactly the two tentacles its own response needs.
		scn.StackCardsOn(waters, foul, huge);

		scn.StartGame();

		assertEquals(2, scn.GetStackedCards(waters2).size());
		assertEquals(2, scn.GetStackedCards(waters).size());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsUseCardAction(song);
		assertTrue(scn.FreepsHasCardChoiceAvailable(waters, waters2));
		scn.FreepsChooseCard(waters2);

		// Nothing is offered: the condition goes, and what was stacked on it goes with it.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, waters2, vile, troop);
		assertEquals(3, scn.GetShadowDiscardCount());
		assertInZone(Zone.SUPPORT, waters);
		assertInZone(Zone.STACKED, foul, huge);
		assertEquals(2, scn.GetStackedCards(waters).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The second Song takes the copy that holds two tentacles.
		scn.FreepsUseCardAction(song2);
		scn.FreepsChooseCard(waters);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(waters));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SUPPORT, waters);
		assertInZone(Zone.DISCARD, foul, huge);
		assertEquals(0, scn.GetStackedCards(waters).size());
		assertEquals(5, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
