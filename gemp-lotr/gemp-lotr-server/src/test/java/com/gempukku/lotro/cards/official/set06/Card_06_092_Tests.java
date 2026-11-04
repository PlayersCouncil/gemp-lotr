package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "6_92");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY, cost 1; a fellowship ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or
					// Eowyn can be spotted; his own abilities are used only on purpose
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					// Cards to hold in hand, one of each type and of two cultures, with no text that reaches a test
					// from there: a [gondor] companion, a [shire] possession (Hobbit Sword), a [shire] event whose
					// phase is the fellowship (Sorry About Everything), a [shire] condition (A Talent for Not Being
					// Seen), and two more [gondor] companions
					put("h1", "5_35");
					put("h2", "1_299");
					put("h3", "1_312");
					put("h4", "1_316");
					put("h5", "5_35");
					put("h6", "5_35");
					put("h7", "5_35");          // three more, for the test that uses the ability three times
					put("h8", "5_35");
					put("h9", "5_35");

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai minion, strength 5, vitality 3; damage +1 is its whole text
					put("otsea", "1_235");      // Ulaire Otsea, Lieutenant of Morgul: a Nazgul minion, strength 9, vitality 3; a Shadow ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Éomer, Rohirrim Captain
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Valiant</b>. While you can spot a [rohan] Man, Éomer's twilight cost is -1.<br><b>Skirmish:</b> Discard 3 cards from hand to make a [rohan] Man strength +2 for each wound on each minion in his or her skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", card.getBlueprint().getTitle());
		assertEquals("Rohirrim Captain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void WithARohanManCompanionToSpotEomerIsPlayedForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, aragorn);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		// Another companion in the hand, beside the [rohan] Man he spots, costs what it is printed at.
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(eomer);

		// The text names Éomer himself, so it applies to him as he is played.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanHobbitAndAGondorManDoNotLowerHisCostAndARohanManAllyPlayedThenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, guma);
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(eomer));

		scn.FreepsPlayCard(guma);

		assertInZone(Zone.SUPPORT, guma);
		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(eomer);

		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		// Two [rohan] companions: Merry is still no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsThreeCardsToMakeARohanManStrengthPlusTwoForEachWoundOnEachMinionInHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var h4 = scn.GetFreepsCard("h4");
		var h5 = scn.GetFreepsCard("h5");
		var h6 = scn.GetFreepsCard("h6");
		scn.MoveCompanionsToTable(eomer, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(h1, h2, h3, h4, h5, h6);
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		assertInHand(h1, h2, h3, h4, h5, h6);
		scn.FreepsPass();

		// At Ettenmoors, in the Shadow phase.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(troop, savage, otsea);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(otsea, 2);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(otsea));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eomer, troop}, new PhysicalCardImpl[]{aragorn, otsea});
		// The minion left over: the Shadow player puts the Uruk-hai on Éomer as well.
		scn.ShadowAssignToMinions(eomer, savage);
		scn.FreepsResolveSkirmish(eomer);

		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(6, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);
		// A companion, a possession and an event: the cost takes any three cards, of any type or culture.
		scn.FreepsChooseCards(h1, h2, h3);
		assertInDiscard(h1, h2, h3);
		assertEquals(3, scn.GetFreepsHandCount());

		// Two [rohan] companions: Merry is a Hobbit and no Man. A [rohan] Man in this skirmish and the [rohan]
		// ally in none; not Merry, nor Aragorn, a Man of [gondor] in a skirmish still to come.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(eomer);

		// 1 wound on the Orc and 2 on the Uruk-hai: 7 + 2 x 3.  The Nazgul's 2 wounds are on a minion in another
		// skirmish.
		assertEquals(13, scn.GetStrength(eomer));
		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Shadow player exerts the Orc by Ettenmoors: the bonus was counted as the ability was used.
		scn.ShadowUseCardAction(scn.GetCurrentSite());
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(13, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		// The next skirmish: the bonus lasted the skirmish it was used in.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertEquals(7, scn.GetStrength(eomer));
	}

	@Test
	public void TheWoundsCountedAreThoseInTheChosenMansOwnSkirmishAndWithNoneTheCardsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var h4 = scn.GetFreepsCard("h4");
		var h5 = scn.GetFreepsCard("h5");
		var h6 = scn.GetFreepsCard("h6");
		var h7 = scn.GetFreepsCard("h7");
		var h8 = scn.GetFreepsCard("h8");
		var h9 = scn.GetFreepsCard("h9");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.MoveCardsToHand(h1, h2, h3, h4, h5, h6, h7, h8, h9);
		var troop = scn.GetShadowCard("troop");
		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		assertInHand(h1, h2, h3, h4, h5, h6, h7, h8, h9);
		scn.MoveMinionsToTable(troop, otsea);
		scn.AddWoundsToChar(otsea, 2);
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eomer, troop}, new PhysicalCardImpl[]{guard, otsea});
		scn.FreepsResolveSkirmish(guard);

		// The Guard's skirmish, Éomer waiting for his own: Éomer uses it on the Guard.
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);
		scn.FreepsChooseCards(h1, h2, h3);
		assertEquals(6, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guard));
		scn.FreepsChooseCard(guard);

		// The Nazgul's 2 wounds: 6 + 2 x 2.  Éomer's own minion has none.
		assertEquals(10, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// Again in the Guard's skirmish, on Éomer, who waits for his own against the unwounded Troop: the Nazgul's
		// wounds are in a skirmish that is not his, and count for nothing.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);
		scn.FreepsChooseCards(h4, h5, h6);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guard));
		scn.FreepsChooseCard(eomer);
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(10, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(eomer);

		// His own skirmish, against an unwounded minion: offered, the cards paid, and nothing gained.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(eomer));
		assertInHand(h7, h8, h9);
		scn.FreepsUseCardAction(eomer);
		// The last three cards in hand are discarded unasked.
		assertInDiscard(h7, h8, h9);
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsChooseCard(eomer);

		assertEquals(7, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyTwoCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(h1, h2);
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(h1, h2);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, troop);
		scn.FreepsResolveSkirmish(eomer);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(eomer));
	}
}
