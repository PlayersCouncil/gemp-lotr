package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_120_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wff", "103_120");
					put("sam", "1_311");
					put("merry", "1_302");
					put("pippin", "1_306");
					put("aragorn", "1_89");
					put("cond1", "1_206");  // Bent on Discovery (Shadow condition)
					put("cond2", "1_173");  // Goblin Armory (Shadow condition)
					put("cond3", "2_77");   // His Terrible Servants (Shadow condition)
					put("filler", "1_178"); // stacked on a condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WorthFightingForStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Worth Fighting For
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: To play, spot a Hobbit companion.
		 * 	Make the Shadow player exhaust 1 of your characters.  For each exertion made, hinder a Shadow support card.
		 * 	If you cannot spot more than 4 companions, you may make the Shadow player shuffle one of their hindered
		 * 	cards (and all cards stacked on it) into their owner's draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wff");

		assertEquals("Worth Fighting For", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WorthFightingForExhaustsAndHindersThenShufflesWithFewCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wff = scn.GetFreepsCard("wff");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var cond1 = scn.GetShadowCard("cond1");
		var cond2 = scn.GetShadowCard("cond2");
		var cond3 = scn.GetShadowCard("cond3");
		var filler = scn.GetShadowCard("filler");

		// 3 companions (Frodo + Sam + Merry), under 5
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(wff);
		scn.MoveCardsToSupportArea(cond1, cond2, cond3);
		scn.StackCardsOn(cond1, filler);

		scn.StartGame();

		// Fellowship phase: play Worth Fighting For
		scn.FreepsPlayCard(wff);

		// Shadow chooses a FP character to exhaust — pick Sam (vit 4)
		scn.ShadowChooseCard(sam);

		assertTrue(scn.IsExhausted(sam));

		// FP hinders 3 Shadow support cards (one per exertion)
		scn.FreepsChooseCard(cond1);
		scn.FreepsChooseCard(cond2);

		assertTrue(scn.IsHindered(cond1));
		assertTrue(scn.IsHindered(cond2));
		assertTrue(scn.IsHindered(cond3));

		// With ≤4 companions, Shadow chooses a hindered card to shuffle into deck
		// Choose cond1 (which has filler stacked on it)
		scn.ShadowChooseCard(cond1);

		// cond1 and its stacked filler card both shuffled into Shadow draw deck
		assertInZone(Zone.DECK, cond1);
		assertInZone(Zone.DECK, filler);

		// cond2 still hindered in support, cond3 unaffected
		assertInZone(Zone.SUPPORT, cond2);
		assertTrue(scn.IsHindered(cond2));
		assertInZone(Zone.SUPPORT, cond3);
		assertTrue(scn.IsHindered(cond3));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WorthFightingForDoesNotShuffleWith5PlusCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wff = scn.GetFreepsCard("wff");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cond1 = scn.GetShadowCard("cond1");
		var cond2 = scn.GetShadowCard("cond2");
		var cond3 = scn.GetShadowCard("cond3");

		// 5 companions: Frodo + Sam + Merry + Pippin + Aragorn
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.MoveCardsToHand(wff);
		scn.MoveCardsToSupportArea(cond1, cond2, cond3);

		scn.StartGame();

		// Play Worth Fighting For
		scn.FreepsPlayCard(wff);

		// Shadow exhausts Sam (vit 4 → 3 exertions)
		scn.ShadowChooseCard(sam);
		assertTrue(scn.IsExhausted(sam));

		// FP hinders 2 Shadow support cards
		scn.FreepsChooseCard(cond1);
		scn.FreepsChooseCard(cond2);

		assertTrue(scn.IsHindered(cond1));
		assertTrue(scn.IsHindered(cond2));
		assertTrue(scn.IsHindered(cond3));

		// With 5+ companions, shuffle step is skipped — hindered cards remain in support
		assertInZone(Zone.SUPPORT, cond1);
		assertInZone(Zone.SUPPORT, cond2);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
