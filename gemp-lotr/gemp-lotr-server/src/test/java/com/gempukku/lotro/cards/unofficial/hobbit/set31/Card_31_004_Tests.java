package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_004_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. A skirmish at site N
	// is reached by SkipToSite(N - 1) and the fellowship phase's move; the Barricade is laid after the skip, since
	// the turns it plays end in regroup.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barricade", "31_4");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a Fellowship ability not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, strength 7; a Maneuver ability not used
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally; his regroup offer wants a follower
					// Crown of Erebor: its Assignment ability, with no Thorin in play, lets [dwarven] allies be assigned
					put("crown", "32_1");
					put("pot", "33_5");         // Dwarven Cooking Pot: another [dwarven] support-area possession; its texts not reached

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6; its Assignment ability not used
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, strength 7, fierce; its Skirmish ability not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, strength 4: beats Bilbo and leaves him alive
					put("goblin", "32_27");     // Assaulting Goblin: [moria] Orc, strength 7; a response at a battleground
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GreatBarricadeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Great Barricade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>. To play, exert a [dwarven] companion.
		 * 	If the fellowship moves during the regroup phase, discard this possession.
		 * 	At sites 7 and 8, each minion skirmishing a [dwarven] companion is strength -3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("barricade");

		assertEquals("Great Barricade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsADwarvenCompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barricade = scn.GetFreepsCard("barricade");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, nori);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.MoveCardsToHand(barricade);
		scn.FreepsRefreshDecision();
		assertInHand(barricade);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));

		assertTrue(scn.FreepsPlayAvailable(barricade));
		scn.FreepsPlayCard(barricade);
		// Two [dwarven] companions; not Bilbo, a [shire] companion, nor Dain, a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, nori));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(nori);

		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertInZone(Zone.SUPPORT, barricade);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarvenCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barricade = scn.GetFreepsCard("barricade");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.MoveCardsToHand(barricade);
		// Balin, vitality 3, exhausted; Bilbo and Dain could exert, and are no [dwarven] companion.
		scn.AddWoundsToChar(balin, 2);
		scn.FreepsRefreshDecision();
		assertInHand(barricade);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(0, scn.GetWoundsOn(dain));

		assertFalse(scn.FreepsPlayAvailable(barricade));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsDiscardedWhenTheFellowshipMovesInTheRegroupPhaseAndNotInTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barricade = scn.GetFreepsCard("barricade");
		var pot = scn.GetFreepsCard("pot");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(barricade, pot);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, barricade, pot);

		// The fellowship phase's move, to site 2.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, barricade, pot);

		// The regroup phase's move, to site 3.
		scn.SkipToMovementDecision();
		assertInZone(Zone.SUPPORT, barricade, pot);
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInDiscard(barricade);
		assertInZone(Zone.SUPPORT, pot);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7EachMinionSkirmishingADwarvenCompanionIsStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barricade = scn.GetFreepsCard("barricade");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var aggressor = scn.GetShadowCard("aggressor");
		var spider = scn.GetShadowCard("spider");
		var bats = scn.GetShadowCard("bats");
		var goblin = scn.GetShadowCard("goblin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(barricade);
		assertInZone(Zone.SUPPORT, barricade);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor, spider, bats, goblin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, aggressor}, new PhysicalCardImpl[]{bilbo, bats},
				new PhysicalCardImpl[]{nori, goblin});
		// The Spider, left over, put on Balin by the Shadow player: two minions skirmishing him.
		scn.ShadowAssignToMinions(balin, spider);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Bilbo, a [shire] companion: his minion is untouched; those assigned to the Dwarves, waiting, too.
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, bats));
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(7, scn.GetStrength(spider));
		assertEquals(7, scn.GetStrength(goblin));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, aggressor));
		assertTrue(scn.IsCharSkirmishingAgainst(balin, spider));
		// 6 - 3 and 7 - 3; Nori's Goblin still waits.
		assertEquals(3, scn.GetStrength(aggressor));
		assertEquals(4, scn.GetStrength(spider));
		assertEquals(7, scn.GetStrength(goblin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite8AMinionSkirmishingADwarvenCompanionIsStrengthMinus3AndOneSkirmishingADwarvenAllyIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barricade = scn.GetFreepsCard("barricade");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		var crown = scn.GetFreepsCard("crown");
		var aggressor = scn.GetShadowCard("aggressor");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCompanionsToTable(nori);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(barricade, crown);
		assertInZone(Zone.SUPPORT, barricade, crown);
		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor, goblin);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsUseCardAction(crown);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{nori, aggressor}, new PhysicalCardImpl[]{dain, goblin});
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(dain);
		assertTrue(scn.IsCharSkirmishingAgainst(dain, goblin));
		assertEquals(7, scn.GetStrength(goblin));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(nori);
		assertTrue(scn.IsCharSkirmishingAgainst(nori, aggressor));
		// 6 - 3
		assertEquals(3, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite6AMinionSkirmishingADwarvenCompanionKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barricade = scn.GetFreepsCard("barricade");
		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToSupportArea(barricade);
		assertInZone(Zone.SUPPORT, barricade);
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, aggressor));
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite9AMinionSkirmishingADwarvenCompanionKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barricade = scn.GetFreepsCard("barricade");
		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(barricade);
		assertInZone(Zone.SUPPORT, barricade);
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, aggressor));
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
