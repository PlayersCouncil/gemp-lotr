package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_038_Tests
{

	// The Fellowship path: the skirmishes of the first turn are at Ettenmoors, "Skirmish: Exert your companion or
	// minion to make that character strength +2."
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treebeard", "15_38");

					// Gandalf, Friend of the Shirefolk: [gandalf] Wizard, strength 6, and +1 for the Hobbit beside him
					put("gandalf", "1_72");
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent, not unique; +2 only with 3 Ents in play
					// Dasron, Merchant From Dorwinion: [gandalf] Man, cost 2; his offer at the start of a fellowship
					// phase needs 2 culture tokens
					put("dasron", "13_29");
					put("erland", "2_21");      // Erland, Advisor to Brand: a [gandalf] ally; Shadow players may not look at the hand
					// Long I Fell: [gandalf] Skirmish event, cost 2.  "Spot Gandalf to prevent all wounds to him. Any
					// Shadow player may make you wound a minion to prevent this."
					put("fell", "4_97");

					// Orc War Band: [sauron] Orc, strength 10, vitality 2.  "Skirmish: Exert this minion to wound a
					// character it is skirmishing", never used here.
					put("warband", "1_272");
					put("assassin", "1_262");   // Orc Assassin: [sauron] Orc, strength 6, vitality 2; its assignment ability needs 2 Hobbits
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text is about a weapon played on it
					put("hunters", "1_267");    // Orc Hunters: [sauron] Orc, strength 9, vitality 2; the War Band's ability, never used here
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so it can never exert
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13; no text
					// Goblin Flankers: [moria] Orc, strength 11, vitality 3; its text is about being played
					put("flankers", "2_61");
					// Ulaire Enquea, Thrall of The One: Nazgul, strength 11, vitality 4.  "Enduring.  Shadow cards
					// cannot exert Ulaire Enquea during a skirmish phase."  His skirmish ability is never used here.
					put("thrall", "10_68");
					// Ulaire Enquea, Lieutenant of Morgul: Nazgul, strength 11, vitality 4.  Fierce; his maneuver
					// ability needs 6 companions or 5 burdens.
					put("lieutenant", "1_231");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreebeardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Treebeard, Enraged Shepherd
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 12
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot 3 [gandalf] companions.<br>Each time Treebeard wins a skirmish, the first Shadow
		 * player must exert X minions, where X is the difference between Treebeard's strength and the losing
		 * character's strength.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("treebeard");

		assertEquals("Treebeard", card.getBlueprint().getTitle());
		assertEquals("Enraged Shepherd", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilAThirdGandalfCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var dasron = scn.GetFreepsCard("dasron");
		var erland = scn.GetFreepsCard("erland");
		// Two [gandalf] companions, a Wizard and an Ent.  Beside them a [gandalf] ally, and Frodo, a companion
		// who is not [gandalf].
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(erland);
		scn.MoveCardsToHand(treebeard, dasron);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(treebeard));

		// The third, a Man.
		scn.FreepsPlayCard(dasron);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(treebeard));

		scn.FreepsPlayCard(treebeard);
		assertInZone(Zone.FREE_CHARACTERS, treebeard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TreebeardExerts2MinionsAgainstStrength10Minion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(treebeard, gandalf);

		var warband = scn.GetShadowCard("warband");
		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");
		var hunters = scn.GetShadowCard("hunters");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(warband, assassin, chieftain, hunters, savage, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, warband);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(treebeard);
		assertEquals(12, scn.GetStrength(treebeard));
		assertEquals(10, scn.GetStrength(warband));
		scn.PassSkirmishActions();

		// The wound for losing and Treebeard's text come at once.  The wound first: the War Band is exhausted.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(warband));

		// 12 - 10: the Shadow player must exert 2 minions, and is not asked whether.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin, chieftain, hunters, savage));
		// The exhausted War Band and the Runner, of vitality 1, cannot exert; a companion is no minion.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warband, runner, treebeard, gandalf, frodo));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.ShadowChooseCards(assassin, savage);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(hunters));
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheLosersStrengthIsWhatItWasAsTheSkirmishResolvedThoughHisWoundForLosingRaisesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(treebeard);

		var thrall = scn.GetShadowCard("thrall");
		var warband = scn.GetShadowCard("warband");
		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(thrall, warband, assassin, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, thrall);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(treebeard);
		// Ettenmoors: Treebeard exerts and is 12 + 2.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(treebeard);
		scn.PassSkirmishActions();
		assertEquals(14, scn.GetStrength(treebeard));
		assertEquals(11, scn.GetStrength(thrall));
		assertEquals(0, scn.GetWoundsOn(thrall));

		// The wound for losing is taken first: enduring, the Nazgul is now 13.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(13, scn.GetStrength(thrall));
		assertEquals(1, scn.GetWoundsOn(thrall));

		// 14 - 11, the strength the Nazgul had as the skirmish resolved (F-0480: A), not 14 - 13: three minions.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(thrall, warband, assassin, chieftain));

		// Treebeard is no Shadow card: the Nazgul may be exerted in a skirmish phase.
		scn.ShadowChooseCards(thrall, warband, assassin);
		assertEquals(2, scn.GetWoundsOn(thrall));
		assertEquals(1, scn.GetWoundsOn(warband));
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheLosersStrengthIsTakenAsItStandsInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(treebeard);

		var party = scn.GetShadowCard("party");
		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(party, assassin, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, party);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(treebeard);
		scn.FreepsPass();
		// Ettenmoors: the Raiding Party exerts and is 9 + 2.
		scn.ShadowUseCardAction(scn.GetCurrentSite());
		scn.ShadowChooseCard(party);
		assertEquals(11, scn.GetStrength(party));
		assertEquals(1, scn.GetWoundsOn(party));
		scn.PassSkirmishActions();

		// This time Treebeard's text is taken ahead of the wound for losing.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Treebeard");

		// 12 - 11, not 12 - 9: one minion.  The loser has not yet been wounded for losing and may be the one.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(party, assassin, savage));

		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		// Its exertion at the site, and the wound for losing.
		assertEquals(2, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachSkirmishHeWinsExertsMinionsByHisStrengthAsItThenStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(treebeard);

		var lieutenant = scn.GetShadowCard("lieutenant");
		var warband = scn.GetShadowCard("warband");
		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");
		var hunters = scn.GetShadowCard("hunters");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(lieutenant, warband, assassin, chieftain, hunters, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, lieutenant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(treebeard);
		scn.PassSkirmishActions();

		// 12 against the Nazgul's 11: one minion.
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(lieutenant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(hunters);
		assertEquals(1, scn.GetWoundsOn(hunters));

		// The Nazgul is fierce, and fights Treebeard again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(treebeard, lieutenant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(treebeard);

		// Ettenmoors: Treebeard exerts and is 12 + 2.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(treebeard);
		assertEquals(14, scn.GetStrength(treebeard));
		assertEquals(1, scn.GetWoundsOn(treebeard));
		scn.PassSkirmishActions();

		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(lieutenant));

		// 14 - 11, not 12 - 11: three minions.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lieutenant, warband, assassin, chieftain, savage));
		// The Hunters exerted for the first skirmish and cannot again.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hunters));

		scn.ShadowChooseCards(warband, assassin, chieftain);
		assertEquals(1, scn.GetWoundsOn(warband));
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(lieutenant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFewerMinionsThanTheDifferenceEveryMinionThatCanExertDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(treebeard);

		var savage = scn.GetShadowCard("savage");
		var warband = scn.GetShadowCard("warband");
		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, warband, assassin, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(treebeard);
		scn.PassSkirmishActions();

		// 12 against 5: the Savage is overwhelmed.  Treebeard's text is taken first here; taken after the
		// overwhelming it measures the Savage as it was all the same (WhenTheLoserIsKilledByTheWoundForLosing...).
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Treebeard");

		// 12 - 5 is 7, and three minions can exert: all three do, with nothing to choose.
		assertEquals(1, scn.GetWoundsOn(warband));
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(treebeard));
		// Then the Savage is killed.
		assertInDiscard(savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NobodyExertsWhenAnotherGandalfCompanionWinsOrTreebeardLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(treebeard, gandalf);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var warband = scn.GetShadowCard("warband");
		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, warband, assassin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gandalf, savage},
				new PhysicalCardImpl[]{treebeard, troop}
		);
		scn.ShadowDeclineAssignments();

		// Gandalf, 6 + 1 for the Hobbit, beats the Savage (5) beside Treebeard: the wound for losing, and no more.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(warband));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Treebeard, 12, loses to the Patrol Troop, 13.
		scn.FreepsResolveSkirmish(treebeard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(warband));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BeatingTwoMinionsAtOnceTheDifferenceIsFromTheirStrengthTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(treebeard);

		var savage = scn.GetShadowCard("savage");
		var assassin = scn.GetShadowCard("assassin");
		var warband = scn.GetShadowCard("warband");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, assassin, warband, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, savage);
		scn.ShadowAssignToMinions(treebeard, assassin);
		scn.FreepsResolveSkirmish(treebeard);
		scn.PassSkirmishActions();

		// 12 against 5 + 6.  Each loser takes its wound: the Assassin is exhausted.
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(assassin));

		// He won by 1: one minion, not the 7 or the 6 that either loser alone would give.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, warband, chieftain));

		scn.ShadowChooseCard(warband);
		assertEquals(1, scn.GetWoundsOn(warband));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheLoserIsKilledByTheWoundForLosingTakenFirstTheDifferenceIsFromTheStrengthItHad() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(treebeard);

		var warband = scn.GetShadowCard("warband");
		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");
		var hunters = scn.GetShadowCard("hunters");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(warband, assassin, chieftain, hunters, savage);
		// One wound from death.
		scn.AddWoundsToChar(warband, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, warband);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(treebeard);
		assertEquals(10, scn.GetStrength(warband));
		scn.PassSkirmishActions();

		// The wound for losing is taken first, and kills the War Band.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInDiscard(warband);

		// 12 - 10, the strength it had as the skirmish resolved (F-0480: A): two minions of the four.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin, chieftain, hunters, savage));

		scn.ShadowChooseCards(assassin, savage);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(hunters));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheOnlyMinionIsKilledInTheSkirmishTheDifferenceIsFromTheStrengthItHad() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(treebeard, gandalf);
		scn.MoveCardsToHand(fell);

		var flankers = scn.GetShadowCard("flankers");
		var warband = scn.GetShadowCard("warband");
		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(flankers, warband, assassin, savage);
		// One wound from death.
		scn.AddWoundsToChar(flankers, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, flankers);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(treebeard);
		assertEquals(11, scn.GetStrength(flankers));

		// Long I Fell: the Shadow player has the Free Peoples player wound a minion, and it is the Flankers.
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		scn.FreepsChooseCard(flankers);
		assertInDiscard(flankers);

		// With no minion left in it the skirmish is over and Treebeard has won it (CR 4.2 "skirmish phase(s)"):
		// the Flankers are the losing character.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		// 12 - 11: one minion of the three.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to exert"));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(warband, assassin, savage));

		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(warband));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
