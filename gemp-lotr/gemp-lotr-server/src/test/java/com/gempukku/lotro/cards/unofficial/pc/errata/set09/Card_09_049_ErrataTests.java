package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_049_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bilbo", "59_49");
					put("thereandback", "1_317");   // There and Back Again: [shire] tale, condition, cost 1, bearer must be a Hobbit companion; a maneuver ability never used here
					put("redbook", "2_113");        // Red Book of Westmarch: [shire] tale, possession, cost 2, to the support area; its text does nothing while it is in hand
					put("trolls", "3_114");         // Three Monstrous Trolls: [shire] tale, condition, cost 0, to the support area
					put("ninefingered", "10_112");  // Nine-fingered Frodo and the Ring of Doom: [shire] tale, Skirmish event, cost 0
					put("sword", "1_299");          // Hobbit Sword: [shire] possession, cost 1, bearer must be a Hobbit; no tale
					put("stairs", "1_24");          // Stairs of Khazad-dum: [dwarven] tale, condition, cost 0, to the support area, nothing to spot
				}},
				// Site 2 is Ettenmoors (Shadow number 2), site 3 Rivendell Valley (Shadow number 0).
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "1_299");          // Hobbit Sword: so that the decks are not empty; never played
				}},
				VirtualTableScenario.FellowshipSites,
				"59_49",                            // Bilbo is the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Bilbo, Bearer of Things Burgled
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 9
		 * Can be the Ring-bearer: True
		 * Game Text: <b>Ring-bound.</b>
		 * 	Each time the fellowship moves, if the twilight pool has 2 or fewer twilight tokens, add 2 burdens or add (3).
		 * 	<b>Regroup:</b> Add a burden to play a [shire] tale.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bilbo");

		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Bearer of Things Burgled", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(9, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void MovingWithTwoTwilightTokensInThePoolAsksForTwoBurdensOrThreeTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(bilbo);

		scn.StartGame();
		scn.SetTwilight(2);
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetCurrentSiteNumber());

		// Passing the fellowship phase moves the fellowship.  Bilbo's text is carried out as it moves, before
		// the move's own twilight is added: the pool still holds 2.
		scn.FreepsPass();
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Add (3)"));
		assertEquals(2, scn.GetTwilight());

		scn.FreepsChoose("burdens");
		assertEquals(2, scn.GetBurdens());
		// 2 in the pool + 2 for the Shadow number of Ettenmoors + 2 companions, and no 3 more.
		assertEquals(6, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingWithThreeTwilightTokensInThePoolAddsNeitherBurdensNorTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(bilbo);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetBurdens());
		// 3 in the pool + 2 for the Shadow number of Ettenmoors + 2 companions.
		assertEquals(7, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());

		// A second move with those 7 still in the pool: nothing again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetBurdens());
		// 7 + 0 for the Shadow number of Rivendell Valley + 2 companions.
		assertEquals(9, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
	}

	@Test
	public void EachMoveOfATurnAsksAgainAndThreeTwilightMayBeAddedInsteadOfTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(bilbo);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		// An empty pool is "2 or fewer" as well.
		scn.FreepsPass();
		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Add (3)"));

		scn.FreepsChoose("Add (3)");
		assertEquals(0, scn.GetBurdens());
		// 0 in the pool + 3 + 2 for the Shadow number of Ettenmoors + 2 companions.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second move of the turn, with the pool brought back down to 2.
		scn.SkipToMovementDecision();
		scn.SetTwilight(2);
		scn.FreepsChooseToMove();
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Add (3)"));

		scn.FreepsChoose("burdens");
		assertEquals(2, scn.GetBurdens());
		// 2 in the pool + 0 for the Shadow number of Rivendell Valley + 2 companions.
		assertEquals(4, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenBilboIsTheRingBearerTheMoveAsksTheSameAndTheBurdensAreHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var bilbo = scn.GetRingBearer();
		assertEquals("Bilbo", bilbo.getBlueprint().getTitle());

		scn.StartGame();
		scn.SetTwilight(1);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Add (3)"));

		scn.FreepsChoose("burdens");
		assertEquals(2, scn.GetBurdens());
		// 1 in the pool + 2 for the Shadow number of Ettenmoors + 1 companion.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionAddsABurdenToPlayAShireTaleFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var thereandback = scn.GetFreepsCard("thereandback");
		var redbook = scn.GetFreepsCard("redbook");
		var trolls = scn.GetFreepsCard("trolls");
		var ninefingered = scn.GetFreepsCard("ninefingered");
		var sword = scn.GetFreepsCard("sword");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(bilbo);
		scn.MoveCardsToHand(thereandback, redbook, ninefingered, sword, stairs);
		scn.MoveCardsToDiscard(trolls);

		scn.StartGame();
		// Three tokens in the pool keep Bilbo's other text quiet when the fellowship moves.
		scn.SetTwilight(3);
		// Not a fellowship action.
		assertFalse(scn.FreepsActionAvailable(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		// 3 in the pool + 2 for the Shadow number of Ettenmoors + 2 companions.
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertEquals(1, scn.GetBurdens());
		// The [shire] tales in hand that can be played now: a condition and a possession.  Not the [shire] tale
		// that is a Skirmish event, the [shire] possession that is no tale, the [dwarven] tale, or the [shire]
		// tale in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thereandback, redbook));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ninefingered, sword, stairs, trolls));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(thereandback);
		// There and Back Again goes on a Hobbit companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bilbo));
		scn.FreepsChooseCard(bilbo);
		assertAttachedTo(thereandback, bilbo);
		// 7 + 1 for There and Back Again.
		assertEquals(8, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.HAND, redbook, ninefingered, sword, stairs);
		assertInZone(Zone.DISCARD, trolls);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithoutAShireTaleInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var thereandback = scn.GetFreepsCard("thereandback");
		var trolls = scn.GetFreepsCard("trolls");
		var ninefingered = scn.GetFreepsCard("ninefingered");
		var sword = scn.GetFreepsCard("sword");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(bilbo);
		// A [shire] tale that cannot be played in the regroup phase (a Skirmish event), a [shire] card that
		// is no tale and a tale that is not [shire] in hand; a [shire] tale in the discard pile.
		scn.MoveCardsToHand(ninefingered, sword, stairs);
		scn.MoveCardsToDiscard(trolls);

		scn.StartGame();
		// Three tokens in the pool keep Bilbo's other text quiet when the fellowship moves.
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bilbo));
		assertEquals(0, scn.GetBurdens());

		// With a [shire] tale in hand that can be played, it is.
		scn.MoveCardsToHand(thereandback);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(bilbo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
