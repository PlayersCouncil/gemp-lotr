package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					put("bow", "1_90");         // Aragorn's Bow: makes Aragorn an archer, with an Archery ability of its own
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, vitality 1
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, Damage +1 and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Legolas, Greenleaf
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Archer.
		 * Archery: Exert Legolas to wound a minion; Legolas does not add to the fellowship archery total.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Greenleaf", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void ArcheryAbilityExertsLegolasToWoundAMinionAndHeAddsNothingToTheArcheryTotalForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(aragorn, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		// Legolas and Aragorn, an archer by the Bow, each add 1.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Legolas no longer adds; Aragorn still does.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// The one archery wound of the fellowship is placed by the Shadow player, on the Uruk-hai.
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));

		// Once the archery phase is over, Legolas counts again.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileLegolasIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(aragorn, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(legolas, 2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
		// The Bow's ability, with Aragorn able to exert, is offered beside it.
		assertTrue(scn.FreepsActionAvailable(bow));
	}
}
