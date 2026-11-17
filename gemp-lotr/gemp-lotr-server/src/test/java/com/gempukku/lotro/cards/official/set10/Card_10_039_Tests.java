package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ruffian", "10_39");
					put("ruffian2", "10_39");

					put("buccaneer", "8_53");   // Corsair Buccaneer: a corsair whose own text acts only as it is played
					put("gunners", "8_55");     // Corsair Gunners: a corsair, cost 5, with nothing on being played
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no corsair
					put("champion", "13_84");   // Corsair Champion: a [men] Man, "Corsair" in his title only

					put("raiders", "4_242");    // Raiders From the East: [raider] condition that holds [raider] tokens
					put("ships1", "8_65");      // Ships of Great Draught: [raider] possession in the support area
					put("ships2", "8_65");      //   that holds [raider] tokens
					put("fleet", "18_66");      // Fleet of Corsair Ships: [men] possession that holds [men] tokens

					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairRuffianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Corsair Ruffian
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br>When you play this minion, you may spot another corsair and discard the top card of the Free Peoples player's draw deck to add 2 [raider] tokens to a card that already has a [raider] token on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ruffian");

		assertEquals("Corsair Ruffian", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedBesideAnotherCorsairMayDiscardTheFreePeoplesTopCardToAddTwoRaiderTokensToACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var raiders = scn.GetShadowCard("raiders");
		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToHand(ruffian);
		scn.MoveCardsToSupportArea(raiders, ships1, ships2, fleet);
		scn.AddTokensToCard(raiders, 1);
		scn.AddTokensToCard(ships1, 2);
		// A [men] token: a token, but not a [raider] one.
		scn.AddTokensToCard(fleet, 1);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer);
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(2, scn.GetCultureTokensOn(ships1));
		assertEquals(0, scn.GetCultureTokensOn(ships2));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		scn.SetTwilight(10);
		scn.FreepsPass();

		var freepsTop = scn.GetFreepsTopOfDeck();
		var shadowTop = scn.GetShadowTopOfDeck();
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(ruffian));
		scn.ShadowPlayCard(ruffian);
		assertInZone(Zone.SHADOW_CHARACTERS, ruffian);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruffian));
		scn.ShadowAcceptOptionalTrigger();

		// The top card of the Free Peoples player's deck is discarded, and the Shadow player's own deck is left alone.
		assertInZone(Zone.DISCARD, freepsTop);
		assertEquals(freepsDeck - 1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertSame(shadowTop, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());

		// A card that already has a [raider] token: not one with none, and not one with a [men] token.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, ships1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ships2, fleet));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		scn.ShadowChooseCard(raiders);

		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertEquals(2, scn.GetCultureTokensOn(ships1));
		assertEquals(0, scn.GetCultureTokensOn(ships2));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheDrawDeckAndEveryTokenAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(ruffian);
		scn.MoveCardsToSupportArea(raiders);
		scn.AddTokensToCard(raiders, 1);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer);
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		scn.SetTwilight(10);
		scn.FreepsPass();

		var freepsTop = scn.GetFreepsTopOfDeck();
		int freepsDeck = scn.GetFreepsDeckCount();
		scn.ShadowPlayCard(ruffian);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruffian));
		scn.ShadowDeclineOptionalTrigger();

		assertSame(freepsTop, scn.GetFreepsTopOfDeck());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertInZone(Zone.SHADOW_CHARACTERS, ruffian);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedWithNoOtherCorsairToSpotNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var gunners = scn.GetShadowCard("gunners");
		var axeman = scn.GetShadowCard("axeman");
		var champion = scn.GetShadowCard("champion");
		var raiders = scn.GetShadowCard("raiders");
		// A corsair in hand is not spotted.
		scn.MoveCardsToHand(ruffian, gunners);
		scn.MoveCardsToSupportArea(raiders);
		scn.AddTokensToCard(raiders, 1);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		// A [raider] Man who is no corsair, and a Man whose title has the word and whose keywords have not.
		scn.MoveMinionsToTable(axeman, champion);
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		scn.SetTwilight(10);
		scn.FreepsPass();

		int freepsDeck = scn.GetFreepsDeckCount();
		scn.ShadowPlayCard(ruffian);

		assertInZone(Zone.SHADOW_CHARACTERS, ruffian);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(ruffian);
		scn.MoveCardsToSupportArea(raiders);
		scn.AddTokensToCard(raiders, 1);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.MoveMinionsToTable(buccaneer);
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsDeckCount());
		scn.ShadowPlayCard(ruffian);

		assertInZone(Zone.SHADOW_CHARACTERS, ruffian);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCardBearingARaiderTokenTheTopCardIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var ships2 = scn.GetShadowCard("ships2");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToHand(ruffian);
		scn.MoveCardsToSupportArea(ships2, fleet);
		// The only token on the table is a [men] one.
		scn.AddTokensToCard(fleet, 1);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer);
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		scn.SetTwilight(10);
		scn.FreepsPass();

		var freepsTop = scn.GetFreepsTopOfDeck();
		scn.ShadowPlayCard(ruffian);

		// The offer has a cost that can be paid, so it is made though it will add nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruffian));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, freepsTop);
		assertEquals(0, scn.GetCultureTokensOn(ships2));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondRuffianIsAnotherCorsairAndOnlyTheCopyPlayedMakesTheOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var ruffian2 = scn.GetShadowCard("ruffian2");
		var gunners = scn.GetShadowCard("gunners");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(ruffian2, gunners);
		scn.MoveCardsToSupportArea(raiders);
		scn.AddTokensToCard(raiders, 1);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian);
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		scn.SetTwilight(20);
		scn.FreepsPass();

		var freepsTop = scn.GetFreepsTopOfDeck();
		// The Ruffian in play is the only other corsair, and the offer is the played copy's alone.
		scn.ShadowPlayCard(ruffian2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruffian2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ruffian));
		scn.ShadowAcceptOptionalTrigger();

		// One card has a [raider] token, so it is not asked for.
		assertInZone(Zone.DISCARD, freepsTop);
		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another corsair played beside them is no play of either Ruffian.
		int freepsDeck = scn.GetFreepsDeckCount();
		scn.ShadowPlayCard(gunners);
		assertInZone(Zone.SHADOW_CHARACTERS, gunners);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
