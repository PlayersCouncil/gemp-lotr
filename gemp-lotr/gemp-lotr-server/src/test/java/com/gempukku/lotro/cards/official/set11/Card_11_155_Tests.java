package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_155_Tests
{

	// The Fellowship path: Bag End 1_319 at site 1 (its text needs a Hobbit to exert and is never used); no site up to
	// 3 is underground, where Asfaloth would be discarded.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "11_155");

					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; a skirmish ability that needs a mount
					put("rider2", "4_286");     // a second Rider of Rohan (not unique)
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, strength 3; an archery ability that needs a villager
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn; may exert to heal another [rohan] Man who exerts
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; stronger only against a Nazgul
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: there for Saved From the Fire to spot

					// Firefoot, Mearas of The Mark and Snowmane, Noble Mearas: [rohan] mounts, strength +1 each, on a [rohan]
					// Man; their play triggers reinforce a [rohan] token and are withheld with none in play, and their
					// other texts need Éomer or Théoden
					put("firefoot", "13_126");
					put("snowmane", "13_136");
					// Asfaloth: an [elven] mount on an Elf; strength +2 at a plains, discarded at an underground site
					put("asfaloth", "1_31");
					put("spear", "4_268");      // Éomer's Spear: [rohan] hand weapon, strength +2, on a [rohan] Man; its text is about Éomer mounted

					// Saved From the Fire: Fellowship event, spot Gandalf and place a companion (except the Ring-bearer)
					// in the dead pile to take up to 3 cards of his culture from the draw deck
					put("saved", "8_20");
					put("slayer", "3_93");      // Morgul Slayer: "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RidingLikeTheWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Riding Like the Wind
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, exert a [rohan] Man.<br>While you can spot 2 [rohan] mounts, the move limit is +1.<br>Each of your companions that is not mounted is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wind");

		assertEquals("Riding Like the Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayARohanManCompanionOrAllyExertsAndNoOtherCharacterIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var wind = scn.GetFreepsCard("wind");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(wind);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(aldor));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.FreepsPlayAvailable(wind));
		scn.FreepsPlayCard(wind);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, nor Merry, a [rohan] Hobbit who is no
		// Man beside two [rohan] companions and no Eowyn, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aldor);
		// Merry's own text offers to exert him to heal the [rohan] Man who exerted: declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertInZone(Zone.SUPPORT, wind);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyRohanManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var rider = scn.GetFreepsCard("rider");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(rider, boromir);
		scn.MoveCardsToHand(wind);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.  Boromir, a Man of [gondor], could exert.
		scn.AddWoundsToChar(rider, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(boromir));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wind));
		assertInZone(Zone.HAND, wind);
	}

	@Test
	public void WithASecondRohanMountPlayedTheMoveLimitIsThreeAndOtherCardsDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var aldor = scn.GetFreepsCard("aldor");
		var arwen = scn.GetFreepsCard("arwen");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(rider, rider2, arwen);
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(rider, firefoot);
		scn.AttachCardsTo(rider2, spear);
		scn.AttachCardsTo(arwen, asfaloth);
		assertAttachedTo(firefoot, rider);
		assertAttachedTo(spear, rider2);
		assertAttachedTo(asfaloth, arwen);
		scn.MoveCardsToSupportArea(wind);
		scn.MoveCardsToHand(snowmane);

		scn.StartGame();

		// One [rohan] mount, beside an [elven] mount and a [rohan] possession that is no mount.
		assertEquals(2, scn.GetMoveLimit());

		// The second [rohan] mount, on the ally.
		scn.FreepsPlayCard(snowmane);
		scn.FreepsChooseCard(aldor);
		assertAttachedTo(snowmane, aldor);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// CR 4.2 "move limit": a change to the move limit for a turn holds for the whole turn (Ketura on Q-0179).  The
	// +1 the second mount gives as it is played in the fellowship phase is gone once that phase ends.
	@Test
	public void TheLimitGivenByAMountPlayedInTheFellowshipPhaseIsStillThreeAfterTheMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCompanionsToTable(rider);
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(rider, firefoot);
		assertAttachedTo(firefoot, rider);
		scn.MoveCardsToSupportArea(wind);
		scn.MoveCardsToHand(snowmane);

		scn.StartGame();
		assertEquals(2, scn.GetMoveLimit());

		// Aldor is the one [rohan] Man with no mount: Snowmane goes on him unasked.
		scn.FreepsPlayCard(snowmane);
		assertAttachedTo(snowmane, aldor);
		assertEquals(3, scn.GetMoveLimit());

		// Both mounts are still there after the move: the limit is the turn's, not the fellowship phase's.
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(1, scn.GetMoveCount());
		assertAttachedTo(firefoot, rider);
		assertAttachedTo(snowmane, aldor);
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void WhenAMountsBearerDiesInTheRegroupPhaseTheMoveLimitIsStillThreeAndTheFellowshipMayMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCompanionsToTable(rider);
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(rider, firefoot);
		scn.AttachCardsTo(aldor, snowmane);
		assertAttachedTo(firefoot, rider);
		assertAttachedTo(snowmane, aldor);
		scn.MoveCardsToSupportArea(wind);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		// Vitality 3: one wound more kills him.
		scn.AddWoundsToChar(rider, 2);
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetMoveCount());

		// The Rider is the one companion the Slayer can wound: he dies, and Firefoot goes with him.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		assertInZone(Zone.DEAD, rider);
		assertInZone(Zone.DISCARD, firefoot);
		// One [rohan] mount is left, and the limit is still 3: a change to the move limit holds for the whole turn.
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void TheBonusHoldsThroughASecondMoveAfterAMountHasGoneAndEndsWithTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		var saved = scn.GetFreepsCard("saved");
		scn.MoveCompanionsToTable(rider, gandalf);
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(rider, firefoot);
		scn.AttachCardsTo(aldor, snowmane);
		assertAttachedTo(firefoot, rider);
		assertAttachedTo(snowmane, aldor);
		scn.MoveCardsToSupportArea(wind);
		assertInZone(Zone.SUPPORT, wind);
		scn.MoveCardsToHand(saved);

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		// Saved From the Fire: the Rider is placed in the dead pile in the fellowship phase, and Firefoot is discarded.
		scn.FreepsPlayCard(saved);
		scn.FreepsChooseCard(rider);
		// The search of the deck for [rohan] cards: shown first, and nothing taken.
		scn.FreepsDismissRevealedCards();
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.DEAD, rider);
		assertInZone(Zone.DISCARD, firefoot);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The second move
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		scn.FreepsChooseToMove();

		// The Shadow phase after it: the bonus is still there.
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// So after two moves the fellowship is asked about a third.
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();

		// The other player's turn: 2 again.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		// And on the next turn of this card's player, with one [rohan] mount, it is 2.
		scn.SkipToOtherPlayersTurn();
		assertInZone(Zone.SUPPORT, wind);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusIsGivenAgainOnTheNextTurnAndNotOnTheOtherPlayersTurnBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCompanionsToTable(rider);
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(rider, firefoot);
		scn.AttachCardsTo(aldor, snowmane);
		assertAttachedTo(firefoot, rider);
		assertAttachedTo(snowmane, aldor);
		scn.MoveCardsToSupportArea(wind);

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		// The other player's turn: the mounts are not his, and the bonus of the turn before has ended with it.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// The next turn of this card's player: both mounts are still there, and the limit is 3 again and no more.
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachOfYourCompanionsThatIsNotMountedIsStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var wind = scn.GetFreepsCard("wind");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		var aldor = scn.GetFreepsCard("aldor");
		var firefoot = scn.GetFreepsCard("firefoot");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(rider, rider2, boromir, arwen);
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(rider, firefoot);
		scn.AttachCardsTo(rider2, spear);
		scn.AttachCardsTo(arwen, asfaloth);
		assertAttachedTo(firefoot, rider);
		assertAttachedTo(spear, rider2);
		assertAttachedTo(asfaloth, arwen);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		var arwenStrength = scn.GetStrength(arwen);
		assertEquals(6, scn.GetStrength(rider));       // 5 + 1 for Firefoot
		assertEquals(7, scn.GetStrength(rider2));      // 5 + 2 for the Spear
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(frodo));       // 3 + 1 for The Ruling Ring
		assertEquals(3, scn.GetStrength(aldor));
		assertEquals(13, scn.GetStrength(troop));

		scn.MoveCardsToSupportArea(wind);
		assertInZone(Zone.SUPPORT, wind);

		// The companions with no mount, the one bearing a possession that is no mount among them, are -1.
		assertEquals(6, scn.GetStrength(rider2));
		assertEquals(6, scn.GetStrength(boromir));
		assertEquals(3, scn.GetStrength(frodo));
		// A companion on a [rohan] mount, one on an [elven] mount, an ally and a minion are not.
		assertEquals(6, scn.GetStrength(rider));
		assertEquals(arwenStrength, scn.GetStrength(arwen));
		assertEquals(3, scn.GetStrength(aldor));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
