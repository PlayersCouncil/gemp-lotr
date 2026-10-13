package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_208_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_208");
					put("tracker", "4_193");    // Uruk Runner: [isengard] tracker, strength 8, vitality 2; stronger in a fierce skirmish
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					// Dwarven Armor: a possession that changes no number, put on Aragorn by the rig
					put("armor1", "1_8");
					put("armor2", "1_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VengeanceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Vengeance
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an [isengard] tracker strength +2 for each card borne by the character it is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Vengeance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void VengeanceTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void AHinderedCardBorneByTheCharacterTheTrackerIsSkirmishingIsCountedWithTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vengeance = scn.GetShadowCard("card");
		var tracker = scn.GetShadowCard("tracker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor1 = scn.GetFreepsCard("armor1");
		var armor2 = scn.GetFreepsCard("armor2");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor1, armor2);
		scn.MoveCardsToHand(vengeance);

		scn.StartGame();
		scn.MoveMinionsToTable(tracker);
		// A hindered card is a card still, and still borne
		scn.HinderCard(armor2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, tracker);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(armor2));
		assertEquals(8, scn.GetStrength(tracker));

		scn.ShadowPlayCard(vengeance);
		// 8 + 2 for each of the two cards Aragorn bears
		assertEquals(12, scn.GetStrength(tracker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
