package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lurtz", "19_33");
					// Defend It and Hope: [gondor] Maneuver event, cost 0: exert a [gondor] Man to wound a minion once
					// (or twice if that minion is an Uruk-hai)
					put("defend", "4_115");
					// Boromir, Son of Denethor: the [gondor] Man who pays for it; a skirmish ability never used
					put("boromir", "1_97");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6; valiant is its whole text
					// Rider's Mount: on a [rohan] Man; at the start of each skirmish involving bearer, each minion
					// skirmishing bearer must exert
					put("mount", "4_287");
					put("party", "1_158");      // Uruk-hai Raiding Party: an [isengard] Uruk-hai, vitality 3; damage +1 is its whole text
					put("cuguard", "10_81");    // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 is its whole text
					put("warrior", "4_165");    // Orthanc Warrior: an [isengard] Uruk-hai, vitality 2; damage +1 is its whole text
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard), vitality 4;
					// each companion is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc; its text answers its own play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Lurtz, Resilient Captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Archer</b>. <b>Damage +1</b>.
		 * 	<b>Response:</b> If Lurtz is about to take a wound or be exerted, remove (2) (or exert another Uruk-hai) to prevent that wound or exertion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lurtz");

		assertEquals("Lurtz", card.getBlueprint().getTitle());
		assertEquals("Resilient Captain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RemovingTwoTwilightPreventsEachWoundWhenNoOtherUrukHaiCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var defend = scn.GetFreepsCard("defend");
		var boromir = scn.GetFreepsCard("boromir");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		// Saruman can exert, but is no Uruk-hai: removing (2) is the one way to pay.
		scn.MoveMinionsToTable(lurtz, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(4);

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(lurtz);

		// Lurtz is an Uruk-hai, so he is about to take a first wound of two.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		scn.ShadowChooseOptionalTrigger(lurtz);
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lurtz));

		// And about to take the second: offered again while (2) can still be removed.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		scn.ShadowChooseOptionalTrigger(lurtz);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void RemovingTwoTwilightPreventsAnExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		// Lurtz is an archer: his archery wound goes on Frodo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(scn.GetRingBearer());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, lurtz);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lurtz));
		// At the start of the skirmish, Rider's Mount makes Lurtz exert.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		scn.ShadowChooseOptionalTrigger(lurtz);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneTwilightAnotherUrukHaiExertsToPreventAnExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var party = scn.GetShadowCard("party");
		var cuguard = scn.GetShadowCard("cuguard");
		var saruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, party, cuguard, saruman, runner);
		// Lurtz is an archer: his archery wound goes on Frodo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(scn.GetRingBearer());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, lurtz);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(cuguard));
		assertEquals(0, scn.GetWoundsOn(saruman));
		// At the start of the skirmish, Rider's Mount makes Lurtz exert.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		scn.ShadowChooseOptionalTrigger(lurtz);

		// (2) cannot be removed from a pool of 1, so another Uruk-hai exerts: an [isengard] or a [sauron] one, not
		// Lurtz himself, Saruman (an [uruk-hai] minion who is no Uruk-hai) or the Goblin Runner.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(party, cuguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lurtz, saruman, runner));
		scn.ShadowChooseCard(cuguard);

		assertEquals(1, scn.GetWoundsOn(cuguard));
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithBothWaysOpenAnExertionIsPreventedByTheWayTheShadowPlayerChooses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, party);
		// Lurtz is an archer: his archery wound goes on Frodo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(scn.GetRingBearer());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, lurtz);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(2);

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(party));
		// At the start of the skirmish, Rider's Mount makes Lurtz exert.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		scn.ShadowChooseOptionalTrigger(lurtz);

		// (2) can be removed and the Raiding Party can exert: he is asked which.
		assertTrue(scn.ShadowChoiceAvailable("Remove (2)"));
		assertTrue(scn.ShadowChoiceAvailable("Exert another Uruk-hai"));
		scn.ShadowChoose("Remove (2)");

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithBothWaysOpenTheShadowPlayerChoosesAndAnotherUrukHaiOfAnyCultureMayExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var defend = scn.GetFreepsCard("defend");
		var boromir = scn.GetFreepsCard("boromir");
		var party = scn.GetShadowCard("party");
		var cuguard = scn.GetShadowCard("cuguard");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, party, cuguard, warrior, saruman, runner);
		// Orthanc Warrior, vitality 2, is exhausted by one wound.
		scn.AddWoundsToChar(warrior, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(cuguard));
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(lurtz);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		scn.ShadowChooseOptionalTrigger(lurtz);
		// (2) can be removed and an Uruk-hai can exert: he is asked which.
		assertTrue(scn.ShadowChoiceAvailable("Remove (2)"));
		assertTrue(scn.ShadowChoiceAvailable("Exert another Uruk-hai"));
		scn.ShadowChoose("Exert another Uruk-hai");

		// An [isengard] and a [sauron] Uruk-hai; not Lurtz himself, the exhausted Orthanc Warrior, Saruman (an
		// [uruk-hai] minion who is no Uruk-hai) or the Goblin Runner.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(party, cuguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lurtz, warrior, saruman, runner));
		scn.ShadowChooseCard(party);

		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(cuguard));
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lurtz));

		// The second wound: both ways are still open, and this time the (2) is removed.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		scn.ShadowChooseOptionalTrigger(lurtz);
		assertTrue(scn.ShadowChoiceAvailable("Remove (2)"));
		assertTrue(scn.ShadowChoiceAvailable("Exert another Uruk-hai"));
		scn.ShadowChoose("Remove (2)");

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(cuguard));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneTwilightAnUrukHaiExertsAndOnceNoneCanTheResponseIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var defend = scn.GetFreepsCard("defend");
		var boromir = scn.GetFreepsCard("boromir");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, warrior, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(lurtz);

		// (2) cannot be removed from a pool of 1: the Orthanc Warrior, the one other Uruk-hai, exerts with no
		// question asked.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		scn.ShadowChooseOptionalTrigger(lurtz);
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(1, scn.GetTwilight());

		// The second wound: the Warrior is exhausted and the pool is still 1, so nothing can pay, and the wound
		// is placed at once.  Saruman could exert, but is no Uruk-hai.  Of the two wounds, one was prevented.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		assertEquals(1, scn.GetWoundsOn(lurtz));
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnotherUrukHaiIsAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var defend = scn.GetFreepsCard("defend");
		var boromir = scn.GetFreepsCard("boromir");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, party);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(4);

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(party));
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(party);

		// Both wounds land on the Raiding Party; Lurtz's response is for Lurtz alone.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		assertEquals(2, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnotherUrukHaiIsAboutToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, party);
		// Lurtz is an archer: his archery wound goes on Frodo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(scn.GetRingBearer());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, party);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(party));
		// At the start of the skirmish, Rider's Mount makes the Raiding Party exert; Lurtz's response is for
		// Lurtz alone.
		scn.FreepsResolveSkirmish(guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lurtz));
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
