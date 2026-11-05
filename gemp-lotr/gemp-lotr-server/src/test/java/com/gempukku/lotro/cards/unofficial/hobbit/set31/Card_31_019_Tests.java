package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_019_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  A doubt is the HDG's burden.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nothing", "31_19");
					// Gollum of two cultures, and the [gollum] minion who is no Gollum.
					put("gollum", "31_20");      // Gollum, Small Slimy Creature: [gollum] minion, cost 2, site 4; a skirmish-win trigger and an Assignment ability, not used
					put("wraith", "103_66");     // Gollum, Half a Wraith Himself: [wraith] minion, cost 2, site 3; answers a Nazgûl played, an Assignment ability: neither used
					put("shelob", "8_26");       // Shelob, Last Child of Ungoliant: [gollum] Spider minion, cost 6
					put("stinker", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, cost 2, site 3; a regroup ability, not used
					put("deceiver", "7_58");     // Gollum, Plotting Deceiver: a Gollum of another blueprint, for the other player's discard pile
					// Orcs of two cultures, and a minion who is no Orc.
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc; an Assignment ability, not used
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, no text
					put("troll", "32_31");       // Demolition Troll: [moria] Troll; a Skirmish ability, not used
					// [gandalf] events of two phases.
					// A Wizard Is Never Late: Fellowship, cost 1, plays Gandalf or an ally who is no Dwarf from the
					// draw deck or the discard pile.
					put("late", "30_23");
					// His Wrath Was Redoubled: Assignment, cost 3; strengthens [dwarven] companions bearing followers
					// (there are none).
					put("wrath", "31_16");
					put("bard", "30_22");        // Bard, The Bowman: [esgaroth] ally, the card the Fellowship event plays
					put("betsy", "33_19");       // Betsy Butterbur: a [gandalf] ally, cost 0, a card of the culture that is no event
					put("blunt", "33_4");        // Blunt The Knives!: a [dwarven] Fellowship event, cost 0; "draw 2 cards" chosen
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BetterThanNothingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Better Than Nothing
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot Gollum, each [gandalf] event gains this cost: "add a doubt."<br>At the end of
		 * your Shadow phase, you may discard an Orc from play to play Gollum from your draw deck or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nothing");

		assertEquals("Better Than Nothing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileGollumCanBeSpottedAGandalfEventCostsADoubtAndOtherCardsDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var stinker = scn.GetShadowCard("stinker");
		var late = scn.GetFreepsCard("late");
		var bard = scn.GetFreepsCard("bard");
		var betsy = scn.GetFreepsCard("betsy");
		var blunt = scn.GetFreepsCard("blunt");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		scn.SetFreepsDrawDeck(bard, scn.GetFreepsCard("troop"), scn.GetFreepsCard("troll"));
		scn.MoveCardsToHand(late, betsy, blunt);
		// A [gollum] Gollum stands in the fellowship phase, as he would later in the turn (Q-0228).
		scn.MoveMinionsToTable(stinker);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, nothing);
		assertInZone(Zone.SHADOW_CHARACTERS, stinker);
		assertInZone(Zone.DECK, bard);
		assertEquals(0, scn.FreepsGetBurdens());

		// A [gandalf] card that is no event: no doubt.
		scn.FreepsPlayCard(betsy);
		assertInZone(Zone.SUPPORT, betsy);
		assertEquals(0, scn.FreepsGetBurdens());

		// The [gandalf] event: a doubt more, and the event carried out.
		assertTrue(scn.FreepsPlayAvailable(late));
		scn.FreepsPlayCard(late);
		assertEquals(1, scn.FreepsGetBurdens());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(bard);
		assertInZone(Zone.SUPPORT, bard);
		assertInZone(Zone.DISCARD, late);
		assertEquals(1, scn.FreepsGetBurdens());

		// No Balin to play and one card in the discard pile: the draw is the one branch that can be done in full,
		// and is carried out unasked (Q-0418).
		scn.FreepsPlayCard(blunt);
		// An event of another culture: no doubt more.
		assertInZone(Zone.DISCARD, blunt);
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfEventOfAnotherPhaseCostsADoubtWhileTheWraithGollumCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var wraith = scn.GetShadowCard("wraith");
		var wrath = scn.GetFreepsCard("wrath");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		scn.MoveCardsToHand(wrath);
		scn.MoveMinionsToTable(wraith);
		assertInZone(Zone.SUPPORT, nothing);
		assertInZone(Zone.SHADOW_CHARACTERS, wraith);
		assertInHand(wrath);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.FreepsGetBurdens());

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(wrath));
		scn.FreepsPlayCard(wrath);

		assertInZone(Zone.DISCARD, wrath);
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNoGollumInPlayAGandalfEventCostsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var shelob = scn.GetShadowCard("shelob");
		var late = scn.GetFreepsCard("late");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveCardsToSupportArea(nothing);
		scn.MoveCardsToHand(late);
		scn.MoveCardsToDiscard(bard);

		scn.StartGame();
		// No ally in the draw deck: the discard pile is the one place to play from.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop"));
		// A [gollum] minion who is no Gollum.
		scn.MoveMinionsToTable(shelob);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, nothing);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertInZone(Zone.DISCARD, bard);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(late));

		scn.FreepsPlayCard(late);
		// Bard in the discard pile is the one card the event could play, so that pile, and he, come unasked
		// (F-0744).

		assertInZone(Zone.DISCARD, late);
		assertInZone(Zone.SUPPORT, bard);
		// The same reading as before the play, and it is the check: were the burden charged whether or not
		// Gollum is in play, this would read 1.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheEndOfTheShadowPhaseDiscardsAnOrcToPlayGollumFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var gollum = scn.GetShadowCard("gollum");
		var wraith = scn.GetShadowCard("wraith");
		var shelob = scn.GetShadowCard("shelob");
		var aggressor = scn.GetShadowCard("aggressor");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, nothing);
		scn.FreepsPass();
		// A Gollum in each pile, so that the player chooses which; Shelob in the deck, which is no Gollum.
		scn.SetShadowDrawDeck(gollum, shelob);
		scn.MoveCardsToDiscard(wraith);
		scn.MoveMinionsToTable(aggressor, troop, troll);
		// At site 2 both Gollums roam: 2 + 2.  Enough for Shelob too.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, nothing);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, troop, troll);
		assertInZone(Zone.DECK, gollum, shelob);
		assertInZone(Zone.DISCARD, wraith);
		int shuffles = scn.GetShadowDeckShuffleCount();

		// Not an action of the phase: it waits for the phase's end.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nothing));
		scn.ShadowPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nothing));
		scn.ShadowAcceptOptionalTrigger();
		// Orcs of two cultures; not a minion who is no Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll));
		scn.ShadowChooseCard(troop);
		assertInZone(Zone.DISCARD, troop);

		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob));
		scn.ShadowChooseCardBPFromSelection(gollum);

		assertInZone(Zone.SHADOW_CHARACTERS, gollum, aggressor, troll);
		assertInZone(Zone.DECK, shelob);
		assertInZone(Zone.DISCARD, wraith, troop);
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void PlaysAGollumOfAnotherCultureFromTheDiscardPileWithNoShuffle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var gollum = scn.GetShadowCard("gollum");
		var wraith = scn.GetShadowCard("wraith");
		var shelob = scn.GetShadowCard("shelob");
		var aggressor = scn.GetShadowCard("aggressor");
		var stinker = scn.GetShadowCard("stinker");
		var freepsDeceiver = scn.GetFreepsCard("deceiver");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, nothing);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(gollum);
		// Gollums of two cultures in the discard pile, Shelob in the same pile; a Gollum in the other player's own
		// discard pile, of a blueprint this player's piles do not hold.
		scn.MoveCardsToDiscard(wraith, stinker, shelob, freepsDeceiver);
		scn.MoveMinionsToTable(aggressor);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DECK, gollum);
		assertInZone(Zone.DISCARD, wraith, stinker, shelob, freepsDeceiver);
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPass();

		scn.ShadowAcceptOptionalTrigger();
		// The one Orc is discarded unasked.
		assertInZone(Zone.DISCARD, aggressor);
		scn.ShadowChoose("discard pile");
		assertTrue(scn.ShadowHasCardChoiceAvailable(wraith, stinker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob, freepsDeceiver));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(wraith);

		assertInZone(Zone.SHADOW_CHARACTERS, wraith);
		assertInZone(Zone.DECK, gollum);
		assertInZone(Zone.DISCARD, stinker, shelob, freepsDeceiver, aggressor);
		// No draw deck was looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithGollumOnlyInTheDrawDeckItIsPlayedFromThereUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var gollum = scn.GetShadowCard("gollum");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, nothing);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(gollum);
		scn.MoveMinionsToTable(aggressor);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DECK, gollum);
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowPass();

		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, aggressor);
		// No choice of pile: the discard pile holds no Gollum.
		assertFalse(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(gollum);

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithGollumOnlyInTheDiscardPileItIsPlayedFromThereUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var wraith = scn.GetShadowCard("wraith");
		var aggressor = scn.GetShadowCard("aggressor");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, nothing);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(troll);
		scn.MoveCardsToDiscard(wraith);
		scn.MoveMinionsToTable(aggressor);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DISCARD, wraith);
		assertInZone(Zone.DECK, troll);
		scn.ShadowPass();

		scn.ShadowAcceptOptionalTrigger();

		// No choice of pile, the draw deck holding no Gollum, and the one Gollum there is played unasked.
		assertInZone(Zone.DISCARD, aggressor);
		assertInZone(Zone.SHADOW_CHARACTERS, wraith);
		assertInZone(Zone.DECK, troll);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DeclinedItLeavesEverythingAsItWasAndIsNotOfferedAtTheEndOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var gollum = scn.GetShadowCard("gollum");
		var aggressor = scn.GetShadowCard("aggressor");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, nothing);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(gollum);
		scn.MoveMinionsToTable(aggressor, troop);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, troop);
		assertInZone(Zone.DECK, gollum);
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nothing));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, troop);
		assertInZone(Zone.DECK, gollum);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The end of the next phase does not set it off.
		scn.PassCurrentPhaseActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nothing));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInZone(Zone.DECK, gollum);
	}

	@Test
	public void IsNotOfferedWithNoOrcToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var gollum = scn.GetShadowCard("gollum");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, nothing);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(gollum);
		scn.MoveMinionsToTable(troll);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertInZone(Zone.DECK, gollum);
		scn.ShadowPass();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nothing));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoGollumInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var shelob = scn.GetShadowCard("shelob");
		var aggressor = scn.GetShadowCard("aggressor");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, nothing);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(troll);
		scn.MoveCardsToDiscard(shelob);
		scn.MoveMinionsToTable(aggressor);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DECK, troll);
		assertInZone(Zone.DISCARD, shelob);
		scn.ShadowPass();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nothing));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileGollumIsInPlayAndAnotherCopyIsOnlyInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nothing = scn.GetShadowCard("nothing");
		var gollum = scn.GetShadowCard("gollum");
		var wraith = scn.GetShadowCard("wraith");
		var aggressor = scn.GetShadowCard("aggressor");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(nothing);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, nothing);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(troll);
		scn.MoveCardsToDiscard(wraith);
		// Gollum is unique: the one in the discard pile cannot be played while this one is in play.
		scn.MoveMinionsToTable(aggressor, gollum);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, gollum);
		assertInZone(Zone.DECK, troll);
		assertInZone(Zone.DISCARD, wraith);
		scn.ShadowPass();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nothing));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
