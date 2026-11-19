package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sapper", "12_94");
					put("sapper2", "12_94");    // a second Orc Sapper: the minion that is not "this minion"
					put("goblin", "11_137");    // Scurrying Goblin: [orc] Orc, cost 1; its text asks for an underground site
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, resistance 5
					put("farin", "11_7");       // Farin, Emissary of Erebor: [dwarven] Dwarf companion, resistance 6; muster at a battleground or underground site only
					put("gimli", "11_8");       // Gimli, Lively Combatant: [dwarven] Dwarf companion, strength 6, resistance 7; a draw when he wins
					put("bounder", "1_286");    // Bounder: Hobbit ally, resistance 0 as every ally has; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcSapperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orc Sapper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot a companion who has resistance 4 or less to prevent that companion from being assigned to this minion until the regroup phase.
		*/

		var scn = GetScenario();

		var sapper = scn.GetFreepsCard("sapper");

		assertEquals("Orc Sapper", sapper.getBlueprint().getTitle());
		assertNull(sapper.getBlueprint().getSubtitle());
		assertFalse(sapper.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sapper.getBlueprint().getSide());
		assertEquals(Culture.ORC, sapper.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sapper.getBlueprint().getCardType());
		assertEquals(Race.ORC, sapper.getBlueprint().getRace());
		assertEquals(3, sapper.getBlueprint().getTwilightCost());
		assertEquals(9, sapper.getBlueprint().getStrength());
		assertEquals(2, sapper.getBlueprint().getVitality());
		assertEquals(4, sapper.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMaySpotACompanionOfResistance4OrLessWhoCannotBeAssignedToItUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var sapper = scn.GetShadowCard("sapper");
		var sapper2 = scn.GetShadowCard("sapper2");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCompanionsToTable(sam, farin, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sapper, goblin);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(sapper2);
		scn.SetTwilight(10);
		// Two burdens take 2 off every companion's resistance.
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(4, scn.GetResistance(farin));
		assertEquals(5, scn.GetResistance(gimli));
		scn.FreepsPass();

		scn.ShadowPlayCard(sapper);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sapper));
		scn.ShadowAcceptOptionalTrigger();
		// Resistance 3 and 4 are offered; 5, Frodo's 8 and the ally (no companion) are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, farin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, frodo, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(farin);

		// Another [orc] Orc played after it sets nothing off.
		scn.ShadowPlayCard(goblin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssignAgainst(farin, sapper));
		assertTrue(scn.FreepsCanAssignAgainst(sam, sapper));
		assertTrue(scn.FreepsCanAssignAgainst(farin, sapper2));
		assertTrue(scn.FreepsCanAssignAgainst(farin, goblin));

		// Gimli (6) loses to the Sapper (9) and takes a wound; the other minions are left unassigned.
		scn.FreepsAssignToMinions(gimli, sapper);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		// At the first regroup decision the bar is gone already.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsCanAssignAgainst(farin, sapper));

		// A second move, and so a second assignment phase in the same turn: the bar ended as the regroup phase began.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		assertInPlay(sapper);
		assertTrue(scn.FreepsCanAssignAgainst(farin, sapper));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void DecliningTheOfferLeavesEveryCompanionFreeToBeAssignedToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var farin = scn.GetFreepsCard("farin");
		var sapper = scn.GetShadowCard("sapper");
		scn.MoveCompanionsToTable(sam, farin);
		scn.MoveCardsToHand(sapper);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.SetTwilight(10);
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(4, scn.GetResistance(farin));
		scn.FreepsPass();

		scn.ShadowPlayCard(sapper);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sapper));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssignAgainst(farin, sapper));
		assertTrue(scn.FreepsCanAssignAgainst(sam, sapper));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void WithNoCompanionOfResistance4OrLessNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var farin = scn.GetFreepsCard("farin");
		var sapper = scn.GetShadowCard("sapper");
		scn.MoveCompanionsToTable(sam, farin);
		scn.MoveCardsToHand(sapper);

		scn.StartGame();
		scn.SetTwilight(10);
		// No burdens: Sam's 5 is the lowest, one over.
		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(farin));
		scn.FreepsPass();

		scn.ShadowPlayCard(sapper);
		assertInPlay(sapper);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
