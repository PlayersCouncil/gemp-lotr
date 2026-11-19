package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_150_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("visitors1", "15_150");
					put("visitors2", "15_150");

					put("merry", "1_302");          // Merry, Friend to Sam: a [shire] Hobbit companion; a skirmish ability used only on purpose
					put("pippin", "103_31");        // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability used only on purpose
					// Followers.  Rohan Worker: no printed number; its texts answer a transfer to a companion and a
					// skirmish won.  The Gaffer: strength +1; heals a [shire] companion he is transferred to.
					// Elendil's Army: strength +2, no other text.  Daddy Twofoot: resistance +1; answers its own Aid.
					put("worker", "15_135");
					put("gaffer", "13_151");
					put("army", "18_47");
					put("twofoot", "13_144");
					put("sword", "1_299");          // Hobbit Sword: a hand weapon on a Hobbit, which is no follower; no other text

					put("runner1", "1_178");        // Goblin Runner: a [moria] Orc, strength 5; its when-played text is not reached (cheated in)
					put("runner2", "1_178");
					put("ted", "18_75");            // Ted Sandyman: a [men] minion who is a Hobbit, strength 3; abilities used only on purpose
					// Saruman, Servant of Sauron: a Shadow follower, whose Aid takes him to a minion; his
					// trigger answers a skirmish his bearer wins
					put("saruman", "17_38");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship block format, where a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("visitors1", "15_150");
					put("worker", "15_135");        // Rohan Worker: a follower with no printed number
					put("runner1", "1_178");        // Goblin Runner: a [moria] Orc, strength 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void NoVisitorsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: No Visitors
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Transfer a follower from a Hobbit to your support area to cancel a skirmish involving that Hobbit.
		*/

		var scn = GetScenario();

		var visitors = scn.GetFreepsCard("visitors1");

		assertEquals("No Visitors", visitors.getBlueprint().getTitle());
		assertNull(visitors.getBlueprint().getSubtitle());
		assertFalse(visitors.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, visitors.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, visitors.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, visitors.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(visitors, Timeword.SKIRMISH));
		assertEquals(2, visitors.getBlueprint().getTwilightCost());
	}

	@Test
	public void TransfersAFollowerFromAHobbitOfEitherCultureToTheSupportAreaToCancelHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var visitors1 = scn.GetFreepsCard("visitors1");
		var visitors2 = scn.GetFreepsCard("visitors2");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetFreepsCard("worker");
		var gaffer = scn.GetFreepsCard("gaffer");
		var army = scn.GetFreepsCard("army");
		var twofoot = scn.GetFreepsCard("twofoot");
		var sword = scn.GetFreepsCard("sword");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.AttachCardsTo(merry, worker);
		scn.AttachCardsTo(pippin, gaffer);
		scn.AttachCardsTo(aragorn, army);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(twofoot);
		scn.MoveCardsToHand(visitors1, visitors2);

		scn.StartGame();
		assertAttachedTo(worker, merry);
		assertAttachedTo(gaffer, pippin);
		assertAttachedTo(army, aragorn);
		assertAttachedTo(sword, frodo);
		assertInZone(Zone.SUPPORT, twofoot);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner1}, new PhysicalCardImpl[]{pippin, runner2});

		// A [shire] Hobbit's skirmish.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner1));
		assertTrue(scn.FreepsPlayAvailable(visitors1));
		scn.FreepsPlayCard(visitors1);
		// Followers on Hobbits of two cultures; not one on a Man, not a possession on a Hobbit, not one in the
		// support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, gaffer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(army, sword, twofoot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(worker);

		assertInZone(Zone.SUPPORT, worker);
		assertNull(worker.getAttachedTo());
		assertAttachedTo(gaffer, pippin);
		assertInDiscard(visitors1);
		assertFalse(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharSkirmishing(runner1));

		// A [gondor] Hobbit's skirmish: his follower is the one left on a Hobbit, and goes unasked.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runner2));
		scn.FreepsPlayCard(visitors2);
		assertInZone(Zone.SUPPORT, gaffer);
		assertInDiscard(visitors2);
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertFalse(scn.IsCharSkirmishing(runner2));

		// Nobody fought, and no skirmish is left.
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayersFollowerOnHisOwnHobbitCannotBeTransferredToYourSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var visitors1 = scn.GetFreepsCard("visitors1");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetFreepsCard("worker");
		var gaffer = scn.GetFreepsCard("gaffer");
		var ted = scn.GetShadowCard("ted");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.AttachCardsTo(merry, worker);
		scn.AttachCardsTo(pippin, gaffer);
		scn.MoveCardsToHand(visitors1);

		scn.StartGame();
		assertAttachedTo(worker, merry);
		assertAttachedTo(gaffer, pippin);
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, saruman);
		assertAttachedTo(saruman, ted);

		// The followers' Aids are offered as the maneuver phase starts, the Free Peoples ones and then Saruman's.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, ted);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn skirmishes the Shadow player's Hobbit, who bears the Shadow player's follower.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ted));
		scn.FreepsPlayCard(visitors1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, gaffer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman));
		scn.FreepsChooseCard(worker);

		assertInZone(Zone.SUPPORT, worker);
		assertAttachedTo(saruman, ted);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ted));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TakingTheFollowerOfAHobbitInAnotherSkirmishCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var visitors1 = scn.GetFreepsCard("visitors1");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var worker = scn.GetFreepsCard("worker");
		var gaffer = scn.GetFreepsCard("gaffer");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.AttachCardsTo(merry, worker);
		scn.AttachCardsTo(pippin, gaffer);
		scn.MoveCardsToHand(visitors1);

		scn.StartGame();
		assertAttachedTo(worker, merry);
		assertAttachedTo(gaffer, pippin);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner1}, new PhysicalCardImpl[]{pippin, runner2});

		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner1));
		scn.FreepsPlayCard(visitors1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, gaffer));
		scn.FreepsChooseCard(gaffer);

		// The cost is paid with Pippin's follower; Pippin's skirmish is not this one, and Merry's goes on.
		assertInZone(Zone.SUPPORT, gaffer);
		assertAttachedTo(worker, merry);
		assertInDiscard(visitors1);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner1));
		assertTrue(scn.IsCharAssignedAgainst(pippin, runner2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoFollowerOnAHobbitItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var visitors1 = scn.GetFreepsCard("visitors1");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var army = scn.GetFreepsCard("army");
		var twofoot = scn.GetFreepsCard("twofoot");
		var sword = scn.GetFreepsCard("sword");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, sword);
		scn.AttachCardsTo(aragorn, army);
		scn.MoveCardsToSupportArea(twofoot);
		scn.MoveCardsToHand(visitors1);

		scn.StartGame();
		assertAttachedTo(sword, merry);
		assertAttachedTo(army, aragorn);
		assertInZone(Zone.SUPPORT, twofoot);
		assertInHand(visitors1);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);

		// A follower on a Man, one in the support area, a possession on the Hobbit: no follower on a Hobbit.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner1));
		assertInHand(visitors1);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(visitors1));
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsTheRingBearersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var visitors1 = scn.GetFreepsCard("visitors1");
		var frodo = scn.GetRingBearer();
		var worker = scn.GetFreepsCard("worker");
		var runner1 = scn.GetShadowCard("runner1");
		scn.AttachCardsTo(frodo, worker);
		scn.MoveCardsToHand(visitors1);

		scn.StartGame();
		assertAttachedTo(worker, frodo);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner1));
		scn.FreepsPlayCard(visitors1);

		assertInZone(Zone.SUPPORT, worker);
		assertInDiscard(visitors1);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathTheRingBearersSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var visitors1 = scn.GetFreepsCard("visitors1");
		var frodo = scn.GetRingBearer();
		var worker = scn.GetFreepsCard("worker");
		var runner1 = scn.GetShadowCard("runner1");
		scn.AttachCardsTo(frodo, worker);
		scn.MoveCardsToHand(visitors1);

		scn.StartGame();
		assertAttachedTo(worker, frodo);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner1));
		assertTrue(scn.FreepsPlayAvailable(visitors1));
		scn.FreepsPlayCard(visitors1);

		// The cost is paid; the Ring-bearer's skirmish goes on.
		assertInZone(Zone.SUPPORT, worker);
		assertInDiscard(visitors1);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
