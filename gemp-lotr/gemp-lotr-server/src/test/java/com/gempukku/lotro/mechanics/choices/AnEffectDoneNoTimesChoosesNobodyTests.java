package com.gempukku.lotro.mechanics.choices;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * "Exert (wound, heal) a <kind> X times" as an effect, with X come to 0: nobody is chosen and nothing is asked
 * (Q-0337: B, settled on Ketura's F-0116, "the prompt should be smarter").  With X of 1 the choice is asked as
 * before.  The same for a choice of cards from one's own hand whose count has come to 0: "must discard X cards
 * from hand" asked a selection of 0 to 0 (F-0560); now it asks nothing.  Each pair is shown on the one printed card
 * that reaches it with two candidates on the table: They Will Never Stop Hunting You 3_84 (exert), Reckless We
 * Rode 8_12 (wound), Hobbit Appetite 1_294 (heal), The Dark Lord Advances 3_87 (discard from hand).
 *
 * An exertion, wound or heal paid as a COST is asked as before; no printed cost of that shape can come to 0, so
 * that side is shown by no card.
 */
public class AnEffectDoneNoTimesChoosesNobodyTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// They Will Never Stop Hunting You: "Maneuver: Spot a Nazgûl to reveal a card at random from the
					// Free Peoples player's hand. Exert a companion bearing a ranged weapon X times, where X is the
					// twilight cost of the card revealed."
					put("hunting", "3_84");
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl; weakens only bearers of a [wraith] condition
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; an ability used only on purpose
					put("aragornsbow", "1_90"); // Aragorn's Bow: a ranged weapon
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion, vitality 3
					put("bow", "1_41");         // Elven Bow: a ranged weapon; bearer is an archer
					put("wreathed", "1_238");   // Wreathed in Shadow: twilight cost 0
					put("runner", "1_178");     // Goblin Runner: twilight cost 1

					// Reckless We Rode: "Archery: Exert an Elf companion and make the fellowship archery total -X
					// (to a minimum of 0) to wound a minion X times."
					put("reckless", "8_12");
					put("troop1", "1_177");     // Goblin Patrol Troop: an Orc of vitality 3 and no text
					put("troop2", "1_177");

					// Hobbit Appetite: "Fellowship: Add (X) to heal a Hobbit companion X times."
					put("appetite", "1_294");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion; his ability is never used

					// The Dark Lord Advances: "Regroup: Exert a [sauron] minion to reveal a card at random from the
					// Free Peoples player's hand. That player must discard X cards from hand, where X is the
					// twilight cost of the card revealed."
					put("advances", "3_87");
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc; his text answers only a weapon played on him
					put("strike", "1_3");       // Axe Strike: twilight cost 0
					put("fury", "1_4");         // Battle Fury: twilight cost 0
					put("blow", "1_5");         // Cleaving Blow: twilight cost 1
					put("guard", "1_7");        // Dwarf Guard: twilight cost 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ExertingNoTimesAsksNobodyToBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var wreathed = scn.GetFreepsCard("wreathed");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornsbow"));
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("bow"));
		scn.MoveCardsToHand(hunting, wreathed);
		assertInHand(hunting, wreathed);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("lemenya"));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(hunting);
		assertTrue(scn.ShadowHasCardChoiceAvailable(wreathed, false));
		scn.ShadowDismissRevealedCards();

		// X is 0: two companions bear a ranged weapon, and neither is asked for.
		assertFalse(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInDiscard(hunting);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ExertingOnceStillAsksWhoExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornsbow"));
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("bow"));
		scn.MoveCardsToHand(hunting, runner);
		assertInHand(hunting, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("lemenya"));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(hunting);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		scn.ShadowDismissRevealedCards();

		// X is 1: the Shadow player chooses which of the two exerts.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, arwen));
		scn.ShadowChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void WoundingNoTimesAsksForNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reckless = scn.GetFreepsCard("reckless");
		var arwen = scn.GetFreepsCard("arwen");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("bow"));
		scn.MoveCardsToHand(reckless);
		assertInHand(reckless);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ARCHERY);

		scn.FreepsPlayCard(reckless);
		assertEquals(1, scn.GetWoundsOn(arwen));
		// The archery total is 1 (Arwen with the bow), so X is asked from 0 to 1.
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("0");

		// X is 0: two minions, and neither is asked for.
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertInDiscard(reckless);
	}

	@Test
	public void WoundingOnceStillAsksWhichMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reckless = scn.GetFreepsCard("reckless");
		var arwen = scn.GetFreepsCard("arwen");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("bow"));
		scn.MoveCardsToHand(reckless);
		assertInHand(reckless);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ARCHERY);

		scn.FreepsPlayCard(reckless);
		scn.FreepsChoose("1");

		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2));
		scn.FreepsChooseCard(troop2);
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop1));
	}

	@Test
	public void HealingNoTimesAsksForNoHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var appetite = scn.GetFreepsCard("appetite");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(appetite);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		scn.FreepsPlayCard(appetite);
		scn.FreepsChoose("0");

		// X is 0: two wounded Hobbit companions, and neither is asked for.
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to heal"));
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInDiscard(appetite);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealingOnceStillAsksWhichHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var appetite = scn.GetFreepsCard("appetite");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(appetite);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		scn.FreepsPlayCard(appetite);
		scn.FreepsChoose("1");

		assertTrue(scn.FreepsDecisionAvailable("Choose cards to heal"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(sam);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
	}

	@Test
	public void DiscardingNoCardsFromHandAsksForNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCardsToHand(advances, strike, fury);
		assertInHand(advances, strike, fury);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("chieftain"));
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(advances);
		scn.ShadowDismissRevealedCards();

		// Either card revealed costs 0: two cards in hand, and no selection of 0 to 0 is put to him.
		assertFalse(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertInHand(strike, fury);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DiscardingOneCardFromHandStillAsksWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var blow = scn.GetFreepsCard("blow");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(advances, blow, guard);
		assertInHand(advances, blow, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("chieftain"));
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(advances);
		scn.ShadowDismissRevealedCards();

		// Either card revealed costs 1: he chooses which of the two goes.
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(blow, guard));
		scn.FreepsChooseCard(guard);
		assertInDiscard(guard);
		assertInHand(blow);
	}
}
