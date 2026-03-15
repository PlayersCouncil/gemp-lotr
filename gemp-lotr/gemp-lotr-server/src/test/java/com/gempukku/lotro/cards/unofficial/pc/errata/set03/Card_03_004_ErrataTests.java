package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_004_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("welcome", "53_4");
					put("gimli", "1_12");   // Gimli, Dwarf of Erebor (Dwarven companion, STR 6, VIT 3)
					put("guard", "1_7");    // Dwarf Guard (unbound Dwarven companion, STR 4, VIT 2)
					put("grimir", "1_17");  // Grimir, Dwarven Elder (Dwarven ally, STR 3, VIT 3)
					put("runner", "1_178"); // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ARoyalWelcomeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: A Royal Welcome
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Fellowship: Exert a [dwarven] companion to heal a [dwarven] ally.
		 * 	Skirmish: Exert a [dwarven] ally to add that ally's strength or <b>damage</b> bonuses
		 *  to an unbound [dwarven] companion (limit once per phase).
		 * 	Regroup: Exert a [dwarven] ally to heal a [dwarven] companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("welcome");

		assertEquals("A Royal Welcome", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityExertsCompanionToHealAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");

		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCardsToSupportArea(grimir);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		// Wound Grimir so we can verify the heal
		scn.AddWoundsToChar(grimir, 1);
		assertEquals(1, scn.GetWoundsOn(grimir));

		// Use fellowship ability
		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);

		// Only one Dwarven companion (Gimli) and one Dwarven ally (Grimir) -- auto-selected
		assertEquals(1, scn.GetWoundsOn(gimli)); // Gimli exerted
		assertEquals(0, scn.GetWoundsOn(grimir)); // Grimir healed
	}

	@Test
	public void SkirmishAbilityAddsAllyStrengthToCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCardsToSupportArea(grimir);
		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, runner);

		// Guard base STR = 4, Grimir STR = 3
		assertEquals(4, scn.GetStrength(guard));

		// Use skirmish ability
		scn.FreepsUseCardAction(welcome);
		// Choose "Add strength bonus"
		scn.FreepsChoose("Add strength bonus");
		// Guard should now be STR 4 + 3 = 7
		assertEquals(7, scn.GetStrength(guard));
	}

	@Test
	public void RegroupAbilityExertsAllyToHealCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");

		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCardsToSupportArea(grimir);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		// Wound Gimli so we can verify the heal
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.SkipToPhase(Phase.REGROUP);

		// Use regroup ability
		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);

		// Only one Dwarven ally (Grimir) and one wounded Dwarven companion (Gimli)
		assertEquals(1, scn.GetWoundsOn(grimir)); // Grimir exerted
		assertEquals(0, scn.GetWoundsOn(gimli));  // Gimli healed
	}
}
