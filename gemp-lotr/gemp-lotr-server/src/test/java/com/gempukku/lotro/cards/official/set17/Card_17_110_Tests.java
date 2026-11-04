package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaf", "17_110");
					put("leaf2", "17_110");
					put("leaf3", "17_110");
					put("halfling", "17_106");  // Halfling Leaf: [shire] pipeweed condition (to play, spot a Hobbit); a skirmish ability used only on purpose
					put("ship", "18_27");       // Ship of Smoke: [gandalf] pipeweed condition; its trigger needs Gandalf, never in play
					put("toby", "1_305");       // Old Toby: [shire] pipeweed possession, to the support area; an optional draw when played
					put("toby2", "1_305");      // a second Old Toby, kept in hand
					put("pipe", "1_285");       // Bilbo's Pipe: a [shire] pipe, which is no pipeweed
					put("stash", "103_7");      // Wizard's Stash: a pipeweed whose bearer must be a Wizard; none is in play

					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] unbound Hobbit, strength 3; his text is about tales
					// Merry, Master Holbytla: a [rohan] unbound Hobbit, strength 3; his abilities are used only on purpose
					put("merry", "103_85");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is a Man

					put("brigand", "4_10");     // Dunlending Brigand: strength 8, no text
					put("brigand2", "4_10");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; his abilities are used only on purpose
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthfarthingLeafStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Southfarthing Leaf
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Pipeweed</b>. To play, spot an unbound Hobbit.<br><b>Skirmish:</b> Discard a pipeweed from play to make an unbound Hobbit strength +2.<br><b>Regroup:</b> Discard this pipeweed from play to play a pipeweed from your draw deck. Reshuffle your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leaf");

		assertEquals("Southfarthing Leaf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnUnboundHobbitIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(leaf, pippin);

		scn.StartGame();
		assertInHand(leaf, pippin);
		// The Ring-bearer and Sam are Hobbits but Ring-bound, Bounder is a Hobbit but no companion, and Aragorn is
		// unbound but no Hobbit.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(pippin);
		assertTrue(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayBePlayedWithAnUnboundHobbitOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(leaf);

		scn.StartGame();
		assertInHand(leaf);
		assertTrue(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionDiscardsAPipeweedToMakeAnUnboundHobbitStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var ship = scn.GetFreepsCard("ship");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		var pipe = scn.GetFreepsCard("pipe");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin, merry, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(pippin, pipe);
		scn.MoveCardsToSupportArea(leaf, halfling, ship, toby);
		scn.MoveCardsToHand(toby2);
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		var ted = scn.GetShadowCard("ted");
		assertInZone(Zone.SUPPORT, leaf, halfling, ship, toby);
		assertInZone(Zone.ATTACHED, pipe);
		assertInHand(toby2);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2, ted);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, brigand}, new PhysicalCardImpl[]{merry, brigand2});
		// Ted is left over, and the Shadow player assigns him to nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(pippin);
		assertInZone(Zone.SUPPORT, leaf, halfling, ship, toby);
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(leaf));

		scn.FreepsUseCardAction(leaf);
		// Any pipeweed in play, of either culture and either type; not the pipe, nor the pipeweed in hand.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(leaf, halfling, ship, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, toby2));
		scn.FreepsChooseCard(ship);
		assertInZone(Zone.DISCARD, ship);
		assertInZone(Zone.SUPPORT, leaf, halfling, toby);

		// The unbound Hobbit skirmishing and the one in a later skirmish, of two cultures; not the Ring-bound
		// Hobbits, the Hobbit ally, the Hobbit minion, nor an unbound companion who is no Hobbit.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, ted, aragorn));
		scn.FreepsChooseCard(merry);
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		// Merry's own skirmish is the next phase: the bonus lasted the skirmish it was given in.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUnboundHobbitTheSkirmishActionIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(leaf, halfling);
		var brigand = scn.GetShadowCard("brigand");
		assertInZone(Zone.SUPPORT, leaf, halfling);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, brigand);
		scn.FreepsResolveSkirmish(sam);
		assertInZone(Zone.SUPPORT, leaf, halfling);
		assertTrue(scn.FreepsActionAvailable(leaf));

		scn.FreepsUseCardAction(leaf);
		scn.FreepsChooseCard(halfling);
		assertInZone(Zone.DISCARD, halfling);
		// Nobody to choose: no character is any stronger.
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo)); // 3 + 1 for The Ruling Ring
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NeitherActionIsOfferedInThePhaseBeforeItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(leaf, halfling);
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		assertInZone(Zone.DECK, scn.GetFreepsCard("toby"));
		scn.SkipToPhase(Phase.MANEUVER);
		// The regroup action, with a pipeweed in the deck to play.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leaf));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The skirmish action, with an unbound Hobbit to make stronger.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leaf));
	}

	@Test
	public void RegroupActionDiscardsThisPipeweedToPlayAPipeweedOfAnyKindFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var leaf2 = scn.GetFreepsCard("leaf2");
		var leaf3 = scn.GetFreepsCard("leaf3");
		var halfling = scn.GetFreepsCard("halfling");
		var ship = scn.GetFreepsCard("ship");
		var toby = scn.GetFreepsCard("toby");
		var pipe = scn.GetFreepsCard("pipe");
		var stash = scn.GetFreepsCard("stash");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(leaf, leaf2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SUPPORT, leaf, leaf2);
		assertInZone(Zone.DECK, leaf3, halfling, ship, toby, pipe, stash);
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(leaf));

		scn.FreepsUseCardAction(leaf);
		// This copy pays, and the other stays.
		assertInZone(Zone.DISCARD, leaf);
		assertInZone(Zone.SUPPORT, leaf2);

		// The deck is shown, and then every pipeweed in it that can be played: of either culture and either type,
		// a third Southfarthing Leaf among them. Not the pipe, nor the pipeweed that needs a Wizard to bear it.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(leaf3, halfling, ship, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, stash));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(ship);
		assertInZone(Zone.SUPPORT, ship, leaf2);
		assertInZone(Zone.DECK, leaf3, halfling, toby, pipe, stash);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck looked through, and the reshuffle
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoPipeweedInTheDrawDeckTheRegroupActionIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var stash = scn.GetFreepsCard("stash");
		var pipe = scn.GetFreepsCard("pipe");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(leaf);
		// Every pipeweed that could be played out of the deck; the Wizard's Stash, which cannot, and the pipe stay in it.
		scn.MoveCardsToFreepsDiscard("leaf2", "leaf3", "halfling", "ship", "toby", "toby2");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SUPPORT, leaf);
		assertInZone(Zone.DECK, stash, pipe);
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(leaf));

		scn.FreepsUseCardAction(leaf);
		assertInZone(Zone.DISCARD, leaf);
		// Shown the deck, with nothing in it to play.
		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.DECK, stash, pipe);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
