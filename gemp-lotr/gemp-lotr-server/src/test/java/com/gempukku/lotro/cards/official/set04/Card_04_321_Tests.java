package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_321_Tests
{

	// The Towers path, whose sites 1 to 5 are the card's "sites 1[T] to 5[T]".
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swiftly", "4_321");
					put("swiftly2", "4_321");
					put("swiftly3", "4_321");
					put("swiftly4", "4_321");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, vitality 4 (cheated in); a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; abilities used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, strength 2; a skirmish ability used only on purpose
					put("follow", "1_109");     // One Whom Men Would Follow: [gondor] maneuver event, exert Aragorn so that an ally takes part in skirmishes

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					// Uruk Savage: strength 5 and Damage +1, no other text. A Hobbit of strength 3 loses to it and takes two
					// wounds, and is not overwhelmed (5 is less than twice 3).
					put("savage1", "1_151");
					put("savage2", "1_151");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers block format, where a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetTowersBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swiftly", "4_321");
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"ttt_block"
		);
	}

	// The Fellowship block sites: site 5 is The Bridge of Khazad-dum 1_349, whose Shadow ability is used only on purpose.
	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swiftly", "4_321");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("savage1", "1_151");    // Uruk Savage: strength 5 and Damage +1, no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwiftlyandSoftlyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Swiftly and Softly
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>.
		 * <b>Skirmish:</b> At sites 1[T] to 5[T], cancel a skirmish involving a Hobbit. At any other site, prevent a Hobbit from taking more than 1 wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swiftly");

		assertEquals("Swiftly and Softly", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite5ItCancelsASkirmishInvolvingAHobbitOfAnyCultureOrSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swiftly = scn.GetFreepsCard("swiftly");
		var swiftly2 = scn.GetFreepsCard("swiftly2");
		var swiftly3 = scn.GetFreepsCard("swiftly3");
		var swiftly4 = scn.GetFreepsCard("swiftly4");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var follow = scn.GetFreepsCard("follow");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(swiftly, swiftly2, swiftly3, swiftly4, follow);
		scn.MoveMinionsToTable(troop1, troop2, troop3, ted);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(5, scn.GetCurrentSiteNumber());
		// A Hobbit ally is at his home site only in the Fellowship block, so he is made to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, troop1}, new PhysicalCardImpl[]{pippin, troop2},
				new PhysicalCardImpl[]{aragorn, ted}, new PhysicalCardImpl[]{bounder, troop3});

		// A [shire] Hobbit companion.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertTrue(scn.FreepsPlayAvailable(swiftly));
		scn.FreepsPlayCard(swiftly);
		assertFalse(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharSkirmishing(troop1));
		assertInDiscard(swiftly);

		// A [gondor] Hobbit companion.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop2));
		scn.FreepsPlayCard(swiftly2);
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertInDiscard(swiftly2);

		// The Shadow player's Hobbit, a minion.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ted));
		scn.FreepsPlayCard(swiftly3);
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(ted));
		assertInDiscard(swiftly3);

		// A Hobbit ally.
		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, troop3));
		scn.FreepsPlayCard(swiftly4);
		assertFalse(scn.IsCharSkirmishing(bounder));
		assertFalse(scn.IsCharSkirmishing(troop3));
		assertInDiscard(swiftly4);

		// Nobody fought: no wound anywhere.
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtSite5ASkirmishInvolvingNoHobbitIsNotCancelledAndNoHobbitIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swiftly = scn.GetFreepsCard("swiftly");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry, smeagol);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(swiftly);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(smeagol, troop1);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		assertTrue(scn.FreepsPlayAvailable(swiftly));

		// Smeagol is [shire] and no Hobbit: the event is played, and nothing is cancelled. At site 5 the second
		// sentence is not reached either: no Hobbit is asked for, though Merry and Frodo are there.
		scn.FreepsPlayCard(swiftly);
		assertInDiscard(swiftly);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swiftly = scn.GetFreepsCard("swiftly");
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(swiftly);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swiftly));
	}

	@Test
	public void AtSite6ItPreventsAHobbitOfAnyCultureOrSideFromTakingMoreThanOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swiftly = scn.GetFreepsCard("swiftly");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var savage1 = scn.GetShadowCard("savage1");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(swiftly);
		scn.MoveMinionsToTable(savage1, ted);
		scn.SkipToAssignments();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, savage1}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage1));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsPlayAvailable(swiftly));

		scn.FreepsPlayCard(swiftly);
		// Hobbits in this skirmish, in another, and in none, companions, an ally and a minion of both sides; not
		// Smeagol, Aragorn or the Savage.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, frodo, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, savage1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertInDiscard(swiftly);
		// Nothing is cancelled at site 6.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage1));

		// Merry loses (3 against 5), and of the two wounds he would take (Damage +1) he takes one.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheWoundLimitLastsOnlyForTheSkirmishPhaseItWasMadeIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swiftly = scn.GetFreepsCard("swiftly");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(swiftly);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, savage1}, new PhysicalCardImpl[]{pippin, savage2});
		scn.FreepsResolveSkirmish(merry);

		// Pippin, who waits for his own skirmish, is the Hobbit chosen during Merry's.
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.FreepsPlayCard(swiftly);
		scn.FreepsChooseCard(pippin);

		// Merry, not chosen, takes both wounds of his lost skirmish.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(merry));

		// In the next skirmish phase the limit is gone: Pippin loses (3 against 5) and takes both wounds.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, savage2));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtSite5OfTheFellowshipPathItLimitsTheWoundsAndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var swiftly = scn.GetFreepsCard("swiftly");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(swiftly);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		// Site 5 of the Fellowship block is no site 5[T] (CR 4.2 "block symbol").
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(merry, savage1);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage1));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsPlayCard(swiftly);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		scn.FreepsChooseCard(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage1));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InTheTowersBlockFormatItCancelsTheRingBearersSkirmishAtSite2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersBlockScenario();

		var swiftly = scn.GetFreepsCard("swiftly");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveCardsToHand(swiftly);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// Site 2, the first site at which a skirmish is fought.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		scn.FreepsPlayCard(swiftly);

		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(swiftly);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathItIsPlayedAndTheRingBearersSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swiftly = scn.GetFreepsCard("swiftly");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(swiftly);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.FreepsPlayAvailable(swiftly));
		scn.FreepsPlayCard(swiftly);

		assertInDiscard(swiftly);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
