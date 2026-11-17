package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_343_Tests
{

	// Balin's Tomb is site 4 of the Fellowship sites; the tests use the copy on the path.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Greatest Kingdom of My People: unique [dwarven] tale, a support-area condition; its trigger
					// answers an Orc played by the opponent
					put("kingdom", "1_16");
					// Red Book of Westmarch: unique [shire] tale, a support-area possession; its trigger needs Bilbo
					put("redbook", "2_113");
					// Banner of the White Tree: a [gondor] tale, a Maneuver event
					put("banner", "3_39");
					put("banner2", "3_39");
					// Lord of Moria: a [dwarven] support-area condition that is no tale; each Dwarf is damage +1
					put("lordofmoria", "1_21");
					// Elendil's Valor: a [gondor] Maneuver event that is no tale
					put("valor", "1_103");

					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf companion, vitality 3
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2; a skirmish ability
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BalinsTombStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Balin's Tomb
		 * Unique: False
		 * Shadow Number: 3
		 * Type: Site
		 * Block: Fellowship
		 * Direction: Right
		 * Site Number: 4
		 * Game Text: <b>Underground</b>. <b>Maneuver:</b> Discard your tale from play or from hand to heal your companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Balin's Tomb", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ManeuverActionDiscardsATaleFromPlayToHealACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var redbook = scn.GetFreepsCard("redbook");
		var banner = scn.GetFreepsCard("banner");
		var banner2 = scn.GetFreepsCard("banner2");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var valor = scn.GetFreepsCard("valor");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(kingdom, redbook, lordofmoria);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(banner, valor);
		scn.MoveCardsToDiscard(banner2);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(troop, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		var tomb = scn.GetCurrentSite();
		assertEquals("Balin's Tomb", tomb.getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, kingdom, redbook, lordofmoria);
		assertInHand(banner, valor);
		assertInDiscard(banner2);

		assertTrue(scn.FreepsActionAvailable(tomb));
		scn.FreepsUseCardAction(tomb);

		// A tale is both in play and in hand, so the player is asked which.
		assertEquals(List.of("Discard your tale from play", "Discard tale from hand"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard your tale from play");

		// A [dwarven] condition and a [shire] possession; not a condition in play that is no tale, nor the
		// tales in hand and in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(kingdom, redbook));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lordofmoria, banner, banner2, valor));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(kingdom);
		assertInDiscard(kingdom);
		assertInZone(Zone.SUPPORT, redbook, lordofmoria);
		assertInHand(banner, valor);

		// Two wounded companions, the Ring-bearer among them; not a wounded ally, nor a wounded minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionDiscardsATaleFromHandToHealACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var redbook = scn.GetFreepsCard("redbook");
		var banner = scn.GetFreepsCard("banner");
		var banner2 = scn.GetFreepsCard("banner2");
		var valor = scn.GetFreepsCard("valor");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(kingdom);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(banner, redbook, valor);
		scn.MoveCardsToDiscard(banner2);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.AddWoundsToChar(gimli, 2);

		scn.SkipToPhase(Phase.MANEUVER);
		var tomb = scn.GetCurrentSite();
		assertEquals("Balin's Tomb", tomb.getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, kingdom);
		assertInHand(banner, redbook, valor);
		assertInDiscard(banner2);

		scn.FreepsUseCardAction(tomb);
		scn.FreepsChooseOption("Discard tale from hand");

		// A [gondor] event and a [shire] possession in hand; not the card in hand that is no tale, nor the tale
		// in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(banner, redbook));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(valor, banner2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(banner);

		// Gimli is the only wounded companion, and is healed unasked.
		assertInDiscard(banner, banner2);
		assertInHand(redbook, valor);
		assertInZone(Zone.SUPPORT, kingdom);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoTaleInHandAndNoCompanionWoundedTheTaleInPlayIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var valor = scn.GetFreepsCard("valor");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(kingdom);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(valor);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));

		scn.SkipToPhase(Phase.MANEUVER);
		var tomb = scn.GetCurrentSite();
		assertEquals("Balin's Tomb", tomb.getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, kingdom);

		// The action has a cost that can be paid, so it is offered with nobody to heal.
		assertTrue(scn.FreepsActionAvailable(tomb));
		scn.FreepsUseCardAction(tomb);

		// Only the tale in play can pay: it is discarded unasked, and nothing is healed or asked.
		assertInDiscard(kingdom);
		assertInHand(valor);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoTaleInPlayTheTaleInHandIsDiscardedForTheHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var redbook = scn.GetFreepsCard("redbook");
		var banner = scn.GetFreepsCard("banner");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var valor = scn.GetFreepsCard("valor");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(lordofmoria);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(banner, redbook, valor);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.AddWoundsToChar(gimli, 2);

		scn.SkipToPhase(Phase.MANEUVER);
		var tomb = scn.GetCurrentSite();
		assertEquals("Balin's Tomb", tomb.getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, lordofmoria);
		assertInHand(banner, redbook, valor);

		assertTrue(scn.FreepsActionAvailable(tomb));
		scn.FreepsUseCardAction(tomb);

		// Only a condition that is no tale is in play, so the tale in play cannot pay and the player is asked
		// for a tale in hand at once.
		assertTrue(scn.FreepsHasCardChoiceAvailable(banner, redbook));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(valor, lordofmoria));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(redbook);

		assertInDiscard(redbook);
		assertInHand(banner, valor);
		assertInZone(Zone.SUPPORT, lordofmoria);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoTaleInPlayOrInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var valor = scn.GetFreepsCard("valor");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(lordofmoria);

		scn.StartGame();
		scn.SkipToSite(3);
		// A tale only in the discard pile; a condition in play and an event in hand that are no tales.
		scn.SetFreepsHand(valor);
		scn.MoveCardsToDiscard(banner);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.AddWoundsToChar(gimli, 2);

		scn.SkipToPhase(Phase.MANEUVER);
		var tomb = scn.GetCurrentSite();
		assertEquals("Balin's Tomb", tomb.getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInDiscard(banner);
		assertInZone(Zone.SUPPORT, lordofmoria);
		assertInHand(valor);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tomb));
	}

	@Test
	public void TheShadowPlayerMayDiscardHisTaleFromHandAndHealsNoneOfTheFreePeoplesCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var kingdom = scn.GetFreepsCard("kingdom");
		var frodo = scn.GetRingBearer();
		var shadowBanner = scn.GetShadowCard("banner");
		var shadowValor = scn.GetShadowCard("valor");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(kingdom);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand();
		scn.SetShadowHand(shadowBanner, shadowValor);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(frodo, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		var tomb = scn.GetCurrentSite();
		assertEquals("Balin's Tomb", tomb.getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(shadowBanner, shadowValor);
		assertInZone(Zone.SUPPORT, kingdom);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(tomb));
		scn.ShadowUseCardAction(tomb);

		// The Free Peoples player's tale is in play but is not his, so only the tale in his hand can pay and it is
		// discarded unasked; he has no companion of his own in play this turn, and the Free Peoples player's are
		// not his.
		assertInDiscard(shadowBanner);
		assertInHand(shadowValor);
		assertInZone(Zone.SUPPORT, kingdom);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
