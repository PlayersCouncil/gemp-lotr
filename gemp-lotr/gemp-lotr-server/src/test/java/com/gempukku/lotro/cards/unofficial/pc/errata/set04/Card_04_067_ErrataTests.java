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

public class Card_04_067_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fereveldir", "54_67");
					put("lorien", "1_53");      // Lorien Elf: Elf companion, vitality 2, no text in play
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, no requirement to play; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion: a companion who is no Elf

					put("finalcount", "4_69");  // Final Count: [elven] condition in the support area; nothing without Legolas
					put("uncertain", "11_26");  // Uncertain Future: [elven] condition in the support area; a maneuver ability only
					put("strands", "9_22");     // Strands of Elven Hair: [elven] possession in the support area; a fellowship ability only
					put("axe", "4_52");         // My Axe Is Notched: [dwarven] condition in the support area; nothing without Gimli

					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its when-played trigger does nothing when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FereveldirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Fereveldir, Son of Thandronen
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
		 * Skirmish: Exert Fereveldir and discard an [elven] token from your condition to wound a minion Fereveldir is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fereveldir");

		assertEquals("Fereveldir", card.getBlueprint().getTitle());
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

		var fereveldir = scn.GetFreepsCard("fereveldir");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var finalcount = scn.GetFreepsCard("finalcount");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(fereveldir, orophin);

		scn.StartGame();

		// A companion who is no Elf and an [elven] condition in play, and no Elf.
		assertFalse(scn.FreepsPlayAvailable(fereveldir));
		scn.FreepsPlayCard(orophin);
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.FreepsPlayAvailable(fereveldir));
		scn.FreepsPlayCard(fereveldir);
		assertInZone(Zone.FREE_CHARACTERS, fereveldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyElfIsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fereveldir = scn.GetFreepsCard("fereveldir");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(lorien);
		scn.MoveCardsToHand(fereveldir);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(fereveldir));
		scn.FreepsPlayCard(fereveldir);
		assertInZone(Zone.FREE_CHARACTERS, fereveldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsHimAndDiscardsAnElvenTokenFromYourConditionToWoundAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fereveldir = scn.GetFreepsCard("fereveldir");
		var lorien = scn.GetFreepsCard("lorien");
		var finalcount = scn.GetFreepsCard("finalcount");
		var uncertain = scn.GetFreepsCard("uncertain");
		var strands = scn.GetFreepsCard("strands");
		var axe = scn.GetFreepsCard("axe");
		var patrol = scn.GetShadowCard("patrol");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(fereveldir, lorien);
		scn.MoveCardsToSupportArea(finalcount, uncertain, strands, axe);
		scn.AddTokensToCard(finalcount, 2);
		scn.AddTokensToCard(uncertain, 2);
		// An [elven] possession with [elven] tokens, and a condition with tokens of another culture.
		scn.AddTokensToCard(strands, 2);
		scn.AddTokensToCard(axe, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, savage, runner);
		scn.SkipToAssignments();
		// The runner is assigned to Lorien Elf, whose skirmish has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{fereveldir, patrol}, new PhysicalCardImpl[]{lorien, runner});
		// The Free Peoples player assigns one minion to each companion; the Shadow player adds the second.
		scn.ShadowAssignToMinions(fereveldir, savage);
		scn.FreepsResolveSkirmish(fereveldir);

		assertEquals(0, scn.GetWoundsOn(fereveldir));
		assertEquals(2, scn.GetCultureTokensOn(finalcount));
		assertEquals(2, scn.GetCultureTokensOn(uncertain));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(fereveldir));

		scn.FreepsUseCardAction(fereveldir);
		assertEquals(1, scn.GetWoundsOn(fereveldir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(finalcount, uncertain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(strands, axe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(uncertain);
		assertEquals(1, scn.GetCultureTokensOn(uncertain));
		assertEquals(2, scn.GetCultureTokensOn(finalcount));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetCultureTokensOn(axe));

		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(lorien));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhenHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fereveldir = scn.GetFreepsCard("fereveldir");
		var finalcount = scn.GetFreepsCard("finalcount");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable(fereveldir);
		scn.MoveCardsToSupportArea(finalcount);
		scn.AddTokensToCard(finalcount, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToAssignments();
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(fereveldir, 2);
		scn.FreepsAssignToMinions(fereveldir, patrol);
		scn.FreepsResolveSkirmish(fereveldir);

		assertEquals(2, scn.GetWoundsOn(fereveldir));
		assertEquals(2, scn.GetCultureTokensOn(finalcount));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(fereveldir));
	}

	@Test
	public void NotOfferedWhenTheOnlyElvenTokensAreOnAPossessionAndTheOnlyConditionHasTokensOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fereveldir = scn.GetFreepsCard("fereveldir");
		var finalcount = scn.GetFreepsCard("finalcount");
		var strands = scn.GetFreepsCard("strands");
		var axe = scn.GetFreepsCard("axe");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable(fereveldir);
		// Final Count is an [elven] condition, but bears no tokens.
		scn.MoveCardsToSupportArea(finalcount, strands, axe);
		scn.AddTokensToCard(strands, 2);
		scn.AddTokensToCard(axe, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(fereveldir, patrol);
		scn.FreepsResolveSkirmish(fereveldir);

		assertEquals(0, scn.GetWoundsOn(fereveldir));
		assertEquals(0, scn.GetCultureTokensOn(finalcount));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(fereveldir));
	}
}
