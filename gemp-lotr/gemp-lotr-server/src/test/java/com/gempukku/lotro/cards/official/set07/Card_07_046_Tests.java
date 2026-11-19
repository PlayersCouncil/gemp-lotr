package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("peace", "7_46");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: a [gandalf] companion, no text
					// Ent Avenger: a [gandalf] companion who is no Gandalf, and an unbound companion who is no Hobbit
					put("avenger", "6_27");
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound [shire] Hobbit, vitality 4; a skirmish ability only
					// Pippin, Trollslayer: an unbound [gondor] Hobbit, vitality 4; enduring, a skirmish ability only
					put("pippin", "103_31");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PeaceOfMindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Peace of Mind
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot Gandalf to heal an unbound Hobbit 3 times.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("peace");

		assertEquals("Peace of Mind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGandalfHealsTheUnboundHobbitChosenThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peace = scn.GetFreepsCard("peace");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gandalf, avenger, merry, pippin, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(peace);
		// Only to arrange the board: room on Merry for four wounds, so that three heals leave one.
		scn.ApplyAdHocModifier(new VitalityModifier(null, merry, null, 1));

		scn.StartGame();
		scn.AddWoundsToChar(merry, 4);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(avenger, 1);
		assertEquals(4, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertInHand(peace);

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsPlayAvailable(peace));
		scn.FreepsPlayCard(peace);

		// Unbound Hobbits of two cultures; not the Ring-bearer or Sam (Ring-bound), the Hobbit ally, or the unbound
		// companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, avenger));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		// Three wounds off the one Hobbit chosen, and none off anyone else.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertInDiscard(peace);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peace = scn.GetFreepsCard("peace");
		var avenger = scn.GetFreepsCard("avenger");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(avenger, merry);
		scn.MoveCardsToHand(peace);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 2);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertInHand(peace);

		scn.SkipToPhase(Phase.REGROUP);

		// A [gandalf] companion, but no Gandalf.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(peace));
		assertEquals(2, scn.GetWoundsOn(merry));
	}

	@Test
	public void WithNoWoundedUnboundHobbitItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peace = scn.GetFreepsCard("peace");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gandalf, merry, sam);
		scn.MoveCardsToHand(peace);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 2);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertInHand(peace);

		scn.SkipToPhase(Phase.REGROUP);

		// The heal is no requirement: the event is played and heals nobody.
		assertTrue(scn.FreepsPlayAvailable(peace));
		scn.FreepsPlayCard(peace);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertInDiscard(peace);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
