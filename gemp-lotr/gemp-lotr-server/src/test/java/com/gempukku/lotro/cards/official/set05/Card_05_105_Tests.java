package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fighter", "5_105");
					put("fighter2", "5_105");     // a second copy: not unique, and "exert this minion"
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
	public void OrcFighterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Orc Fighter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Regroup:</b> Spot a [sauron] condition and exert this minion to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fighter");

		assertEquals("Orc Fighter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilitySpotsASauronConditionAndExertsThisMinionToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter = scn.GetShadowCard("fighter");
		var fighter2 = scn.GetShadowCard("fighter2");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		// Player 1's second turn: both markers on site 2, and a move to site 3 ahead.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(fear);
		scn.MoveMinionsToTable(fighter, fighter2);
		assertInZone(Zone.SUPPORT, fear);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(fighter));
		assertEquals(0, scn.GetWoundsOn(fighter2));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(fighter));
		scn.ShadowUseCardAction(fighter);

		// The copy used exerts, the other copy does not; the condition is spotted, not spent.
		assertEquals(1, scn.GetWoundsOn(fighter));
		assertEquals(0, scn.GetWoundsOn(fighter2));
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

		var fighter = scn.GetShadowCard("fighter");
		var evil = scn.GetShadowCard("evil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.AttachCardsTo(aragorn, evil);
		scn.MoveMinionsToTable(fighter);
		assertAttachedTo(evil, aragorn);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(fighter));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(fighter));
		scn.ShadowUseCardAction(fighter);

		assertEquals(1, scn.GetWoundsOn(fighter));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithAConditionOfAnotherCultureAndASauronPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter = scn.GetShadowCard("fighter");
		var armory = scn.GetShadowCard("armory");
		var scimitar = scn.GetShadowCard("scimitar");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(fighter, chieftain);
		scn.AttachCardsTo(chieftain, scimitar);
		assertInZone(Zone.SUPPORT, armory);
		assertAttachedTo(scimitar, chieftain);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		// He could exert, and a site could be taken: only the spot is missing.
		assertEquals(0, scn.GetWoundsOn(fighter));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fighter));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter = scn.GetShadowCard("fighter");
		var fighter2 = scn.GetShadowCard("fighter2");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(fear);
		scn.MoveMinionsToTable(fighter, fighter2);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(fighter, 2);
		assertEquals(2, scn.GetWoundsOn(fighter));
		assertInZone(Zone.SUPPORT, fear);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(fighter));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fighter));
		// The other copy, able to exert, is offered its own.
		assertTrue(scn.ShadowActionAvailable(fighter2));
	}

	@Test
	public void WithNoSiteThatCanBeControlledTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter = scn.GetShadowCard("fighter");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.MoveCardsToSupportArea(fear);
		scn.MoveMinionsToTable(fighter);
		assertInZone(Zone.SUPPORT, fear);
		// The first turn: the fellowship moves to site 2 and the other player's marker stays on site 1.
		scn.SkipToPhase(Phase.REGROUP);
		var site1 = scn.GetSite(1);

		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertEquals(0, scn.GetWoundsOn(fighter));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(fighter));
		scn.ShadowUseCardAction(fighter);

		// The exertion is paid, and no site is taken.
		assertEquals(1, scn.GetWoundsOn(fighter));
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
