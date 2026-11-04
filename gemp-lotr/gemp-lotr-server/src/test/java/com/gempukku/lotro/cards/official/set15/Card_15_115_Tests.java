package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blow", "15_115");
					put("desolation", "15_107");  // Desolation Orc: an [orc] Orc, strength 12, vitality 3; its only text is an extra cost to play (cheated in)
					put("halftroll", "14_12");    // Half-troll of Far Harad: an [orc] Half-troll (no Troll), strength 8, vitality 2; a start-of-assignment trigger never reached
					put("mtroll", "15_112");      // Mountain-troll: an [orc] Troll, strength 22; its texts are a discount to play and a Shadow ability never used
					put("cavetroll", "1_165");    // Cave Troll of Moria: a [moria] Troll, strength 15; its texts are about its play (cheated in)
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc with no text, strength 13, vitality 3
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: the [gandalf] Wizard Traveled Leader spots; his text is about his strength
					put("leader", "12_34");       // Traveled Leader: Maneuver or Regroup: spot a [gandalf] Wizard to replace a site in the fellowship's current region
					put("leader2", "12_34");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PummelingBlowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Pummeling Blow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert an [orc] minion. Plays on the fellowship's current site.<br>This site cannot be replaced. While the fellowship is at this site, each Troll is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blow");

		assertEquals("Pummeling Blow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnOrcMinionToPlayOnTheFellowshipsSiteWhereEachTrollIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetShadowCard("blow");
		var desolation = scn.GetShadowCard("desolation");
		var halftroll = scn.GetShadowCard("halftroll");
		var mtroll = scn.GetShadowCard("mtroll");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(blow);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(desolation, halftroll, mtroll, cavetroll, troop);
		scn.ShadowRefreshDecision();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(22, scn.GetStrength(mtroll));
		assertEquals(15, scn.GetStrength(cavetroll));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(12, scn.GetStrength(desolation));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(desolation));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(mtroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(cavetroll));
		assertTrue(scn.ShadowPlayAvailable(blow));

		scn.ShadowPlayCard(blow);
		// The [orc] minions of three races are offered; the [moria] Orc and the [moria] Troll are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(desolation, halftroll, mtroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, cavetroll));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(halftroll);
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(desolation));
		assertEquals(0, scn.GetWoundsOn(mtroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(cavetroll));
		// On the fellowship's site, not the one behind it; no site was asked for.
		assertAttachedTo(blow, site2);
		assertNotAttachedTo(blow, site1);
		// Each Troll, of either culture, is +1; the Half-troll and the Orcs are not.
		assertEquals(23, scn.GetStrength(mtroll));
		assertEquals(16, scn.GetStrength(cavetroll));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(12, scn.GetStrength(desolation));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyOrcMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetShadowCard("blow");
		var desolation = scn.GetShadowCard("desolation");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(blow);
		assertInZone(Zone.HAND, blow);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(desolation, troop);
		// Vitality 3 with 2 wounds: the Desolation Orc cannot exert; the [moria] Orc could.
		scn.AddWoundsToChar(desolation, 2);
		assertEquals(2, scn.GetWoundsOn(desolation));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(blow));
		assertInZone(Zone.HAND, blow);
	}

	@Test
	public void OnTheFellowshipsSiteThatSiteIsNotReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetShadowCard("blow");
		var desolation = scn.GetShadowCard("desolation");
		var gandalf = scn.GetFreepsCard("gandalf");
		var leader = scn.GetFreepsCard("leader");
		var freepsSite2 = scn.GetFreepsSite(2);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(blow, leader);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(desolation);
		scn.ShadowRefreshDecision();

		// Site 2 was laid by the move, and is the Shadow player's; the Free Peoples player's own site 2 is in his adventure deck.
		var site2 = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(scn.GetShadowSite(2), site2);
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2);
		assertEquals(0, scn.GetWoundsOn(desolation));

		// The Desolation Orc, the only [orc] minion, exerts unasked.
		scn.ShadowPlayCard(blow);
		assertEquals(1, scn.GetWoundsOn(desolation));
		assertAttachedTo(blow, site2);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(leader));
		scn.FreepsPlayCard(leader);
		// The event still asks for a site of the region; the one Pummeling Blow is on is not replaced.
		scn.FreepsChooseCard(site2);
		assertSame(site2, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2);
		assertAttachedTo(blow, site2);
		assertInZone(Zone.DISCARD, leader);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OnASiteTheFellowshipHasLeftTrollsAreNotStrengthenedAndOnlyThatSiteCannotBeReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetShadowCard("blow");
		var mtroll = scn.GetShadowCard("mtroll");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var gandalf = scn.GetFreepsCard("gandalf");
		var leader = scn.GetFreepsCard("leader");
		var leader2 = scn.GetFreepsCard("leader2");
		var freepsSite2 = scn.GetFreepsSite(2);
		var freepsSite3 = scn.GetFreepsSite(3);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(leader, leader2);
		// Where it would have been played in the first turn's Shadow phase, at site 2; it stays there.
		var site2 = scn.GetCurrentSite();
		scn.AttachCardsTo(site2, blow);
		assertAttachedTo(blow, site2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(mtroll, cavetroll);
		scn.ShadowRefreshDecision();

		var site3 = scn.GetCurrentSite();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(blow, site2);
		// The fellowship is at site 3: the Trolls have their printed strength.
		assertEquals(22, scn.GetStrength(mtroll));
		assertEquals(15, scn.GetStrength(cavetroll));

		scn.SkipToPhase(Phase.MANEUVER);
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2, freepsSite3);
		scn.FreepsPlayCard(leader);
		// "This site" is the one it is on, wherever the fellowship is: site 2 is not replaced.
		scn.FreepsChooseCard(site2);
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2);
		assertAttachedTo(blow, site2);

		// The fellowship's own site, which it is not on, is replaced.
		scn.ShadowPass();
		scn.FreepsPlayCard(leader2);
		scn.FreepsChooseCard(site3);
		assertSame(freepsSite3, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertAttachedTo(blow, site2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
