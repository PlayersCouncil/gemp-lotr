package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knowledge", "13_54");

					put("smeagol", "6_45");         // Sméagol, Poor Creature: [gollum] companion, strength 3, vitality 4; a skirmish ability, never used
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, strength 3; an assignment ability only
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only

					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5, vitality 4;
													// his texts answer a Nazgûl played, or want threats or a companion of cost 0 who is not the Ring-bearer
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: the [gollum] minion who is no Gollum, strength 8, vitality 8
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1; its "when played" text is not reached by a cheat

					// Cards that hold culture tokens; each gets them only by its own text.
					put("fish1", "15_46");          // Nice Fish: [gollum] support-area condition, a token each time Sméagol wins a skirmish
					put("fish2", "15_46");
					put("path", "8_42");            // A Path Appointed: [gondor] support-area condition, [gondor] tokens by its fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OutofAllKnowledgeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Out of All Knowledge
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot Sméagol or exert Gollum to make Sméagol strength +2 and you may reinforce a [gollum] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("knowledge");

		assertEquals("Out of All Knowledge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingTheShadowPlayersGollumMakesSmeagolStrengthPlus2UntilTheEndOfHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fish = scn.GetFreepsCard("fish1");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(knowledge);
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(fish);
		scn.AddTokensToCard(fish, 1);

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(fish));
		scn.MoveMinionsToTable(gollum, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, gollum}, new PhysicalCardImpl[]{aragorn, runner});
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.FreepsPlayAvailable(knowledge));

		scn.FreepsPlayCard(knowledge);
		// Sméagol can be spotted and the Shadow player's Gollum can exert, so the Free Peoples player is asked which.
		assertEquals(List.of("Spot Sméagol", "Exert Gollum"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert Gollum");

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(smeagol));

		// The reinforce is declined: the token stays as it was, and the +2 stands.
		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetCultureTokensOn(fish));
		assertInZone(Zone.DISCARD, knowledge);
		assertEquals(5, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gollum 5 against Sméagol 5: Sméagol loses, and the next skirmish is a phase of its own.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SpottingSmeagolExertsNobodyAndTheReinforceGoesOnAChosenGollumToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var smeagol = scn.GetFreepsCard("smeagol");
		var fish1 = scn.GetFreepsCard("fish1");
		var fish2 = scn.GetFreepsCard("fish2");
		var path = scn.GetFreepsCard("path");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(knowledge);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(fish1, fish2, path);
		scn.AddTokensToCard(fish1, 1);
		scn.AddTokensToCard(fish2, 1);
		scn.AddTokensToCard(path, 1);

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(fish1));
		assertEquals(1, scn.GetCultureTokensOn(fish2));
		assertEquals(1, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, gollum);
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(3, scn.GetStrength(smeagol));
		scn.FreepsPlayCard(knowledge);
		assertEquals(List.of("Spot Sméagol", "Exert Gollum"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Spot Sméagol");

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(smeagol));

		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseYes();
		// Each Nice Fish holds a [gollum] token; A Path Appointed holds a [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fish1, fish2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(path));
		scn.FreepsChooseCard(fish2);

		assertEquals(1, scn.GetCultureTokensOn(fish1));
		assertEquals(2, scn.GetCultureTokensOn(fish2));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheShireSmeagolOutsideAnySkirmishIsMadeStrongerWithNoGollumToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fish = scn.GetFreepsCard("fish1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(knowledge);
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(fish);
		scn.AddTokensToCard(fish, 1);

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(fish));
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(knowledge);

		// No Gollum is in play, so the spot is the only cost and nothing is asked; Smeagol, in no skirmish, is the one Sméagol.
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetCultureTokensOn(fish));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoSmeagolTheWraithGollumExertsAndNobodyIsMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fish = scn.GetFreepsCard("fish1");
		var gollum = scn.GetShadowCard("wraithgollum");
		scn.MoveCardsToHand(knowledge);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(fish);
		scn.AddTokensToCard(fish, 1);

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(fish));
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.FreepsPlayAvailable(knowledge));
		scn.FreepsPlayCard(knowledge);

		// Sméagol cannot be spotted, so the exertion is the cost, paid with no question; there is no Sméagol to strengthen.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseNo();
		assertInZone(Zone.DISCARD, knowledge);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWithNoSmeagolAndGollumExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(knowledge);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInHand(knowledge);
		scn.MoveMinionsToTable(gollum, shelob);
		// Vitality 4 with 3 wounds: Gollum cannot exert.  Shelob, fresh, is no Gollum.
		scn.AddWoundsToChar(gollum, 3);
		assertEquals(3, scn.GetWoundsOn(gollum));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInHand(knowledge);
		assertFalse(scn.FreepsPlayAvailable(knowledge));
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
	}

	@Test
	public void WithNoGollumTokenInPlayTheReinforceIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var smeagol = scn.GetFreepsCard("smeagol");
		var path = scn.GetFreepsCard("path");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(knowledge);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(path);
		scn.AddTokensToCard(path, 1);

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, gollum);
		scn.FreepsResolveSkirmish(smeagol);

		scn.FreepsPlayCard(knowledge);
		scn.FreepsChoose("Spot Sméagol");

		// Only a [gondor] token is in play: there is nothing to reinforce, and nothing is asked.
		assertFalse(scn.FreepsDecisionAvailable("reinforce"));
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
