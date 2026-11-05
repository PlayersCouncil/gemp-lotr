package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_045_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  Turn 1's Shadow phase and skirmishes are at The Troll Hoard 30_50 (site 2), whose only text is a
	// start-of-turn offer.  None of the minions laid for the skirmish tests is an archer, so no archery wound is placed.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin", "33_45");

					// Orc archers, and an archer and an Orc that are not both, for the hand.
					put("bowman", "2_60");       // Goblin Bowman: [moria] Orc archer, cost 2, no other text
					put("narzug", "30_36");      // Narzug: [gundabad] Orc archer, cost 2; triggers on leaving play, not reached
					put("assassin", "1_131");    // Orthanc Assassin: [isengard] Uruk-hai archer, cost 5, no other text
					// Orcs to discard, and a minion that is no Orc.
					put("runner", "31_30");      // Goblin Runner: [moria] Orc, cost 1; a "when you play" offer, not reached
					put("yazneg", "30_41");      // Yazneg: [gundabad] Orc; a maneuver ability, not used
					put("bats", "32_30");        // Dark Bats: [moria] Creature, no Orc; a Shadow ability, not used

					put("thorin", "30_19");      // Thorin, Oakenshield: [dwarven] companion, vitality 4; a Fellowship ability, not used
					put("gloin", "30_11");       // Glóin, Father of Gimli: [dwarven] companion; a Skirmish ability, not used
					put("master", "32_20");      // The Master: [esgaroth] Man ally, vitality 3; a Fellowship ability, not used
					put("negotiator", "31_43");  // Skillful Negotiator: Maneuver event, an ally participates in archery fire and skirmishes
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheGreatGoblinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: The Great Goblin, Goblin-King
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Fierce</b>.<br>Each Orc archer is twilight cost -1.<br><b>Skirmish:</b> Discard another Orc to wound a character The Great Goblin is skirmishing. The Free Peoples player may add a doubt to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goblin");

		assertEquals("The Great Goblin", card.getBlueprint().getTitle());
		assertEquals("Goblin-King", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachOrcArcherCostsOneLessAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var bowman = scn.GetShadowCard("bowman");
		var narzug = scn.GetShadowCard("narzug");
		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(bowman, narzug, assassin, runner);

		scn.StartGame();
		assertInHand(bowman, narzug, assassin, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Without The Great Goblin: the printed costs.
		assertEquals(2, scn.GetTwilightCost(bowman));
		assertEquals(2, scn.GetTwilightCost(narzug));
		assertEquals(5, scn.GetTwilightCost(assassin));
		assertEquals(1, scn.GetTwilightCost(runner));

		scn.MoveMinionsToTable(goblin);
		scn.ShadowRefreshDecision();
		// Orc archers of two cultures are -1; an archer who is no Orc and an Orc who is no archer are not.
		assertEquals(1, scn.GetTwilightCost(bowman));
		assertEquals(1, scn.GetTwilightCost(narzug));
		assertEquals(5, scn.GetTwilightCost(assassin));
		assertEquals(1, scn.GetTwilightCost(runner));

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(bowman);
		// 2 printed - 1 + 2 roaming at site 2.
		assertEquals(pool - 3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, bowman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionDiscardsAnotherOrcToWoundTheCompanionHeIsSkirmishingUnlessADoubtIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var runner = scn.GetShadowCard("runner");
		var yazneg = scn.GetShadowCard("yazneg");
		var bats = scn.GetShadowCard("bats");
		var thorin = scn.GetFreepsCard("thorin");
		var gloin = scn.GetFreepsCard("gloin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, gloin);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, runner, yazneg, bats);
		scn.FreepsRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, goblin);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.ShadowActionAvailable(goblin));
		scn.ShadowUseCardAction(goblin);
		// Another Orc of either culture, in no skirmish; not a minion who is no Orc, nor The Great Goblin himself.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, yazneg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, goblin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(yazneg);
		assertInZone(Zone.DISCARD, yazneg);

		// The Free Peoples player adds a doubt: Thorin is not wounded.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(thorin));

		// Again, with the Runner the only other Orc: declined, and the character he is skirmishing is wounded.
		scn.FreepsPass();
		scn.ShadowUseCardAction(goblin);
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertInZone(Zone.SHADOW_CHARACTERS, bats);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionWoundsAnAllyHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var runner = scn.GetShadowCard("runner");
		var thorin = scn.GetFreepsCard("thorin");
		var master = scn.GetFreepsCard("master");
		var negotiator = scn.GetFreepsCard("negotiator");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(negotiator);

		scn.StartGame();
		assertInHand(negotiator);
		scn.MoveAlliesToTable(master);
		scn.MoveMinionsToTable(goblin, runner);
		scn.FreepsRefreshDecision();

		// The Master is made to take part in skirmishes, and is assigned to The Great Goblin.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(negotiator);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(master, goblin);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(master);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		scn.ShadowUseCardAction(goblin);
		assertInZone(Zone.DISCARD, runner);
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithNoOtherOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var bats = scn.GetShadowCard("bats");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, bats);
		scn.FreepsRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, goblin);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		// The one other minion is no Orc.
		assertInZone(Zone.SHADOW_CHARACTERS, bats);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(goblin));
	}

	@Test
	public void OutsideHisOwnSkirmishTheOrcIsDiscardedAndNobodyIsWoundedOrAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");
		var runner = scn.GetShadowCard("runner");
		var yazneg = scn.GetShadowCard("yazneg");
		var thorin = scn.GetFreepsCard("thorin");
		var gloin = scn.GetFreepsCard("gloin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, gloin);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, runner, yazneg);
		scn.FreepsRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, goblin},
				new PhysicalCardImpl[]{gloin, runner});
		scn.ShadowDeclineAssignments();

		// The Great Goblin's skirmish first: Thorin loses it and takes a wound.
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(thorin));

		// In the Runner's skirmish, The Great Goblin is skirmishing nobody.
		scn.FreepsResolveSkirmish(gloin);
		scn.FreepsPass();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.ShadowActionAvailable(goblin));
		scn.ShadowUseCardAction(goblin);
		scn.ShadowChooseCard(yazneg);

		assertInZone(Zone.DISCARD, yazneg);
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
