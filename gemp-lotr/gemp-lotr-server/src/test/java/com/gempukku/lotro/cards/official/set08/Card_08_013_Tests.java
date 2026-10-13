package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_13");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is an archery phase
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShakeOfftheShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Shake Off the Shadow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Exert an Elf companion and either a [gandalf] companion or a [dwarven] companion to add an [elven] token here.<br><b>Archery:</b> Remove an [elven] token here to make the fellowship archery total +1 (limit +2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Shake Off the Shadow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ShakeOfftheShadowTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void ArcheryAbilityAtItsLimitStillRemovesATokenAndAddsNothingToTheArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToSupportArea(card);
		scn.AddTokensToCard(card, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals("false", scn.FreepsActionNoEffect(card));
		scn.FreepsUseCardAction(card);
		scn.ShadowPass();
		scn.FreepsUseCardAction(card);
		scn.ShadowPass();
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetCultureTokensOn(card));

		// Limit +2: a token can still be removed, so the ability is still offered, flagged as doing nothing
		// (Ketura on Q-0041).
		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals("limit", scn.FreepsActionNoEffect(card));

		scn.FreepsUseCardAction(card);
		assertEquals(0, scn.GetCultureTokensOn(card));
		assertEquals(2, scn.GetFreepsArcheryTotal());
	}
}
