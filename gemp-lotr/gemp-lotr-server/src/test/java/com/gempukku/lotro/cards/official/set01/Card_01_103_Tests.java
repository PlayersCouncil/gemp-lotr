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

public class Card_01_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("valor", "1_103");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; his own maneuver ability is not used here
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man who is an ally; a maneuver ability used only on purpose
					put("eowyn", "4_270");      // Éowyn, Lady of Rohan: a [rohan] Man companion, strength 6; a response to a wound on an unbound companion

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, so its text does nothing
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElendilsValorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Elendil's Valor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Make a [gondor] companion <b>defender +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("valor");

		assertEquals("Elendil's Valor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAChosenGondorCompanionDefenderPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valor = scn.GetFreepsCard("valor");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(aragorn, pippin, eowyn);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(valor);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(valor));

		scn.FreepsPlayCard(valor);
		// The [gondor] companions, a Man and a Hobbit; not the [gondor] ally, the [rohan] Man or the [shire] Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, eowyn, frodo, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(eowyn, Keyword.DEFENDER));
		assertInDiscard(valor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		// Defender +1: Aragorn may take both minions, and Éowyn may not.
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, runner, runner2));
		assertFalse(scn.FreepsCanAssignAgainst(eowyn, runner, runner2));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eowyn, runner},
				new PhysicalCardImpl[]{aragorn, runner2});

		scn.FreepsResolveSkirmish(eowyn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);

		// In the second skirmish, after the first is over, Aragorn is still defender +1.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
	}

	@Test
	public void IsPlayedForNothingWithNoGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valor = scn.GetFreepsCard("valor");
		var frodo = scn.GetRingBearer();
		var merchant = scn.GetFreepsCard("merchant");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(valor);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(valor));
		scn.FreepsPlayCard(valor);

		assertInDiscard(valor);
		assertEquals(0, scn.GetKeywordCount(eowyn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valor = scn.GetFreepsCard("valor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(valor);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(valor));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInHand(valor);
		assertFalse(scn.FreepsPlayAvailable(valor));
	}
}
