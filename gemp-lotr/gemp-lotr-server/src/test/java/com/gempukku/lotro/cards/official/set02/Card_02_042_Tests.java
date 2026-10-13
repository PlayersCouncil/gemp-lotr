package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_42");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetLookScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblinman", "2_42");
					// The Free Peoples player's hand: a companion, a possession and a minion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("armor", "1_8");        // Dwarven Armor
					put("runner", "1_178");     // Goblin Runner
					put("gimli", "1_13");       // Gimli, Son of Glóin: left in the Free Peoples draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinManStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Goblin Man
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 2
		 * Game Text: Discard this minion if underground.<br><b>Shadow:</b> Exert this minion to look at the Free Peoples player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Goblin Man", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsThisMinionAndShowsTheFreePeoplesPlayersHandToItsOwnerAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLookScenario();

		var goblinMan = scn.GetShadowCard("goblinman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(aragorn, armor, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinMan);
		// To Ettenmoors, which is not underground.
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, gimli);
		assertEquals(0, scn.GetWoundsOn(goblinMan));
		assertTrue(scn.ShadowActionAvailable(goblinMan));

		scn.ShadowUseCardAction(goblinMan);
		assertEquals(1, scn.GetWoundsOn(goblinMan));
		// The Shadow player is shown the whole hand, with nothing in it to choose, and no card from the draw deck.
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(gimli, false));
		assertEquals(0, scn.ShadowGetSelectableCount());
		// A look is the looker's alone: the hand's owner is shown nothing and asked nothing.
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		scn.ShadowDismissRevealedCards();
		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, aragorn, armor, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
