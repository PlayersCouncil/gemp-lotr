package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_150_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rager", "1_150");
					put("rager2", "1_150");     // a second copy: not unique, and "this minion" is the one that wins
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a card for the draw deck
					put("guard1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("guard2", "1_7");
					put("hunt", "1_126");       // Hunt Them Down!: Maneuver event, "Make an Uruk-hai fierce until the regroup phase"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukRagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Rager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time this minion wins a skirmish, the Free Peoples player must discard the top 2 cards of his or her draw deck.
		*/

		var scn = GetScenario();

		var rager = scn.GetFreepsCard("rager");

		assertEquals("Uruk Rager", rager.getBlueprint().getTitle());
		assertNull(rager.getBlueprint().getSubtitle());
		assertFalse(rager.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, rager.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, rager.getBlueprint().getCulture());
		assertEquals(CardType.MINION, rager.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, rager.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(rager, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(rager, Keyword.DAMAGE));
		assertEquals(4, rager.getBlueprint().getTwilightCost());
		assertEquals(9, rager.getBlueprint().getStrength());
		assertEquals(2, rager.getBlueprint().getVitality());
		assertEquals(5, rager.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishTheFreePeoplesPlayerDiscardsTheTopTwoCardsOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("guard1");
		var top3 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(aragorn);

		var rager = scn.GetShadowCard("rager");
		var rager2 = scn.GetShadowCard("rager2");

		scn.StartGame();
		scn.MoveMinionsToTable(rager, rager2);
		scn.MoveCardsToTopOfDeck(top3, top2, top1);
		assertInZone(Zone.DECK, top1, top2, top3);
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		// The other copy is left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();

		// The Rager, 9, beats Aragorn, 8: 2 wounds with damage +1, and the top two cards go, with nothing asked.
		scn.PassCurrentPhaseActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, top1, top2);
		assertEquals(top3, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());
		// Not the Shadow player's deck.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinOfThisMinionAndNoOtherSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(aragorn, guard1, guard2);

		var rager = scn.GetShadowCard("rager");
		var rager2 = scn.GetShadowCard("rager2");
		var hunt = scn.GetShadowCard("hunt");
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(rager, rager2);
		scn.SetTwilight(10);
		// Aragorn at 10 beats a Rager, which lives through the one wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The first Rager is made fierce, to win a second skirmish in the turn.
		scn.ShadowPlayCard(hunt);
		scn.ShadowChooseCard(rager);
		assertTrue(scn.HasKeyword(rager, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(rager2, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rager2}, new PhysicalCardImpl[]{guard1, rager});
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();

		// The second Rager loses: nothing is discarded, and the next skirmish is chosen.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(rager2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The first Rager, 9, overwhelms the Guard, 4: two cards.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DEAD, guard1);
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());

		// And wins again, in the fierce skirmish: two more.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard2, rager);
		scn.FreepsResolveSkirmish(guard2);
		scn.PassFierceSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DEAD, guard2);
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertEquals(discard + 4, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
