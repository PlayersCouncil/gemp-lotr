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

public class Card_04_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_59");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two Elf companions, one of them in a skirmish against an Uruk Savage.
	protected VirtualTableScenario GetTwoElvesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "4_59");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3
					put("elf", "1_53");         // Lorien Elf: Elf companion, strength 4, vitality 2, no text of its own in play
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArrowandBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Arrow and Blade
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert X Elf companions to make a minion skirmishing an unbound companion strength -X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Arrow and Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// "Exert X Elf companions to make a minion ... strength -X": an X of exertions is at least 1 (Q-0076).  The
	// definition let the choice of Elves be answered with none, so the event could be played with no Elf able to
	// exert.
	// F-0446.
	@Test
	public void AtLeastOneElfCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoElvesScenario();

		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(arwen, elf);
		scn.MoveCardsToHand(blade);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		// Both Elves exhausted (Frodo, who is no Elf, could exert): the event cannot be played.
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(elf, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(blade));

		scn.RemoveWoundsFromChar(arwen, 2);
		scn.RemoveWoundsFromChar(elf, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(blade));
		scn.FreepsPlayCard(blade);

		// One Elf at the least, and Frodo is not among them.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(arwen, elf);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(elf));
		assertEquals(3, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ArrowandBladeTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
