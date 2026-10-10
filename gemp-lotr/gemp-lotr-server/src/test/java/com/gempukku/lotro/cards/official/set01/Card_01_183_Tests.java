package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_183_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_183");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackedOrcsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swarms", "1_183");
					// Goblin Runner: [moria] Orc, cost 1, site 4.  "When you play this minion, you may add (2)",
					// which the game adds without asking: something that happens only to a card that is played.
					put("runner", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinSwarmsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Swarms
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br><b>Response:</b> If your [moria] Orc wins a skirmish, discard cards and wounds on that Orc and stack that Orc on this condition.<br><b>Shadow:</b> Play an Orc stacked here as if played from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Goblin Swarms", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityPlaysAnOrcStackedHereAsIfFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackedOrcsScenario();

		var swarms = scn.GetShadowCard("swarms");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, runner, troop);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for Ettenmoors: enough for either Orc (the Troop is 6 + 2 roaming)
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.STACKED, runner, troop);
		assertTrue(scn.ShadowActionAvailable(swarms));

		scn.ShadowUseCardAction(swarms);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(runner);
		// 10 - 1 for the Runner - 2 roaming (a site 4 minion at site 2) + 2 the Runner adds on being played
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.STACKED, troop);
		assertEquals(1, scn.GetStackedCards(swarms).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
