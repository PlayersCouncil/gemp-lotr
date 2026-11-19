package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_019_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("betsy", "33_19");
					// Master Worrywort, Hobbit Farmer: [shire] Hobbit ally, home 1H; a trigger on Bilbo's win and a
					// skirmish ability only
					put("worrywort", "33_53");
					// Lindir, Elven Minstrel: [elven] Elf ally, home 3H; a move trigger that heals an [elven] ally
					put("lindir", "33_14");
					// Orkish Aggressor: [gundabad] Orc, vitality 2; an Assignment ability never used
					put("aggressor", "30_39");
					// Warg: [gundabad] mount on a [gundabad] Orc, "Bearer is fierce. Regroup: If you cannot spot
					// Thorin, exert bearer to wound an ally."
					put("warg", "31_27");
					// Free Peoples events of two cultures and phases.
					put("fury", "30_3");        // Battle Fury: [dwarven] skirmish event
					put("late", "30_23");       // A Wizard Is Never Late: [gandalf] fellowship event
					// Rise of the Necromancer: a [sauron] skirmish event, a Shadow one
					put("rise", "33_51");
					// Vilya: an [elven] artifact, no event
					put("vilya", "33_17");
					// Wise Advice: an [elven] fellowship event, laid in the other player's discard pile
					put("advice", "33_18");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BetsyButterburStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Betsy Butterbur, Prancing Pony Proprietor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 1
		 * Vitality: 2
		 * Home: 1H
		 * Game Text: Betsy Butterbur cannot take wounds.<br><b>Fellowship:</b> Exert Betsy Butterbur to take a Free Peoples event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("betsy");

		assertEquals("Betsy Butterbur", card.getBlueprint().getTitle());
		assertEquals("Prancing Pony Proprietor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 1)));
	}

	@Test
	public void BetsyIsNotOfferedForAWoundThatOtherAlliesMayTake() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betsy = scn.GetFreepsCard("betsy");
		var worrywort = scn.GetFreepsCard("worrywort");
		var lindir = scn.GetFreepsCard("lindir");
		var aggressor = scn.GetShadowCard("aggressor");
		var warg = scn.GetShadowCard("warg");
		scn.MoveAlliesToTable(betsy, worrywort, lindir);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);
		scn.FreepsRefreshDecision();
		assertAttachedTo(warg, aggressor);

		// Nobody is assigned, in either assignment phase (the Warg makes its Orc fierce): no skirmish.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(betsy));
		assertEquals(0, scn.GetWoundsOn(worrywort));
		assertEquals(0, scn.GetWoundsOn(lindir));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(warg));

		scn.ShadowUseCardAction(warg);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		// Allies of two cultures and races may take the wound; Betsy may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worrywort, lindir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(betsy));
		scn.ShadowChooseCard(lindir);

		assertEquals(0, scn.GetWoundsOn(betsy));
		assertEquals(0, scn.GetWoundsOn(worrywort));
		assertEquals(1, scn.GetWoundsOn(lindir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipActionExertsBetsyToTakeAFreePeoplesEventIntoHandUntilSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betsy = scn.GetFreepsCard("betsy");
		var worrywort = scn.GetFreepsCard("worrywort");
		var fury = scn.GetFreepsCard("fury");
		var late = scn.GetFreepsCard("late");
		var rise = scn.GetFreepsCard("rise");
		var vilya = scn.GetFreepsCard("vilya");
		var advice = scn.GetShadowCard("advice");
		scn.MoveAlliesToTable(betsy, worrywort);
		scn.MoveCardsToDiscard(fury, late, rise, vilya, advice);

		scn.StartGame();
		assertInZone(Zone.DISCARD, fury, late, rise, vilya, advice);
		assertEquals(0, scn.GetWoundsOn(betsy));
		assertEquals(0, scn.GetWoundsOn(worrywort));

		assertTrue(scn.FreepsActionAvailable(betsy));
		scn.FreepsUseCardAction(betsy);
		// An exertion is no wound (CR 4.2 "exert"): she pays it.
		assertEquals(1, scn.GetWoundsOn(betsy));
		assertEquals(0, scn.GetWoundsOn(worrywort));

		// Free Peoples events of two cultures and phases; not a Shadow event in the same pile, a Free Peoples card
		// that is no event, nor the other player's Free Peoples event in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, late));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rise, vilya, advice));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(late);

		assertInZone(Zone.HAND, late);
		assertInZone(Zone.DISCARD, fury, rise, vilya, advice);

		// Vitality 2 with 1 wound: she cannot exert again, and Battle Fury is still in the pile.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(betsy));
	}

	@Test
	public void WithNoFreePeoplesEventInTheDiscardPileItIsStillOfferedAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betsy = scn.GetFreepsCard("betsy");
		var rise = scn.GetFreepsCard("rise");
		var vilya = scn.GetFreepsCard("vilya");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveAlliesToTable(betsy);
		scn.MoveCardsToDiscard(rise, vilya);

		scn.StartGame();
		assertInZone(Zone.DISCARD, rise, vilya);
		assertInZone(Zone.DECK, fury);
		assertEquals(0, scn.GetWoundsOn(betsy));

		// An action with a cost is offered whatever its effect can find (Q-0034).
		assertTrue(scn.FreepsActionAvailable(betsy));
		scn.FreepsUseCardAction(betsy);
		assertEquals(1, scn.GetWoundsOn(betsy));
		assertInZone(Zone.DISCARD, rise, vilya);
		assertInZone(Zone.DECK, fury);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
