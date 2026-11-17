package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swear", "15_52");
					put("swear2", "15_52");

					put("smeagol", "6_45");     // Sméagol, Poor Creature: the [gollum] Sméagol; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion of another title; a maneuver ability used only on purpose

					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a minion who is no Sméagol; a regroup ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; cheated in, so its text never comes up
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges 9_4 as Ring-bearer: no Frodo bears the Ring.  His start-of-skirmish text answers only
	// a skirmish he is in, and none is fought here.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swear", "15_52");

					put("smeagol", "6_45");         // Sméagol, Poor Creature: the [gollum] Sméagol; a skirmish ability used only on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability used only on purpose
					// Frodo, Frenzied Fighter: the one Frodo who can stand beside another Ring-bearer; a skirmish
					// ability used only on purpose, and an overwhelm text no skirmish here reaches
					put("frodo", "13_149");
					// Frodo Gamgee, Son of Samwise: a follower whose title only begins with "Frodo"; his Aid is
					// declined as the maneuver phase starts
					put("gamgee", "13_150");

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; cheated in, so its text never comes up
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, site 3, no text
					put("brigand2", "4_10");    // a second Brigand, played from hand after the second move
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwearbythePreciousStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Swear by the Precious
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot Frodo and Sméagol to discard a minion. That minion's owner may make each minion strength -2 until the end of the turn to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swear");

		assertEquals("Swear by the Precious", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFrodoAndSmeagolSpottedItDiscardsTheMinionChosenWhenTheShadowPlayerDoesNotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swear = scn.GetFreepsCard("swear");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(swear);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, brigand);
		assertInHand(swear);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.FreepsPlayAvailable(swear));
		scn.FreepsPlayCard(swear);

		// The Shadow player is asked first, before the minion is named, and declines.
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// Minions of two cultures and races; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, smeagol, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(brigand);

		assertInDiscard(brigand);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(5, scn.GetStrength(runner));
		assertInDiscard(swear);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithoutSmeagolItCannotBePlayedThoughGollumIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swear = scn.GetFreepsCard("swear");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(aragorn);
		// Sméagol in hand, not in play.
		scn.MoveCardsToHand(swear, smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		assertInHand(swear);
		assertInHand(smeagol);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swear));
	}

	@Test
	public void WithoutFrodoItCannotBePlayedThoughFrodoGamgeeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var swear = scn.GetFreepsCard("swear");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gamgee = scn.GetFreepsCard("gamgee");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(gamgee);
		scn.MoveCardsToHand(swear);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertInHand(swear);
		assertInZone(Zone.SUPPORT, gamgee);
		scn.SkipToPhase(Phase.MANEUVER);
		// Frodo Gamgee's Aid, offered as the maneuver phase starts, is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gamgee));
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swear));
	}

	@Test
	public void TheShadowPlayerPreventsItByMakingEachMinionStrengthMinus2UntilTheEndOfTheTurnAndAMinionPlayedLaterIsNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var swear = scn.GetFreepsCard("swear");
		var gimli = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		// Frodo, Frenzied Fighter is no Ring-bearer, and is spotted all the same; the [shire] Sméagol.
		scn.MoveCompanionsToTable(frodo, smeagol);
		scn.MoveCardsToHand(swear);
		scn.MoveCardsToHand(brigand2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, brigand);
		assertInHand(swear);
		assertInHand(brigand2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(smeagol));
		// Gimli: 6, and 1 for The Ruling Ring.
		assertEquals(7, scn.GetStrength(gimli));
		assertTrue(scn.FreepsPlayAvailable(swear));
		scn.FreepsPlayCard(swear);

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		// Nothing is discarded, and each minion is strength -2; the companions are as they were.
		assertInZone(Zone.SHADOW_CHARACTERS, runner, brigand);
		assertEquals(3, scn.GetStrength(runner));
		assertEquals(6, scn.GetStrength(brigand));
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(7, scn.GetStrength(gimli));
		assertInDiscard(swear);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Still so in the regroup phase (the minions left unassigned) ...
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetStrength(runner));
		assertEquals(6, scn.GetStrength(brigand));

		// ... and in the Shadow phase after a second move, where a minion played now is not weakened (the
		// event's "each minion" is the minions in play as it was played).
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetStrength(runner));
		assertEquals(6, scn.GetStrength(brigand));
		scn.ShadowPlayCard(brigand2);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand2);
		assertEquals(8, scn.GetStrength(brigand2));
		assertEquals(6, scn.GetStrength(brigand));

		// The end of the turn takes the minions themselves away: at the move limit, the regroup phase ends it.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		assertInDiscard(runner, brigand, brigand2);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithNoMinionLeftASecondCopyIsPlayedForNothingAndThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swear = scn.GetFreepsCard("swear");
		var swear2 = scn.GetFreepsCard("swear2");
		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(swear, swear2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertInHand(swear);
		assertInHand(swear2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(swear);
		scn.ShadowChooseNo();
		// The only minion is taken without asking.
		assertInDiscard(runner);
		assertInDiscard(swear);
		scn.ShadowPass();

		// Frodo and Sméagol are still spotted: the second copy is played, with nothing to discard or prevent.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(swear2));
		scn.FreepsPlayCard(swear2);
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertInDiscard(swear2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
