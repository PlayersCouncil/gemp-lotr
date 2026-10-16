package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_266_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "2_84");           // Úlairë Nelya: [wraith] Nazgûl, cost 5, site 2; fierce only at 2 burdens
					put("khamul", "103_61");        // Úlairë Attëa, Khamûl of the East: [raider] Nazgûl, cost 6, site 3; his text needs 3 burdens
					put("hound", "7_192");          // Morgul Hound: [wraith] Orc and no Nazgûl, cost 2, site 4; stronger at 2 wounds on the Ring-bearer
					put("toldea", "1_236");         // Úlairë Toldëa: a Nazgûl of a third title, cost 6, site 3; an assignment ability at 4 burdens
					put("rider", "12_161");         // Black Rider: a Nazgûl of a fourth title, cost 5, site 3; strength +1 for each companion
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");         // Fangorn Glade: its text answers a move to it; the game starts there
					put("woodyend", "11_266");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site5", "18_138");
					put("site6", "12_187");
					put("site7", "13_185");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void WoodyEndStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Woody-End
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Forest</b>. <b>Shadow:</b> Remove a burden to play a Nazgûl from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("woodyend");

		assertEquals("Woody-End", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void ShadowAbilityRemovesABurdenToPlayANazgulFromTheShadowPlayersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToDiscard(nelya, khamul, hound);
		scn.MoveCardsToHand(rider);
		// A Nazgûl in the Free Peoples player's own discard pile, of a title none of the Shadow player's piles holds.
		var toldea = scn.GetFreepsCard("toldea");
		scn.MoveCardsToDiscard(toldea);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(2);
		// Enough for any of them, roaming included: Toldëa or Khamûl 6 + 2, the Hound 2 + 2.
		scn.SetTwilight(15);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("woodyend"));

		var woodyend = scn.GetCurrentSite();
		assertEquals("Woody-End", woodyend.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(woodyend));

		scn.ShadowUseCardAction(woodyend);
		assertEquals(1, scn.GetBurdens());
		// A Nazgûl of each culture in his discard pile is offered; a [wraith] minion who is no Nazgûl there, a Nazgûl
		// in his hand and a Nazgûl in the other player's discard pile are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, rider, toldea));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertInDiscard(nelya, hound, toldea);
		assertInHand(rider);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToDiscard(nelya);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.SetTwilight(15);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("woodyend"));

		var woodyend = scn.GetCurrentSite();
		assertEquals("Woody-End", woodyend.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(nelya);
		assertFalse(scn.ShadowActionAvailable(woodyend));
	}

	@Test
	public void NotOfferedWithThePoolOneShortOfTheNazgulsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToDiscard(nelya);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddBurdens(1);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("woodyend"));

		var woodyend = scn.GetCurrentSite();
		assertEquals("Woody-End", woodyend.getBlueprint().getTitle());
		assertEquals(1, scn.GetBurdens());

		// Nelya costs 5, and her site number is 2: no roaming at site 2.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(woodyend));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(woodyend));
	}
}
