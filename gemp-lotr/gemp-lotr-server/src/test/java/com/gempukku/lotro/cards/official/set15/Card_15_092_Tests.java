package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_92");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The card's own block: a path whose sites are laid from the adventure decks as the game goes.
	protected VirtualTableScenario GetReplacedSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("southron", "15_92");
					// Corps of Harad: a [men] Man of vitality 3; fierce only at a battleground, and none of
					// these sites is one
					put("corps1", "11_73");
					put("corps2", "11_73");
				}},
				// Nine different sites: site1 is Fangorn Glade and site2 Abandoned Mine Shaft.  None of them
				// does anything when it replaces site 2 in a maneuver phase.
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void SavageSouthronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Savage Southron
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> If the Free People's player has at least 1 site in his or her adventure deck, exert 2 [men] Men to randomly choose a site from that deck. Replace the fellowship's current site with that site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Savage Southron", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsTwoMenMenToReplaceTheCurrentSiteWithOneFromTheFreePeoplesAdventureDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReplacedSiteScenario();

		var southron = scn.GetShadowCard("southron");
		var corps1 = scn.GetShadowCard("corps1");
		var corps2 = scn.GetShadowCard("corps2");
		var shaft = scn.GetShadowSite("site2");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(southron, corps1, corps2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The fellowship is at the Shadow player's site, and the Free Peoples player's other eight sites are
		// in his adventure deck.
		assertEquals(shaft, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		PhysicalCardImpl[] freepsSites = {
				scn.GetFreepsSite("site2"), scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"),
				scn.GetFreepsSite("site5"), scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"),
				scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9")
		};
		assertInZone(Zone.ADVENTURE_DECK, freepsSites);
		assertTrue(scn.ShadowActionAvailable(southron));

		scn.ShadowUseCardAction(southron);
		// "Exert 2 [men] Men": three can, and two are chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(southron, corps1, corps2));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(corps1, corps2);
		assertEquals(1, scn.GetWoundsOn(corps1));
		assertEquals(1, scn.GetWoundsOn(corps2));
		assertEquals(0, scn.GetWoundsOn(southron));

		// The game shows both players the site it has picked before it is laid.
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// Which site is chance.  It is one of the eight that were in the Free Peoples player's adventure deck,
		// it lies where the Shadow player's did, and the other seven are in that deck still.  One time in
		// eight it is his own Abandoned Mine Shaft, the twin of the site replaced, which the game allows as
		// built (Q-0226, open: under its answer B a twin may not replace a twin, and this test wants
		// looking at).
		var laid = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNotSame(shaft, laid);
		assertEquals(scn.P1(), laid.getOwner());
		assertInZone(Zone.ADVENTURE_PATH, laid);
		assertInZone(Zone.ADVENTURE_DECK, shaft);
		int laidOfTheEight = 0;
		for (var site : freepsSites) {
			if (site == laid)
				laidOfTheEight++;
			else
				assertInZone(Zone.ADVENTURE_DECK, site);
		}
		assertEquals(1, laidOfTheEight);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsOfferedWithOneSiteLeftInTheFreePeoplesAdventureDeckAndNotWithNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReplacedSiteScenario();

		var southron = scn.GetShadowCard("southron");
		var corps1 = scn.GetShadowCard("corps1");
		var corps2 = scn.GetShadowCard("corps2");
		var shaft = scn.GetShadowSite("site2");
		var last = scn.GetFreepsSite("site5");
		PhysicalCardImpl[] otherFreepsSites = {
				scn.GetFreepsSite("site2"), scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"),
				scn.GetFreepsSite("site9")
		};

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(southron, corps1, corps2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// "At least 1 site": one site is left in the Free Peoples player's adventure deck, the board late in a
		// game in which the sites laid have been his own.  The ability is offered.
		scn.SetFreepsAdventureDeck(last);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.ADVENTURE_DECK, last);
		assertNotInZone(Zone.ADVENTURE_DECK, otherFreepsSites);
		assertTrue(scn.ShadowActionAvailable(southron));

		// None is left there.  All three [men] Men are unwounded, so two could still exert, and the Shadow
		// player's own adventure deck holds his other sites: it is the Free Peoples player's deck that is read,
		// and the ability is not offered.
		scn.SetFreepsAdventureDeck();
		scn.ShadowRefreshDecision();
		assertNotInZone(Zone.ADVENTURE_DECK, last);
		assertNotInZone(Zone.ADVENTURE_DECK, otherFreepsSites);
		assertInZone(Zone.ADVENTURE_DECK, scn.GetShadowSite("site3"), scn.GetShadowSite("site9"));
		assertEquals(0, scn.GetWoundsOn(southron));
		assertEquals(0, scn.GetWoundsOn(corps1));
		assertEquals(0, scn.GetWoundsOn(corps2));
		assertFalse(scn.ShadowActionAvailable(southron));
		assertEquals(shaft, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
