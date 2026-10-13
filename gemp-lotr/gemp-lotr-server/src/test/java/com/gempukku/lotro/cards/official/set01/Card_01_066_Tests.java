package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tale", "1_66");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgûl
					put("elf", "1_53");         // Lórien Elf: Elf companion, strength 4, vitality 2; "to play, spot an Elf" (cheated in)
					// Silinde, Elf of Mirkwood: an Elf ally; her text takes on your site 3, which is never reached here
					put("silinde", "1_60");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability never used
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not set off by a cheat
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheTaleofGilgaladStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Tale of Gil-galad
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Vitality: +1
		 * Game Text: Tale. Bearer must be an Elf.
		 * Skirmish: Discard this condition to make bearer strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tale");

		assertEquals("The Tale of Gil-galad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAnElfCompanionOrAnElfAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(tale);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(tale));
		scn.FreepsPlayCard(tale);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(silinde);
		assertAttachedTo(tale, silinde);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsItToMakeBearerStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var elf = scn.GetFreepsCard("elf");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(elf, arwen);
		scn.AttachCardsTo(elf, tale);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, guard);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not a skirmish phase: not offered.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tale));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, runner},
				new PhysicalCardImpl[]{arwen, guard});
		scn.FreepsResolveSkirmish(elf);

		assertEquals(4, scn.GetStrength(elf));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.FreepsActionAvailable(tale));

		assertAttachedTo(tale, elf);
		scn.FreepsUseCardAction(tale);
		assertInZone(Zone.DISCARD, tale);
		assertEquals(6, scn.GetStrength(elf));
		// Arwen, another Elf companion, is not the bearer.
		assertEquals(6, scn.GetStrength(arwen));

		// Still in force as the skirmish is resolved: 6 beats the Runner's 5, which 4 would not.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(elf));
		assertInZone(Zone.DISCARD, runner);

		// Gone in the next skirmish, a phase of its own.
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(4, scn.GetStrength(elf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesTheBearerStrongerAndNotTheOtherElfWhoIsTheOneSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var elf = scn.GetFreepsCard("elf");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(elf, arwen);
		scn.AttachCardsTo(elf, tale);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, guard);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, runner},
				new PhysicalCardImpl[]{arwen, guard});

		// Arwen's skirmish comes first: the bearer waits for his own, and the other Elf is the one in this one.
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(4, scn.GetStrength(elf));
		assertEquals(6, scn.GetStrength(arwen));
		assertAttachedTo(tale, elf);
		assertTrue(scn.FreepsActionAvailable(tale));

		scn.FreepsUseCardAction(tale);
		assertInZone(Zone.DISCARD, tale);
		assertEquals(6, scn.GetStrength(elf));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
