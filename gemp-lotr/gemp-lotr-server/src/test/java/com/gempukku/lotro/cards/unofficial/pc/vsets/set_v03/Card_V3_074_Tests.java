package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "103_74");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; a fellowship ability, and a response if Frodo dies
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, home site 2, strength 2, vitality 2; a skirmish ability never used here
					put("runner", "1_178");     // Goblin Runner: strength 5; its text is about being played, and it is never played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Úlairë Enquëa, Magnified through Suffering
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 12
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Fierce. Relentless. <i>(This minion participates in 1 extra round of skirmishes after fierce.)</i>
		 * 	At the start of each skirmish involving Ulaire Enquea and a wounded companion or ally, you may exert him
		 * 	to heal that character and exert a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Magnified through Suffering", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.RELENTLESS));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfHisSkirmishWithAWoundedCompanionHeMayExertToHealThatCompanionAndExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(aragorn, 1);
		// A second wounded companion, who will not be in the skirmish.
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));

		scn.ShadowChooseOptionalTrigger(enquea);

		assertEquals(1, scn.GetWoundsOn(enquea));
		// The companion he is skirmishing is healed; the other wounded companion is not.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		// Any companion may be the one exerted, the one just healed and the Ring-bearer included; the ally may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bounder, enquea));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AWoundedAllyInHisSkirmishIsHealedAndIsNotACompanionToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		// The first move is to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(bounder, enquea);
		scn.FreepsResolveSkirmish(bounder);

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));

		scn.ShadowChooseOptionalTrigger(enquea);

		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(bounder));
		// Healed, the Bounder could exert (vitality 2), but he is no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedAgainstAnUnwoundedCompanionNorWhenAnotherMinionSkirmishesAWoundedOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var enquea = scn.GetShadowCard("enquea");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, runner);
		// Enquea is wounded himself, and can still exert (vitality 4): a wounded minion in his skirmish is no
		// wounded companion or ally.
		scn.AddWoundsToChar(enquea, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, enquea}, new PhysicalCardImpl[]{sam, runner});

		// Sam is wounded, but it is the Goblin Runner he skirmishes: Enquea is not in this skirmish.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		// Nothing is put to the Shadow player: the skirmish goes straight to its actions.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();
		// Goblin Runner 5 beats Sam 3
		assertEquals(2, scn.GetWoundsOn(sam));

		// Enquea skirmishes Aragorn, who has no wound, while Sam has two and Enquea himself has one.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(2, scn.GetWoundsOn(sam));
	}

	@Test
	public void DecliningLeavesHimUnexertedTheCompanionWoundedAndNobodyExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(enquea, 3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		// Nothing is put to the Shadow player: the skirmish goes straight to its actions.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void IsOfferedAgainAtTheStartOfTheFierceSkirmishAndOfTheRelentlessOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToAssignments();

		// The first skirmish.
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowChooseOptionalTrigger(enquea);
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		// Enquea 12 beats Aragorn 8: Aragorn is wounded again.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fierce skirmish. This time the companion healed is the one exerted.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowChooseOptionalTrigger(enquea);
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The relentless skirmish.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowChooseOptionalTrigger(enquea);
		assertEquals(3, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Three rounds and no fourth.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		assertEquals(3, scn.GetWoundsOn(enquea));
	}
}
