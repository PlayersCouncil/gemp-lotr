package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_032_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  The only battleground of the path is its site 9, Battle of the Five Armies 30_57, so Watchful Orc's
	// Shadow ability makes the sites battlegrounds for the turn elsewhere.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "32_32");
					// A second copy, for "this possession".
					put("warg2", "32_32");
					// Exerts twice to make each site on the path gain battleground until the end of the turn.
					put("watchful", "30_40");    // Watchful Orc: [gundabad] Orc, vitality 3; its "When you play" text is never met (cheated in)
					put("runner", "31_30");      // Goblin Runner: [moria] Orc; a text on its own play only
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc; an Assignment ability, not used
					// Minions that are no Orcs.
					put("bats", "32_30");        // Dark Bats: [moria] Creature; a Shadow ability, not used
					put("bridging", "33_36");    // Bridging Troll: [moria] Troll; a Skirmish ability, not used
					// Another mount, played after the Warg.
					put("savage", "31_25");      // Savage Warg: [gundabad] mount, cost 3, bearer must be an Orc; a trigger on its bearer's win
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ViciousWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Vicious Warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 3
		 * Vitality: 1
		 * Game Text: Bearer must be an Orc.<br>Bearer is <b>fierce</b>.<br>When you play this possession at a
		 * battleground site, you may add (4) (except at site 9).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warg");

		assertEquals("Vicious Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAnOrcMakesHimFierceAndMayAdd4AtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var watchful = scn.GetShadowCard("watchful");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		var bridging = scn.GetShadowCard("bridging");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(watchful, runner, bats, bridging);
		scn.SetShadowHand(warg);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, watchful, runner, bats, bridging);
		assertInHand(warg);

		// The Troll Hoard (site 2: forest, underground) is made a battleground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(20, scn.GetTwilight());
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(watchful, Keyword.FIERCE));

		scn.ShadowPlayCard(warg);
		// Orcs of two cultures; not a Creature, nor a Troll.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, watchful));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, bridging));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);
		assertAttachedTo(warg, runner);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowAcceptOptionalTrigger();
		// 20 - 3 to play + 4
		assertEquals(21, scn.GetTwilight());

		// The bearer is fierce, and the other Orc is not.
		assertTrue(scn.HasKeyword(runner, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(watchful, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningAddsNothingAndAnotherMountOrASecondCopyPlayedAfterwardsGetsNothingFromTheFirst() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var warg2 = scn.GetShadowCard("warg2");
		var savage = scn.GetShadowCard("savage");
		var watchful = scn.GetShadowCard("watchful");
		var runner = scn.GetShadowCard("runner");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(watchful, runner, aggressor);
		scn.SetShadowHand(warg, warg2, savage);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, watchful, runner, aggressor);
		assertInHand(warg, warg2, savage);

		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(20, scn.GetTwilight());

		scn.ShadowPlayCard(warg);
		scn.ShadowChooseCard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowDeclineOptionalTrigger();
		// 20 - 3 to play
		assertEquals(17, scn.GetTwilight());

		// Another mount played afterwards, at the same battleground: the Warg in play offers nothing.
		scn.ShadowPlayCard(savage);
		scn.ShadowChooseCard(watchful);
		assertAttachedTo(savage, watchful);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		// 17 - 3
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy offers its own (4), once, and the first copy offers nothing for it.  The Aggressor is the
		// one Orc left with no mount, and takes it unasked.
		scn.ShadowPlayCard(warg2);
		assertAttachedTo(warg2, aggressor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowAcceptOptionalTrigger();
		// 14 - 3 + 4
		assertEquals(15, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MayAdd4AtSite8MadeABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var watchful = scn.GetShadowCard("watchful");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.FreepsPass();
		scn.MoveMinionsToTable(watchful, runner);
		scn.SetShadowHand(warg);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, watchful, runner);
		assertInHand(warg);

		// Smaug's Den (site 8: mountain, underground) is made a battleground.
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(20, scn.GetTwilight());

		scn.ShadowPlayCard(warg);
		scn.ShadowChooseCard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowAcceptOptionalTrigger();
		// 20 - 3 + 4
		assertEquals(21, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedAtSite9ThoughItIsABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		scn.SetShadowHand(warg);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInHand(warg);

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.ShadowPlayCard(warg);
		assertAttachedTo(warg, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		// 20 - 3 to play, and nothing added
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedAwayFromABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var watchful = scn.GetShadowCard("watchful");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(watchful, runner);
		scn.SetShadowHand(warg);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, watchful, runner);
		assertInHand(warg);

		// Watchful Orc's ability is not used: The Troll Hoard is no battleground.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.ShadowPlayCard(warg);
		scn.ShadowChooseCard(runner);
		assertAttachedTo(warg, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		// 20 - 3 to play
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
