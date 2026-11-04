package com.gempukku.lotro.mechanics.sites;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card that is discarded for where the fellowship is, on the move that takes the fellowship there.
 *
 * Comprehensive Rules 4.2, "moving your fellowship", lists the steps of a move: the next site is placed if
 * needed; "When you move from..."; "When the fellowship moves..."; the player marker is moved; "When you move
 * to..."; then twilight tokens are added for the new site's Shadow number, for the region and for each
 * companion.  And: "After you have completed any moving actions and have added twilight tokens to the twilight
 * pool, the fellowship has moved to the new site and the game text there can be used".
 *
 * Decipher's ruling on Bill the Pony (3_106: "The Shadow number of each site is -1.  Discard Bill the Pony when
 * at an underground site."), which Ketura quoted on Q-0133, 2026-10-08: "When the fellowship moves with Bill the
 * Pony to an underground site, the Shadow number of that site is reduced before Bill the Pony is discarded."
 * Ketura's answer to the question itself was that the discard is "a continuous effect, same as the Watcher": a state
 * the card is in, not something that happens once on arriving.
 *
 * So the game looks at such a check all the time, except between the moving of the marker and the adding of the
 * move's twilight.  In between, the card is still in play and its text still counts; what answers the move ("when
 * the fellowship moves to") reads the new site as before.  When the twilight has been added the card goes, with
 * nothing asked of anyone first.  The checks this reaches are the ones a card makes on the site the fellowship
 * is at: "discard ... when at an underground site" (Bill the Pony, Asfaloth, Black Steed), "if underground"
 * (Goblin Man, Bill Ferny), "if not underground" (The Balrog), "if not at a marsh" (the tentacles and the
 * Watcher in the Water).  Other standing checks (the Watcher's "discard all other minions") do not wait.
 */
public class AtASiteWhenTheMoveIsFinishedTests
{
	private VirtualTableScenario GetScenario(String site4) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bill", "3_106");       // Bill the Pony: bearer must be a Hobbit companion
					put("sam", "1_311");        // Sam, Son of Hamfast: a Fellowship ability used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: a Skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion; a Skirmish ability only
					// The Long Dark: "When the fellowship moves to site 4 or 5, add (2) for each Dwarf companion."
					put("longdark", "1_188");
					// Goblin Man: "Discard this minion if underground."  Strength 6, vitality 2; a Shadow ability.
					put("goblinman", "2_42");
				}},
				new HashMap<>() {{
					put("site1", "1_319");  // Bag End, 0
					put("site2", "1_331");  // Ettenmoors, 2
					put("site3", "1_341");  // Rivendell Valley, 0; a sanctuary
					put("site4", site4);
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Balin's Tomb: underground, Shadow number 3.  Its text is a Maneuver ability.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario("1_343");
	}

	// Dwarrowdelf Chamber: underground, Shadow number 2.  "When the fellowship moves to Dwarrowdelf Chamber, Gimli
	// or 2 other companions must exert."
	protected VirtualTableScenario GetChamberScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario("1_344");
	}

	@Test
	public void TheShadowNumberOfTheSiteMovedToIsCountedWhileTheCardDiscardedThereIsStillInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(sam, bill);

		int pool = scn.GetTwilight();
		scn.FreepsPass();

		// 3 - 1 for Balin's Tomb, and 2 for Frodo and Sam.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(pool + 2 + 2, scn.GetTwilight());
		// And Bill is gone before anybody is asked anything at the new site, which reads 3 again.
		assertInDiscard(bill);
		assertEquals(0, scn.GetAttachedCards(sam).size());
		assertEquals(3, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheCardIsStillInPlayWhileWhatAnswersTheMoveIsCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChamberScenario();

		var bill = scn.GetFreepsCard("bill");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(sam, bill);

		int pool = scn.GetTwilight();
		scn.FreepsPass();

		// The marker is on Dwarrowdelf Chamber and the site's own "when the fellowship moves to" is being carried
		// out: with no Gimli, two of the three companions must exert.  No twilight has been added yet, and Bill
		// is where he was.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(pool, scn.GetTwilight());
		assertAttachedTo(bill, sam);

		scn.FreepsChooseCards(frodo, merry);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		// 2 - 1 for Dwarrowdelf Chamber, and 3 for the companions.
		assertEquals(pool + 1 + 3, scn.GetTwilight());
		assertInDiscard(bill);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwilightAddedByACardThatAnswersTheMoveDoesNotEndTheWait() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var longdark = scn.GetShadowCard("longdark");
		scn.MoveCompanionsToTable(sam, gimli);
		scn.MoveCardsToSupportArea(longdark);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(sam, bill);

		int pool = scn.GetTwilight();
		scn.FreepsPass();

		// The Long Dark adds 2 for Gimli as the fellowship moves to site 4, before the move's own twilight: 3 - 1
		// for Balin's Tomb still, and 3 for the companions.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(pool + 2 + 2 + 3, scn.GetTwilight());
		assertInDiscard(bill);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionDiscardedForWhereTheFellowshipIsGoesWhenAMoveInTheRegroupPhaseIsFinished() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblinman = scn.GetShadowCard("goblinman");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(goblinman);

		// Rivendell Valley is not underground: the Goblin Man stays through the turn's phases.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman);

		scn.SkipToMovementDecision();
		int pool = scn.GetTwilight();
		scn.FreepsChooseToMove();

		// Balin's Tomb is underground.  Nothing of the Goblin Man's bears on the twilight: 3 for the site and 1
		// for Frodo.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(pool + 3 + 1, scn.GetTwilight());
		assertInDiscard(goblinman);
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OutsideAMoveTheCheckDoesNotWait() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(bill);
		scn.FreepsRefreshDecision();

		// The fellowship is at Balin's Tomb and nothing is moving: Bill is discarded as he comes into play.
		assertTrue(scn.FreepsPlayAvailable(bill));
		scn.FreepsPlayCard(bill);
		scn.FreepsChooseCard(sam); // Frodo is a Hobbit companion too

		assertInDiscard(bill);
		assertEquals(0, scn.GetAttachedCards(sam).size());
		assertEquals(3, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
