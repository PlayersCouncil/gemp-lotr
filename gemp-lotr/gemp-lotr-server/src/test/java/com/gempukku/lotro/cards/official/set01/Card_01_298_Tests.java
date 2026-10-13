package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_298_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stealth", "1_298");
					put("stealth2", "1_298");
					put("stealth3", "1_298");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3 (cheated in); a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; abilities used only on purpose

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
					put("troop2", "1_177");
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
					put("stealth", "1_298");
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	// The King block sites: site 2 is King's Tent 7_335, whose Shadow ability is used only on purpose.
	protected VirtualTableScenario GetKingPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stealth", "1_298");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3; a skirmish ability used only on purpose
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitStealthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Hobbit Stealth
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>.
		 * <b>Skirmish:</b> At sites 1 to 5, cancel a skirmish involving a Hobbit. At any other site, make a Hobbit strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stealth");

		assertEquals("Hobbit Stealth", card.getBlueprint().getTitle());
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

		var stealth = scn.GetFreepsCard("stealth");
		var stealth2 = scn.GetFreepsCard("stealth2");
		var stealth3 = scn.GetFreepsCard("stealth3");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, pippin, aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(stealth, stealth2, stealth3);
		scn.MoveMinionsToTable(troop1, troop2, ted);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, troop1}, new PhysicalCardImpl[]{pippin, troop2},
				new PhysicalCardImpl[]{aragorn, ted});

		// A [shire] Hobbit companion.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertTrue(scn.FreepsPlayAvailable(stealth));
		scn.FreepsPlayCard(stealth);
		assertFalse(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharSkirmishing(troop1));
		assertInDiscard(stealth);

		// A [gondor] Hobbit companion.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop2));
		scn.FreepsPlayCard(stealth2);
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertInDiscard(stealth2);

		// The Shadow player's Hobbit, a minion.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ted));
		scn.FreepsPlayCard(stealth3);
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(ted));
		assertInDiscard(stealth3);

		// Nobody fought: no wound anywhere.
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtSite5ASkirmishInvolvingNoHobbitIsNotCancelledAndNoHobbitIsMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stealth = scn.GetFreepsCard("stealth");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry, smeagol);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(stealth);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(smeagol, troop1);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		assertEquals(3, scn.GetStrength(merry));
		// 3 and The Ruling Ring's +1
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(stealth));

		// Smeagol is [shire] and no Hobbit: the event is played, and nothing is cancelled.
		scn.FreepsPlayCard(stealth);
		assertInDiscard(stealth);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		// At site 5 the second sentence does nothing either.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stealth = scn.GetFreepsCard("stealth");
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(stealth);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stealth));
	}

	@Test
	public void AtSite6ItMakesAHobbitOfAnyCultureOrSideStrengthPlus2AndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stealth = scn.GetFreepsCard("stealth");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, pippin, smeagol, aragorn);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(stealth);
		scn.MoveMinionsToTable(troop1, ted);
		scn.SkipToAssignments();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, troop1}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsPlayAvailable(stealth));

		scn.FreepsPlayCard(stealth);
		// Hobbits in this skirmish, in another, and in none, of both sides; not Smeagol, Aragorn or the Troop.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, frodo, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, troop1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertEquals(5, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(ted));
		assertInDiscard(stealth);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheStrengthBonusLastsToTheEndOfTheSkirmishPhaseAndNotIntoTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stealth = scn.GetFreepsCard("stealth");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(pippin, aragorn);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(stealth);
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop1}, new PhysicalCardImpl[]{pippin, troop2});
		scn.FreepsResolveSkirmish(aragorn);

		// Pippin is a Hobbit who waits for his own skirmish, the second of the turn.
		assertEquals(3, scn.GetStrength(pippin));
		scn.FreepsPlayCard(stealth);
		scn.FreepsChooseCard(pippin);
		assertEquals(5, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the last decision of the skirmish phase it was made in.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(pippin));

		// Gone when the next skirmish phase begins, and in his own skirmish.
		scn.FreepsPass();
		assertEquals(3, scn.GetStrength(pippin));
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop2));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnTheKingPathAtSite2ItMakesAHobbitStrongerAndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingPathScenario();

		var stealth = scn.GetFreepsCard("stealth");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(stealth);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(merry, troop1);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertEquals(3, scn.GetStrength(merry));

		// Site 2 of the King block is no site 1 to 5 of the Fellowship block (CR 4.2 "block symbol").
		scn.FreepsPlayCard(stealth);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		scn.FreepsChooseCard(merry);

		assertEquals(5, scn.GetStrength(merry));
		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsTheRingBearersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var stealth = scn.GetFreepsCard("stealth");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveCardsToHand(stealth);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// Site 2, the first site at which a skirmish is fought.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		scn.FreepsPlayCard(stealth);

		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(stealth);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathItIsPlayedAndTheRingBearersSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stealth = scn.GetFreepsCard("stealth");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(stealth);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.FreepsPlayAvailable(stealth));
		scn.FreepsPlayCard(stealth);

		assertInDiscard(stealth);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
