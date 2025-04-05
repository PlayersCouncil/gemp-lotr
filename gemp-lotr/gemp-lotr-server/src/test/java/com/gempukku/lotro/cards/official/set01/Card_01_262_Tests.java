package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_262_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("assassin", "1_262");
					put("runner", "1_178");
					put("sam", "2_114");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void OrcAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Assassin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Tracker</b>.<br>The roaming penalty for each [sauron] minion you play is -1.<br><b>Assignment:</b> Spot 2 Hobbit companions to make the Free Peoples player assign a Hobbit to skirmish this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("assassin");

		assertEquals("Orc Assassin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.TRACKER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OrcAssassinDoesNotInterruptSkirmishesAfterUsingAssignmentAbility() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(assassin, runner);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(assassin));
		scn.ShadowUseCardAction(assassin);
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		assertTrue(scn.CanBeAssignedViaAction(sam));
		scn.FreepsChooseCard(sam);
		assertTrue(scn.IsCharAssigned(sam));
		assertTrue(scn.IsCharAssigned(assassin));

		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsCanAssign(sam));
		assertFalse(scn.FreepsCanAssign(assassin));
		assertTrue(scn.FreepsCanAssign(runner));
		assertTrue(scn.FreepsCanAssign(frodo));
		scn.FreepsAssignToMinions(frodo, runner);
		// If beginning skirmishes now fails, then there is some dangling minion that Gemp thinks
		// is unassigned, which isn't right.
		scn.FreepsResolveSkirmish(sam);
	}

	@Test
	public void OrcAssassinDoesNotPreventDefenderBonusFromBeingUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(assassin, runner);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(sam);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(assassin));
		scn.ShadowUseCardAction(assassin);
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		assertTrue(scn.CanBeAssignedViaAction(sam));
		scn.FreepsChooseCard(sam);
		assertTrue(scn.IsCharAssigned(sam));
		assertTrue(scn.IsCharAssigned(assassin));

		scn.PassCurrentPhaseActions();

		assertTrue(scn.hasKeyword(sam, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		assertTrue(scn.FreepsCanAssign(sam));
		scn.FreepsAssignToMinions(sam, runner);
		assertTrue(scn.IsCharAssigned(sam));
		assertTrue(scn.IsCharAssigned(assassin));
		assertTrue(scn.IsCharAssigned(runner));
		// If beginning skirmishes now fails, then there is some issue with defender, which isn't right
		scn.FreepsResolveSkirmish(sam);
	}
}
