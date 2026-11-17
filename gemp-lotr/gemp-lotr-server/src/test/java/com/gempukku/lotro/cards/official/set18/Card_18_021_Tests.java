package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stand", "18_21");
					put("stand2", "18_21");
					// Erland, Dale Counselor: [gandalf] Man companion, strength 5, vitality 3; a skirmish ability used
					// only on purpose
					put("erland", "11_30");
					// Ottar, Man of Laketown: [gandalf] Man ally, vitality 2; a fellowship ability used only on purpose
					put("ottar", "1_80");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion who is no Man
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; a trigger on its own play only
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LastStandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Last Stand
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Exert a [gandalf] Man to add 2 [gandalf] tokens here.<br><b>Skirmish:</b> Remove 3 [gandalf] tokens from here to make a companion strength +3.
		*/

		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");

		assertEquals("Last Stand", stand.getBlueprint().getTitle());
		assertNull(stand.getBlueprint().getSubtitle());
		assertFalse(stand.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, stand.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, stand.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, stand.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(stand, Keyword.SUPPORT_AREA));
		assertEquals(2, stand.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityExertsAGandalfManCompanionOrAllyToAddTwoTokensHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var stand2 = scn.GetFreepsCard("stand2");
		var erland = scn.GetFreepsCard("erland");
		var ottar = scn.GetFreepsCard("ottar");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(erland, gandalf, aragorn);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToSupportArea(stand, stand2);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(stand));
		assertEquals(0, scn.GetCultureTokensOn(stand2));
		assertEquals(0, scn.GetWoundsOn(ottar));
		assertTrue(scn.FreepsActionAvailable(stand));

		scn.FreepsUseCardAction(stand);
		// A [gandalf] Man companion and ally; not a [gandalf] Wizard, nor a Man of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(erland, ottar));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(ottar);
		assertEquals(1, scn.GetWoundsOn(ottar));
		assertEquals(0, scn.GetWoundsOn(erland));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Two, on this copy only.
		assertEquals(2, scn.GetCultureTokensOn(stand));
		assertEquals(0, scn.GetCultureTokensOn(stand2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWhileTheOnlyGandalfManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var erland = scn.GetFreepsCard("erland");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(erland, gandalf, aragorn);
		scn.MoveCardsToSupportArea(stand);
		// Vitality 3: exhausted.  Gandalf and Aragorn could exert, but are no [gandalf] Man.
		scn.AddWoundsToChar(erland, 2);
		assertEquals(2, scn.GetWoundsOn(erland));

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(stand));
	}

	@Test
	public void SkirmishAbilityRemovesThreeTokensToMakeACompanionStrengthPlus3ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var erland = scn.GetFreepsCard("erland");
		var ottar = scn.GetFreepsCard("ottar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(erland, aragorn);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToSupportArea(stand);
		scn.AddTokensToCard(stand, 4);
		assertEquals(4, scn.GetCultureTokensOn(stand));

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{erland, runner2});

		// Aragorn's skirmish; Erland's waits.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.FreepsActionAvailable(stand));
		assertEquals(5, scn.GetStrength(erland));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(stand);
		// Any companion, in this skirmish or not; not an ally, not a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, erland, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ottar, runner, runner2));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(erland);
		// 4 - 3.
		assertEquals(1, scn.GetCultureTokensOn(stand));
		assertEquals(8, scn.GetStrength(erland));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Erland's own skirmish is another phase: the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(erland);
		assertEquals(5, scn.GetStrength(erland));
		// One token left: not enough for another use.
		assertFalse(scn.FreepsActionAvailable(stand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithTwoTokensHereThoughTheOtherCopyHasThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var stand2 = scn.GetFreepsCard("stand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stand, stand2);
		scn.AddTokensToCard(stand, 2);
		scn.AddTokensToCard(stand2, 3);
		assertEquals(2, scn.GetCultureTokensOn(stand));
		assertEquals(3, scn.GetCultureTokensOn(stand2));

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(stand));
		assertTrue(scn.FreepsActionAvailable(stand2));
		assertEquals(2, scn.GetCultureTokensOn(stand));
	}
}
