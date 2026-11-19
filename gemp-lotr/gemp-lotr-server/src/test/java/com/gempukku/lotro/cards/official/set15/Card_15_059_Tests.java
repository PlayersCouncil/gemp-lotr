package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dunedain", "15_59");
					put("dunedain2", "15_59");
					// Ranger of Osgiliath: a [gondor] ranger of another title, strength 6, no hunter; a Regroup ability used only on purpose
					put("osgiliath", "7_116");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion who is no ranger, strength 6; Knight is his whole text
					// Arwen, Daughter of Elrond: a ranger of another culture ([elven]), strength 6; +3 only while skirmishing a Nazgul
					put("arwen", "1_30");

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("pursuer", "4_190");    // Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3
					put("pursuer2", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunedainoftheSouthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Dúnedain of the South
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper>
		 * 	Each time this companion wins a skirmish, you may make a [gondor] ranger strength +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dunedain");

		assertEquals("Dúnedain of the South", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WhenHeWinsAGondorRangerMayBeMadeStrengthPlus2UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunedain = scn.GetFreepsCard("dunedain");
		var dunedain2 = scn.GetFreepsCard("dunedain2");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var knight = scn.GetFreepsCard("knight");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(dunedain, dunedain2, osgiliath, knight, arwen);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dunedain, runner}, new PhysicalCardImpl[]{osgiliath, runner2});
		scn.FreepsResolveSkirmish(dunedain);
		scn.PassSkirmishActions();

		// 6 + 1 (hunter, against the Runner, who is no hunter) against 5: he wins. The other copy did not win, and
		// makes no offer of its own.
		assertInDiscard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dunedain));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dunedain2));
		scn.FreepsAcceptOptionalTrigger();

		// A [gondor] ranger of either title, himself and the other copy among them; not a [gondor] Man who is no
		// ranger, nor a ranger of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(dunedain, dunedain2, osgiliath));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, arwen));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(6, scn.GetStrength(osgiliath));
		scn.FreepsChooseCard(osgiliath);

		assertEquals(8, scn.GetStrength(osgiliath));
		assertEquals(6, scn.GetStrength(dunedain));
		assertEquals(6, scn.GetStrength(dunedain2));

		// Still so in the skirmish that follows.
		scn.FreepsResolveSkirmish(osgiliath);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(osgiliath));

		// 8 against 5: the Ranger wins, and the regroup phase begins.
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(osgiliath));
	}

	@Test
	public void NothingIsOfferedWhenHeLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunedain = scn.GetFreepsCard("dunedain");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(dunedain, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dunedain, troop);
		scn.FreepsResolveSkirmish(dunedain);
		assertEquals(0, scn.GetWoundsOn(dunedain));
		scn.PassSkirmishActions();

		// 7 against 13: he loses, and lives with a wound.
		assertEquals(1, scn.GetWoundsOn(dunedain));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dunedain));
		assertEquals(6, scn.GetStrength(osgiliath));
		assertEquals(6, scn.GetStrength(dunedain));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferWhenHeWinsLeavesEveryRangerAsHeWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunedain = scn.GetFreepsCard("dunedain");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(dunedain, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dunedain, runner);
		scn.FreepsResolveSkirmish(dunedain);
		scn.PassSkirmishActions();

		assertInDiscard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dunedain));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(6, scn.GetStrength(osgiliath));
		assertEquals(6, scn.GetStrength(dunedain));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeHeWinsTheOfferIsMadeAgainAndTheBonusesAddUp() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunedain = scn.GetFreepsCard("dunedain");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		scn.MoveCompanionsToTable(dunedain, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, pursuer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dunedain, pursuer}, new PhysicalCardImpl[]{osgiliath, pursuer2});
		scn.FreepsResolveSkirmish(dunedain);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.PassSkirmishActions();

		// 7 against 5: he wins, and the Pursuer lives with a wound.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dunedain));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(osgiliath);
		assertEquals(8, scn.GetStrength(osgiliath));

		// 8 against 5: the Ranger wins, and the second Pursuer lives too.
		scn.FreepsResolveSkirmish(osgiliath);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer2));

		// Both fierce Pursuers are fought again, his skirmish first, and he wins again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dunedain, pursuer}, new PhysicalCardImpl[]{osgiliath, pursuer2});
		scn.FreepsResolveSkirmish(dunedain);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dunedain));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(osgiliath);

		// The two bonuses add up, and are both still in force in the Ranger's fierce skirmish: 6 + 2 + 2.
		assertEquals(10, scn.GetStrength(osgiliath));
		scn.FreepsResolveSkirmish(osgiliath);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(osgiliath));
	}
}
