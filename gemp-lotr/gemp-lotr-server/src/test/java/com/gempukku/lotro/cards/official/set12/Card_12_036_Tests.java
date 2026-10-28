package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_036_Tests
{

	// Site 3 Rivendell Valley (a sanctuary), site 4 Balin's Tomb (underground only).  No battleground on this path.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("doom", "12_36");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: no text
					// Ent Avenger: a [gandalf] Ent companion who is no Gandalf; +2 strength only beside 3 Ents
					put("avenger", "6_27");
					// Ottar, Man of Laketown: a [gandalf] Man ally, home 3F; to play, spot Gandalf (cheated in); a
					// fellowship ability used only on purpose
					put("ottar", "1_80");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability only
					// Gandalf's Wisdom: a [gandalf] condition in the support area, which is no character; a discount only
					put("wisdom", "2_23");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 2 Eastfold (plains, no battleground), site 3 Barrows of Edoras (a sanctuary), site 4 White Rocks (a
	// battleground and nothing else; its Shadow number counts mounted companions), site 6 Caves of Aglarond
	// (underground and a sanctuary).
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("doom", "12_36");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: no text
					// Ent Avenger: a [gandalf] Ent companion who is no Gandalf; +2 strength only beside 3 Ents
					put("avenger", "6_27");
					// Ottar, Man of Laketown: a [gandalf] Man ally, home 3F; to play, spot Gandalf (cheated in); a
					// fellowship ability used only on purpose
					put("ottar", "1_80");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability only
					// Gandalf's Wisdom: a [gandalf] condition in the support area, which is no character; a discount only
					put("wisdom", "2_23");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 2 is Uruk Camp: a battleground and a plains; each Uruk-hai played there must exert (none is played).
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("doom", "12_36");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: no text
					// Ent Avenger: a [gandalf] Ent companion who is no Gandalf; +2 strength only beside 3 Ents
					put("avenger", "6_27");
					// Ottar, Man of Laketown: a [gandalf] Man ally, home 3F; to play, spot Gandalf (cheated in); a
					// fellowship ability used only on purpose
					put("ottar", "1_80");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability only
					// Gandalf's Wisdom: a [gandalf] condition in the support area, which is no character; a discount only
					put("wisdom", "2_23");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithDoomWeComeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: With Doom We Come
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While Gandalf is at an underground site, he gains <b>muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper><br>While the fellowship is at a battleground site, each [gandalf] character gains <b>muster</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("doom");

		assertEquals("With Doom We Come", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GandalfGainsMusterAtAnUndergroundSiteAndAnotherGandalfCompanionDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doom = scn.GetFreepsCard("doom");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");

		scn.StartGame();
		scn.SkipToSite(3);
		// Put down after the skip, whose regroup phases would stop on a muster offer.
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToSupportArea(doom);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, doom);

		// Rivendell Valley is no underground site.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(avenger, Keyword.MUSTER));

		scn.FreepsPass();

		// Balin's Tomb is.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(avenger, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAnUndergroundSanctuaryGandalfGainsMusterToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var doom = scn.GetFreepsCard("doom");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.FreepsRefreshDecision();

		// Caves of Aglarond, without the condition.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));

		scn.MoveCardsToSupportArea(doom);
		assertInZone(Zone.SUPPORT, doom);
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(avenger, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtABattlegroundEachGandalfCharacterGainsMusterAndNoOtherCardDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var doom = scn.GetFreepsCard("doom");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var ottar = scn.GetFreepsCard("ottar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wisdom = scn.GetFreepsCard("wisdom");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToSupportArea(doom, wisdom);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, doom, wisdom);

		// Barrows of Edoras is no battleground.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(avenger, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(ottar, Keyword.MUSTER));

		scn.FreepsPass();

		// White Rocks is.  The [gandalf] characters, a Wizard and an Ent companion and a Man ally, gain muster; a
		// companion of another culture and a [gandalf] card that is no character do not.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(avenger, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(ottar, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(wisdom, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(doom, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtUrukCampABattlegroundAndAPlainsEachGandalfCharacterGainsMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var doom = scn.GetFreepsCard("doom");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ottar = scn.GetFreepsCard("ottar");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToSupportArea(doom);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, doom);
		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(ottar, Keyword.MUSTER));

		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(ottar, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAPlainsThatIsNoBattlegroundNobodyGainsMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var doom = scn.GetFreepsCard("doom");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var ottar = scn.GetFreepsCard("ottar");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToSupportArea(doom);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, doom);
		scn.FreepsPass();

		// Eastfold: a plains, as Uruk Camp is, and no battleground (nor underground).
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(avenger, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(ottar, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
