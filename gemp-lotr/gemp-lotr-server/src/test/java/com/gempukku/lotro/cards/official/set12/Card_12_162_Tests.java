package com.gempukku.lotro.cards.official.set12;

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

public class Card_12_162_Tests
{

	// The Fellowship path with five forests: sites 1, 2, 3, 6 and 7.  Sites 4 (Balin's Tomb) and 5 (The Bridge of
	// Khazad-dûm) are no forests.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("approach", "12_162");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, cost 5, site 2
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: [wraith] Nazgûl, cost 8, site 3; text about his strength only
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, cost 6, site 3
					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] Nazgûl, cost 4, laid in the discard pile
					put("hound", "7_192");      // Morgul Hound: a [wraith] minion who is an Orc and no Nazgûl, cost 2
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "2_116");      // Hobbiton Woods: forest; a fellowship ability used only on purpose
					put("site2", "1_334");      // Trollshaw Forest: forest; draws on a possession played on a companion
					put("site3", "1_342");      // Rivendell Waterfall: forest, sanctuary; its text needs a ranger
					put("site6", "1_352");      // Lothlórien Woods: forest, sanctuary; its text is for allies
					put("site7", "1_354");      // Anduin Wilderland: forest, river; the archery phase is skipped
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkApproachStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Dark Approach
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play a Nazgûl. His twilight cost is -1 for each forest site you can spot (limit -4).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("approach");

		assertEquals("Dark Approach", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithThreeForestsSpottedHisCostIsThreeLessAndTheNextNazgulPaysInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var approach = scn.GetShadowCard("approach");
		var nelya = scn.GetShadowCard("nelya");
		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.SkipToSite(3);
		// The Shadow player has drawn on the way: his hand is made these three cards alone.
		scn.SetShadowHand(approach, nelya, witchking);
		scn.FreepsPass();

		// Shadow phase at site 4: sites 1 to 3 are forests, Balin's Tomb is not.  Nelya costs 5: through Dark
		// Approach 2, and no less.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(approach));
		assertFalse(scn.ShadowPlayAvailable(nelya));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(approach));
		assertFalse(scn.ShadowPlayAvailable(nelya));

		scn.ShadowPlayCard(approach);

		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertInDiscard(approach);
		// 2 - (5 - 3); Nelya (site 2) is not roaming at site 4
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// "His" twilight cost: the next Nazgûl played this phase pays his own in full.
		assertEquals(8, scn.GetTwilightCost(witchking));
		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(witchking);

		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		// 8 - 8; The Witch-king (site 3) is not roaming at site 4
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OffersEveryNazgulInHandAndWithTwoForestsSpottedHisCostIsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var approach = scn.GetShadowCard("approach");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveCardsToHand(approach, nelya, khamul, hound);
		scn.MoveCardsToDiscard(lemenya);

		scn.FreepsPass();
		// Shadow phase at site 2: Hobbiton Woods and Trollshaw Forest.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(approach);

		// Nazgûl of two cultures in hand; neither a [wraith] minion who is no Nazgûl, nor a Nazgûl in the discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, lemenya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertInDiscard(approach);
		assertInHand(nelya);
		assertInHand(hound);
		assertInDiscard(lemenya);
		// 20 - (6 - 2 + 2 roaming): Khamûl is site 3
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFiveForestsSpottedHisCostIsFourLessAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var approach = scn.GetShadowCard("approach");
		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.SkipToSite(6);
		// The Shadow player has drawn on the way: his hand is made these two cards alone.
		scn.SetShadowHand(approach, witchking);
		scn.FreepsPass();

		// Shadow phase at site 7: sites 1, 2, 3, 6 and 7 are forests.  The Witch-king costs 8: through Dark
		// Approach 4 (limit -4), not 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(approach));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(approach));
		assertFalse(scn.ShadowPlayAvailable(witchking));

		scn.ShadowPlayCard(approach);

		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertInDiscard(approach);
		// 4 - (8 - 4)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoNazgulInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var approach = scn.GetShadowCard("approach");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveCardsToHand(approach, hound);
		scn.MoveCardsToDiscard(lemenya);

		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// The minion in hand is no Nazgûl, and the Nazgûl is not in hand.
		assertTrue(scn.ShadowPlayAvailable(hound));
		assertFalse(scn.ShadowPlayAvailable(approach));
	}
}
