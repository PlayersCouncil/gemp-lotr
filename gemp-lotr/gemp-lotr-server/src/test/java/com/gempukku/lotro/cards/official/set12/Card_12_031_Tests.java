package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_31");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wizard1", "12_31");
					put("wizard2", "12_31");
					// Gandalf, Friend of the Shirefolk: strength 6, and +1 for each of Hobbit, Dwarf, Elf and Man
					// among the companions: 7 beside the Ring-bearer alone
					put("gandalf", "1_72");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MysteriousWizardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Mysterious Wizard
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>. Make Gandalf strength +2 (or +4 if you cannot spot 5 burdens).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Mysterious Wizard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesGandalfStrengthPlus4WithFourBurdensAndPlus2WithFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var wizard1 = scn.GetFreepsCard("wizard1");
		var wizard2 = scn.GetFreepsCard("wizard2");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wizard1, wizard2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddBurdens(4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(4, scn.GetBurdens());
		// 6, and 1 for the Hobbit who bears the Ring
		assertEquals(7, scn.GetStrength(gandalf));

		// Four burdens: five cannot be spotted.
		scn.FreepsPlayCard(wizard1);
		// 7 + 4
		assertEquals(11, scn.GetStrength(gandalf));
		assertInZone(Zone.DISCARD, wizard1);

		// The fifth burden, and the second copy in the same skirmish.
		scn.ShadowPass();
		scn.AddBurdens(1);
		assertEquals(5, scn.GetBurdens());

		scn.FreepsPlayCard(wizard2);
		// 7 + 4 + 2
		assertEquals(13, scn.GetStrength(gandalf));
		assertInZone(Zone.DISCARD, wizard2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
