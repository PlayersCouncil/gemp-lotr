package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_018_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anduril", "103_18");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability never used here
					put("rohanaragorn", "17_93");   // Aragorn, Defender of Rohan: the Aragorn who is [rohan], strength 8; his text needs the Ring-bearer assigned
					put("boromir", "1_97");         // Boromir, Son of Denethor: a [gondor] Man who is not Aragorn
					put("eomer", "4_267");          // Éomer, Third Marshal of Riddermark: a Man who is [rohan]
					put("deadman", "10_27");        // Dead Man of Dunharrow: a [gondor] companion who is a Wraith; enduring
					put("legolas", "1_50");         // Legolas, Greenleaf: an [elven] Elf; Archery: exert him to wound a minion
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: a [gandalf] Wizard, strength 7
					put("sam", "1_311");            // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					put("aldor", "4_262");          // Aldor, Soldier of Edoras: an ally, and a Man; vitality 2

					put("slayer1", "3_93");         // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
					put("slayer2", "3_93");
					put("slayer3", "3_93");
					put("assassin", "2_93");        // Tower Assassin: Maneuver: exert him to wound an ally
					put("uruk1", "1_143");          // Troop of Uruk-hai: strength 9, damage +1; a character skirmishing it gains no strength from weapons
					put("uruk2", "1_143");
					put("marksman1", "1_176");      // Goblin Marksman: archer and nothing else
					put("marksman2", "1_176");
					put("troop", "1_177");          // Goblin Patrol Troop: vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndurilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Andúril, Legend Remade
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: 3
		 * Game Text: Bearer must be Aragorn.  He is <b>damage +1</b>.
		 * 	Response: If an unbound companion (except Aragorn) is about to take a wound,
		 * 	add 2 threats to hinder that companion.
		 * 	Add (2) unless that companion was [gondor] or a Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("anduril");

		assertEquals("Andúril", card.getBlueprint().getTitle());
		assertEquals("Legend Remade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedForThreeOnAnAragornOfEitherCultureWhoGainsThreeStrengthAndDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rohanAragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		// Two Aragorns can only stand together by cheating; they are here to be told apart from a [gondor] Man.
		scn.MoveCompanionsToTable(aragorn, rohanAragorn, boromir);
		scn.MoveCardsToHand(anduril);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(rohanAragorn));
		assertFalse(scn.HasKeyword(rohanAragorn, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(anduril));

		scn.FreepsPlayCard(anduril);

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, rohanAragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, frodo));
		scn.FreepsChooseCard(rohanAragorn);

		assertEquals(3, scn.GetTwilight());
		assertAttachedTo(anduril, rohanAragorn);
		// 8 + 3
		assertEquals(11, scn.GetStrength(rohanAragorn));
		assertEquals(1, scn.GetKeywordCount(rohanAragorn, Keyword.DAMAGE));
		// The Aragorn who does not bear it, and Boromir, are as they were.
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(boromir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HinderingAnElfAboutToBeWoundedAddsTwoThreatsAndTwoTwilightAndTheWoundIsNotPlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var eomer = scn.GetFreepsCard("eomer");
		var slayer = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(aragorn, legolas, eomer);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		int twilight = scn.GetTwilight();
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(slayer));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(legolas);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsHindered(legolas));
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsChooseOptionalTrigger(anduril);

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsHindered(legolas));
		assertEquals(0, scn.GetWoundsOn(legolas));
		// Legolas is neither [gondor] nor a Man.
		assertEquals(twilight + 2, scn.GetTwilight());
		// The other unbound companion and Aragorn are untouched.
		assertFalse(scn.IsHindered(eomer));
		assertFalse(scn.IsHindered(aragorn));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoTwilightIsAddedForAGondorCompanionWhoIsNoManNorForAManWhoIsNotOfGondor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deadman = scn.GetFreepsCard("deadman");
		var eomer = scn.GetFreepsCard("eomer");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		scn.MoveCompanionsToTable(aragorn, deadman, eomer);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer1, slayer2);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());

		// A [gondor] Wraith.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(deadman);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));
		scn.FreepsChooseOptionalTrigger(anduril);

		assertTrue(scn.IsHindered(deadman));
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(2, scn.GetThreats());
		assertEquals(twilight, scn.GetTwilight());

		// A [rohan] Man. Four companions, so there is room for two threats more.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(eomer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));
		scn.FreepsChooseOptionalTrigger(anduril);

		assertTrue(scn.IsHindered(eomer));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(4, scn.GetThreats());
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForAnAllyForAragornOrForARingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var eomer = scn.GetFreepsCard("eomer");
		var aldor = scn.GetFreepsCard("aldor");
		var assassin = scn.GetShadowCard("assassin");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var slayer3 = scn.GetShadowCard("slayer3");
		scn.MoveCompanionsToTable(aragorn, sam, eomer);
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);

		// An ally, who is a Man besides: only companions are unbound.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(assassin));
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer1, slayer2, slayer3);

		// Aragorn, who bears it.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Sam, who is Ring-bound and does not bear the Ring.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// On the same board an unbound companion who is not Aragorn brings the offer.
		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer3);
		scn.ShadowChooseCard(eomer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));
		scn.FreepsChooseOptionalTrigger(anduril);

		assertTrue(scn.IsHindered(eomer));
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(aldor));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheResponseLeavesTheCompanionToTakeTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var slayer = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		int twilight = scn.GetTwilight();
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(legolas);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(legolas));
		assertEquals(0, scn.GetThreats());
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithRoomForOnlyOneThreatAndOfferedWithRoomForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer1, slayer2);
		// Frodo, Aragorn and Legolas: three companions, so with 2 threats only one more can be added.
		scn.AddThreats(2);
		assertEquals(3, scn.GetThreatLimit());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(legolas);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.IsHindered(legolas));
		assertEquals(2, scn.GetThreats());

		scn.RemoveThreats(1);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(legolas);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));
		scn.FreepsChooseOptionalTrigger(anduril);

		// 1 threat + 2
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.IsHindered(legolas));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionHinderedOnLosingASkirmishTakesNoneOfItsWoundsEvenOnceRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { gandalf, uruk1 },
				new PhysicalCardImpl[] { aragorn, uruk2 }
		);
		scn.FreepsResolveSkirmish(gandalf);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());

		// Gandalf's 7 loses to the Troop's 9; it is damage +1, so two wounds are on their way.
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));
		assertFalse(scn.IsHindered(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsChooseOptionalTrigger(anduril);

		assertTrue(scn.IsHindered(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetThreats());
		// A [gandalf] Wizard: 2 are added.
		assertEquals(twilight + 2, scn.GetTwilight());

		// Aragorn gains no strength from his weapon against this minion: 8 loses to 9, he takes both wounds, and
		// no response is offered for him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetThreats());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.IsHindered(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// The fellowship stays and reconciles, which restores Gandalf: the wounds do not come back for him.
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToStay();

		assertFalse(scn.IsHindered(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AnUnboundCompanionExertingIsNotAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(legolas));

		// Legolas exerts to wound the Troop.
		scn.FreepsUseCardAction(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(legolas));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AnArcheryWoundOnTheRingBearerBringsNoOfferAndOneOnAnUnboundCompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToArcheryWounds();

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetShadowArcheryTotal());

		// The first archery wound goes to Frodo.
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The second goes to Boromir, a [gondor] Man.
		scn.FreepsChooseCard(boromir);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));
		scn.FreepsChooseOptionalTrigger(anduril);

		assertTrue(scn.IsHindered(boromir));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetThreats());
		assertEquals(twilight, scn.GetTwilight());
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
