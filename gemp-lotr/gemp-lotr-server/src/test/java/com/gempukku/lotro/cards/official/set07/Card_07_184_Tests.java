package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_184_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unbearable", "7_184");
					put("unbearable2", "7_184");
					// Ulaire Nelya, Ringwraith in Twilight: a [wraith] Nazgul; fierce and damage +1 only at 2 burdens or
					// 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul; her text is about [wraith] conditions borne
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul; fierce, and his texts read burdens only
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgul (an Orc); stronger only at 2 wounds on the Ring-bearer
					put("hound", "7_192");

					// Five cards for the Free Peoples hand, so that he would have initiative: Goblin Patrol Troop, no text.
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
					put("troop5", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoreUnbearableStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: More Unbearable
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Nazgûl.
		 * <b>Regroup:</b> Discard a Nazgûl to place a [wraith] token on this card.
		 * While there are 3 [wraith] tokens on this card, the Shadow has initiative, regardless of the Free Peoples player's hand.
		*/

		var scn = GetScenario();

		var unbearable = scn.GetFreepsCard("unbearable");

		assertEquals("More Unbearable", unbearable.getBlueprint().getTitle());
		assertNull(unbearable.getBlueprint().getSubtitle());
		assertFalse(unbearable.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, unbearable.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, unbearable.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, unbearable.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(unbearable, Keyword.SUPPORT_AREA));
		assertEquals(1, unbearable.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedToTheSupportAreaWithARaiderNazgulToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unbearable = scn.GetShadowCard("unbearable");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(unbearable);
		assertInHand(unbearable);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(unbearable));
		scn.ShadowPlayCard(unbearable);
		assertInZone(Zone.SUPPORT, unbearable);
		assertEquals(0, scn.GetCultureTokensOn(unbearable));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilANazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unbearable = scn.GetShadowCard("unbearable");
		var hound = scn.GetShadowCard("hound");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(unbearable, nelya);
		assertInHand(unbearable, nelya);

		scn.StartGame();
		// A [wraith] minion who is no Nazgul.
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(unbearable));

		scn.ShadowPlayCard(nelya);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertTrue(scn.ShadowPlayAvailable(unbearable));

		scn.ShadowPlayCard(unbearable);
		assertInZone(Zone.SUPPORT, unbearable);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsANazgulOfEitherCultureToPlaceAWraithTokenOnThatCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unbearable = scn.GetShadowCard("unbearable");
		var unbearable2 = scn.GetShadowCard("unbearable2");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(unbearable, unbearable2);
		assertEquals(0, scn.GetCultureTokensOn(unbearable));
		assertEquals(0, scn.GetCultureTokensOn(unbearable2));

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, khamul, hound);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Khamul is fierce: a fierce assignment phase, with nobody assigned again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(unbearable));
		scn.ShadowUseCardAction(unbearable);
		// A Nazgul of either culture; not the [wraith] minion who is no Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertInDiscard(khamul);
		assertInPlay(nelya, hound);
		// The token goes on the copy used, and not on the other.
		assertEquals(1, scn.GetCultureTokensOn(unbearable));
		assertEquals(0, scn.GetCultureTokensOn(unbearable2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithNoNazgulToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unbearable = scn.GetShadowCard("unbearable");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(unbearable);
		assertInZone(Zone.SUPPORT, unbearable);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// Only a [wraith] minion who is no Nazgul to discard.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(unbearable));
		assertInPlay(hound);
		assertEquals(0, scn.GetCultureTokensOn(unbearable));
	}

	@Test
	public void TheThirdTokenPlacedGivesTheShadowInitiativeWhateverTheFreePeoplesHandAndAFourthKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"),
				scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"));

		var unbearable = scn.GetShadowCard("unbearable");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(unbearable);

		scn.StartGame();
		scn.AddTokensToCard(unbearable, 2);
		scn.MoveMinionsToTable(nelya, lemenya);
		assertEquals(2, scn.GetCultureTokensOn(unbearable));
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// Two tokens: the five cards in hand give the Free Peoples player initiative.
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.ShadowUseCardAction(unbearable);
		scn.ShadowChooseCard(nelya);
		assertInDiscard(nelya);
		assertEquals(3, scn.GetCultureTokensOn(unbearable));
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());

		// A fourth token: still at least three, and still the Shadow's initiative.
		scn.FreepsPass();
		scn.ShadowUseCardAction(unbearable);
		assertInDiscard(lemenya);
		assertEquals(4, scn.GetCultureTokensOn(unbearable));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoTokensOnEachOfTwoCopiesDoNotGiveTheShadowInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"),
				scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"));

		var unbearable = scn.GetShadowCard("unbearable");
		var unbearable2 = scn.GetShadowCard("unbearable2");
		scn.MoveCardsToSupportArea(unbearable, unbearable2);

		scn.StartGame();
		scn.AddTokensToCard(unbearable, 2);
		scn.AddTokensToCard(unbearable2, 2);
		scn.FreepsRefreshDecision();

		// Four [wraith] tokens in all, but three on neither card.
		assertEquals(2, scn.GetCultureTokensOn(unbearable));
		assertEquals(2, scn.GetCultureTokensOn(unbearable2));
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.AddTokensToCard(unbearable2, 1);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetCultureTokensOn(unbearable2));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
