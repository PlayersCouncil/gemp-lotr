package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_32_015_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Every [esgaroth]
	// ally here is at home at site 6 (Esgaroth 30_54); the maneuver tests are fought at sites 2 and 3, away from it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bain", "32_15");
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally, archer; a Regroup ability, not used
					put("percy", "32_19");      // Percy, Bard's Lieutenant: [esgaroth] Man ally; lets [esgaroth] Men bearing a weapon take part (none bears one); a Response, not used
					put("master", "32_20");     // The Master, Master of Lake-Town: [esgaroth] Man ally; a start-of-turn heal that needs Bard and a Fellowship ability
					put("betsy", "33_19");      // Betsy Butterbur: a [gandalf] Man ally; cannot take wounds, wounded only by exerting
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7, damage +1; no follower, his ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability, not used
					put("marksman", "1_176");   // Goblin Marksman: an archer minion; Archer is its whole text
					put("bats1", "32_30");      // Dark Bats: [moria] minion, strength 4, vitality 2; a Shadow ability, not used
					put("bats2", "32_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Baïn, Son of Bard
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Esgaroth
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Man
		 * Home: 6H
		 * Strength: 4
		 * Vitality: 3
		 * Game Text: At the start of each of your turns, you may heal a [esgaroth] Man (except The Master).<br><b>Maneuver:</b> Exert Bain to allow Bard to participate in archery fire and skirmishes until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bain");

		assertEquals("Baïn", card.getBlueprint().getTitle());
		assertEquals("Son of Bard", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ESGAROTH, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 6)));
	}

	@Test
	public void AtTheStartOfTheTurnHealsOneEsgarothManOtherThanTheMaster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bain = scn.GetFreepsCard("bain");
		var bard = scn.GetFreepsCard("bard");
		var percy = scn.GetFreepsCard("percy");
		var master = scn.GetFreepsCard("master");
		var betsy = scn.GetFreepsCard("betsy");
		// Bard is in play, so The Master's own start-of-turn heal (Bard and The Master) is offered too; it is declined.
		scn.MoveAlliesToTable(bain, bard, percy, master, betsy);
		scn.AddWoundsToChar(bard, 2);
		scn.AddWoundsToChar(percy, 2);
		scn.AddWoundsToChar(master, 2);
		scn.AddWoundsToChar(betsy, 1);
		scn.AddWoundsToChar(bain, 1);
		assertEquals(2, scn.GetWoundsOn(bard));
		assertEquals(2, scn.GetWoundsOn(percy));
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(1, scn.GetWoundsOn(betsy));
		assertEquals(1, scn.GetWoundsOn(bain));

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bain));
		scn.FreepsChooseOptionalTrigger(bain);

		// [esgaroth] Men of three titles, Bain himself among them; not The Master, nor a Man of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bard, percy, bain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(master, betsy));
		scn.FreepsChooseCard(bard);

		// One wound off Bard, and nobody else healed.
		assertEquals(1, scn.GetWoundsOn(bard));
		assertEquals(2, scn.GetWoundsOn(percy));
		assertEquals(2, scn.GetWoundsOn(master));
		assertEquals(1, scn.GetWoundsOn(betsy));
		assertEquals(1, scn.GetWoundsOn(bain));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(master));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheHealLeavesEveryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bain = scn.GetFreepsCard("bain");
		var percy = scn.GetFreepsCard("percy");
		scn.MoveAlliesToTable(bain, percy);
		scn.AddWoundsToChar(percy, 1);
		assertEquals(1, scn.GetWoundsOn(percy));

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bain));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(percy));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NoOfferWhenOnlyTheMasterAndAManOfAnotherCultureAreWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bain = scn.GetFreepsCard("bain");
		var percy = scn.GetFreepsCard("percy");
		var master = scn.GetFreepsCard("master");
		var betsy = scn.GetFreepsCard("betsy");
		scn.MoveAlliesToTable(bain, percy, master, betsy);
		scn.AddWoundsToChar(master, 1);
		scn.AddWoundsToChar(betsy, 1);
		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(0, scn.GetWoundsOn(percy));
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(1, scn.GetWoundsOn(betsy));

		scn.StartGame();

		// The heal would have nothing to act on: no offer (The Master's own heal wants Bard, who is not in play).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bain));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(1, scn.GetWoundsOn(betsy));
	}

	@Test
	public void TheHealIsOfferedAgainAtTheStartOfTheOwnersNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bain = scn.GetFreepsCard("bain");
		var percy = scn.GetFreepsCard("percy");
		scn.MoveAlliesToTable(bain, percy);
		scn.AddWoundsToChar(percy, 2);
		assertEquals(2, scn.GetWoundsOn(percy));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bain));
		scn.FreepsChooseOptionalTrigger(bain);
		// Percy is the one candidate, healed unasked.
		assertEquals(1, scn.GetWoundsOn(percy));

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Turn 3, the owner's next: the same offer, the same Man.
		assertEquals(1, scn.GetWoundsOn(percy));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bain));
		scn.FreepsChooseOptionalTrigger(bain);
		assertEquals(0, scn.GetWoundsOn(percy));
		// The Troll Hoard 30_50, the site the turn starts at, makes no offer of its own: no hand weapon in the deck
		// (F-0800).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
	}

	@Test
	public void ManeuverAbilityExertsBainToLetBardTakeArcheryWoundsAndBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bain = scn.GetFreepsCard("bain");
		var bard = scn.GetFreepsCard("bard");
		var percy = scn.GetFreepsCard("percy");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");
		var bats1 = scn.GetShadowCard("bats1");

		scn.StartGame();
		// Laid after the start of the turn, so that the start-of-turn heal is not asked.
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(bain, bard, percy);
		scn.MoveMinionsToTable(marksman, bats1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(bain));
		scn.FreepsUseCardAction(bain);
		assertEquals(1, scn.GetWoundsOn(bain));
		// Bard is an archer: taking part in archery fire, he adds 1 to the fellowship archery total (Percy, also
		// an archer, does not).
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// The Marksman's archery wound may go on Bard; not on Percy, another [esgaroth] ally, nor on Bain.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin, bard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(percy, bain));
		scn.FreepsChooseCard(thorin);
		// Bard's archery wound, placed by the Shadow player: the Marksman (vitality 1) dies of it.
		scn.ShadowChooseCard(marksman);

		// Either player may assign Bard; Percy and Bain neither.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(bard));
		assertTrue(scn.ShadowCanAssign(bard));
		assertFalse(scn.FreepsCanAssign(percy));
		assertFalse(scn.ShadowCanAssign(percy));
		assertFalse(scn.FreepsCanAssign(bain));
		assertFalse(scn.ShadowCanAssign(bain));

		scn.FreepsAssignToMinions(bard, bats1);
		assertTrue(scn.IsCharAssignedAgainst(bard, bats1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithNoBardInPlayBainIsExertedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bain = scn.GetFreepsCard("bain");
		var percy = scn.GetFreepsCard("percy");
		var bats1 = scn.GetShadowCard("bats1");

		scn.StartGame();
		scn.MoveAlliesToTable(bain, percy);
		scn.MoveMinionsToTable(bats1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(bain));
		assertTrue(scn.FreepsActionAvailable(bain));
		scn.FreepsUseCardAction(bain);
		assertEquals(1, scn.GetWoundsOn(bain));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Nobody was let take part.
		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(percy));
		assertFalse(scn.FreepsCanAssign(bain));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void IsNotOfferedWhenBainIsExhaustedAndBardThenTakesNoPart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bain = scn.GetFreepsCard("bain");
		var bard = scn.GetFreepsCard("bard");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(bain, bard);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(bain, 2);
		scn.MoveMinionsToTable(marksman);
		assertEquals(2, scn.GetWoundsOn(bain));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetVitality(bain));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bain));

		// Away from home and not let take part, Bard adds nothing to the archery total, is offered no archery
		// wound and is not to be assigned.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bard));
		scn.FreepsChooseCard(thorin);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(bard));
		assertFalse(scn.ShadowCanAssign(bard));
		assertEquals(2, scn.GetWoundsOn(bain));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void BardTakesPartUntilTheRegroupPhaseAndNotAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bain = scn.GetFreepsCard("bain");
		var bard = scn.GetFreepsCard("bard");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");
		var bats1 = scn.GetShadowCard("bats1");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(bain, bard);
		scn.MoveMinionsToTable(bats1, bats2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(bain));
		scn.FreepsUseCardAction(bain);
		assertEquals(1, scn.GetWoundsOn(bain));

		// Bard's archery wound goes on the first Bats (vitality 2), who lives through it.
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(bats1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{thorin, bats1},
				new PhysicalCardImpl[]{balin, bats2});
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();

		// The second skirmish of the turn, after one already resolved: he may still take part.
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsCanAssign(bard));
		assertTrue(scn.ShadowCanAssign(bard));
		scn.PassSkirmishActions();

		// The regroup phase: no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsCanAssign(bard));
		assertFalse(scn.ShadowCanAssign(bard));

		// A second move, to site 3, and archery there: he is offered no wound.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(marksman);
		scn.ShadowRefreshDecision();
		scn.SkipToArcheryWounds();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bard));
		scn.FreepsChooseCard(thorin);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(bard));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}
}
