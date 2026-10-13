package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_07_361_Tests
{

	// The King path, Haunted Pass its site 9.  Site 8 is Watchers of Cirith Ungol, whose only text is a skirmish ability.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability only

					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc minion, no text
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man minion; strength +3 and fierce only while 2 burdens can be spotted
					put("galley", "8_59");      // Corsair War Galley: a Shadow possession in the support area, no minion; a regroup ability only
					put("rhun", "4_239");       // Men of Rhûn: a [raider] Skirmish event, the card to be drawn
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_361");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HauntedPassStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Haunted Pass
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Shadow:</b> Remove a threat and spot your minion to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Haunted Pass", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ShadowAbilityRemovesAThreatToDrawACardWhileYourMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli");

		var patrol = scn.GetShadowCard("patrol");
		var rhun = scn.GetShadowCard("rhun");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();

		var pass = scn.GetCurrentSite();
		assertEquals("Haunted Pass", pass.getBlueprint().getTitle());

		scn.MoveMinionsToTable(patrol);
		scn.AddThreats(1);
		scn.SetShadowHand();
		// Two cards in the draw deck, the Men of Rhun on top: one card drawn, and not two.
		scn.SetShadowDrawDeck(rhun, axeman);
		var freepsHand = scn.GetFreepsHandCount();
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowActionAvailable(pass));

		scn.ShadowUseCardAction(pass);

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.HAND, rhun);
		assertInZone(Zone.DECK, axeman);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli");

		var axeman = scn.GetShadowCard("axeman");
		var galley = scn.GetShadowCard("galley");
		var rhun = scn.GetShadowCard("rhun");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();

		var pass = scn.GetCurrentSite();
		assertEquals("Haunted Pass", pass.getBlueprint().getTitle());

		// A Shadow card in play that is no minion.
		scn.MoveCardsToSupportArea(galley);
		scn.AddThreats(1);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(rhun);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(pass));

		// A minion of another culture and race than the Troop of the other test.
		scn.MoveMinionsToTable(axeman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(pass));

		scn.ShadowUseCardAction(pass);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.HAND, rhun);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli");

		var patrol = scn.GetShadowCard("patrol");
		var rhun = scn.GetShadowCard("rhun");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();

		var pass = scn.GetCurrentSite();
		assertEquals("Haunted Pass", pass.getBlueprint().getTitle());

		scn.MoveMinionsToTable(patrol);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(rhun);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(pass));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(pass));
	}
}
