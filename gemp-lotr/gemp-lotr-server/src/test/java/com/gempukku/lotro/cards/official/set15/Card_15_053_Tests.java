package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_053_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foe", "15_53");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion; a regroup ability only, never used
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: the [gollum] minion who is no Gollum; strength 8, enduring, fierce
					put("stole", "6_46");       // They Stole It: a [gollum] condition in the support area; a skirmish ability, never used
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion; its trigger is on its play only, and it is cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability, never used
					// Glorfindel, Eldarin Lord: [elven] Elf companion, strength 9; to play, spot 2 [elven] companions (cheated in);
					// his other texts answer a skirmish
					put("glorfindel", "18_13");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability, never used
					// Glamdring: strength +2 on Gandalf, so that he is 9 by a borne bonus; its regroup ability is used only on purpose
					put("glamdring", "1_75");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability only
					// Elrond, Lord of Rivendell: an ally of strength 8; his texts are at the start of the turn and in the fellowship phase
					put("elrond", "1_40");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: the [gollum] companion, a Free Peoples [gollum] card; strength 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnseenFoeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Unseen Foe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Discard a [gollum] minion from play to wound each companion with strength 8 or more.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foe");

		assertEquals("Unseen Foe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAGollumMinionToWoundEachCompanionWithStrengthEightOrMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foe = scn.GetShadowCard("foe");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var stole = scn.GetShadowCard("stole");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn, glorfindel, gandalf, boromir, smeagol);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(foe);
		// Vitality 4: exhausted.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertAttachedTo(glamdring, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob, runner, troop);
		scn.MoveCardsToSupportArea(stole);
		assertInZone(Zone.SUPPORT, stole);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Shelob is fierce: the fierce assignment phase follows, and its assignments are declined as well.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(glorfindel));
		// 7 + 2 from Glamdring
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(8, scn.GetStrength(shelob));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(glorfindel));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowPlayAvailable(foe));
		scn.ShadowPlayCard(foe);

		// The cost: either [gollum] minion, of two titles; neither the [moria] minions, nor the [gollum] condition,
		// nor the [gollum] companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, troop, stole, smeagol));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gollum);

		assertInDiscard(gollum, foe);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob, runner, troop);

		// Companions of strength 8 (exactly), 9, and 9 by a borne bonus are wounded, the exhausted Gandalf killed.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(glorfindel));
		assertInZone(Zone.DEAD, gandalf);
		// Not a companion of strength 7, an ally of 8, minions of 8 and 13, or the weaker companions.
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAGollumMinionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foe = scn.GetShadowCard("foe");
		var shelob = scn.GetShadowCard("shelob");
		var stole = scn.GetShadowCard("stole");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(foe, shelob);
		assertInHand(foe, shelob);

		scn.StartGame();
		assertInHand(foe, shelob);
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(stole);
		assertInZone(Zone.SUPPORT, stole);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// A [gollum] minion in hand, a [gollum] card in play that is no minion, a [gollum] companion and a minion
		// of another culture; a companion of strength 8 to wound.
		assertInHand(foe, shelob);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SUPPORT, stole);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(foe));
	}

	@Test
	public void WithNoCompanionOfStrengthEightTheGollumMinionIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foe = scn.GetShadowCard("foe");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(foe);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(elrond));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.ShadowPlayAvailable(foe));
		scn.ShadowPlayCard(foe);

		// The one [gollum] minion is discarded unasked; nobody has strength 8 or more, and nobody is wounded.
		assertInDiscard(gollum, foe);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
