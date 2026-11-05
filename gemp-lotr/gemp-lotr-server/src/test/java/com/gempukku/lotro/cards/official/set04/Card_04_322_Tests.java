package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_322_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warmed", "4_322");
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, unbound; his skirmish ability is not used
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, unbound; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion who is Ring-bound by rule
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion who is no Hobbit; a skirmish ability only
					put("bounder", "1_286");    // Bounder: a Hobbit ally, never unbound; a skirmish ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarmedUpaBitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Warmed Up a Bit
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>.<br><b>Skirmish:</b> Discard an unbound Hobbit.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warmed");

		assertEquals("Warmed Up a Bit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAnUnboundHobbitOfAnyCultureWhetherOrNotHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warmed = scn.GetFreepsCard("warmed");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, pippin, sam, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(warmed);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertTrue(scn.FreepsPlayAvailable(warmed));
		scn.FreepsPlayCard(warmed);

		// Merry in this skirmish and Pippin, a [gondor] Hobbit in none: offered.  Frodo and Sam (Ring-bound),
		// Gimli (no Hobbit) and Bounder (a Hobbit ally, never unbound): not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, gimli, bounder, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		// Discarded, not killed: to the discard pile, not the dead pile.
		assertInDiscard(pippin, warmed);
		assertInZone(Zone.FREE_CHARACTERS, merry, sam, gimli);
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoUnboundHobbitItIsPlayedAndDiscardsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warmed = scn.GetFreepsCard("warmed");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(warmed);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, troop));

		// An event's target is no requirement: it is played, and nobody is discarded.
		assertTrue(scn.FreepsPlayAvailable(warmed));
		scn.FreepsPlayCard(warmed);

		assertInDiscard(warmed);
		assertInZone(Zone.FREE_CHARACTERS, frodo, sam, gimli);
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warmed = scn.GetFreepsCard("warmed");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(warmed);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(warmed));
		assertInZone(Zone.FREE_CHARACTERS, merry);
	}
}
