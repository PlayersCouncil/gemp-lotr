package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_011_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The archery phase and
	// skirmishes of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slingshot", "33_11");
					put("ori", "30_18");        // Ori, Dwarven Chronicler: the one Ori; a [dwarven] Dwarf follower, cost 1
					put("bifur", "30_5");       // Bifur, Inarticulate: another [dwarven] Dwarf follower; a fierce-win trigger not reached
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					// Dwarven Axe: a [dwarven] hand weapon, a possession played after the Slingshot; damage +1 only at a mountain
					put("axe", "31_1");

					put("runner", "31_30");     // Goblin Runner: [moria] Orc, 5/1; a "when you play" text, never reached
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, 7/2, fierce; a skirmish ability used only on purpose
					put("goblin", "32_27");     // Assaulting Goblin: [moria] Orc, 7/2; a response only at a battleground
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrisSlingshotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Ori's Slingshot
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: When you play this possession, you may play Ori from your draw deck.
		 * 	<b>Archery:</b> Discard Ori to make a minion strength -3 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slingshot");

		assertEquals("Ori's Slingshot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayingItMayPlayOriFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slingshot = scn.GetFreepsCard("slingshot");
		var ori = scn.GetFreepsCard("ori");
		var bifur = scn.GetFreepsCard("bifur");

		scn.StartGame();
		scn.MoveCardsToHand(slingshot);
		scn.SetFreepsDrawDeck(ori, bifur);
		scn.FreepsRefreshDecision();
		assertInHand(slingshot);
		assertInZone(Zone.DECK, ori, bifur);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(slingshot);
		assertInZone(Zone.SUPPORT, slingshot);
		assertTrue(scn.FreepsDecisionAvailable("play Ori"));
		scn.FreepsChooseYes();

		scn.FreepsDismissRevealedCards();
		// Ori, and not Bifur, another [dwarven] follower. A play from a draw deck is 0 to 1 (Q-0019).
		assertTrue(scn.FreepsHasCardChoiceAvailable(ori));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bifur));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(ori);

		assertInZone(Zone.SUPPORT, ori);
		assertInZone(Zone.DECK, bifur);
		// Shuffled as Ori leaves the deck that was looked through, and again once the look is over.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningLeavesOriInTheDeckAndAnotherPossessionPlayedAfterOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slingshot = scn.GetFreepsCard("slingshot");
		var ori = scn.GetFreepsCard("ori");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveCardsToHand(slingshot, axe);
		scn.SetFreepsDrawDeck(ori);
		scn.FreepsRefreshDecision();
		assertInHand(slingshot, axe);
		assertInZone(Zone.DECK, ori);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(slingshot);
		assertTrue(scn.FreepsDecisionAvailable("play Ori"));
		scn.FreepsChooseNo();

		assertInZone(Zone.DECK, ori);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another possession played once the Slingshot is in play: nothing is asked.
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, balin);
		assertFalse(scn.FreepsDecisionAvailable("play Ori"));
		assertInZone(Zone.DECK, ori);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoOriInTheDrawDeckPlayingItAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slingshot = scn.GetFreepsCard("slingshot");
		var ori = scn.GetFreepsCard("ori");
		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveCardsToHand(slingshot);
		scn.MoveCardsToDiscard(ori);
		scn.SetFreepsDrawDeck(bifur);
		scn.FreepsRefreshDecision();
		assertInHand(slingshot);
		assertInDiscard(ori);
		assertInZone(Zone.DECK, bifur);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(slingshot);
		// Ori in the discard pile and another [dwarven] follower in the deck: nothing to play.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// the question is not put, and the deck is not looked through.
		assertInZone(Zone.SUPPORT, slingshot);
		assertFalse(scn.FreepsDecisionAvailable("play Ori"));
		assertInZone(Zone.DECK, bifur);
		assertInDiscard(ori);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryActionDiscardsOriBorneByACompanionToMakeAMinionStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var slingshot = scn.GetFreepsCard("slingshot");
		var ori = scn.GetFreepsCard("ori");
		var balin = scn.GetFreepsCard("balin");
		var runner = scn.GetShadowCard("runner");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(slingshot);
		scn.AttachCardsTo(balin, ori);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, spider);
		assertInZone(Zone.SUPPORT, slingshot);
		assertAttachedTo(ori, balin);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.FreepsActionAvailable(slingshot));
		scn.FreepsUseCardAction(slingshot);
		assertInDiscard(ori);

		// Minions of two cultures and races; not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, spider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(balin, bilbo));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(spider));
		scn.FreepsChooseCard(spider);

		assertEquals(4, scn.GetStrength(spider));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(balin));
		assertInZone(Zone.SUPPORT, slingshot);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void OriInTheSupportAreaPaysAndTheMinusThreeLastsUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var slingshot = scn.GetFreepsCard("slingshot");
		var ori = scn.GetFreepsCard("ori");
		var balin = scn.GetFreepsCard("balin");
		var runner = scn.GetShadowCard("runner");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(slingshot, ori);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, goblin);
		assertInZone(Zone.SUPPORT, slingshot, ori);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsUseCardAction(slingshot);
		assertInDiscard(ori);
		scn.FreepsChooseCard(goblin);
		assertEquals(4, scn.GetStrength(goblin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, runner}, new PhysicalCardImpl[]{bilbo, goblin});

		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		assertInDiscard(runner);

		// In the skirmish after one already resolved, the Goblin is still 7 - 3; Bilbo's 3 loses to it and he lives.
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(4, scn.GetStrength(goblin));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bilbo));

		// At the regroup phase it is gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(goblin));
	}

	@Test
	public void ArcheryActionIsNotOfferedWithOriOutOfPlayThoughAnotherFollowerIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slingshot = scn.GetFreepsCard("slingshot");
		var ori = scn.GetFreepsCard("ori");
		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(slingshot, bifur);

		scn.StartGame();
		scn.MoveCardsToHand(ori);
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.SUPPORT, slingshot, bifur);
		assertInHand(ori);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(slingshot));
		assertInZone(Zone.SUPPORT, bifur);
		assertInHand(ori);
		assertEquals(5, scn.GetStrength(runner));
	}
}
