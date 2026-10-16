package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "4_49");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, unbound, strength 8; a maneuver ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion who is no Legolas, strength 6; +3 only against a Nazgul
					put("legolas", "11_21");    // Legolas, Companion of the Ring: strength 6, no archer; +2 only at a battleground or forest, which no site of this path is
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally; a maneuver ability only
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and no other text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its play trigger does nothing when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Gimli, Unbidden Guest
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Damage +1</b>.
		 * <b>Skirmish:</b> Exert Gimli to make an unbound companion strength +1 (or +2 if that companion is Legolas).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Unbidden Guest", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void SkirmishAbilityExertsGimliToMakeAnUnboundCompanionStrengthPlus1UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli, aragorn, arwen, sam);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{arwen, runner});

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(gimli));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		// Unbound companions, in this skirmish, assigned to another and in none; never the Ring-bound, an ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, thrarin, savage, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(gimli));

		// Still in force at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(aragorn));

		// Aragorn's 9 beats the Savage's 5; the next skirmish is a phase of its own, and the +1 is gone.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void LegolasIsMadeStrengthPlus2AndAnotherElfPlus1UntilGimliIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, legolas, arwen);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(arwen));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(legolas);
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(arwen));

		scn.ShadowPass();
		// An Elf companion who is no Legolas gets +1 only.
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(arwen);
		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(8, scn.GetStrength(legolas));

		// Vitality 3 with 2 wounds: exhausted, so he can no longer exert.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gimli));
	}
}
