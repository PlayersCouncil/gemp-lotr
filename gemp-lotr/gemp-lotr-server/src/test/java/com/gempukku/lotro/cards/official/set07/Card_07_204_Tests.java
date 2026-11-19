package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_204_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("outofsight", "7_204");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgul, cost 4, site 3; her text lowers a
					// character who bears a [wraith] condition, and none does
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: the [raider] Nazgul, cost 6, site 3; fierce, and his texts
					// need 3 burdens
					put("khamul", "103_61");
					// Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgul, cost 5, site 2, so not roaming at
					// site 2; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Úlairë Toldëa, Messenger of Morgul: a Nazgul of another title, for the Free Peoples player's
					// own discard pile; fierce, and an assignment ability that needs 4 burdens
					put("toldea", "1_236");
					// Morgul Hound: a [wraith] Orc, no Nazgul, cost 2; +4 and fierce only at 2 wounds on the
					// Ring-bearer
					put("hound", "7_192");

					// Sam, Son of Hamfast: a companion already in play; his abilities are never used
					put("sam", "1_311");
					// Gimli, Son of Glóin: [dwarven] Dwarf companion, cost 2, no requirement to play
					put("gimli", "1_13");
					// Boromir, Son of Denethor: [gondor] Man companion, cost 3, no requirement to play
					put("boromir", "1_97");
					// Bounder: [shire] Hobbit ally, cost 1, no requirement to play
					put("bounder", "1_286");
					// Barliman Butterbur: an ally already in play, vitality 2; his ability is never used
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OutofSightandShotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Out of Sight and Shot
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a Nazgûl.
		 * Each time a companion is played, the Free Peoples player must exert a companion.
		 * <b>Shadow:</b> Spot a Nazgûl and discard this condition to play a Nazgûl from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("outofsight");

		assertEquals("Out of Sight and Shot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilANazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var outofsight = scn.GetShadowCard("outofsight");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(outofsight, lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Only the Morgul Hound, a [wraith] Orc who is no Nazgul, is in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(outofsight));

		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.ShadowPlayAvailable(outofsight));

		scn.ShadowPlayCard(outofsight);
		assertInZone(Zone.SUPPORT, outofsight);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheRaiderNazgulIsSpottedToPlayItAndItsAbilityWantsTheNazgulsCostInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var outofsight = scn.GetShadowCard("outofsight");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(outofsight);
		scn.MoveCardsToShadowDiscard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInDiscard(nelya);
		assertTrue(scn.ShadowPlayAvailable(outofsight));
		scn.ShadowPlayCard(outofsight);
		assertInZone(Zone.SUPPORT, outofsight);

		// Nelya costs 5 at site 2, her own site number: one short of it, the play cannot be paid.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(outofsight));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(outofsight));
	}

	@Test
	public void ShadowAbilityDiscardsItToPlayANazgulFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var outofsight = scn.GetShadowCard("outofsight");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var freepsToldea = scn.GetFreepsCard("toldea");
		scn.MoveCardsToSupportArea(outofsight);
		scn.MoveCardsToShadowDiscard("khamul", "nelya", "hound");
		scn.MoveCardsToFreepsDiscard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInDiscard(khamul, nelya, hound, freepsToldea);
		assertInZone(Zone.SUPPORT, outofsight);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.ShadowActionAvailable(outofsight));

		scn.ShadowUseCardAction(outofsight);
		assertInDiscard(outofsight);
		// Both Nazgul in his own discard pile; neither the Hound, no Nazgul, nor the Nazgul in the other player's
		// discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(khamul, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, freepsToldea));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(khamul);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertInDiscard(nelya, hound, freepsToldea);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilANazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var outofsight = scn.GetShadowCard("outofsight");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(outofsight);
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToShadowDiscard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Only the Morgul Hound is in play; Nelya lies in the discard pile with the pool to pay for her.
		assertInDiscard(nelya);
		assertInZone(Zone.SUPPORT, outofsight);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(outofsight));

		// The pool is 10 + 2 for site 2 + 1 for the companion = 13; Lemenya costs 4 + 2 roaming, leaving 7, enough for
		// Nelya's 5.
		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.ShadowActionAvailable(outofsight));
		assertInZone(Zone.SUPPORT, outofsight);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeACompanionIsPlayedTheFreePeoplesPlayerMustExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var outofsight = scn.GetShadowCard("outofsight");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(gimli, boromir, bounder);

		scn.StartGame();
		scn.MoveCardsToSupportArea(outofsight);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));

		// A companion played: the Free Peoples player must choose a companion to exert, the one just played
		// among them, and no ally.
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another companion, another exertion.
		scn.FreepsPlayCard(boromir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, gimli, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An ally played: nothing is asked and nobody exerts.
		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
