package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Where The One Ring is when its bearer is killed.
 *
 * Comprehensive Rules 4.2, "leaving play": "When a card (other than a site) leaves play for any reason, any cards
 * played on that card (or borne by or stacked on that card) are discarded."  The Ring is the one card that is not:
 * Ketura, 2026-10-07, on Q-0109: "The Ring-bearer and One Ring should never be discarded for any reason."  And on
 * Q-0232 (2026-10-10), of the Ring that used to go to the discard pile with a dead Ring-bearer: "If the Ring-bearer
 * is killed, let's ensure the One Ring is moved to the same position the Ring-bearer previously occupied,
 * including in skirmishes (as the skirmish UI will very frequently leave a finished game in the state of the last
 * skirmish with a blank spot where the One Ring used to be).  ...  When Frodo dies and Sam is in play with his
 * ability, there should thus be a brief moment (until the response is accepted by the player) where the Ring is
 * where Frodo once was, before Sam's ability kicks in and transfers, with gameplay resuming as normal."
 *
 * So the Ring stays in play: it lies in the zone its bearer was in, borne by nobody, and each client is told it
 * has moved there.  If he was killed as the fellowship character of a skirmish, each client is also told the Ring
 * is in that skirmish, so that it is drawn in his place; for the game it fights nobody there, and the skirmish has
 * no fellowship character.  A new Ring-bearer takes it up from where it lies; if there is none, the game ends
 * with it there.  What a client is told is read here as each seat was sent it and as a client joining the seat
 * afterwards is sent it (FreepsSeesCardInZone, FreepsSeesCardBorneBy, FreepsSeesCardInSkirmish and their twins).
 */
