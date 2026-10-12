package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("contest", "102_7");

					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion
					put("guardian", "4_76");    // Lorien Guardian: an [elven] companion who is not Legolas
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] companion who is not Gimli
					put("prince", "31_9");      // Legolas, Prince of Mirkwood: the Legolas who is an ally
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: [elven] Elf ally
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man

					// Conditions that hold [elven] tokens.
					put("count", "4_69");       // Final Count
					put("arms", "4_82");        // Strength of Arms
					put("shake", "8_13");       // Shake Off the Shadow
					// Conditions that hold [dwarven] tokens.
					put("notched", "4_52");     // My Axe Is Notched
					put("stout", "4_57");       // Stout and Strong
					put("kinfolk", "8_8");      // Wish For Our Kinfolk

					put("troop", "1_177");      // Goblin Patrol Troop: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeadlyContestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Deadly Contest
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot an [elven] companion to reinforce a [dwarven] token or spot a [dwarven] companion
		 * to reinforce an [elven] token.
		 * If you can spot both Legolas and Gimli you may reinforce both an [elven] and [dwarven] token an
		 * additional time.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("contest");

		assertEquals("Deadly Contest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElvenCompanionReinforcesADwarvenTokenOnTheCardChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var guardian = scn.GetFreepsCard("guardian");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		var kinfolk = scn.GetFreepsCard("kinfolk");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(guardian);
		// A Dwarf who is an ally: no [dwarven] companion is spotted.
		scn.MoveAlliesToTable(scn.GetFreepsCard("grimir"));
		scn.MoveCardsToSupportArea(count, arms, notched, stout, kinfolk);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(arms, 1);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(stout, 2);
		// Wish For Our Kinfolk is given no token.

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);

		// With no [dwarven] companion there is nothing to choose between: the next thing asked is which card.
		// A card that already has a [dwarven] token may be reinforced; one with none, or with [elven] tokens, may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(notched, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(kinfolk, count, arms));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stout);

		assertEquals(3, scn.GetCultureTokensOn(stout));
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertEquals(0, scn.GetCultureTokensOn(kinfolk));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(1, scn.GetCultureTokensOn(arms));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpottingADwarvenCompanionReinforcesAnElvenTokenOnTheCardChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var guard = scn.GetFreepsCard("guard");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		var shake = scn.GetFreepsCard("shake");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(guard);
		// An Elf who is an ally: no [elven] companion is spotted.
		scn.MoveAlliesToTable(scn.GetFreepsCard("golradir"));
		scn.MoveCardsToSupportArea(count, arms, shake, notched, stout);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(arms, 2);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(stout, 1);
		// Shake Off the Shadow is given no token.

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);

		assertTrue(scn.FreepsHasCardChoiceAvailable(count, arms));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shake, notched, stout));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arms);

		assertEquals(3, scn.GetCultureTokensOn(arms));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoElvenOrDwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		scn.MoveCardsToHand(contest);
		// A companion of another culture, and an ally of each culture the text names.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("golradir"), scn.GetFreepsCard("grimir"));
		scn.MoveCardsToSupportArea(count, notched);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(notched, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(contest));
	}

	@Test
	public void MayBePlayedWithACompanionToSpotAndNoTokenToReinforce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var shake = scn.GetFreepsCard("shake");
		var kinfolk = scn.GetFreepsCard("kinfolk");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guardian"));
		// A card that could hold a token of each culture, and no token on either.
		scn.MoveCardsToSupportArea(shake, kinfolk);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// The spot is all that playing the event asks for; reinforcing comes after the "to".
		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);

		// Neither half has anything to reinforce, so the game does not ask which to attempt (F-0116).
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));

		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(0, scn.GetCultureTokensOn(kinfolk));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithLegolasAndADwarfWhoIsNotGimliTheDwarvenTokenMayBeChosenAndNothingMoreIsReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("guard"));
		scn.MoveCardsToSupportArea(count, arms, notched, stout);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(arms, 1);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(stout, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(contest);
		// Both spots are met, so the player says which half is used: here, the [elven] companion for a [dwarven] token.
		scn.FreepsChooseOption("Spot an");

		assertTrue(scn.FreepsHasCardChoiceAvailable(notched, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(count, arms));
		scn.FreepsChooseCard(notched);

		// Legolas without Gimli: no additional reinforcement, and the player is not asked about one.
		assertFalse(scn.FreepsDecisionAvailable("Would you like to reinforce both"));
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(1, scn.GetCultureTokensOn(arms));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithGimliAndAnElfWhoIsNotLegolasTheElvenTokenMayBeChosenAndNothingMoreIsReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("gimli"), scn.GetFreepsCard("guardian"));
		scn.MoveCardsToSupportArea(count, arms, notched, stout);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(arms, 1);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(stout, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(contest);
		// The other half: the [dwarven] companion for an [elven] token.
		scn.FreepsChooseOption("Spot a ");

		assertTrue(scn.FreepsHasCardChoiceAvailable(count, arms));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(notched, stout));
		scn.FreepsChooseCard(count);

		// Gimli without Legolas: no additional reinforcement, and the player is not asked about one.
		assertFalse(scn.FreepsDecisionAvailable("Would you like to reinforce both"));
		assertEquals(2, scn.GetCultureTokensOn(count));
		assertEquals(1, scn.GetCultureTokensOn(arms));
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithLegolasAndGimliAnElvenAndADwarvenTokenAreReinforcedAnAdditionalTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		var shake = scn.GetFreepsCard("shake");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		var kinfolk = scn.GetFreepsCard("kinfolk");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToSupportArea(count, arms, shake, notched, stout, kinfolk);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(arms, 1);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(stout, 1);
		// Shake Off the Shadow and Wish For Our Kinfolk are given no token.

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(contest);
		scn.FreepsChooseOption("Spot an");
		scn.FreepsChooseCard(notched);
		assertEquals(2, scn.GetCultureTokensOn(notched));

		// With Legolas and Gimli to spot, the player is asked whether to reinforce both an additional time.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reinforce both"));
		scn.FreepsChooseYes();

		// The additional [elven] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(count, arms));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shake, notched, stout, kinfolk));
		scn.FreepsChooseCard(arms);
		assertEquals(2, scn.GetCultureTokensOn(arms));

		// The additional [dwarven] token, which may go on the card already reinforced.
		assertTrue(scn.FreepsHasCardChoiceAvailable(notched, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(kinfolk, count, arms, shake));
		scn.FreepsChooseCard(notched);

		// 1 + 1 for the first sentence + 1 for the second.
		assertEquals(3, scn.GetCultureTokensOn(notched));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(2, scn.GetCultureTokensOn(arms));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(0, scn.GetCultureTokensOn(kinfolk));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheAdditionalTokensFollowTheElvenHalfTooAndMayGoOnOtherCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToSupportArea(count, arms, notched, stout);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(arms, 1);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(stout, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(contest);
		scn.FreepsChooseOption("Spot a ");
		scn.FreepsChooseCard(count);
		assertEquals(2, scn.GetCultureTokensOn(count));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to reinforce both"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCard(arms);
		scn.FreepsChooseCard(stout);

		assertEquals(2, scn.GetCultureTokensOn(count));
		assertEquals(2, scn.GetCultureTokensOn(arms));
		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void LegolasTheAllyIsSpottedWithGimliForTheAdditionalTokensButIsNoElvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("gimli"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("prince"));
		scn.MoveCardsToSupportArea(count, arms, notched, stout);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(arms, 1);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(stout, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(contest);

		// The only companion is [dwarven], so the first token is an [elven] one and no half is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(count, arms));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(notched, stout));
		scn.FreepsChooseCard(count);
		assertEquals(2, scn.GetCultureTokensOn(count));
		assertEquals(1, scn.GetCultureTokensOn(notched));

		// Legolas and Gimli are both spotted: the player is asked, and takes one more of each.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reinforce both"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCard(count);
		scn.FreepsChooseCard(notched);

		assertEquals(3, scn.GetCultureTokensOn(count));
		assertEquals(1, scn.GetCultureTokensOn(arms));
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithBothCompanionsAndNoElvenTokenInPlayTheDwarvenTokenIsReinforcedWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var shake = scn.GetFreepsCard("shake");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guardian"), scn.GetFreepsCard("guard"));
		scn.MoveCardsToSupportArea(shake, notched, stout);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(stout, 1);
		// Shake Off the Shadow, the only [elven] card that could hold a token, has none.

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(contest);

		// Only one half can be carried out, so that is the one used: the next thing asked is which [dwarven] card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(notched, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shake));
		scn.FreepsChooseCard(stout);

		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithLegolasAndGimliAndOnlyElvenTokensInPlayAnElvenTokenIsReinforcedTwiceAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		var kinfolk = scn.GetFreepsCard("kinfolk");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToSupportArea(count, arms, kinfolk);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(arms, 1);
		// Wish For Our Kinfolk, the only [dwarven] card that could hold a token, has none.

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(contest);

		// The first sentence can only reinforce an [elven] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(count, arms));
		scn.FreepsChooseCard(count);
		assertEquals(2, scn.GetCultureTokensOn(count));

		// Of the additional pair only the [elven] one can be done.  The player is asked all the same, and
		// saying yes reinforces the one token there is to reinforce.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reinforce both"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(count, arms));
		scn.FreepsChooseCard(count);

		assertEquals(3, scn.GetCultureTokensOn(count));
		assertEquals(1, scn.GetCultureTokensOn(arms));
		assertEquals(0, scn.GetCultureTokensOn(kinfolk));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnElvenCompanionAloneCannotReinforceAnElvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		var kinfolk = scn.GetFreepsCard("kinfolk");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guardian"));
		scn.MoveCardsToSupportArea(count, kinfolk);
		scn.AddTokensToCard(count, 1);
		// No [dwarven] token anywhere: Wish For Our Kinfolk could hold one and has none.

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// The spot is met, so the event may be played; it has nothing it can reinforce.
		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);

		// One half has no [dwarven] token to reinforce and the other no [dwarven] companion to spot: neither
		// can do anything, the game does not ask which to attempt (F-0116), and nothing is reinforced.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));

		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(0, scn.GetCultureTokensOn(kinfolk));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ADwarvenCompanionAloneCannotReinforceADwarvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var notched = scn.GetFreepsCard("notched");
		var shake = scn.GetFreepsCard("shake");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard"));
		scn.MoveCardsToSupportArea(notched, shake);
		scn.AddTokensToCard(notched, 1);
		// No [elven] token anywhere: Shake Off the Shadow could hold one and has none.

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);

		// One half has no [elven] token to reinforce and the other no [elven] companion to spot: neither can do
		// anything, the game does not ask which to attempt (F-0116), and nothing is reinforced.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));

		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheAdditionalReinforcementMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		var notched = scn.GetFreepsCard("notched");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToSupportArea(count, arms, notched, stout);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(arms, 1);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(stout, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(contest);
		scn.FreepsChooseOption("Spot an");
		scn.FreepsChooseCard(notched);
		assertEquals(2, scn.GetCultureTokensOn(notched));

		// Asked whether to reinforce both an additional time, the player says no.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reinforce both"));
		scn.FreepsChooseNo();

		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(1, scn.GetCultureTokensOn(arms));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithLegolasAndGimliAndNoTokenOfEitherCultureTheAdditionalReinforcementIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var shake = scn.GetFreepsCard("shake");
		var kinfolk = scn.GetFreepsCard("kinfolk");
		scn.MoveCardsToHand(contest);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("gimli"));
		// A card that could hold a token of each culture, and no token on either.
		scn.MoveCardsToSupportArea(shake, kinfolk);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// An event may be played though it will do nothing.
		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);
		// Neither half has anything to reinforce, so the game does not ask which to attempt (F-0116).
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));

		// An additional reinforcement would do nothing either, and a "may" that costs nothing and can do nothing
		// is not put to the player.
		assertFalse(scn.FreepsDecisionAvailable("Would you like to reinforce both"));
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(0, scn.GetCultureTokensOn(kinfolk));
		assertInDiscard(contest);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
