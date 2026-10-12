package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lurtz", "18_118");
					// Uruk Decimator: [uruk-hai] Uruk-hai, strength 12, with no keyword; stronger at a battleground
					// and damage +1 only while it bears a possession
					put("decimator", "12_150");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit, unbound; a skirmish ability used only on purpose
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit, unbound; his text is about [shire] tales
					// Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer; a fellowship ability and a response
					// to Frodo's death
					put("sam", "1_311");
					// Bounder: [shire] Hobbit ally whose home is site 2 of this path, where he may be assigned to
					// a skirmish; a skirmish ability used only on purpose
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Lurtz, Halfling Hunter
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Archer</b>. <b>Damage +1</b>.<br><b>Maneuver:</b> Spot 6 companions to make Lurtz <b>fierce</b> until the regroup phase.<br><b>Assignment:</b> Exert Lurtz twice to assign it to an unbound companion. The Free Peoples player may exert that companion or add a burden to prevent this.
		 */

		var scn = GetScenario();

		var lurtz = scn.GetFreepsCard("lurtz");

		assertEquals("Lurtz", lurtz.getBlueprint().getTitle());
		assertEquals("Halfling Hunter", lurtz.getBlueprint().getSubtitle());
		assertTrue(lurtz.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, lurtz.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, lurtz.getBlueprint().getCulture());
		assertEquals(CardType.MINION, lurtz.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, lurtz.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(lurtz, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(lurtz, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		assertEquals(7, lurtz.getBlueprint().getTwilightCost());
		assertEquals(13, lurtz.getBlueprint().getStrength());
		assertEquals(3, lurtz.getBlueprint().getVitality());
		assertEquals(5, lurtz.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForSevenAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCardsToHand(lurtz);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, lurtz);
		assertTrue(scn.ShadowPlayAvailable(lurtz));

		scn.ShadowPlayCard(lurtz);

		// 7 to play + 2 roaming: its site number is 5 and the fellowship is at site 2
		assertEquals(twilight - 9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, lurtz);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverAbilityMakesLurtzFierceUntilTheRegroupPhaseWhileSixCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		// With Frodo: the Ring-bearer, a Ring-bound Hobbit, two unbound Hobbits, a Man and a Dwarf.
		scn.MoveCompanionsToTable(aragorn, sam, scn.GetFreepsCard("gimli"), scn.GetFreepsCard("merry"), scn.GetFreepsCard("pippin"));

		var lurtz = scn.GetShadowCard("lurtz");
		var decimator = scn.GetShadowCard("decimator");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, decimator);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(lurtz));

		scn.ShadowUseCardAction(lurtz);
		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));
		// The other [uruk-hai] minion is not.
		assertFalse(scn.HasKeyword(decimator, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(lurtz));

		// Fierce already, he may be made fierce again: the offer stands.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(lurtz));
		scn.ShadowPass();

		// Lurtz is an archer: his one wound goes to Sam.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(sam);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 13 against 8, damage +1: Aragorn lives with two wounds.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// With that skirmish over there are fierce assignments, the last before the regroup phase: Lurtz is
		// still fierce and is the one minion to be assigned.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lurtz));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(decimator));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Five companions with Frodo, and an ally at his home site.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("merry"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("bounder"));

		var lurtz = scn.GetShadowCard("lurtz");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lurtz));

		// A sixth companion, and it is.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("pippin"));
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(lurtz));
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
	}

	@Test
	public void AssignmentAbilityExertsLurtzTwiceToAssignItToAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, merry, sam);
		scn.MoveAlliesToTable(bounder);

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Lurtz goes down once the archery phase is over, so that his archery wound is not in the way.
		scn.MoveMinionsToTable(lurtz);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertTrue(scn.ShadowActionAvailable(lurtz));

		scn.ShadowUseCardAction(lurtz);
		assertEquals(2, scn.GetWoundsOn(lurtz));
		// A Man, a Dwarf and a Hobbit, all unbound. Not the Ring-bearer, not Sam, and not the ally, who may
		// be assigned to a skirmish at this site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder));
		assertTrue(scn.FreepsCanAssign(bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(aragorn, lurtz));
		assertFalse(scn.IsCharAssigned(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void LurtzAbilityOffersChoiceOfPrevention() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lurtz));
		scn.ShadowUseCardAction(lurtz);
		assertEquals(2, scn.GetWoundsOn(lurtz));
		scn.ShadowChooseCard(aragorn);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Add 1 burden"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());

		// The companion he was to be assigned to exerts, and no other.
		scn.FreepsChooseOption("Exert");
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(lurtz));
		assertFalse(scn.IsCharAssigned(aragorn));
		// What Lurtz paid stays paid.
		assertEquals(2, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AddingABurdenPreventsTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.FreepsPass();

		scn.ShadowUseCardAction(lurtz);
		// The one unbound companion is chosen without asking.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetBurdens());

		scn.FreepsChooseOption("Add 1 burden");
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(lurtz));
		assertEquals(2, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithThatCompanionExhaustedABurdenIsWhatPrevents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 4: three wounds leave Aragorn exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.FreepsPass();

		scn.ShadowUseCardAction(lurtz);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetBurdens());

		// He cannot exert, so yes is the burden, with no choice between the two.
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(lurtz));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoBurdenToBeAddedTheExertionIsWhatPrevents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		// No burden is added to a hindered Ring-bearer.
		scn.HinderCard(scn.GetRingBearer());
		scn.FreepsPass();

		scn.ShadowUseCardAction(lurtz);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Yes is the exertion, with no choice between the two.
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(lurtz));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNeitherPayableTheFreePeoplesPlayerIsNotAskedAndLurtzIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.HinderCard(scn.GetRingBearer());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(lurtz));
		scn.ShadowUseCardAction(lurtz);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, lurtz));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedOnceLurtzCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		// Vitality 3: with one wound he could exert once, and not twice.
		scn.AddWoundsToChar(lurtz, 1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lurtz));
		assertEquals(1, scn.GetWoundsOn(lurtz));
	}

	@Test
	public void WithNoUnboundCompanionLurtzExertsForNothingAndNobodyIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		// The fellowship is the Ring-bearer and Sam, with an ally at his home site.
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(scn.GetFreepsCard("bounder"));

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.FreepsPass();

		// The ability has a cost, so it is offered though nobody can be chosen.
		assertTrue(scn.ShadowActionAvailable(lurtz));
		scn.ShadowUseCardAction(lurtz);

		assertEquals(2, scn.GetWoundsOn(lurtz));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertFalse(scn.IsCharAssigned(lurtz));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