public class TheOneRingWhenItsBearerIsKilledTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: strength 3.  Response: If Frodo is killed, make Sam the Ring-bearer (resistance 5).
					put("sam", "1_311");
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text that acts here
					put("sword", "1_299");      // Hobbit Sword: bearer must be a Hobbit; strength +2
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, no text
					put("troop2", "1_177");
					put("marksman", "1_176");   // Goblin Marksman: archer, and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, vitality 4
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	@Test
	public void KilledInASkirmishTheRingLiesInItsBearersPlaceThereAndTheGameEndsWithItThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop1");
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		// The Ring on its bearer, as each client has it: borne by Frodo, and no part of the skirmish he is in.
		assertAttachedTo(ring, frodo);
		assertTrue(scn.FreepsSeesCardBorneBy(ring, frodo));
		assertTrue(scn.ShadowSeesCardBorneBy(ring, frodo));
		assertTrue(scn.FreepsSeesCardInSkirmish(frodo));
		assertTrue(scn.ShadowSeesCardInSkirmish(frodo));
		assertFalse(scn.FreepsSeesCardInSkirmish(ring));
		assertFalse(scn.ShadowSeesCardInSkirmish(ring));
		assertEquals(0, scn.GetFreepsDiscardCount());

		// 3 + 2 for the sword + 1 for the Ring against 13: Frodo is overwhelmed and killed.
		scn.MarkGameLog();
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, frodo);
		// What he bore is discarded with him.  The Ring is not: it is in play, where he was, borne by nobody.
		assertInDiscard(sword);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.FREE_CHARACTERS, ring);
		assertNotAttachedTo(ring, frodo);
		assertTrue(scn.GetAttachedCards(frodo).isEmpty());

		// Each client is told as much: the Ring among the Free Peoples characters, on no card.
		assertTrue(scn.FreepsSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.ShadowSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.FreepsSeesCardBorneBy(ring, null));
		assertTrue(scn.ShadowSeesCardBorneBy(ring, null));
		assertTrue(scn.FreepsSeesCardInZone(frodo, Zone.DEAD));
		assertTrue(scn.ShadowSeesCardInZone(frodo, Zone.DEAD));

		// And in his place in the skirmish, which the clients still have before them with the minion in it.
		assertTrue(scn.FreepsSeesCardInSkirmish(ring));
		assertTrue(scn.ShadowSeesCardInSkirmish(ring));
		assertFalse(scn.FreepsSeesCardInSkirmish(frodo));
		assertFalse(scn.ShadowSeesCardInSkirmish(frodo));
		assertTrue(scn.FreepsSeesCardInSkirmish(troop));
		assertTrue(scn.ShadowSeesCardInSkirmish(troop));
		// For the game the Ring fights nobody: the minion is in a skirmish with no fellowship character.
		assertFalse(scn.IsCharSkirmishing(ring));
		assertTrue(scn.IsCharSkirmishing(troop));

		// Nobody takes it up, so the game is over, with the Ring lying there.
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.GameLogSinceMarkSays("lost due to: The Ring-Bearer is dead"));
	}

	@Test
	public void KilledOutsideASkirmishTheRingLiesAmongTheCompanionsAndInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);

		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertAttachedTo(ring, frodo);
		assertFalse(scn.GameIsFinished());

		// One archery wound, and Frodo the only one to take it: his fourth.
		scn.MarkGameLog();
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, ring);
		assertNotAttachedTo(ring, frodo);
		assertEquals(0, scn.GetFreepsDiscardCount());

		assertTrue(scn.FreepsSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.ShadowSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.FreepsSeesCardBorneBy(ring, null));
		assertTrue(scn.ShadowSeesCardBorneBy(ring, null));
		// He was in no skirmish, so the Ring is in none.
		assertFalse(scn.FreepsSeesCardInSkirmish(ring));
		assertFalse(scn.ShadowSeesCardInSkirmish(ring));
		assertFalse(scn.IsCharSkirmishing(ring));

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.GameLogSinceMarkSays("lost due to: The Ring-Bearer is dead"));
	}

	@Test
	public void KilledDuringAnothersSkirmishTheRingIsInNeitherThatSkirmishNorTheOneHeWasAssignedTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var guard = scn.GetFreepsCard("guard");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.AddThreats(1);

		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop1}, new PhysicalCardImpl[]{frodo, troop2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.IsCharAssignedAgainst(frodo, troop2));
		assertTrue(scn.IsCharSkirmishingAgainst(guard, troop1));

		// 4 against 13: the Dwarf Guard is overwhelmed and killed, and the threat becomes a wound.  Frodo is the
		// only companion left to take it.  The Ruling Ring offers to turn it into a burden (the wound comes in a
		// skirmish phase); declined, the wound is his fourth.
		scn.MarkGameLog();
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, ring);
		assertTrue(scn.FreepsSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.ShadowSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.FreepsSeesCardBorneBy(ring, null));
		assertTrue(scn.ShadowSeesCardBorneBy(ring, null));

		// The skirmish on the table was the Dwarf Guard's, not Frodo's: the Ring is not put in it.
		assertTrue(scn.FreepsSeesCardInSkirmish(troop1));
		assertFalse(scn.FreepsSeesCardInSkirmish(ring));
		assertFalse(scn.ShadowSeesCardInSkirmish(ring));
		// The skirmish Frodo was assigned to and never fought is gone with him, and the Ring has no part in it.
		assertFalse(scn.IsCharAssigned(troop2));
		assertFalse(scn.IsCharAssigned(ring));

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.GameLogSinceMarkSays("lost due to: The Ring-Bearer is dead"));
	}

	@Test
	public void WhileSamsResponseIsOnOfferTheRingLiesWhereFrodoWasAndSamTakesItUpFromThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertAttachedTo(ring, frodo);
		assertEquals(3, scn.GetStrength(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed and killed, and Sam's response is offered.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		// The moment before it is answered: the game goes on, and the Ring lies where Frodo was, in play and in
		// his place in the skirmish, in no discard pile and on nobody.
		assertFalse(scn.GameIsFinished());
		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, ring);
		assertNotAttachedTo(ring, sam);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.ShadowSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.FreepsSeesCardBorneBy(ring, null));
		assertTrue(scn.ShadowSeesCardBorneBy(ring, null));
		assertTrue(scn.FreepsSeesCardInSkirmish(ring));
		assertTrue(scn.ShadowSeesCardInSkirmish(ring));
		assertFalse(scn.IsCharSkirmishing(ring));
		// It is on nobody, so it strengthens nobody.
		assertEquals(3, scn.GetStrength(sam));

		scn.FreepsAcceptOptionalTrigger();

		// Sam has taken it up: he bears it, each client is told so, and it is out of the skirmish's place.
		assertSame(sam, scn.GetRingBearer());
		assertAttachedTo(ring, sam);
		assertTrue(scn.FreepsSeesCardInZone(ring, Zone.ATTACHED));
		assertTrue(scn.ShadowSeesCardInZone(ring, Zone.ATTACHED));
		assertTrue(scn.FreepsSeesCardBorneBy(ring, sam));
		assertTrue(scn.ShadowSeesCardBorneBy(ring, sam));
		assertFalse(scn.FreepsSeesCardInSkirmish(ring));
		assertFalse(scn.ShadowSeesCardInSkirmish(ring));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(4, scn.GetStrength(sam));

		// And play resumes: the skirmish is over and the regroup phase begins.
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenSamsResponseIsDeclinedTheGameEndsWithTheRingStillLyingThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.GameIsFinished());
		assertInZone(Zone.FREE_CHARACTERS, ring);

		scn.MarkGameLog();
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.GameLogSinceMarkSays("lost due to: The Ring-Bearer is dead"));
		assertSame(frodo, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, sam);

		// The finished game shows the Ring where Frodo fell: among the companions, on nobody, in the skirmish.
		assertInZone(Zone.FREE_CHARACTERS, ring);
		assertNotAttachedTo(ring, sam);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.ShadowSeesCardInZone(ring, Zone.FREE_CHARACTERS));
		assertTrue(scn.FreepsSeesCardBorneBy(ring, null));
		assertTrue(scn.ShadowSeesCardBorneBy(ring, null));
		assertTrue(scn.FreepsSeesCardInSkirmish(ring));
		assertTrue(scn.ShadowSeesCardInSkirmish(ring));
		assertTrue(scn.FreepsSeesCardInSkirmish(troop));
		assertTrue(scn.ShadowSeesCardInSkirmish(troop));
	}
}
