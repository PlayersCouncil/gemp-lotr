package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("words", "12_75");
					put("men", "11_100");       // Strange-looking Men: [men] Man minion, strength 10, vitality 2; its ability is used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, vitality 4; his abilities are used only on purpose
					put("dagger", "18_69");     // Henchman's Dagger: [men] possession, bearer must be a [men] minion; no other text
					put("companies", "12_58");  // Countless Companies: [men] condition, support area; a response to a [men] possession discarded, and none is
					put("wielding", "11_105");  // Wielding the Ring: [men] condition borne by a companion; resistance -2 only
					put("legion", "11_88");     // Legion of Harad: [men] Man minion; Toil 2 is its whole text (kept in hand)
					put("axeman", "11_69");     // Axeman of Harad: [men] Man minion; a cost reduction only (kept in the discard pile)
					put("easterling", "4_224"); // Easterling Axeman: [raider] Man minion; +3 and fierce only at 2 burdens, and there are none
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: his ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PoisonousWordsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Poisonous Words
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Maneuver or Regroup
		 * Game Text: <b>Toil 2</b>. <helper>(For each [men] character you exert when playing this, its twilight cost is -2.)</helper><br>Add (1) for each [men] card you can spot.
		*/

		var scn = GetScenario();

		var words = scn.GetFreepsCard("words");

		assertEquals("Poisonous Words", words.getBlueprint().getTitle());
		assertNull(words.getBlueprint().getSubtitle());
		assertFalse(words.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, words.getBlueprint().getSide());
		assertEquals(Culture.MEN, words.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, words.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(words, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(words, Keyword.TOIL));
		assertTrue(scn.HasTimeword(words, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(words, Timeword.REGROUP));
		assertEquals(4, words.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachMenCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetShadowCard("words");
		var ted = scn.GetShadowCard("ted");
		var men = scn.GetShadowCard("men");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(words);

		scn.StartGame();
		assertInHand(words);
		scn.MoveMinionsToTable(ted, men, troop);
		// Vitality 2 with 1 wound: the Men are exhausted and cannot exert for toil.
		scn.AddWoundsToChar(men, 1);
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(men));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// 4 - 2 is the least it can cost, with Ted the one [men] character who can exert; the [moria] Troop does
		// not count. Toil 1 would want 3, and toil 3 (or the Troop counted) would let 1 do.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(words));
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(words));

		// The one exertion owed is the one candidate there is: made without a question.
		scn.ShadowPlayCard(words);
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(men));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 2 - (4 - 2) + 2 for Ted and the Men, the [men] cards in play
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(words);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void InTheManeuverPhaseAddsOneForEachMenCardInPlayOfEveryKindAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetShadowCard("words");
		var men = scn.GetShadowCard("men");
		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		var companies = scn.GetShadowCard("companies");
		var wielding = scn.GetShadowCard("wielding");
		var legion = scn.GetShadowCard("legion");
		var axeman = scn.GetShadowCard("axeman");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, wielding);
		scn.MoveCardsToHand(words, legion);
		scn.MoveCardsToDiscard(axeman);
		scn.MoveCardsToSupportArea(companies);

		scn.StartGame();
		assertAttachedTo(wielding, aragorn);
		assertInHand(words, legion);
		assertInDiscard(axeman);
		assertInZone(Zone.SUPPORT, companies);
		scn.MoveMinionsToTable(men, ted, easterling);
		scn.AttachCardsTo(ted, dagger);
		assertAttachedTo(dagger, ted);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertEquals(4, scn.GetTwilight());
		scn.ShadowPlayCard(words);
		// The pool pays the 4 in full: toil is still asked, from 0, and declined.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		// 4 - 4 + 5: the Men, Ted, the Dagger, Countless Companies and Wielding the Ring. Not the [raider]
		// Easterling, the Legion in hand, the Axeman in the discard pile, nor the event itself.
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(men));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertInDiscard(words);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void InTheRegroupPhaseAddsOneForEachOfTwoMenCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetShadowCard("words");
		var men = scn.GetShadowCard("men");
		var companies = scn.GetShadowCard("companies");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(words);
		scn.MoveCardsToSupportArea(companies);

		scn.StartGame();
		assertInHand(words);
		assertInZone(Zone.SUPPORT, companies);
		scn.MoveMinionsToTable(men, troop);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(6, scn.GetTwilight());
		scn.ShadowPlayCard(words);
		scn.ShadowDeclineChoosing();

		// 6 - 4 + 2: the Men and Countless Companies; the [moria] Troop is no [men] card.
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(words);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheArcheryPhaseOrInASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var words = scn.GetShadowCard("words");
		var men = scn.GetShadowCard("men");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(words);

		scn.StartGame();
		assertInHand(words);
		scn.MoveMinionsToTable(men);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(words));

		scn.ShadowPass();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, men);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(words));
		assertInHand(words);
	}
}
