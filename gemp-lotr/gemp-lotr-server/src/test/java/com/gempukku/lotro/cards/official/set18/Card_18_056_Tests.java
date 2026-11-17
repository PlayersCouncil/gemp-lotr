package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger1", "18_56");
					put("ranger2", "18_56");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6; Knight is its whole text
					// Garrison of Osgiliath: [gondor] support-area condition; "When you play this condition, you may
					// spot a [gondor] Man to place 2 [gondor] tokens here." Its skirmish ability is not used.
					put("garrison", "6_52");
					// Stout and Strong: [dwarven] support-area condition that takes [dwarven] tokens as Dwarves win
					// skirmishes; its skirmish ability is not used
					put("stout", "4_57");
					// Ithilien Trap: [gondor] support-area condition that takes [gondor] tokens only as [gondor] Men
					// win skirmishes, here with none; its maneuver ability is not used
					put("trap", "4_126");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RangeroftheSouthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Ranger of the South
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>.<br>While you can spot a [gondor] token, this companion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ranger1");

		assertEquals("Ranger of the South", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void EachCopyIsStrengthPlus2WhileAGondorTokenCanBeSpottedAndNotOnceTheTokensAreGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var knight = scn.GetFreepsCard("knight");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(ranger1, ranger2, knight);
		scn.MoveCardsToHand(garrison);

		scn.StartGame();

		assertEquals(5, scn.GetStrength(ranger1));
		assertEquals(5, scn.GetStrength(ranger2));
		assertEquals(6, scn.GetStrength(knight));

		// Garrison of Osgiliath played by legal play, spotting a [gondor] Man for its 2 [gondor] tokens.
		scn.FreepsPlayCard(garrison);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(garrison));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		// Each Ranger is +2 for himself alone (7, not 9 for the two copies), and the Knight beside them is unchanged.
		assertEquals(7, scn.GetStrength(ranger1));
		assertEquals(7, scn.GetStrength(ranger2));
		assertEquals(6, scn.GetStrength(knight));

		// The tokens taken off to the last: no [gondor] token is left to spot.
		scn.RemoveTokensFromCard(garrison, 2);
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(5, scn.GetStrength(ranger1));
		assertEquals(5, scn.GetStrength(ranger2));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ATokenOfAnotherCultureOrAGondorConditionWithNoTokenDoesNotMakeHimStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var stout = scn.GetFreepsCard("stout");
		var trap = scn.GetFreepsCard("trap");
		scn.MoveCompanionsToTable(ranger1);
		scn.MoveCardsToSupportArea(stout, trap);
		// A [dwarven] token, as a Dwarf's win would put there.
		scn.AddTokensToCard(stout, 1);
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(trap));

		scn.StartGame();

		assertEquals(5, scn.GetStrength(ranger1));

		// Two [gondor] tokens on the [gondor] Trap, as a [gondor] Man's wins would put there: now he is +2.
		scn.AddTokensToCard(trap, 2);
		assertEquals(2, scn.GetCultureTokensOn(trap));
		assertEquals(7, scn.GetStrength(ranger1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
