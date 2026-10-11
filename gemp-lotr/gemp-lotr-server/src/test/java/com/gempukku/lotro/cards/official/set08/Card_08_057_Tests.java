package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_057_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("marauder", "8_57");
					put("marauder2", "8_57");

					put("buccaneer", "8_53");   // Corsair Buccaneer: a corsair whose own text acts only as it is played
					put("gunners", "8_55");     // Corsair Gunners: a corsair, cost 5, with nothing on being played
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no corsair
					put("champion", "13_84");   // Corsair Champion: a [men] Man, "Corsair" in his title only

					put("raiders", "4_242");    // Raiders From the East: [raider] condition that holds [raider] tokens
					put("ships1", "8_65");      // Ships of Great Draught: [raider] possession in the support area
					put("ships2", "8_65");      //   that holds [raider] tokens
					put("fleet", "18_66");      // Fleet of Corsair Ships: [men] possession that holds [men] tokens

					put("gimli", "1_13");
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession, borne
					put("ring", "9_6");         // Ring of Accretion: a Free Peoples artifact, an item that is no possession
				}}
		);
	}

	@Test
	public void CorsairMarauderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair Marauder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br>When you play this minion, if you can spot another corsair, you may discard a possession to add 2 [raider] tokens to a card that already has a [raider] token on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marauder");

		assertEquals("Corsair Marauder", card.getBlueprint().getTitle());
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
	public void PlayedBesideAnotherCorsairMayDiscardAPossessionToAddTwoRaiderTokensToACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var raiders = scn.GetShadowCard("raiders");
		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToHand(marauder);
		scn.MoveCardsToSupportArea(raiders, ships1, ships2, fleet);
		scn.AddTokensToCard(raiders, 1);
		scn.AddTokensToCard(ships1, 2);
		// A [men] token: a token, but not a [raider] one.
		scn.AddTokensToCard(fleet, 1);

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor, ring);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(marauder));
		scn.ShadowPlayCard(marauder);

		// 4 to play + 2 roaming (site number 4 at site 2)
		assertEquals(pool - 6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marauder));
		scn.ShadowAcceptOptionalTrigger();

		// Any possession will do, whoever's it is and wherever it lies; an artifact and a condition will not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, ships1, ships2, fleet));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, raiders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(armor);

		assertInZone(Zone.DISCARD, armor);
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, ships1, ships2, fleet);

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
	public void DecliningLeavesEveryPossessionAndEveryTokenAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var raiders = scn.GetShadowCard("raiders");
		var ships1 = scn.GetShadowCard("ships1");
		scn.MoveCardsToHand(marauder);
		scn.MoveCardsToSupportArea(raiders, ships1);
		scn.AddTokensToCard(raiders, 1);
		scn.AddTokensToCard(ships1, 2);

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marauder));
		scn.ShadowDeclineOptionalTrigger();

		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, ships1);
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(2, scn.GetCultureTokensOn(ships1));
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedWithNoOtherCorsairToSpotNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var gunners = scn.GetShadowCard("gunners");
		var axeman = scn.GetShadowCard("axeman");
		var champion = scn.GetShadowCard("champion");
		var raiders = scn.GetShadowCard("raiders");
		var ships1 = scn.GetShadowCard("ships1");
		// A corsair in hand is not spotted.
		scn.MoveCardsToHand(marauder, gunners);
		scn.MoveCardsToSupportArea(raiders, ships1);
		scn.AddTokensToCard(raiders, 1);

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		// A [raider] Man who is no corsair, and a Man whose title has the word and whose keywords have not.
		scn.MoveMinionsToTable(axeman, champion);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(marauder);

		// 4 to play + 2 roaming
		assertEquals(pool - 6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, ships1);
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoPossessionToDiscardNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(marauder);
		// The only cards in play that are not characters or the Ring: a condition and an artifact.
		scn.MoveCardsToSupportArea(raiders);
		scn.AddTokensToCard(raiders, 1);

		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, ring);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);

		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, raiders);
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCardBearingARaiderTokenThePossessionIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var ships2 = scn.GetShadowCard("ships2");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToHand(marauder);
		scn.MoveCardsToSupportArea(ships2, fleet);
		// The only token on the table is a [men] one.
		scn.AddTokensToCard(fleet, 1);

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);

		// The offer has a cost that can be paid, so it is made though it will add nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marauder));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, ships2, fleet));
		scn.ShadowChooseCard(armor);

		assertInZone(Zone.DISCARD, armor);
		assertInZone(Zone.SUPPORT, ships2, fleet);
		assertEquals(0, scn.GetCultureTokensOn(ships2));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondMarauderIsAnotherCorsairAndOnlyTheCopyPlayedMakesTheOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var marauder2 = scn.GetShadowCard("marauder2");
		var gunners = scn.GetShadowCard("gunners");
		var raiders = scn.GetShadowCard("raiders");
		var ships2 = scn.GetShadowCard("ships2");
		scn.MoveCardsToHand(marauder2, gunners);
		scn.MoveCardsToSupportArea(raiders, ships2);
		scn.AddTokensToCard(raiders, 1);

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(marauder);
		scn.SetTwilight(15);
		scn.FreepsPass();

		// The Marauder in play is the only other corsair, and the offer is the played copy's alone.
		scn.ShadowPlayCard(marauder2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marauder2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, ships2));
		scn.ShadowChooseCard(armor);

		// One card has a [raider] token, so it is not asked for.
		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertInZone(Zone.DISCARD, armor);
		assertInZone(Zone.SUPPORT, ships2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another corsair played beside them is no play of either Marauder.
		scn.ShadowPlayCard(gunners);
		assertInZone(Zone.SHADOW_CHARACTERS, gunners);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, ships2);
		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
