package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_137_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin", "11_137");
					put("goblin2", "11_137");

					// Denizen of Khazad-dum: an [orc] Orc, cost 3; strength +2 at an underground site, read by no test.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc.  Its trigger at the start of the assignment
					// phase is never reached.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc, no text.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] support-area condition, no minion (nothing below 5 burdens).
					put("beckoning", "12_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin", "11_137");
					put("goblin2", "11_137");

					// Denizen of Khazad-dum: an [orc] Orc, cost 3; strength +2 at an underground site, read by no test.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc.  Its trigger at the start of the assignment
					// phase is never reached.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc, no text.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] support-area condition, no minion (nothing below 5 burdens).
					put("beckoning", "12_83");
				}},
				// Site 3 is The Dimholt 8_117: underground and a sanctuary.  Its fellowship action is never used.
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ScurryingGoblinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Scurrying Goblin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion at an underground site, you may spot another [orc] minion to add (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goblin");

		assertEquals("Scurrying Goblin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedAtAnUndergroundSiteBesideAnOrcOrcItMayAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToHand(goblin);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(10);
		// The move to Balin's Tomb, site 4, underground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(denizen);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblin));
		int pool = scn.GetTwilight();

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtAnUndergroundSanctuaryBesideAnOrcMinionWhoIsNoOrcItMayAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(goblin);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetTwilight(10);
		// The move to The Dimholt, site 3.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(halftroll);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblin));
		int pool = scn.GetTwilight();

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingIsAddedAndAnotherOrcMinionPlayedAfterItOffersNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var goblin2 = scn.GetShadowCard("goblin2");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToHand(goblin, denizen, goblin2);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(20);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		// The Denizen first, so that the Goblin's spot is met.
		scn.ShadowPlayCard(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		scn.ShadowPlayCard(goblin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblin));
		int pool = scn.GetTwilight();

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Scurrying Goblin, another [orc] minion, played at the same site: its own offer, and none from the
		// first.
		scn.ShadowPlayCard(goblin2);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblin2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(goblin));
		int poolAtSecondOffer = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(poolAtSecondOffer + 2, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedAwayFromAnUndergroundSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToHand(goblin);

		scn.StartGame();
		scn.SetTwilight(10);
		// The move to Ettenmoors, site 2, plains.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(denizen);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoOtherOrcMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(goblin);
		scn.MoveCardsToSupportArea(beckoning);
		assertInZone(Zone.SUPPORT, beckoning);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// A [moria] Orc and an [orc] card that is no minion; the Goblin itself is the only [orc] minion.
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, beckoning);

		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
