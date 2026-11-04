package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armed", "11_106");

					// Denizen of Khazad-dum: an [orc] Orc minion, cost 3, site 4.  Strength +2 only at an underground
					// site, and no site of this path is one.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc.  Its trigger is at the start of the
					// assignment phase, never reached.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc minion, no text.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] condition (support area).  Its corruption needs 5 burdens and an
					// empty Free Peoples draw deck.
					put("beckoning", "12_83");

					// Bladed Gauntlets: an [orc] possession (armor), cost 1, "Bearer must be an [orc] minion".  Its
					// skirmish ability is never used.
					put("gauntlets", "11_109");
					// Grond, Forged With Black Steel: an [orc] possession that plays to the support area, cost 3,
					// "To play, spot an [orc] minion".  Its trigger is on a regroup move, never reached.
					put("grond", "18_82");
					// Rider's Gear: an [orc] possession (armor), cost 1, "Bearer must be an [orc] Orc".  Its text is
					// for a mounted bearer.
					put("gear", "17_90");
					// Spider Poison: a [gollum] possession that lies in the support area, cost 1.  Its skirmish ability
					// and its trigger on a bearer are never reached.
					put("poison", "8_28");
				}},
				// Site 2 is Eastfold 4_331 (plains, no battleground); site 4 is White Rocks 4_346 (battleground).
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armed", "11_106");

					// Denizen of Khazad-dum: an [orc] Orc minion, cost 3, site 4.  Strength +2 only at an underground
					// site, and no site of this path is one.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc.  Its trigger is at the start of the
					// assignment phase, never reached.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc minion, no text.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] condition (support area).  Its corruption needs 5 burdens and an
					// empty Free Peoples draw deck.
					put("beckoning", "12_83");

					// Bladed Gauntlets: an [orc] possession (armor), cost 1, "Bearer must be an [orc] minion".  Its
					// skirmish ability is never used.
					put("gauntlets", "11_109");
					// Grond, Forged With Black Steel: an [orc] possession that plays to the support area, cost 3,
					// "To play, spot an [orc] minion".  Its trigger is on a regroup move, never reached.
					put("grond", "18_82");
					// Rider's Gear: an [orc] possession (armor), cost 1, "Bearer must be an [orc] Orc".  Its text is
					// for a mounted bearer.
					put("gear", "17_90");
					// Spider Poison: a [gollum] possession that lies in the support area, cost 1.  Its skirmish ability
					// and its trigger on a bearer are never reached.
					put("poison", "8_28");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					// Uruk Camp: a battleground that is a plains as well.  Its trigger answers an Uruk-hai played.
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmedforBattleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Armed for Battle
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot your [orc] minion to play an [orc] possession from your discard pile (or, if that minion is at a battleground site, from your draw deck or discard pile).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armed");

		assertEquals("Armed for Battle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AwayFromABattlegroundPlaysAnOrcPossessionFromTheDiscardPileOntoTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freepsGear = scn.GetFreepsCard("gear");
		scn.MoveCardsToFreepsDiscard("gear");

		var armed = scn.GetShadowCard("armed");
		var denizen = scn.GetShadowCard("denizen");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var grond = scn.GetShadowCard("grond");
		var beckoning = scn.GetShadowCard("beckoning");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.SetShadowHand(armed);
		scn.MoveCardsToShadowDiscard("gauntlets", "grond", "beckoning", "poison");
		scn.MoveMinionsToTable(denizen);
		assertInDiscard(gauntlets, grond, beckoning, poison, freepsGear);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		scn.FreepsPass();

		// Eastfold, site 2: a plains, and no battleground.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(armed));

		scn.ShadowPlayCard(armed);
		// Two [orc] possessions, borne and in the support area; not an [orc] condition, a [gollum] possession,
		// nor the Free Peoples player's [orc] possession in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gauntlets, grond));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(beckoning, poison, freepsGear));
		scn.ShadowChooseCardBPFromSelection(gauntlets);

		// The Denizen is the one bearer the Gauntlets can have, so he gets them unasked.
		assertAttachedTo(gauntlets, denizen);
		assertInDiscard(armed, grond, beckoning, poison, freepsGear);
		// No draw deck was looked through.
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcMinionWhoIsNoOrcIsSpottedAndAPossessionGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armed = scn.GetShadowCard("armed");
		var halftroll = scn.GetShadowCard("halftroll");
		var grond = scn.GetShadowCard("grond");

		scn.StartGame();
		scn.SetShadowHand(armed);
		scn.MoveCardsToShadowDiscard("grond");
		scn.MoveMinionsToTable(halftroll);
		assertInDiscard(grond);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(armed));

		scn.ShadowPlayCard(armed);
		assertInZone(Zone.SUPPORT, grond);
		assertInDiscard(armed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armed = scn.GetShadowCard("armed");
		var denizen = scn.GetShadowCard("denizen");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		var gauntlets = scn.GetShadowCard("gauntlets");

		scn.StartGame();
		scn.SetShadowHand(armed, denizen);
		scn.MoveCardsToShadowDiscard("gauntlets");
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(beckoning);
		assertInDiscard(gauntlets);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A [moria] Orc minion and an [orc] condition: no [orc] minion to spot.
		assertFalse(scn.ShadowPlayAvailable(armed));

		scn.ShadowPlayCard(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertTrue(scn.ShadowPlayAvailable(armed));

		scn.ShadowPlayCard(armed);
		assertAttachedTo(gauntlets, denizen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armed = scn.GetShadowCard("armed");
		var denizen = scn.GetShadowCard("denizen");
		var gauntlets = scn.GetShadowCard("gauntlets");

		scn.StartGame();
		scn.SetShadowHand(armed);
		scn.MoveCardsToShadowDiscard("gauntlets");
		scn.MoveMinionsToTable(denizen);
		assertInDiscard(gauntlets);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(armed));
	}

	@Test
	public void CannotBePlayedWhenThePossessionInTheDiscardPileCannotBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armed = scn.GetShadowCard("armed");
		var denizen = scn.GetShadowCard("denizen");
		var grond = scn.GetShadowCard("grond");

		scn.StartGame();
		scn.SetShadowHand(armed);
		scn.MoveCardsToShadowDiscard("grond");
		scn.MoveMinionsToTable(denizen);
		assertInDiscard(grond);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		scn.FreepsPass();

		// Grond costs 3.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(armed));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(armed));

		scn.ShadowPlayCard(armed);
		assertInZone(Zone.SUPPORT, grond);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtABattlegroundPlaysAnOrcPossessionFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armed = scn.GetShadowCard("armed");
		var denizen = scn.GetShadowCard("denizen");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var grond = scn.GetShadowCard("grond");
		var beckoning = scn.GetShadowCard("beckoning");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(armed);
		scn.MoveMinionsToTable(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		// The possessions lie in the Shadow player's draw deck, and nothing in his discard pile.
		assertInZone(Zone.DECK, gauntlets, grond, beckoning, poison);
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.FreepsPass();

		// White Rocks, site 4: a battleground.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(armed));

		scn.ShadowPlayCard(armed);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gauntlets, grond));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(beckoning, poison));
		scn.ShadowChooseCardBPFromSelection(gauntlets);

		assertAttachedTo(gauntlets, denizen);
		assertInDiscard(armed);
		// Once as the Gauntlets leave the deck that was looked through, and once more after the play.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AwayFromABattlegroundAPossessionInTheDrawDeckAloneCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armed = scn.GetShadowCard("armed");
		var denizen = scn.GetShadowCard("denizen");
		var gauntlets = scn.GetShadowCard("gauntlets");

		scn.StartGame();
		scn.SetShadowHand(armed);
		scn.MoveMinionsToTable(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertInZone(Zone.DECK, gauntlets);
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(armed));
	}

	@Test
	public void AtABattlegroundThatIsAlsoAPlainsEitherPileMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var armed = scn.GetShadowCard("armed");
		var denizen = scn.GetShadowCard("denizen");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var grond = scn.GetShadowCard("grond");

		scn.StartGame();
		scn.SetShadowHand(armed);
		scn.MoveCardsToShadowDiscard("gauntlets");
		scn.MoveMinionsToTable(denizen);
		assertInDiscard(gauntlets);
		assertInZone(Zone.DECK, grond);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		scn.FreepsPass();

		// Uruk Camp, site 2: a battleground and a plains.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(armed));

		scn.ShadowPlayCard(armed);
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		scn.ShadowChoose("discard pile");

		assertAttachedTo(gauntlets, denizen);
		assertInZone(Zone.DECK, grond);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
