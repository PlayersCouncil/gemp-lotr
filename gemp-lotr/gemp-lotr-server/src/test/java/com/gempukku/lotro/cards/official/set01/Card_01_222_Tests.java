package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_222_Tests
{

	// The Fellowship block format, the one the card's ruling names.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("paths", "1_222");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl; his text reads only companions bearing [wraith] conditions
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his text needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl; his text needs 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	// The Fellowship block format, with a second site 2 put in each player's adventure deck; each test makes the
	// first player's site 2 Trollshaw Forest and the second player's Ettenmoors.
	protected VirtualTableScenario GetTrollshawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("paths", "1_222");
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl; his text needs 2 wounds on the Ring-bearer
					put("trollshaw", "1_334");  // Trollshaw Forest: a Fellowship-block site 2 of another title than Ettenmoors; its text answers only possessions played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	// The default format, multipath, on the same Fellowship-block path, with the second site 2 as above.
	protected VirtualTableScenario GetMultipathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("paths", "1_222");
					put("trollshaw", "1_334");  // Trollshaw Forest: a Fellowship-block site 2 of another title than Ettenmoors; its text answers only possessions played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows-block path, whose sites have no number until they are played.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("paths", "1_222");
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void PathsSeldomTroddenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Paths Seldom Trodden
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. To play, spot a Nazgûl. Plays to your support area.
		 * <b>Shadow:</b> Remove (3) to replace the fellowship's site with your version of the same site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("paths");

		assertEquals("Paths Seldom Trodden", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilANazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetShadowCard("paths");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();

		scn.MoveMinionsToTable(hound);
		scn.MoveCardsToHand(paths, lemenya);
		scn.SetTwilight(10);
		assertInHand(paths, lemenya);

		scn.FreepsPass();

		// A [wraith] minion who is no Nazgûl is not enough.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(paths));

		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowPlayAvailable(paths));
		scn.ShadowPlayCard(paths);

		assertInZone(Zone.SUPPORT, paths);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulOfAnotherCultureIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetShadowCard("paths");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();

		scn.MoveMinionsToTable(khamul);
		scn.MoveCardsToHand(paths);
		scn.SetTwilight(10);
		assertInHand(paths);

		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(paths));
		scn.ShadowPlayCard(paths);

		assertInZone(Zone.SUPPORT, paths);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionRemoves3ToReplaceTheFellowshipsSiteWithYourSiteOfTheSameNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTrollshawScenario();

		var paths = scn.GetShadowCard("paths");
		var trollshaw = scn.GetFreepsCard("trollshaw");
		var freepsSite1 = scn.GetFreepsSite(1);
		var shadowSite2 = scn.GetShadowSite("site2");
		var shadowSite3 = scn.GetShadowSite("site3");

		scn.StartGame();

		// The first player's site 2 is Trollshaw Forest and the second player's Ettenmoors, and the first player
		// lays his himself (as a card that plays the next site would).
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.SetShadowAdventureDeck(scn.GetShadowSite("site1"), scn.GetShadowSite("site2"), scn.GetShadowSite("site3"),
				scn.GetShadowSite("site4"), scn.GetShadowSite("site5"), scn.GetShadowSite("site6"), scn.GetShadowSite("site7"),
				scn.GetShadowSite("site8"), scn.GetShadowSite("site9"));
		scn.MoveCardToAdventurePath(trollshaw);
		scn.MoveCardsToSupportArea(paths);
		scn.SetTwilight(10);
		assertSame(trollshaw, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2, shadowSite3);

		scn.FreepsPass();

		assertSame(trollshaw, scn.GetCurrentSite());
		// 10 + 1 for Trollshaw Forest + 1 companion
		assertEquals(12, scn.GetTwilight());

		assertTrue(scn.ShadowActionAvailable(paths));
		scn.ShadowUseCardAction(paths);

		assertEquals(9, scn.GetTwilight());
		// The Shadow player's own site of the same number lies where the fellowship is, and the fellowship is
		// still at site 2; the old site is back in its owner's adventure deck.
		assertSame(shadowSite2, scn.GetSite(2));
		assertSame(shadowSite2, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_PATH, shadowSite2);
		assertInZone(Zone.ADVENTURE_DECK, trollshaw);
		// Neither another site of his nor another site on the path is touched.
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertSame(freepsSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtHisOwnSiteTheTwilightIsRemovedAndNothingIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetShadowCard("paths");
		var freepsSite2 = scn.GetFreepsSite("site2");
		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame();

		scn.MoveCardsToSupportArea(paths);
		scn.SetTwilight(10);
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2, shadowSite2);

		// The move lays the Shadow player's own site 2.
		scn.FreepsPass();

		assertSame(shadowSite2, scn.GetCurrentSite());
		// 10 + 2 for Ettenmoors + 1 companion
		assertEquals(13, scn.GetTwilight());

		// The ability has a cost, and is offered; his version of the site is already there, and the other
		// player's is not his.
		assertTrue(scn.ShadowActionAvailable(paths));
		scn.ShadowUseCardAction(paths);

		assertEquals(10, scn.GetTwilight());
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithLessThan3Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTrollshawScenario();

		var paths = scn.GetShadowCard("paths");
		var trollshaw = scn.GetFreepsCard("trollshaw");

		scn.StartGame();

		scn.SetFreepsAdventureDeck(scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.SetShadowAdventureDeck(scn.GetShadowSite("site1"), scn.GetShadowSite("site2"), scn.GetShadowSite("site3"),
				scn.GetShadowSite("site4"), scn.GetShadowSite("site5"), scn.GetShadowSite("site6"), scn.GetShadowSite("site7"),
				scn.GetShadowSite("site8"), scn.GetShadowSite("site9"));
		scn.MoveCardToAdventurePath(trollshaw);
		scn.MoveCardsToSupportArea(paths);
		assertSame(trollshaw, scn.GetSite(2));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		// 0 + 1 for Trollshaw Forest + 1 companion
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(paths));
		assertSame(trollshaw, scn.GetCurrentSite());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTrollshawScenario();

		var paths = scn.GetShadowCard("paths");
		var hound = scn.GetShadowCard("hound");
		var trollshaw = scn.GetFreepsCard("trollshaw");

		scn.StartGame();

		scn.SetFreepsAdventureDeck(scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.SetShadowAdventureDeck(scn.GetShadowSite("site1"), scn.GetShadowSite("site2"), scn.GetShadowSite("site3"),
				scn.GetShadowSite("site4"), scn.GetShadowSite("site5"), scn.GetShadowSite("site6"), scn.GetShadowSite("site7"),
				scn.GetShadowSite("site8"), scn.GetShadowSite("site9"));
		scn.MoveCardToAdventurePath(trollshaw);
		scn.MoveCardsToSupportArea(paths);
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(10);
		assertSame(trollshaw, scn.GetSite(2));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 10 + 1 for Trollshaw Forest + 1 companion
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(paths));
		assertSame(trollshaw, scn.GetCurrentSite());
	}

	@Test
	public void OnAShadowsBlockPathNothingIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var paths = scn.GetShadowCard("paths");
		var freepsSite2 = scn.GetFreepsSite("site2");
		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame(scn.GetFreepsSite("site1"));

		// The first player's own site lies next, and the fellowship moves to it.
		scn.MoveCardToAdventurePath(freepsSite2);
		scn.MoveCardsToSupportArea(paths);
		scn.SetTwilight(10);
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);

		scn.FreepsPass();

		assertSame(freepsSite2, scn.GetCurrentSite());
		// 10 + 0 for Abandoned Mine Shaft + 1 companion
		assertEquals(11, scn.GetTwilight());

		// The ability has a cost, and is offered; the Shadow player's adventure deck holds no site of the same
		// number (his copy of Abandoned Mine Shaft has none until it is played), and nothing is replaced.
		assertTrue(scn.ShadowActionAvailable(paths));
		scn.ShadowUseCardAction(paths);

		assertEquals(8, scn.GetTwilight());
		assertSame(freepsSite2, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InTheMultipathFormatOnTheFellowshipPathItReplacesTheSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var paths = scn.GetShadowCard("paths");
		var trollshaw = scn.GetFreepsCard("trollshaw");
		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame();

		scn.SetFreepsAdventureDeck(scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.SetShadowAdventureDeck(scn.GetShadowSite("site1"), scn.GetShadowSite("site2"), scn.GetShadowSite("site3"),
				scn.GetShadowSite("site4"), scn.GetShadowSite("site5"), scn.GetShadowSite("site6"), scn.GetShadowSite("site7"),
				scn.GetShadowSite("site8"), scn.GetShadowSite("site9"));
		scn.MoveCardToAdventurePath(trollshaw);
		scn.MoveCardsToSupportArea(paths);
		scn.SetTwilight(10);
		assertSame(trollshaw, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);

		scn.FreepsPass();

		assertSame(trollshaw, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(paths));
		scn.ShadowUseCardAction(paths);

		assertSame(shadowSite2, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, trollshaw);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
