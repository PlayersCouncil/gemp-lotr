package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_191_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_191");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The site itself on a Shadows-block path, as the first site, where the game starts.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					// Dwarven Armor: a possession that changes no number, put on Aragorn by the rig
					put("armor1", "1_8");
					put("armor2", "1_8");
				}},
				new HashMap<>()
				{{
					put("site1", "13_191");     // Fords of Isen
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void FordsofIsenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Fords of Isen
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 2
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>River</b>. Each character is strength +1 for each card borne by that character.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite();
		var card = scn.GetFreepsCard("card");

		assertEquals("Fords of Isen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void FordsofIsenTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void AHinderedCardACharacterBearsStrengthensHimLikeTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var armor1 = scn.GetFreepsCard("armor1");
		var armor2 = scn.GetFreepsCard("armor2");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("Fords of Isen", scn.GetCurrentSite().getBlueprint().getTitle());

		assertEquals(8, scn.GetStrength(aragorn));
		scn.AttachCardsTo(aragorn, armor1, armor2);
		// 8 + 1 for each of two cards
		assertEquals(10, scn.GetStrength(aragorn));

		// A hindered card is a card still, and still borne
		scn.HinderCard(armor2);
		assertTrue(scn.IsHindered(armor2));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
