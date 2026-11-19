package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goaded", "12_67");
					put("goaded2", "12_67");
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, cost 1, site 2; abilities used only on purpose
					put("harad", "11_69");      // Axeman of Harad: [men] Man minion; a cost reduction only
					put("easterling", "4_224"); // Easterling Axeman: [raider] Man minion; +3 and fierce only at 2 burdens
				}},
				// Towers path: site 2 Eastfold (Plains), site 3 Barrows of Edoras (Sanctuary), site 4 White Rocks
				// (Battleground), site 5 Deeping Wall (Battleground).
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Uruk Camp as site 2: a battleground and a plains at once.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goaded", "12_67");
					put("goaded2", "12_67");
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, cost 1, site 2; abilities used only on purpose
					put("harad", "11_69");      // Axeman of Harad: [men] Man minion; a cost reduction only
					put("easterling", "4_224"); // Easterling Axeman: [raider] Man minion; +3 and fierce only at 2 burdens
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Shadow number 1; each Uruk-hai played must exert
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoadedtoWarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Goaded to War
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [men] minion.<br>Each time the fellowship moves to a battleground or plains site, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goaded");

		assertEquals("Goaded to War", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAMenMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goaded = scn.GetShadowCard("goaded");
		var goaded2 = scn.GetShadowCard("goaded2");
		var ted = scn.GetShadowCard("ted");
		var easterling = scn.GetShadowCard("easterling");

		scn.StartGame();
		scn.SetShadowHand(goaded, ted);
		// A Man minion of another culture, and a [men] card that is no minion.
		scn.MoveMinionsToTable(easterling);
		scn.MoveCardsToSupportArea(goaded2);
		assertInHand(goaded, ted);
		assertInZone(Zone.SHADOW_CHARACTERS, easterling);
		assertInZone(Zone.SUPPORT, goaded2);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertFalse(scn.ShadowPlayAvailable(goaded));
		// Ted Sandyman is a [men] minion who is a Hobbit; site 2 is his own, so he does not roam.
		scn.ShadowPlayCard(ted);
		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertTrue(scn.ShadowPlayAvailable(goaded));
		scn.ShadowPlayCard(goaded);
		assertInZone(Zone.SUPPORT, goaded);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMenManIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goaded = scn.GetShadowCard("goaded");
		var harad = scn.GetShadowCard("harad");

		scn.StartGame();
		scn.SetShadowHand(goaded);
		scn.MoveMinionsToTable(harad);
		assertInHand(goaded);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertTrue(scn.ShadowPlayAvailable(goaded));
		scn.ShadowPlayCard(goaded);
		assertInZone(Zone.SUPPORT, goaded);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AddsOneWhenTheFellowshipMovesToAPlainsSiteAndNothingWhenItMovesFromOneToASiteThatIsNeither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goaded = scn.GetShadowCard("goaded");
		scn.MoveCardsToSupportArea(goaded);
		assertInZone(Zone.SUPPORT, goaded);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Eastfold, a plains: 2 for the site + 1 for Frodo + 1 for this card.
		assertEquals(4, scn.GetTwilight());

		// No minions, and the regroup move to Barrows of Edoras, a sanctuary with no terrain.
		scn.SkipToMovementDecision();
		assertEquals(4, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 4 + 0 for the site + 1 for Frodo, and nothing for leaving a plains.
		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void AddsOneOnlyForASiteThatIsBothABattlegroundAndAPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var goaded = scn.GetShadowCard("goaded");
		scn.MoveCardsToSupportArea(goaded);
		assertInZone(Zone.SUPPORT, goaded);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Uruk Camp: 1 for the site + 1 for Frodo + 1 for this card, once.
		assertEquals(3, scn.GetTwilight());
	}

	@Test
	public void AddsOneEachTimeTheFellowshipMovesToABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goaded = scn.GetShadowCard("goaded");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(goaded);
		assertInZone(Zone.SUPPORT, goaded);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// White Rocks, a battleground only: 3 for the site + 1 for Frodo + 1 for this card.
		assertEquals(5, scn.GetTwilight());

		scn.SkipToMovementDecision();
		assertEquals(5, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Deeping Wall, another battleground: 5 + 7 for the site + 1 for Frodo + 1 for this card again.
		assertEquals(14, scn.GetTwilight());
	}
}
