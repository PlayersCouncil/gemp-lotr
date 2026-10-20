package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watch1", "12_35");
					put("watch2", "12_35");
					put("watch3", "12_35");
					put("watch4", "12_35");

					// Gandalf, Friend of the Shirefolk: a [gandalf] companion; strength 6, +1 for each race he can
					// spot in the fellowship.
					put("gandalf", "1_72");
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion of another title and race, cost 4
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] companion, strength 8
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("erland", "2_21");        // Erland, Advisor to Brand: a [gandalf] ally; nothing that matters here
					// Stern Words: a [gandalf] condition that a companion bears; its texts act on a win and at the end of the turn.
					put("stern", "19_9");

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("pursuer", "4_190");      // Uruk Pursuer: strength 5, vitality 3, fierce; its text is about unbound Hobbits
					put("brute", "4_17");         // Dunlending Savage: strength 11, vitality 1 and no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WatchandWaitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Watch and Wait
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Game Text: To play, spot a [gandalf] companion. Bearer must be a companion (except the Ring-bearer). Limit 1
		 * 	per bearer.
		 * 	Each time bearer wins a skirmish, you may remove a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watch1");

		assertEquals("Watch and Wait", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertFalse(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NeedsAGandalfCompanionAndGoesOnACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var watch = scn.GetFreepsCard("watch1");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var erland = scn.GetFreepsCard("erland");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(erland);
		scn.MoveCardsToHand(watch, avenger);

		scn.StartGame();

		// A [gondor] companion and a [gandalf] ally: no [gandalf] companion to spot.
		assertFalse(scn.FreepsPlayAvailable(watch));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(avenger);
		assertTrue(scn.FreepsPlayAvailable(watch));

		scn.FreepsPlayCard(watch);
		// Companions of three cultures, the Ring-bound Sam among them; not the Ring-bearer, and not the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger, aragorn, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, erland));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(watch, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionMayBearOnlyOneCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var watch1 = scn.GetFreepsCard("watch1");
		var watch2 = scn.GetFreepsCard("watch2");
		var watch3 = scn.GetFreepsCard("watch3");
		var watch4 = scn.GetFreepsCard("watch4");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var stern = scn.GetFreepsCard("stern");
		scn.MoveCompanionsToTable(gandalf, aragorn, sam);
		scn.AttachCardsTo(aragorn, watch1);
		// Another [gandalf] condition does not count against the limit.
		scn.AttachCardsTo(gandalf, stern);
		scn.MoveCardsToHand(watch2, watch3, watch4);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(watch2));
		scn.FreepsPlayCard(watch2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(watch2, gandalf);

		// Sam is the one companion left who may bear one, so nobody is asked.
		scn.FreepsPlayCard(watch3);
		assertAttachedTo(watch3, sam);

		// Every companion who may bear one bears one, and Gandalf is still there to spot.
		assertFalse(scn.FreepsPlayAvailable(watch4));
		assertInZone(Zone.HAND, watch4);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishABurdenMayBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watch1 = scn.GetFreepsCard("watch1");
		var watch2 = scn.GetFreepsCard("watch2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(aragorn, watch1);
		// A second copy on a companion who does not skirmish.
		scn.AttachCardsTo(gandalf, watch2);

		scn.StartGame();
		scn.AddBurdens(2);
		// A fierce minion that loses and lives gives Aragorn two skirmishes to win in one turn.
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, pursuer);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(2, scn.GetBurdens());
		scn.PassSkirmishActions();

		// 8 against 5: he wins.  The offer is his copy's, not Gandalf's.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(watch1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(watch2));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, pursuer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		// He wins again, and the offer is made again; declined, the burden stays.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(watch1));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenACompanionWhoIsNotTheBearerWinsNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watch = scn.GetFreepsCard("watch1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(aragorn, watch);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(2, scn.GetBurdens());
		scn.PassSkirmishActions();

		// Gandalf, who bears nothing, wins: 6 + 1 (Frodo's Hobbit) + 1 (Aragorn's Man) against 5.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheBearerLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watch = scn.GetFreepsCard("watch1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brute = scn.GetShadowCard("brute");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, watch);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(brute);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brute);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// 8 against 11: the bearer loses, and takes one wound from a minion with no damage bonus.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(brute));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoBurdenToRemoveNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watch = scn.GetFreepsCard("watch1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, watch);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
