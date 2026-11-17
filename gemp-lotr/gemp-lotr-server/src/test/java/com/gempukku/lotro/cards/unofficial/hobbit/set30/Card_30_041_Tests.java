package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_041_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase
	// of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("yazneg", "30_41");
					// Thorin, Oakenshield: [dwarven] Dwarf companion; +1 strength for each [dwarven] follower he
					// bears and a fellowship ability, neither reaching a test
					put("thorin", "30_19");
					// Bombur, Chief Cook: [dwarven] follower, borne by Thorin; bearer is damage +1
					put("bombur", "30_7");
					// Old Thrush, Messenger Bird: [shire] follower, borne by Bilbo; a maneuver ability of the
					// Free Peoples player's, not used
					put("thrush", "31_42");
					// Saruman, Servant of Sauron: an [isengard] follower of the Shadow player's, in his support
					// area; its Aid costs (1), so with the pool empty at the start of the maneuver phase it is not offered
					put("saruman", "17_38");
					// Dwarven Ponies: a [dwarven] possession in the support area, no follower; texts on playing
					// Bifur, Bofur or Bombur and a fellowship ability
					put("ponies", "33_6");
					// Dwarven Axe: a [dwarven] possession borne by Thorin, no follower; damage +1 only at a mountain
					put("axe", "31_1");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void YaznegStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Yazneg, Orkish Assassin
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Maneuver:</b> Remove (3) and discard this minion from play to discard a follower.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("yazneg");

		assertEquals("Yazneg", card.getBlueprint().getTitle());
		assertEquals("Orkish Assassin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverActionRemoves3AndDiscardsYaznegToDiscardAFollowerOfEitherSideBorneOrNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var yazneg = scn.GetShadowCard("yazneg");
		var saruman = scn.GetShadowCard("saruman");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var bombur = scn.GetFreepsCard("bombur");
		var thrush = scn.GetFreepsCard("thrush");
		var ponies = scn.GetFreepsCard("ponies");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, bombur, axe);
		scn.AttachCardsTo(bilbo, thrush);
		scn.MoveCardsToSupportArea(ponies, saruman);

		scn.StartGame();
		assertAttachedTo(bombur, thorin);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(thrush, bilbo);
		assertInZone(Zone.SUPPORT, ponies, saruman);
		scn.MoveMinionsToTable(yazneg);
		scn.FreepsPass();
		// Nothing in the pool as the maneuver phase starts, so Saruman's Aid of (1) is not offered.
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);
		// The Free Peoples followers' Aids are offered, borne as they are: declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(yazneg));
		scn.ShadowUseCardAction(yazneg);

		// Both costs paid: the pool emptied and Yazneg discarded.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(yazneg);
		// A follower borne by a companion, one borne by the Ring-bearer, and the Shadow player's own in his
		// support area; not the possessions, borne or in the support area.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bombur, thrush, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ponies, axe));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(thrush);

		assertInDiscard(thrush);
		assertAttachedTo(bombur, thorin);
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.SUPPORT, ponies, saruman);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithOnly2TwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var yazneg = scn.GetShadowCard("yazneg");
		var thorin = scn.GetFreepsCard("thorin");
		var bombur = scn.GetFreepsCard("bombur");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, bombur);

		scn.StartGame();
		assertAttachedTo(bombur, thorin);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);
		// Bombur's Aid is offered: declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.SetTwilight(2);
		scn.FreepsPass();

		// One short of the (3) to remove: not offered, with a follower there to discard.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(yazneg));

		// With 3 it is.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(yazneg));
		assertInPlay(yazneg);
		assertAttachedTo(bombur, thorin);
	}

	@Test
	public void ManeuverActionWithNoFollowerInPlayIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var yazneg = scn.GetShadowCard("yazneg");
		var thorin = scn.GetFreepsCard("thorin");
		var ponies = scn.GetFreepsCard("ponies");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, axe);
		scn.MoveCardsToSupportArea(ponies);

		scn.StartGame();
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.SUPPORT, ponies);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(yazneg));
		scn.ShadowUseCardAction(yazneg);

		// Paid in full, and no follower to discard: the possessions stay.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(yazneg);
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.SUPPORT, ponies);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
