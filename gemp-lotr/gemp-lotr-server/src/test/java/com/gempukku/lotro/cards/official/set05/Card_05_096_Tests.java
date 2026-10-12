package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eye", "5_96");
					put("eye2", "5_96");
					// Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("chieftain", "1_266");
					// Orc Taskmaster: [sauron] Orc, strength 8, vitality 3; a Shadow ability only
					put("taskmaster", "2_90");
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc of [moria], strength 13, no text
					// Cirith Ungol Guard: a [sauron] minion who is an Uruk-hai, strength 9; Damage +1 is its whole text
					put("ungol", "10_81");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: strength 6, vitality 3; Valiant is its whole text
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, strength 3
					// Bounder: Hobbit ally, strength 2, home site 2, where the first move takes the fellowship
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eye", "5_96");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: he bears Glamdring
					// Glamdring: "Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand.  Remove (1) for
					// each Orc revealed."  How a Free Peoples player reveals a card from a Shadow player's hand.
					put("glamdring", "1_75");
					put("savage", "1_151");     // Uruk Savage: a second card in the Shadow hand, and no Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResponseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eye", "5_96");
					put("eye2", "5_96");
					put("savage", "1_151");     // Uruk Savage: another card for the Shadow hand, and no Orc
					// More cards for a Shadow hand of six different cards, one of which is shown at random
					put("troop", "1_177");      // Goblin Patrol Troop
					put("runner", "1_178");     // Goblin Runner
					put("chieftain", "1_266");  // Orc Chieftain

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4
					// Treachery Deeper Than You Know: Fellowship event, cost 1, "Spot Gandalf to reveal an
					// opponent's hand"
					put("treachery", "1_86");
					// Glamdring: "Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand. Remove (1) for
					// each Orc revealed."
					put("glamdring", "1_75");
					// Legolas, Dauntless Hunter: "The twilight cost of each Shadow event and Shadow condition is +1
					// for each unbound Hobbit you can spot." Also the Elf who exerts at Galadriel's Glade
					put("legolas", "4_73");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound Hobbit
					// Meant to Be Alone: Fellowship event, cost 1. "Spot a Hobbit companion (except the Ring-bearer)
					// to reveal a card at random from an opponent's hand. Remove (X), where X is the twilight cost
					// of the card revealed."
					put("alone", "3_109");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					// Hidden Knowledge: Fellowship event, cost 1. "Spot a [gondor] companion to reveal the top 5
					// cards of an opponent's draw deck", a threat for each Shadow card among them, and a shuffle
					put("hidden", "7_104");

					// Troll's Keyward: [orc] Orc, vitality 3. "Shadow: Exert Troll's Keyward and reveal your hand
					// to add (1) for each [orc] Troll revealed."
					put("keyward", "12_105");
				}},
				// Site 6 is Galadriel's Glade: "Fellowship: Exert an Elf to look at an opponent's hand."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetItBurnsUsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eye", "5_96");
					put("savage", "1_151");     // Uruk Savage: a second card in the Shadow hand, and no Orc
					// It Burns Us: Free Peoples Regroup event, cost 1. "Spot Sam and Sméagol (or Gollum) to choose an
					// opponent who must reveal his or her hand. Wound a minion X times, where X is the number of
					// different cultures revealed." With no minion on the table nothing is wounded.
					put("burns", "6_110");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound
					// Smeagol, Once Like Us: Ring-bound companion; to play, add a burden (cheat him in); his one
					// ability is an assignment action
					put("smeagol", "103_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EyeofBaradDurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Eye of Barad-Dûr
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a companion or ally skirmishing a [sauron] Orc strength -1 for each Ring-bound companion.<br><b>Response:</b> If a Free Peoples player reveals this card from your hand, discard this card to add 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eye");

		assertEquals("Eye of Barad-Dûr", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheCompanionSkirmishingASauronOrcStrengthMinusOneForEachRingBoundCompanionForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, guard);

		var eye = scn.GetShadowCard("eye");
		var chieftain = scn.GetShadowCard("chieftain");
		var taskmaster = scn.GetShadowCard("taskmaster");
		scn.MoveCardsToHand(eye);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, taskmaster);

		// A skirmish event: not offered at the Shadow player's assignment actions.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(eye));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{gimli, taskmaster});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(eye));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));    // 3 + 1 for The Ruling Ring
		assertEquals(0, scn.GetBurdens());
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(eye);

		// Aragorn is the one companion skirmishing a [sauron] Orc, so nothing is asked. Frodo and Sam are the
		// Ring-bound companions; the three unbound ones add nothing. Gimli is assigned to a [sauron] Orc, but
		// that skirmish has not begun.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, eye);
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		scn.ShadowPass();

		// 6 against the Chieftain's 7: Aragorn loses, and with the skirmish over he is himself again.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAllySkirmishingASauronOrcIsMadeWeakerByOneWithFrodoTheOnlyRingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		var eye = scn.GetShadowCard("eye");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(eye);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);

		// The fellowship has moved to site 2, the Bounder's home.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, chieftain);
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();

		assertEquals(2, scn.GetStrength(bounder));
		assertTrue(scn.ShadowPlayAvailable(eye));
		scn.ShadowPlayCard(eye);

		// One Ring-bound companion beside two unbound ones.
		assertEquals(1, scn.GetStrength(bounder));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertInZone(Zone.DISCARD, eye);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayedInASkirmishAgainstAnOrcOfAnotherCultureOrASauronMinionWhoIsNoOrcItChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, guard);

		var eye = scn.GetShadowCard("eye");
		var eye2 = scn.GetShadowCard("eye2");
		var troop = scn.GetShadowCard("troop");
		var ungol = scn.GetShadowCard("ungol");
		var taskmaster = scn.GetShadowCard("taskmaster");
		scn.MoveCardsToHand(eye, eye2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, ungol, taskmaster);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, ungol},
				new PhysicalCardImpl[]{guard, taskmaster});

		// Aragorn against an Orc of [moria]. The Guard is assigned to a [sauron] Orc, and is not skirmishing it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(eye));
		scn.ShadowPlayCard(eye);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, eye);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));    // 3 + 1 for The Ruling Ring

		scn.FreepsPass();
		scn.ShadowPass();

		// Gimli against a [sauron] Uruk-hai.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(eye2));
		scn.ShadowPlayCard(eye2);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, eye2);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
	}

	@Test
	public void ResponseIsPlayedWhenAFreePeoplesPlayerRevealsItFromHandAndAddsTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye, savage);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(glamdring));

		scn.FreepsUseCardAction(glamdring);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// The Shadow hand is shown to the Free Peoples player, the Eye in it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eye, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		scn.FreepsDismissRevealedCards();

		// The response is the event played from hand (CR 4.2, the ruling on this card: it "must be played in
		// order to use its response text"; "discard this card to" is superfluous).
		assertTrue(scn.ShadowPlayAvailable(eye));
		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(eye);

		assertEquals(2, scn.GetBurdens());
		assertInDiscard(eye);
		assertInHand(savage);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCopyRevealedByAFreePeoplesEventMayBePlayedOrKept() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var treachery = scn.GetFreepsCard("treachery");
		var eye = scn.GetShadowCard("eye");
		var eye2 = scn.GetShadowCard("eye2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(treachery);
		scn.MoveCardsToHand(eye, eye2, savage);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(treachery);
		assertEquals(1, scn.GetTwilight());
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.ShadowPlayAvailable(eye));
		assertTrue(scn.ShadowPlayAvailable(eye2));
		scn.ShadowPlayCard(eye);

		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.DISCARD, eye);
		// It costs nothing.
		assertEquals(1, scn.GetTwilight());

		// The other copy was revealed as well, and answers for itself: kept, it adds nothing.
		assertTrue(scn.ShadowPlayAvailable(eye2));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetBurdens());
		assertInHand(eye2, savage);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheShadowPlayerRevealsHisOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var eye = scn.GetShadowCard("eye");
		var savage = scn.GetShadowCard("savage");
		var keyward = scn.GetShadowCard("keyward");
		scn.MoveCardsToHand(eye, savage);

		scn.StartGame();
		scn.MoveMinionsToTable(keyward);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(keyward));
		scn.ShadowUseCardAction(keyward);

		assertEquals(1, scn.GetWoundsOn(keyward));
		// The Free Peoples player is shown the hand, the Eye in it, but did not reveal it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eye, false));
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(eye));
		assertEquals(0, scn.GetBurdens());
		assertInHand(eye, savage);
	}

	@Test
	public void NotOfferedWhenTheFreePeoplesPlayerLooksAtTheHandAndDoesNotRevealIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var eye = scn.GetShadowCard("eye");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand(eye, savage);

		var glade = scn.GetCurrentSite();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(glade));
		scn.FreepsUseCardAction(glade);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasCardChoiceAvailable(eye, false));
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertInHand(eye, savage);
	}

	// It Burns Us has the Shadow player "reveal his or her hand".  He has no choice in it, so the one revealing is
	// the Free Peoples player whose card it is (Ketura, Q-0251), and the Eye answers.
	@Test
	public void WhenAFreePeoplesEventHasTheShadowPlayerRevealHisHandTheEyeIsOfferedAndAddsTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetItBurnsUsScenario();

		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var burns = scn.GetFreepsCard("burns");
		var eye = scn.GetShadowCard("eye");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.MoveCardsToHand(burns);
		scn.MoveCardsToHand(eye, savage);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(burns));
		scn.FreepsPlayCard(burns);

		// The Shadow hand is shown to the Free Peoples player, the Eye in it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eye, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.ShadowPlayAvailable(eye));
		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(eye);

		assertEquals(2, scn.GetBurdens());
		assertInDiscard(eye);
		assertInHand(savage);
		assertEquals(1, scn.GetShadowHandCount());
		// There is no minion for It Burns Us to wound, and the regroup phase goes on.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WhenOneCardOfTheHandIsRevealedAtRandomTheEyeIsOfferedOnlyIfItWasTheCardShown() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var merry = scn.GetFreepsCard("merry");
		var alone = scn.GetFreepsCard("alone");
		var eye = scn.GetShadowCard("eye");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(alone);
		// Six different cards, so that the card shown is most often not the Eye.
		scn.MoveCardsToShadowHand("eye", "savage", "keyward", "troop", "runner", "chieftain");

		scn.StartGame();

		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(alone);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is whether it was the Eye.
		var eyeShown = scn.FreepsHasCardChoiceAvailable(eye, false);
		scn.FreepsDismissRevealedCards();

		// Another card of the hand revealed is not "this card": the Shadow player is then asked nothing, and
		// the fellowship phase goes on.
		assertEquals(eyeShown, scn.ShadowPlayAvailable(eye));
		assertEquals(!eyeShown, scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetShadowHandCount());
	}

	@Test
	public void ACopyRevealedFromTheTopOfTheDrawDeckDoesNotLetTheCopyInHandBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var hidden = scn.GetFreepsCard("hidden");
		var eye = scn.GetShadowCard("eye");
		var eye2 = scn.GetShadowCard("eye2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hidden);
		scn.MoveCardsToHand(eye2);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(eye);

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(hidden);

		// An Eye is among the five cards revealed from the top of the Shadow draw deck.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eye, false));
		scn.DismissRevealedCards();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertInHand(eye2);
		assertInZone(Zone.DECK, eye);
	}

	// CR 4.2, the ruling on this card: "This card must be played in order to use its response text, which is
	// affected by cards like Legolas, Dauntless Hunter."
	@Test
	public void BesideLegolasDauntlessHunterAndAnUnboundHobbitTheResponseMustBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var legolas = scn.GetFreepsCard("legolas");
		var merry = scn.GetFreepsCard("merry");
		var eye = scn.GetShadowCard("eye");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf, legolas, merry);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye, savage);

		scn.StartGame();

		// 0 + 1 for Merry, the one unbound Hobbit.
		assertEquals(1, scn.GetTwilightCost(eye));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(glamdring);
		assertTrue(scn.FreepsHasCardChoiceAvailable(eye, false));
		scn.FreepsDismissRevealedCards();

		// Revealed, with nothing in the pool to play it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertInHand(eye);

		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(glamdring);
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.ShadowPlayAvailable(eye));
		scn.ShadowPlayCard(eye);

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.DISCARD, eye);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
