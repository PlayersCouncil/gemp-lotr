package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_014_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Turn 1's skirmish
	// is at The Troll Hoard (site 2), whose only text is a start-of-fellowship offer; Bilbo skirmishes a Goblin
	// Runner.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eagles", "31_14");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion; a discard bar that needs 2 [dwarven]
					// followers, a discount on Thorin and a fellowship ability (and no trigger on [gandalf] events)
					put("gandalf", "30_26");
					// Ent Avenger: a [gandalf] companion who is no Gandalf; strength +2 while 3 Ents are spotted
					put("avenger", "6_27");
					// Thranduil, Elven King: [elven] Elf ally; a maneuver ability that wants Thorin or a [dwarven] artifact
					put("thranduil", "31_12");
					// Radagast, Tender of Beasts: [gandalf] follower, cost 2, Aid (2); a bar on possession bonuses that
					// wants 2 [gandalf] companions
					put("radagast", "13_38");
					// Gwaihir, Lord of the Eagles: [gandalf] Eagle follower, cost 1; a skirmish ability while attached
					put("gwaihir", "31_15");
					// Pallando, Far-travelling One: [gandalf] follower, cost 2; texts on his transfer and his bearer's win
					put("pallando", "13_37");
					// Meneldor, Misty Mountain Eagle: [gandalf] follower, Aid (1); bearer is strength +1 for each other
					// [gandalf] follower
					put("meneldor", "17_22");
					// Alatar, Final Envoy: [gandalf] follower, borne by a companion here; texts on his transfer and his
					// bearer's skirmishes
					put("alatar", "13_28");
					// Dori, Really a decent fellow: [dwarven] follower; bearer is hard to overwhelm
					put("dori", "30_8");
					// Bombur, Chief Cook: [dwarven] follower; bearer is damage +1
					put("bombur", "30_7");
					// Glamdring: [gandalf] artifact, hand weapon, bearer must be Gandalf: a [gandalf] card that is no
					// follower and could be played
					put("glamdring", "30_29");
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, 5/1; a "when you play" trigger, not reached
					put("card1", "1_177");      // Goblin Patrol Troop: a card with no text, to fill a draw deck
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheEaglesAreComingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: The Eagles Are Coming
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>.<br>Exert Gandalf to play a [gandalf] follower from your draw deck or discard pile.<br>You may attach a [gandalf] follower from your support area to a companion (without paying the aid cost).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eagles");

		assertEquals("The Eagles Are Coming", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfToPlayAFollowerFromTheDrawDeckAndAttachesOneToACompanionWithoutItsAid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eagles = scn.GetFreepsCard("eagles");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var thranduil = scn.GetFreepsCard("thranduil");
		var radagast = scn.GetFreepsCard("radagast");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var pallando = scn.GetFreepsCard("pallando");
		var meneldor = scn.GetFreepsCard("meneldor");
		var alatar = scn.GetFreepsCard("alatar");
		var dori = scn.GetFreepsCard("dori");
		var bombur = scn.GetFreepsCard("bombur");
		var glamdring = scn.GetFreepsCard("glamdring");
		var bilbo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(thranduil);
		scn.StartGame();
		scn.SetFreepsDrawDeck(radagast, gwaihir, dori, glamdring, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(pallando);
		scn.MoveCardsToSupportArea(meneldor, bombur);
		scn.AttachCardsTo(avenger, alatar);
		scn.MoveCardsToHand(eagles);
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.DECK, radagast, gwaihir, dori, glamdring);
		assertInZone(Zone.DISCARD, pallando);
		assertInZone(Zone.SUPPORT, meneldor, bombur);
		assertAttachedTo(alatar, avenger);
		assertInHand(eagles);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		int pool = scn.GetTwilight();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(eagles));
		scn.FreepsPlayCard(eagles);

		// The one Gandalf exerts unasked; Ent Avenger, a [gandalf] companion of another title, does not.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// Pallando lies in the discard pile, so either pile can be played from.
		assertTrue(scn.FreepsChoiceAvailable("draw deck"));
		assertTrue(scn.FreepsChoiceAvailable("discard pile"));
		scn.FreepsChoose("draw deck");
		scn.FreepsDismissRevealedCards();
		// [gandalf] followers of two kinds; not a [dwarven] follower, nor a [gandalf] card that is no follower.
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, gwaihir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dori, glamdring));
		// A play from a draw deck is chosen from 0 (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(radagast);

		assertInZone(Zone.SUPPORT, radagast);
		assertInZone(Zone.DECK, gwaihir, dori, glamdring);
		assertInZone(Zone.DISCARD, pallando);
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());

		// The attach: the follower just played and another [gandalf] follower in the support area; not a
		// [dwarven] follower there, nor a [gandalf] follower already borne.
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, meneldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bombur, alatar));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(radagast);
		// Companions, skirmishing or not; not an ally, nor a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, gandalf, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thranduil, runner));
		scn.FreepsChooseCard(bilbo);

		assertAttachedTo(radagast, bilbo);
		assertInZone(Zone.SUPPORT, meneldor, bombur);
		assertAttachedTo(alatar, avenger);
		assertInDiscard(eagles);
		// 3 for the event and 2 for Radagast; his Aid (2) is not paid.
		assertEquals(pool + 3 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlaysAFollowerFromTheDiscardPileWithNoShuffleAndTheAttachMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eagles = scn.GetFreepsCard("eagles");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var pallando = scn.GetFreepsCard("pallando");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var dori = scn.GetFreepsCard("dori");
		var glamdring = scn.GetFreepsCard("glamdring");
		var bilbo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf);
		scn.StartGame();
		scn.SetFreepsDrawDeck(radagast, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(pallando, gwaihir, dori, glamdring);
		scn.MoveCardsToHand(eagles);
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.DECK, radagast);
		assertInZone(Zone.DISCARD, pallando, gwaihir, dori, glamdring);
		assertInHand(eagles);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsPlayCard(eagles);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.FreepsChoose("discard pile");
		assertTrue(scn.FreepsHasCardChoiceAvailable(pallando, gwaihir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dori, glamdring));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(pallando);

		assertInZone(Zone.SUPPORT, pallando);
		assertInZone(Zone.DISCARD, gwaihir, dori, glamdring);
		assertInZone(Zone.DECK, radagast);
		// No draw deck was looked through.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		scn.FreepsChooseNo();
		assertInZone(Zone.SUPPORT, pallando);
		assertTrue(scn.GetAttachedCards(bilbo).isEmpty());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNothingToPlayInTheDrawDeckTheDiscardPileIsPlayedFromUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eagles = scn.GetFreepsCard("eagles");
		var gandalf = scn.GetFreepsCard("gandalf");
		var pallando = scn.GetFreepsCard("pallando");
		var dori = scn.GetFreepsCard("dori");
		var glamdring = scn.GetFreepsCard("glamdring");
		var bilbo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf);
		scn.StartGame();
		scn.SetFreepsDrawDeck(dori, glamdring, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(pallando);
		scn.MoveCardsToHand(eagles);
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.DECK, dori, glamdring);
		assertInZone(Zone.DISCARD, pallando);
		assertInHand(eagles);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		scn.FreepsPlayCard(eagles);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// Not asked which pile: the discard pile's one [gandalf] follower is played unasked.
		assertFalse(scn.FreepsChoiceAvailable("draw deck"));
		assertInZone(Zone.SUPPORT, pallando);
		assertInZone(Zone.DECK, dori, glamdring);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to attach"));
	}

	@Test
	public void CannotBePlayedWhileGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eagles = scn.GetFreepsCard("eagles");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var radagast = scn.GetFreepsCard("radagast");
		var pallando = scn.GetFreepsCard("pallando");
		var bilbo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf, avenger);
		// Vitality 4 with 3 wounds: Gandalf cannot exert. Ent Avenger could, and does not pay.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.StartGame();
		scn.SetFreepsDrawDeck(radagast, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(pallando);
		scn.MoveCardsToHand(eagles);
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.DECK, radagast);
		assertInZone(Zone.DISCARD, pallando);
		assertInHand(eagles);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(eagles));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
	}

	@Test
	public void CannotBePlayedWithNoGandalfFollowerInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eagles = scn.GetFreepsCard("eagles");
		var gandalf = scn.GetFreepsCard("gandalf");
		var meneldor = scn.GetFreepsCard("meneldor");
		var dori = scn.GetFreepsCard("dori");
		var bombur = scn.GetFreepsCard("bombur");
		var glamdring = scn.GetFreepsCard("glamdring");
		var bilbo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf);
		scn.StartGame();
		// A [dwarven] follower and a [gandalf] card that is no follower in each pile; a [gandalf] follower in the
		// support area, which the attach could reach.
		scn.SetFreepsDrawDeck(dori, glamdring, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(bombur);
		scn.MoveCardsToSupportArea(meneldor);
		scn.MoveCardsToHand(eagles);
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.DECK, dori, glamdring);
		assertInZone(Zone.DISCARD, bombur);
		assertInZone(Zone.SUPPORT, meneldor);
		assertInHand(eagles);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(eagles));
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}
}
