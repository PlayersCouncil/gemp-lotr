package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erestor", "3_14");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgul
					put("lorien", "1_53");      // Lorien Elf: Elf companion, strength 4, vitality 2; no text but "To play, spot an Elf"
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, vitality 3; an archery ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, vitality 2; a fellowship ability only
					put("shoulder", "1_59");    // Shoulder to Shoulder: [elven] support-area condition, a card of the culture that is no Elf; a maneuver ability only

					put("hate1", "1_250");      // Hate: [sauron] Maneuver event, exert a [sauron] Orc to wound a companion (except the Ring-bearer)
					put("hate2", "1_250");
					put("orc1", "1_271");       // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("orc2", "1_271");
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, vitality 3; Maneuver: exert to wound an ally
					put("hatred", "1_164");     // Bitter Hatred: [moria] Archery event, spot a [moria] archer to wound an Elf
					put("marksman", "1_176");   // Goblin Marksman: [moria] archer with no other text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text answers only its own play
					// Window of the Eye: a [sauron] condition, no minion and no event.  "Each time the fellowship moves
					// during the regroup phase, you may remove a threat to wound an unbound companion."
					put("window", "10_103");
					// Orc Archer Troop: a [sauron] archer, strength 15; with the Shadow player's initiative the minion
					// archery total is +1 and he is fierce.
					put("archers", "7_294");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErestorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Erestor, Chief Advisor to Elrond
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: To play, spot an Elf.<br><b>Response:</b> If an Elf is about to take a wound from a [sauron] minion or [sauron] event, exert Erestor to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("erestor");

		assertEquals("Erestor", card.getBlueprint().getTitle());
		assertEquals("Chief Advisor to Elrond", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedThenPlaysToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var shoulder = scn.GetFreepsCard("shoulder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(arwen, erestor);

		scn.StartGame();

		// A Man companion, a Man ally and an [elven] condition in play, and no Elf.
		assertInZone(Zone.HAND, erestor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(erestor));

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(erestor));

		scn.FreepsPlayCard(erestor);
		assertInZone(Zone.SUPPORT, erestor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyIsEnoughToPlayErestor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(erestor);

		scn.StartGame();

		assertInZone(Zone.HAND, erestor);
		assertTrue(scn.FreepsPlayAvailable(erestor));
		scn.FreepsPlayCard(erestor);
		assertInZone(Zone.SUPPORT, erestor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsToPreventAWoundASauronEventGivesAnElfButNotAMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(erestor);

		var hate1 = scn.GetShadowCard("hate1");
		var hate2 = scn.GetShadowCard("hate2");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		scn.MoveCardsToHand(hate1, hate2);

		scn.StartGame();
		scn.MoveMinionsToTable(orc1, orc2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(erestor));

		scn.ShadowPlayCard(hate1);
		scn.ShadowChooseCard(orc1);
		scn.ShadowChooseCard(aragorn);
		// Aragorn is a Man: nothing is offered and he takes the wound.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(erestor));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();
		scn.ShadowPlayCard(hate2);
		scn.ShadowChooseCard(arwen);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(erestor));
		scn.FreepsChooseOptionalTrigger(erestor);

		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(orc2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ExertsToPreventAWoundASauronMinionGivesAnElfAllyButNotAManAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveAlliesToTable(erestor, orophin, barliman);

		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(assassin));

		scn.ShadowUseCardAction(assassin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(erestor, orophin, barliman));
		scn.ShadowChooseCard(barliman);
		// Barliman is a Man: nothing is offered and he takes the wound.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);
		scn.ShadowChooseCard(orophin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(erestor));
		scn.FreepsChooseOptionalTrigger(erestor);

		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(2, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ErestorMayPreventTheWoundASauronMinionGivesHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveAlliesToTable(erestor, barliman);

		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(assassin));

		scn.ShadowUseCardAction(assassin);
		scn.ShadowChooseCard(erestor);
		// Erestor is himself an Elf: vitality 2, so he can pay the exertion and is offered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(erestor));
		scn.FreepsChooseOptionalTrigger(erestor);

		// His one wound is the exertion: the Assassin's wound was prevented.
		assertEquals(1, scn.GetWoundsOn(erestor));
		assertInZone(Zone.SUPPORT, erestor);
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ExertsToPreventTheSkirmishWoundOfAnElfWhoLosesToASauronMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor);

		var orc = scn.GetShadowCard("orc1");

		scn.StartGame();
		scn.MoveMinionsToTable(orc);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, orc);

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// Arwen 6 against the Orc's 7.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(erestor));
		scn.FreepsChooseOptionalTrigger(erestor);

		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(orc));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundFromAMoriaEventIsNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor);

		var hatred = scn.GetShadowCard("hatred");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(erestor));

		scn.ShadowPlayCard(hatred);
		scn.ShadowChooseCard(arwen);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(erestor));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheSkirmishWoundOfAnElfWhoLosesToAMoriaMinionIsNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(lorien);
		scn.MoveAlliesToTable(erestor);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(lorien, runner);

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(lorien));

		// The Lorien Elf 4 against the Runner's 5: lost, and not overwhelmed.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(erestor));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundFromASauronConditionIsNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor);

		var window = scn.GetShadowCard("window");
		var orc = scn.GetShadowCard("orc1");
		scn.MoveCardsToSupportArea(window);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(orc);
		// The threat the Window's trigger removes.
		scn.AddThreats(1);
		scn.SkipToMovementDecision();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(erestor));

		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(window));
		scn.ShadowAcceptOptionalTrigger();

		// Arwen is the only unbound companion: the [sauron] condition's wound lands, and Erestor is not offered.
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnArcheryWoundIsNotPreventedThoughTheArcherIsASauronMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor);

		var archers = scn.GetShadowCard("archers");

		scn.StartGame();
		scn.MoveMinionsToTable(archers);
		scn.SkipToArcheryWounds();

		// The Free Peoples hand is empty, so the Shadow player has initiative: the archery total is 2.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(erestor));

		scn.FreepsChooseCard(arwen);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));
		assertEquals(1, scn.GetWoundsOn(arwen));
		scn.FreepsChooseCard(arwen);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(erestor));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnExhaustedErestorCannotPreventAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor);

		var hate = scn.GetShadowCard("hate1");
		var orc = scn.GetShadowCard("orc1");
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.AddWoundsToChar(erestor, 1);
		assertEquals(1, scn.GetWoundsOn(erestor));
		scn.MoveMinionsToTable(orc);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.ShadowPlayCard(hate);

		// Vitality 2 with 1 wound: he cannot exert, and Arwen, the only companion Hate can choose, takes the wound.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(erestor));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
