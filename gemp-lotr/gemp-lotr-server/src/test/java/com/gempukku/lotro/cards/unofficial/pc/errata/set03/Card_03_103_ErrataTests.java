package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_103_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dawn", "53_103");
					put("gimli", "1_12");       // Gimli, Dwarf of Erebor: companion; Fellowship: if the pool has fewer than 2, add (2) to put a card from hand beneath the draw deck
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally; Fellowship: exert Elrond to draw a card
					put("pipe", "3_2");         // Gimli's Pipe: possession borne by Gimli; Fellowship: exert Gimli to add 1 to the pipes you can spot
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, his ability is only ever used on purpose
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion with no text but a play requirement
					put("otherfrodo", "1_290"); // Frodo, Old Bilbo's Heir: a copy of the Ring-bearer's title, to discard to heal him
					put("cruelty", "3_91");     // His Cruelty and Malice: [sauron] support condition with a regroup ability
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] minion; Shadow: exert this minion to add (1)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text
					put("filler", "1_7");       // a card for Gimli's ability to put beneath the draw deck
					put("eowyn", "4_270");      // Éowyn, Lady of Rohan: Response: if an unbound companion is about to take a wound, exert Éowyn and add (1) to prevent that wound
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc; Archer is its whole text
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
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a companion or ally's special ability is used, you may spot a [sauron] card to wound that character.  The Free Peoples player may discard another companion to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dawn");

		assertEquals("Terrible as the Dawn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CompanionUsingASpecialAbilityIsWoundedWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(dawn);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToFreepsHand("filler");

		scn.StartGame();
		scn.MoveMinionsToTable("taskmaster");
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(gimli);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dawn));
		scn.ShadowChooseOptionalTrigger(dawn);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInDiscard(dawn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AllyUsingASpecialAbilityIsWoundedAndTheShadowPlayerMayDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToHand(dawn);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.MoveMinionsToTable("taskmaster");
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));

		// Declined: nothing more happens and the event stays in hand.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dawn));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertInHand(dawn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dawn));
		scn.ShadowChooseOptionalTrigger(dawn);

		// An ally is no companion, so the Free Peoples player may still discard any companion but the Ring-bearer:
		// here there is none, and nothing is asked.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(3, scn.GetWoundsOn(elrond)); // 2 exertions + 1 wound
		assertInDiscard(dawn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CompanionUsingAResponseSpecialAbilityIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(dawn);
		scn.MoveCompanionsToTable(eowyn, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable("taskmaster", "marksman");
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		int twilight = scn.GetTwilight();

		// One archery wound, placed on Aragorn: an unbound companion is about to take a wound.
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);

		// Her Response's costs are paid, and a companion's special ability has been used (CR 4.2 "response").
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dawn));
		scn.ShadowChooseOptionalTrigger(dawn);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		// "That character" is Éowyn, about to take a wound herself: her own Response is offered for it, and left.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(eowyn)); // 1 exertion + 1 wound
		assertInDiscard(dawn);

		// The Response she used is carried out all the same: the archery wound is prevented.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.FREE_CHARACTERS, eowyn, aragorn);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void PossessionAndMinionSpecialAbilitiesDoNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var gimli = scn.GetFreepsCard("gimli");
		var pipe = scn.GetFreepsCard("pipe");
		var taskmaster = scn.GetShadowCard("taskmaster");
		scn.MoveCardsToHand(dawn);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, pipe);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.FreepsRefreshDecision();

		// The pipe's ability exerts Gimli, a companion, but it is the possession's ability.
		scn.FreepsUseCardAction(pipe);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dawn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowUseCardAction(taskmaster);
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dawn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInHand(dawn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DoesNotAnswerACompanionDiscardedToHealByRule() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var frodo = scn.GetRingBearer();
		var otherfrodo = scn.GetFreepsCard("otherfrodo");
		scn.MoveCardsToHand(dawn, otherfrodo);
		scn.MoveShadowCardsToSupportArea("cruelty");
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable("heal by discard"));
		scn.FreepsDiscardToHeal(otherfrodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dawn));
		assertInHand(dawn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpotsASauronConditionButNotACardOfAnotherCultureOrItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var elrond = scn.GetFreepsCard("elrond");
		var cruelty = scn.GetShadowCard("cruelty");
		scn.MoveCardsToHand(dawn);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.FreepsRefreshDecision();

		// Only a [moria] minion: no [sauron] card to spot, and the event cannot spot itself.
		scn.FreepsUseCardAction(elrond);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dawn));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [sauron] condition and no [sauron] minion.
		scn.MoveCardsToSupportArea(cruelty);
		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dawn));
		scn.ShadowChooseOptionalTrigger(dawn);
		assertEquals(3, scn.GetWoundsOn(elrond)); // 2 exertions + 1 wound
		assertInDiscard(dawn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FreePeoplesPlayerMayDiscardAnotherCompanionToPreventTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var elrond = scn.GetFreepsCard("elrond");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(dawn);
		scn.MoveCompanionsToTable(gimli, aragorn, guard);
		scn.MoveCardsToFreepsHand("filler");

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.MoveMinionsToTable("taskmaster");
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(gimli);
		scn.ShadowChooseOptionalTrigger(dawn);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// Another companion: not an ally, and not the Ring-bearer, who is never discarded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInDiscard(guard, dawn);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, gimli, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheCompanionWhoseAbilityWasUsedIsNotAnotherCompanionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(dawn);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToFreepsHand("filler");

		scn.StartGame();
		scn.MoveMinionsToTable("taskmaster");
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(gimli);
		scn.ShadowChooseOptionalTrigger(dawn);

		// Gimli is not "another companion", and Frodo, the Ring-bearer, cannot be discarded: nothing can pay
		// for the prevention, so it is not asked.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(dawn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
