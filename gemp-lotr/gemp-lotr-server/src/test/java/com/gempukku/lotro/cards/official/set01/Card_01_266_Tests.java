package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_266_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chieftain1", "1_266");
					put("chieftain2", "1_266");

					// Every weapon a [sauron] Orc can bear is a [sauron] hand weapon; three titles of them.
					put("scimitar", "1_269");   // Orc Scimitar: hand weapon, cost 1; bearer must be a [sauron] Orc; a skirmish ability used only on purpose
					put("blade", "2_95");       // Vile Blade: hand weapon, cost 1; bearer must be a [sauron] Orc; +2 more only against a Man or Elf
					put("banner", "6_98");      // Banner of the Eye: hand weapon, cost 0; bearer must be a [sauron] Orc; an optional trigger on a skirmish won
					put("warg", "10_89");       // Gothmog's Warg: unique mount, no weapon, cost 1; bearer must be a [sauron] Orc; +1 for each site controlled
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose

					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf to exert for Stand Against Darkness; an archery ability used only on purpose
					put("stand", "1_63");       // Stand Against Darkness: Maneuver: exert an Elf to discard a [sauron] minion, condition or possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcChieftainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Chieftain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: When you play a weapon on this minion, add (2) (limit once per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chieftain1");

		assertEquals("Orc Chieftain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandHeAddsNothingThenAMountAddsNothingAndAWeaponAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain1");
		var warg = scn.GetShadowCard("warg");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToHand(chieftain, warg, scimitar);

		scn.StartGame();
		scn.SetTwilight(10);
		assertInHand(chieftain, warg, scimitar);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(chieftain);
		// His own play is no weapon played on him: 2 to play + 2 roaming (site 6 at site 2), and nothing added.
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(pool - 2 - 2, scn.GetTwilight());

		pool = scn.GetTwilight();
		scn.ShadowPlayCard(warg);
		// A possession that is no weapon: its cost, and nothing added.
		assertAttachedTo(warg, chieftain);
		assertEquals(pool - 1, scn.GetTwilight());

		pool = scn.GetTwilight();
		scn.ShadowPlayCard(scimitar);
		assertAttachedTo(scimitar, chieftain);
		assertEquals(pool - 1 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyAnswersAWeaponPlayedOnItselfOnlyAndAnotherOrcsWeaponAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain1 = scn.GetShadowCard("chieftain1");
		var chieftain2 = scn.GetShadowCard("chieftain2");
		var soldier = scn.GetShadowCard("soldier");
		var blade = scn.GetShadowCard("blade");
		var scimitar = scn.GetShadowCard("scimitar");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToHand(blade, scimitar, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain1, chieftain2, soldier);
		scn.SetTwilight(10);
		assertInHand(blade, scimitar, banner);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(blade);
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain1, chieftain2, soldier));
		scn.ShadowChooseCard(soldier);
		// A weapon on an Orc who is not this minion: its cost alone.
		assertAttachedTo(blade, soldier);
		assertEquals(pool - 1, scn.GetTwilight());

		pool = scn.GetTwilight();
		scn.ShadowPlayCard(scimitar);
		scn.ShadowChooseCard(chieftain2);
		// The second copy answers its own weapon, once: 2, not 4.
		assertAttachedTo(scimitar, chieftain2);
		assertEquals(pool - 1 + 2, scn.GetTwilight());

		pool = scn.GetTwilight();
		// The other two Orcs bear a hand weapon each: the first copy is the only bearer left, and is taken unasked.
		scn.ShadowPlayCard(banner);
		// The first copy's own limit is untouched by the second's: the Banner costs 0, and 2 is added.
		assertAttachedTo(banner, chieftain1);
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondWeaponPlayedOnHimInTheSameTurnAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain1");
		var scimitar = scn.GetShadowCard("scimitar");
		var blade = scn.GetShadowCard("blade");
		var legolas = scn.GetFreepsCard("legolas");
		var stand = scn.GetFreepsCard("stand");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(stand);
		scn.MoveCardsToHand(scimitar, blade);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SetTwilight(10);
		assertInHand(scimitar, blade, stand);
		scn.FreepsPass();

		// The first Shadow phase of the turn: a weapon on him adds 2.
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(scimitar);
		assertAttachedTo(scimitar, chieftain);
		assertEquals(pool - 1 + 2, scn.GetTwilight());

		// The maneuver phase: the Free Peoples player discards the weapon.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsPlayCard(stand);
		scn.FreepsChooseCard(scimitar);
		assertInDiscard(scimitar);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		// Nobody skirmishes; the fellowship moves again, and the minion stays.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		// The second Shadow phase of the same turn: a weapon on him again, and nothing added.
		pool = scn.GetTwilight();
		scn.ShadowPlayCard(blade);
		assertAttachedTo(blade, chieftain);
		assertEquals(pool - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
