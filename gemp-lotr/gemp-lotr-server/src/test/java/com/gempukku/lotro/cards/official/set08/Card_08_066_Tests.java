package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_08_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "8_66");

					put("ships1", "8_65");      // Ships of Great Draught: [raider] support-area possession to hold [raider] tokens; its abilities are never used
					put("ships2", "8_65");
					put("buccaneer", "8_53");   // Corsair Buccaneer: [raider] corsair Man, strength 8; his play trigger is not met here (he is cheated in)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WindThatSpedShipsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Wind That Sped Ships
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a corsair strength +1 for each [raider] token you spot (limit +6). If you have initiative, you may place this event on top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wind");

		assertEquals("Wind That Sped Ships", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheNumberOfTokensToSpotComesFilledInWithAllUpToTheLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wind = scn.GetShadowCard("wind");
		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var buccaneer = scn.GetShadowCard("buccaneer");
		scn.MoveCardsToHand(wind);

		scn.StartGame();
		scn.MoveCardsToSupportArea(ships1, ships2);
		// Seven [raider] tokens, over two cards: more than the limit of +6.
		scn.AddTokensToCard(ships1, 4);
		scn.AddTokensToCard(ships2, 3);
		scn.MoveMinionsToTable(buccaneer);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, buccaneer);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(buccaneer));
		scn.ShadowPlayCard(wind);

		// The player names how many tokens he spots, 0 to the limit; the box comes filled in with the most
		// (Q-0341: A, as F-0561), and the player accepts it.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(6, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(6), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("6");

		assertEquals(14, scn.GetStrength(buccaneer));
		// The Shadow player has initiative (the Free Peoples hand is empty) and is offered the deck; he declines.
		scn.ShadowChooseNo();
		assertInZone(Zone.DISCARD, wind);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
