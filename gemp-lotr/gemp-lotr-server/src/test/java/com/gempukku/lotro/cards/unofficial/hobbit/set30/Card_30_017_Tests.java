package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_017_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The fellowship phase
	// of turn 1 is at Bag End (site 1), the skirmishes at The Troll Hoard (site 2), whose only text is a
	// start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orcrist", "30_17");
					put("thorin", "30_19");     // Thorin, Oakenshield: the one Thorin; Damage +1 printed
					put("thorin3", "17_6");     // Thorin III, Stonehelm: a [dwarven] Dwarf companion whose title is not Thorin
					put("balin", "30_2");       // Balin, Brother of Dwalin: a [dwarven] Dwarf companion; a fellowship ability only
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: a [dwarven] companion printed Damage +1

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an assignment ability not used
					put("yazneg", "30_41");     // Yazneg: [gundabad] Orc, vitality 2; a maneuver ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted as laid)
					put("bats", "32_30");       // Dark Bats: a [moria] minion who is no Orc (a creature)
					put("goblin", "31_36");     // The Great Goblin: a fierce [moria] Orc, strength 10, vitality 3
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrcristStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Orcrist
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Thorin.
		 * 	He is <b>damage +1</b>.
		 * 	Each time Thorin wins a skirmish, you may wound an Orc.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orcrist");

		assertEquals("Orcrist", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnThorinAndMakesHimDamagePlusOneMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orcrist = scn.GetFreepsCard("orcrist");
		var thorin = scn.GetFreepsCard("thorin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCompanionsToTable(thorin3, balin, dwalin);

		scn.StartGame();
		scn.MoveCardsToHand(orcrist);
		scn.FreepsRefreshDecision();
		assertInHand(orcrist);

		// No Thorin: neither Thorin III nor Balin may bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(orcrist));

		scn.MoveCompanionsToTable(thorin);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetKeywordCount(thorin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(dwalin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(balin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thorin3, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(orcrist));
		scn.FreepsPlayCard(orcrist);

		// Thorin is the one bearer it may have, so nothing is asked.
		assertAttachedTo(orcrist, thorin);
		// His printed Damage +1 and Orcrist's; Dwalin, damage +1 himself and bearing nothing, is left at 1.
		assertEquals(2, scn.GetKeywordCount(thorin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(dwalin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(balin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thorin3, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThorinWinningMayWoundAnOrcOfEitherCultureAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orcrist = scn.GetFreepsCard("orcrist");
		var thorin = scn.GetFreepsCard("thorin");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, orcrist);

		scn.StartGame();
		assertAttachedTo(orcrist, thorin);
		scn.MoveMinionsToTable(aggressor, yazneg, runner, bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, aggressor);
		// The other three are left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();

		// 7 + 2 for Orcrist against 6: Thorin wins.
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(orcrist));
		scn.FreepsAcceptOptionalTrigger();

		// An Orc of either culture, in no skirmish; the [moria] minion who is no Orc is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(yazneg, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bats));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(yazneg);

		assertEquals(1, scn.GetWoundsOn(yazneg));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThorinLosingASkirmishWoundsNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orcrist = scn.GetFreepsCard("orcrist");
		var thorin = scn.GetFreepsCard("thorin");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, orcrist);

		scn.StartGame();
		assertAttachedTo(orcrist, thorin);
		scn.MoveMinionsToTable(aggressor, yazneg);
		// Strength 11 by hand, so that the Aggressor beats Thorin's 9 and Thorin lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aggressor, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, aggressor);
		scn.ShadowDeclineAssignments();
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(thorin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(orcrist));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinIsOfferedAgainAndAnExhaustedOrcWoundedIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orcrist = scn.GetFreepsCard("orcrist");
		var thorin = scn.GetFreepsCard("thorin");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var runner = scn.GetShadowCard("runner");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, orcrist);

		scn.StartGame();
		assertAttachedTo(orcrist, thorin);
		scn.MoveMinionsToTable(aggressor, yazneg, runner, goblin);
		// Strength 12 by hand, so that Thorin also beats The Great Goblin's 10 in the fierce round.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thorin, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, aggressor);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(orcrist));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(yazneg, runner, goblin));
		// The Goblin Runner has 1 vitality: the wound kills him.
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner);

		// The Great Goblin is fierce: Thorin takes him on in the fierce round and wins again.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(thorin, goblin);
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(orcrist));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: Yazneg, the one Orc left, is not wounded.
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
