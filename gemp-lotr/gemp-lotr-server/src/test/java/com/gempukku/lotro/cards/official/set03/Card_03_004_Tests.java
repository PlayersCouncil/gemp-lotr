package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("welcome", "3_4");

					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, vitality 3; a skirmish ability only used on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, vitality 3; strength +2 only while skirmishing an Orc
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4: a companion who is no Dwarf
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability only used on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, vitality 2; a maneuver ability only used on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, vitality 3: an ally who is no Dwarf
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
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br><b>Regroup:</b> Exert a Dwarf ally to heal a Dwarf companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("welcome");

		assertEquals("A Royal Welcome", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityExertsADwarfAllyToHealADwarfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir, thrarin, bilbo);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(farin, 1);
		scn.AddWoundsToChar(aragorn, 1);
		// Grimir, wounded once, can still exert, and is a wounded Dwarf who is no companion.
		scn.AddWoundsToChar(grimir, 1);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);

		// The two Dwarf allies; not Bilbo, an ally who is no Dwarf, nor Gimli, a Dwarf who is no ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(grimir, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		// The two wounded Dwarf companions; not Grimir, a wounded Dwarf who is no companion, nor Aragorn, a wounded
		// companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// One wound healed of his two.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenTheOnlyDwarfAllyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir, bilbo);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 1);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(grimir, 2);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.SkipToPhase(Phase.REGROUP);

		// Bilbo could exert but is no Dwarf, and Gimli could exert but is no ally; Gimli is there to heal.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(welcome));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(grimir));
	}

	@Test
	public void WithNoWoundedDwarfCompanionTheAllyExertsAndNothingIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.SkipToPhase(Phase.REGROUP);

		// The ability has a cost, so it is offered though no Dwarf companion is wounded.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);

		// Grimir, the one Dwarf ally, exerts unasked; Aragorn's wound is not healed.
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
