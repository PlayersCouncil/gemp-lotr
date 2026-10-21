package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("memories", "10_2");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability never used here
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, cost 1; a fellowship ability never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man; a maneuver ability never used here
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man. "Regroup: Discard 2 cards from hand to exhaust a minion."
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion, cost 1; to play, spot a Dwarf
					put("disquiet1", "2_2");    // Disquiet of Our People: [dwarven] Maneuver event, cost 1: exert a Dwarf to make that Dwarf defender +1 until the regroup phase
					put("disquiet2", "2_2");
					put("endurance", "2_4");    // Endurance of Dwarves: [dwarven] condition, cost 1, bearer must be a Dwarf, vitality +1; a skirmish ability only
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition, cost 2, to the support area: each Dwarf is damage +1
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] condition, cost 0, to the support area; a Dwarf skirmishing a [moria] minion is strength +1
					put("heart", "1_10");       // Dwarven Heart: [dwarven] condition, bearer must be a Dwarf; the Shadow player's copy lies in his discard pile
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
	public void MemoriesofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

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
		 * Each time you lose initiative (except during the fellowship phase), you may play a [dwarven] condition
		 * from hand or from your discard pile.
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
	public void CannotBePlayedUntilADwarfIsSpottedAndThenGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(memories));

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(memories));

		scn.FreepsPlayCard(memories);
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(memories));

		scn.FreepsPlayCard(grimir);
		assertTrue(scn.FreepsPlayAvailable(memories));

		scn.FreepsPlayCard(memories);
		assertInZone(Zone.SUPPORT, memories);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LosingInitiativeInTheManeuverPhaseOffersADwarvenConditionFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var disquiet2 = scn.GetFreepsCard("disquiet2");
		var endurance = scn.GetFreepsCard("endurance");
		var lord = scn.GetFreepsCard("lord");
		var trolls = scn.GetFreepsCard("trolls");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		// Two [dwarven] conditions and a [shire] condition, each of which could be played.
		scn.MoveCardsToHand(disquiet1, disquiet2, endurance, lord, trolls);
		assertInZone(Zone.HAND, endurance, lord, trolls);

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

		// Four cards to three: initiative is lost.
		scn.FreepsPlayCard(disquiet2);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));

		scn.FreepsAcceptOptionalTrigger();
		// The two [dwarven] conditions in hand; not the [shire] condition.  Nothing is in the discard pile that
		// could be played, so no choice of piles is asked.
		assertTrue(scn.FreepsHasCardChoiceAvailable(endurance, lord));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(trolls));
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		// Gimli is the only Dwarf to bear it.
		scn.FreepsChooseCard(endurance);
		assertAttachedTo(endurance, gimli);
		assertInZone(Zone.HAND, lord, trolls);
		assertEquals(2, scn.GetWoundsOn(gimli));
		// Three cards to two is no loss of initiative: nothing more is offered.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAConditionInHandAndInTheDiscardPileThePlayerChoosesTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var endurance = scn.GetFreepsCard("endurance");
		var sword = scn.GetFreepsCard("sword");
		var ring = scn.GetFreepsCard("ring");
		var lord = scn.GetFreepsCard("lord");
		var stairs = scn.GetFreepsCard("stairs");
		var armor = scn.GetFreepsCard("armor");
		var trolls = scn.GetFreepsCard("trolls");
		var heart = scn.GetShadowCard("heart");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(disquiet1, endurance, sword, ring);
		// Two [dwarven] conditions, a [dwarven] possession and a [shire] condition in his discard pile, and a
		// [dwarven] condition of a title his piles do not hold in the Shadow player's.
		scn.MoveCardsToDiscard(lord, stairs, armor, trolls, heart);
		assertInZone(Zone.HAND, endurance, sword, ring);
		assertInZone(Zone.DISCARD, lord, stairs, armor, trolls, heart);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(disquiet1);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));
		scn.FreepsAcceptOptionalTrigger();

		// Either pile can be played from, so he is asked which.
		assertTrue(scn.FreepsChoiceAvailable("hand"));
		assertTrue(scn.FreepsChoiceAvailable("discard"));
		scn.FreepsChoose("discard");

		assertTrue(scn.FreepsHasCardChoiceAvailable(lord, stairs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, trolls, heart));
		scn.FreepsChooseCardBPFromSelection(lord);

		assertInZone(Zone.SUPPORT, lord);
		assertInZone(Zone.DISCARD, stairs, armor, trolls, heart);
		assertInZone(Zone.HAND, endurance, sword, ring);
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
		var endurance = scn.GetFreepsCard("endurance");
		var lord = scn.GetFreepsCard("lord");
		var trolls = scn.GetFreepsCard("trolls");
		scn.MoveCompanionsToTable(gimli, ranger);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(disquiet1, disquiet2, endurance, lord, trolls);
		assertInZone(Zone.HAND, disquiet1, disquiet2, endurance, lord, trolls);

		scn.StartGame();
		// No minion is in play, so the turn goes from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasInitiative());

		// Ranger of Osgiliath discards 2 cards from hand: five cards to three.
		scn.FreepsUseCardAction(ranger);
		scn.FreepsChooseCards(disquiet1, disquiet2);
		assertInZone(Zone.DISCARD, disquiet1, disquiet2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));

		scn.FreepsAcceptOptionalTrigger();
		// Not the [shire] condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(endurance, lord));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(trolls));
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		scn.FreepsChooseCard(lord);
		assertInZone(Zone.SUPPORT, lord);
		assertInZone(Zone.HAND, endurance, trolls);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ThePlayMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var endurance = scn.GetFreepsCard("endurance");
		var lord = scn.GetFreepsCard("lord");
		var sword = scn.GetFreepsCard("sword");
		var stairs = scn.GetFreepsCard("stairs");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(disquiet1, endurance, lord, sword);
		scn.MoveCardsToDiscard(stairs);
		assertInZone(Zone.HAND, endurance, lord, sword);
		assertInZone(Zone.DISCARD, stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(disquiet1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));

		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.HAND, endurance, lord, sword);
		assertInZone(Zone.DISCARD, stairs);
		assertEquals(3, scn.GetFreepsHandCount());
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
		var endurance = scn.GetFreepsCard("endurance");
		var lord = scn.GetFreepsCard("lord");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		var trolls = scn.GetFreepsCard("trolls");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(disquiet1, disquiet2, endurance, lord);
		assertInZone(Zone.HAND, disquiet1, disquiet2, endurance, lord);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(disquiet1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(endurance);
		assertAttachedTo(endurance, gimli);
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(2, scn.GetFreepsHandCount());

		// Lord of Moria goes to the discard pile, and three cards to the hand: four again, and initiative with
		// them.  The hand now holds no [dwarven] condition (a [dwarven] possession, a [shire] possession and a
		// [shire] condition), so the discard pile is the one place left to play from.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.MoveCardsToDiscard(lord);
		scn.MoveCardsToHand(armor, sword, trolls);
		assertInZone(Zone.DISCARD, lord);
		assertInZone(Zone.HAND, armor, sword, trolls);
		assertEquals(4, scn.GetFreepsHandCount());
		scn.ShadowPass();
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(disquiet2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(memories));
		scn.FreepsAcceptOptionalTrigger();
		// Lord of Moria is the one [dwarven] condition left, in the discard pile: it is played unasked.
		assertInZone(Zone.SUPPORT, lord);
		assertInZone(Zone.HAND, armor, sword, trolls);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenNoDwarvenConditionIsInHandOrInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet1 = scn.GetFreepsCard("disquiet1");
		var disquiet2 = scn.GetFreepsCard("disquiet2");
		var ring = scn.GetFreepsCard("ring");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var trolls = scn.GetFreepsCard("trolls");
		var heart = scn.GetShadowCard("heart");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		// [dwarven] cards that are no conditions: an event, an artifact Gimli could bear and a companion who could
		// be played.  In the discard pile a [dwarven] possession and a [shire] condition; the Shadow player's
		// discard pile holds a [dwarven] condition.
		scn.MoveCardsToHand(disquiet1, disquiet2, ring, guard);
		scn.MoveCardsToDiscard(armor, trolls, heart);
		assertInZone(Zone.HAND, disquiet2, ring, guard);
		assertInZone(Zone.DISCARD, armor, trolls, heart);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(disquiet1);
		assertFalse(scn.FreepsHasInitiative());
		assertInZone(Zone.HAND, disquiet2, ring, guard);
		assertInZone(Zone.DISCARD, armor, trolls, heart);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void LosingInitiativeInTheFellowshipPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memories = scn.GetFreepsCard("memories");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var endurance = scn.GetFreepsCard("endurance");
		var lord = scn.GetFreepsCard("lord");
		var sword = scn.GetFreepsCard("sword");
		var trolls = scn.GetFreepsCard("trolls");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(memories);
		scn.MoveCardsToHand(endurance, lord, sword, trolls);
		scn.MoveCardsToDiscard(stairs);
		assertInZone(Zone.HAND, endurance, lord, sword, trolls);
		assertInZone(Zone.DISCARD, stairs);

		scn.StartGame();
		assertTrue(scn.FreepsHasInitiative());

		// Four cards to three, in the fellowship phase, with [dwarven] conditions in hand and in the discard pile.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertFalse(scn.FreepsHasInitiative());
		assertInZone(Zone.HAND, endurance, lord, trolls);
		assertInZone(Zone.DISCARD, stairs);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
