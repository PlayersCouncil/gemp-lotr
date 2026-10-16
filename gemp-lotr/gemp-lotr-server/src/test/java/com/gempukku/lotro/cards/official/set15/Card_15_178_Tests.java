package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_178_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stormer", "15_178");
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; overwhelmed by the Stormer's 10
					put("scout", "15_116");     // Scouting Orc: [orc] Orc; "Skirmish: Exert this minion to wound a companion it is skirmishing."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukVillageStormerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Uruk Village Stormer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While you can spot a companion of strength 12 or more, this minion is strength +4.<br>Each time a companion or ally is killed during a skirmish involving an [uruk-hai] minion, you may take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Uruk Village Stormer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledACompanionKilledInAnUrukHaiMinionsSkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var stormer = scn.GetShadowCard("stormer");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(stormer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, stormer);
		scn.FreepsResolveSkirmish(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 10 against 4: the Guard is overwhelmed.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledACompanionKilledInAnUrukHaiMinionsSkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var stormer = scn.GetShadowCard("stormer");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(stormer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, stormer);
		scn.FreepsResolveSkirmish(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stormer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	// "Killed during a skirmish involving an [uruk-hai] minion" is any kill in that skirmish, whatever wound
	// dealt it, and not "killed by" one (Q-0328 is for "<X> kills"): a kill by another minion's own wound is
	// answered. Uruk Stormer 4_198 and Uruk Slaughterer 12_154 were built the other way (F-0546, F-0547).
	@Test
	public void ACompanionKilledInAnUrukHaiMinionsSkirmishByAnotherMinionsWoundOffersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var stormer = scn.GetShadowCard("stormer");
		var scout = scn.GetShadowCard("scout");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(stormer, scout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, stormer);
		// The Shadow player puts the [orc] Orc in the same skirmish.
		scn.ShadowAssignToMinions(guard, scout);
		scn.FreepsResolveSkirmish(guard);

		scn.FreepsPass();
		scn.ShadowUseCardAction(scout);
		// The Orc's wound kills the Guard before the skirmish is resolved.
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		// The Guard was the skirmish's only Free Peoples character: it is over, and so is the phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
