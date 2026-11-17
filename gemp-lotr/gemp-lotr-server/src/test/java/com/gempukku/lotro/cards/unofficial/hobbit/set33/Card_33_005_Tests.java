package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_005_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Turn 1 begins at Bag
	// End (site 1); its regroup phase is at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pot", "33_5");

					// The three [dwarven] followers it names, and one it does not; each plays to the support area
					put("bifur", "30_5");           // Bifur, Inarticulate
					put("bofur", "30_6");           // Bofur, Encouraging Fellow
					put("bombur", "30_7");          // Bombur, Chief Cook
					put("dori", "30_8");            // Dori, Really a decent fellow

					// [dwarven] events of twilight cost 1, of two phases
					put("fury", "30_3");            // Battle Fury: skirmish
					put("king", "32_6");            // King Under the Mountain: fellowship
					// Cards it may not take
					put("azanulbizar", "30_4");     // Battle of Azanulbizar: a [dwarven] event of cost 0
					put("ancestral", "30_1");       // Ancestral Knowledge: a [dwarven] event of cost 2
					put("axe", "31_1");             // Dwarven Axe: a [dwarven] possession of cost 1
					put("wizard", "30_23");         // A Wizard Is Never Late: a [gandalf] event of cost 1
					put("lore", "30_13");           // Lore of Imladris: a [dwarven] event of cost 1, only ever in the opponent's discard pile

					put("run", "18_2");             // Run Until Found: a [dwarven] support-area condition that holds a [dwarven] token
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DwarvenCookingPotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Dwarven Cooking Pot
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Each time you play Bifur, Bofur or Bombur, you may place a [dwarven] token here.
		 * 	<b>Fellowship or Regroup:</b> Remove a [dwarven] token from here to take a [dwarven] event with a twilight cost of 1 into hand from your discard pile (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pot");

		assertEquals("Dwarven Cooking Pot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeBifurOrBofurIsPlayedADwarvenTokenMayBePlacedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pot = scn.GetFreepsCard("pot");
		var bifur = scn.GetFreepsCard("bifur");
		var bofur = scn.GetFreepsCard("bofur");
		scn.MoveCardsToSupportArea(pot);
		scn.MoveCardsToHand(bifur, bofur);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(pot));

		scn.FreepsPlayCard(bifur);
		assertInZone(Zone.SUPPORT, bifur);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pot));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(pot));

		scn.FreepsPlayCard(bofur);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pot));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(pot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingBomburOffersATokenThatMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pot = scn.GetFreepsCard("pot");
		var bombur = scn.GetFreepsCard("bombur");
		scn.MoveCardsToSupportArea(pot);
		scn.MoveCardsToHand(bombur);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(pot));

		scn.FreepsPlayCard(bombur);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pot));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, bombur);
		assertEquals(0, scn.GetCultureTokensOn(pot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingThePotItselfOrAnotherDwarvenFollowerOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pot = scn.GetFreepsCard("pot");
		var dori = scn.GetFreepsCard("dori");
		scn.MoveCardsToHand(pot, dori);

		scn.StartGame();

		assertInZone(Zone.HAND, pot, dori);

		// The Pot's own play is a [dwarven] card played that is none of the three.
		scn.FreepsPlayCard(pot);
		assertInZone(Zone.SUPPORT, pot);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(pot));
		assertEquals(0, scn.GetCultureTokensOn(pot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(dori);

		assertInZone(Zone.SUPPORT, dori);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(pot));
		assertEquals(0, scn.GetCultureTokensOn(pot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovesATokenToTakeADwarvenEventOfCostOneOncePerPhaseInTheFellowshipAndAgainInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pot = scn.GetFreepsCard("pot");
		var fury = scn.GetFreepsCard("fury");
		var king = scn.GetFreepsCard("king");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var ancestral = scn.GetFreepsCard("ancestral");
		var axe = scn.GetFreepsCard("axe");
		var wizard = scn.GetFreepsCard("wizard");
		var shadowLore = scn.GetShadowCard("lore");
		scn.MoveCardsToSupportArea(pot);
		scn.MoveCardsToDiscard(fury, king, azanulbizar, ancestral, axe, wizard, shadowLore);

		scn.StartGame();
		scn.AddTokensToCard(pot, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetCultureTokensOn(pot));
		assertInDiscard(fury, king, azanulbizar, ancestral, axe, wizard, shadowLore);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(pot));

		scn.FreepsUseCardAction(pot);
		assertEquals(1, scn.GetCultureTokensOn(pot));
		// Of either phase; not a [dwarven] event of another cost, a [dwarven] possession of cost 1, an event of cost 1
		// of another culture, or a card in the opponent's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, king));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(azanulbizar, ancestral, axe, wizard, shadowLore));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(fury);

		assertInHand(fury);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(king, azanulbizar, ancestral, axe, wizard, shadowLore);
		// Once per phase: a token and a [dwarven] event of cost 1 are still there.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pot));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(pot));
		assertTrue(scn.FreepsActionAvailable(pot));

		scn.FreepsUseCardAction(pot);
		// King Under the Mountain is the only card the choice can take, so it is taken unasked.
		assertEquals(0, scn.GetCultureTokensOn(pot));
		assertInHand(fury, king);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(azanulbizar, ancestral, axe, wizard, shadowLore);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoTokenHereThoughAnotherDwarvenCardHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pot = scn.GetFreepsCard("pot");
		var run = scn.GetFreepsCard("run");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCardsToSupportArea(pot, run);
		scn.MoveCardsToDiscard(fury);

		scn.StartGame();
		scn.AddTokensToCard(run, 1);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetCultureTokensOn(pot));
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertInDiscard(fury);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pot));

		assertEquals(1, scn.GetCultureTokensOn(run));
		assertInDiscard(fury);
	}

	@Test
	public void WithNoDwarvenEventOfCostOneInTheDiscardPileTheTokenIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pot = scn.GetFreepsCard("pot");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var ancestral = scn.GetFreepsCard("ancestral");
		var axe = scn.GetFreepsCard("axe");
		var wizard = scn.GetFreepsCard("wizard");
		scn.MoveCardsToSupportArea(pot);
		scn.MoveCardsToDiscard(azanulbizar, ancestral, axe, wizard);

		scn.StartGame();
		scn.AddTokensToCard(pot, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetCultureTokensOn(pot));
		assertInDiscard(azanulbizar, ancestral, axe, wizard);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(pot));

		scn.FreepsUseCardAction(pot);

		assertEquals(0, scn.GetCultureTokensOn(pot));
		assertInDiscard(azanulbizar, ancestral, axe, wizard);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
