package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_71");
					// put other cards in here as needed for the test case
					// Web: [gollum] support possession.  "Regroup: If there are fewer than 3 cards stacked here, spot
					// your Orc or [gollum] minion to stack that minion here.  Shadow: Play a [gollum] minion stacked
					// here as if played from hand."  A [wraith] Orc can be stacked on it by its own text.
					put("web", "8_30");
					// Morgul Lackey: [wraith] Orc, cost 2; its skirmish ability is never used here
					put("lackey1", "7_193");
					put("lackey2", "7_193");
					put("onweb", "7_193");
					put("onflung", "7_193");
					put("inhand", "7_193");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlungIntotheFrayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Flung Into the Fray
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play a minion stacked here, you may exert a companion.<br><b>Regroup:</b> If no minion is stacked here, spot your [wraith] minion to stack it here.<br><b>Shadow:</b> Spot 2 [wraith] minions to play a [wraith] minion stacked here as if played from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Flung Into the Fray", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnlyAWraithMinionStackedOnItselfNotOneStackedOnAnotherCard() throws DecisionResultInvalidException, CardNotFoundException {
		// F-0488: "a [wraith] minion stacked here" is one stacked on Flung Into the Fray, not on any card.
		var scn = GetScenario();

		var flung = scn.GetShadowCard("card");
		var web = scn.GetShadowCard("web");
		var lackey1 = scn.GetShadowCard("lackey1");
		var lackey2 = scn.GetShadowCard("lackey2");
		var onweb = scn.GetShadowCard("onweb");
		var onflung = scn.GetShadowCard("onflung");
		var inhand = scn.GetShadowCard("inhand");

		scn.StartGame();
		scn.MoveCardsToSupportArea(flung, web);
		// The two [wraith] minions to spot
		scn.MoveMinionsToTable(lackey1, lackey2);
		scn.StackCardsOn(web, onweb);
		scn.MoveCardsToHand(inhand);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A [wraith] minion is stacked on Web and none on Flung Into the Fray: nothing for it to play.
		assertFalse(scn.ShadowActionAvailable(flung));

		// One is stacked here as well, and the Shadow player is asked again after a play from hand.
		scn.StackCardsOn(flung, onflung);
		scn.ShadowPlayCard(inhand);
		assertTrue(scn.ShadowActionAvailable(flung));

		// The one stacked here is the only one it can play, and is played with nothing asked.
		scn.ShadowUseCardAction(flung);
		assertInZone(Zone.SHADOW_CHARACTERS, onflung);
		assertInZone(Zone.STACKED, onweb);
		assertTrue(scn.GetStackedCards(web).contains(onweb));
		// "When you play a minion stacked here, you may exert a companion."
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(flung));
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void FlungIntotheFrayTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
