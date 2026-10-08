package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_01_198_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				// Thirteen cards in each deck: the deck and hand counts in the tests are worked out from that.
				new HashMap<>()
				{{
					put("gimli", "1_13");      // Gimli, Son of Gloin: Dwarf companion
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion
					put("celeborn", "1_34");   // Celeborn, Lord of Lorien: Elf ally
					put("grimir", "1_17");     // Grimir, Dwarven Elder: Dwarf ally
					put("guard", "1_7");       // Dwarf Guard: Dwarf companion, used as the Shadow player's own

					put("ttmm", "51_198");
					put("runner", "1_178");    // Goblin Runner: [moria] Orc, vitality 1, so it can never exert
					put("scout", "1_191");     // Moria Scout: [moria] Orc; adds (2) on being played only if an Elf is spotted
					put("troll", "1_165");     // Cave Troll of Moria: [moria] Troll
					put("soldier", "1_271");   // Orc Soldier: [sauron] Orc

					put("chaff1", "1_177");    // Goblin Patrol Troop: no text, fills the decks
					put("chaff2", "1_177");
					put("chaff3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				// Thirteen cards in each deck: the deck and hand counts in the tests are worked out from that.
				new HashMap<>()
				{{
					put("gimli", "1_13");      // Gimli, Son of Gloin: Dwarf companion
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion
					put("celeborn", "1_34");   // Celeborn, Lord of Lorien: Elf ally
					put("grimir", "1_17");     // Grimir, Dwarven Elder: Dwarf ally
					put("guard", "1_7");       // Dwarf Guard: Dwarf companion, used as the Shadow player's own

					put("ttmm", "51_198");
					put("runner", "1_178");    // Goblin Runner: [moria] Orc, vitality 1, so it can never exert
					put("scout", "1_191");     // Moria Scout: [moria] Orc; adds (2) on being played only if an Elf is spotted
					put("troll", "1_165");     // Cave Troll of Moria: [moria] Troll
					put("soldier", "1_271");   // Orc Soldier: [sauron] Orc

					put("chaff1", "1_177");    // Goblin Patrol Troop: no text, fills the decks
					put("chaff2", "1_177");
					put("chaff3", "1_177");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThroughtheMistyMountainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Through the Misty Mountains
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search.</b> To play, exert a [moria] minion.
		 * 	Each time the fellowship moves to any site 4, 5, or 6 and contains a Dwarf or Elf, draw a card.  The Free Peoples player may discard 1 card at random from hand to prevent this.
		 */

		var scn = GetScenario();

		var ttmm = scn.GetFreepsCard("ttmm");

		assertEquals("Through the Misty Mountains", ttmm.getBlueprint().getTitle());
		assertNull(ttmm.getBlueprint().getSubtitle());
		assertFalse(ttmm.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, ttmm.getBlueprint().getSide());
		assertEquals(Culture.MORIA, ttmm.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, ttmm.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(ttmm, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(ttmm, Keyword.SUPPORT_AREA));
		assertEquals(1, ttmm.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAMoriaMinionAbleToExertToPlayToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ttmm = scn.GetShadowCard("ttmm");
		var runner = scn.GetShadowCard("runner");
		var scout = scn.GetShadowCard("scout");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(scout, ttmm);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The only [moria] minion cannot exert (vitality 1) and the [sauron] Orc beside it does not count.
		assertEquals(13, scn.GetTwilight()); // 10 set + 1 companion + 2 for Ettenmoors
		assertFalse(scn.ShadowPlayAvailable(ttmm));

		scn.ShadowPlayCard(scout);
		assertEquals(9, scn.GetTwilight()); // 2 for Moria Scout + 2 roaming
		assertTrue(scn.ShadowPlayAvailable(ttmm));

		scn.MoveMinionsToTable(troll);
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(soldier));

		scn.ShadowPlayCard(ttmm);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);

		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, ttmm);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADwarfInTheFellowshipDrawsACardOnEachMoveToSite4Or5Or6AndNotTo3Or7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		// The Free Peoples player has no cards in hand all game, so cannot prevent the draw and is never asked.
		scn.MoveCardsToFreepsDiscard("arwen", "celeborn", "grimir", "guard", "ttmm", "runner", "scout", "troll",
				"soldier", "chaff1", "chaff2", "chaff3");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);

		scn.StartGame();
		scn.SkipToSite(2);

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipCurrentSite();

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipCurrentSite();

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 5
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipCurrentSite();

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 6
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipCurrentSite();

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 7
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnElfInTheFellowshipDrawsACardOnEachMoveToSite4Or5Or6AndNotTo3Or7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		// The Free Peoples player has no cards in hand all game, so cannot prevent the draw and is never asked.
		scn.MoveCardsToFreepsDiscard("gimli", "celeborn", "grimir", "guard", "ttmm", "runner", "scout", "troll",
				"soldier", "chaff1", "chaff2", "chaff3");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);

		scn.StartGame();
		scn.SkipToSite(2);

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 3
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipCurrentSite();

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 4
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipCurrentSite();

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 5
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipCurrentSite();

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 6
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipCurrentSite();

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 7
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DwarfAndElfAlliesTheShadowPlayersOwnDwarfAndADwarvenElvenHobbitDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Allies are not members of the fellowship (CR 4.2 "ally"); the Shadow player's Dwarf companion is in his own.
		scn.MoveAlliesToTable("celeborn", "grimir");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToFreepsDiscard("arwen", "gimli", "guard", "ttmm", "runner", "scout", "troll",
				"soldier", "chaff1", "chaff2", "chaff3");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);
		// Frodo, the only companion in the fellowship, is of the [dwarven] and [elven] cultures but neither a Dwarf
		// nor an Elf: the text names races, not cultures (F-0019).
		var frodo = scn.GetRingBearer();
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, frodo, null, Culture.DWARVEN));
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, frodo, null, Culture.ELVEN));

		scn.StartGame();
		scn.SkipToSite(2);

		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to 3
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipToSite(4);

		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipToSite(5);

		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipToSite(6);

		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnySite4OnAnotherBlocksPathCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToFreepsDiscard("arwen", "celeborn", "grimir", "guard", "ttmm", "runner", "scout", "troll",
				"soldier", "chaff1", "chaff2", "chaff3");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPass(); // move to Osgiliath Fallen, King-block site 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreePeoplesPlayerMayDeclineOrDiscardOneCardAtRandomToPreventEachDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("arwen", "gimli");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPass(); // move to 4
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		// A Dwarf and an Elf both in the fellowship still draw only one card.
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.SkipToMovementDecision();
		int shadowHand = scn.GetShadowHandCount();
		int freepsHand = scn.GetFreepsHandCount();
		scn.FreepsChooseToMove(); // move to 5, the second move of the turn
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(freepsHand - 1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PreventionIsNotAskedWhenTheShadowPlayerHasNoCardToDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("gimli");
		// The Shadow player's deck and hand are emptied: the draw cannot happen.
		scn.MoveCardsToShadowDiscard("arwen", "celeborn", "grimir", "guard", "runner", "scout", "troll",
				"soldier", "chaff1", "chaff2", "chaff3", "gimli");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);

		scn.StartGame();
		scn.SkipToSite(3);

		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(8, scn.GetFreepsHandCount());

		scn.FreepsPass(); // move to 4
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
