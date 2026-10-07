package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_050_Tests
{

	// The Shadow phase of every test is at site 2 of the Fellowship path, shadow number 2, where a minion of
	// site number 4 pays 2 more for roaming.  With Frodo alone the pool there is 3 plus whatever was set.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sails1", "8_50");
					put("sails2", "8_50");
					// Corsair Buccaneer: a corsair, cost 3, site number 4.  When played, may remove a threat to add
					// tokens: with no threat to remove it asks nothing.
					put("buccaneer", "8_53");
					// Corsair Gunners: a corsair, cost 5, site number 4.  A skirmish ability and nothing on being played.
					put("gunners", "8_55");
					// Corsair Plunderer: a corsair, cost 2.  Only ever in hand here.
					put("plunderer", "8_58");
					// Easterling Axeman: a [raider] minion that is no corsair, cost 2.  Its text needs 2 burdens.
					put("axeman", "4_224");
					// Corsair War Galley: a [raider] possession with Corsair in its title, and no corsair.  Cost 1.
					put("galley", "8_59");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackSailsofUmbarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Black Sails of Umbar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: When you play this possession, you may add a [raider] token here.<br><b>Shadow:</b> Remove X [raider] tokens here to play a corsair from your discard pile; its twilight cost is -X. Discard this possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sails1");

		assertEquals("Black Sails of Umbar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForTwoToTheSupportAreaAndATokenMayBeAddedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inPlay = scn.GetShadowCard("sails1");
		var played = scn.GetShadowCard("sails2");
		scn.MoveCardsToSupportArea(inPlay);
		scn.MoveCardsToHand(played);

		scn.StartGame();
		scn.FreepsPass();

		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(played));

		scn.ShadowPlayCard(played);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, played);
		// The copy that was played makes the offer; the copy already in play does not.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(played));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(inPlay));
		assertEquals(0, scn.GetCultureTokensOn(played));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(played));
		assertEquals(0, scn.GetCultureTokensOn(inPlay));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheTokenMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sails = scn.GetShadowCard("sails1");
		scn.MoveCardsToHand(sails);

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowPlayCard(sails);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sails));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetCultureTokensOn(sails));
		assertInZone(Zone.SUPPORT, sails);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovingTwoTokensHerePlaysACorsairFromTheDiscardPileForTwoLessAndDiscardsThisPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var used = scn.GetShadowCard("sails1");
		var other = scn.GetShadowCard("sails2");
		var buccaneer = scn.GetShadowCard("buccaneer");
		scn.MoveCardsToSupportArea(used, other);
		scn.MoveCardsToDiscard(buccaneer);

		scn.StartGame();
		scn.AddTokensToCard(used, 3);
		scn.AddTokensToCard(other, 4);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for the site
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(used));

		scn.ShadowUseCardAction(used);

		// The pool pays for the Buccaneer in full, so X may be 0; and no more than the 3 tokens here, whatever
		// the other copy holds.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertInZone(Zone.SHADOW_CHARACTERS, buccaneer);
		// 3 - 2 for the tokens removed + 2 for roaming
		assertEquals(7, scn.GetTwilight());
		assertInDiscard(used);
		assertInZone(Zone.SUPPORT, other);
		assertEquals(4, scn.GetCultureTokensOn(other));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithXOfZeroTheCorsairIsPlayedAtFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sails = scn.GetShadowCard("sails1");
		var buccaneer = scn.GetShadowCard("buccaneer");
		scn.MoveCardsToSupportArea(sails);
		scn.MoveCardsToDiscard(buccaneer);

		scn.StartGame();
		scn.AddTokensToCard(sails, 3);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		scn.ShadowUseCardAction(sails);
		scn.ShadowChoose("0");

		assertInZone(Zone.SHADOW_CHARACTERS, buccaneer);
		// 3 + 2 for roaming
		assertEquals(5, scn.GetTwilight());
		assertInDiscard(sails);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnXGreaterThanTheCorsairsCostMakesItCostNothingAndRoamingIsStillPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sails = scn.GetShadowCard("sails1");
		var buccaneer = scn.GetShadowCard("buccaneer");
		scn.MoveCardsToSupportArea(sails);
		scn.MoveCardsToDiscard(buccaneer);

		scn.StartGame();
		scn.AddTokensToCard(sails, 5);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		scn.ShadowUseCardAction(sails);
		assertEquals(5, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("5");

		assertInZone(Zone.SHADOW_CHARACTERS, buccaneer);
		// 3 - 5 is no less than 0; + 2 for roaming
		assertEquals(8, scn.GetTwilight());
		assertInDiscard(sails);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EitherCorsairInTheDiscardPileMayBePlayedAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sails = scn.GetShadowCard("sails1");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var gunners = scn.GetShadowCard("gunners");
		var plunderer = scn.GetShadowCard("plunderer");
		var axeman = scn.GetShadowCard("axeman");
		var galley = scn.GetShadowCard("galley");
		scn.MoveCardsToSupportArea(sails);
		scn.MoveCardsToDiscard(buccaneer, gunners, axeman, galley);
		scn.MoveCardsToHand(plunderer);

		scn.StartGame();
		scn.AddTokensToCard(sails, 1);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// The pool pays for anything here: the Plunderer and the War Galley could be played from hand or
		// discard pile if the text reached them.
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(plunderer));

		scn.ShadowUseCardAction(sails);
		scn.ShadowChoose("1");

		// A corsair, of either cost, in the discard pile.  Not a [raider] minion that is no corsair, not a
		// possession with Corsair in its title, not a corsair in hand.
		assertTrue(scn.ShadowHasCardChoiceAvailable(buccaneer, gunners));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, galley, plunderer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(gunners);

		assertInZone(Zone.SHADOW_CHARACTERS, gunners);
		// 5 - 1 for the token removed + 2 for roaming
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(buccaneer, axeman, galley);
		assertInZone(Zone.HAND, plunderer);
		assertInDiscard(sails);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithACorsairInHandAndOnlyOtherRaiderCardsInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sails = scn.GetShadowCard("sails1");
		var plunderer = scn.GetShadowCard("plunderer");
		var axeman = scn.GetShadowCard("axeman");
		var galley = scn.GetShadowCard("galley");
		scn.MoveCardsToSupportArea(sails);
		scn.MoveCardsToDiscard(axeman, galley);
		scn.MoveCardsToHand(plunderer);

		scn.StartGame();
		scn.AddTokensToCard(sails, 3);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The pool is enough: the Plunderer can be played from hand.
		assertTrue(scn.ShadowPlayAvailable(plunderer));
		assertFalse(scn.ShadowActionAvailable(sails));
	}

	@Test
	public void TheSmallestXThatMayBeNamedIsOneThatPaysForACorsair() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sails = scn.GetShadowCard("sails1");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var gunners = scn.GetShadowCard("gunners");
		scn.MoveCardsToSupportArea(sails);
		scn.MoveCardsToDiscard(buccaneer, gunners);

		scn.StartGame();
		scn.AddTokensToCard(sails, 5);
		scn.FreepsPass();

		// 1 companion + 2 for the site.  The Buccaneer costs 3 + 2 for roaming, 2 more than the pool; the
		// Gunners cost 5 + 2 for roaming, 4 more.  So X has to be at least 2, what brings a corsair within reach.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(sails));

		scn.ShadowUseCardAction(sails);

		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// At X = 2 the Buccaneer is the only corsair that can be paid for, and is played without a choice.
		assertInZone(Zone.SHADOW_CHARACTERS, buccaneer);
		assertInDiscard(gunners);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(sails);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedOnlyWhereTheTokensHereBringACorsairWithinThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oneToken = scn.GetShadowCard("sails1");
		var twoTokens = scn.GetShadowCard("sails2");
		var buccaneer = scn.GetShadowCard("buccaneer");
		scn.MoveCardsToSupportArea(oneToken, twoTokens);
		scn.MoveCardsToDiscard(buccaneer);

		scn.StartGame();
		scn.AddTokensToCard(oneToken, 1);
		scn.AddTokensToCard(twoTokens, 2);
		scn.FreepsPass();

		// 1 companion + 2 for the site.  The Buccaneer costs 3 + 2 for roaming: with every token of the first
		// copy removed it would still cost 4, and with both tokens of the second, 3.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(oneToken));
		assertTrue(scn.ShadowActionAvailable(twoTokens));

		scn.ShadowUseCardAction(twoTokens);

		// Both tokens are needed, so 2 is the only X there is, and the game takes it without asking.

		assertInZone(Zone.SHADOW_CHARACTERS, buccaneer);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(twoTokens);
		assertInZone(Zone.SUPPORT, oneToken);
		assertEquals(1, scn.GetCultureTokensOn(oneToken));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
