package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_198_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_198");
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
					put("stormer", "4_198");
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; overwhelmed by the Stormer's 8
					put("scout", "15_116");     // Scouting Orc: [orc] Orc, no Uruk-hai; "Skirmish: Exert this minion to wound a companion it is skirmishing."
					put("xbow", "4_183");       // Uruk Crossbowman: Uruk-hai archer; "Archery: Exert this minion to make the Free Peoples player wound an unbound companion ..."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukStormerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Stormer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time a companion or ally is killed during a skirmish involving an Uruk-hai, you may take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Uruk Stormer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledACompanionKilledInAnUrukHaisSkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
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
		// 8 against 4: the Guard is overwhelmed.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledACompanionKilledInAnUrukHaisSkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
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

	// "Killed during a skirmish involving an Uruk-hai" is any kill in that skirmish, whatever wound dealt it, and
	// not "killed by an Uruk-hai" (CR 4.2's definition of that is for "<X> kills", Q-0328). The card was built
	// with `killer: urukHai`: this kill, by another minion's own wound, was not answered. F-0546.
	@Test
	public void ACompanionKilledInAnUrukHaisSkirmishByAnotherMinionsWoundOffersIt() throws DecisionResultInvalidException, CardNotFoundException {
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

	// The other side of the same words: an Uruk-hai's own wound outside any skirmish. As built (`killer`, whose
	// `inSkirmish` was not asked of a wound) the archery-phase kill offered a site. F-0546.
	@Test
	public void ACompanionKilledOutsideASkirmishByAnUrukHaisOwnWoundOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var stormer = scn.GetShadowCard("stormer");
		var xbow = scn.GetShadowCard("xbow");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(stormer, xbow);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		// The Guard is the only unbound companion, and takes the wound with nothing asked.
		scn.ShadowUseCardAction(xbow);
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stormer));
		assertTrue(scn.FreepsDecisionAvailable("Play Archery action or Pass"));

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}
}
