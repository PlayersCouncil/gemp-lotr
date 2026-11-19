package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unabated", "10_24");

					// [gollum] minions of two titles and two races (Gollum has none), and the cards that nearly are.
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5; a regroup ability only
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: [gollum] Spider, strength 8, fierce, enduring; damage +1 only in a fierce skirmish
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion, strength 5, vitality 1; its trigger is on being played
					put("smeagol", "6_45");     // Smeagol, Poor Creature: a [gollum] companion; a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8; a maneuver ability only

					// Elendil's Valor: Maneuver event, cost 0, played to take the hand from four to three.
					// Aragorn is the only [gondor] companion, and is chosen unasked.
					put("valor", "1_103");
					// Dwarven Armor: cards to hold in hand and nothing else (no Dwarf to bear one).
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");
					put("filler4", "1_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnabatedinMaliceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Unabated in Malice
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you have initiative, each [gollum] minion is <b>damage +1</b>.
		 * 	<b>Skirmish:</b> Discard this condition to make a [gollum] minion strength +1 for each culture you can spot.
		*/

		var scn = GetScenario();

		var unabated = scn.GetFreepsCard("unabated");

		assertEquals("Unabated in Malice", unabated.getBlueprint().getTitle());
		assertNull(unabated.getBlueprint().getSubtitle());
		assertTrue(unabated.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, unabated.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, unabated.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, unabated.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(unabated, Keyword.SUPPORT_AREA));
		assertEquals(0, unabated.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileTheShadowHasInitiativeEachGollumMinionIsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unabated = scn.GetShadowCard("unabated");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(unabated);
		scn.MoveCardsToHand(valor, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertInZone(Zone.SUPPORT, unabated);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Four cards in hand: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetKeywordCount(gollum, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(shelob, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		// Four cards to three: the Shadow has initiative.
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetKeywordCount(gollum, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(shelob, Keyword.DAMAGE));
		// A minion of another culture, and a [gollum] character who is no minion.
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		// A hand of four again: the bonus is gone.
		scn.MoveCardsToHand(scn.GetFreepsCard("filler4"));
		scn.ShadowPass();
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetKeywordCount(gollum, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(shelob, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsItToMakeAGollumMinionStrongerForEachOfFourCulturesForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unabated = scn.GetShadowCard("unabated");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(unabated);
		assertInZone(Zone.SUPPORT, unabated);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{smeagol, runner});
		// Shelob is left over and stays out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.ShadowActionAvailable(unabated));
		scn.ShadowUseCardAction(unabated);

		// A [gollum] minion in this skirmish and one in none; not a minion of another culture, not a [gollum] companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, smeagol));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gollum);

		assertInDiscard(unabated);
		// 5 + 4 cultures: [shire] (Frodo), [gondor] (Aragorn), [gollum] (Gollum, Shelob, Smeagol) and [moria] (the Runner)
		assertEquals(9, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gollum (9) beats Aragorn (8); in the next skirmish phase the bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoCulturesToSpotAMinionInNoSkirmishIsStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unabated = scn.GetShadowCard("unabated");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(unabated);
		assertInZone(Zone.SUPPORT, unabated);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));
		scn.ShadowUseCardAction(unabated);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, shelob));
		scn.ShadowChooseCard(shelob);

		assertInDiscard(unabated);
		// 8 + 2 cultures: [shire] (Frodo) and [gollum] (Gollum, Shelob)
		assertEquals(10, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoGollumMinionOnTheTableTheAbilityIsStillOfferedAndTheConditionDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unabated = scn.GetShadowCard("unabated");
		var runner = scn.GetShadowCard("runner");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(unabated);
		assertInZone(Zone.SUPPORT, unabated);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		// The Runner is a [moria] minion and Smeagol a [gollum] companion: nothing to choose, and an ability with a cost
		// is offered whatever its effect will do.
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.ShadowActionAvailable(unabated));
		scn.ShadowUseCardAction(unabated);

		assertInDiscard(unabated);
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unabated = scn.GetShadowCard("unabated");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(unabated);
		assertInZone(Zone.SUPPORT, unabated);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(unabated));
		assertInZone(Zone.SUPPORT, unabated);
	}
}
