package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_011_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("allegiance", "102_11");
					// A second copy, which stays in the draw deck: an [elven] event.
					put("allegiance2", "102_11");

					// Valiant [elven] companions, all Elves of vitality 3.  None of their texts does anything in
					// the fellowship phase.
					put("defender", "68_9");     // Elven Defender: not unique
					put("balglin", "55_10");     // Balglin, Elven Warrior: unique; to play, spot an Elf; cost 2
					put("guardian", "54_76");    // Lorien Guardian: not unique; a regroup ability
					// Valiant [rohan] Men, vitality 3.
					put("guard", "5_83");        // Household Guard: "Valiant" is his whole text
					put("recruit", "18_103");    // Rohirrim Recruit: stronger beside a [rohan] token
					// Lorien Elf: an [elven] Elf companion who is not valiant, vitality 2.
					put("lorienelf", "1_53");

					// [elven] possessions: a ranged weapon, one with no class, a hand weapon.
					put("elvenbow", "1_41");
					put("lembas", "4_75");
					put("sword", "4_64");
					// [elven] cards that are neither companions nor possessions.
					put("vilya", "3_27");        // an artifact
					put("silinde", "1_60");      // Silinde, Elf of Mirkwood: an ally
					put("eyes", "1_43");         // Far-seeing Eyes: a condition
					// Gondor Bow: a possession of another culture.
					put("gondorbow", "7_94");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ToHonourThatAllegianceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: To Honour That Allegiance
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Exert a valiant companion (or spot 2 valiant Elves) to take a valiant [elven] companion or
		 * [elven] possession into hand from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("allegiance");

		assertEquals("To Honour That Allegiance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAValiantCompanionToTakeAValiantElvenCompanionIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var allegiance = scn.GetFreepsCard("allegiance");
		var defender = scn.GetFreepsCard("defender");
		var guard = scn.GetFreepsCard("guard");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var balglin = scn.GetFreepsCard("balglin");
		// One valiant Elf only, so there are not two to spot.
		scn.MoveCompanionsToTable(defender, guard, lorienelf);
		scn.MoveCardsToHand(allegiance);

		scn.StartGame();

		assertInZone(Zone.DECK, balglin);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(allegiance));

		scn.FreepsPlayCard(allegiance);

		// A valiant Elf and a valiant Man; not the Elf who is not valiant, and not Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(defender, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienelf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(defender));

		// Looking through the draw deck shows the whole of it first.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(balglin));
		scn.FreepsChooseCardBPFromSelection(balglin);

		assertInZone(Zone.HAND, balglin);
		assertInZone(Zone.DISCARD, allegiance);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetTwilight());
		// A draw deck that has been looked through is reshuffled: once, and only the deck that was looked
		// through.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilThereIsAValiantCompanionWhoCanExertOrASecondValiantElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allegiance = scn.GetFreepsCard("allegiance");
		var defender = scn.GetFreepsCard("defender");
		var guard = scn.GetFreepsCard("guard");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var balglin = scn.GetFreepsCard("balglin");
		scn.MoveCompanionsToTable(defender, guard, lorienelf);
		scn.MoveCardsToHand(allegiance, balglin);

		// Both valiant companions, an Elf and a Man, are exhausted.  The Elf who is not valiant could exert,
		// and is an Elf.
		scn.AddWoundsToChar(defender, 2);
		scn.AddWoundsToChar(guard, 2);

		scn.StartGame();

		assertTrue(scn.IsExhausted(defender));
		assertTrue(scn.IsExhausted(guard));
		assertFalse(scn.IsExhausted(lorienelf));
		// Nobody valiant can exert, and there is one valiant Elf to spot.
		assertFalse(scn.FreepsPlayAvailable(allegiance));

		scn.FreepsPlayCard(balglin);

		assertInZone(Zone.FREE_CHARACTERS, balglin);
		assertTrue(scn.FreepsPlayAvailable(allegiance));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpotsTwoValiantElvesWhenNoValiantCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allegiance = scn.GetFreepsCard("allegiance");
		var defender = scn.GetFreepsCard("defender");
		var guardian = scn.GetFreepsCard("guardian");
		var guard = scn.GetFreepsCard("guard");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		scn.MoveCompanionsToTable(defender, guardian, guard);
		scn.MoveCardsToHand(allegiance);
		scn.AddWoundsToChar(defender, 2);
		scn.AddWoundsToChar(guardian, 2);
		scn.AddWoundsToChar(guard, 2);

		scn.StartGame();

		assertTrue(scn.IsExhausted(defender));
		assertTrue(scn.IsExhausted(guardian));
		assertTrue(scn.IsExhausted(guard));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(allegiance));

		scn.FreepsPlayCard(allegiance);

		// Nobody is asked to exert: the next thing is the draw deck.
		scn.FreepsDismissRevealedCards();
		assertEquals(2, scn.GetWoundsOn(defender));
		assertEquals(2, scn.GetWoundsOn(guardian));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsHasCardChoiceAvailable(elvenbow));
		scn.FreepsChooseCardBPFromSelection(elvenbow);

		assertInZone(Zone.HAND, elvenbow);
		assertInZone(Zone.DISCARD, allegiance);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoValiantElvesToSpotNobodyIsAskedToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allegiance = scn.GetFreepsCard("allegiance");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var guard = scn.GetFreepsCard("guard");
		var guardian = scn.GetFreepsCard("guardian");
		// Two valiant Elves to spot, and three valiant companions who could exert.
		scn.MoveCompanionsToTable(defender, balglin, guard);
		scn.MoveCardsToHand(allegiance);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(allegiance));

		scn.FreepsPlayCard(allegiance);

		// A branch in parentheses is mandatory when it is available: the spot is taken without asking, and the
		// draw deck is shown at once.
		scn.FreepsDismissRevealedCards();
		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(balglin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian));
		scn.FreepsChooseCardBPFromSelection(guardian);

		assertInZone(Zone.HAND, guardian);
		assertInZone(Zone.DISCARD, allegiance);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakesOnlyAValiantElvenCompanionOrAnElvenPossessionAndOnlyFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allegiance = scn.GetFreepsCard("allegiance");
		var allegiance2 = scn.GetFreepsCard("allegiance2");
		var guard = scn.GetFreepsCard("guard");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var recruit = scn.GetFreepsCard("recruit");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var lembas = scn.GetFreepsCard("lembas");
		var sword = scn.GetFreepsCard("sword");
		var vilya = scn.GetFreepsCard("vilya");
		var silinde = scn.GetFreepsCard("silinde");
		var eyes = scn.GetFreepsCard("eyes");
		var gondorbow = scn.GetFreepsCard("gondorbow");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(allegiance);
		// An [elven] possession that is not in the draw deck.
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();

		assertInZone(Zone.DECK, defender, balglin, elvenbow, lembas);
		assertInZone(Zone.DECK, lorienelf, recruit, vilya, silinde, eyes, allegiance2, gondorbow);
		assertInZone(Zone.DISCARD, sword);
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(allegiance);
		// The Household Guard is the only valiant companion, and exerts without a choice.
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsDismissRevealedCards();

		// Valiant [elven] companions, unique or not, and [elven] possessions with a class or without.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balglin, defender, elvenbow, lembas));
		// Not an [elven] companion who is not valiant, a valiant companion of another culture, an [elven]
		// artifact, ally, condition or event, or a possession of another culture; and not the possession in
		// the discard pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienelf, recruit, vilya, silinde, eyes, allegiance2, gondorbow, sword));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(lembas);

		assertInZone(Zone.HAND, lembas);
		assertInZone(Zone.DECK, defender, balglin, elvenbow);
		assertInZone(Zone.DISCARD, sword);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThePlayerMayTakeNothingAndTheExertionStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allegiance = scn.GetFreepsCard("allegiance");
		var guard = scn.GetFreepsCard("guard");
		var balglin = scn.GetFreepsCard("balglin");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(allegiance);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(allegiance);
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(balglin, elvenbow));
		// A player who has looked through his draw deck may choose not to find what he was looking for.
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, balglin, elvenbow);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, allegiance);
		// He did look, so the deck is shuffled.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingInTheDrawDeckToTakeItIsStillPlayedAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allegiance = scn.GetFreepsCard("allegiance");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(allegiance);
		// Every valiant [elven] companion and every [elven] possession is taken out of the draw deck.  What is
		// left includes an [elven] companion who is not valiant, valiant [rohan] companions, an [elven]
		// artifact, and a [gondor] possession.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("defender"), scn.GetFreepsCard("balglin"), scn.GetFreepsCard("guardian"),
				scn.GetFreepsCard("elvenbow"), scn.GetFreepsCard("lembas"), scn.GetFreepsCard("sword"));

		scn.StartGame();

		assertInZone(Zone.DECK, scn.GetFreepsCard("lorienelf"), scn.GetFreepsCard("recruit"), scn.GetFreepsCard("vilya"), scn.GetFreepsCard("gondorbow"));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(guard));
		// What follows "to" is an effect, and an effect that can find nothing does not stop the event.
		assertTrue(scn.FreepsPlayAvailable(allegiance));

		scn.FreepsPlayCard(allegiance);
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, allegiance);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
