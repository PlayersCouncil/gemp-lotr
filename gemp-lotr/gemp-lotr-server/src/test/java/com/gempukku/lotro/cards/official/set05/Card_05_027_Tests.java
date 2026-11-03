package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wretch1", "5_27");
					put("wretch2", "5_27");
					// Sméagol, Poor Creature: [gollum] companion; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability used only on purpose
					put("shiresmeagol", "103_116");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; adds (2) only as he is played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PoorWretchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Poor Wretch
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Strength: +1
		 * Game Text: Plays on Sméagol.
		 * 		<b>Skirmish:</b> Discard this condition to make Sméagol <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wretch1");

		assertEquals("Poor Wretch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsInPlayAndThenGoesOnHimAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var wretch = scn.GetFreepsCard("wretch1");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wretch);

		scn.StartGame();

		// Frodo and Aragorn are companions, but neither is Sméagol.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, wretch);
		assertFalse(scn.FreepsPlayAvailable(wretch));

		scn.MoveCompanionsToTable(smeagol);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(wretch));

		scn.FreepsPlayCard(wretch);
		// Sméagol is the one bearer it may have, so nobody is asked.
		assertAttachedTo(wretch, smeagol);
		assertEquals(0, scn.GetAttachedCards(aragorn).size());
		assertNotAttachedTo(wretch, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnTheShireSmeagolToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wretch = scn.GetFreepsCard("wretch1");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(shiresmeagol, aragorn);
		scn.MoveCardsToHand(wretch);

		scn.StartGame();

		assertInZone(Zone.HAND, wretch);
		assertTrue(scn.FreepsPlayAvailable(wretch));

		scn.FreepsPlayCard(wretch);
		assertAttachedTo(wretch, shiresmeagol);
		assertEquals(0, scn.GetAttachedCards(aragorn).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThisCopyToMakeSmeagolDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wretch1 = scn.GetFreepsCard("wretch1");
		var wretch2 = scn.GetFreepsCard("wretch2");
		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, wretch1, wretch2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(wretch1, smeagol);
		assertAttachedTo(wretch2, smeagol);

		// The phase before the skirmishes: no skirmish ability is offered there.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(wretch1));
		assertFalse(scn.FreepsActionAvailable(wretch2));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(wretch1));
		assertTrue(scn.FreepsActionAvailable(wretch2));

		scn.FreepsUseCardAction(wretch1);
		// The copy used is discarded and the other stays on him: damage +1, not +2.
		assertInDiscard(wretch1);
		assertAttachedTo(wretch2, smeagol);
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InAnotherCompanionsSkirmishTheUnassignedShireSmeagolIsDamagePlusOneForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var wretch = scn.GetFreepsCard("wretch1");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(shiresmeagol, aragorn);
		scn.AttachCardsTo(shiresmeagol, wretch);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		assertAttachedTo(wretch, shiresmeagol);

		scn.SkipToAssignments();
		// Smeagol is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{frodo, runner2});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(shiresmeagol, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(wretch));

		scn.FreepsUseCardAction(wretch);
		assertInDiscard(wretch);
		assertEquals(1, scn.GetKeywordCount(shiresmeagol, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		// Still so at the next decision of that skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(shiresmeagol, Keyword.DAMAGE));

		// Aragorn (8) kills the Runner (5, vitality 1); the next skirmish is a phase of its own, and the bonus is gone.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(shiresmeagol, Keyword.DAMAGE));
	}
}
