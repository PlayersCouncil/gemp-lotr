package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_364_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "4_364");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion who is Ring-bound
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit
					// Pippin, Trollslayer: an unbound Hobbit who is [gondor] and not [shire].  His skirmish ability
					// is never used here.
					put("pippin", "103_31");
					put("gaffer", "1_291");     // The Gaffer: a Hobbit ally; only a companion is unbound
					put("helpless", "2_76");    // Helpless: a [wraith] condition on Sam, "Sam's game text does not apply"

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc with no text, vitality 3
					put("troop2", "1_177");
					put("troop3", "1_177");
					// Ted Sandyman: a [men] minion whose race is Hobbit, vitality 4.  A minion of another culture
					// and race to wound, and a Hobbit who is no companion.  His abilities are never used here.
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 3 is Rivendell Waterfall: "While you can spot a ranger at Rivendell Waterfall, the move limit is +1
	// for this turn."  Aragorn is a ranger, so a fellowship that reaches it on a regroup move may move once more.
	protected VirtualTableScenario GetThreeMoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "4_364");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit
					put("pippin", "103_31");    // Pippin, Trollslayer: an unbound [gondor] Hobbit

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc with no text, vitality 3
					put("ted", "18_75");        // Ted Sandyman: a [men] minion, vitality 4
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Sam, Bearer of Great Need is the Ring-bearer here, so that Helpless can lie on the Ring-bearer.
	protected VirtualTableScenario GetSamRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "4_364");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit
					put("helpless", "2_76");    // Helpless: a [wraith] condition on Sam, "Sam's game text does not apply"

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc with no text, vitality 3
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Aragorn, Wingfoot
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Ranger</b>.
		 * Each time the fellowship moves, you may wound a minion for each unbound Hobbit you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Wingfoot", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void MovingWoundsOneMinionPerUnboundHobbitSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(aragorn, sam, merry, pippin);
		scn.MoveAlliesToTable(gaffer);

		var troop1 = scn.GetShadowCard("troop1");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, ted);

		// Frodo (the Ring-bearer) and Sam are Ring-bound; the Gaffer is an ally and Ted a minion.
		assertTrue(scn.HasKeyword(scn.GetRingBearer(), Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertFalse(scn.HasKeyword(merry, Keyword.RING_BOUND));
		assertFalse(scn.HasKeyword(pippin, Keyword.RING_BOUND));

		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(ted));

		// The second move of the turn, with the minions of the first still on the table.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		// No number is asked for: the first wound is the first thing asked, a minion or none.  "A minion", of
		// whatever culture or race, and no companion.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merry));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(ted));

		// The second wound is a choice of its own: here it goes to the other minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, ted));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(ted);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(ted));

		// Only Merry and Pippin are unbound Hobbits: two wounds, and no third is offered.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BothWoundsForTwoUnboundHobbitsMayBePlacedOnOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, merry, pippin);

		var troop1 = scn.GetShadowCard("troop1");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, ted);
		scn.SkipToMovementDecision();

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		scn.FreepsChooseCard(troop1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		// The Troop is offered again for the second wound, and takes it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, ted));
		scn.FreepsChooseCard(troop1);
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertInZone(Zone.SHADOW_CHARACTERS, troop1, ted);

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingNoMinionEndsTheWoundsThoughAnotherUnboundHobbitCouldBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, merry, pippin);

		var troop1 = scn.GetShadowCard("troop1");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, ted);
		scn.SkipToMovementDecision();

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		// Two unbound Hobbits, and he spots neither: no minion at the first wound, and the second is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, ted));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingTriggerIsOptional() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToMovementDecision();

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveOfATurnOffersTheWoundsAgainAndTheMinionsChosenAreTheNumberOfWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeMoveScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, merry, pippin);

		var troop1 = scn.GetShadowCard("troop1");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, ted);
		scn.SkipToMovementDecision();

		// The move from site 2 to Rivendell Waterfall.  One of the two unbound Hobbits is spotted: a minion
		// for the first wound, and none for the second.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(ted);
		assertEquals(1, scn.GetWoundsOn(ted));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, ted));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With a ranger at the Waterfall the fellowship may move a third time, and the same minions are there.
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(ted);
		scn.FreepsChooseCard(troop1);
		assertEquals(2, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(troop1));

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayTheMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, merry, pippin);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The move that ends the fellowship phase: two unbound Hobbits to spot, and no minion to wound.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUnboundHobbitToSpotTheMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		// Hobbits who are Ring-bound (Frodo and Sam), a Hobbit ally, an unbound companion who is a Man, and a
		// Hobbit among the minions: nobody is an unbound Hobbit.
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(gaffer);

		var troop1 = scn.GetShadowCard("troop1");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, ted);
		scn.SkipToMovementDecision();
		assertFalse(scn.HasKeyword(aragorn, Keyword.RING_BOUND));

		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));

		assertEquals(0, scn.GetWoundsOn(troop1));

		// The fellowship stays where it is: the turn ends with nothing offered, and the other player's begins.
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void SamIsNotRingBoundWhenHisGameTextIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		// Ruling (reverting #1061): Helpless removes all of Sam's game text; Ring-bound is a keyword and keywords are
		// game text, so a Helpless Sam (who is not the Ring-bearer) is an unbound Hobbit and Wingfoot may count him.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var helpless = scn.GetShadowCard("helpless");
		scn.MoveCompanionsToTable(aragorn, sam, merry, pippin);
		scn.AttachCardsTo(sam, helpless);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, troop2, troop3);

		assertFalse(scn.HasKeyword(sam, Keyword.RING_BOUND));
		// The Ring-bearer stays Ring-bound regardless
		assertTrue(scn.HasKeyword(scn.GetRingBearer(), Keyword.RING_BOUND));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		// Sam, Merry and Pippin are all unbound Hobbits now: three wounds are offered, one after another
		scn.FreepsChooseCard(troop1);
		scn.FreepsChooseCard(troop2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2, troop3));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop3);

		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(1, scn.GetWoundsOn(troop3));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RingBearerStaysRingBoundWhenGameTextIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		// Unlike Sam's keyword as a companion, being Ring-bound as the Ring-bearer is a rule, not game text.
		var scn = GetSamRingBearerScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var helpless = scn.GetShadowCard("helpless");
		scn.MoveCompanionsToTable(aragorn, merry);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		var sam = scn.GetRingBearer();
		assertEquals("Sam", sam.getBlueprint().getTitle());
		scn.AttachCardsTo(sam, helpless);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, troop2);

		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		// Only Merry counts: one wound is offered, and with it placed no second is
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
