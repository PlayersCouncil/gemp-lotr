package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_014_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erestor", "53_14");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6; her response is only for the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, vitality 3
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, vitality 2
					put("shoulder", "1_59");    // Shoulder to Shoulder: [elven] support-area condition, a card of the culture that is no Elf; a maneuver ability only

					put("hate1", "1_250");      // Hate: [sauron] Maneuver event, exert a [sauron] Orc to wound a companion (except the Ring-bearer)
					put("hate2", "1_250");
					put("orc1", "1_271");       // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("orc2", "1_271");
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, vitality 3; Maneuver: exert to wound an ally
					put("enquea", "1_231");     // Ulaire Enquea: [ringwraith] Nazgul, strength 11; Maneuver: with 5 burdens, exert to wound a companion (except the Ring-bearer)
					put("terror", "1_226");     // Their Power Is in Terror: [ringwraith] Maneuver event, exert a Nazgul to wound every ally
					put("lemenya", "1_232");    // Ulaire Lemenya: [ringwraith] Nazgul whose text only weakens bearers of [ringwraith] conditions
					put("hatred", "1_164");     // Bitter Hatred: [moria] Archery event, spot a [moria] archer to wound an Elf
					put("marksman", "1_176");   // Goblin Marksman: [moria] archer with no other text
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
		 * Home: 3
		 * Game Text: To play, spot an Elf.
		 * Response: If an Elf is about to take a wound from a [sauron] or [ringwraith] card, exert Erestor to
		 * prevent that wound.
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
	public void CannotBePlayedUntilAnElfIsSpottedThenPlaysForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertFalse(scn.FreepsPlayAvailable(erestor));

		scn.FreepsPlayCard(arwen);
		assertEquals(2, scn.GetTwilight());
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
	public void ExertsToPreventAWoundARingwraithMinionGivesAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor);

		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		// Enquea's ability needs 6 companions or 5 burdens.
		scn.AddBurdens(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(enquea));

		scn.ShadowUseCardAction(enquea);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(erestor));
		scn.FreepsChooseOptionalTrigger(erestor);

		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(enquea));
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
	public void ARingwraithEventWoundingEveryAllyLetsErestorPreventHisOwnWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(erestor, barliman);

		var terror = scn.GetShadowCard("terror");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(terror);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(terror);
		// The allies are wounded one at a time, each wound offered on its own; only Erestor's is offered here.
		assertTrue(scn.FreepsDecisionAvailable("Wound Erestor"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(erestor));
		scn.FreepsChooseOptionalTrigger(erestor);

		// Erestor's one wound is the exertion: the wound Terror gave him was prevented.
		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(lemenya));
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
	public void ExertsToPreventTheSkirmishWoundOfAnElfWhoLosesToARingwraithMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor);

		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, enquea);

		assertEquals(0, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// Arwen 6 against Enquea's 11: lost, but not overwhelmed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(erestor));
		scn.FreepsChooseOptionalTrigger(erestor);

		assertEquals(1, scn.GetWoundsOn(erestor));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(enquea));
		// Enquea is fierce.
		assertTrue(scn.FreepsDecisionAvailable("assign"));
	}

	@Test
	public void AWoundFromAMoriaCardIsNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
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

		scn.ShadowPlayCard(hatred);
		scn.ShadowChooseCard(arwen);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(erestor));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnExhaustedErestorCannotPreventAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor);
		scn.AddWoundsToChar(erestor, 1);

		var hate = scn.GetShadowCard("hate1");
		var orc = scn.GetShadowCard("orc1");
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.MoveMinionsToTable(orc);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(hate);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(erestor));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(erestor));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
