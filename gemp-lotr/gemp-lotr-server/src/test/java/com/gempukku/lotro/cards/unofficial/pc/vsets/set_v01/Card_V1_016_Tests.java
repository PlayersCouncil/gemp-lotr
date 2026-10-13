package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_016_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("myheart", "101_16");

					// Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, cost 4, vitality 4.  His text
					// only changes his own strength.
					put("gandalf", "1_72");
					// Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4.  A maneuver ability,
					// never used here.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: [dwarven] Dwarf, strength 6, vitality 3, damage +1.  A skirmish
					// ability, never used here.
					put("gimli", "1_13");
					// Legolas, Greenleaf: [elven] Elf, vitality 3.  "Archery: Exert Legolas to wound a minion;
					// Legolas does not add to the fellowship archery total."  A wound to a minion outside a
					// skirmish; using it leaves the fellowship with no archery fire.
					put("legolas", "1_50");
					// Dwarf Guard: strength 4, vitality 2.  A companion who loses his skirmish.
					put("guard", "1_7");
					// Barliman Butterbur: a [gandalf] ally, vitality 2.  A fellowship ability, never used here.
					put("barliman", "1_70");

					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3.  Damage +1 is its whole text.
					put("savage", "1_151");
					// Moria Scout: [moria] Orc, strength 6, vitality 2.  Adds twilight when played with an Elf
					// spotted; it is never played.
					put("scout", "1_191");
					// Orc Chieftain: [sauron] Orc, strength 7, vitality 2.  Adds twilight when a weapon is played
					// on it; none is.
					put("chieftain", "1_266");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MyHeartTellsMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: My Heart Tells Me
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a minion is about to take a wound, you may spot Gandalf to prevent that wound
		 * and heal a companion (limit once per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("myheart");

		assertEquals("My Heart Tells Me", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PreventsTheWoundOfAMinionThatLosesASkirmishAndHealsAChosenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var myheart = scn.GetFreepsCard("myheart");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToSupportArea(myheart);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();

		// Three wounded companions of three cultures, and a wounded ally.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(barliman, 1);

		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveMinionsToTable(chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, chieftain);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The Orc Chieftain loses and is about to take a wound.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(myheart));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));

		scn.FreepsChooseCard(gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(barliman));
		// The wound was prevented.
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoWoundedCompanionTheWoundIsStillPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var myheart = scn.GetFreepsCard("myheart");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(myheart);
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();

		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveMinionsToTable(chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, chieftain);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(myheart));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		// Nobody was there to heal, and nobody is asked to choose.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithGandalfInHandAndIsOnceHeHasBeenPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var myheart = scn.GetFreepsCard("myheart");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(myheart);
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		var scout = scn.GetShadowCard("scout");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveMinionsToTable(scout);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(legolas));

		// My Heart Tells Me is itself a [gandalf] card in play, but Gandalf is in hand.
		scn.FreepsUseCardAction(legolas);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// On to the next turn of the card's owner, where Gandalf is played.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		scn.MoveMinionsToTable(chieftain);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetWoundsOn(legolas));

		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(myheart));
		assertEquals(2, scn.GetWoundsOn(legolas));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(chieftain));
		// Legolas is the only wounded companion.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionIsAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var myheart = scn.GetFreepsCard("myheart");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(myheart);
		scn.MoveCompanionsToTable(gandalf, guard, aragorn);

		scn.StartGame();

		var scout = scn.GetShadowCard("scout");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveMinionsToTable(scout, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, scout },
				new PhysicalCardImpl[] { aragorn, chieftain });

		// Dwarf Guard, strength 4, loses to the Moria Scout, strength 6.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(scout));
		// Nothing was offered: the game has gone on to the choice of the next skirmish.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));

		// On the same board, the Orc Chieftain losing to Aragorn is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(myheart));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(chieftain));
		// The Dwarf Guard is the only wounded companion.
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLetsTheWoundLandAndLeavesTheCardToBeUsedOnAnotherMinionThatTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var myheart = scn.GetFreepsCard("myheart");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(myheart);
		scn.MoveCompanionsToTable(gandalf, legolas, aragorn);

		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 2);

		var scout = scn.GetShadowCard("scout");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveMinionsToTable(scout, chieftain);

		// A [moria] Orc is wounded by Legolas in the archery phase.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(scout);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(myheart));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// A [sauron] Orc loses a skirmish later in the same turn.  The card has not been used yet.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, chieftain);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(myheart));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsOnlyTheFirstOfTheTwoWoundsOfADamagePlusOneSkirmishLoss() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var myheart = scn.GetFreepsCard("myheart");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(myheart);
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn);

		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 2);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		// Gimli, strength 6 and damage +1, beats the Uruk Savage, strength 5, which is to take 2 wounds, one
		// at a time.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(myheart));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The first wound is prevented, and Aragorn, the only wounded companion, is healed once.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The second wound is not offered, and lands.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsUsedOnceInATurnWhateverThePhaseAndAgainOnItsOwnersNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var myheart = scn.GetFreepsCard("myheart");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(myheart);
		scn.MoveCompanionsToTable(gandalf, legolas, aragorn);

		scn.StartGame();

		var scout = scn.GetShadowCard("scout");
		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(scout, chieftain);

		// Used in the archery phase, on the Moria Scout.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(scout);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(myheart));
		assertEquals(1, scn.GetWoundsOn(legolas));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(scout));
		// Legolas, who exerted, is the only wounded companion.
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// In a skirmish phase of the same turn the Orc Chieftain loses to Aragorn: nothing is offered and the
		// wound lands.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, chieftain);
		assertEquals(0, scn.GetWoundsOn(chieftain));
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The opponent's turn, and then the owner's next.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(myheart));
		assertEquals(1, scn.GetWoundsOn(legolas));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
