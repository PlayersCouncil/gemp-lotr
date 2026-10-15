package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_339_Tests
{

	// The Towers path with the Stables as site 3.  Site 2 is Eastfold (a Shadow ability that needs 3 [isengard]
	// trackers) and site 4 White Rocks (Shadow number +1 for each mounted companion).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Household Guard: a [rohan] Man companion, vitality 3, valiant and no other text
					put("guard", "5_83");
					// Guma, Plains Farmer: a [rohan] Man ally, vitality 2.  His fellowship ability is not used, and nobody
					// controls his home, 4T.
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
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site3", "4_339");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers-block path with this site as site 3.
	protected VirtualTableScenario GetSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: a [rohan] Man, vitality 3
					put("mount", "4_287");      // Rider's Mount: a [rohan] mount, cost 1; bearer must be a [rohan] Man
					put("mount2", "4_287");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_339");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StablesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Stables
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 3T
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Exert a [rohan] Man to play a [rohan] mount from your draw
		 * deck (limit once per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Stables", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
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

		scn.StartGame();
		scn.SkipToSite(3);
		// The turns played on the way drew the deck into hand.
		scn.MoveCardsToTopOfDeck(ridersmount, horse, sword, knightsmount);

		var stables = scn.GetCurrentSite();
		assertEquals("Stables", stables.getBlueprint().getTitle());
		assertInZone(Zone.DECK, ridersmount, horse, sword, knightsmount);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(stables));

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsUseCardAction(stables);

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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheOnlyRohanManExhaustedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, merry, knight);

		var ridersmount = scn.GetFreepsCard("ridersmount");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToTopOfDeck(ridersmount);
		// The Household Guard is exhausted.  Frodo, Merry and the Knight could each exert, and none is a [rohan] Man.
		scn.AddWoundsToChar(guard, 2);
		scn.FreepsRefreshDecision();

		var stables = scn.GetCurrentSite();
		assertEquals("Stables", stables.getBlueprint().getTitle());
		assertInZone(Zone.DECK, ridersmount);
		assertTrue(scn.IsExhausted(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(stables));
	}

	@Test
	public void TheRohanManExertsAndThePlayerMayStillPlayNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToTopOfDeck(ridersmount);

		var stables = scn.GetCurrentSite();
		assertEquals("Stables", stables.getBlueprint().getTitle());
		assertInZone(Zone.DECK, ridersmount);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// The Household Guard is the only [rohan] Man: he exerts without a choice.
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsUseCardAction(stables);
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(ridersmount));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, ridersmount);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRohanMountInTheDrawDeckTheExertionBuysALookThatFindsNothingAndSpendsTheTurnsUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, knight);
		var ridersmount = scn.GetFreepsCard("ridersmount");
		var sword = scn.GetFreepsCard("sword");
		var knightsmount = scn.GetFreepsCard("knightsmount");

		scn.StartGame();
		scn.SkipToSite(3);
		// No [rohan] mount in the deck: the Sword of Rohan and the Knight's Mount are there.
		scn.MoveCardsToDiscard(ridersmount, scn.GetFreepsCard("horse"));
		scn.MoveCardsToTopOfDeck(sword, knightsmount);

		var stables = scn.GetCurrentSite();
		assertEquals("Stables", stables.getBlueprint().getTitle());
		assertInZone(Zone.DECK, sword, knightsmount);
		assertTrue(scn.FreepsActionAvailable(stables));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsUseCardAction(stables);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(knight));
		// The deck is shown, and nothing in it may be chosen.
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, sword, knightsmount);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The look was this turn's use: with a [rohan] mount now on the deck and the Guard able to exert again, the
		// ability is not offered.
		scn.MoveCardsToTopOfDeck(ridersmount);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, ridersmount);
		assertFalse(scn.IsExhausted(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(stables));
	}

	@Test
	public void FellowshipAbilityIsNotOfferedAgainInTheTurnItWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetSiteScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(eomer);

		scn.StartGame();
		scn.SkipToSite(3);
		// The turns played on the way drew the deck into hand.
		scn.MoveCardsToTopOfDeck(mount, mount2);

		var site = scn.GetCurrentSite();
		assertEquals("Stables", site.getBlueprint().getTitle());

		assertTrue(scn.FreepsActionAvailable(site));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetAttachedCards(eomer).size());

		scn.FreepsUseCardAction(site);
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(mount);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(1, scn.GetAttachedCards(eomer).size());

		// Limit once per turn: a limit on uses, so the ability is not offered again, though Eomer could still
		// exert and the draw deck still holds a mount (Ketura on Q-0093).
		assertFalse(scn.FreepsActionAvailable(site));
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
