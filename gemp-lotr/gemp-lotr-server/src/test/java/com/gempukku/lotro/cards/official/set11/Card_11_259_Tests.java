package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_259_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Household Guard: a [rohan] Man companion, vitality 3, valiant and no other text
					put("guard", "5_83");
					// Guma, Plains Farmer: a [rohan] Man ally, vitality 2.  His fellowship ability is not used, and his
					// home (4T) is on no path laid here.
					put("guma", "4_277");
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, and no Man while fewer than 3 [rohan]
					// companions (and no Eowyn) can be spotted.
					put("merry", "103_85");
					// Gondorian Knight: a [gondor] Man companion, a knight, and no other text
					put("knight", "5_35");
					// Rider's Mount: a [rohan] mount, cost 1.  Bearer must be a [rohan] Man.
					put("ridersmount", "4_287");
					// Horse of Rohan: a [rohan] mount, cost 2.  To play, spot a [rohan] Man.  Bearer must be a Man, Elf
					// or Wizard.
					put("horse", "4_283");
					// Sword of Rohan: a [rohan] hand weapon, cost 1.  Bearer must be a [rohan] Man.
					put("sword", "4_291");
					// Knight's Mount: a [gondor] mount, cost 1.  Bearer must be a knight.
					put("knightsmount", "8_40");
				}},
				new HashMap<>() {{
					put("stables", "11_259");
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void StablesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Stables
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: At the start of your fellowship phase, you may exert a [rohan] Man to play a [rohan] mount from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stables");

		assertEquals("Stables", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ARohanManExertsToPlayARohanMountFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, merry, knight);
		scn.MoveAlliesToTable(guma);

		var ridersmount = scn.GetFreepsCard("ridersmount");
		var horse = scn.GetFreepsCard("horse");
		var sword = scn.GetFreepsCard("sword");
		var knightsmount = scn.GetFreepsCard("knightsmount");

		var stables = scn.GetFreepsCard("stables");

		scn.StartGame(stables);

		assertSame(stables, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stables));
		assertInZone(Zone.DECK, ridersmount, horse, sword, knightsmount);

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();

		// A [rohan] Man companion and a [rohan] Man ally may exert.  Not a [rohan] Hobbit, a [gondor] Man, or the
		// Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(guma));
		scn.FreepsChooseCard(guma);

		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		// Merry's own text answers the exertion of another [rohan] Man: he may exert to heal Guma.  Declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsDismissRevealedCards();

		// Two [rohan] mounts may be played.  Not a [rohan] possession that is no mount, or a mount of another
		// culture that the Gondorian Knight could bear.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ridersmount, horse));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, knightsmount));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(ridersmount);

		// The Rider's Mount goes on a [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertAttachedTo(ridersmount, guard);
		assertInZone(Zone.DECK, horse, sword, knightsmount);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// The deck is shuffled as the card leaves it, and again when the look is over.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stables));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheOnlyRohanManExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, merry, knight);
		// The Household Guard is exhausted.  Frodo, Merry and the Knight could each exert, and none is a [rohan] Man.
		scn.AddWoundsToChar(guard, 2);

		var ridersmount = scn.GetFreepsCard("ridersmount");
		var stables = scn.GetFreepsCard("stables");

		scn.StartGame(stables);

		assertSame(stables, scn.GetCurrentSite());
		assertInZone(Zone.DECK, ridersmount);
		assertTrue(scn.IsExhausted(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningExertsNobodyAndTheOtherPlayerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToTopOfDeck(ridersmount);

		// The second player has a [rohan] Man of his own on the table, able to exert.
		var shadowGuard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(shadowGuard);

		var stables = scn.GetFreepsCard("stables");

		scn.StartGame(stables);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stables));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(shadowGuard));
		assertInZone(Zone.DECK, ridersmount);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRohanManExertsAndThePlayerMayStillPlayNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		scn.MoveCompanionsToTable(guard);

		var stables = scn.GetFreepsCard("stables");

		scn.StartGame(stables);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stables));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// The Household Guard is the only [rohan] Man: he exerts without a choice.
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(ridersmount));
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, ridersmount);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRohanMountInTheDrawDeckTheOfferIsStillMadeAndTheExertionBuysALookThatFindsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, knight);
		// Both [rohan] mounts are taken out of the deck.  The Sword of Rohan, the Knight's Mount, Guma and Merry
		// are left in it.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("ridersmount"), scn.GetFreepsCard("horse"));

		var stables = scn.GetFreepsCard("stables");

		scn.StartGame(stables);

		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stables));
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(knight));
		// The deck is shown, and nothing in it may be chosen.
		scn.FreepsDismissRevealedCards();

		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipIsAtTheStablesAndNotOnceItHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);

		var shadowGuard = scn.GetShadowCard("guard");
		var shadowMount = scn.GetShadowCard("ridersmount");
		scn.MoveCompanionsToTable(shadowGuard);

		var stables = scn.GetFreepsCard("stables");
		var ruins = scn.GetShadowSite("site5");     // Sirannon Ruins: a river, and no other text

		scn.StartGame(stables);

		// The first player's turn: he is offered and declines.  The second player, whose own Household Guard could
		// exert, is offered nothing on a turn that is not his.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stables));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(shadowGuard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// He moves on to site 2.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(2, scn.FreepsGetSiteNumber());

		// The second player's turn begins at site 1, which is the first player's card: the offer is the
		// second player's.  His own Household Guard exerts, and the Rider's Mount from his own draw deck goes on
		// him, his only [rohan] Man.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertSame(stables, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stables));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.MoveCardsToTopOfDeck(shadowMount);
		assertEquals(0, scn.GetWoundsOn(shadowGuard));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(shadowGuard));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guma));
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(shadowMount);
		assertAttachedTo(shadowMount, shadowGuard);

		// The first player's second turn begins at site 2, with the Household Guard and Guma able to exert.  The
		// Stables lie behind him, and nothing is offered.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(stables, scn.GetSite(1));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
