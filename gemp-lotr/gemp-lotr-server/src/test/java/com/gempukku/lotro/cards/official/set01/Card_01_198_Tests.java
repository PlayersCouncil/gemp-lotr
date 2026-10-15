package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_198_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ttmm", "1_198");

					put("gimli", "1_13");      // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion; a bonus against a Nazgûl only
					put("celeborn", "1_34");   // Celeborn, Lord of Lorien: Elf ally, home site 6; a fellowship ability used only on purpose
					put("grimir", "1_17");     // Grimir, Dwarven Elder: Dwarf ally, home site 3; a fellowship ability used only on purpose
					put("guard", "1_7");       // Dwarf Guard: Dwarf companion, laid in the Shadow player's own fellowship

					put("runner", "1_178");    // Goblin Runner: [moria] Orc, vitality 1, so it can never exert
					put("scout", "1_191");     // Moria Scout: [moria] Orc, vitality 2; adds (2) on being played only if an Elf is spotted
					put("troll", "1_165");     // Cave Troll of Moria: [moria] Troll, vitality 4
					put("soldier", "1_271");   // Orc Soldier: [sauron] Orc, vitality 2

					put("chaff1", "1_177");    // Goblin Patrol Troop: no text; the Free Peoples player's copies make up his hand
					put("chaff2", "1_177");
					put("chaff3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ttmm", "1_198");

					put("gimli", "1_13");      // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose

					put("chaff1", "1_177");    // Goblin Patrol Troop: no text; the Free Peoples player's copies make up his hand
					put("chaff2", "1_177");
					put("chaff3", "1_177");
				}},
				VirtualTableScenario.TowersSites,
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
		 * Game Text: <b>Search</b>. To play, exert a [moria] minion. Plays to your support area.
		 * Each time the fellowship moves to site 4, 5, or 6 and contains a Dwarf or Elf, the Free Peoples player discards 2 cards at random from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ttmm");

		assertEquals("Through the Misty Mountains", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
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
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(ttmm));

		// One [moria] minion able to exert, played legally: the condition can now be played.
		scn.ShadowPlayCard(scout);
		assertTrue(scn.ShadowPlayAvailable(ttmm));

		scn.MoveMinionsToTable(troll);
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(soldier));

		scn.ShadowPlayCard(ttmm);
		// A [moria] Orc and a [moria] Troll; not the Runner, who cannot exert, nor the [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);

		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInZone(Zone.SUPPORT, ttmm);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADwarfInTheFellowshipDiscardsTwoCardsAtRandomOnEachMoveToSite4Or5Or6AndNotTo3Or7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		var chaff3 = scn.GetFreepsCard("chaff3");
		scn.MoveCompanionsToTable(gimli);

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);

		scn.StartGame();
		scn.SkipToSite(2);

		assertInZone(Zone.SUPPORT, ttmm);
		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		int discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());

		scn.SkipCurrentSite();

		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());

		scn.SkipCurrentSite();

		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to 5
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());

		scn.SkipCurrentSite();

		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to 6
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());

		scn.SkipCurrentSite();

		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to 7
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnElfInTheFellowshipDiscardsTwoCardsAtRandomOnTheMoveToSite6AndNotTo7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		var chaff3 = scn.GetFreepsCard("chaff3");
		scn.MoveCompanionsToTable(arwen);

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);

		scn.StartGame();
		scn.SkipToSite(5);

		assertInZone(Zone.SUPPORT, ttmm);
		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		int discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to 6
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());

		scn.SkipCurrentSite();

		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to 7
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DwarfAndElfAlliesAndTheShadowPlayersOwnDwarfDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		var chaff3 = scn.GetFreepsCard("chaff3");
		var celeborn = scn.GetFreepsCard("celeborn");
		var grimir = scn.GetFreepsCard("grimir");
		// Allies are not members of the fellowship (CR 4.2 "ally"); the Shadow player's Dwarf companion is in his own.
		scn.MoveAlliesToTable(celeborn, grimir);
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(guard);

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);
		// Frodo, the only companion in the fellowship, is neither a Dwarf nor an Elf.

		scn.StartGame();
		scn.SkipToSite(3);

		assertInZone(Zone.SUPPORT, ttmm, celeborn, grimir);
		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		int discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());

		scn.SkipCurrentSite();
		scn.SkipCurrentSite();

		// Site 6, Celeborn's home: he is at the fellowship's site, and still no member of it.
		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to 6
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite4OfTheTowerBlockNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		var chaff3 = scn.GetFreepsCard("chaff3");
		scn.MoveCompanionsToTable(gimli);

		var ttmm = scn.GetShadowCard("ttmm");
		scn.MoveCardsToSupportArea(ttmm);

		scn.StartGame();
		scn.SkipToSite(3);

		assertInZone(Zone.SUPPORT, ttmm);
		scn.SetFreepsHand(chaff1, chaff2, chaff3);
		int discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass(); // move to White Rocks, Tower-block site 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
