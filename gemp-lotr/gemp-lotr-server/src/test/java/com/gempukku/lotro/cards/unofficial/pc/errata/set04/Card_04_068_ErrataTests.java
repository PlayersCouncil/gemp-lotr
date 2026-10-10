package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_068_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ferevellon", "54_68");
					put("lorien", "1_53");      // Lorien Elf: Elf companion, strength 4, vitality 2, no text in play
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, no requirement to play; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion: a companion who is no Elf
					put("finalcount", "4_69");  // Final Count: [elven] condition in the support area, a card of the culture that is no Elf; nothing without Legolas

					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its when-played trigger does nothing when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FerevellonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Ferevellon, Son of Thandronen
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Valiant.
		 * To play, spot an Elf.
		 * Skirmish: Exert Ferevellon to make a minion skirmishing him strength -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ferevellon");

		assertEquals("Ferevellon", card.getBlueprint().getTitle());
		assertEquals("Son of Thandronen", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedAndThenPlaysForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferevellon = scn.GetFreepsCard("ferevellon");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var finalcount = scn.GetFreepsCard("finalcount");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(ferevellon, orophin);

		scn.StartGame();

		// A companion who is no Elf and an [elven] condition in play, and no Elf.
		assertFalse(scn.FreepsPlayAvailable(ferevellon));
		scn.FreepsPlayCard(orophin);
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.FreepsPlayAvailable(ferevellon));
		scn.FreepsPlayCard(ferevellon);
		// 2 for Orophin + 2 for Ferevellon
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, ferevellon);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyElfIsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferevellon = scn.GetFreepsCard("ferevellon");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(lorien);
		scn.MoveCardsToHand(ferevellon);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(ferevellon));
		scn.FreepsPlayCard(ferevellon);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, ferevellon);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsHimToMakeAMinionHeIsSkirmishingStrengthMinusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferevellon = scn.GetFreepsCard("ferevellon");
		var lorien = scn.GetFreepsCard("lorien");
		var patrol = scn.GetShadowCard("patrol");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(ferevellon, lorien);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, savage, runner);
		scn.SkipToAssignments();
		// The runner is assigned to Lorien Elf, whose skirmish has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ferevellon, patrol}, new PhysicalCardImpl[]{lorien, runner});
		// The Free Peoples player assigns one minion to each companion; the Shadow player adds the second.
		scn.ShadowAssignToMinions(ferevellon, savage);
		scn.FreepsResolveSkirmish(ferevellon);

		assertEquals(0, scn.GetWoundsOn(ferevellon));
		assertEquals(13, scn.GetStrength(patrol));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ferevellon));

		scn.FreepsUseCardAction(ferevellon);
		assertEquals(1, scn.GetWoundsOn(ferevellon));
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(patrol);

		assertEquals(11, scn.GetStrength(patrol));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(4, scn.GetStrength(lorien));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void StrengthPenaltyLastsUntilHisSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferevellon = scn.GetFreepsCard("ferevellon");
		var lorien = scn.GetFreepsCard("lorien");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(ferevellon, lorien);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ferevellon, savage}, new PhysicalCardImpl[]{lorien, runner});
		scn.FreepsResolveSkirmish(ferevellon);

		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsUseCardAction(ferevellon);
		// The Savage is the only minion in his skirmish, so it is chosen without asking.
		assertEquals(3, scn.GetStrength(savage));
		scn.ShadowPass();

		// Still in his skirmish, the last decision before it resolves.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsPass();

		// 5 against 3: Ferevellon wins and the Savage takes a wound (a tie at 5 would have gone to the Shadow side).
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(ferevellon));
		scn.FreepsResolveSkirmish(lorien);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage));
	}

	@Test
	public void NotOfferedWhenHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferevellon = scn.GetFreepsCard("ferevellon");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(ferevellon);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(ferevellon, 2);
		scn.FreepsAssignToMinions(ferevellon, savage);
		scn.FreepsResolveSkirmish(ferevellon);

		assertEquals(2, scn.GetWoundsOn(ferevellon));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ferevellon));
	}
}
