package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_49");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetManeuverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_49");
					// Saruman's Ambition: an [isengard] condition in the Shadow support area; its texts are about
					// [isengard] events and a skirmish
					put("ambition", "1_133");
					// Lingering Shadow: a [wraith] condition in the Shadow support area; its ability needs a skirmish
					put("shadow", "12_166");
					// Ships of Great Draught: a [raider] possession in the Shadow support area, a Shadow card that
					// is no condition; a Shadow and a Regroup ability
					put("ships", "8_65");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("runner", "1_178");     // Goblin Runner: the known card on top of the Shadow draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GladdenHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Gladden Homestead
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot Sméagol.<br>When you play this, add a [gollum] token here for each [gollum] card you can spot.<br><b>Maneuver:</b> Discard this from play or remove 2 tokens from here to spot a Shadow condition and place that condition on top of its owner's draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gladden Homestead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardedToPlaceAShadowConditionOnTopOfItsOwnersDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetManeuverScenario();

		// In play with no token on it, so that discarding it is the only way to pay.
		var homestead = scn.GetFreepsCard("homestead");
		scn.MoveCardsToSupportArea(homestead);

		var ambition = scn.GetShadowCard("ambition");
		var shadow = scn.GetShadowCard("shadow");
		var ships = scn.GetShadowCard("ships");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(ambition, shadow, ships);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(runner);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(homestead));
		assertEquals(runner, scn.GetShadowTopOfDeck());
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		// Nothing is asked about the cost: the Homestead is discarded, and the condition is then chosen.
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		assertInDiscard(homestead);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ambition, shadow));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ships));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(ambition);

		// It lies on top of its owner's draw deck, the card that was on top now under it.
		assertEquals(ambition, scn.GetShadowTopOfDeck());
		assertEquals(runner, scn.GetFromTopOfShadowDeck(2));
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertInZone(Zone.SUPPORT, shadow, ships);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
