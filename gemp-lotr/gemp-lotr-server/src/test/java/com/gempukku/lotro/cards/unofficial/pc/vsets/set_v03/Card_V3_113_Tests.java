package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merryspipe", "103_113");
					put("merry", "1_302");        // Merry, Friend to Sam: an unbound Hobbit, vitality 4; a skirmish ability never used here
					put("pippin", "1_306");       // Pippin, Friend to Frodo: an unbound Hobbit, vitality 4; his text is about [shire] tales
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound companion who is no Hobbit
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: a Wizard; his text is about his own strength
					put("bounder", "1_286");      // Bounder: a Hobbit ally; a skirmish ability

					// "Fellowship: Discard a pipeweed possession and spot X pipes to ..."
					put("aragornspipe", "1_91");  // Aragorn's Pipe: [gondor], bearer a [gondor] companion; heals X companions
					put("gafferspipe", "1_292");  // The Gaffer's Pipe: [shire], bearer a Hobbit; removes (X)

					put("toby", "1_305");         // Old Toby: a [shire] pipeweed possession, cost 1, support area. When played, may draw a card
					put("longbottom", "1_300");   // Longbottom Leaf: the same, but when played may discard up to 2 cards from hand
					put("halfling", "17_106");    // Halfling Leaf: a [shire] pipeweed condition, cost 1, support area; a skirmish ability
					// Hornblower Leaf: a [shire] pipeweed condition, cost 1. "Regroup: Discard a pipeweed from play to remove a
					// threat." (and another to discard a minion): the way these tests discard a pipeweed, itself included.
					put("hornblower1", "17_108");
					put("hornblower2", "17_108");
					// Saruman's Hospitality: a [gandalf] pipeweed possession, cost 1, strength +1; bearer an unbound companion
					// bearing a pipe. Its trigger on moving needs pipes of 3 cultures, which no board here has.
					put("hospitality", "103_4");
					// Wizard's Stash: a [gandalf] pipeweed possession, cost 2; bearer must be a Wizard. Companions bearing a
					// pipe are strength +1; a regroup ability never used here.
					put("stash", "103_7");

					put("beauty1", "1_205");      // Beauty Is Fading: "Maneuver: Exert a Nazgul to discard a Free Peoples possession
					put("beauty2", "1_205");      // or Free Peoples condition." Cost 5.
					put("witchking", "1_237");    // The Witch-king, Lord of Angmar: a Nazgul of vitality 4
					put("marksman", "1_176");     // Goblin Marksman: an archer and nothing else
					// Sleep, Caradhras: "Fellowship: Exert Gandalf to discard every condition." Cost 3. The way to discard
					// two pipeweeds at once.
					put("sleep", "1_84");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerrysPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Merry's Pipe
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be an unbound Hobbit.
		 * 	Each time a pipeweed is discarded, you may exert bearer to play that pipeweed from your discard pile.  Hinder it and this pipe.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merryspipe");

		assertEquals("Merry's Pipe", card.getBlueprint().getTitle());
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
		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");

		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(merryspipe);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(merryspipe));

		scn.FreepsPlayCard(merryspipe);

		// The two unbound Hobbits. Frodo and Sam are Ring-bound Hobbits, Aragorn is unbound and no Hobbit, and the
		// Bounder is a Hobbit and an ally: only a companion is unbound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, aragorn, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(pippin);

		assertAttachedTo(merryspipe, pippin);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APipeweedDiscardedByAnotherPipeMayBePlayedFromTheDiscardPileForAnExertionOfBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var toby = scn.GetFreepsCard("toby");
		var longbottom = scn.GetFreepsCard("longbottom");
		var halfling = scn.GetFreepsCard("halfling");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, merryspipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToSupportArea(toby, longbottom);
		// A pipeweed that was in the discard pile already.
		scn.MoveCardsToDiscard(halfling);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(merry));
		int hand = scn.GetFreepsHandCount();

		// Aragorn's Pipe: "Discard a pipeweed possession and spot X pipes to heal X companions."
		scn.FreepsUseCardAction(aragornspipe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, longbottom));
		scn.FreepsChooseCard(toby);

		// The offer comes with Old Toby in the discard pile.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		assertInZone(Zone.DISCARD, toby);
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsAcceptOptionalTrigger();

		// It was played: Old Toby's own "When you play this possession, you may draw a card."
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetFreepsHandCount());

		// The bearer of this pipe exerted, not the bearer of the other one.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Old Toby costs 1.
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(merryspipe));
		// Not the other pipeweed in play, the other pipe, or the bearer.
		assertFalse(scn.IsHindered(longbottom));
		assertFalse(scn.IsHindered(aragornspipe));
		assertFalse(scn.IsHindered(merry));
		// "That pipeweed": the one that lay in the discard pile before is still there.
		assertInZone(Zone.DISCARD, halfling);

		// Aragorn's Pipe goes on to its spotting. Merry's Pipe is hindered and is no pipe to spot: 0 or 1.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("0");

		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APipeDiscardedByAShadowCardIsNotAnsweredAndAPipeweedDiscardedByItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var toby = scn.GetFreepsCard("toby");
		var beauty1 = scn.GetShadowCard("beauty1");
		var beauty2 = scn.GetShadowCard("beauty2");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, merryspipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToHand(beauty1, beauty2);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.SetTwilight(20);

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPass();
		scn.ShadowPlayCard(beauty1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(merryspipe, aragornspipe, toby));
		scn.ShadowChooseCard(aragornspipe);

		// A pipe is no pipeweed.
		assertInZone(Zone.DISCARD, aragornspipe);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsPass();
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(beauty2);
		scn.ShadowChooseCard(toby);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		assertInZone(Zone.DISCARD, toby);

		scn.FreepsAcceptOptionalTrigger();
		// Old Toby's own offer of a card.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// 5 removed for the event, 1 added for Old Toby
		assertEquals(twilight - 5 + 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(merryspipe));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningLeavesEverythingAsItWasAndTheNextPipeweedDiscardedIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var gafferspipe = scn.GetFreepsCard("gafferspipe");
		var halfling = scn.GetFreepsCard("halfling");
		var hornblower = scn.GetFreepsCard("hornblower1");

		scn.MoveCompanionsToTable(merry, pippin);
		scn.AttachCardsTo(merry, merryspipe);
		scn.AttachCardsTo(pippin, gafferspipe);
		scn.MoveCardsToSupportArea(halfling, hornblower);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		int twilight = scn.GetTwilight();

		scn.FreepsUseCardAction(hornblower, "Remove a threat");
		assertTrue(scn.FreepsHasCardChoiceAvailable(halfling, hornblower));
		scn.FreepsChooseCard(halfling);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, halfling);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(twilight, scn.GetTwilight());
		assertFalse(scn.IsHindered(merryspipe));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		scn.ShadowPass();
		// The Hornblower Leaf is the one pipeweed left in play and discards itself.
		scn.FreepsUseCardAction(hornblower, "Remove a threat");

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		assertInZone(Zone.DISCARD, hornblower);
		scn.FreepsAcceptOptionalTrigger();

		// The pipeweed just discarded is the one played; the one declined earlier stays where it is.
		assertInZone(Zone.SUPPORT, hornblower);
		assertInZone(Zone.DISCARD, halfling);
		assertTrue(scn.IsHindered(hornblower));
		assertTrue(scn.IsHindered(merryspipe));
		assertFalse(scn.IsHindered(gafferspipe));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WhileHinderedItAnswersNothingAndOnceRestoredItAnswersTheSamePipeweedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var halfling = scn.GetFreepsCard("halfling");
		var hornblower1 = scn.GetFreepsCard("hornblower1");
		var hornblower2 = scn.GetFreepsCard("hornblower2");

		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, merryspipe);
		scn.MoveCardsToSupportArea(halfling, hornblower1, hornblower2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsUseCardAction(hornblower1, "Remove a threat");
		scn.FreepsChooseCard(halfling);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.SUPPORT, halfling);
		assertTrue(scn.IsHindered(halfling));
		assertTrue(scn.IsHindered(merryspipe));
		assertEquals(1, scn.GetWoundsOn(merry));

		// With the pipe hindered, a pipeweed that could be played again and a bearer able to exert draw no offer.
		scn.ShadowPass();
		scn.FreepsUseCardAction(hornblower1, "Remove a threat");
		assertTrue(scn.FreepsHasCardChoiceAvailable(hornblower1, hornblower2));
		scn.FreepsChooseCard(hornblower1);

		assertInZone(Zone.DISCARD, hornblower1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// Restored (as a reconcile would), it answers again, in the same phase and for the same pipeweed.
		scn.RestoreCard(merryspipe, halfling);
		scn.ShadowPass();
		scn.FreepsUseCardAction(hornblower2, "Remove a threat");
		assertTrue(scn.FreepsHasCardChoiceAvailable(halfling, hornblower2));
		scn.FreepsChooseCard(halfling);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.SUPPORT, halfling);
		assertTrue(scn.IsHindered(halfling));
		assertTrue(scn.IsHindered(merryspipe));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void OfTwoPipeweedsDiscardedAtOnceEachIsOfferedAndThePipeAnswersOnlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var halfling = scn.GetFreepsCard("halfling");
		var hornblower = scn.GetFreepsCard("hornblower1");
		var sleep = scn.GetFreepsCard("sleep");

		scn.MoveCompanionsToTable(merry, gandalf);
		scn.AttachCardsTo(merry, merryspipe);
		// Two pipeweeds that are conditions.
		scn.MoveCardsToSupportArea(halfling, hornblower);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsPlayCard(sleep);

		// One offer for each pipeweed, each named for its own.
		assertInZone(Zone.DISCARD, halfling);
		assertInZone(Zone.DISCARD, hornblower);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe, "Halfling Leaf"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe, "Hornblower Leaf"));

		scn.FreepsChooseOptionalTrigger(merryspipe, "Hornblower Leaf");

		// "That pipeweed": the one whose offer was taken, not the one discarded with it.
		assertInZone(Zone.SUPPORT, hornblower);
		assertInZone(Zone.DISCARD, halfling);
		assertTrue(scn.IsHindered(hornblower));
		assertTrue(scn.IsHindered(merryspipe));
		assertEquals(1, scn.GetWoundsOn(merry));
		// 3 for the event, 1 for the Hornblower Leaf
		assertEquals(4, scn.GetTwilight());

		// The pipe is hindered now and answers nothing: its offer for the Halfling Leaf is gone, though Merry
		// could exert again and the Leaf could be played.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var halfling = scn.GetFreepsCard("halfling");
		var hornblower = scn.GetFreepsCard("hornblower1");

		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, merryspipe);
		scn.MoveCardsToSupportArea(halfling, hornblower);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(merry, 3);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsUseCardAction(hornblower, "Remove a threat");
		scn.FreepsChooseCard(halfling);

		assertInZone(Zone.DISCARD, halfling);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(merry));
		assertFalse(scn.IsHindered(merryspipe));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhileBearerIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var halfling = scn.GetFreepsCard("halfling");
		var hornblower = scn.GetFreepsCard("hornblower1");

		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, merryspipe);
		scn.MoveCardsToSupportArea(halfling, hornblower);

		scn.StartGame();
		// A hindered character cannot exert, and what he bears stays active.
		scn.HinderCard(merry);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.IsHindered(merry));
		assertFalse(scn.IsHindered(merryspipe));

		scn.FreepsUseCardAction(hornblower, "Remove a threat");
		scn.FreepsChooseCard(halfling);

		assertInZone(Zone.DISCARD, halfling);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.IsHindered(merryspipe));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForAPipeweedThatCannotBePlayedAndOfferedForOneThatCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stash = scn.GetFreepsCard("stash");
		var hornblower = scn.GetFreepsCard("hornblower1");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, merryspipe);
		// Wizard's Stash may only be played on a Wizard. Put on Aragorn with no Wizard in play, it stands in for a
		// Stash whose Wizard has left play: once discarded it cannot be played.
		scn.AttachCardsTo(aragorn, stash);
		scn.MoveCardsToSupportArea(hornblower);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsUseCardAction(hornblower, "Remove a threat");
		assertTrue(scn.FreepsHasCardChoiceAvailable(stash, hornblower));
		scn.FreepsChooseCard(stash);

		assertInZone(Zone.DISCARD, stash);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// On the same board the Hornblower Leaf, which can be played, is offered.
		scn.ShadowPass();
		scn.FreepsUseCardAction(hornblower, "Remove a threat");

		assertInZone(Zone.DISCARD, hornblower);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.SUPPORT, hornblower);
		assertInZone(Zone.DISCARD, stash);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void APipeweedBorneByACharacterIsPlayedOntoACharacterWhoMayBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var stash = scn.GetFreepsCard("stash");
		var hornblower = scn.GetFreepsCard("hornblower1");

		scn.MoveCompanionsToTable(merry, aragorn, gandalf);
		scn.AttachCardsTo(merry, merryspipe);
		// As in the test before, but with a Wizard in play.
		scn.AttachCardsTo(aragorn, stash);
		scn.MoveCardsToSupportArea(hornblower);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		int twilight = scn.GetTwilight();

		scn.FreepsUseCardAction(hornblower, "Remove a threat");
		scn.FreepsChooseCard(stash);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));
		assertInZone(Zone.DISCARD, stash);
		scn.FreepsAcceptOptionalTrigger();

		// Gandalf is the one Wizard. Wizard's Stash costs 2.
		assertAttachedTo(stash, gandalf);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.IsHindered(stash));
		assertTrue(scn.IsHindered(merryspipe));
		assertFalse(scn.IsHindered(gandalf));
		assertFalse(scn.IsHindered(hornblower));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	@Ignore("F-0170: a pipeweed discarded with its bearer when he is killed draws no offer; the trigger asks for a discarding card")
	public void APipeweedDiscardedWhenItsBearerIsKilledIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var gafferspipe = scn.GetFreepsCard("gafferspipe");
		var hospitality = scn.GetFreepsCard("hospitality");
		var marksman = scn.GetShadowCard("marksman");

		scn.MoveCompanionsToTable(merry, pippin);
		scn.AttachCardsTo(merry, merryspipe);
		scn.AttachCardsTo(pippin, gafferspipe, hospitality);
		// Vitality 4 with 3 wounds: the archery wound kills him.
		scn.AddWoundsToChar(pippin, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToArcheryWounds();

		int twilight = scn.GetTwilight();

		scn.FreepsChooseCard(pippin);

		// Pippin is dead, and what he bore went to the discard pile with him (CR 4.2 "leaving play").
		assertInZone(Zone.DEAD, pippin);
		assertInZone(Zone.DISCARD, gafferspipe, hospitality);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merryspipe));

		scn.FreepsAcceptOptionalTrigger();

		// Merry, who bears this pipe, is the one unbound companion left who bears a pipe.
		assertAttachedTo(hospitality, merry);
		assertInZone(Zone.DISCARD, gafferspipe);
		assertTrue(scn.IsHindered(hospitality));
		assertTrue(scn.IsHindered(merryspipe));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void APipeweedDiscardedFromHandIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var toby = scn.GetFreepsCard("toby");
		var longbottom = scn.GetFreepsCard("longbottom");

		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, merryspipe);
		scn.MoveCardsToHand(longbottom, toby);

		scn.StartGame();

		// Longbottom Leaf: "When you play this possession, you may discard up to 2 cards from hand."
		scn.FreepsPlayCard(longbottom);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(longbottom));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(toby);

		// "Discarded" is discarded from play; a discard from hand always says so.
		assertInZone(Zone.DISCARD, toby);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.IsHindered(merryspipe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
