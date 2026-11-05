package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_026_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow and
	// maneuver phases are at The Troll Hoard 30_50 (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spies", "33_26");

					put("braga", "33_25");      // Braga, Captain of the Guard: [men] Man, site 6; a Skirmish ability not used
					put("alfrid", "32_25");     // Alfrid, Lickspittle: [men] Man, cost 2, site 6; his texts need an [esgaroth] ally, and none is laid
					put("influence", "33_24");  // Alfrid's Influence: [men] condition, cost 1; a Shadow ability not used
					put("spear", "33_28");      // Spear of the Guard: [men] possession, cost 0, borne by a [men] Man
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] minion who is a Hobbit, site 2; abilities not used
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: [gundabad] Man, site 2; a Regroup ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, site 3; an Assignment ability not used
					put("footman", "30_42");    // Goblin Footman: [moria] Orc; its "when you play" offer does not answer a card cheated in
					put("marauder", "30_38");   // Orkish Marauder: [gundabad] Orc, cost 3; its "when you play" offer wants 4 [dwarven] followers
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GangsquadSpiesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Gangsquad Spies
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Fierce.
		 * 	The site number of each [men] Man is -3.
		 * 	Maneuver: Place an Orc from play beneath your draw deck to play a [men] card from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spies");

		assertEquals("Gangsquad Spies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SiteNumberOfEachMenManIsMinus3IncludingItsOwnAndNoOtherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var braga = scn.GetShadowCard("braga");
		var ted = scn.GetShadowCard("ted");
		var ferny = scn.GetShadowCard("ferny");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(spies);

		scn.StartGame();
		scn.MoveMinionsToTable(braga, ted, ferny, aggressor);
		// Enough for the Spies, roaming at site 2; not read.
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(6, scn.GetMinionSiteNumber(braga));
		assertEquals(2, scn.GetMinionSiteNumber(ted));
		assertEquals(2, scn.GetMinionSiteNumber(ferny));
		assertEquals(3, scn.GetMinionSiteNumber(aggressor));

		scn.ShadowPlayCard(spies);

		// Each [men] Man, the Spies among them: 6 - 3.
		assertInZone(Zone.SHADOW_CHARACTERS, spies);
		assertEquals(3, scn.GetMinionSiteNumber(spies));
		assertEquals(3, scn.GetMinionSiteNumber(braga));
		// A [men] Hobbit, a [gundabad] Man and an Orc keep theirs.
		assertEquals(2, scn.GetMinionSiteNumber(ted));
		assertEquals(2, scn.GetMinionSiteNumber(ferny));
		assertEquals(3, scn.GetMinionSiteNumber(aggressor));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverPlacesAnOrcOfEitherCultureToPlayAMenConditionOrPossessionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var braga = scn.GetShadowCard("braga");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		var alfrid = scn.GetShadowCard("alfrid");
		var influence = scn.GetShadowCard("influence");
		var spear = scn.GetShadowCard("spear");
		var marauder = scn.GetShadowCard("marauder");
		scn.MoveCardsToDiscard(alfrid, influence, spear, marauder);

		scn.StartGame();
		assertInZone(Zone.DISCARD, alfrid, influence, spear, marauder);
		scn.MoveMinionsToTable(spies, braga, aggressor, footman);
		// Enough for any play from the discard pile; the pool is not read.
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.DISCARD, alfrid, influence, spear, marauder);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(spies));
		var deck = scn.GetShadowDeckCount();
		scn.ShadowUseCardAction(spies);

		// An Orc of each culture; the Men are no Orcs.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, footman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(spies, braga));
		scn.ShadowChooseCard(footman);

		assertEquals(footman, scn.GetShadowBottomOfDeck());
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		// A [men] minion, condition and possession; the Marauder, which the pool could pay for, is no [men] card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(alfrid, influence, spear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(marauder));
		scn.ShadowChooseCard(influence);

		assertInZone(Zone.SUPPORT, influence);
		assertInZone(Zone.DISCARD, alfrid, spear, marauder);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Again, with the Aggressor: the possession, on a [men] Man.
		scn.FreepsPass();
		scn.ShadowUseCardAction(spies);
		scn.ShadowChooseCard(spear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(spies, braga));
		scn.ShadowChooseCard(braga);

		assertEquals(aggressor, scn.GetShadowBottomOfDeck());
		assertEquals(footman, scn.GetFromBottomOfShadowDeck(2));
		assertAttachedTo(spear, braga);
		assertInZone(Zone.DISCARD, alfrid, marauder);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverPlaysAMenMinionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var aggressor = scn.GetShadowCard("aggressor");
		var alfrid = scn.GetShadowCard("alfrid");
		scn.MoveCardsToDiscard(alfrid);

		scn.StartGame();
		assertInZone(Zone.DISCARD, alfrid);
		scn.MoveMinionsToTable(spies, aggressor);
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.DISCARD, alfrid);
		scn.ShadowUseCardAction(spies);

		// The Aggressor is the one Orc and Alfrid the one card to play: nothing is asked.
		assertEquals(aggressor, scn.GetShadowBottomOfDeck());
		assertInZone(Zone.SHADOW_CHARACTERS, alfrid);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverIsNotOfferedWithNoOrcInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var braga = scn.GetShadowCard("braga");
		var influence = scn.GetShadowCard("influence");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToDiscard(influence, aggressor);

		scn.StartGame();
		assertInZone(Zone.DISCARD, influence, aggressor);
		scn.MoveMinionsToTable(spies, braga);
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Only Men in play; the Orc lies in the discard pile.
		assertInZone(Zone.DISCARD, influence, aggressor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(spies));
	}

	@Test
	public void ManeuverIsNotOfferedWithNoMenCardInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var aggressor = scn.GetShadowCard("aggressor");
		var marauder = scn.GetShadowCard("marauder");
		scn.MoveCardsToDiscard(marauder);

		scn.StartGame();
		assertInZone(Zone.DISCARD, marauder);
		scn.MoveMinionsToTable(spies, aggressor);
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The play is a cost (Q-0062): an Orc to place, but nothing [men] to play.
		assertInZone(Zone.DISCARD, marauder);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(spies));
	}
}
