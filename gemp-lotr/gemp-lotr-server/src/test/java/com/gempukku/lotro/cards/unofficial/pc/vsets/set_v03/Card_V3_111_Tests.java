package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("getup", "103_111");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound [shire] companion, vitality 4
					put("merry", "1_302");      // Merry, Friend to Sam: unbound [shire] companion, vitality 4
					put("pippin", "1_306");     // Pippin, Friend to Frodo: unbound [shire] companion
					// Aragorn, Ranger of the North: [gondor] companion, vitality 4.  His maneuver ability is
					// never used here.
					put("aragorn", "1_89");
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion with no text in play
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, bearer is strength +2
					// Farmer Maggot: [shire] Hobbit ally, vitality 3, with a fellowship ability only
					put("maggot", "1_288");

					// Cards to hold and to draw.  Dwarf Guards again: nothing in a hand can play them.
					put("held1", "1_7");
					put("held2", "1_7");
					put("deck1", "1_7");
					put("deck2", "1_7");
					put("deck3", "1_7");
					put("deck4", "1_7");
					put("deck5", "1_7");
					put("deck6", "1_7");
					put("deck7", "1_7");
					put("deck8", "1_7");

					put("troop", "1_177");      // Goblin Patrol Troop: no text; there to bring a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: [shire], vitality 4
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSamRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("getup", "103_111");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] companion
					// Aragorn, Ranger of the North.  "Maneuver: Exert Aragorn to make him defender +1 until
					// the regroup phase."
					put("aragorn", "1_89");
					// Frodo, Frenzied Fighter: the one Frodo who cannot be the Ring-bearer.  His text is about
					// being overwhelmed and a skirmish ability, and does nothing in a maneuver phase.
					put("fighter", "13_149");
					put("troop", "1_177");      // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Sam, Bearer of Great Need: a Ring-bound [shire] Hobbit Ring-bearer who is not Frodo
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GetUpMisterFrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Get Up, Mister Frodo
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot Frodo to restore a [shire] card.  If you cannot spot more than 4 companions, you may
		 * 	add (3) to reconcile your hand and heal up to 2 [shire] companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("getup");

		assertEquals("Get Up, Mister Frodo", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RestoresAHinderedShireCardOfThePlayersChoiceAndTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getup = scn.GetFreepsCard("getup");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn, guard);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToHand(getup, scn.GetFreepsCard("held1"), scn.GetFreepsCard("held2"));
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Six companions on the table, two of them hindered: Frodo, Sam, Pippin and Aragorn can be spotted.
		scn.HinderCard(merry, guard, sword);

		int twilight = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.IsHindered(merry));
		assertTrue(scn.IsHindered(sword));
		assertTrue(scn.IsHindered(guard));
		// The hindered sword gives Sam nothing.
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsPlayAvailable(getup));

		scn.FreepsPlayCard(getup);

		// A [shire] companion and a [shire] possession, both hindered, are offered.  The hindered Dwarf is
		// not [shire], and Frodo, Sam and Pippin are [shire] and not hindered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, frodo, sam, pippin, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sword);

		assertFalse(scn.IsHindered(sword));
		assertEquals(5, scn.GetStrength(sam));
		assertTrue(scn.IsHindered(merry));
		assertTrue(scn.IsHindered(guard));

		// Four companions can be spotted, the hindered two not being counted, so the offer is made.
		assertTrue(scn.FreepsDecisionAvailable("reconcile your hand"));
		assertEquals(twilight + 2, scn.GetTwilight());
		scn.FreepsChooseNo();

		// Declined: nothing added, no reconcile (nothing restored, discarded or drawn), nobody healed.
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.IsHindered(merry));
		assertTrue(scn.IsHindered(guard));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInDiscard(getup);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithFourCompanionsToSpotAddingThreeReconcilesTheHandAndHealsUpToTwoShireCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getup = scn.GetFreepsCard("getup");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var maggot = scn.GetFreepsCard("maggot");
		var held1 = scn.GetFreepsCard("held1");
		var held2 = scn.GetFreepsCard("held2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, merry, aragorn, guard);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(getup, held1, held2);
		scn.AddWoundsToChar(frodo, 1);
		// Two wounds on each of the two who will be healed: a heal takes one wound off, not both.
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(maggot, 1);
		// Aragorn is made a Hobbit, so that he fails only the culture.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, aragorn, null, Race.HOBBIT));

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Exactly four companions can be spotted: Frodo, Sam, Merry and Aragorn.  Beside them are an ally
		// and a fifth companion who is hindered.
		scn.HinderCard(guard);

		int twilight = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 7);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// No [shire] card is hindered: the event is played all the same and restores nothing.
		assertTrue(scn.FreepsPlayAvailable(getup));
		scn.FreepsPlayCard(getup);

		assertTrue(scn.IsHindered(guard));
		assertTrue(scn.FreepsDecisionAvailable("reconcile your hand"));
		assertEquals(twilight + 2, scn.GetTwilight());
		scn.FreepsChooseYes();

		// 2 for the event + 3 for the offer
		assertEquals(twilight + 5, scn.GetTwilight());

		// The reconcile: every hindered card of the player's is restored, then one card may be discarded,
		// then the hand is drawn up to 8.
		assertFalse(scn.IsHindered(guard));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(held1, held2));
		scn.FreepsChooseCard(held1);
		assertInDiscard(held1);
		// 1 card kept + 7 drawn
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 7, scn.GetFreepsDeckCount());

		// Three wounded [shire] companions: the Ring-bearer, a Ring-bound one and an unbound one.  Aragorn
		// is a wounded Hobbit companion of another culture, Farmer Maggot a wounded [shire] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, maggot, guard));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.FreepsChooseCards(sam, merry);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertInDiscard(getup);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithFiveCompanionsToSpotNoOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getup = scn.GetFreepsCard("getup");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		// Frodo, Sam, Merry, Pippin and Aragorn: five companions, none hindered.
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.MoveCardsToHand(getup, scn.GetFreepsCard("held1"), scn.GetFreepsCard("held2"));
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(getup));

		scn.FreepsPlayCard(getup);

		// The event is played and paid for, and that is all.
		assertFalse(scn.FreepsDecisionAvailable("reconcile your hand"));
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInDiscard(getup);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void RestoringAFifthCompanionLeavesMoreThanFourToSpotAndNoOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getup = scn.GetFreepsCard("getup");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToHand(getup, scn.GetFreepsCard("held1"), scn.GetFreepsCard("held2"));
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Five companions on the table.  With Merry hindered only four can be spotted as the event is
		// played; the other test of this board restores the sword instead and is made the offer.
		scn.HinderCard(merry, sword);

		int twilight = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsPlayAvailable(getup));

		scn.FreepsPlayCard(getup);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, sword));
		scn.FreepsChooseCard(merry);

		// The first sentence is done before the second is read: Merry is back, five can be spotted.
		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.IsHindered(sword));
		assertFalse(scn.FreepsDecisionAvailable("reconcile your hand"));
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInDiscard(getup);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithSamAsRingBearerItCannotBePlayedUntilAFrodoWhoIsNotTheRingBearerIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamRingBearerScenario();

		var getup = scn.GetFreepsCard("getup");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fighter = scn.GetFreepsCard("fighter");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(getup);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.HinderCard(merry);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals("Sam", scn.GetRingBearer().getBlueprint().getTitle());
		// A [shire] card is there to be restored, and it is the maneuver phase: Aragorn's maneuver ability
		// is on offer.  The event is not.
		assertTrue(scn.IsHindered(merry));
		assertInHand(getup);
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertFalse(scn.FreepsPlayAvailable(getup));

		// "Frodo" is a title, not the Ring-bearer: a Frodo who does not bear the Ring is spotted all the same.
		scn.MoveCompanionsToTable(fighter);
		scn.FreepsRefreshDecision();
		assertEquals("Frodo", fighter.getBlueprint().getTitle());
		assertEquals("Sam", scn.GetRingBearer().getBlueprint().getTitle());
		assertTrue(scn.FreepsPlayAvailable(getup));
		assertInHand(getup);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileFrodoIsHinderedAndSoCannotRestoreHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getup = scn.GetFreepsCard("getup");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(getup);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(merry);
		scn.FreepsRefreshDecision();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());
		assertTrue(scn.FreepsPlayAvailable(getup));

		// A hindered Frodo cannot be spotted, though he is a hindered [shire] card and so is Merry.
		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();
		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(merry));
		assertFalse(scn.FreepsPlayAvailable(getup));

		scn.RestoreCard(frodo);
		scn.FreepsRefreshDecision();
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.FreepsPlayAvailable(getup));
		assertInHand(getup);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
