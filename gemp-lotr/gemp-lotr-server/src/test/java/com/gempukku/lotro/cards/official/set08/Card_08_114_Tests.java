package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("straining", "8_114");
					// Gandalf, Friend of the Shirefolk: a [gandalf] Wizard with the Frodo signet; a strength bonus only
					put("gandalf", "1_72");
					// Legolas, Greenleaf: an [elven] Elf with the Frodo signet; his archery ability is used only on purpose
					put("legolas", "1_50");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the Gandalf signet; his maneuver ability is used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: the Aragorn signet; +2 only while he bears a weapon
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] companion with the Aragorn signet; his abilities are used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Son of Drogo: the Ring-bearer, with the Frodo signet; his fellowship ability is used only on purpose
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGimliRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("straining", "8_114");
					// Frodo, Frenzied Fighter: the one Frodo who can be in play beside another Ring-bearer; [shire]
					// Hobbit, Ring-bound, no signet, vitality 4; his skirmish ability is used only on purpose
					put("frodo", "13_149");
					put("legolas", "1_50");     // Legolas, Greenleaf: the Frodo signet
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] companion with the Aragorn signet
				}},
				VirtualTableScenario.FellowshipSites,
				// Gimli, Bearer of Grudges: the Ring-bearer, a [dwarven] Dwarf with no signet; his trigger is at the
				// start of his own skirmishes
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrainingTowardsUsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Straining Towards Us
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Exert Frodo to remove (1) for each Frodo signet you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("straining");

		assertEquals("Straining Towards Us", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsFrodoToRemoveOneForEachFrodoSignetTheNumberNamedFromThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var straining = scn.GetFreepsCard("straining");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(gandalf, legolas, aragorn, merry);
		scn.MoveCardsToHand(straining);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.FreepsPlayAvailable(straining));
		scn.FreepsPlayCard(straining);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(legolas));

		// Frodo, Gandalf and Legolas have the Frodo signet; Aragorn (Gandalf signet) and Merry (Aragorn signet) are
		// not counted.  The player spots as many as he likes, filled in with all of them.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		// 5 - 2
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(straining);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithFrodoTheOneFrodoSignetItRemovesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var straining = scn.GetFreepsCard("straining");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(straining);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(straining);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		// 5 - 1
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(straining);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileFrodoIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var straining = scn.GetFreepsCard("straining");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(straining);
		// Vitality 4 with 3 wounds: exhausted.  Sam, a [shire] companion who is no Frodo, can exert.
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(straining));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void ItIsFrodoWhoExertsAndNotAnotherRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRingBearerScenario();

		var straining = scn.GetFreepsCard("straining");
		var gimli = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(frodo, legolas, sam);
		scn.MoveCardsToHand(straining);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPlayCard(straining);
		// The one Frodo exerts unasked; neither the Ring-bearer nor Sam does.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		// This Frodo has no signet: Legolas is the one Frodo signet.
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertEquals(4, scn.GetTwilight());
		assertInDiscard(straining);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
