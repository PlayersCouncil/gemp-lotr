package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kept", "6_111");
					// Sudden Fury: [shire] support-area condition, not unique; takes a [shire] token each time a
					// [shire] companion loses a skirmish, and its skirmish ability is used only on purpose
					put("fury", "12_132");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unbound [shire] Hobbit
					put("pippin", "103_31");    // Pippin, Trollslayer: an unbound Hobbit of [gondor]
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit who is Ring-bound
					put("bounder", "1_286");    // Bounder: a Hobbit ally, and only a companion is unbound
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: an unbound companion who is no Hobbit
					// Goblin Marksman: a [moria] archer and nothing else; each adds 1 to the minion archery total
					put("marksman1", "1_176");
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("marksman4", "1_176");
					put("marksman5", "1_176");
					put("assassin", "2_93");    // Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KeptSafeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Kept Safe
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area. When you play this condition, place 2 [shire] tokens here.<br><b>Response:</b> If an unbound Hobbit is about to take a wound, discard this condition or remove a [shire] token from here to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("kept");

		assertEquals("Kept Safe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromHandItTakesTwoShireTokensAndAShireConditionPlayedAfterItTakesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kept = scn.GetFreepsCard("kept");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCardsToHand(kept, fury);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(kept));
		assertEquals(0, scn.GetCultureTokensOn(fury));
		scn.FreepsPlayCard(kept);
		assertInZone(Zone.SUPPORT, kept);
		assertEquals(2, scn.GetCultureTokensOn(kept));

		// Another [shire] condition played once Kept Safe is in play: the tokens are for Kept Safe's own play.
		scn.FreepsPlayCard(fury);
		assertInZone(Zone.SUPPORT, fury);
		assertEquals(0, scn.GetCultureTokensOn(fury));
		assertEquals(2, scn.GetCultureTokensOn(kept));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovesATokenToPreventAWoundToAnUnboundHobbitOfEitherCultureButNotToSamTheRingBearerOrGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kept = scn.GetFreepsCard("kept");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(kept);
		scn.MoveCompanionsToTable(merry, pippin, sam, gandalf);

		scn.StartGame();
		// Cheated into play, Kept Safe has none of the tokens its play would give it.
		scn.AddTokensToCard(kept, 2);
		assertEquals(2, scn.GetCultureTokensOn(kept));
		assertInZone(Zone.SUPPORT, kept);
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3", "marksman4", "marksman5");
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(5, scn.GetShadowArcheryTotal());
		scn.PassArcheryActions();

		assertEquals(2, scn.GetCultureTokensOn(kept));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Sam is a Hobbit, but Ring-bound: the wound is placed with nothing offered.
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kept));
		assertTrue(scn.FreepsDecisionAvailable("Choose character to assign archery wound"));

		// The Ring-bearer is a Hobbit and Ring-bound too.
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kept));
		assertTrue(scn.FreepsDecisionAvailable("Choose character to assign archery wound"));

		// Gandalf is unbound, and no Hobbit.
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kept));
		assertTrue(scn.FreepsDecisionAvailable("Choose character to assign archery wound"));
		assertEquals(2, scn.GetCultureTokensOn(kept));

		scn.FreepsChooseCard(merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kept));
		scn.FreepsChooseOptionalTrigger(kept);
		// With a token here both costs can be paid, and the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("token");
		assertEquals(1, scn.GetCultureTokensOn(kept));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertInZone(Zone.SUPPORT, kept);

		// Pippin, Trollslayer is a [gondor] Hobbit: unbound all the same.
		scn.FreepsChooseCard(pippin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kept));
		scn.FreepsChooseOptionalTrigger(kept);
		scn.FreepsChooseOption("token");
		assertEquals(0, scn.GetCultureTokensOn(kept));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertInZone(Zone.SUPPORT, kept);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithATokenHereTheConditionMayBeDiscardedInsteadToPreventTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kept = scn.GetFreepsCard("kept");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(kept);
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.AddTokensToCard(kept, 1);
		assertEquals(1, scn.GetCultureTokensOn(kept));
		assertInZone(Zone.SUPPORT, kept);
		scn.MoveMinionsToTable("marksman1");
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();

		assertEquals(1, scn.GetCultureTokensOn(kept));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsChooseCard(merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kept));
		scn.FreepsChooseOptionalTrigger(kept);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Discard");

		assertInDiscard(kept);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoTokenHereTheConditionIsDiscardedUnaskedAndASuddenFuryKeepsItsShireToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kept = scn.GetFreepsCard("kept");
		var fury = scn.GetFreepsCard("fury");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(kept, fury);
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.AddTokensToCard(fury, 1);
		assertEquals(1, scn.GetCultureTokensOn(fury));
		assertEquals(0, scn.GetCultureTokensOn(kept));
		assertInZone(Zone.SUPPORT, kept, fury);
		scn.MoveMinionsToTable("marksman1");
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();

		assertEquals(0, scn.GetCultureTokensOn(kept));
		assertEquals(1, scn.GetCultureTokensOn(fury));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsChooseCard(merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kept));
		scn.FreepsChooseOptionalTrigger(kept);

		// A [shire] token on another card is no token "from here": the discard is the one cost left, paid unasked.
		assertInDiscard(kept);
		assertEquals(1, scn.GetCultureTokensOn(fury));
		assertInZone(Zone.SUPPORT, fury);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedForAWoundToAHobbitAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kept = scn.GetFreepsCard("kept");
		var bounder = scn.GetFreepsCard("bounder");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCardsToSupportArea(kept);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.AddTokensToCard(kept, 2);
		assertEquals(2, scn.GetCultureTokensOn(kept));
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetCultureTokensOn(kept));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowActionAvailable(assassin));

		// The Bounder is the only ally: he is wounded unasked, and Kept Safe is not offered.
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kept));
		assertEquals(2, scn.GetCultureTokensOn(kept));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
