package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dawn", "3_103");

					put("galadriel", "1_45");   // Galadriel, Lady of Light: [elven] Elf ally, vitality 3; a start-of-turn heal and a fellowship ability, neither reached
					put("redeemed", "10_11");   // Galadriel, Lady Redeemed: [elven] Elf companion, vitality 3; a fellowship and regroup ability never used
					put("nenya", "3_23");       // Nenya: vitality +1, borne by Galadriel; an optional trigger at the start of the regroup phase, not reached
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion who is no Galadriel; an archery ability never used
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf ally who is no Galadriel; an archery ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, no Elf; a maneuver ability never used
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a Hobbit ally, no Elf; a fellowship ability never used

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability never used
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai; damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion; no text
					put("hatred", "7_310");     // Sauron's Hatred: a [sauron] condition (no minion) in the support area; Shadow and regroup abilities never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TerribleastheDawnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Terrible as the Dawn
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a [sauron] minion to wound Galadriel 3 times. The Free Peoples player may discard 2 Elves to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dawn");

		assertEquals("Terrible as the Dawn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithASauronMinionGaladrielIsWoundedThreeTimesWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var nenya = scn.GetFreepsCard("nenya");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var dawn = scn.GetShadowCard("dawn");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(galadriel, orophin);
		// Galadriel at vitality 4, so three wounds leave her alive and a fourth would not.
		scn.AttachCardsTo(galadriel, nenya);
		assertInZone(Zone.ATTACHED, nenya);
		scn.MoveCardsToHand(dawn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(4, scn.GetVitality(galadriel));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertInZone(Zone.ATTACHED, nenya);
		assertTrue(scn.ShadowPlayAvailable(dawn));

		scn.ShadowPlayCard(dawn);
		// Three Elves are in play, so he is asked.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(3, scn.GetWoundsOn(galadriel));
		assertInZone(Zone.SUPPORT, galadriel);
		// Elves who are no Galadriel, a companion and an ally, are not touched.
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.SUPPORT, orophin);
		assertInZone(Zone.DISCARD, dawn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithGaladrielTheOnlyElfNothingIsAskedAndTheWoundsKillTheCompanionGaladriel() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var redeemed = scn.GetFreepsCard("redeemed");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var dawn = scn.GetShadowCard("dawn");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCompanionsToTable(redeemed, aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(dawn);

		scn.StartGame();
		// A [sauron] Uruk-hai is the only minion.
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(redeemed));
		assertEquals(3, scn.GetVitality(redeemed));
		assertTrue(scn.ShadowPlayAvailable(dawn));

		scn.ShadowPlayCard(dawn);

		// Galadriel is the only Elf: two cannot be discarded, so the Free Peoples player is not asked.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		// Three wounds on vitality 3 kill her; three exertions could not have been made.
		assertInZone(Zone.DEAD, redeemed);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertInZone(Zone.DISCARD, dawn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardTwoElvesToPreventTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var dawn = scn.GetShadowCard("dawn");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(galadriel, orophin, bilbo);
		scn.MoveCardsToHand(dawn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(galadriel));
		scn.ShadowPlayCard(dawn);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// Elves, a companion and allies, Galadriel herself among them, are offered; a Man companion and a Hobbit ally are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(galadriel, legolas, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, bilbo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(legolas, orophin);

		assertInDiscard(legolas, orophin);
		assertInZone(Zone.SUPPORT, galadriel, bilbo);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertInZone(Zone.DISCARD, dawn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASauronMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var dawn = scn.GetShadowCard("dawn");
		var troop = scn.GetShadowCard("troop");
		var hatred = scn.GetShadowCard("hatred");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToHand(dawn);

		scn.StartGame();
		// A minion of another culture and a [sauron] card that is no minion.
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(hatred);
		assertInZone(Zone.SUPPORT, hatred);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dawn));

		scn.MoveMinionsToTable(sentry);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(dawn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoGaladrielInPlayItIsPlayedForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var dawn = scn.GetShadowCard("dawn");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(dawn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Galadriel is no requirement of the play.
		assertTrue(scn.ShadowPlayAvailable(dawn));
		scn.ShadowPlayCard(dawn);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.SUPPORT, orophin);
		assertInZone(Zone.DISCARD, dawn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
