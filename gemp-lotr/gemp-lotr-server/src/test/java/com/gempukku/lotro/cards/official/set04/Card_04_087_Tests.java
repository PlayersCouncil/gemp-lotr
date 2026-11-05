package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("valor", "4_87");
					put("valor2", "4_87");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6; +3 only against a Nazgûl
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5; +2 only at sites 6 to 8
					// Silinde, Elf of Mirkwood: an Elf ally (home site 3), in the support area here
					put("silinde", "1_60");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf, strength 8
					// Uruk Savage: strength 5, vitality 3, damage +1 and nothing else; lives through a wound
					put("savage", "1_151");
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Valor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Elf strength +2 (or +3 if skirmishing a wounded minion).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("valor");

		assertEquals("Valor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnElfStrengthPlus2UntilTheEndOfTheSkirmishWhenHerMinionIsUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valor = scn.GetFreepsCard("valor");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(arwen, haldir, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(valor);

		scn.StartGame();

		scn.MoveMinionsToTable(savage, savage2);
		// Arwen is wounded herself, and the wounded minion is in Aragorn's skirmish, not hers.
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(savage2, 1);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertInHand(valor);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not in the assignment phase, the phase before the skirmishes.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(valor);
		assertFalse(scn.FreepsPlayAvailable(valor));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(valor));

		scn.FreepsPlayCard(valor);
		// Each Elf is offered, in this skirmish, waiting for none, and an ally; no other companion is.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, haldir, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(arwen);

		// 6 + 2
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(haldir));
		assertInDiscard(valor);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Arwen (8) beats the Savage (5); in Aragorn's skirmish, which follows, she is back to 6.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage2));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesAnElfSkirmishingAWoundedMinionStrengthPlus3AndOneWaitingForHerSkirmishPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.MoveCardsToHand(valor, valor2);

		scn.StartGame();

		scn.MoveMinionsToTable(savage, savage2);
		// Each Elf has a wounded minion: Arwen's skirmish is fought first, Haldir's after it.
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(savage2, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertInHand(valor, valor2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{haldir, savage2});
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(haldir));
		assertInHand(valor, valor2);

		scn.FreepsPlayCard(valor);
		scn.FreepsChooseCard(arwen);
		// 6 + 3: she is skirmishing a wounded minion.
		assertEquals(9, scn.GetStrength(arwen));

		scn.ShadowPass();
		scn.FreepsPlayCard(valor2);
		scn.FreepsChooseCard(haldir);
		// 5 + 2: Haldir is assigned to a wounded minion, but his skirmish has not begun.
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(9, scn.GetStrength(arwen));
		assertInDiscard(valor, valor2);

		// Arwen (9) beats her Savage; in Haldir's own skirmish both are as printed again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(haldir);
		assertTrue(scn.IsCharSkirmishingAgainst(haldir, savage2));
		assertEquals(5, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
