package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("softly", "13_56");

					put("smeagol", "6_45");         // Sméagol, Poor Creature: [gollum] companion; a skirmish ability, never used
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability only

					// For the hand: [gollum] cards of three kinds and both sides, and a card of another culture.
					put("birthday", "13_51");       // It's My Birthday: [gollum] Shadow skirmish event
					put("fish", "15_46");           // Nice Fish: [gollum] Free Peoples condition
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5; a regroup ability only
					put("runner", "1_178");         // Goblin Runner: [moria] Orc

					// Minions.
					put("scout", "1_191");          // Moria Scout: [moria] Orc, strength 6; "when you play" text not reached by a cheat
					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai, strength 5; damage +1 only
					put("soldier", "1_271");        // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SoftlyUpBehindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Softly Up Behind
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot Sméagol and discard 2 [gollum] cards from hand to return a minion with strength 6 or less to its owner's hand.<br>While this is in your discard pile, Sméagol gains <b>muster</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("softly");

		assertEquals("Softly Up Behind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotSmeagolAndDiscardTwoGollumCardsToReturnAMinionOfStrength6OrLessToItsOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var softly = scn.GetFreepsCard("softly");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		var birthday = scn.GetFreepsCard("birthday");
		var fish = scn.GetFreepsCard("fish");
		var gollumcard = scn.GetFreepsCard("gollum");
		var runnercard = scn.GetFreepsCard("runner");
		var scout = scn.GetShadowCard("scout");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(softly, birthday, fish, gollumcard, runnercard);

		scn.StartGame();
		assertInHand(softly, birthday, fish, gollumcard, runnercard);
		scn.MoveMinionsToTable(scout, savage, soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		// While the event is in hand, nobody has muster.
		assertFalse(scn.HasKeyword(smeagol, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		int shadowHand = scn.GetShadowHandCount();
		assertTrue(scn.FreepsPlayAvailable(softly));
		scn.FreepsPlayCard(softly);

		// The [gollum] cards of both sides may be discarded; the [moria] Runner may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(birthday, fish, gollumcard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runnercard));
		scn.FreepsChooseCards(birthday, fish);
		assertInDiscard(birthday, fish);
		assertInHand(gollumcard, runnercard);

		// Strength 6 and 5 may be returned; strength 7 may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier));
		scn.FreepsChooseCard(scout);
		assertInZone(Zone.HAND, scout);
		// Into its owner's hand, the Shadow player's: his hand is one larger, and the Free Peoples hand holds only the two cards kept.
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, savage, soldier);
		assertInDiscard(softly);

		// In the discard pile, it gives Sméagol muster, and nobody else.
		assertTrue(scn.HasKeyword(smeagol, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSpottedAndPlayedWithNoMinionWeakEnoughNothingIsReturned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var softly = scn.GetFreepsCard("softly");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var birthday = scn.GetFreepsCard("birthday");
		var fish = scn.GetFreepsCard("fish");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(softly, birthday, fish);

		scn.StartGame();
		assertInHand(softly, birthday, fish);
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(smeagol, Keyword.MUSTER));
		assertTrue(scn.FreepsPlayAvailable(softly));
		scn.FreepsPlayCard(softly);

		// Two [gollum] cards and two to discard: they go unasked; the Soldier, strength 7, is no target.
		assertInDiscard(softly, birthday, fish);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.HasKeyword(smeagol, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWithoutSmeagol() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var softly = scn.GetFreepsCard("softly");
		var birthday = scn.GetFreepsCard("birthday");
		var fish = scn.GetFreepsCard("fish");
		var gollum = scn.GetShadowCard("gollum");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToHand(softly, birthday, fish);

		scn.StartGame();
		assertInHand(softly, birthday, fish);
		// The Shadow player's Gollum is no Sméagol.
		scn.MoveMinionsToTable(gollum, scout);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(softly));
	}

	@Test
	public void NotPlayableWithOnlyOneOtherGollumCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var softly = scn.GetFreepsCard("softly");
		var smeagol = scn.GetFreepsCard("smeagol");
		var fish = scn.GetFreepsCard("fish");
		var runnercard = scn.GetFreepsCard("runner");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(softly, fish, runnercard);

		scn.StartGame();
		assertInHand(softly, fish, runnercard);
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);

		// The event itself is [gollum] but cannot pay for its own play (CR 4.2 "playing a card").
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(softly));
	}
}
