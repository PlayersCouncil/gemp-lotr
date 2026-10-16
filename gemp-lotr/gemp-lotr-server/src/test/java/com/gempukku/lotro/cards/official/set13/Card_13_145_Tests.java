package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_145_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_145");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows path: the fellowship moves from Fangorn Glade 11_239 to Buckland Homestead 11_230, a dwelling with no
	// text, where the skirmishes are.  Allies have no home sites on a Shadows path (CR 4.2 "ally"), so Rosie
	// Cotton is at no site.  No Hobbit ally has a home that is a dwelling.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("letgo", "13_145");
					put("letgo2", "13_145");
					put("rosie", "1_309");      // Rosie Cotton, Hobbiton Lass: Hobbit ally, home 1F, strength 1; "Sam is strength +1", and a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void DontLetGoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Don't Let Go
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Discard this condition to make a Hobbit strength +1 for each of the following that is true: he or she is participating in a fierce skirmish; he or she has resistance 7 or more; he or she is at a dwelling site; you can spot an unwounded Hobbit.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Don't Let Go", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void DontLetGoTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void AnAllyIsAtNoDwellingWithTheFellowshipAtOneOnAShadowsPath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var letgo = scn.GetFreepsCard("letgo");
		var letgo2 = scn.GetFreepsCard("letgo2");
		var frodo = scn.GetRingBearer();
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveAlliesToTable(rosie);
		scn.MoveCardsToSupportArea(letgo, letgo2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site6"));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Buckland Homestead", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.DWELLING));
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		int frodoStrength = scn.GetStrength(frodo);
		assertEquals(1, scn.GetStrength(rosie));

		// Frodo: at the dwelling, with the fellowship; resistance 7 or more; an unwounded Hobbit spotted: +3.
		scn.FreepsUseCardAction(letgo);
		scn.FreepsChooseCard(frodo);
		assertEquals(frodoStrength + 3, scn.GetStrength(frodo));

		// Rosie is at no site: of the four, an unwounded Hobbit spotted alone: +1.
		scn.ShadowPass();
		scn.FreepsUseCardAction(letgo2);
		scn.FreepsChooseCard(rosie);
		assertEquals(2, scn.GetStrength(rosie));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
