package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("infantry", "5_106");
					put("infantry2", "5_106");     // a second copy: not unique, and "discard this minion"
					put("fear", "1_278");         // Strength Born of Fear: [sauron] support-area condition; a skirmish ability used only on purpose
					put("evil", "1_283");         // You Bring Great Evil: [sauron] condition borne by a companion; no heal at a sanctuary
					put("armory", "1_173");       // Goblin Armory: [moria] support-area condition; answers only [moria] weapons and wounds
					put("scimitar", "1_269");     // Orc Scimitar: [sauron] possession, a hand weapon on a [sauron] Orc; a skirmish ability
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc to bear the Scimitar; his text answers only a weapon played on him
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion to bear You Bring Great Evil
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcInfantryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Orc Infantry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Regroup:</b> Spot a [sauron] condition and discard this minion to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("infantry");

		assertEquals("Orc Infantry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilitySpotsASauronConditionAndDiscardsThisMinionToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var infantry2 = scn.GetShadowCard("infantry2");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		// Player 1's second turn: both markers on site 2, and a move to site 3 ahead.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(fear);
		scn.MoveMinionsToTable(infantry, infantry2);
		assertInZone(Zone.SUPPORT, fear);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.SHADOW_CHARACTERS, infantry);
		assertInZone(Zone.SHADOW_CHARACTERS, infantry2);

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(infantry));
		scn.ShadowUseCardAction(infantry);

		// The copy used is discarded, the other copy stays; the condition is spotted, not spent.
		assertInZone(Zone.DISCARD, infantry);
		assertInZone(Zone.SHADOW_CHARACTERS, infantry2);
		assertInZone(Zone.SUPPORT, fear);
		// Site 1, the lowest site every marker has passed, is his.
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASauronConditionBorneByACompanionIsSpottedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var evil = scn.GetShadowCard("evil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.AttachCardsTo(aragorn, evil);
		scn.MoveMinionsToTable(infantry);
		assertAttachedTo(evil, aragorn);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, infantry);
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(infantry));
		scn.ShadowUseCardAction(infantry);

		assertInZone(Zone.DISCARD, infantry);
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithAConditionOfAnotherCultureAndASauronPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var armory = scn.GetShadowCard("armory");
		var scimitar = scn.GetShadowCard("scimitar");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(infantry, chieftain);
		scn.AttachCardsTo(chieftain, scimitar);
		assertInZone(Zone.SUPPORT, armory);
		assertAttachedTo(scimitar, chieftain);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		// He could be discarded, and a site could be taken: only the spot is missing.
		assertInZone(Zone.SHADOW_CHARACTERS, infantry);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(infantry));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void WithNoSiteThatCanBeControlledTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var infantry = scn.GetShadowCard("infantry");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.MoveCardsToSupportArea(fear);
		scn.MoveMinionsToTable(infantry);
		assertInZone(Zone.SUPPORT, fear);
		// The first turn: the fellowship moves to site 2 and the other player's marker stays on site 1.
		scn.SkipToPhase(Phase.REGROUP);
		var site1 = scn.GetSite(1);

		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, infantry);

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(infantry));
		scn.ShadowUseCardAction(infantry);

		// The minion is discarded, and no site is taken.
		assertInZone(Zone.DISCARD, infantry);
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
