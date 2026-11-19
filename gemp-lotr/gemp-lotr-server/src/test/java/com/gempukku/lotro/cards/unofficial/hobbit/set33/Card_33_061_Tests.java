package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_061_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sorcery", "33_61");
					// Orkish Aggressor: [gundabad] Orc, vitality 2; an Assignment ability never used
					put("aggressor", "30_39");
					// Orkish Veteran: [sauron] Orc, vitality 2; a Regroup ability never used, archer only at site 9
					put("veteran", "32_43");
					// Bill Ferny Sr.: a [gundabad] Man minion, no Orc, vitality 2; a Regroup ability never used
					put("ferny", "33_22");
					// Gandalf, The Grey: "Wise. Each time you play a [gandalf] event, you may wound a minion (except
					// Smaug). Fellowship: Add 2 doubts to play an artifact or a Dwarf companion from your draw deck or
					// discard pile."
					put("gandalf", "30_27");
					// Saruman, The White: [gandalf] Wizard ally, "Wise. Gandalf is defender +1."; his abilities are
					// never used
					put("saruman", "32_24");
					// Thorin, Oakenshield: a Dwarf companion in the draw deck, for Gandalf's Fellowship ability to find
					put("thorin", "30_19");
					// Gathering of the Three Rings: a [gandalf] Maneuver event, "Make X minions strength -X until the
					// regroup phase", X the Gandalfs, Elronds and Galadriels spotted
					put("gathering1", "32_22");
					put("gathering2", "32_22");
					// Saruman, Servant of the Eye: an [isengard] Wizard minion whose first sentence is "Saruman may
					// not take wounds during the archery phase and may not be assigned to a skirmish" (the Hobbit sets
					// print no Saruman minion)
					put("servant", "3_69");
					// Goblin Patrol Troop: a [moria] Orc minion with no text, in the draw deck to be drawn
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DarkSorceryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Dark Sorcery
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert an Orc.<br>The first sentence of Gandalf's game text does not apply.<br><b>Shadow:</b> Discard this condition to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sorcery");

		assertEquals("Dark Sorcery", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsAnOrcOfEitherCultureAndNotAMinionWhoIsNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorcery = scn.GetShadowCard("sorcery");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCardsToHand(sorcery);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, veteran, ferny);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(ferny));

		assertTrue(scn.ShadowPlayAvailable(sorcery));
		scn.ShadowPlayCard(sorcery);
		// Orcs of two cultures; not the Man, who could exert as well.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(veteran);

		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(ferny));
		assertInZone(Zone.SUPPORT, sorcery);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorcery = scn.GetShadowCard("sorcery");
		var aggressor = scn.GetShadowCard("aggressor");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCardsToHand(sorcery);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, ferny);
		// Vitality 2 with 1 wound: the Orc cannot exert.  The Man beside him can, and is no Orc.
		scn.AddWoundsToChar(aggressor, 1);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(ferny));
		assertInZone(Zone.HAND, sorcery);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sorcery));

		scn.RemoveWoundsFromChar(aggressor, 1);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.ShadowPlayAvailable(sorcery));
	}

	@Test
	public void OncePlayedGandalfsSentenceAfterWiseNoLongerOffersItsWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorcery = scn.GetShadowCard("sorcery");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gathering1 = scn.GetFreepsCard("gathering1");
		var gathering2 = scn.GetFreepsCard("gathering2");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(gathering1, gathering2);
		scn.MoveCardsToHand(sorcery);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, veteran);
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(veteran));

		// Without Dark Sorcery: a [gandalf] event offers Gandalf's wound.
		scn.FreepsPlayCard(gathering1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsChooseCard(aggressor);
		assertInDiscard(gathering1);

		// No skirmishes; the fellowship moves on in the regroup phase, and in that Shadow phase Dark Sorcery is
		// played.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(sorcery);
		scn.ShadowChooseCard(veteran);
		assertInZone(Zone.SUPPORT, sorcery);
		scn.SkipToPhase(Phase.MANEUVER);

		// With Dark Sorcery in play: the same event offers nothing of Gandalf's.
		scn.FreepsPlayCard(gathering2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsChooseCard(aggressor);
		assertInDiscard(gathering2);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void GandalfKeepsWiseAndHisLaterSentenceAndSarumansFirstSentenceStillMakesHimDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorcery = scn.GetShadowCard("sorcery");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToSupportArea(sorcery);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, sorcery);
		assertInZone(Zone.DECK, thorin);

		// "Wise." is the keyword line, not the first sentence (Q-0405: A).
		assertTrue(scn.HasKeyword(gandalf, Keyword.WISE));
		// Saruman's sentence after his own "Wise." is no sentence of Gandalf's.
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		// Gandalf's Fellowship ability is a later sentence, and still offered.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void SarumansFirstSentenceStillApplies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorcery = scn.GetShadowCard("sorcery");
		var servant = scn.GetShadowCard("servant");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(sorcery);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, aggressor);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SUPPORT, sorcery);

		// "Saruman ... may not be assigned to a skirmish": Dark Sorcery names Gandalf's text, not Saruman's.
		assertTrue(scn.FreepsCanAssign(aggressor));
		assertFalse(scn.FreepsCanAssign(servant));
		assertTrue(scn.ShadowCanAssign(aggressor));
		assertFalse(scn.ShadowCanAssign(servant));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsDarkSorceryToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorcery = scn.GetShadowCard("sorcery");
		var aggressor = scn.GetShadowCard("aggressor");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		scn.MoveCardsToSupportArea(sorcery);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetShadowDrawDeck(troop1, troop2, troop3);
		assertInZone(Zone.SUPPORT, sorcery);
		assertEquals(3, scn.GetShadowDeckCount());
		int hand = scn.GetShadowHandCount();

		assertTrue(scn.ShadowActionAvailable(sorcery));
		scn.ShadowUseCardAction(sorcery);

		assertInDiscard(sorcery);
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorcery = scn.GetShadowCard("sorcery");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(sorcery);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SUPPORT, sorcery);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sorcery));
	}
}
