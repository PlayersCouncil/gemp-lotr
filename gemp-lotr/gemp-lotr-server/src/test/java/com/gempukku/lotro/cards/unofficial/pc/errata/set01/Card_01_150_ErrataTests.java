package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_150_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rager1", "51_150");
					put("rager2", "51_150");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, strength 3, vitality 4
					put("guard1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("guard2", "1_7");
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an ally, so a character who is no companion; her text needs your site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCourteousHalflingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rager1", "51_150");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("guard1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("guard2", "1_7");
					put("sam", "1_311");        // these three stay in the draw deck, so that three cards could be discarded from it
					put("silinde", "1_60");
					put("guard3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",                        // Frodo, Courteous Halfling: with 3 unbound companions spotted, Shadow cards may not discard cards from the top of your draw deck
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
		 * Game Text: <b>Damage +1</b>.
		 * Each time this minion wins a skirmish, the Free Peoples player must exert a companion or discard the top 3 cards of their draw deck.
		 */

		var scn = GetScenario();

		var rager = scn.GetFreepsCard("rager1");

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
	public void WinningASkirmishTheFreePeoplesPlayerMayChooseToExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();
		scn.MoveMinionsToTable(rager);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();

		// The Rager, 9, beats Aragorn, 8: 2 wounds with damage +1, then the trigger.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		assertFalse(scn.ShadowChoiceAvailable("Exert"));

		scn.FreepsChooseOption("Exert");
		// Any companion, in the skirmish or not, the Ring-bearer among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(silinde));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningASkirmishTheFreePeoplesPlayerMayChooseToDiscardTheTopThreeCardsOfTheirDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("guard1");
		var top3 = scn.GetFreepsCard("guard2");
		var top4 = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(rager);
		scn.MoveCardsToTopOfDeck(top4, top3, top2, top1);
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsChooseOption("Discard");
		assertInZone(Zone.DISCARD, top1, top2, top3);
		assertEquals(top4, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(discard + 3, scn.GetFreepsDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoCompanionAbleToExertTheTopThreeCardsAreDiscardedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("guard1");
		var top3 = scn.GetFreepsCard("guard2");
		var top4 = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn);
		// Vitality 4 each: Frodo exhausted now, Aragorn once the skirmish's 2 wounds are placed.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(rager);
		scn.MoveCardsToTopOfDeck(top4, top3, top2, top1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsChoiceAvailable("Exert"));
		assertInZone(Zone.DISCARD, top1, top2, top3);
		assertEquals(top4, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoCardsInTheDrawDeckACompanionIsExertedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager = scn.GetShadowCard("rager1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(aragorn, guard1);

		scn.StartGame();
		scn.MoveMinionsToTable(rager);
		// Leave Guard and Silinde in the draw deck.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("rager1"), scn.GetFreepsCard("rager2"), scn.GetFreepsCard("sam"));
		assertEquals(2, scn.GetFreepsDeckCount());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.FreepsResolveSkirmish(aragorn);

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsChoiceAvailable("Discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard1, frodo));

		scn.FreepsChooseCard(guard1);
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhereShadowCardsMayNotDiscardFromTheTopOfTheDrawDeckTheDiscardIsStillOfferedAndTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourteousHalflingScenario();

		var rager = scn.GetShadowCard("rager1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("silinde");
		var top3 = scn.GetFreepsCard("guard3");
		// Frodo is Ring-bound; Aragorn and the two Guards are three unbound companions.
		scn.MoveCompanionsToTable(aragorn, guard1, guard2);

		scn.StartGame();
		scn.MoveMinionsToTable(rager);
		scn.MoveCardsToTopOfDeck(top3, top2, top1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		// The Rager, Sam, Silinde and the third Guard: more than the three cards the Rager would discard.
		assertEquals(4, deck);

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// Frodo stops Shadow cards that make the Free Peoples player discard, but the Rager gives him a choice
		// and he may choose to discard (F-0205: not a fault): the discard is offered beside the exertion, and taken.
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));

		scn.FreepsChooseOption("Discard");
		assertInZone(Zone.DISCARD, top1, top2, top3);
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(discard + 3, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinOfThisMinionAndNoOtherSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rager1 = scn.GetShadowCard("rager1");
		var rager2 = scn.GetShadowCard("rager2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(aragorn, guard1, guard2);

		scn.StartGame();
		scn.MoveMinionsToTable(rager1, rager2);
		// Aragorn at 10 beats a Rager; the first Rager is made fierce, to win a second skirmish in the turn.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, rager1, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rager2}, new PhysicalCardImpl[]{guard1, rager1});
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();

		// The second Rager loses: nothing to answer, and the next skirmish is chosen.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(rager2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(deck, scn.GetFreepsDeckCount());

		// The first Rager, 9, overwhelms the Guard, 4.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, guard1);
		scn.FreepsChooseOption("Exert");
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(deck, scn.GetFreepsDeckCount());

		// And wins again, in the fierce skirmish.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard2, rager1);
		scn.FreepsResolveSkirmish(guard2);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, guard2);
		scn.FreepsChooseOption("Discard");
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
