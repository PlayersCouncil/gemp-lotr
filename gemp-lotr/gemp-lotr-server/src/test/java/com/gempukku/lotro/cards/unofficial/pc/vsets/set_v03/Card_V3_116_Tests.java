package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_116_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "103_116");
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion
					put("merry", "103_85");     // Merry, Master Holbytla: a Hobbit companion who is [rohan], not [shire]; his texts need a [rohan] Man or a skirmish
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a [dwarven] ally; a maneuver ability never used here
					put("raider", "1_158");     // Uruk-hai Raiding Party: [isengard] minion, twilight cost 4; damage +1 and nothing else
					put("hunter", "1_256");     // Morgul Hunter: [sauron] minion, twilight cost 5; stronger for each companion and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion with no text
					put("armory", "1_173");     // Goblin Armory: a Shadow condition in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Smeagol, Once Like Us
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype:
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Ring-bound. To play, add a burden.
		 * 	Assignment: If you cannot spot a companion with a culture other than [shire], exert Smeagol and
		 * 	hinder him to spot a minion.  Add (2) (and add 2 threats if that minion is twilight cost 5 or
		 * 	more) to hinder that minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Smeagol", card.getBlueprint().getTitle());
		assertEquals("Once Like Us", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		// No Smeagol has a race
		assertNull(card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedFromHandForOneTwilightAndABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		scn.FreepsPlayCard(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileNoBurdenCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		// No burden can be added while the Ring-bearer is hindered
		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsPlayAvailable(smeagol));

		scn.RestoreCard(frodo);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AbilityIsOfferedUnlessACompanionOfAnotherCultureCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();

		var raider = scn.GetShadowCard("raider");
		scn.MoveMinionsToTable(raider);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Frodo, Sam and Smeagol are the companions, all [shire]; Thrarin is [dwarven], but an ally.
		assertTrue(scn.FreepsActionAvailable(smeagol));

		// Merry is a Hobbit, and a [rohan] companion.
		scn.MoveCompanionsToTable(merry);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsActionAvailable(smeagol));

		// A hindered companion cannot be spotted.
		scn.HinderCard(merry);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsActionAvailable(smeagol));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AbilityExertsAndHindersSmeagolToSpotOneMinionAndHinderItForTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, sam);

		scn.StartGame();

		var raider = scn.GetShadowCard("raider");
		var hunter = scn.GetShadowCard("hunter");
		var troop = scn.GetShadowCard("troop");
		var armory = scn.GetShadowCard("armory");
		scn.MoveMinionsToTable(raider, hunter, troop);
		scn.MoveCardsToSupportArea(armory);
		scn.HinderCard(troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertFalse(scn.IsHindered(smeagol));
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.IsHindered(smeagol));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.IsHindered(sam));

		// One minion is spotted: not a minion already hindered, not a Shadow condition, not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(raider, hunter));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, armory, sam));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(twilight, scn.GetTwilight());

		// The Raiding Party costs 4: (2) and no threats.
		scn.FreepsChooseCard(raider);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.IsHindered(raider));
		assertFalse(scn.IsHindered(hunter));
		assertFalse(scn.IsHindered(armory));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MinionCostingFiveIsHinderedForTwoTwilightAndTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();

		var raider = scn.GetShadowCard("raider");
		var hunter = scn.GetShadowCard("hunter");
		scn.MoveMinionsToTable(raider, hunter);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Frodo and Smeagol: room for 2 threats, which Smeagol still counts towards once he is hindered.
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());

		scn.FreepsUseCardAction(smeagol);
		assertTrue(scn.IsHindered(smeagol));
		assertTrue(scn.FreepsHasCardChoiceAvailable(raider, hunter));

		// The Morgul Hunter costs exactly 5.
		scn.FreepsChooseCard(hunter);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsHindered(hunter));
		assertFalse(scn.IsHindered(raider));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void TwoThreatsAndNoMoreAreAddedWhereThereIsRoomForThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, sam);

		scn.StartGame();

		var raider = scn.GetShadowCard("raider");
		var hunter = scn.GetShadowCard("hunter");
		scn.MoveMinionsToTable(raider, hunter);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Frodo, Sam and Smeagol: room for 3 threats, where the board with Frodo and Smeagol alone has room
		// for no more than the 2 the text asks for.
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(0, scn.GetThreats());

		scn.FreepsUseCardAction(smeagol);
		assertTrue(scn.FreepsHasCardChoiceAvailable(raider, hunter));

		scn.FreepsChooseCard(hunter);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsHindered(hunter));
		assertFalse(scn.IsHindered(raider));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MinionCostingFourIsHinderedWithNoRoomForThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();

		var raider = scn.GetShadowCard("raider");
		var hunter = scn.GetShadowCard("hunter");
		scn.MoveMinionsToTable(raider, hunter);
		// 2 companions and 2 threats: no more can be added.
		scn.AddThreats(2);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		assertTrue(scn.FreepsHasCardChoiceAvailable(raider, hunter));

		scn.FreepsChooseCard(raider);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsHindered(raider));
		assertFalse(scn.IsHindered(hunter));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MinionCostingFiveIsNotHinderedAndNothingIsAddedWithRoomForOnlyOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();

		var raider = scn.GetShadowCard("raider");
		var hunter = scn.GetShadowCard("hunter");
		scn.MoveMinionsToTable(raider, hunter);
		// 2 companions and 1 threat: one more can be added, not two.
		scn.AddThreats(1);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		int twilight = scn.GetTwilight();
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		assertTrue(scn.FreepsHasCardChoiceAvailable(raider, hunter));

		scn.FreepsChooseCard(hunter);
		// Smeagol has paid; the price of hindering the Hunter cannot be paid in full, so none of it is.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.IsHindered(smeagol));
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.IsHindered(hunter));
		assertFalse(scn.IsHindered(raider));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedOnceSmeagolIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.AddWoundsToChar(smeagol, 2);

		scn.StartGame();

		var raider = scn.GetShadowCard("raider");
		scn.MoveMinionsToTable(raider);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Vitality 4 with 2 wounds: one exertion left.
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.AddWoundsToChar(smeagol, 1);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsActionAvailable(smeagol));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoMinionToSpotSmeagolStillPaysAndTwoTwilightIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();

		var hunter = scn.GetShadowCard("hunter");
		scn.MoveMinionsToTable(hunter);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The only minion is hindered once the phase has begun (with none to fight, the phase would be
		// skipped), and a hindered card cannot be spotted as a minion.
		scn.HinderCard(hunter);
		scn.FreepsRefreshDecision();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		// Spotting a minion comes after "to": the ability is offered while Smeagol can pay.
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.IsHindered(smeagol));
		// No minion is spotted, so there is none whose cost could call for threats; the (2) is added all the same.
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
