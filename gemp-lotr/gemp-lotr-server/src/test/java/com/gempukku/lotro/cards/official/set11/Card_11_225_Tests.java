package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_225_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "11_225");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; abilities not used
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its (2) is never added, being cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Úlairë Toldëa, Eighth of the Nine Riders
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>Each time Úlairë Toldëa wins a skirmish, you may play the fellowship's next site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Úlairë Toldëa", card.getBlueprint().getTitle());
		assertEquals("Eighth of the Nine Riders", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachTimeToldeaWinsTheShadowPlayerMayPlayTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		scn.FreepsPass();

		// Strength 12 against 8: he wins and Aragorn takes a wound.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, toldea);
		scn.FreepsResolveSkirmish(aragorn);
		var shadowSite3 = scn.GetShadowSite(3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
		scn.ShadowChooseOptionalTrigger(toldea);
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, shadowSite3);
		assertEquals(2, scn.GetCurrentSiteNumber());

		// He is fierce and wins again; his own site 3 already lies next, so there is nothing to play.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, toldea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertSame(shadowSite3, scn.GetSite(3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A move to that site 3 and a second skirmish phase in the same turn: a third win is answered again, and
		// declined.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, toldea);
		scn.FreepsResolveSkirmish(aragorn);
		assertNull(scn.GetSite(4));
		scn.PassSkirmishActions();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(toldea));
		scn.ShadowDeclineOptionalTrigger();
		assertNull(scn.GetSite(4));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ASkirmishToldeaLosesAndLivesThroughOffersNothingNorDoesAnotherMinionsWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		// Strength 8 + 5 = 13: Aragorn beats Toldëa's 12.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.MoveMinionsToTable(toldea, runner);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, toldea},
				new PhysicalCardImpl[]{sam, runner});

		// Another minion wins: the Runner's 5 beats Sam's 3.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertNull(scn.GetSite(3));

		// Toldëa loses, and lives through his wound (vitality 3).
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(toldea));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(toldea));
		assertInZone(Zone.SHADOW_CHARACTERS, toldea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(toldea));
		assertNull(scn.GetSite(3));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
