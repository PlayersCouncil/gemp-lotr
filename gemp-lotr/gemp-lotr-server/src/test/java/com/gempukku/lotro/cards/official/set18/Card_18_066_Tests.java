package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fleet1", "18_66");
					put("fleet2", "18_66");

					put("halberd", "18_63");    // Corsair Halberd: a [men] possession played on a [men] minion; no play trigger
					put("corps", "11_73");      // Corps of Harad: [men] Man minion; fierce only at a battleground (none on this path)
					put("stragglers", "13_99"); // Stragglers: a [men] condition; lies in a discard pile here
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] minion with no text; lies in a discard pile here
					put("howdah", "13_94");     // Howdah: a [men] condition; lies in the Free Peoples player's discard pile here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRedhornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fleet1", "18_66");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Redhorn Pass: a Shadows-block mountain, laid by MoveCardToAdventurePath as site 2; "Culture tokens
					// cannot be added, removed, or reinforced."
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FleetofCorsairShipsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Fleet of Corsair Ships
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: When you play this possession, you may add a [men] token here.
		 * 	<b>Regroup:</b> Remove 4 [men] tokens from here to place a [men] card from your discard pile on the top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fleet1");

		assertEquals("Fleet of Corsair Ships", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedItMayAddAMenTokenToItselfAndNotToAnotherCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fleet1 = scn.GetShadowCard("fleet1");
		var fleet2 = scn.GetShadowCard("fleet2");

		scn.StartGame();
		scn.MoveCardsToSupportArea(fleet2);
		scn.MoveCardsToHand(fleet1);
		scn.SetTwilight(5);
		assertInZone(Zone.HAND, fleet1);
		assertEquals(0, scn.GetCultureTokensOn(fleet2));
		scn.FreepsPass();

		scn.ShadowPlayCard(fleet1);
		assertInZone(Zone.SUPPORT, fleet1);
		// The offer is the copy played's alone.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fleet1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fleet2));
		scn.ShadowChooseOptionalTrigger(fleet1);

		assertEquals(1, scn.GetCultureTokensOn(fleet1));
		assertEquals(0, scn.GetCultureTokensOn(fleet2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesItWithNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fleet1 = scn.GetShadowCard("fleet1");

		scn.StartGame();
		scn.MoveCardsToHand(fleet1);
		scn.SetTwilight(5);
		assertInZone(Zone.HAND, fleet1);
		scn.FreepsPass();

		scn.ShadowPlayCard(fleet1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fleet1));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, fleet1);
		assertEquals(0, scn.GetCultureTokensOn(fleet1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMenPossessionPlayedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fleet1 = scn.GetShadowCard("fleet1");
		var halberd = scn.GetShadowCard("halberd");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.MoveCardsToSupportArea(fleet1);
		scn.MoveMinionsToTable(corps);
		scn.MoveCardsToHand(halberd);
		scn.SetTwilight(5);
		assertInZone(Zone.HAND, halberd);
		assertEquals(0, scn.GetCultureTokensOn(fleet1));
		scn.FreepsPass();

		scn.ShadowPlayCard(halberd);
		assertAttachedTo(halberd, corps);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fleet1));
		assertEquals(0, scn.GetCultureTokensOn(fleet1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhereNoTokenCanBeAddedPlayingItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornScenario();

		var fleet1 = scn.GetShadowCard("fleet1");
		var redhorn = scn.GetShadowSite("redhorn");

		scn.StartGame();
		scn.MoveCardToAdventurePath(redhorn);
		scn.MoveCardsToHand(fleet1);
		scn.SetTwilight(5);
		assertInZone(Zone.HAND, fleet1);
		scn.FreepsPass();

		assertSame(redhorn, scn.GetCurrentSite());
		scn.ShadowPlayCard(fleet1);
		assertInZone(Zone.SUPPORT, fleet1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fleet1));
		assertEquals(0, scn.GetCultureTokensOn(fleet1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityRemovesFourTokensHereToPlaceAMenCardFromTheDiscardPileOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetFreepsCard("howdah");

		var fleet1 = scn.GetShadowCard("fleet1");
		var fleet2 = scn.GetShadowCard("fleet2");
		var corps = scn.GetShadowCard("corps");
		var stragglers = scn.GetShadowCard("stragglers");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveCardsToSupportArea(fleet1, fleet2);
		scn.AddTokensToCard(fleet1, 7);
		scn.AddTokensToCard(fleet2, 4);
		scn.MoveCardsToDiscard(corps, stragglers, brigand, howdah);
		assertInZone(Zone.DISCARD, corps, stragglers, brigand, howdah);
		assertEquals(7, scn.GetCultureTokensOn(fleet1));
		assertEquals(4, scn.GetCultureTokensOn(fleet2));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(7, scn.GetCultureTokensOn(fleet1));
		assertEquals(4, scn.GetCultureTokensOn(fleet2));
		assertNotSame(corps, scn.GetShadowTopOfDeck());
		assertTrue(scn.ShadowActionAvailable(fleet1));

		scn.ShadowUseCardAction(fleet1);

		// 4 tokens from the copy used, none from the other.
		assertEquals(3, scn.GetCultureTokensOn(fleet1));
		assertEquals(4, scn.GetCultureTokensOn(fleet2));

		// A [men] card of either type in his own discard pile; not the card of another culture, and not the
		// [men] card in the other player's pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, stragglers));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, howdah));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(corps);

		assertSame(corps, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DISCARD, stragglers, brigand, howdah);

		// The copy used has 3 tokens left and cannot pay again; the other copy, with 4, can.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fleet1));
		assertTrue(scn.ShadowActionAvailable(fleet2));
	}

	@Test
	public void WithNoMenCardInTheDiscardPileTheRegroupAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fleet1 = scn.GetShadowCard("fleet1");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveCardsToSupportArea(fleet1);
		scn.AddTokensToCard(fleet1, 4);
		scn.MoveCardsToDiscard(brigand);
		assertInZone(Zone.DISCARD, brigand);
		assertEquals(4, scn.GetCultureTokensOn(fleet1));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(4, scn.GetCultureTokensOn(fleet1));
		var top = scn.GetShadowTopOfDeck();
		assertTrue(scn.ShadowActionAvailable(fleet1));

		scn.ShadowUseCardAction(fleet1);

		assertEquals(0, scn.GetCultureTokensOn(fleet1));
		assertInZone(Zone.DISCARD, brigand);
		assertSame(top, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
