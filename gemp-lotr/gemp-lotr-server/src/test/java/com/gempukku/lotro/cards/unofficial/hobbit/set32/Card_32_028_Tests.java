package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_028_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. No site of the path
	// below site 9 is a battleground, so Watchful Orc's Shadow ability makes the sites battlegrounds for the turn.
	// The [moria] minions are site 4 cards and so roam at sites 2 and 3: +2 to play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("azog", "32_28");

					// Exerts twice to make each site on the path gain battleground until the end of the turn
					put("watchful", "30_40");   // Watchful Orc: [gundabad] Orc; its "When you play" text is never met (cheated in)

					// For the discard pile
					put("troll", "32_31");      // Demolition Troll: [moria] Troll, cost 5; a skirmish ability not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, cost 1; a Shadow ability not used
					put("footman", "30_42");    // Goblin Footman: [moria] Orc, cost 3; a "When you play" text with no possession to play
					put("sneak", "31_32");      // Goblin Sneak: [moria] Orc, cost 1; only in the Free Peoples player's discard pile
					put("worms", "32_33");      // Were-worms: [moria] condition, cost 0
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2, site 3; its assignment ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AzogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Azog, Commander of the Wolves Army
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>.
		 * <b>Maneuver:</b> Exert Azog at a battleground to play a [moria] minion from your discard pile. Its twilight cost is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("azog");

		assertEquals("Azog", card.getBlueprint().getTitle());
		assertEquals("Commander of the Wolves Army", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverExertsAzogAtABattlegroundToPlayAMoriaMinionFromTheDiscardPileAtTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var watchful = scn.GetShadowCard("watchful");
		var troll = scn.GetShadowCard("troll");
		var bats = scn.GetShadowCard("bats");
		var worms = scn.GetShadowCard("worms");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		var freepsSneak = scn.GetFreepsCard("sneak");

		scn.StartGame();
		scn.MoveCardsToDiscard(troll, bats, worms, aggressor, freepsSneak);
		scn.MoveCardsToHand(azog, footman);
		assertInZone(Zone.DISCARD, troll, bats, worms, aggressor, freepsSneak);
		assertInHand(azog, footman);
		scn.MoveMinionsToTable(watchful);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Azog's own play from hand, a [moria] minion played by no ability of his, is at full cost: 4 + 2 roaming.
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(azog);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);
		assertEquals(pool - 6, scn.GetTwilight());

		// The Troll Hoard (site 2) is made a battleground.
		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(azog));
		assertEquals(3, scn.GetTwilightCost(footman));
		pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(azog));
		scn.ShadowUseCardAction(azog);
		assertEquals(1, scn.GetWoundsOn(azog));
		// [moria] minions of two races; not a [moria] card that is no minion, nor a minion of another culture,
		// though both could be paid for, nor the Free Peoples player's [moria] Orc in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll, bats));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worms, aggressor, freepsSneak));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		// 5 - 2 + 2 roaming
		assertEquals(pool - 5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, bats, worms, aggressor);
		// The discount is the played minion's alone.
		assertEquals(3, scn.GetTwilightCost(footman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedAwayFromABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var watchful = scn.GetShadowCard("watchful");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveCardsToDiscard(troll);
		assertInZone(Zone.DISCARD, troll);
		scn.MoveMinionsToTable(azog, watchful);
		scn.SetTwilight(20);
		// Watchful Orc's ability is not used: The Troll Hoard is no battleground.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertInZone(Zone.DISCARD, troll);
		assertEquals(0, scn.GetWoundsOn(azog));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(azog));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileAzogIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var watchful = scn.GetShadowCard("watchful");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveCardsToDiscard(troll);
		assertInZone(Zone.DISCARD, troll);
		scn.MoveMinionsToTable(azog, watchful);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(azog, 2);
		assertEquals(2, scn.GetWoundsOn(azog));
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, troll);
		assertEquals(2, scn.GetWoundsOn(azog));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(azog));
	}

	@Test
	public void ManeuverAbilityWaitsUntilTheMinionCanBePaidForAtTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var watchful = scn.GetShadowCard("watchful");
		var footman = scn.GetShadowCard("footman");
		var worms = scn.GetShadowCard("worms");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToDiscard(footman, worms, aggressor);
		assertInZone(Zone.DISCARD, footman, worms, aggressor);
		scn.MoveMinionsToTable(azog, watchful);
		// The Troll Hoard's start-of-fellowship offer, then the move to site 3.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();

		// Rivendell (site 3), a sanctuary, is made a battleground.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, footman, worms, aggressor);
		assertEquals(0, scn.GetWoundsOn(azog));

		// The Footman costs 3 - 2 + 2 roaming = 3. With 2 in the pool only Were-worms (0) and the Aggressor (2, no
		// roaming at site 3) could be paid for, and neither is a [moria] minion.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(azog));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(azog));
		scn.ShadowUseCardAction(azog);
		assertEquals(1, scn.GetWoundsOn(azog));
		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, worms, aggressor);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
