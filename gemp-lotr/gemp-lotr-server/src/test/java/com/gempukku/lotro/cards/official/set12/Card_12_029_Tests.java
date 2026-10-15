package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_29");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 4 is White Rocks 4_346, a battleground (its Shadow number counts mounted companions).
	// The only [gandalf] Wizard is Saruman, The White 32_24, an ally of the Hobbit sets whose home is site 3 of
	// the Hobbit path: on this path he has no home site and is at no site (CR 4.2 "ally").  He is the one printed
	// card that tells a Wizard at a battleground from the fellowship at one: every other [gandalf] Wizard is a
	// companion.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("introspection", "12_29");
					put("saruman", "32_24");    // Saruman, The White: [gandalf] Wizard ally, home 3H; a Gandalf is defender +1 beside him, abilities used only on purpose
					put("host", "5_94");        // Thundering Host: a Free Peoples condition in the support area, used only on purpose
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] support-area condition, inert without a [sauron] Orc
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IntrospectionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Introspection
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>. Choose one: Spot a [gandalf] Wizard to choose an opponent who must discard one of his or her conditions from play; or spot a [gandalf] Wizard at a battleground site to discard a condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Introspection", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void IntrospectionTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void AWizardAllyIsAtNoBattlegroundWithTheFellowshipAtOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var introspection = scn.GetFreepsCard("introspection");
		var saruman = scn.GetFreepsCard("saruman");
		var host = scn.GetFreepsCard("host");
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToSupportArea(host);
		scn.MoveCardsToHand(introspection);

		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToSupportArea(bowmen);

		scn.StartGame();
		scn.SkipToSite(4);
		assertInZone(Zone.SUPPORT, bowmen, host);
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		// Saruman is spotted, but at no battleground: the second choice does nothing, so the first is carried out
		// unasked, and the opponent's one condition goes.
		scn.FreepsPlayCard(introspection);
		assertFalse(scn.FreepsDecisionAvailable("Choose"));
		assertInDiscard(bowmen);
		assertInZone(Zone.SUPPORT, host);
	}
}
