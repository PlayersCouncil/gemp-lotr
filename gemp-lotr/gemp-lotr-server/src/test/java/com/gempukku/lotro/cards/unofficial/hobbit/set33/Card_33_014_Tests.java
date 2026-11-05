package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_014_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lindir", "33_14");
					// Elrond, Elven Lord: [elven] Elf ally, home 3H; a start-of-turn heal (wounds are laid after the
					// turn has begun) and a regroup ability only
					put("elrond", "30_21");
					// Thranduil, Elven King: [elven] Elf ally, home 5H; a maneuver ability only
					put("thranduil", "31_12");
					// Dain Ironfoot: a [dwarven] Dwarf ally; Thorin +1, and a regroup trigger that needs a [dwarven] follower
					put("dain", "32_2");
					// Legolas, Greenleaf: an [elven] Elf companion (the Hobbit sets print none); an archery ability only
					put("legolas", "1_50");
					// Wise Advice: [elven] fellowship event, "Spot Elrond to draw 4 cards ..."
					put("advice", "33_18");
					// The Evil Becomes Stronger: [elven] archery event
					put("evil", "31_11");
					// Lore of Imladris: a [dwarven] maneuver event, named by Lindir's text
					put("lore", "30_13");
					// Battle Fury: a [dwarven] skirmish event that is not Lore of Imladris
					put("fury", "30_3");
					// Vilya: an [elven] artifact, no event; "Bearer must be Elrond"
					put("vilya", "33_17");
					// Defiance: an [elven] skirmish event, laid in the other player's discard pile
					put("defiance", "1_37");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void LindirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Lindir, Elven Minstrel
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 4
		 * Home: 3H
		 * Game Text: Each time the fellowship moves, you may heal an [elven] ally.<br><b>Fellowship:</b> Exert Lindir twice to take an [elven] event (or Lore of Imladris) into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lindir");

		assertEquals("Lindir", card.getBlueprint().getTitle());
		assertEquals("Elven Minstrel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 3)));
	}

	@Test
	public void EachMoveOffersToHealAnElvenAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var elrond = scn.GetFreepsCard("elrond");
		var thranduil = scn.GetFreepsCard("thranduil");
		var dain = scn.GetFreepsCard("dain");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		scn.MoveAlliesToTable(lindir, elrond, thranduil, dain);
		scn.MoveCompanionsToTable(legolas);
		scn.AddWoundsToChar(elrond, 2);
		scn.AddWoundsToChar(thranduil, 1);
		scn.AddWoundsToChar(lindir, 1);
		scn.AddWoundsToChar(dain, 1);
		scn.AddWoundsToChar(legolas, 1);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(lindir));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(legolas));

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lindir));
		scn.FreepsAcceptOptionalTrigger();
		// [elven] allies of two homes, Lindir himself among them; not a [dwarven] ally nor an [elven] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lindir, elrond, thranduil));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elrond);

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(lindir));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheRegroupMoveOffersTheHealAgainOnTheSameAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var elrond = scn.GetFreepsCard("elrond");

		scn.StartGame();
		scn.MoveAlliesToTable(lindir, elrond);
		scn.AddWoundsToChar(elrond, 2);
		assertEquals(2, scn.GetWoundsOn(elrond));

		scn.FreepsPass();
		scn.FreepsAcceptOptionalTrigger();
		// Elrond is the one wounded [elven] ally: healed unasked.
		assertEquals(1, scn.GetWoundsOn(elrond));

		// The second move of the turn, from site 2 in the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lindir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheHealMayBeDeclinedAndStayingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var elrond = scn.GetFreepsCard("elrond");

		scn.StartGame();
		scn.MoveAlliesToTable(lindir, elrond);
		scn.AddWoundsToChar(elrond, 2);
		assertEquals(2, scn.GetWoundsOn(elrond));

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lindir));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(elrond));

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lindir));
		// The fellowship stays: the turn ends with nothing offered, and the other player's begins.
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lindir));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithNoElvenAllyWoundedTheMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var elrond = scn.GetFreepsCard("elrond");
		var dain = scn.GetFreepsCard("dain");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		scn.MoveAlliesToTable(lindir, elrond, dain);
		scn.MoveCompanionsToTable(legolas);
		scn.AddWoundsToChar(dain, 1);
		scn.AddWoundsToChar(legolas, 1);
		assertEquals(0, scn.GetWoundsOn(lindir));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(legolas));

		// Only a [dwarven] ally and an [elven] companion are wounded: a heal would do nothing, and nothing is offered.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lindir));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipActionExertsLindirTwiceToTakeAnElvenEventOrLoreOfImladrisIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var elrond = scn.GetFreepsCard("elrond");
		var advice = scn.GetFreepsCard("advice");
		var evil = scn.GetFreepsCard("evil");
		var lore = scn.GetFreepsCard("lore");
		var fury = scn.GetFreepsCard("fury");
		var vilya = scn.GetFreepsCard("vilya");
		var defiance = scn.GetShadowCard("defiance");
		scn.MoveAlliesToTable(lindir, elrond);
		scn.MoveCardsToDiscard(advice, evil, lore, fury, vilya, defiance);

		scn.StartGame();
		assertInZone(Zone.DISCARD, advice, evil, lore, fury, vilya, defiance);
		assertEquals(0, scn.GetWoundsOn(lindir));
		assertEquals(0, scn.GetWoundsOn(elrond));

		assertTrue(scn.FreepsActionAvailable(lindir));
		scn.FreepsUseCardAction(lindir);
		assertEquals(2, scn.GetWoundsOn(lindir));
		assertEquals(0, scn.GetWoundsOn(elrond));

		// [elven] events of two phases, and Lore of Imladris; not an [elven] artifact, another [dwarven] event, nor the
		// other player's [elven] event in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(advice, evil, lore));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(vilya, fury, defiance));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(lore);

		assertInZone(Zone.HAND, lore);
		assertInZone(Zone.DISCARD, advice, evil, fury, vilya, defiance);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWhileLindirCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var advice = scn.GetFreepsCard("advice");
		scn.MoveAlliesToTable(lindir);
		scn.MoveCardsToDiscard(advice);

		scn.StartGame();
		// Vitality 4 with 2 wounds: one exertion left, not two.
		scn.AddWoundsToChar(lindir, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(lindir));
		assertInZone(Zone.DISCARD, advice);
		assertFalse(scn.FreepsActionAvailable(lindir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RemoveWoundsFromChar(lindir, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(lindir));
		assertTrue(scn.FreepsActionAvailable(lindir));
	}

	@Test
	public void WithNoElvenEventInTheDiscardPileItIsStillOfferedAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindir = scn.GetFreepsCard("lindir");
		var fury = scn.GetFreepsCard("fury");
		var vilya = scn.GetFreepsCard("vilya");
		var advice = scn.GetFreepsCard("advice");
		scn.MoveAlliesToTable(lindir);
		scn.MoveCardsToDiscard(fury, vilya);

		scn.StartGame();
		assertInZone(Zone.DISCARD, fury, vilya);
		assertInZone(Zone.DECK, advice);
		assertEquals(0, scn.GetWoundsOn(lindir));

		// An action with a cost is offered whatever its effect can find (Q-0034).
		assertTrue(scn.FreepsActionAvailable(lindir));
		scn.FreepsUseCardAction(lindir);
		assertEquals(2, scn.GetWoundsOn(lindir));
		assertInZone(Zone.DISCARD, fury, vilya);
		assertInZone(Zone.DECK, advice);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
