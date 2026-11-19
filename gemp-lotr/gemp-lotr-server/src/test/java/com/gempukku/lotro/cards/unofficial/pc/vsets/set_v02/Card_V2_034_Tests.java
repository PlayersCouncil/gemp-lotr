package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_034_Tests
{
	// A Towers path on which sites 2, 4, 5 and 7 are battlegrounds and sites 1, 3 and 6 are not.  A site is
	// taken control of lowest first, and only from below every player's marker, so three battlegrounds are
	// controlled once sites 1 to 5 are, with the fellowship at site 6 or beyond.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("occupier", "102_34");
					put("occupier2", "102_34");

					put("savage", "1_151");      // Uruk Savage: an [isengard] Uruk-hai, cost 2, strength 5, site 5, damage +1
					// Down to the Last Child: "Shadow: Remove 2 [isengard] tokens from this card to take control
					// of a site."  Its other text needs a companion to lose to an Uruk-hai.
					put("lastchild", "4_148");
					// Trail of Savagery: "Each site you control gains battleground."  Its skirmish ability needs
					// an Uruk-hai in a skirmish.
					put("trail", "102_31");
					put("axestrike", "1_3");     // Axe Strike: a Free Peoples skirmish event, something to hold in hand
					// New Authority: "Response: If a minion uses its special ability, discard this condition to
					// cancel the effect of that special ability."  Its other text answers a burden added.
					put("authority", "11_37");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");       // Eastemnet Gullies
					// Uruk Camp: battleground.  "Each time an Uruk-hai is played, that minion must exert", which
					// is why nothing here is played while the fellowship is at site 2.
					put("site2", "4_335");
					put("site3", "4_337");       // Barrows of Edoras: sanctuary, shadow number 0
					put("site4", "4_346");       // White Rocks: battleground
					put("site5", "4_348");       // Deeping Wall: battleground
					put("site6", "4_352");       // Caves of Aglarond
					put("site7", "102_71");      // Helm's Tower: battleground, shadow number 6
					put("site8", "4_358");       // Ring of Isengard
					put("site9", "4_363");       // Palantir Chamber
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukOccupierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Uruk Occupier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Damage +1.
		 * This minion is strength +1 for each battleground you control.
		 * Shadow: If you control 3 battlegrounds, discard a card from hand to play this minion from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("occupier");

		assertEquals("Uruk Occupier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedForThreeAndTwoMoreForRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var occupier = scn.GetShadowCard("occupier");

		scn.StartGame();
		scn.SkipToSite(2);

		scn.MoveCardsToHand(occupier);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// 4 set + 1 for Frodo + 0 for the site: exactly the 3 to play and the 2 for roaming at site 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(occupier));

		scn.ShadowPlayCard(occupier);

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, occupier);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(8, scn.GetStrength(occupier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus1ForEachBattlegroundItsOwnerControlsAndNotForAnyOtherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var occupier = scn.GetShadowCard("occupier");
		var occupier2 = scn.GetShadowCard("occupier2");
		var savage = scn.GetShadowCard("savage");
		var lastChild = scn.GetShadowCard("lastchild");
		var trail = scn.GetShadowCard("trail");

		scn.StartGame();
		scn.SkipToSite(6);

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		var site5 = scn.GetSite(5);
		scn.MoveMinionsToTable(occupier, occupier2, savage);
		scn.MoveCardsToSupportArea(lastChild);
		// Enough to take control of five sites, one at a time and lowest first.
		scn.AddTokensToCard(lastChild, 10);
		scn.FreepsPass();

		// The fellowship is at Helm's Tower, a battleground nobody controls.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(8, scn.GetStrength(occupier));

		scn.ShadowUseCardAction(lastChild);
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertEquals(8, scn.GetStrength(occupier));

		scn.ShadowUseCardAction(lastChild);
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertEquals(9, scn.GetStrength(occupier));

		scn.ShadowUseCardAction(lastChild);
		assertTrue(scn.ShadowControls(site3));
		assertFalse(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertEquals(9, scn.GetStrength(occupier));

		scn.ShadowUseCardAction(lastChild);
		assertTrue(scn.ShadowControls(site4));
		assertTrue(scn.HasKeyword(site4, Keyword.BATTLEGROUND));
		assertEquals(10, scn.GetStrength(occupier));

		scn.ShadowUseCardAction(lastChild);
		assertTrue(scn.ShadowControls(site5));
		assertTrue(scn.HasKeyword(site5, Keyword.BATTLEGROUND));
		// 8 + 3, with five sites controlled
		assertEquals(5, scn.GetShadowControlledSiteCount());
		assertEquals(11, scn.GetStrength(occupier));
		// "This minion": each copy has its own +3, and another [isengard] Uruk-hai has nothing.
		assertEquals(11, scn.GetStrength(occupier2));
		assertEquals(5, scn.GetStrength(savage));

		// A site that is a battleground only by another card's doing counts as well: with Trail of Savagery
		// all five are battlegrounds.
		scn.MoveCardsToSupportArea(trail);
		assertTrue(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		// 8 + 5
		assertEquals(13, scn.GetStrength(occupier));
		assertEquals(13, scn.GetStrength(occupier2));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BattlegroundsTheOtherPlayerControlsCountForNeitherText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theirOccupier = scn.GetFreepsCard("occupier");
		var theirOccupier2 = scn.GetFreepsCard("occupier2");
		var theirAxeStrike = scn.GetFreepsCard("axestrike");

		scn.StartGame();
		scn.SkipToSite(6);

		var site2 = scn.GetSite(2);
		var site4 = scn.GetSite(4);
		var site5 = scn.GetSite(5);
		// Player 2, the Shadow player this turn, controls sites 1 to 5: three battlegrounds.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site4));
		assertTrue(scn.ShadowControls(site5));
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(site4, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(site5, Keyword.BATTLEGROUND));

		// On player 2's own turn player 1 is the Shadow player.  One Occupier of player 1's is in his discard
		// pile, with a card in his hand to discard for it.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToDiscard(theirOccupier2);
		scn.MoveCardsToHand(theirAxeStrike);
		scn.ShadowPass();

		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertTrue(scn.ShadowControls(site5));
		assertFalse(scn.FreepsControls(site5));
		assertEquals(5, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		// 1 for player 2's Frodo + 6 for Helm's Tower: more than the Occupier's 3, with no roaming at site 7
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.HAND, theirAxeStrike);
		assertInZone(Zone.DISCARD, theirOccupier2);
		assertFalse(scn.FreepsActionAvailable(theirOccupier2));

		// And an Occupier of player 1's on the table is as printed.
		scn.MoveMinionsToTable(theirOccupier);
		assertInZone(Zone.SHADOW_CHARACTERS, theirOccupier);
		assertEquals(8, scn.GetStrength(theirOccupier));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void ShadowActionIsOfferedOnceAThirdBattlegroundIsControlledAndDiscardsACardFromHandToPlayThatCopyFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var occupier = scn.GetShadowCard("occupier");
		var occupier2 = scn.GetShadowCard("occupier2");
		var savage = scn.GetShadowCard("savage");
		var lastChild = scn.GetShadowCard("lastchild");
		var axeStrike = scn.GetShadowCard("axestrike");

		scn.StartGame();
		scn.SkipToSite(6);

		var site4 = scn.GetSite(4);
		var site5 = scn.GetSite(5);
		// Every card of the Shadow player's is put where it is wanted: the turns skipped have filled his hand.
		scn.MoveCardsToDiscard(occupier, occupier2);
		scn.MoveCardsToSupportArea(lastChild);
		// Enough to take control of one site by legal play, which makes the game look again at what is on offer.
		scn.AddTokensToCard(lastChild, 2);
		// A Shadow card and a Free Peoples card in hand.
		scn.MoveCardsToHand(savage, axeStrike);
		scn.MoveCardsToBottomOfDeck(scn.GetShadowCard("trail"), scn.GetShadowCard("authority"));
		// Sites 1 to 4 are his before the Shadow phase begins.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsPass();

		// 1 for Frodo + 6 for Helm's Tower
		assertEquals(7, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());

		// Four sites controlled, two of them battlegrounds.
		assertEquals(4, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowControls(site4));
		assertFalse(scn.IsSiteControlled(site5));
		assertFalse(scn.ShadowActionAvailable(occupier));
		assertFalse(scn.ShadowActionAvailable(occupier2));

		scn.ShadowUseCardAction(lastChild);
		// The fifth is Deeping Wall, the third battleground.
		assertTrue(scn.ShadowControls(site5));
		assertTrue(scn.HasKeyword(site5, Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowActionAvailable(occupier));
		assertTrue(scn.ShadowActionAvailable(occupier2));

		scn.ShadowUseCardAction(occupier);

		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, axeStrike));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(axeStrike);

		assertInZone(Zone.DISCARD, axeStrike);
		assertInZone(Zone.HAND, savage);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, occupier);
		// 3 to play, and no roaming at site 7
		assertEquals(4, scn.GetTwilight());
		// 8 + 3
		assertEquals(11, scn.GetStrength(occupier));
		// The other copy stays where it was, and still makes its offer; the copy in play makes none.
		assertInZone(Zone.DISCARD, occupier2);
		assertTrue(scn.ShadowActionAvailable(occupier2));
		assertFalse(scn.ShadowActionAvailable(occupier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceTheHandIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var occupier = scn.GetShadowCard("occupier");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToDiscard(occupier);
		scn.MoveCardsToHand(savage);
		scn.MoveCardsToBottomOfDeck(scn.GetShadowCard("occupier2"), scn.GetShadowCard("axestrike"),
				scn.GetShadowCard("trail"), scn.GetShadowCard("authority"), scn.GetShadowCard("lastchild"));
		// Sites 1 to 5: three battlegrounds.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsPass();

		assertEquals(7, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(occupier));

		// The one card in hand is played.
		scn.ShadowPlayCard(savage);

		assertEquals(0, scn.GetShadowHandCount());
		// 7 - 2 for the Uruk Savage: still more than the Occupier's 3
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, occupier);
		assertFalse(scn.ShadowActionAvailable(occupier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenThePoolIsOneShortOfItsCostAndIsWhenThePoolIsExactlyEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var occupier = scn.GetShadowCard("occupier");
		var savage = scn.GetShadowCard("savage");
		var lastChild = scn.GetShadowCard("lastchild");
		var axeStrike = scn.GetShadowCard("axestrike");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToDiscard(occupier);
		scn.MoveCardsToSupportArea(lastChild);
		// Enough to take control of one site by legal play, which costs nothing from the pool and makes the
		// game look again at what is on offer.
		scn.AddTokensToCard(lastChild, 2);
		scn.MoveCardsToHand(savage, axeStrike);
		scn.MoveCardsToBottomOfDeck(scn.GetShadowCard("occupier2"), scn.GetShadowCard("trail"), scn.GetShadowCard("authority"));
		// Sites 1 to 4 are his before the Shadow phase begins.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsPass();

		scn.SetTwilight(2);
		scn.ShadowUseCardAction(lastChild);

		// Three battlegrounds and two cards in hand, and a pool of 2 where the Occupier costs 3.
		assertEquals(5, scn.GetShadowControlledSiteCount());
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(occupier));

		// The pool is made 5 and the Uruk Savage played for 2, so that the game looks again with 3 in it.
		scn.SetTwilight(5);
		scn.ShadowPlayCard(savage);

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(occupier));

		// One card in hand: nothing to choose.
		scn.ShadowUseCardAction(occupier);

		assertInZone(Zone.DISCARD, axeStrike);
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, occupier);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStillOfferedWithAFourthBattlegroundControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var occupier = scn.GetShadowCard("occupier");
		var savage = scn.GetShadowCard("savage");
		var axeStrike = scn.GetShadowCard("axestrike");

		scn.StartGame();
		// Helm's Tower, the fourth battleground, is site 7: it can be controlled once both markers are beyond
		// it, which is with both fellowships at site 8.
		scn.SkipToSite(8);

		var site5 = scn.GetSite(5);
		var site7 = scn.GetSite(7);
		scn.MoveCardsToDiscard(occupier);
		scn.MoveCardsToHand(savage, axeStrike);
		scn.MoveCardsToBottomOfDeck(scn.GetShadowCard("occupier2"), scn.GetShadowCard("trail"), scn.GetShadowCard("authority"),
				scn.GetShadowCard("lastchild"));
		// Sites 1 to 5: three battlegrounds.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site5));
		assertFalse(scn.IsSiteControlled(site7));
		// Sites 6 and 7 as well: four battlegrounds, and who controls 4 controls 3.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsPass();

		// 1 for Frodo + 9 for Palantir Chamber
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetTwilight());

		assertTrue(scn.ShadowControls(site7));
		assertTrue(scn.HasKeyword(site7, Keyword.BATTLEGROUND));
		assertEquals(7, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowActionAvailable(occupier));

		scn.ShadowUseCardAction(occupier);
		scn.ShadowChooseCard(axeStrike);

		assertInZone(Zone.DISCARD, axeStrike);
		assertInZone(Zone.HAND, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, occupier);
		// 3 to play, and no roaming at site 9
		assertEquals(7, scn.GetTwilight());
		// 8 + 4
		assertEquals(12, scn.GetStrength(occupier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void UsedFromTheDiscardPileItIsAMinionsSpecialAbilityAndNewAuthorityCancelsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var occupier = scn.GetShadowCard("occupier");
		var savage = scn.GetShadowCard("savage");
		var lastChild = scn.GetShadowCard("lastchild");
		var axeStrike = scn.GetShadowCard("axestrike");
		var authority = scn.GetFreepsCard("authority");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCardsToSupportArea(authority);
		scn.MoveCardsToDiscard(occupier);
		scn.MoveCardsToSupportArea(lastChild);
		// Enough to take control of one site by its own ability, which is a special ability and not a minion's.
		scn.AddTokensToCard(lastChild, 2);
		scn.MoveCardsToHand(savage, axeStrike);
		scn.MoveCardsToBottomOfDeck(scn.GetShadowCard("occupier2"), scn.GetShadowCard("trail"), scn.GetShadowCard("authority"));
		// Sites 1 to 4 are his before the Shadow phase begins, and the fifth is taken by Down to the Last Child.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsPass();

		scn.ShadowUseCardAction(lastChild);

		assertEquals(5, scn.GetShadowControlledSiteCount());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(occupier));
		// Taking control of a site is a condition's ability and was not answered.
		assertInZone(Zone.SUPPORT, authority);

		scn.ShadowUseCardAction(occupier);
		scn.ShadowChooseCard(axeStrike);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(authority));

		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, authority);
		// The effect is cancelled: the Occupier is not played and nothing is paid for it.
		assertInZone(Zone.DISCARD, occupier);
		assertEquals(7, scn.GetTwilight());
		// The cost stands.
		assertInZone(Zone.DISCARD, axeStrike);
		assertEquals(1, scn.GetShadowHandCount());
		// With a card still in hand the ability may be used again.
		assertTrue(scn.ShadowActionAvailable(occupier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
