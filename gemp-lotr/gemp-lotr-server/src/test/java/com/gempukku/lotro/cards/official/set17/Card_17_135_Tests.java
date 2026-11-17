package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_135_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran", "17_135");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two [uruk-hai] events in the Shadow player's discard pile, and the cards beside them that are not.
	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran1", "17_135");
					put("veteran2", "17_135");
					put("ghastly", "18_117");   // Ghastly Wound: an [uruk-hai] skirmish event
					put("forges", "18_120");    // New Forges Built: an [uruk-hai] maneuver event
					put("spear", "17_117");     // Spear of the White Hand: an [uruk-hai] possession
					put("bred", "1_121");       // Bred for Battle: an [isengard] skirmish event
					put("fear", "17_137");      // You Do Not Know Fear: an [uruk-hai] event, the Free Peoples player's copy in his own discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandVeteranStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Veteran
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion, if you control a site, you may take an [uruk-hai] event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veteran");

		assertEquals("White Hand Veteran", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWhileItsPlayerControlsASiteItTakesAnUrukHaiEventIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var fear = scn.GetFreepsCard("fear");
		scn.MoveCardsToDiscard(fear);
		assertInDiscard(fear);

		var veteran = scn.GetShadowCard("veteran1");
		var ghastly = scn.GetShadowCard("ghastly");
		var forges = scn.GetShadowCard("forges");
		var spear = scn.GetShadowCard("spear");
		var bred = scn.GetShadowCard("bred");
		scn.MoveCardsToHand(veteran);
		scn.MoveCardsToDiscard(ghastly, forges, spear, bred);
		assertInDiscard(ghastly, forges, spear, bred);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInDiscard(ghastly, forges, spear, bred, fear);
		assertTrue(scn.ShadowPlayAvailable(veteran));
		scn.ShadowPlayCard(veteran);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(veteran));
		scn.ShadowAcceptOptionalTrigger();
		// Either [uruk-hai] event; not the [uruk-hai] possession, the [isengard] event, or the Free Peoples
		// player's [uruk-hai] event in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ghastly, forges));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(spear, bred, fear));
		scn.ShadowChooseCard(ghastly);

		assertInHand(ghastly);
		assertInDiscard(forges, spear, bred, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheTakeMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var veteran = scn.GetShadowCard("veteran1");
		var ghastly = scn.GetShadowCard("ghastly");
		var forges = scn.GetShadowCard("forges");
		scn.MoveCardsToHand(veteran);
		scn.MoveCardsToDiscard(ghastly, forges);
		assertInDiscard(ghastly, forges);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInDiscard(ghastly, forges);
		scn.ShadowPlayCard(veteran);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(veteran));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(ghastly, forges);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyTheFreePeoplesPlayerControllingASiteNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var veteran = scn.GetShadowCard("veteran1");
		var ghastly = scn.GetShadowCard("ghastly");
		scn.MoveCardsToHand(veteran);
		scn.MoveCardsToDiscard(ghastly);
		assertInDiscard(ghastly);

		scn.StartGame();
		scn.SkipToSite(2);
		// A controlled site, but not one of the Veteran's player's.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInDiscard(ghastly);
		scn.ShadowPlayCard(veteran);

		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(veteran));
		assertInDiscard(ghastly);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUrukHaiEventInItsPlayersDiscardPileNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var fear = scn.GetFreepsCard("fear");
		scn.MoveCardsToDiscard(fear);
		assertInDiscard(fear);

		var veteran = scn.GetShadowCard("veteran1");
		var spear = scn.GetShadowCard("spear");
		var bred = scn.GetShadowCard("bred");
		scn.MoveCardsToHand(veteran);
		scn.MoveCardsToDiscard(spear, bred);
		assertInDiscard(spear, bred);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInDiscard(spear, bred, fear);
		scn.ShadowPlayCard(veteran);

		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(veteran));
		assertInDiscard(spear, bred, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherVeteranPlayedLaterSetsOffOnlyItsOwnTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var veteran1 = scn.GetShadowCard("veteran1");
		var veteran2 = scn.GetShadowCard("veteran2");
		var ghastly = scn.GetShadowCard("ghastly");
		scn.MoveCardsToHand(veteran2);
		scn.MoveCardsToDiscard(ghastly);
		assertInDiscard(ghastly);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		// The first Veteran is already in play, laid ahead of the Shadow phase.
		scn.MoveMinionsToTable(veteran1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInDiscard(ghastly);
		scn.ShadowPlayCard(veteran2);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(veteran2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(veteran1));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(ghastly);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
