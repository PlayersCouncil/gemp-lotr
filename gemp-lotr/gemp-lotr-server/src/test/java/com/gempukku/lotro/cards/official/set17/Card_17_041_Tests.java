package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "17_41");
					put("armor2", "17_41");

					// Man of Bree: a [men] Man, vitality 1, site 4 (roaming at site 2); strength +1 for each card in
					// the Free Peoples hand
					put("bree", "11_90");
					put("bree2", "11_90");
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard), vitality 4, site 4; his
					// strength bonus wants a [men] minion bearing a possession, his Skirmish ability is not used
					put("saruman", "17_48");
					// Dunlending Brigand: a [dunland] Man with no text, site 3
					put("brigand", "4_10");
					// Henchman's Dagger: a [men] possession, no minion; no text in play here
					put("dagger", "18_69");
					// Squad of Haradrim: a [men] Man, vitality 2; its Regroup ability is not reached
					put("squad", "11_99");
					// Column of Easterlings: a [men] Man, lurker.  "Skirmish: Discard this minion to make a [men]
					// minion strength +4."
					put("column", "11_72");
					// Easterling Axeman: a [raider] Man, vitality 2; nothing more below 2 burdens
					put("axeman", "4_224");

					// Aragorn, Ranger of the North: a ranger and a [gondor] Man, vitality 4
					put("aragorn", "1_89");
					// What Are They?: [gondor] Maneuver event.  "Spot a ranger to discard a roaming minion."
					put("what1", "1_119");
					put("what2", "1_119");
					put("what3", "1_119");
					// Defend It and Hope: [gondor] Maneuver event.  "Exert a [gondor] Man to wound a minion once (or
					// twice if that minion is an Uruk-hai)."
					put("defend1", "4_115");
					put("defend2", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CeremonialArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Ceremonial Armor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Each time a [men] minion is discarded during the maneuver phase, you may stack it here.<br><b>Response:</b> Discard a minion stacked here or discard this to prevent a wound to a [men] minion.
		*/

		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");

		assertEquals("Ceremonial Armor", armor.getBlueprint().getTitle());
		assertNull(armor.getBlueprint().getSubtitle());
		assertFalse(armor.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, armor.getBlueprint().getSide());
		assertEquals(Culture.MEN, armor.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, armor.getBlueprint().getCardType());
		assertNull(armor.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(armor, Keyword.SUPPORT_AREA));
		assertEquals(2, armor.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachMenMinionDiscardedInTheManeuverPhaseMayBeStackedHereAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var what1 = scn.GetFreepsCard("what1");
		var what2 = scn.GetFreepsCard("what2");
		var what3 = scn.GetFreepsCard("what3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(what1, what2, what3);

		var armor = scn.GetShadowCard("armor");
		var bree = scn.GetShadowCard("bree");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCardsToSupportArea(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(bree, saruman, brigand);
		scn.AttachCardsTo(bree, dagger);
		assertInZone(Zone.SUPPORT, armor);
		assertAttachedTo(dagger, bree);
		assertEquals(0, scn.GetStackedCards(armor).size());
		scn.SkipToPhase(Phase.MANEUVER);

		// All three are roaming at site 2.  Man of Bree goes, and his Dagger, a [men] card that is no minion, with
		// him: one offer, for the Man.
		scn.FreepsPlayCard(what1);
		scn.FreepsChooseCard(bree);
		assertInZone(Zone.DISCARD, bree, dagger);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, bree);
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertInZone(Zone.DISCARD, dagger);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armor));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A second time, for a [men] minion who is no Man.
		scn.ShadowPass();
		scn.FreepsPlayCard(what2);
		scn.FreepsChooseCard(saruman);
		assertInZone(Zone.DISCARD, saruman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, saruman);
		assertEquals(2, scn.GetStackedCards(armor).size());

		// A Man of [dunland]: no offer.
		scn.ShadowPass();
		scn.FreepsPlayCard(what3);
		assertInZone(Zone.DISCARD, brigand);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armor));
		assertEquals(2, scn.GetStackedCards(armor).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTwoCopiesTheMinionGoesToTheCopyTakenAndIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var what1 = scn.GetFreepsCard("what1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(what1);

		var armor = scn.GetShadowCard("armor");
		var armor2 = scn.GetShadowCard("armor2");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(armor, armor2);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.SUPPORT, armor, armor2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(what1);
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor2));
		scn.ShadowChooseOptionalTrigger(armor2);

		assertInZone(Zone.STACKED, bree);
		assertEquals(1, scn.GetStackedCards(armor2).size());
		assertEquals(0, scn.GetStackedCards(armor).size());
		// The Man is stacked: the other copy has nothing left to stack.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armor));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningLeavesTheMinionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var what1 = scn.GetFreepsCard("what1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(what1);

		var armor = scn.GetShadowCard("armor");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.SUPPORT, armor);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(what1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, bree);
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMenMinionKilledInTheManeuverPhaseIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);

		var armor = scn.GetShadowCard("armor");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.SUPPORT, armor);
		scn.SkipToPhase(Phase.MANEUVER);

		// The wound takes Man of Bree's one vitality.  The Armor's Response to the wound is declined on the way.
		scn.FreepsPlayCard(defend1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, bree);

		// Killed, not discarded: no offer to stack him.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armor));
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMenMinionDiscardedInASkirmishIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var armor = scn.GetShadowCard("armor");
		var column = scn.GetShadowCard("column");
		var squad = scn.GetShadowCard("squad");
		scn.MoveCardsToSupportArea(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(column, squad);
		assertInZone(Zone.SUPPORT, armor);
		scn.SkipToAssignments();
		// Both minions on Aragorn, so that his skirmish goes on once the Column has gone.
		scn.FreepsAssignToMinions(aragorn, column);
		scn.ShadowAssignToMinions(aragorn, squad);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(column));
		scn.ShadowUseCardAction(column);
		assertInZone(Zone.DISCARD, column);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armor));
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ResponseDiscardsAMinionStackedHereOrTheArmorToPreventAWoundToAMenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2);

		var armor = scn.GetShadowCard("armor");
		var bree = scn.GetShadowCard("bree");
		var squad = scn.GetShadowCard("squad");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, axeman);
		scn.StackCardsOn(armor, bree);
		assertInZone(Zone.SUPPORT, armor);
		assertInZone(Zone.STACKED, bree);
		assertEquals(0, scn.GetWoundsOn(squad));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(squad);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowAcceptOptionalTrigger();
		// Both costs can be paid, and the player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("Discard a minion stacked here"));
		assertTrue(scn.ShadowChoiceAvailable("Discard this"));
		scn.ShadowChoose("Discard a minion stacked here");

		assertInZone(Zone.DISCARD, bree);
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertInZone(Zone.SUPPORT, armor);
		assertEquals(0, scn.GetWoundsOn(squad));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A wound to a Man of [raider]: no offer, though "discard this" could still be paid.
		scn.ShadowPass();
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(axeman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armor));
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInZone(Zone.SUPPORT, armor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionStackedOnItTheArmorDiscardsItselfThoughAnotherCopyHoldsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);

		var armor = scn.GetShadowCard("armor");
		var armor2 = scn.GetShadowCard("armor2");
		var bree = scn.GetShadowCard("bree");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(armor, armor2);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.StackCardsOn(armor2, bree);
		assertInZone(Zone.SUPPORT, armor, armor2);
		assertInZone(Zone.STACKED, bree);
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.SkipToPhase(Phase.MANEUVER);

		// Saruman, a [men] minion who is no Man, the one minion to wound.
		scn.FreepsPlayCard(defend1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowChooseOptionalTrigger(armor);

		// Nothing is stacked on this copy: it discards itself, unasked, and the other copy's Man stays.
		assertInZone(Zone.DISCARD, armor);
		assertInZone(Zone.STACKED, bree);
		assertEquals(1, scn.GetStackedCards(armor2).size());
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
