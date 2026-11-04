package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "18_57");
					put("shield2", "18_57");

					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; Knight is its whole text
					// Gondorian Merchant: the [gondor] Man ally; his maneuver ability is used only on purpose
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only
					// on purpose
					put("pippin", "103_31");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is its whole text

					put("marksman1", "1_176");  // Goblin Marksman: [moria] Orc archer, vitality 1; Archer is its whole text
					put("marksman2", "1_176");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text

					// The Shadow player's hand and draw deck are made of these: Goblin Patrol Troop, a minion with no text.
					put("s1", "1_177");
					put("s2", "1_177");
					put("s3", "1_177");
					put("s4", "1_177");
					put("s5", "1_177");
					put("s6", "1_177");
					put("s7", "1_177");
					put("s8", "1_177");
					put("s9", "1_177");
					put("s10", "1_177");
					put("s11", "1_177");
					put("s12", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShieldoftheWhiteTreeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Shield of the White Tree
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Shield
		 * Game Text: Bearer must be a [gondor] Man.<br>The minion archery total is -1.<br><b>Regroup:</b> Discard this possession from play to make each opponent shuffle his or her hand into his or her draw deck and draw 8 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shield");

		assertEquals("Shield of the White Tree", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAGondorManCompanionOrAllyAndNotOnAGondorHobbitOrARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var shield = scn.GetFreepsCard("shield");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(shield);

		scn.StartGame();

		assertInHand(shield);
		assertTrue(scn.FreepsPlayAvailable(shield));
		scn.FreepsPlayCard(shield);

		// A [gondor] Man companion and the [gondor] Man ally; not a [gondor] Hobbit, a [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(shield, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileItIsBorneTheMinionArcheryTotalIsOneLessAndTheFellowshipsIsUnchanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(shield);

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.FreepsRefreshDecision();

		// Two archers: a minion archery total of 2; no Free Peoples archer.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(shield);
		assertAttachedTo(shield, knight);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// And so it is in the archery phase.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsItToMakeTheShadowPlayerShuffleHisHandIntoHisDeckAndDrawEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var shield2 = scn.GetFreepsCard("shield2");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(knight, shield);
		scn.AttachCardsTo(merchant, shield2);
		assertAttachedTo(shield, knight);
		assertAttachedTo(shield2, merchant);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// A Shadow hand of 3 and a Shadow draw deck of 9.
		scn.SetShadowHand(scn.GetShadowCard("s1"), scn.GetShadowCard("s2"), scn.GetShadowCard("s3"));
		scn.SetShadowDrawDeck(scn.GetShadowCard("s4"), scn.GetShadowCard("s5"), scn.GetShadowCard("s6"),
				scn.GetShadowCard("s7"), scn.GetShadowCard("s8"), scn.GetShadowCard("s9"),
				scn.GetShadowCard("s10"), scn.GetShadowCard("s11"), scn.GetShadowCard("s12"));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(9, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsHand = scn.GetFreepsHandCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsActionAvailable(shield));
		scn.FreepsUseCardAction(shield);

		// The cost: the Shield used is discarded from play; the second copy stays on its bearer.
		assertInDiscard(shield);
		assertAttachedTo(shield2, merchant);
		// The Shadow player's 3 cards went into his deck, which was shuffled once, and he drew 8 of the 12:
		// 8, not 11 (no shuffle in) and not 3 (no draw); nothing went to his discard pile.
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());

		// The Free Peoples player's own hand and deck are left alone: "each opponent".
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, shield);
		assertAttachedTo(shield, knight);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shield));

		// The same turn's regroup phase offers it.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(shield));
	}
}
