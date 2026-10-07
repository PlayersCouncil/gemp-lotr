package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_043_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eyes", "51_43");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, cost 2
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, cost 2
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, cost 1
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4
					put("axe", "1_3");          // Axe Strike: a Free Peoples event
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarseeingEyesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Far-seeing Eyes
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you play an Elf, draw a card.  Any Shadow player may discard a card from hand
		 * to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eyes");

		assertEquals("Far-seeing Eyes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachElfPlayedDrawsACardWhileTheShadowPlayerHoldsNoCard() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Thrarin is made an Elf wherever he is: an Elf who is a [dwarven] card. The modifier goes by his
		// name because he is still to be played.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateFreepsFilter("name(Thrarin)"), null, Race.ELF));
		scn.MoveCardsToHand(eyes, arwen, orophin, thrarin);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());

		// The condition itself is no Elf: nothing is drawn for it.
		scn.FreepsPlayCard(eyes);
		assertInZone(Zone.SUPPORT, eyes);
		assertEquals(2, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());

		// An Elf companion: one card out of the hand, one drawn.
		scn.FreepsPlayCard(arwen);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An Elf ally
		scn.FreepsPlayCard(orophin);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		// An Elf of another culture
		scn.FreepsPlayCard(thrarin);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDiscardACardOfHisChoiceFromHandToPreventTheDraw() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var axe = scn.GetFreepsCard("axe");
		// The Shadow player's hand: two Shadow cards and a Free Peoples card.
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var shadowThrarin = scn.GetShadowCard("thrarin");
		scn.MoveCardsToSupportArea(eyes);
		// A Man of the [elven] culture is still no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, scn.GenerateFreepsFilter("name(Aragorn)"), null, Culture.ELVEN));
		scn.MoveCardsToHand(aragorn, arwen, orophin, axe);
		scn.MoveCardsToHand(runner, troop, shadowThrarin);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// No Elf played: nothing to draw and nothing to prevent.
		scn.FreepsPlayCard(aragorn);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());

		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop, shadowThrarin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(shadowThrarin);
		assertInZone(Zone.DISCARD, shadowThrarin);
		assertInZone(Zone.HAND, runner);
		assertInZone(Zone.HAND, troop);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		// Prevented: Arwen left the hand and nothing came into it.
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, axe);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The next Elf: he is asked again, and lets it be.
		scn.FreepsPlayCard(orophin);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));

		scn.ShadowChooseNo();
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		// Orophin out, one card drawn
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HisLastCardMayBeDiscardedToPreventAndWithAnEmptyHandHeIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(eyes);
		scn.MoveCardsToHand(arwen, orophin);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.ShadowDecisionAvailable("Discard a card from hand to prevent"));

		// His only card: it is discarded without his being asked which.
		scn.ShadowChooseYes();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With no card left he cannot pay: he is not asked, and the card is drawn.
		scn.FreepsPlayCard(orophin);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("Q-0114: with an empty draw deck the Shadow player is still asked to discard a card to prevent a draw that cannot happen")
	public void WithNoCardToDrawTheShadowPlayerIsNotAskedToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(eyes);
		scn.MoveCardsToHand(arwen);
		// Everything else of the Free Peoples player's deck is put in his discard pile.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("orophin"), scn.GetFreepsCard("thrarin"), scn.GetFreepsCard("aragorn"),
				scn.GetFreepsCard("axe"), scn.GetFreepsCard("runner"), scn.GetFreepsCard("troop"));
		scn.MoveCardsToHand(runner, troop);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsPlayCard(arwen);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
