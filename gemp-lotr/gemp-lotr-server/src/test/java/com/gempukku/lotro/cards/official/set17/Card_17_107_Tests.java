package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_107_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "17_107");

					// Huorn: [gandalf] Tree companion, strength 6, "strength +1 for each Ent you can spot". He is no Ent.
					put("huorn", "9_25");
					// Ent Avenger: [gandalf] Ent companion, strength 6, "While you can spot 3 Ents, this companion is
					// strength +2". Not unique: two copies.
					put("avenger1", "6_27");
					put("avenger2", "6_27");
					// Urgency: [isengard] support-area condition of the Shadow player's, "Each time the fellowship
					// moves, add (2) for each Ent you can spot": an Ent count that is not Merry's player's.
					put("urgency", "19_17");

					// Pipeweeds of two cultures and two types.
					put("toby", "1_305");       // Old Toby: [shire] pipeweed possession
					put("ship", "18_27");       // Ship of Smoke: [gandalf] pipeweed condition
					put("pipe", "1_285");       // Bilbo's Pipe: a [shire] pipe, which is no pipeweed
					put("leaf", "17_106");      // Halfling Leaf: [shire] pipeweed condition, laid in the other player's discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Merry, In the Bloom of Health
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: Add 1 to the number of Ents you can spot.<br><b>Regroup:</b> Exert Merry to place a pipeweed from your discard pile beneath your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("In the Bloom of Health", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayingMerryAddsOneToEachCountOfEntsHisPlayerMakes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var huorn = scn.GetFreepsCard("huorn");
		var avenger1 = scn.GetFreepsCard("avenger1");
		var avenger2 = scn.GetFreepsCard("avenger2");
		scn.MoveCardsToHand(merry);
		scn.MoveCompanionsToTable(huorn, avenger1, avenger2);

		scn.StartGame();

		// Two Ents: Huorn 6 + 2; two Ents are short of the Avengers' 3.
		assertEquals(8, scn.GetStrength(huorn));
		assertEquals(6, scn.GetStrength(avenger1));
		assertEquals(6, scn.GetStrength(avenger2));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// Two Ents and Merry's 1: Huorn 6 + 3, and the Avengers can spot 3 Ents.
		assertEquals(9, scn.GetStrength(huorn));
		assertEquals(8, scn.GetStrength(avenger1));
		assertEquals(8, scn.GetStrength(avenger2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoEntInPlayMerryMakesTheCountOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCardsToHand(merry);
		scn.MoveCompanionsToTable(huorn);

		scn.StartGame();

		// No Ent: Huorn is his printed 6.
		assertEquals(6, scn.GetStrength(huorn));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// 0 Ents + 1
		assertEquals(7, scn.GetStrength(huorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayersCountOfEntsIsNotRaised() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var avenger = scn.GetFreepsCard("avenger1");
		var urgency = scn.GetShadowCard("urgency");
		scn.MoveCompanionsToTable(merry, avenger);
		scn.MoveCardsToSupportArea(urgency);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 2 for Ettenmoors + 3 companions + 2 for the one Ent the Shadow player can spot. Merry's 1 counted for
		// him would make it 4 for Urgency, 9 in all.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionExertsMerryToPlaceAPipeweedFromHisDiscardPileBeneathHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var toby = scn.GetFreepsCard("toby");
		var ship = scn.GetFreepsCard("ship");
		var pipe = scn.GetFreepsCard("pipe");
		var leaf = scn.GetShadowCard("leaf");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveCardsToDiscard(toby, ship, pipe, leaf);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertNotSame(ship, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DISCARD, toby, ship, pipe, leaf);
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		// A pipeweed of either culture and either type; neither the pipe nor the other player's Halfling Leaf.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, ship));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, leaf));

		scn.FreepsChooseCard(ship);
		assertSame(ship, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, ship);
		assertInZone(Zone.DISCARD, toby, pipe, leaf);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoPipeweedInHisDiscardPileTheActionIsOfferedAndMerryExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveCardsToDiscard(pipe);

		scn.SkipToPhase(Phase.REGROUP);
		var bottom = scn.GetFreepsBottomOfDeck();
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInZone(Zone.DISCARD, pipe);
		assertSame(bottom, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhileMerryIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveCardsToDiscard(toby);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(merry, 3);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.IsExhausted(merry));
		assertInZone(Zone.DISCARD, toby);
		assertFalse(scn.FreepsActionAvailable(merry));
	}
}
