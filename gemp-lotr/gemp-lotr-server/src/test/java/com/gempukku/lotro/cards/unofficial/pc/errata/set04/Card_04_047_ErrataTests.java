package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_047_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armory", "54_47");
					put("gimli", "1_12");    // Gimli, Dwarf of Erebor (Dwarf companion, STR 6, VIT 3)
					put("guard", "1_7");     // Dwarf Guard (FP card to stack from hand)
					put("runner", "1_178");  // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FromTheArmoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: From the Armory
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Exert a Dwarf and stack a Free Peoples card from hand
		 *  here to make that Dwarf unable to take wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armory");

		assertEquals("From the Armory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ArmoryMakesDwarfUnableToTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(guard);  // FP card in hand to stack
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		// During skirmish, use From the Armory
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);
		// Exert Gimli (only Dwarf -- auto-selected)
		// Choose FP card from hand to stack--Dwarf Guard selected automatically

		// Gimli should have 1 wound (from exertion cost)
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Now resolve the skirmish -- Gimli should not take additional wounds
		scn.PassSkirmishActions();
		// Gimli should still only have 1 wound (the exertion), no combat wounds
		assertEquals(1, scn.GetWoundsOn(gimli));
	}
}
