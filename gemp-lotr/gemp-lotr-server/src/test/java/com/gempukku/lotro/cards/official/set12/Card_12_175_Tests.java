package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_175_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "12_175");
					// [wraith] events with toil, of two toil numbers and two phases: Dark Powers Strengthen (Shadow, cost 6,
					// Toil 2: spot a Nazgûl to draw 3 cards), Shadow in the East (Shadow, cost 4, Toil 1: to play, spot a
					// [wraith] card in your support area; spot a companion to draw a card for each wound on him, and one more
					// with a Nazgûl) and Keening Wail (Maneuver, cost 7, Toil 2: spot a Nazgûl to add a burden)
					put("darkpowers", "11_207");
					put("shadoweast", "13_180");
					put("wail", "11_211");
					// Not Easily Destroyed: a [wraith] condition for the support area; remove (3) to heal a Nazgûl, used only on purpose
					put("notdestroyed", "1_220");
					put("cry", "7_208");        // There Came a Cry: a [wraith] Shadow event with no toil, cost 1: spot a Nazgûl to add a threat
					// The Witch-king, Captain of the Nine Riders: a [wraith] card with toil that is no event (a minion, cost 8,
					// site 3, Toil 2; fierce and muster)
					put("witchking", "11_226");
					// Storming the Ramparts: an [orc] Maneuver event with Toil 2, cost 3: spot an [orc] minion to make the Free
					// Peoples player discard one of his conditions (he has none)
					put("storming", "12_103");
					// Denizen of the Black Pit: an [orc] Orc for Storming the Ramparts to spot; +2 only underground
					put("denizen", "11_117");
					// The other [wraith] characters for her own toil: Úlairë Nelya 2_84 (a Nazgûl; nothing below 2 burdens or
					// 2 wounds on the Ring-bearer) and Morgul Hound 7_192 (an Orc; nothing below 2 wounds on the Ring-bearer)
					put("nelya", "2_84");
					put("hound", "7_192");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text, who counts for no [wraith] toil
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion to wound; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResponseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "12_175");
					// Minas Morgul Answers: [wraith] Shadow event, cost 4.  "Toil 2.  Spot a Nazgûl to discard a
					// Free Peoples condition from play."
					put("answers", "12_167");
					// Sword Rack: a [rohan] support-area condition for the event to discard; its response answers a
					// [rohan] possession discarded from play, and there is none
					put("rack", "11_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Úlairë Enquëa, Black Threat
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>. <b>Toil 1</b>. <helper>(For each [wraith] character you exert when playing this, its twilight cost is -1.)</helper><br><b>Response:</b> If you are playing a [wraith] event that has toil X, exert Úlairë Enquëa to reduce that event's twilight cost by X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Black Threat", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ResponseExertsEnqueaToTakeTheToilNumberOffAWraithEventAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var enquea = scn.GetShadowCard("enquea");
		var answers = scn.GetShadowCard("answers");
		var rack = scn.GetFreepsCard("rack");
		scn.MoveCardsToSupportArea(rack);
		assertInZone(Zone.SUPPORT, rack);
		scn.MoveCardsToHand(answers);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.FreepsPass();

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(answers));
		assertEquals(0, scn.GetWoundsOn(enquea));

		// The response answers the event being played: it is offered before anything has been paid.
		scn.ShadowPlayCard(answers);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertNotInZone(Zone.DISCARD, answers);
		assertEquals(2, scn.GetTwilight());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));

		// He may answer the same play again (CR 4.2 "response"), and can still exert: declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();

		// Then toil is asked.  The event now costs 4 - 2, which the pool covers, so nobody need be exerted for
		// it; Enquëa is exerted all the same.
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(enquea);
		assertEquals(2, scn.GetWoundsOn(enquea));

		// 4 - 2 for Enquëa's response - 2 for toil = 0: nothing leaves the pool.
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(answers);
		assertInDiscard(rack);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseAloneTakesTwoOffAToilTwoEventWithNobodyExertedForToil() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var enquea = scn.GetShadowCard("enquea");
		var answers = scn.GetShadowCard("answers");
		var rack = scn.GetFreepsCard("rack");
		scn.MoveCardsToSupportArea(rack);
		assertInZone(Zone.SUPPORT, rack);
		scn.MoveCardsToHand(answers);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.FreepsPass();

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(enquea));

		scn.ShadowPlayCard(answers);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));
		scn.ShadowDeclineOptionalTrigger();

		// The response has taken the event's own toil number off, 4 - 2, and not Enquëa's Toil 1: with 3 still
		// to pay from a pool of 2, an exertion for toil would be owed.  None is.
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(enquea));

		// 4 - 2 for Enquëa's response = 2, paid from the pool.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(answers);
		assertInDiscard(rack);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ToilTakesOneOffHerCostForEachWraithCharacterExertedAsSheIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(enquea);
		scn.MoveMinionsToTable(nelya, hound, troop);
		scn.FreepsPass();
		// Site 3 is her site number: she is not roaming.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 6 - 1 - 1 is the least she can cost, with both [wraith] minions exerted; the [moria] Troop does not count.
		// Toil 2 would let 2 do.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(enquea));
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(enquea));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowPlayCard(enquea);
		// The pool is 1 short of her 6: one exertion is owed, and the second is the player's to make or not.
		// Toil 2 would owe none.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));

		scn.ShadowChooseCard(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 6 - 1 for the Hound = 5, the whole pool.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseTakesTwoOffAToilTwoWraithEventAndCanBeUsedAgainWhileSheCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var darkpowers = scn.GetShadowCard("darkpowers");

		scn.StartGame();
		scn.SetShadowHand(darkpowers);
		scn.MoveMinionsToTable(enquea);
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertInHand(darkpowers);
		assertEquals(0, scn.GetWoundsOn(enquea));
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(darkpowers);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));

		// She may answer the same play again (CR 4.2 "response") while she can exert: declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();

		// Toil is asked, nobody owed; nobody exerted.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea));
		scn.ShadowDeclineChoosing();

		assertEquals(1, scn.GetWoundsOn(enquea));
		// 6 - 2 for the response = 4, from a pool of 6.
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(darkpowers);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseTakesOneOffAToilOneWraithEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var shadoweast = scn.GetShadowCard("shadoweast");
		var notdestroyed = scn.GetShadowCard("notdestroyed");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.StartGame();
		scn.SetShadowHand(shadoweast);
		scn.MoveCardsToSupportArea(notdestroyed);
		scn.MoveMinionsToTable(enquea);
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertInHand(shadoweast);
		assertEquals(0, scn.GetWoundsOn(enquea));

		scn.ShadowPlayCard(shadoweast);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(enquea));

		// Aragorn is spotted: a card for his wound and one for the Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn));
		scn.ShadowChooseCard(aragorn);
		// 4 - 1 for the response = 3, from a pool of 4.  Toil 2 would have left 2.
		assertEquals(1, scn.GetTwilight());
		assertInDiscard(shadoweast);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedForAWraithEventWithNoToilNorForAWraithMinionWithToil() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var cry = scn.GetShadowCard("cry");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(cry, witchking);
		scn.MoveMinionsToTable(enquea);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(cry, witchking);
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetThreats());

		// A [wraith] event with no toil: played straight through, with no response asked.
		scn.ShadowPlayCard(cry);
		assertEquals(1, scn.GetThreats());
		assertInDiscard(cry);
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [wraith] card with toil that is no event: toil is asked, and no response before it.
		scn.ShadowPlayCard(witchking);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea));
		scn.ShadowDeclineChoosing();
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseAnswersAWraithManeuverEventWithToilAndNotAnOrcOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var storming = scn.GetShadowCard("storming");
		var wail = scn.GetShadowCard("wail");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.SetShadowHand(storming, wail);
		scn.MoveMinionsToTable(enquea, denizen);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertInHand(storming, wail);
		assertEquals(0, scn.GetWoundsOn(enquea));
		int pool = scn.GetTwilight();

		// An [orc] event with toil: toil is asked (the Denizen could exert for it), and no response before it.
		scn.ShadowPlayCard(storming);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen));
		scn.ShadowDeclineChoosing();
		assertInDiscard(storming);
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(pool - 3, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(wail);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowDeclineChoosing();

		// 7 - 2 for the response = 5.
		assertEquals(pool - 3 - 5, scn.GetTwilight());
		assertInDiscard(wail);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhileSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var darkpowers = scn.GetShadowCard("darkpowers");

		scn.StartGame();
		scn.SetShadowHand(darkpowers);
		scn.MoveMinionsToTable(enquea);
		scn.AddWoundsToChar(enquea, 3);
		assertEquals(3, scn.GetWoundsOn(enquea));
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertInHand(darkpowers);
		assertEquals(3, scn.GetWoundsOn(enquea));

		// Vitality 4 with 3 wounds: she cannot exert, for the response or for toil, and the pool pays the whole 6.
		scn.ShadowPlayCard(darkpowers);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(darkpowers);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(3, scn.GetWoundsOn(enquea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseAndToilTogetherLetAWraithEventBePlayedFromAPoolThatToilAloneCannotCover() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var darkpowers = scn.GetShadowCard("darkpowers");

		scn.StartGame();
		scn.SetShadowHand(darkpowers);
		scn.MoveMinionsToTable(enquea);
		scn.FreepsPass();
		// 6 - 2 for her toil exertion - 2 for her response = 2.  Toil alone leaves 4 to pay.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertInHand(darkpowers);
		assertEquals(0, scn.GetWoundsOn(enquea));

		assertTrue(scn.ShadowPlayAvailable(darkpowers));
		scn.ShadowPlayCard(darkpowers);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowDeclineOptionalTrigger();
		// With 4 still to pay from 2, her exertion for toil is owed, and she is the one candidate.
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(darkpowers);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
