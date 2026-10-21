package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_002_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("memories", "60_2");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability never used here
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, cost 1; a fellowship ability never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man; a maneuver ability never used here
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man. "Regroup: Discard 2 cards from hand to exhaust a minion."
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion, cost 1; to play, spot a Dwarf
					put("disquiet1", "2_2");    // Disquiet of Our People: [dwarven] Maneuver event, cost 1: exert a Dwarf to make that Dwarf defender +1 until the regroup phase
					put("disquiet2", "2_2");
					put("bracers", "2_3");      // Dwarven Bracers: [dwarven] possession, cost 1, bearer must be a Dwarf; a response only when its bearer is about to take a wound
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition, cost 2, to the support area: each Dwarf is damage +1
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] condition, cost 0, to the support area; a Dwarf skirmishing a [moria] minion is strength +1
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, cost 0, bearer must be a Dwarf
					put("ring", "9_9");         // Ring of Retribution: [dwarven] artifact, cost 0, bearer must be a Dwarf; a response only when its bearer wins a skirmish
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1, bearer must be a Hobbit; no other text
					put("trolls", "3_114");     // Three Monstrous Trolls: [shire] condition, cost 0, to the support area; a response only when a Shadow card adds twilight
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai and no Orc; damage +1 and nothing else.  Here so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MemoriesOfDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Memories of Darkness
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Dwarf.
		 * 	Each time you lose initiative (except during the fellowship phase), you may play a [Dwarven] condition or [Dwarven] possession from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("memories");

		assertEquals("Memories of Darkness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilADwarfIsSpottedAndThenGoesToTheSupportAreaForOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		// A companion who is no Dwarf, and a [dwarven] card that is no Dwarf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCardsToHand(memories, gimli);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stairs);
		assertFalse(scn.FreepsPlayAvailable(memories));

		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(memories));

		scn.FreepsPlayCard(memories);
		// 2 for Gimli + 1
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, memories);
		assertNotAttachedTo(memories, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfAllyIsADwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(memories, grimir);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(memories));

		scn.FreepsPlayCard(grimir);
		assertTrue(scn.FreepsPlayAvailable(memories));

		scn.FreepsPlayCard(memories);
		assertInZone(Zone.SUPPORT, memories);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LosingInitiativeInTheManeuverPhaseOffersADwarvenConditionOrPossessionFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var disquiet2 = scn.GetFreepsCard("disquiet2");
		var bracers = scn.GetFreepsCard("bracers");
		var lord = scn.GetFreepsCard("lord");
		var sword = scn.GetFreepsCard("sword");
		var stairs = scn.GetFreepsCard("stairs");
		var armor = scn.GetFreepsCard("armor");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(disquiet1, disquiet2, bracers, lord, sword);
		// A [dwarven] condition and a [dwarven] possession that are not in hand.
		scn.MoveCardsToDiscard(stairs, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Five cards to four: initiative is kept, and nothing is offered.
		scn.FreepsPlayCard(disquiet1);
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		var twilight = scn.GetTwilight();

		// Four cards to three: initiative is lost.
		scn.FreepsPlayCard(disquiet2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));

		scn.FreepsAcceptOptionalTrigger();
		// The [dwarven] possession and the [dwarven] condition in hand.  Not the [shire] possession, and not the
		// [dwarven] condition and possession in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bracers, lord));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, stairs, armor));
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		// Gimli is the only Dwarf to bear the bracers.
		scn.FreepsChooseCard(bracers);
		assertAttachedTo(bracers, gimli);
		// 1 for Disquiet of Our People + 1 for Dwarven Bracers
		assertEquals(twilight + 2, scn.GetTwilight());
		assertInZone(Zone.HAND, lord, sword);
		assertInZone(Zone.DISCARD, stairs, armor);
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(2, scn.GetWoundsOn(gimli));
		// Three cards to two is no loss of initiative: nothing more is offered.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardingDownToThreeCardsInTheRegroupPhaseOffersItAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var ranger = scn.GetFreepsCard("ranger");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var disquiet2 = scn.GetFreepsCard("disquiet2");
		var bracers = scn.GetFreepsCard("bracers");
		var lord = scn.GetFreepsCard("lord");
		var trolls = scn.GetFreepsCard("trolls");
		var stairs = scn.GetFreepsCard("stairs");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, ranger);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(disquiet1, disquiet2, bracers, lord, trolls);
		scn.MoveCardsToDiscard(stairs, armor);

		scn.StartGame();
		// No minion is in play, so the turn goes from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasInitiative());
		var twilight = scn.GetTwilight();

		// Ranger of Osgiliath discards 2 cards from hand: five cards to three.
		scn.FreepsUseCardAction(ranger);
		scn.FreepsChooseCards(disquiet1, disquiet2);
		assertInZone(Zone.DISCARD, disquiet1, disquiet2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));

		scn.FreepsAcceptOptionalTrigger();
		// Not the [shire] condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bracers, lord));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(trolls, stairs, armor));
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		scn.FreepsChooseCard(lord);
		assertInZone(Zone.SUPPORT, lord);
		// 2 for Lord of Moria
		assertEquals(twilight + 2, scn.GetTwilight());
		assertInZone(Zone.HAND, bracers, trolls);
		assertInZone(Zone.DISCARD, stairs, armor);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ThePlayMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var bracers = scn.GetFreepsCard("bracers");
		var lord = scn.GetFreepsCard("lord");
		var sword = scn.GetFreepsCard("sword");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(disquiet1, bracers, lord, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasInitiative());
		var twilight = scn.GetTwilight();

		scn.FreepsPlayCard(disquiet1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));

		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.HAND, bracers, lord, sword);
		assertEquals(3, scn.GetFreepsHandCount());
		// 1 for Disquiet of Our People and no more
		assertEquals(twilight + 1, scn.GetTwilight());
		// The event that lost initiative is carried out all the same.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachLossOfInitiativeInATurnOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var disquiet2 = scn.GetFreepsCard("disquiet2");
		var bracers = scn.GetFreepsCard("bracers");
		var lord = scn.GetFreepsCard("lord");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(disquiet1, disquiet2, bracers, lord);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(disquiet1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(bracers);
		assertAttachedTo(bracers, gimli);
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(2, scn.GetFreepsHandCount());

		// Two cards put into the hand: four again, and initiative with them.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.MoveCardsToHand(armor, sword);
		scn.ShadowPass();
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(disquiet2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(lord, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword));

		scn.FreepsChooseCard(lord);
		assertInZone(Zone.SUPPORT, lord);
		assertInZone(Zone.HAND, armor, sword);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenNoDwarvenConditionOrPossessionIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var disquiet2 = scn.GetFreepsCard("disquiet2");
		var ring = scn.GetFreepsCard("ring");
		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		var armor = scn.GetFreepsCard("armor");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		// [dwarven] cards that are neither conditions nor possessions: an event, an artifact that Gimli could
		// bear and a companion who could be played.
		scn.MoveCardsToHand(disquiet1, disquiet2, ring, guard);
		scn.MoveCardsToDiscard(stairs, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(disquiet1);
		assertFalse(scn.FreepsHasInitiative());
		assertInZone(Zone.HAND, disquiet2, ring, guard);
		assertInZone(Zone.DISCARD, stairs, armor);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void LosingInitiativeInTheFellowshipPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var bracers = scn.GetFreepsCard("bracers");
		var lord = scn.GetFreepsCard("lord");
		var sword = scn.GetFreepsCard("sword");
		var trolls = scn.GetFreepsCard("trolls");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(bracers, lord, sword, trolls);

		scn.StartGame();
		assertTrue(scn.FreepsHasInitiative());

		// Four cards to three, in the fellowship phase.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.HAND, bracers, lord, trolls);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
