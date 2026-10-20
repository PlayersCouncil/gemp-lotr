package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nobody", "1_23");

					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, home site 3, strength 3
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
					put("soldier", "1_271");      // Orc Soldier: strength 7, vitality 2; a skirmish ability used only on purpose
					put("troop", "1_177");        // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A table whose Shadow draw deck is two cards once both minions are down: the Shadow player's own copies
	// of the event and of Gimli.
	protected VirtualTableScenario GetTwoCardDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nobody", "1_23");
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, damage +1
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
					put("troop", "1_177");        // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NobodyTossesaDwarfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Nobody Tosses a Dwarf
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: Response: If a Dwarf wins a skirmish, make an opponent discard 3 cards from the top of his or her draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nobody");

		assertEquals("Nobody Tosses a Dwarf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfWinningASkirmishLetsTheTopThreeCardsOfTheOpponentsDeckBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var top1 = scn.GetShadowCard("troop");
		var top2 = scn.GetShadowCard("aragorn");
		var top3 = scn.GetShadowCard("grimir");
		var top4 = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(nobody);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// The Shadow player's deck, from the top: a Goblin Patrol Troop, his copies of Aragorn and Grimir, and
		// fourth an Orc Soldier.
		scn.MoveCardsToTopOfDeck(top4, top3, top2, top1);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(nobody));

		// 6 against 5: Gimli wins, and the Savage survives his two wounds.
		scn.FreepsPass();
		scn.ShadowPass();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nobody));

		var shadowDeck = scn.GetShadowDeckCount();
		var freepsDeck = scn.GetFreepsDeckCount();
		var freepsTop = scn.GetFreepsTopOfDeck();
		assertInZone(Zone.DECK, top1, top2, top3, top4);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsChooseOptionalTrigger(nobody);
		assertInZone(Zone.DISCARD, top1, top2, top3);
		assertSame(top4, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck - 3, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		// The player's own deck is not the one, and his discard pile has the event alone.
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertSame(freepsTop, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DISCARD, nobody);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionWhoIsNoDwarfWinsOrWhenADwarfLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(nobody);
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, soldier});
		var shadowDeck = scn.GetShadowDeckCount();

		// 8 against 5: Aragorn, a Man, wins beside a Dwarf.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(nobody));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 6 against 7: Gimli loses.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(nobody));
		assertInZone(Zone.HAND, nobody);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyWinningASkirmishAtHisHomeSiteIsADwarfWinning() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		var top1 = scn.GetShadowCard("troop");
		var top2 = scn.GetShadowCard("aragorn");
		var top3 = scn.GetShadowCard("gimli");
		var top4 = scn.GetShadowCard("soldier");
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(nobody);
		// 3 + 3 against the Savage's 5
		scn.ApplyAdHocModifier(new StrengthModifier(null, grimir, null, 3));
		scn.MoveMinionsToTable(savage);
		// The turns played on the way have drawn the Shadow player's small deck: four of his cards go back on
		// it, a Goblin Patrol Troop on top and an Orc Soldier fourth.
		scn.MoveCardsToTopOfDeck(top4, top3, top2, top1);

		// The fellowship moves to site 3, Grimir's home.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(grimir, savage);
		scn.FreepsPass();
		scn.ShadowPass();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nobody));

		var shadowDeck = scn.GetShadowDeckCount();
		assertInZone(Zone.DECK, top1, top2, top3, top4);
		scn.FreepsChooseOptionalTrigger(nobody);
		assertInZone(Zone.DISCARD, top1, top2, top3);
		assertSame(top4, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck - 3, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, nobody);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoCardsInTheOpponentsDeckBothAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoCardDeckScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var shadowNobody = scn.GetShadowCard("nobody");
		var shadowGimli = scn.GetShadowCard("gimli");
		scn.MoveCardsToHand(nobody);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		assertEquals(2, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, shadowNobody, shadowGimli);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);
		scn.FreepsPass();
		scn.ShadowPass();

		// An event is played whatever its effect will find to do, and does as much of it as can be done.
		var freepsDeck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nobody));
		scn.FreepsChooseOptionalTrigger(nobody);

		assertInZone(Zone.DISCARD, shadowNobody, shadowGimli);
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, nobody);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
