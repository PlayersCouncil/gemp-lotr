package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "9_45");
					put("merry", "1_302");      // Merry, Friend to Sam: vitality 4, Frodo signet; a skirmish ability only
					put("pippin", "1_306");     // Pippin, Friend to Frodo: vitality 4, Frodo signet; his text is about [shire] tales
					// Théoden, Tall and Proud: vitality 3, Aragorn signet; valiant, and a trigger when he is killed
					put("theoden", "8_92");
					// I Am Here, cost 3: "Fellowship: Exert Théoden twice to heal every other companion with the
					// Théoden signet twice."
					put("iamhere", "5_84");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornoftheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Horn of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: 
		 * Strength: 1
		 * Game Text: Bearer must be Merry or Pippin.<br>Bearer gains the Théoden signet.<br><b>Response:</b> If bearer wins a skirmish, exert him to discard each minion involved in that skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Horn of the Mark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerHasTheTheodenSignetForACardThatReadsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo, Son of Drogo: "Fellowship: Exert another companion who has the Frodo signet to heal Frodo."
		var frodo = scn.GetRingBearer();
		var horn = scn.GetFreepsCard("card");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var theoden = scn.GetFreepsCard("theoden");
		var iamhere = scn.GetFreepsCard("iamhere");
		scn.MoveCompanionsToTable(merry, pippin, theoden);
		scn.MoveCardsToHand(horn, iamhere);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(frodo, 1);

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(horn));
		scn.FreepsPlayCard(horn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, theoden));
		scn.FreepsChooseCard(merry);
		assertAttachedTo(horn, merry);
		assertEquals(1, scn.GetTwilight());

		// "Gains": the signet printed on him is still his.  Frodo's ability, which reads the Frodo signet, takes
		// either Hobbit, and not Théoden, who is unwounded here and could exert (his signet is Aragorn's).
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsActionAvailable(frodo));
		scn.FreepsUseCardAction(frodo);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden));
		scn.FreepsChooseCard(merry);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Neither Hobbit has the Théoden signet in print.  The one bearing the Horn is healed twice by a card
		// that heals companions with that signet; the other, with the same printed signet, is not.
		assertTrue(scn.FreepsPlayAvailable(iamhere));
		scn.FreepsPlayCard(iamhere);
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// 1 for the Horn + 3 for the event
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
