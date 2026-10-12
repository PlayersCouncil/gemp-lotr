package com.gempukku.lotro.mechanics.game;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Initiative.
 *
 * Comprehensive Rules 4.2, "initiative": "At any time, either one side or the other side has initiative.  The
 * Free Peoples player has initiative while he has four or more cards in hand.  Otherwise, the Shadow has
 * initiative."  Its example: "Besieging Pike says, 'If you have initiative, bearer is strength +3.'  Since this
 * is a Shadow card, it only gets this bonus when the Free Peoples player has 3 or fewer cards in hand (the cards
 * in the hands of the Shadow players don't matter)."
 *
 * "Cost": "You must meet any requirements to play a card (or perform an action) before paying its costs."  Its
 * example: "Madril, Faramir's Aide says, 'Skirmish: If you have initiative, discard 2 cards from hand to wound a
 * roaming minion Madril is skirmishing.'  You check that you have at least four cards in hand before you discard
 * cards to pay for that ability's cost."
 *
 * "Playing a card": "Reveal The Card from your hand, and it enters the void ...  You cannot count The Card for
 * initiative. ...  If you are the Free Peoples player and initiative is a requirement for playing The Card, you
 * must have four cards in your hand not counting The Card. ...  If initiative is a requirement for an effect, you
 * cannot count The Card."  A cost that is smaller with initiative is read the same way while it is judged and
 * while it is paid (Light the Beacons): the card is in the void, and is not counted.
 *
 * The same procedure's "respond to the playing of The Card (and to losing initiative if necessary)" is in
 * DiscardedOnEntryTests (LossOfInitiativeIsAnsweredTogetherWithThePlayThatCausedIt).  "If there is more than one
 * Shadow player, then all of those Shadow players have initiative" wants a third seat, which no table here has.
 */
public class InitiativeTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Faithful Companion: strength 6, damage +1.  "While you have initiative, Gimli is
					// strength +2."
					put("gimli", "7_6");
					// Elrond, Lord of Rivendell: ally.  "Fellowship: Exert Elrond to draw a card."
					put("elrond", "1_40");
					put("armor", "1_8");        // Dwarven Armor: cost 0, on a Dwarf; a card to play from hand
					// Madril, Faramir's Aide: strength 6, vitality 3.  "Skirmish: If you have initiative,
					// discard 2 cards from hand to wound a roaming minion Madril is skirmishing."
					put("madril", "7_110");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability, not used here
					// All Save One: [gandalf] Skirmish event, cost 2.  "Make Gandalf strength +2 (or +4 and
					// damage +1 if you have initiative)."
					put("allsave1", "7_31");
					put("allsave2", "7_31");
					// Guarded Fastness: [rohan] Maneuver event, cost 1.  "If you have initiative, discard 2
					// cards from hand to heal all [rohan] allies."
					put("fastness", "7_234");
					// Guma, Plains Farmer: [rohan] ally, vitality 2.  His home is a Towers site and his
					// ability a fellowship one.
					put("guma", "4_277");
					// Light the Beacons: [gandalf] Maneuver event, cost 3.  "Exert Gandalf three times (or twice
					// if you have initiative) to make all unbound companions of one culture (except [gandalf])
					// strength +3 until the regroup phase."
					put("beacons1", "7_43");
					put("beacons2", "7_43");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability, not used here
					// Besieging Pike: hand weapon, strength +2, on a [sauron] Orc.  "If you have initiative,
					// bearer is strength +3."
					put("pike", "7_265");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, site 5 (roaming at site 2); damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("marksman", "1_176");   // Goblin Marksman: archer and nothing else
					put("brigand", "4_10");     // Dunlending Brigand: no text

					// Each player's own copies of the last five are cards to hold in hand: a Free Peoples
					// player can play none of them.
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheFreePeoplesPlayerHasInitiativeWithFourCardsInHandAndTheShadowHasItOtherwiseWhateverTheShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var armor = scn.GetFreepsCard("armor");
		var freepsTroop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(armor, scn.GetFreepsCard("savage"), scn.GetFreepsCard("runner"));

		var soldier = scn.GetShadowCard("soldier");
		var pike = scn.GetShadowCard("pike");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(freepsTroop);
		// The Orc and its Pike are laid in the fellowship phase so that the Pike is read on either side of the
		// cards the Free Peoples player draws and plays there.
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, pike);

		// Three cards in the Free Peoples hand: the Shadow has initiative, with no card in the Shadow hand.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(6, scn.GetStrength(gimli));
		// 7 + 2 for the Pike + 3 for initiative
		assertEquals(12, scn.GetStrength(soldier));

		// The Shadow hand is not what is counted: four cards there change nothing.
		scn.MoveCardsToHand(scn.GetShadowCard("savage"), scn.GetShadowCard("runner"), scn.GetShadowCard("marksman"),
				scn.GetShadowCard("brigand"));
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(12, scn.GetStrength(soldier));

		// Elrond draws the fourth card, and the initiative changes sides.
		scn.FreepsUseCardAction(elrond);
		assertInZone(Zone.HAND, freepsTroop);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		// 6 + 2
		assertEquals(8, scn.GetStrength(gimli));
		// 7 + 2 for the Pike, and nothing for initiative though the Shadow hand has four cards too
		assertEquals(9, scn.GetStrength(soldier));

		// A card played from the four takes it back.
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(12, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InitiativeIsCheckedBeforeTheCostThatTakesTheHandBelowFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var card1 = scn.GetFreepsCard("troop");
		var card2 = scn.GetFreepsCard("runner");
		var card3 = scn.GetFreepsCard("marksman");
		var card4 = scn.GetFreepsCard("brigand");
		scn.MoveCompanionsToTable(madril);
		scn.MoveCardsToHand(card1, card2, card3);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(madril, savage);

		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.IsCharSkirmishingAgainst(madril, savage));

		// Three cards: two could be discarded, but there is no initiative and the ability is not on offer.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(madril));

		// The rules' own example.  With a fourth card he has initiative as the ability is used ...
		scn.MoveCardsToHand(card4);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsActionAvailable(madril));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsUseCardAction(madril);
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(card1, card2, card3, card4));
		scn.FreepsChooseCards(card1, card2);

		// ... and the cost then leaves him two cards and no initiative.  The wound is made all the same: the
		// requirement was met before the cost was paid.
		assertInZone(Zone.DISCARD, card1, card2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InitiativeNeededForAnEffectIsReadWithoutTheEventThatIsBeingPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var allsave1 = scn.GetFreepsCard("allsave1");
		var allsave2 = scn.GetFreepsCard("allsave2");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(allsave1, allsave2, scn.GetFreepsCard("runner"), scn.GetFreepsCard("marksman"),
				scn.GetFreepsCard("brigand"));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, troop);

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		// Played from five cards, the first leaves four: with initiative, +4 and damage +1.
		scn.FreepsPlayCard(allsave1);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		// 7 + 4
		assertEquals(11, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		// The second is played from four cards, itself among them: the Free Peoples player has initiative
		// until it leaves his hand, and "you cannot count The Card" for its effect.  +2 and no more damage.
		scn.ShadowPass();
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsPlayAvailable(allsave2));
		scn.FreepsPlayCard(allsave2);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		// 7 + 4 + 2
		assertEquals(13, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		// 13 against the Troop's 13: the Shadow wins a tie and Gandalf takes the wound.  A second +4 would
		// have made it 15, and the Troop's wound, with one more for damage +1.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnEventThatNeedsInitiativeToBePlayedIsNotCountedTowardIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guma = scn.GetFreepsCard("guma");
		var fastness = scn.GetFreepsCard("fastness");
		var card1 = scn.GetFreepsCard("troop");
		var card2 = scn.GetFreepsCard("runner");
		var card3 = scn.GetFreepsCard("marksman");
		var card4 = scn.GetFreepsCard("brigand");
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(fastness, card1, card2, card3);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(guma, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		// Four cards in hand, the event one of them.  He has initiative, but "you must have four cards in your
		// hand not counting The Card": there are three, and the event cannot be played.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fastness));

		// With four cards beside it, it can.
		scn.MoveCardsToHand(card4);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(fastness));
		assertEquals(1, scn.GetWoundsOn(guma));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(fastness);
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(card1, card2, card3, card4));
		scn.FreepsChooseCards(card1, card2);

		assertInZone(Zone.DISCARD, card1, card2, fastness);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACostThatIsSmallerWithInitiativeIsReadWithoutTheEventThatIsBeingPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var madril = scn.GetFreepsCard("madril");
		var beacons1 = scn.GetFreepsCard("beacons1");
		var beacons2 = scn.GetFreepsCard("beacons2");
		scn.MoveCompanionsToTable(gandalf, madril);
		scn.MoveCardsToHand(beacons1, beacons2, scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"),
				scn.GetFreepsCard("marksman"));

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(madril));

		// Played from five cards, four beside it: with initiative, Gandalf exerts twice.  Madril is the one
		// unbound companion who is not [gandalf].
		assertTrue(scn.FreepsPlayAvailable(beacons1));
		scn.FreepsPlayCard(beacons1);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		// 6 + 3
		assertEquals(9, scn.GetStrength(madril));
		assertInZone(Zone.DISCARD, beacons1);

		// Four cards, the second copy one of them.  He has initiative, and three cards beside the event: it
		// wants three exertions, and a Gandalf of vitality 4 with two wounds has one to give.
		scn.ShadowPass();
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, beacons2);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(beacons2));

		// Unwounded he has three, and three are what he pays: the event is no more counted while its costs
		// are paid than while they were judged.
		scn.RemoveWoundsFromChar(gandalf, 2);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(beacons2));
		scn.FreepsPlayCard(beacons2);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		// 6 + 3 + 3
		assertEquals(12, scn.GetStrength(madril));
		assertInZone(Zone.DISCARD, beacons2);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
