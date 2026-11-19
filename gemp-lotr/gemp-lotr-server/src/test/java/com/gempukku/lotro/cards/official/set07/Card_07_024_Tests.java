package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaving", "7_24");
					put("leaving2", "7_24");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion; an archery ability only
					put("orophin", "1_56");     // Orophin, Lórien Bowman: an Elf ally; an archery ability only
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an Elf ally of another title; the text of his owner's site 3, which is not laid
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion who is no Elf; a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion to bear a condition and a possession
					put("endurance", "2_4");    // Endurance of Dwarves: a Free Peoples condition a Dwarf bears; a skirmish ability only
					put("armor", "1_8");        // Dwarven Armor: a possession a Dwarf bears; no number changed
					put("lord", "1_21");        // Lord of Moria: a Free Peoples condition in the support area; each Dwarf is damage +1
					put("ambition", "1_133");   // Saruman's Ambition: a Shadow condition in the support area; a cost bonus and a skirmish ability
					put("savage", "1_151");     // Uruk Savage: a minion, so that there is a maneuver phase; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LeavingForeverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Leaving Forever
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 3 Elves.
		 * Each time the fellowship moves, add a threat or discard this condition.
		 * <b>Regroup:</b> Discard this condition to discard a condition or draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leaving");

		assertEquals("Leaving Forever", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilThreeElvesAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(legolas, boromir);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(leaving, silinde);

		scn.StartGame();

		// Two Elves, a companion and an ally, beside a companion who is no Elf.
		assertFalse(scn.FreepsPlayAvailable(leaving));

		scn.FreepsPlayCard(silinde);
		assertTrue(scn.FreepsPlayAvailable(leaving));

		scn.FreepsPlayCard(leaving);
		assertInZone(Zone.SUPPORT, leaving);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveAsksToAddAThreatOrDiscardItAndAThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(leaving);
		assertInZone(Zone.SUPPORT, leaving);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		// The fellowship moves from site 1.
		scn.FreepsPass();
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, leaving);

		// No minion: on to the regroup phase, and a second move. Two companions leave room for a second threat.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, leaving);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAMoveItMayBeDiscardedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(leaving);
		assertInZone(Zone.SUPPORT, leaving);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		scn.FreepsPass();
		scn.FreepsChooseOption("Discard this condition");
		assertInDiscard(leaving);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoRoomForAThreatItIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		scn.MoveCardsToSupportArea(leaving);
		assertInZone(Zone.SUPPORT, leaving);

		scn.StartGame();
		// Frodo is the only companion: one threat is all there may be.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		scn.FreepsPass();
		assertFalse(scn.FreepsChoiceAvailable("Add a threat"));
		assertInDiscard(leaving);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyAnswersAMoveForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		var leaving2 = scn.GetFreepsCard("leaving2");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(leaving, leaving2);
		assertInZone(Zone.SUPPORT, leaving, leaving2);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		// The copy laid first answers first: discarding it discards that copy alone.
		scn.FreepsPass();
		scn.FreepsChooseOption("Discard this condition");
		assertInDiscard(leaving);
		assertInZone(Zone.SUPPORT, leaving2);

		// The second copy asks for itself.
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, leaving2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsItToDiscardACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		var gimli = scn.GetFreepsCard("gimli");
		var endurance = scn.GetFreepsCard("endurance");
		var armor = scn.GetFreepsCard("armor");
		var lord = scn.GetFreepsCard("lord");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, endurance, armor);
		assertEquals(gimli, endurance.getAttachedTo());
		assertEquals(gimli, armor.getAttachedTo());

		scn.StartGame();
		// Laid after the move from site 1, which would otherwise ask for a threat or its discard.
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(leaving, lord, ambition);
		assertInZone(Zone.SUPPORT, leaving, lord, ambition);
		scn.SkipToPhase(Phase.REGROUP);

		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsActionAvailable(leaving));

		scn.FreepsUseCardAction(leaving);
		assertInDiscard(leaving);
		assertTrue(scn.FreepsChoiceAvailable("Discard a condition"));
		assertTrue(scn.FreepsChoiceAvailable("Draw 2 cards"));
		scn.FreepsChooseOption("Discard a condition");

		// A condition of either side, in a support area or borne; not a possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ambition, lord, endurance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ambition);

		assertInDiscard(ambition);
		assertInZone(Zone.SUPPORT, lord);
		assertEquals(gimli, endurance.getAttachedTo());
		assertEquals(gimli, armor.getAttachedTo());
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityMayDrawTwoCardsInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		var lord = scn.GetFreepsCard("lord");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(leaving, lord);
		assertInZone(Zone.SUPPORT, leaving, lord);
		scn.SkipToPhase(Phase.REGROUP);

		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsUseCardAction(leaving);
		assertTrue(scn.FreepsChoiceAvailable("Discard a condition"));
		scn.FreepsChooseOption("Draw 2 cards");

		assertInDiscard(leaving);
		assertEquals(hand + 2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherConditionInPlayTheDrawIsCarriedOutUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		assertEquals(gimli, armor.getAttachedTo());

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(leaving);
		assertInZone(Zone.SUPPORT, leaving);
		scn.SkipToPhase(Phase.REGROUP);

		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		// The only other card it could discard is a possession.
		scn.FreepsUseCardAction(leaving);
		assertFalse(scn.FreepsChoiceAvailable("Discard a condition"));
		assertInDiscard(leaving);
		assertEquals(hand + 2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(gimli, armor.getAttachedTo());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheDiscardIsCarriedOutUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		var ambition = scn.GetShadowCard("ambition");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(leaving, ambition);
		assertInZone(Zone.SUPPORT, leaving, ambition);
		scn.SetFreepsDrawDeck();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetFreepsDeckCount());
		int hand = scn.GetFreepsHandCount();

		// Two cards cannot be drawn: the condition is discarded, the only one there is, with no question.
		scn.FreepsUseCardAction(leaving);
		assertInDiscard(leaving);
		assertInDiscard(ambition);
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaving = scn.GetFreepsCard("leaving");
		var lord = scn.GetFreepsCard("lord");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(leaving, lord);
		assertInZone(Zone.SUPPORT, leaving, lord);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leaving));
	}
}
