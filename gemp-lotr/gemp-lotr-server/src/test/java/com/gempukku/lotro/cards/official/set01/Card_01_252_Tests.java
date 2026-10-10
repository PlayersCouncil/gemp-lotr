package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_252_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_252");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetLastCardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The Free Peoples player's own copy of this card is the one card of his draw deck
					put("shadow", "1_252");
					// Elrond, Lord of Rivendell: "Fellowship: Exert Elrond to draw a card."  His start-of-turn heal
					// reaches allies of site 3 only.
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFifthBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "1_252");
					// Goblin Runner: strength 5, which beats Frodo (3, and 1 for the Ring) by one, so that the
					// Ring can be put on and turn the wound into a burden
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheIrresistibleShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Irresistible Shadow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert a [sauron] Orc. Plays to your support area.<br>If you can spot 5 burdens and the Free Peoples player has no cards in his or her draw deck, the Ring-bearer is corrupted.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("The Irresistible Shadow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheRingBearerIsCorruptedAtFiveBurdensOnceTheFreePeoplesDrawDeckIsEmptyAndNotWhileItHoldsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLastCardScenario();

		var shadow = scn.GetShadowCard("shadow");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToSupportArea(shadow);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AddBurdens(5);

		// Five burdens, which Frodo's resistance of 10 bears, and one card left to draw: the game goes on.
		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.MarkGameLog();
		scn.FreepsUseCardAction(elrond);
		// The last card is drawn, and with it the game is lost.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetBurdens());
		assertTrue(scn.GameIsFinished());
		// The game's own words for it; a game that ended for any other reason would not say them
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
		assertFalse(scn.GameLogSinceMarkSays(scn.P2() + " lost"));
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyTheRingBearerIsCorruptedAtTheFifthBurdenAndNotAtFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFifthBurdenScenario();

		var shadow = scn.GetShadowCard("shadow");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();
		// The Free Peoples player's copies of the two cards are his whole draw deck
		scn.MoveCardsToDiscard(scn.GetFreepsCard("shadow"), scn.GetFreepsCard("runner"));
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// No card in the draw deck and four burdens, through four phases: the game goes on.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetBurdens());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));

		// Frodo puts on the Ring, which makes a burden of the wound: the fifth.
		scn.MarkGameLog();
		scn.FreepsChooseOptionalTrigger(ring);
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
		assertFalse(scn.GameLogSinceMarkSays(scn.P2() + " lost"));
	}
}
