package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fire", "4_83");
					// Arwen, Daughter of Elrond: Elf companion, strength 6 (+3 only against a Nazgûl)
					put("arwen", "1_30");
					// Lórien Elf: Elf companion, strength 4, vitality 2; to play, spot an Elf (cheated in)
					put("lorien", "1_53");
					// Haldir, Elf of the Golden Wood: Elf companion, strength 5 (+2 only at sites 6 to 8)
					put("haldir", "1_48");
					// Silinde, Elf of Mirkwood: Elf ally, strength 5; has the text of your site 3 only when it is laid
					put("silinde", "1_60");
					// Aragorn, Ranger of the North: a companion who is no Elf, strength 8; his ability used only on purpose
					put("aragorn", "1_89");
					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its trigger answers its own play only
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SupportingFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Supporting Fire
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Elf strength +2 (or +3 if you spot 3 Elf companions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fire");

		assertEquals("Supporting Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoElfCompanionsAnElfIsStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fire = scn.GetFreepsCard("fire");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(arwen, lorien, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(fire);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// A Skirmish event: not in the assignment phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fire));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, runner1},
				new PhysicalCardImpl[]{lorien, runner2}
		);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(fire));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(lorien));
		assertEquals(5, scn.GetStrength(silinde));

		scn.FreepsPlayCard(fire);
		// Any Elf: the one skirmishing, one waiting for his own skirmish, and an ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, lorien, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(lorien);

		assertInZone(Zone.DISCARD, fire);
		// Two Elf companions only: Silinde is an Elf but no companion, Aragorn a companion but no Elf.
		assertEquals(6, scn.GetStrength(lorien));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(silinde));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		// Still in force at the next decision of this skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(lorien));

		// Arwen, 6 against 5, kills the Runner.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(lorien);

		// Gone in the next skirmish, which is another phase.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(lorien));
	}

	@Test
	public void WithThreeElfCompanionsTheElfChosenIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetFreepsCard("fire");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var haldir = scn.GetFreepsCard("haldir");
		var silinde = scn.GetFreepsCard("silinde");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(arwen, lorien, haldir);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(fire);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, runner1);

		assertEquals(5, scn.GetStrength(silinde));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(haldir));
		assertEquals(4, scn.GetStrength(lorien));
		assertTrue(scn.FreepsPlayAvailable(fire));

		scn.FreepsPlayCard(fire);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, lorien, haldir, silinde));
		scn.FreepsChooseCard(silinde);

		// Three Elf companions of three titles are spotted, so the +3 is taken with no further question, and the
		// Elf made stronger need not be one of them.
		assertEquals(8, scn.GetStrength(silinde));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(haldir));
		assertEquals(4, scn.GetStrength(lorien));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
