package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_027_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. No site of the path
	// below site 9 is a battleground, so Watchful Orc's Shadow ability makes the sites battlegrounds for the turn.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin1", "32_27");
					put("goblin2", "32_27");

					// Exerts twice to make each site on the path gain battleground until the end of the turn: a [gundabad] Orc
					put("watchful", "30_40");   // Watchful Orc: [gundabad] Orc, vitality 3; its "When you play" text is never met (cheated in)
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, vitality 1; a text on its own play only
					put("sentinel", "33_49");   // Orc Sentinel: [sauron] Orc, vitality 2; its Shadow ability is not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc, vitality 2; its Shadow ability is not used

					// Archery: Exert Kili to wound a minion; he adds nothing to the archery total once he has used it.
					put("kili", "30_12");       // Kili, Brother of Fili: [dwarven] companion, vitality 3
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AssaultingGoblinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Assaulting Goblin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Response:</b> If another Orc is about to take a wound at a battleground, exert this minion to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goblin1");

		assertEquals("Assaulting Goblin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertsToPreventAWoundToAnotherOrcAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin1 = scn.GetShadowCard("goblin1");
		var goblin2 = scn.GetShadowCard("goblin2");
		var watchful = scn.GetShadowCard("watchful");
		var runner = scn.GetShadowCard("runner");
		var kili = scn.GetFreepsCard("kili");
		scn.MoveCompanionsToTable(kili);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin1, goblin2, watchful, runner);
		scn.FreepsPass();

		// The Troll Hoard (site 2) is made a battleground.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(watchful));
		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(2, scn.GetWoundsOn(watchful));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(goblin1));
		assertEquals(0, scn.GetWoundsOn(goblin2));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(kili));

		// A [moria] Orc: each copy offers its response.
		scn.FreepsUseCardAction(kili);
		scn.FreepsChooseCard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblin1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblin2));
		scn.ShadowChooseOptionalTrigger(goblin1);
		// The exertion is this copy's; the Runner, vitality 1, is not wounded and so not killed.
		assertEquals(1, scn.GetWoundsOn(goblin1));
		assertEquals(0, scn.GetWoundsOn(goblin2));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// A [gundabad] Orc, one vitality left. The first copy, vitality 2 with 1 wound, cannot exert.
		scn.ShadowPass();
		scn.FreepsUseCardAction(kili);
		scn.FreepsChooseCard(watchful);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(goblin1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblin2));
		scn.ShadowChooseOptionalTrigger(goblin2);
		assertEquals(1, scn.GetWoundsOn(goblin2));
		assertEquals(1, scn.GetWoundsOn(goblin1));
		assertEquals(2, scn.GetWoundsOn(watchful));
		assertInZone(Zone.SHADOW_CHARACTERS, watchful);
		assertEquals(2, scn.GetWoundsOn(kili));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void IsNotOfferedForItsOwnWoundNorForAMinionThatIsNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin1 = scn.GetShadowCard("goblin1");
		var goblin2 = scn.GetShadowCard("goblin2");
		var watchful = scn.GetShadowCard("watchful");
		var bats = scn.GetShadowCard("bats");
		var kili = scn.GetFreepsCard("kili");
		scn.MoveCompanionsToTable(kili);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(goblin1, goblin2, watchful, bats);
		// The Troll Hoard's start-of-fellowship offer, then the move to site 3.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();

		// Rivendell (site 3), a sanctuary, is made a battleground.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(goblin1));
		assertEquals(0, scn.GetWoundsOn(goblin2));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetWoundsOn(kili));

		// The first copy, able to exert, does not answer its own wound; the other copy does, and is declined.
		scn.FreepsUseCardAction(kili);
		scn.FreepsChooseCard(goblin1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(goblin1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblin2));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(goblin1));
		assertEquals(0, scn.GetWoundsOn(goblin2));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// A [moria] minion that is no Orc: neither copy answers.
		scn.ShadowPass();
		scn.FreepsUseCardAction(kili);
		scn.FreepsChooseCard(bats);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(1, scn.GetWoundsOn(goblin1));
		assertEquals(0, scn.GetWoundsOn(goblin2));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void IsNotOfferedAwayFromABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin1 = scn.GetShadowCard("goblin1");
		var watchful = scn.GetShadowCard("watchful");
		var sentinel = scn.GetShadowCard("sentinel");
		var kili = scn.GetFreepsCard("kili");
		scn.MoveCompanionsToTable(kili);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin1, watchful, sentinel);
		// Watchful Orc's ability is not used: The Troll Hoard is no battleground.
		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(goblin1));
		assertEquals(0, scn.GetWoundsOn(sentinel));

		scn.FreepsUseCardAction(kili);
		scn.FreepsChooseCard(sentinel);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(goblin1));
		assertEquals(1, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(goblin1));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
