package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("demands", "2_40");
					put("savage", "1_151");

					put("celeborn", "1_34");
					put("rosie", "1_309");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DemandsofthebrSackvilleBagginsesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Demands of the Sackville-Bagginses
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, spot an [isengard] minion. Plays to your support area.<br>Each time a [shire] ally exerts, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("demands");

		assertEquals("Demands of the Sackville-Bagginses", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DemandsoftheSackvilleBagginsesRequiresIsengardMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var demands = scn.GetShadowCard("demands");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(demands, savage);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(demands));
		scn.ShadowPlayCard(savage);
		assertTrue(scn.ShadowPlayAvailable(demands));
	}


	@Test
	public void DemandsoftheSackvilleBagginsesAddsNothingWhenNonShireAllyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCardsToSupportArea(celeborn);

		var demands = scn.GetShadowCard("demands");
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(celeborn);
		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void DemandsoftheSackvilleBagginsesAdds1WhenShireAllyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCardsToSupportArea(rosie);

		var demands = scn.GetShadowCard("demands");
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(rosie);
		assertEquals(1, scn.GetTwilight());
	}
}
