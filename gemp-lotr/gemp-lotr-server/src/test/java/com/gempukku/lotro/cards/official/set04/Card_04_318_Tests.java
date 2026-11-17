package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_318_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seven", "4_318");
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion; a skirmish ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability only
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no Hobbit; a skirmish ability only
					put("tale", "1_317");       // There and Back Again: a [shire] condition, bearer a Hobbit companion; a maneuver ability only
					put("habits", "11_165");    // Habits of Home: a [shire] condition, bearer a Hobbit; answers a move from a dwelling or forest only
					put("weary1", "4_212");     // Weary: an [isengard] condition on a companion or ally; answers an assignment to a tracker only
					put("weary2", "4_212");
					put("weary3", "4_212");
					put("pipe", "1_292");       // The Gaffer's Pipe: a possession a Hobbit bears, no condition; its fellowship ability is not used
					put("bladetip", "1_209");   // Blade Tip: a [wraith] condition in its owner's support area, borne by nobody
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, for a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SevenWeHadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Seven We Had
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Add a burden to discard a condition borne by a Hobbit.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("seven");

		assertEquals("Seven We Had", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsABurdenToDiscardAConditionBorneByAHobbitOfAnyKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seven = scn.GetFreepsCard("seven");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var gimli = scn.GetFreepsCard("gimli");
		var tale = scn.GetFreepsCard("tale");
		var habits = scn.GetFreepsCard("habits");
		var pipe = scn.GetFreepsCard("pipe");
		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");
		var weary3 = scn.GetShadowCard("weary3");
		var bladetip = scn.GetShadowCard("bladetip");
		scn.MoveCompanionsToTable(merry, pippin, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(frodo, weary1, pipe);
		scn.AttachCardsTo(merry, tale);
		scn.AttachCardsTo(pippin, habits);
		scn.AttachCardsTo(bounder, weary2);
		scn.AttachCardsTo(gimli, weary3);
		scn.MoveCardsToSupportArea(bladetip);
		scn.MoveCardsToHand(seven);

		scn.StartGame();

		assertAttachedTo(weary1, frodo);
		assertAttachedTo(pipe, frodo);
		assertAttachedTo(tale, merry);
		assertAttachedTo(habits, pippin);
		assertAttachedTo(weary2, bounder);
		assertAttachedTo(weary3, gimli);
		assertInZone(Zone.SUPPORT, bladetip);
		assertEquals(0, scn.GetBurdens());

		assertTrue(scn.FreepsPlayAvailable(seven));
		scn.FreepsPlayCard(seven);
		assertEquals(1, scn.GetBurdens());

		// Conditions of either side on the Ring-bearer, an unbound [shire] Hobbit, a [gondor] Hobbit and a Hobbit ally:
		// offered.  A condition on Gimli, a possession on Frodo and a condition nobody bears: not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(weary1, tale, habits, weary2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(weary3, pipe, bladetip));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(weary2);

		assertInDiscard(weary2, seven);
		assertAttachedTo(weary1, frodo);
		assertAttachedTo(pipe, frodo);
		assertAttachedTo(tale, merry);
		assertAttachedTo(habits, pippin);
		assertAttachedTo(weary3, gimli);
		assertInZone(Zone.SUPPORT, bladetip);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoConditionBorneByAHobbitItIsPlayedAndAddsABurdenForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seven = scn.GetFreepsCard("seven");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var pipe = scn.GetFreepsCard("pipe");
		var weary3 = scn.GetShadowCard("weary3");
		var bladetip = scn.GetShadowCard("bladetip");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(frodo, pipe);
		scn.AttachCardsTo(gimli, weary3);
		scn.MoveCardsToSupportArea(bladetip);
		scn.MoveCardsToHand(seven);

		scn.StartGame();

		assertAttachedTo(pipe, frodo);
		assertAttachedTo(weary3, gimli);
		assertInZone(Zone.SUPPORT, bladetip);
		assertEquals(0, scn.GetBurdens());

		// An event's target is no requirement: it is played, and the burden is paid for nothing.
		assertTrue(scn.FreepsPlayAvailable(seven));
		scn.FreepsPlayCard(seven);

		assertEquals(1, scn.GetBurdens());
		assertInDiscard(seven);
		assertAttachedTo(pipe, frodo);
		assertAttachedTo(weary3, gimli);
		assertInZone(Zone.SUPPORT, bladetip);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InTheManeuverPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seven = scn.GetFreepsCard("seven");
		var merry = scn.GetFreepsCard("merry");
		var tale = scn.GetFreepsCard("tale");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, tale);
		scn.MoveCardsToHand(seven);

		scn.StartGame();
		assertAttachedTo(tale, merry);
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(seven));
		assertAttachedTo(tale, merry);
	}
}
