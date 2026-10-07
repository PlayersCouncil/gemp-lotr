package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippinspipe", "103_114");
					put("pippin", "1_306");       // Pippin, Friend to Frodo: an unbound Hobbit; his text is about [shire] tales
					put("merry", "1_302");        // Merry, Friend to Sam: an unbound Hobbit; a skirmish ability never used here
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound companion who is no Hobbit
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("bounder", "1_286");      // Bounder: a Hobbit ally; a skirmish ability

					// Merry's Pipe: [shire]. "Each time a pipeweed is discarded, you may exert bearer to play that pipeweed
					// from your discard pile. Hinder it and this pipe." Its offer is declined wherever it is made here.
					put("merryspipe", "103_113");
					// The other pipes all read "Fellowship: Discard a pipeweed possession and spot X pipes to ...", never used here.
					put("aragornspipe", "1_91");  // Aragorn's Pipe: [gondor], bearer a [gondor] companion
					put("gandalfspipe", "1_74");  // Gandalf's Pipe: [gandalf], bearer Gandalf
					put("gafferspipe", "1_292");  // The Gaffer's Pipe: [shire], bearer a Hobbit
					put("bilbospipe", "1_285");   // Bilbo's Pipe: [shire], bearer a Hobbit

					put("toby", "1_305");         // Old Toby: a [shire] pipeweed possession, support area. When played, may draw a card
					put("longbottom", "1_300");   // Longbottom Leaf: the same; when played, may discard cards from hand
					put("halfling", "17_106");    // Halfling Leaf: a [shire] pipeweed condition, support area; a skirmish ability
					// Long-stemmed Pipe: a [gandalf] pipeweed condition and, for all its name, no pipe. A regroup ability.
					put("longstem", "17_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinsPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Pippin's Pipe
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be an unbound Hobbit.
		 * 	Fellowship: Discard a pipeweed to remove X threats, where X is the number of pipes you can spot. Hinder this pipe or hinder bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippinspipe");

		assertEquals("Pippin's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForOneOnAnUnboundHobbitAndOfferedForNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippinspipe = scn.GetFreepsCard("pippinspipe");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");

		scn.MoveCompanionsToTable(pippin, merry, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(pippinspipe);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(pippinspipe));

		scn.FreepsPlayCard(pippinspipe);

		// The two unbound Hobbits. Frodo and Sam are Ring-bound Hobbits, Aragorn is unbound and no Hobbit, and the
		// Bounder is a Hobbit and an ally: only a companion is unbound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, aragorn, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(merry);

		assertAttachedTo(pippinspipe, merry);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsAPipeweedToRemoveAThreatForEachPipeSpottedAndHindersThePipeWhenThatIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippinspipe = scn.GetFreepsCard("pippinspipe");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var gafferspipe = scn.GetFreepsCard("gafferspipe");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var toby = scn.GetFreepsCard("toby");
		var longbottom = scn.GetFreepsCard("longbottom");
		var halfling = scn.GetFreepsCard("halfling");
		var longstem = scn.GetFreepsCard("longstem");

		// Five companions with Frodo.
		scn.MoveCompanionsToTable(pippin, merry, aragorn, gandalf);
		// Pipes of three cultures on a Hobbit, a Man and a Wizard: 3 to spot.
		scn.AttachCardsTo(pippin, pippinspipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.AttachCardsTo(gandalf, gandalfspipe);
		// A fourth pipe in play that will be hindered, and a fifth in the discard pile.
		scn.AttachCardsTo(merry, gafferspipe);
		scn.MoveCardsToDiscard(bilbospipe);
		// Pipeweed: a [shire] possession and a [gandalf] condition in play, a third in play that will be hindered,
		// and one in hand.
		scn.MoveCardsToSupportArea(toby, longstem, halfling);
		scn.MoveCardsToHand(longbottom);

		scn.StartGame();
		scn.HinderCard(gafferspipe, halfling);
		scn.AddThreats(4);

		assertEquals(4, scn.GetThreats());

		scn.FreepsUseCardAction(pippinspipe);

		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, longstem));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(halfling, longbottom, pippinspipe, aragornspipe, gandalfspipe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(longstem);

		assertInZone(Zone.DISCARD, longstem);
		assertInZone(Zone.SUPPORT, toby);
		// Pippin's, Aragorn's and Gandalf's: 3 of the 4 threats. The hindered pipe, the pipe in the discard pile and
		// the pipeweed left in play are no pipes to spot.
		assertEquals(1, scn.GetThreats());

		assertTrue(scn.FreepsChoiceAvailable("Pippin's Pipe"));
		assertTrue(scn.FreepsChoiceAvailable("Friend to Frodo"));
		scn.FreepsChoose("Pippin's Pipe");

		assertTrue(scn.IsHindered(pippinspipe));
		assertFalse(scn.IsHindered(pippin));
		assertFalse(scn.IsHindered(aragornspipe));
		assertFalse(scn.IsHindered(gandalfspipe));
		assertFalse(scn.IsHindered(toby));
		assertEquals(1, scn.GetThreats());

		// A hindered pipe has no ability, though Old Toby is there to discard.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippinspipe));
	}

	@Test
	public void HinderingBearerLeavesThePipeToBeUsedAgainAndThenThePipeIsHinderedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippinspipe = scn.GetFreepsCard("pippinspipe");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var merryspipe = scn.GetFreepsCard("merryspipe");
		var toby = scn.GetFreepsCard("toby");
		var halfling = scn.GetFreepsCard("halfling");

		// Three companions with Frodo, and two pipes.
		scn.MoveCompanionsToTable(pippin, merry);
		scn.AttachCardsTo(pippin, pippinspipe);
		scn.AttachCardsTo(merry, merryspipe);
		scn.MoveCardsToSupportArea(toby, halfling);

		scn.StartGame();
		scn.AddThreats(3);

		scn.FreepsUseCardAction(pippinspipe);
		scn.FreepsChooseCard(toby);
		// Merry's Pipe would play Old Toby again and be hindered for it.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, toby);
		// Two pipes: 2 of the 3 threats.
		assertEquals(1, scn.GetThreats());

		assertTrue(scn.FreepsChoiceAvailable("Pippin's Pipe"));
		assertTrue(scn.FreepsChoiceAvailable("Friend to Frodo"));
		scn.FreepsChoose("Friend to Frodo");

		// Pippin, and not the other unbound Hobbit with a pipe.
		assertTrue(scn.IsHindered(pippin));
		assertFalse(scn.IsHindered(pippinspipe));
		assertFalse(scn.IsHindered(merry));
		assertFalse(scn.IsHindered(merryspipe));

		// The pipe is not hindered and its ability is still there.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pippinspipe));

		// The Halfling Leaf is the one pipeweed left.
		scn.FreepsUseCardAction(pippinspipe);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, halfling);
		// Two pipes still, and one threat to remove.
		assertEquals(0, scn.GetThreats());
		// Pippin is hindered already, so the pipe is what is hindered and nothing is asked.
		assertTrue(scn.IsHindered(pippinspipe));
		assertTrue(scn.IsHindered(pippin));
		assertFalse(scn.IsHindered(merryspipe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoPipeweedInPlayUntilOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippinspipe = scn.GetFreepsCard("pippinspipe");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var toby = scn.GetFreepsCard("toby");
		var halfling = scn.GetFreepsCard("halfling");
		var longstem = scn.GetFreepsCard("longstem");

		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.AttachCardsTo(pippin, pippinspipe);
		// In play: another pipe and a hindered pipeweed. A pipeweed in the discard pile and one in hand.
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToSupportArea(halfling);
		scn.MoveCardsToDiscard(longstem);
		scn.MoveCardsToHand(toby);

		scn.StartGame();
		scn.HinderCard(halfling);
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(pippinspipe));

		scn.FreepsPlayCard(toby);
		// Old Toby's own offer of a card.
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.FreepsActionAvailable(pippinspipe));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoThreatItIsStillOfferedAndItsCostAndItsHinderingAreCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippinspipe = scn.GetFreepsCard("pippinspipe");
		var pippin = scn.GetFreepsCard("pippin");
		var toby = scn.GetFreepsCard("toby");

		scn.MoveCompanionsToTable(pippin);
		scn.AttachCardsTo(pippin, pippinspipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(pippinspipe));

		// Old Toby is the one pipeweed.
		scn.FreepsUseCardAction(pippinspipe);

		assertInZone(Zone.DISCARD, toby);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsChoiceAvailable("Pippin's Pipe"));
		assertTrue(scn.FreepsChoiceAvailable("Friend to Frodo"));
		scn.FreepsChoose("Pippin's Pipe");

		assertTrue(scn.IsHindered(pippinspipe));
		assertFalse(scn.IsHindered(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippinspipe = scn.GetFreepsCard("pippinspipe");
		var pippin = scn.GetFreepsCard("pippin");
		var toby = scn.GetFreepsCard("toby");

		scn.MoveCompanionsToTable(pippin);
		scn.AttachCardsTo(pippin, pippinspipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(pippinspipe));

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippinspipe));
		assertEquals(1, scn.GetThreats());
	}
}
