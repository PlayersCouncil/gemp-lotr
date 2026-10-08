package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_085_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tgat", "53_85");
					put("tgat2", "53_85");

					put("twigul", "101_40");    // Ringwraith in Twilight: [wraith] Nazgul, vitality 3; an optional trigger at the start of the maneuver phase
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, vitality 2; its text needs 3 burdens
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgul; its text needs 3 burdens
					put("lord", "4_219");       // Desert Lord: [raider] Man; "Archery: Exert Desert Lord to exert a companion (except the Ring-bearer)"
					put("breath", "1_207");     // Black Breath: a [wraith] condition; wounds may not be removed from bearer
					put("outofsight", "7_204"); // Out of Sight and Shot: [wraith] condition; "Each time a companion is played, the Free Peoples player must exert a companion."

					put("sam", "1_311");        // Sam, Son of Hamfast: [shire], Ring-bound; "Fellowship: Exert Sam to remove a burden."
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor]; "Maneuver: Exert Aragorn to make him defender +1 ..."
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire], Frodo signet; his text is about [shire] tales only
					put("bounder", "1_286");    // Bounder: [shire] ally, vitality 2; a skirmish ability
					put("bounder2", "1_286");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] card for the hand
					put("boromir2", "1_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TooGreatandTerribleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Too Great and Terrible
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Game Text: To play, exert a [ringwraith] minion. Bearer must be a companion or ally (except the
		 * Ring-bearer). Limit 1 per bearer.
		 * Each time bearer is exerted by a Free Peoples card, exert bearer unless the Free Peoples player
		 * discards a card from hand of bearer's culture.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tgat");

		assertEquals("Too Great and Terrible", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsARingwraithMinionToPlayOnACompanionOrAllyExceptTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var twigul = scn.GetShadowCard("twigul");
		var creeper = scn.GetShadowCard("creeper");
		var attea = scn.GetShadowCard("attea");
		var lord = scn.GetShadowCard("lord");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(tgat);
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(twigul, creeper, attea, lord);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertInHand(tgat);
		assertTrue(scn.ShadowPlayAvailable(tgat));
		scn.ShadowPlayCard(tgat);

		// The bearer: Sam (Ring-bound, not the Ring-bearer) and the ally; not Frodo, the Ring-bearer, nor a minion.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scn.GetRingBearer(), twigul, creeper));
		scn.ShadowChooseCard(bounder);

		// The exertion: either [wraith] minion, Nazgul or Orc; not the [raider] Nazgul nor the [raider] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(twigul, creeper));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attea, lord));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(creeper);

		assertAttachedTo(tgat, bounder);
		assertEquals(1, scn.GetWoundsOn(creeper));
		assertEquals(0, scn.GetWoundsOn(twigul));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(twilight - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenNoRingwraithMinionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var twigul = scn.GetShadowCard("twigul");
		var attea = scn.GetShadowCard("attea");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(tgat);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(twigul, attea);
		// The one [wraith] minion exhausted; the [raider] Nazgul beside it could exert.
		scn.AddWoundsToChar(twigul, 2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(tgat));

		scn.RemoveWoundsFromChar(twigul, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(tgat));
	}

	@Test
	public void ACharacterWhoBearsOneIsNotOfferedASecondButOneBearingAnotherWraithConditionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var tgat2 = scn.GetShadowCard("tgat2");
		var breath = scn.GetShadowCard("breath");
		var twigul = scn.GetShadowCard("twigul");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(tgat2);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(sam, tgat);
		scn.AttachCardsTo(bounder, breath);

		scn.StartGame();
		scn.MoveMinionsToTable(twigul);
		scn.FreepsPass();

		scn.ShadowPlayCard(tgat2);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam));
		scn.ShadowChooseCard(bounder);

		assertAttachedTo(tgat2, bounder);
		assertAttachedTo(tgat, sam);
		assertEquals(1, scn.GetWoundsOn(twigul));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeBearerExertsForAFreePeoplesCardTheFreePeoplesPlayerDiscardsACardOfHisCultureOrHeExertsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var bounder2 = scn.GetFreepsCard("bounder2");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(bounder, bounder2, boromir);
		scn.AttachCardsTo(sam, tgat);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		scn.FreepsChooseYes();
		// Sam is [shire]: the two [shire] cards, and not the [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bounder, bounder2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bounder);

		assertInDiscard(bounder);
		assertInHand(bounder2, boromir);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Again, and this time the Free Peoples player keeps his cards.
		scn.FreepsUseCardAction(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		scn.FreepsChooseNo();

		assertEquals(3, scn.GetWoundsOn(sam));
		assertInHand(bounder2, boromir);
		// The exertion this condition makes is not by a Free Peoples card, so nothing is asked again.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCardOfBearersCultureInHandBearerExertsWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(boromir);
		scn.AttachCardsTo(sam, tgat);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsUseCardAction(sam);

		assertFalse(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertInHand(boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertionByAShadowCardDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var lord = scn.GetShadowCard("lord");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(bounder);
		scn.AttachCardsTo(sam, tgat);

		scn.StartGame();
		scn.MoveMinionsToTable(lord);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(sam));
		// Sam is the one companion who is not the Ring-bearer, so Desert Lord's exertion goes to him.
		scn.ShadowUseCardAction(lord);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		assertInHand(bounder);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnExertionTheFreePeoplesPlayerMakesForAShadowCardDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var outofsight = scn.GetShadowCard("outofsight");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(aragorn, bounder);
		scn.AttachCardsTo(sam, tgat);
		scn.MoveCardsToSupportArea(outofsight);

		scn.StartGame();

		scn.FreepsPlayCard(aragorn);
		// Out of Sight and Shot has the Free Peoples player exert a companion: he picks Sam.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, aragorn));
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		assertInHand(bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherCompanionExertingDoesNotSetItOffAndABearerOfAnotherCultureAsksForThatCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var creeper = scn.GetShadowCard("creeper");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var boromir = scn.GetFreepsCard("boromir");
		var boromir2 = scn.GetFreepsCard("boromir2");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(bounder, boromir, boromir2);
		scn.AttachCardsTo(aragorn, tgat);

		scn.StartGame();

		// Sam exerts for a Free Peoples card, but he is not bearer.
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(creeper);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseYes();
		// Aragorn is [gondor]: the two [gondor] cards, and not the [shire] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, boromir2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		scn.FreepsChooseCard(boromir2);

		assertInDiscard(boromir2);
		assertInHand(boromir, bounder);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ExertionOfBearerByAFreePeoplesCardOtherThanBearerSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(bounder);
		scn.AttachCardsTo(pippin, tgat);

		scn.StartGame();
		// Frodo's fellowship action exerts another companion with the Frodo signet to heal himself, so he needs a wound.
		var frodo = scn.GetRingBearer();
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.FreepsUseCardAction(frodo);
		// Frodo, a Free Peoples card that is not the bearer, exerted Pippin.
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		scn.FreepsChooseNo();

		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInHand(bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenBearerIsExhaustedByTheExertionNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(bounder);
		scn.AttachCardsTo(sam, tgat);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 2);
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(sam);
		// Vitality 4 with 3 wounds: Sam cannot exert again, so there is nothing to prevent.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		assertInHand(bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
