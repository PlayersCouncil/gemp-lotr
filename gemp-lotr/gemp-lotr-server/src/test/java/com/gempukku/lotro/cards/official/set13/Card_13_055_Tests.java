package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "13_55");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only

					put("deagol1", "13_46");    // Déagol, Fateful Finder: [gollum] follower, resistance 1; Skirmish: if bearer is
					put("deagol2", "13_46");    // not assigned, discard this from play to play an artifact or possession from the draw deck on him
					put("birthday", "13_51");   // It's My Birthday: [gollum] Shadow event that names Déagol in its text

					// For the draw deck: [gollum] cards of three kinds and both sides, and a card of another culture.
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, a regroup ability only
					put("larder", "8_23");      // Larder: [gollum] Shadow possession, support area
					put("fish", "15_46");       // Nice Fish: [gollum] Free Peoples condition, support area

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its "when played" text is not reached by a cheat
					put("pursuer", "4_190");    // Uruk Pursuer: fierce, strength 5, vitality 3; its texts are about unbound Hobbits
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Sméagol, Simple Stoor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: 
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Game Text: <b>Ring-bound</b>. To play, add a burden.<br>For each card titled Déagol in your discard pile, Sméagol is strength +2 and resistance -1.<br>Each time Sméagol wins a skirmish, you may take a [gollum] card from your draw deck and place it in your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Simple Stoor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayingHimFromHandAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();
		assertInHand(smeagol);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachDeagolInHisOwnersDiscardPileMakesHimStrengthPlus2AndResistanceMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deagol1 = scn.GetFreepsCard("deagol1");
		var deagol2 = scn.GetFreepsCard("deagol2");
		var birthday = scn.GetFreepsCard("birthday");
		var shadowdeagol = scn.GetShadowCard("deagol1");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(deagol1, deagol2);
		scn.MoveCardsToDiscard(birthday, shadowdeagol);

		scn.StartGame();
		assertInHand(deagol1, deagol2);
		assertInDiscard(birthday, shadowdeagol);

		// Déagol in hand, Déagol in the other player's discard pile, and a card that only names Déagol: none counts.
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(5, scn.GetResistance(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetResistance(aragorn));

		scn.MoveCardsToDiscard(deagol1);
		assertEquals(5, scn.GetStrength(smeagol));     // 3 + 2
		assertEquals(4, scn.GetResistance(smeagol));   // 5 - 1

		scn.MoveCardsToDiscard(deagol2);
		assertEquals(7, scn.GetStrength(smeagol));     // 3 + 2 + 2
		assertEquals(3, scn.GetResistance(smeagol));   // 5 - 1 - 1
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetResistance(aragorn));
	}

	@Test
	public void DeagolDiscardingItselfFromHimSwitchesTheBonusOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deagol = scn.GetFreepsCard("deagol1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.AttachCardsTo(smeagol, deagol);

		scn.StartGame();
		assertEquals(smeagol, deagol.getAttachedTo());
		assertInZone(Zone.ATTACHED, deagol);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(6, scn.GetResistance(smeagol));   // 5 + 1 from Déagol borne
		assertTrue(scn.FreepsActionAvailable(deagol));
		scn.FreepsUseCardAction(deagol);
		assertInDiscard(deagol);
		scn.FreepsDismissRevealedCards();

		assertEquals(5, scn.GetStrength(smeagol));     // 3 + 2
		assertEquals(4, scn.GetResistance(smeagol));   // 5 - 1
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void EachTimeHeWinsASkirmishHeMayPutAGollumCardFromTheDrawDeckInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var deagol1 = scn.GetFreepsCard("deagol1");
		var deagol2 = scn.GetFreepsCard("deagol2");
		var gollum = scn.GetFreepsCard("gollum");
		var larder = scn.GetFreepsCard("larder");
		var fish = scn.GetFreepsCard("fish");
		var freepsrunner = scn.GetFreepsCard("runner");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToDiscard(deagol1, deagol2);

		scn.StartGame();
		assertInDiscard(deagol1, deagol2);
		assertInZone(Zone.DECK, gollum, larder, fish, freepsrunner);
		int discard = scn.GetFreepsDiscardCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		// Strength 7 with two Déagols in the discard pile, against the Runner's 5.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		// A [gollum] character, item and condition, of both sides, may be chosen; the [moria] Runner may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gollum, true));
		assertTrue(scn.FreepsHasCardChoiceAvailable(larder, true));
		assertTrue(scn.FreepsHasCardChoiceAvailable(fish, true));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(freepsrunner));
		scn.FreepsChooseCardBPFromSelection(larder);

		assertInDiscard(larder);
		assertEquals(discard + 1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DECK, gollum, fish, freepsrunner);
		// A deck that was looked through is shuffled (CR 4.2 "draw deck"), and the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheDrawDeckAndDiscardPileAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var deagol1 = scn.GetFreepsCard("deagol1");
		var deagol2 = scn.GetFreepsCard("deagol2");
		var fish = scn.GetFreepsCard("fish");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToDiscard(deagol1, deagol2);

		scn.StartGame();
		assertInDiscard(deagol1, deagol2);
		assertInZone(Zone.DECK, fish);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DECK, fish);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenHeLosesASkirmishAndLives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		// Strength 3 against the Runner's 5: he loses and takes one wound of his 4 vitality.
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfferedAgainWhenHeWinsTheFierceSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var deagol1 = scn.GetFreepsCard("deagol1");
		var deagol2 = scn.GetFreepsCard("deagol2");
		var fish = scn.GetFreepsCard("fish");
		var larder = scn.GetFreepsCard("larder");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToDiscard(deagol1, deagol2);

		scn.StartGame();
		assertInDiscard(deagol1, deagol2);
		assertInZone(Zone.DECK, fish, larder);
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		// Strength 7 against the Pursuer's 5, who lives (vitality 3) to be fought again in the fierce round.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(fish);
		assertInDiscard(fish);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(larder);
		assertInDiscard(fish, larder);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
