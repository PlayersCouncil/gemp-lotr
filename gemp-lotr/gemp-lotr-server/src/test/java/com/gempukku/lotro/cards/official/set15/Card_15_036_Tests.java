package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_36");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shepherd", "15_36");
					put("axe", "1_3");          // Axe Strike: the top card of the draw deck
					// Cleaving Blow: the bottom card, a [dwarven] event and so no [gandalf] character to play
					put("blow", "1_5");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShepherdoftheTreesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Shepherd of the Trees
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: While you can spot 4 Ents, this companion is strength +3.<br><b>Maneuver:</b> Exert this companion to reveal the bottom card of your draw deck. If it is a [gandalf] character, you may play it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Shepherd of the Trees", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ManeuverAbilityExertsThisCompanionToRevealTheBottomCardOfItsOwnersDrawDeckToBothPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var shepherd = scn.GetFreepsCard("shepherd");
		var axe = scn.GetFreepsCard("axe");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(shepherd);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(axe);
		scn.MoveCardsToBottomOfDeck(blow);
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertSame(blow, scn.GetFreepsBottomOfDeck());
		assertEquals(0, scn.GetWoundsOn(shepherd));
		assertTrue(scn.FreepsActionAvailable(shepherd));

		scn.FreepsUseCardAction(shepherd);
		assertEquals(1, scn.GetWoundsOn(shepherd));
		// Each player is shown the bottom card, and not the top one.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(blow, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(axe, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(blow, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(axe, false));

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		// It is no [gandalf] character: nothing is played, and it lies where it lay.
		assertSame(blow, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, blow);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
