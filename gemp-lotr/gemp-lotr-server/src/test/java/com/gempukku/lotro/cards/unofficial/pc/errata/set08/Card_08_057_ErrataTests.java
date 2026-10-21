package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_057_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marauder", "58_57");
					put("marauder2", "58_57");

					put("plunderer", "8_58");   // Corsair Plunderer: corsair, cost 2; when played, may discard 2 cards from hand to add tokens
					put("lookout", "8_56");     // Corsair Lookout: corsair, cost 1; when played, with initiative, may discard an ally to add tokens
					put("southron", "4_254");   // Southron Soldier: [raider] Man, not a corsair; Southron and ambush only

					put("sails", "8_50");       // Black Sails of Umbar: [raider] possession in the support area
					put("sails2", "8_50");
					put("ballista", "8_52");    // Corsair Ballista: [raider] possession borne by a corsair; archer and fierce with initiative
					put("galley", "8_59");      // Corsair War Galley: [raider] possession in the support area that holds [raider] tokens
					put("raiders", "4_242");    // Raiders From the East: [raider] condition in the support area that holds [raider] tokens

					put("cart", "1_73");        // Gandalf's Cart: Free Peoples possession in the support area
					put("moria", "1_21");       // Lord of Moria: Free Peoples condition in the support area; each Dwarf is damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
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
		 * Game Text: Corsair.
		 * When you play this minion, if you can spot another corsair, you may choose 1: discard a Shadow possession
		 * to reinforce a [raider] token twice; or remove 2 [raider] tokens to discard a possession.
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
	public void PlaysForItsCostPlusRoamingAndWithoutAnotherCorsairInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var lookout = scn.GetShadowCard("lookout");
		var southron = scn.GetShadowCard("southron");
		var sails = scn.GetShadowCard("sails");
		var galley = scn.GetShadowCard("galley");
		var cart = scn.GetFreepsCard("cart");
		// A corsair in hand, not in play.
		scn.MoveCardsToHand(marauder, lookout);
		// Both choices could be paid for: a Shadow possession, and 2 [raider] tokens.
		scn.MoveCardsToSupportArea(sails, galley, cart);

		scn.StartGame();
		// A [raider] Man in play who is not a corsair.
		scn.MoveMinionsToTable(southron);
		scn.AddTokensToCard(galley, 2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 + 2 for site 2 + 1 for the one companion.
		assertEquals(13, scn.GetTwilight());
		scn.ShadowPlayCard(marauder);

		// 4 to play + 2 roaming.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder));
		assertInZone(Zone.SUPPORT, sails, cart);
		assertEquals(2, scn.GetCultureTokensOn(galley));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingToDiscardAShadowPossessionReinforcesOneRaiderTokenTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var plunderer = scn.GetShadowCard("plunderer");
		var sails = scn.GetShadowCard("sails");
		var ballista = scn.GetShadowCard("ballista");
		var galley = scn.GetShadowCard("galley");
		var raiders = scn.GetShadowCard("raiders");
		var cart = scn.GetFreepsCard("cart");
		scn.MoveCardsToHand(marauder);
		scn.MoveCardsToSupportArea(sails, galley, raiders, cart);

		scn.StartGame();
		scn.MoveMinionsToTable(plunderer);
		scn.AttachCardsTo(plunderer, ballista);
		scn.AddTokensToCard(galley, 1);
		scn.AddTokensToCard(raiders, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marauder));
		scn.ShadowChooseOptionalTrigger(marauder);

		// Both can be paid for, so both are offered.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Discard a Shadow possession");

		// Shadow possessions in the support area and borne by a minion; not a Free Peoples possession, nor a Shadow condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sails, ballista, galley));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cart, raiders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ballista);
		assertInDiscard(ballista);

		// Cards bearing a [raider] token; Black Sails has none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(galley, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sails));
		scn.ShadowChooseCard(raiders);

		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetCultureTokensOn(galley));
		assertEquals(0, scn.GetCultureTokensOn(sails));
		assertInZone(Zone.SUPPORT, sails, galley, cart);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingToRemoveTwoRaiderTokensDiscardsAPossessionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var plunderer = scn.GetShadowCard("plunderer");
		var sails = scn.GetShadowCard("sails");
		var galley = scn.GetShadowCard("galley");
		var raiders = scn.GetShadowCard("raiders");
		var cart = scn.GetFreepsCard("cart");
		var moria = scn.GetFreepsCard("moria");
		scn.MoveCardsToHand(marauder);
		scn.MoveCardsToSupportArea(sails, galley, raiders, cart, moria);

		scn.StartGame();
		scn.MoveMinionsToTable(plunderer);
		// One [raider] token on each of two cards.
		scn.AddTokensToCard(galley, 1);
		scn.AddTokensToCard(raiders, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);
		scn.ShadowChooseOptionalTrigger(marauder);
		scn.ShadowChooseOption("Remove 2");

		// The tokens are taken one at a time, from any card holding one: here one from each.
		assertTrue(scn.ShadowHasCardChoiceAvailable(galley, raiders));
		scn.ShadowChooseCard(galley);
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(0, scn.GetCultureTokensOn(raiders));

		// A possession of either side; not a condition of either side.
		assertTrue(scn.ShadowHasCardChoiceAvailable(cart, sails, galley));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(moria, raiders));
		scn.ShadowChooseCard(cart);

		assertInDiscard(cart);
		assertInZone(Zone.SUPPORT, sails, galley, raiders, moria);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneRaiderTokenOnlyTheShadowPossessionCanBeDiscardedAndIsWithoutASecondQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var lookout = scn.GetShadowCard("lookout");
		var sails = scn.GetShadowCard("sails");
		var sails2 = scn.GetShadowCard("sails2");
		var cart = scn.GetFreepsCard("cart");
		scn.MoveCardsToHand(marauder);
		scn.MoveCardsToSupportArea(sails, sails2, cart);

		scn.StartGame();
		scn.MoveMinionsToTable(lookout);
		scn.AddTokensToCard(sails, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);
		scn.ShadowChooseOptionalTrigger(marauder);

		// Straight to the possession to discard.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sails, sails2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cart));
		scn.ShadowChooseCard(sails2);

		// The one card with a [raider] token is reinforced without being chosen.
		assertInDiscard(sails2);
		assertEquals(3, scn.GetCultureTokensOn(sails));
		assertInZone(Zone.SUPPORT, cart);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoShadowPossessionOnlyTheTokensCanBeRemovedAndAreWithoutASecondQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var lookout = scn.GetShadowCard("lookout");
		var raiders = scn.GetShadowCard("raiders");
		var cart = scn.GetFreepsCard("cart");
		scn.MoveCardsToHand(marauder);
		scn.MoveCardsToSupportArea(raiders, cart);

		scn.StartGame();
		scn.MoveMinionsToTable(lookout);
		scn.AddTokensToCard(raiders, 2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);
		scn.ShadowChooseOptionalTrigger(marauder);

		// Cart is the one possession, so it is discarded without being chosen.
		assertEquals(0, scn.GetCultureTokensOn(raiders));
		assertInDiscard(cart);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNeitherChoicePayableNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var lookout = scn.GetShadowCard("lookout");
		var raiders = scn.GetShadowCard("raiders");
		var cart = scn.GetFreepsCard("cart");
		scn.MoveCardsToHand(marauder);
		scn.MoveCardsToSupportArea(raiders, cart);

		scn.StartGame();
		scn.MoveMinionsToTable(lookout);
		// One [raider] token, and no Shadow possession.
		scn.AddTokensToCard(raiders, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauder);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder));
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertInZone(Zone.SUPPORT, cart);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondMarauderIsAnotherCorsairAndOnlyThePlayedCopyAsksAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var marauder2 = scn.GetShadowCard("marauder2");
		var plunderer = scn.GetShadowCard("plunderer");
		var sails = scn.GetShadowCard("sails");
		var galley = scn.GetShadowCard("galley");
		var cart = scn.GetFreepsCard("cart");
		scn.MoveCardsToHand(plunderer, marauder2);
		scn.MoveCardsToSupportArea(sails, galley, cart);

		scn.StartGame();
		scn.MoveMinionsToTable(marauder);
		scn.AddTokensToCard(galley, 2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Another corsair played: the Marauder in play does not answer it.
		scn.ShadowPlayCard(plunderer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(marauder2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marauder2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marauder));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, sails, galley, cart);
		assertEquals(2, scn.GetCultureTokensOn(galley));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
