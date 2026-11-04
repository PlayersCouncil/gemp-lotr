package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "6_13");
					put("lorien", "1_53");      // Lórien Elf: an Elf companion, strength 4, vitality 2, no other text
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: an Elf ally; a Fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf, strength 8
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, Damage +1
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Arwen, Evenstar of Her People
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Skirmish:</b> Exert an Elf to make a minion skirmishing Arwen strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Evenstar of Her People", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void SkirmishAbilityExertsAnElfToMakeAMinionSkirmishingArwenStrengthMinusOneForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, lorien, aragorn);
		scn.MoveAlliesToTable(celeborn);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{aragorn, savage2});
		// The minion left over goes to Arwen as well, by the Shadow player's assignment.
		scn.ShadowAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage2));

		assertTrue(scn.FreepsActionAvailable(arwen));
		scn.FreepsUseCardAction(arwen);

		// Any Elf pays: Arwen herself, an Elf companion, an Elf ally; not a companion who is no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, lorien, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(celeborn);
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// Either minion skirmishing Arwen, of two cultures and races; not one assigned to Aragorn and waiting.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2));
		scn.FreepsChooseCard(savage);

		assertEquals(4, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 6 against 4 + 5: Arwen loses and takes 2 wounds (the Savage's damage +1), and every minion lives.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertInPlay(savage);

		// Aragorn's skirmish is a phase of its own: the Savage is back to 5.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoElfAbleToExertTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, lorien, aragorn);
		scn.MoveAlliesToTable(celeborn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Every Elf exhausted; Aragorn, who could exert, is no Elf.
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(lorien, 1);
		scn.AddWoundsToChar(celeborn, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arwen));
	}

	@Test
	public void InAnotherCompanionsSkirmishTheElfExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(celeborn);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{aragorn, savage2});
		// Aragorn's skirmish first, while Arwen waits for hers.
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(savage2));

		assertTrue(scn.FreepsActionAvailable(arwen));
		scn.FreepsUseCardAction(arwen);
		scn.FreepsChooseCard(celeborn);

		// Neither the minion skirmishing Aragorn nor the one waiting for Arwen is made weaker.
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
