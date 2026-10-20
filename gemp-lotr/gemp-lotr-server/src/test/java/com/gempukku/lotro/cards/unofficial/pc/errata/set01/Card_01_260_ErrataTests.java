package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_260_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("few1", "51_260");
					put("few2", "51_260");

					// With Frodo, the first six of these make seven companions; none of them is an archer.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf; a skirmish ability used only on purpose
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf, vitality 3; a response that discards 3 cards from hand, used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability used only on purpose
					put("guard1", "1_7");       // Dwarf Guard: vitality 2; to play, spot a Dwarf
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a companion for Durin's Secret to spot; a fellowship ability used only on purpose
					put("secret", "1_71");      // Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally, so a character who is not a companion

					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text; a minion so that there is a maneuver phase
					put("stand1", "1_63");      // Stand Against Darkness: Maneuver: exert an Elf to discard a [sauron] minion, condition or possession
					put("stand2", "1_63");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheNumberMustBeFewStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Number Must Be Few
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>.
		 * 	While you can spot 7 companions, the move limit for this turn is -1 (to a minimum of 1).  When you can no longer spot 7 companions or this card leaves play, this penalty is immediately lost.
		 */

		var scn = GetScenario();

		var few = scn.GetFreepsCard("few1");

		assertEquals("The Number Must Be Few", few.getBlueprint().getTitle());
		assertNull(few.getBlueprint().getSubtitle());
		assertFalse(few.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, few.getBlueprint().getSide());
		assertEquals(Culture.SAURON, few.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, few.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(few, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(few, Keyword.SUPPORT_AREA));
		assertEquals(2, few.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysToTheSupportAreaForTwoAndLowersTheMoveLimitAtSevenCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		scn.MoveCardsToHand(few);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "guard2");

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowPlayAvailable(few));

		scn.ShadowPlayCard(few);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, few);
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SixCompanionsAndAnAllyLeaveTheLimitAtTwoAndASeventhCompanionEndsTheTurnAfterOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToSupportArea(few);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1");
		// An ally is a character and not a companion: six companions and an ally are not seven companions.
		scn.MoveAlliesToTable("celeborn");
		scn.MoveCardsToHand(guard2);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(guard2);
		assertInZone(Zone.FREE_CHARACTERS, guard2);
		assertEquals(1, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		// At the move limit the turn ends without the question of another move.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WhenACompanionDiesThePenaltyIsLostAndTheFellowshipMayMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		var slayer = scn.GetShadowCard("slayer");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCardsToSupportArea(few);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "guard2");

		scn.StartGame();
		// Vitality 2: one wound more kills him.
		scn.AddWoundsToChar(guard1, 1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard1);
		assertInZone(Zone.DEAD, guard1);
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void TwoCopiesLeaveTheLimitAtOneAndEachTakesOnlyItsOwnPenaltyWhenDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few1 = scn.GetShadowCard("few1");
		var few2 = scn.GetShadowCard("few2");
		var troop = scn.GetShadowCard("troop");
		var arwen = scn.GetFreepsCard("arwen");
		var stand1 = scn.GetFreepsCard("stand1");
		var stand2 = scn.GetFreepsCard("stand2");
		scn.MoveCardsToSupportArea(few1, few2);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "guard2");
		scn.MoveCardsToHand(stand1, stand2);

		scn.StartGame();

		// 2 - 1 - 1 would be 0: the minimum is 1.
		assertEquals(1, scn.GetMoveLimit());

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPlayCard(stand1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(few1, few2));
		scn.FreepsChooseCard(few1);
		assertInDiscard(few1);
		assertInZone(Zone.SUPPORT, few2);
		assertEquals(1, scn.GetWoundsOn(arwen));
		// The copy still in play keeps its own penalty: 2 - 1.
		assertEquals(1, scn.GetMoveLimit());

		scn.ShadowPass();
		scn.FreepsPlayCard(stand2);
		assertInDiscard(few2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ASeventhCompanionPlayedAfterOneWasLostLowersTheLimitAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard3 = scn.GetFreepsCard("guard3");
		scn.MoveCardsToSupportArea(few);
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "guard2");
		scn.MoveCardsToHand(guard3);

		scn.StartGame();
		assertEquals(1, scn.GetMoveLimit());

		// The companion lost is only the setting; what is read is the limit once a seventh is played again.
		scn.MoveCardsToDiscard(guard1);
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(guard3);
		assertInZone(Zone.FREE_CHARACTERS, guard3);
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARaisedLimitIsLoweredByExactlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var few = scn.GetShadowCard("few1");
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCardsToSupportArea(few);
		// Frodo and these six are seven companions; Gimli is the Dwarf and Arwen the Elf that Durin's Secret needs.
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "sam", "guard1", "gandalf");
		scn.MoveCardsToHand(secret);

		scn.StartGame();

		// The limit cannot go below 1, so from 2 a penalty of 1 and a penalty of 2 read the same: raise the limit first.
		assertEquals(1, scn.GetMoveLimit()); // 2 - 1
		scn.FreepsPlayCard(secret);
		assertInDiscard(secret);
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
