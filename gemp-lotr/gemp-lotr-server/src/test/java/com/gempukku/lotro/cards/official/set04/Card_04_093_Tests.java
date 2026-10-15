package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patience", "4_93");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength +1 for each race in the fellowship
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; strength +2 only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit, vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, vitality 4; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, vitality 2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HavePatienceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Have Patience
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot Gandalf to heal an unbound companion twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("patience");

		assertEquals("Have Patience", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGandalfHealsTheChosenUnboundCompanionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patience = scn.GetFreepsCard("patience");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn, merry, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(patience);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		// The wounds were laid under the fellowship actions already on offer.
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(patience));
		scn.FreepsPlayCard(patience);
		// Unbound companions of three races and cultures, Gandalf himself among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merry, gandalf));
		// The Ring-bearer, a Ring-bound companion who is not the Ring-bearer, and an ally.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DISCARD, patience);
		// 3 - 2: not one heal, nor all of them.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGandalfThoughAGandalfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patience = scn.GetFreepsCard("patience");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveCardsToHand(patience);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		// The wounds were laid under the fellowship actions already on offer.
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.HAND, patience);
		assertFalse(scn.FreepsPlayAvailable(patience));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
