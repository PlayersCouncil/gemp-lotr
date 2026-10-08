package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_088_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("road", "53_88");

					put("fear", "1_278");       // Strength Born of Fear: a [sauron] condition; a skirmish ability only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai minion, damage +1 and no other text
					put("lemenya", "1_232");    // Ulaire Lemenya: [wraith] Nazgul; weakens bearers of [wraith] conditions
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgul; its text needs 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc; its text needs 3 burdens
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and no other text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3
					put("bounder", "1_286");    // Bounder: a [shire] ally, vitality 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GetOfftheRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Get Off the Road!
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Search.
		 * Spot a [sauron] card and a Nazgul to make the Free Peoples player exert a companion for each burden you
		 * can spot.  The Free Peoples player may make the move limit -1 to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("road");

		assertEquals("Get Off the Road!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotPlayableWithoutANazgulOrWithoutASauronCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var sentry = scn.GetShadowCard("sentry");
		var creeper = scn.GetShadowCard("creeper");
		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(road);

		scn.StartGame();
		scn.AddBurdens(1);
		// A [sauron] minion, and a [wraith] Orc where a Nazgul should be.
		scn.MoveMinionsToTable(sentry, creeper);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(road));

		// A Nazgul of the [raider] culture.
		scn.MoveMinionsToTable(attea);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(road));

		// The [sauron] card gone, an [isengard] Uruk-hai in its place; the event in hand is not in play.
		scn.MoveCardsToDiscard(sentry);
		scn.MoveMinionsToTable(savage);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(road));
	}

	@Test
	public void AtOneBurdenTheFreePeoplesPlayerExertsACompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var fear = scn.GetShadowCard("fear");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(road);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.AddBurdens(1);
		// Gimli is exhausted.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetMoveLimit());
		// The [sauron] card spotted is a condition, not a minion.
		assertTrue(scn.ShadowPlayAvailable(road));
		scn.ShadowPlayCard(road);

		assertTrue(scn.FreepsDecisionAvailable("move limit"));
		scn.FreepsChooseNo();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, bounder, lemenya));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(twilight, scn.GetTwilight());
		assertInDiscard(road);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtTwoBurdensTheTwoExertionsMayGoToTwoCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var sentry = scn.GetShadowCard("sentry");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(road);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(sentry, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(road);
		scn.FreepsChooseNo();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, sam));
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, sam));
		scn.FreepsChooseCard(sam);

		// Two burdens: two exertions, one on each companion chosen (CR 4.2 "for each").
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtThreeBurdensThreeExertionsAndOneCompanionMayTakeMoreThanOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var sentry = scn.GetShadowCard("sentry");
		var attea = scn.GetShadowCard("attea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(road);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(sentry, attea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(road);
		scn.FreepsChooseNo();

		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(frodo);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayLowerTheMoveLimitForTheTurnToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var sentry = scn.GetShadowCard("sentry");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(road);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(sentry, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetMoveLimit());
		scn.ShadowPlayCard(road);
		assertTrue(scn.FreepsDecisionAvailable("move limit"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetMoveLimit());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(road);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The minions were only there to be spotted: with none left there is no combat.
		scn.MoveCardsToDiscard(sentry, lemenya);
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveLimit());
		scn.PassRegroupActions();

		// One move made and the limit 1: the turn ends without the question of another move.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void WithNoBurdensNothingIsAskedAndNothingIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var sentry = scn.GetShadowCard("sentry");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(road);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(road));
		scn.ShadowPlayCard(road);

		assertFalse(scn.FreepsDecisionAvailable("move limit"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetMoveLimit());
		assertInDiscard(road);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoCompanionAbleToExertThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var sentry = scn.GetShadowCard("sentry");
		var lemenya = scn.GetShadowCard("lemenya");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(road);

		scn.StartGame();
		scn.AddBurdens(1);
		// Frodo, the one companion, is exhausted; the ally beside him could exert but is no companion.
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(sentry, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(road);

		assertFalse(scn.FreepsDecisionAvailable("move limit"));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetMoveLimit());
		assertInDiscard(road);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
