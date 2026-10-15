package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_275_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seeking", "1_275");

					// Orc Scout: [sauron] Orc tracker, strength 6, vitality 2; a response to events that spot or exert a
					// ranger, which nothing here plays
					put("orcscout", "2_89");
					// Orc Ambusher: [sauron] Orc tracker, strength 5, vitality 2; a response to Free Peoples regroup events,
					// which nothing here plays
					put("ambusher", "1_261");
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc who is no tracker; adds (2) only when a weapon is played on him
					// Uruk Scout: an [isengard] Uruk-hai tracker, Damage +1; a response to events that spot or exert a ranger
					put("urukscout", "2_47");

					// Hobbit Stealth: [shire] Skirmish event, Stealth; "At sites 1 to 5, cancel a skirmish involving a Hobbit"
					put("stealth", "1_298");
					// Depart Silently: [gandalf] Maneuver event, Stealth; "At sites 1 to 5, spot Gandalf to prevent Hobbits
					// from being assigned to skirmishes until the regroup phase"
					put("depart", "3_31");
					// Halfling Deftness: [shire] Skirmish event with no Stealth; "Exert a Hobbit to make him strength +3"
					put("deftness", "1_293");
					// Nice Imitation: [shire] condition, Stealth, plays to the support area; a response to a Hobbit's
					// wound, which nothing here sets off
					put("imitation", "2_106");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit, strength 3, vitality 4; a skirmish ability only while unassigned
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Depart Silently's spot; a strength modifier only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeekingItAlwaysStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Seeking It Always
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. Plays to your support area.
		 * 	<b>Response:</b> If a stealth event is played, exert a [sauron] tracker to cancel that event.
		*/

		var scn = GetScenario();

		var seeking = scn.GetFreepsCard("seeking");

		assertEquals("Seeking It Always", seeking.getBlueprint().getTitle());
		assertNull(seeking.getBlueprint().getSubtitle());
		assertFalse(seeking.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, seeking.getBlueprint().getSide());
		assertEquals(Culture.SAURON, seeking.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, seeking.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(seeking, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(seeking, Keyword.SUPPORT_AREA));
		assertEquals(1, seeking.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronTrackerToCancelAStealthEventAndTheSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetShadowCard("seeking");
		var orcscout = scn.GetShadowCard("orcscout");
		var ambusher = scn.GetShadowCard("ambusher");
		var chieftain = scn.GetShadowCard("chieftain");
		var urukscout = scn.GetShadowCard("urukscout");
		var stealth = scn.GetFreepsCard("stealth");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToSupportArea(seeking);
		scn.MoveCardsToHand(stealth);

		scn.StartGame();
		scn.MoveMinionsToTable(orcscout, ambusher, chieftain, urukscout);
		assertInZone(Zone.SUPPORT, seeking);
		assertInHand(stealth);

		// Site 2.  Merry against the Orc Scout; the Ambusher, the Chieftain and the Uruk Scout against Aragorn.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, orcscout}, new PhysicalCardImpl[]{aragorn, ambusher});
		scn.ShadowAssignToMinions(aragorn, chieftain, urukscout);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(orcscout));
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(urukscout));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsPlayCard(stealth);
		assertTrue(scn.ShadowActionAvailable(seeking));
		scn.ShadowUseCardAction(seeking);

		// [sauron] trackers in this skirmish and in another; neither the [sauron] Orc who is no tracker nor the
		// [isengard] tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(orcscout, ambusher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, urukscout));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(orcscout);

		assertEquals(1, scn.GetWoundsOn(orcscout));
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(urukscout));
		assertInDiscard(stealth);
		assertInZone(Zone.SUPPORT, seeking);
		// Hobbit Stealth is cancelled: Merry's skirmish is still being fought.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, orcscout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		// 6 against 3: the skirmish is fought, and Merry is overwhelmed.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void CancelsAStealthEventOfAnotherCultureAndPhaseWithATrackerInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetShadowCard("seeking");
		var orcscout = scn.GetShadowCard("orcscout");
		var ambusher = scn.GetShadowCard("ambusher");
		var depart = scn.GetFreepsCard("depart");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(merry, gandalf);
		scn.MoveCardsToSupportArea(seeking);
		scn.MoveCardsToHand(depart);

		scn.StartGame();
		scn.MoveMinionsToTable(orcscout, ambusher);
		assertInHand(depart);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(orcscout));
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Site 2: Depart Silently would keep every Hobbit from being assigned until the regroup phase.
		scn.FreepsPlayCard(depart);
		assertTrue(scn.ShadowActionAvailable(seeking));
		scn.ShadowUseCardAction(seeking);
		assertTrue(scn.ShadowHasCardChoiceAvailable(orcscout, ambusher));
		scn.ShadowChooseCard(ambusher);

		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(orcscout));
		assertInDiscard(depart);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The event is cancelled: Merry can be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, orcscout);
		assertTrue(scn.IsCharAssignedAgainst(merry, orcscout));
		// The Ambusher is left over for the Shadow player to assign.
		scn.ShadowDeclineAssignments();
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NotOfferedForAnEventThatIsNoStealthEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetShadowCard("seeking");
		var orcscout = scn.GetShadowCard("orcscout");
		var deftness = scn.GetFreepsCard("deftness");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(seeking);
		scn.MoveCardsToHand(deftness);

		scn.StartGame();
		scn.MoveMinionsToTable(orcscout);
		assertInHand(deftness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, orcscout);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));

		// A [shire] skirmish event with no Stealth: carried out at once, and the Shadow player is asked for
		// skirmish actions with the response not among them.
		scn.FreepsPlayCard(deftness);
		// Frodo is a Hobbit too: Merry is chosen to exert.
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(6, scn.GetStrength(merry));
		assertInDiscard(deftness);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(seeking));
		assertEquals(0, scn.GetWoundsOn(orcscout));
	}

	@Test
	public void NotOfferedForAStealthCardThatIsNoEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetShadowCard("seeking");
		var orcscout = scn.GetShadowCard("orcscout");
		var imitation = scn.GetFreepsCard("imitation");
		scn.MoveCardsToSupportArea(seeking);
		scn.MoveCardsToHand(imitation);

		scn.StartGame();
		scn.MoveMinionsToTable(orcscout);
		scn.FreepsRefreshDecision();
		assertInHand(imitation);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Stealth condition: it goes to the support area and the Free Peoples player is asked for his next
		// fellowship action; the Shadow player, with a [sauron] tracker able to exert, was asked nothing.
		scn.FreepsPlayCard(imitation);
		assertInZone(Zone.SUPPORT, imitation);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetWoundsOn(orcscout));
	}

	@Test
	public void NotOfferedWithTheOnlySauronTrackerExhaustedAndTheStealthEventIsCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetShadowCard("seeking");
		var orcscout = scn.GetShadowCard("orcscout");
		var chieftain = scn.GetShadowCard("chieftain");
		var urukscout = scn.GetShadowCard("urukscout");
		var stealth = scn.GetFreepsCard("stealth");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(seeking);
		scn.MoveCardsToHand(stealth);

		scn.StartGame();
		scn.MoveMinionsToTable(orcscout, chieftain, urukscout);
		// Vitality 2 with 1 wound: the Orc Scout cannot exert.  The Chieftain and the Uruk Scout beside it could.
		scn.AddWoundsToChar(orcscout, 1);
		assertEquals(1, scn.GetWoundsOn(orcscout));
		assertTrue(scn.IsExhausted(orcscout));
		assertFalse(scn.IsExhausted(chieftain));
		assertFalse(scn.IsExhausted(urukscout));
		assertInHand(stealth);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, orcscout);
		scn.ShadowAssignToMinions(merry, chieftain, urukscout);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetWoundsOn(merry));

		// Hobbit Stealth cancels Merry's skirmish: nobody is wounded and the turn goes on to the regroup phase.
		scn.FreepsPlayCard(stealth);
		assertInDiscard(stealth);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(orcscout));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(urukscout));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
