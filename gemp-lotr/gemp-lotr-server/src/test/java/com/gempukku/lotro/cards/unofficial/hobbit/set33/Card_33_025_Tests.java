package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("braga", "33_25");
					// Goblin Runner: an Orc.  Its "when you play" text does nothing to a card put into play.
					put("runner", "1_178");
					// Uruk Savage: no Orc and no Man; Damage +1 is its whole text.  A known card to lie on top
					// of the Shadow player's draw deck.
					put("savage", "1_151");
					// Aragorn, Ranger of the North: strength 8, vitality 4.  He fights Braga and is not
					// overwhelmed at 9 or at 12; his own copy in the other deck is the card beneath the Savage.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BragaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Braga, Captain of the Guard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Fierce.  (No sheet has the Hobbit sets and the definition carries no game text: these are
		 * 	the definition's own fields.)
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("braga");

		assertEquals("Braga", card.getBlueprint().getTitle());
		assertEquals("Captain of the Guard", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	// From the definition, there being no text to go by: "Skirmish: Place an Orc from play beneath your draw deck
	// to make a [men] Man strength +3 until the regroup phase."
	@Test
	public void PlacesAnOrcFromPlayBeneathItsOwnersDrawDeckToMakeAMenManStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var braga = scn.GetShadowCard("braga");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var bottom = scn.GetShadowCard("aragorn");

		scn.StartGame();
		scn.MoveMinionsToTable(braga, runner);
		// The Shadow player's draw deck is now two cards: his own Aragorn, with the Uruk Savage on top.
		scn.MoveCardsToTopOfDeck(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, braga);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(braga));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(2, scn.GetShadowDeckCount());
		assertSame(savage, scn.GetShadowTopOfDeck());
		assertSame(bottom, scn.GetShadowBottomOfDeck());
		assertEquals(9, scn.GetStrength(braga));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.ShadowUseCardAction(braga);

		// The Runner is the only Orc and Braga the only [men] Man: nothing is asked.
		assertInZone(Zone.DECK, runner);
		assertEquals(3, scn.GetShadowDeckCount());
		assertSame(runner, scn.GetShadowBottomOfDeck());
		assertSame(savage, scn.GetShadowTopOfDeck());
		assertEquals(12, scn.GetStrength(braga));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
