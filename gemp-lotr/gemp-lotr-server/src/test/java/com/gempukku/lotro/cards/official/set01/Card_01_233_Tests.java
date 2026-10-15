package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_233_Tests
{

	// The Fellowship block format on the Fellowship sites: the one format the ruling on this card lets it work in.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "1_233");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	// The Open format's kind of table: the expanded format on a Shadows-block path, whose sites carry no number
	// until they are played.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "1_233");
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// The Fellowship sites in the default multipath format, which is no Fellowship block format.
	protected VirtualTableScenario GetMultipathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "1_233");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Another block's numbered path: King-block sites in the multipath format.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "1_233");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Úlairë Nelya, Lieutenant of Morgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: <b>Fierce</b>.<br><b>Shadow:</b> Exert Úlairë Nelya and spot an opponent's site to replace it with your site of the same number.
		*/

		var scn = GetScenario();

		var nelya = scn.GetFreepsCard("nelya");

		assertEquals("Úlairë Nelya", nelya.getBlueprint().getTitle());
		assertEquals("Lieutenant of Morgul", nelya.getBlueprint().getSubtitle());
		assertTrue(nelya.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, nelya.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, nelya.getBlueprint().getCulture());
		assertEquals(CardType.MINION, nelya.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, nelya.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertEquals(5, nelya.getBlueprint().getTwilightCost());
		assertEquals(10, nelya.getBlueprint().getStrength());
		assertEquals(3, nelya.getBlueprint().getVitality());
		assertEquals(2, nelya.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsNelyaToReplaceTheOpponentsSiteAndIsNotOfferedOnceNoneIsLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var freepsSite1 = scn.GetFreepsSite(1);
		var shadowSite1 = scn.GetShadowSite(1);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		// The move lays the Shadow player's own site 2: the Free Peoples player's site 1 is the one opponent's site.
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite1);
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(shadowSite1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_PATH, shadowSite1);
		assertInZone(Zone.ADVENTURE_DECK, freepsSite1);
		assertSame(shadowSite2, scn.GetSite(2));

		// Nelya can still exert (1 wound of 3), but no site on the path is the opponent's now.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nelya));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileNelyaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var freepsSite1 = scn.GetFreepsSite(1);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(nelya, 2);
		assertEquals(2, scn.GetWoundsOn(nelya));
		scn.FreepsPass();

		assertSame(freepsSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nelya));
		assertEquals(2, scn.GetWoundsOn(nelya));
	}

	@Test
	public void OnlyTheOpponentsSitesAreOfferedAndOnlyTheOneChosenIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var freepsSite1 = scn.GetFreepsSite(1);
		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		// The Free Peoples player's own site 2 lies next, as where he played it himself, and the fellowship
		// moves onto it.
		scn.MoveCardToAdventurePath(freepsSite2);
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();
		// The Shadow player's own site 3 lies ahead.
		scn.MoveCardToAdventurePath(shadowSite3);
		scn.ShadowRefreshDecision();

		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(freepsSite2, scn.GetSite(2));
		assertSame(shadowSite3, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.ShadowUseCardAction(nelya);

		// The site behind the fellowship and the site it is at are both the opponent's; the site ahead is his own.
		assertTrue(scn.ShadowHasCardChoiceAvailable(freepsSite1, freepsSite2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shadowSite3));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(freepsSite2);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, shadowSite2);
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2);
		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(shadowSite3, scn.GetSite(3));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnAShadowsBlockPathTheAbilityReplacesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var nelya = scn.GetShadowCard("nelya");
		var freepsSite1 = scn.GetFreepsSite("site1");
		var shadowSite1 = scn.GetShadowSite("site1");
		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame(freepsSite1);
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowSite2);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite1);
		assertEquals(0, scn.GetWoundsOn(nelya));

		// The ability has a cost, so it is offered and paid; no site in the Shadow player's adventure deck has a
		// number, so none is his site of the same number.
		scn.ShadowUseCardAction(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(freepsSite1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnAnotherBlocksNumberedPathTheAbilityReplacesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var nelya = scn.GetShadowCard("nelya");
		var freepsSite1 = scn.GetFreepsSite(1);
		var shadowSite1 = scn.GetShadowSite(1);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite1);
		assertEquals(0, scn.GetWoundsOn(nelya));

		// "Your site of the same number" on a set 1 card is a Fellowship-block site: the Shadow player's King-block
		// site 1 is not one, so nothing is replaced, though the cost is paid.
		scn.ShadowUseCardAction(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(freepsSite1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InTheMultipathFormatOnTheFellowshipPathItReplacesTheSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var nelya = scn.GetShadowCard("nelya");
		var freepsSite1 = scn.GetFreepsSite(1);
		var shadowSite1 = scn.GetShadowSite(1);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(freepsSite1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite1);
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.ShadowUseCardAction(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(shadowSite1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
