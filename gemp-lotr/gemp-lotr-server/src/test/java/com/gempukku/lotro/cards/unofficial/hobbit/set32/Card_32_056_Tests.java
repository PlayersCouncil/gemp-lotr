package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_056_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  Everything happens on turn 1 at The Troll Hoard 30_50 (site 2): the minions are laid in its Shadow
	// phase, and the Maneuver ability is used in the maneuver phase that follows.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "32_56");
					// A second copy, for "this possession".
					put("blade2", "32_56");
					// Bearers and minions.
					put("lemenya", "32_62");     // Úlairë Lemenya, Revived: [wraith] Nazgûl, fierce; each [wraith] minion's site number -3; a trigger on a condition discarded, not set off
					put("brute", "7_188");       // Morgul Brute: [wraith] Orc, cost 2; a text on being played (cheated in) and a Skirmish ability, not used
					put("khamul", "103_61");     // Úlairë Attëa, Khamûl of the East: [raider] Nazgûl; Easterling, fierce, texts on burdens (none)
					put("sentinel", "33_49");    // Orc Sentinel: [sauron] Orc; [sauron] Orcs' site number -1, a Shadow ability, not used
					put("bats", "32_30");        // Dark Bats: [moria] Creature, no Orc; a Shadow ability, not used
					// Morgul Axe: [wraith] hand weapon, cost 1, bearer must be a [wraith] Orc; a damage bonus on 2 wounds on the Ring-bearer.
					put("axe", "7_186");
					// [wraith] cards for the piles: a character, a card that is neither character nor item.
					put("nertea", "32_64");      // Úlairë Nertëa, Revived: [wraith] Nazgûl, cost 4
					put("summoning", "32_58");   // He Is Summoning His Servants: unique [wraith] condition, cost 2; [wraith] minions +2 (no Wise characters here), a Shadow ability, not used
					put("summoning2", "32_58");
					// A card of another culture.
					put("jail", "32_39");        // Jail: [sauron] condition, cost 2; a Regroup ability, not used
					put("jail2", "32_39");
					// A [wraith] card for the other player's own discard pile, of a blueprint this player's piles do not hold.
					put("cantea", "32_60");      // Úlairë Cantëa, Revived: [wraith] Nazgûl
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DolGuldurBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Dol Guldur Blade
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [wraith] minion.<br>When you play this possession, add (2).<br><b>Maneuver:</b>
		 * Discard an Orc from play to play a [wraith] card from your draw deck or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("Dol Guldur Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAWraithMinionAndAdds2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var blade2 = scn.GetShadowCard("blade2");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var brute = scn.GetShadowCard("brute");
		var khamul = scn.GetShadowCard("khamul");
		var sentinel = scn.GetShadowCard("sentinel");
		var axe = scn.GetShadowCard("axe");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya, nertea, brute, khamul, sentinel);
		scn.SetShadowHand(blade, blade2, axe);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, nertea, brute, khamul, sentinel);
		assertInHand(blade, blade2, axe);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(blade));

		scn.ShadowPlayCard(blade);
		// [wraith] minions of two races; not a Nazgûl of another culture, nor an Orc of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nertea, brute));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, sentinel));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertAttachedTo(blade, lemenya);
		// 5 - 0 to play + 2
		assertEquals(7, scn.GetTwilight());

		// Another [wraith] hand weapon played afterwards adds nothing: only its own cost is paid.
		scn.ShadowPlayCard(axe);
		assertAttachedTo(axe, brute);
		// 7 - 1
		assertEquals(6, scn.GetTwilight());

		// The second copy adds its own (2), and the first copy adds nothing for it.
		scn.ShadowPlayCard(blade2);
		assertAttachedTo(blade2, nertea);
		// 6 - 0 + 2
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverActionDiscardsAnOrcToPlayAWraithCardFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var lemenya = scn.GetShadowCard("lemenya");
		var brute = scn.GetShadowCard("brute");
		var sentinel = scn.GetShadowCard("sentinel");
		var bats = scn.GetShadowCard("bats");
		var axe = scn.GetShadowCard("axe");
		var nertea = scn.GetShadowCard("nertea");
		var summoning = scn.GetShadowCard("summoning");
		var summoning2 = scn.GetShadowCard("summoning2");
		var jail = scn.GetShadowCard("jail");
		var cantea = scn.GetFreepsCard("cantea");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya, brute, sentinel, bats);
		scn.AttachCardsTo(lemenya, blade);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(summoning);
		scn.MoveCardsToDiscard(axe, nertea, summoning2, jail, cantea);
		assertAttachedTo(blade, lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, brute, sentinel, bats);
		assertInZone(Zone.DECK, summoning);
		assertInZone(Zone.DISCARD, axe, nertea, summoning2, jail, cantea);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(blade));

		scn.ShadowUseCardAction(blade);
		// Orcs of two cultures; not the Nazgûl, nor the Bats, a Creature.
		assertTrue(scn.ShadowHasCardChoiceAvailable(brute, sentinel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, bats));
		scn.ShadowChooseCard(sentinel);
		assertInZone(Zone.DISCARD, sentinel);

		// A [wraith] card in each pile: the player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");
		// A character, an item and a condition, all [wraith]; not a [sauron] card, nor the other player's [wraith]
		// card in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nertea, axe, summoning2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(jail, cantea));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nertea);

		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertInZone(Zone.DISCARD, axe, summoning2, jail, cantea);
		assertInZone(Zone.DECK, summoning);
		// No deck was looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionPlaysAWraithCardFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var lemenya = scn.GetShadowCard("lemenya");
		var brute = scn.GetShadowCard("brute");
		var summoning = scn.GetShadowCard("summoning");
		var summoning2 = scn.GetShadowCard("summoning2");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya, brute);
		scn.AttachCardsTo(lemenya, blade);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(summoning, jail);
		scn.MoveCardsToDiscard(summoning2);
		assertAttachedTo(blade, lemenya);
		assertInZone(Zone.DECK, summoning, jail);
		assertInZone(Zone.DISCARD, summoning2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(blade));

		scn.ShadowUseCardAction(blade);
		// The one Orc is discarded unasked.
		assertInZone(Zone.DISCARD, brute);
		scn.ShadowChoose("draw deck");
		scn.DismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(summoning));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(jail));
		scn.ShadowChooseCardBPFromSelection(summoning);

		assertInZone(Zone.SUPPORT, summoning);
		assertInZone(Zone.DECK, jail);
		assertInZone(Zone.DISCARD, summoning2);
		// One shuffle for the card leaving the deck that was looked through, one for the look itself.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AWraithOrcDiscardedForTheCostIsNotPlayedBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var lemenya = scn.GetShadowCard("lemenya");
		var brute = scn.GetShadowCard("brute");
		var nertea = scn.GetShadowCard("nertea");
		var summoning = scn.GetShadowCard("summoning");
		var summoning2 = scn.GetShadowCard("summoning2");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya, brute);
		// The Blade on the Brute, so that the Orc discarded for the cost takes the Blade with it.
		scn.AttachCardsTo(brute, blade);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(summoning);
		scn.MoveCardsToDiscard(nertea, summoning2);
		assertAttachedTo(blade, brute);
		assertInZone(Zone.SHADOW_CHARACTERS, brute);
		assertInZone(Zone.DISCARD, nertea, summoning2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(blade);
		assertInZone(Zone.DISCARD, brute, blade);
		scn.ShadowChoose("discard pile");
		// The Brute and the Blade, [wraith] cards, were not in the discard pile when the action began.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nertea, summoning2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brute, blade));
		scn.ShadowChooseCard(nertea);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithNoOrcInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var lemenya = scn.GetShadowCard("lemenya");
		var bats = scn.GetShadowCard("bats");
		var nertea = scn.GetShadowCard("nertea");
		var summoning = scn.GetShadowCard("summoning");

		scn.StartGame();
		scn.FreepsPass();
		// A Nazgûl and a Creature; a [wraith] card in each pile.
		scn.MoveMinionsToTable(lemenya, bats);
		scn.AttachCardsTo(lemenya, blade);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(summoning);
		scn.MoveCardsToDiscard(nertea);
		assertAttachedTo(blade, lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, bats);
		assertInZone(Zone.DECK, summoning);
		assertInZone(Zone.DISCARD, nertea);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blade));
	}

	@Test
	public void ManeuverActionIsNotOfferedWithNoWraithCardInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var lemenya = scn.GetShadowCard("lemenya");
		var sentinel = scn.GetShadowCard("sentinel");
		var jail = scn.GetShadowCard("jail");
		var jail2 = scn.GetShadowCard("jail2");
		var cantea = scn.GetFreepsCard("cantea");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya, sentinel);
		scn.AttachCardsTo(lemenya, blade);
		// [sauron] cards in this player's piles, and a [wraith] card in the other player's own discard pile.
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(jail);
		scn.MoveCardsToDiscard(jail2, cantea);
		assertAttachedTo(blade, lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel);
		assertInZone(Zone.DECK, jail);
		assertInZone(Zone.DISCARD, jail2, cantea);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blade));
	}

	@Test
	public void ManeuverActionIsNotOfferedWithOneTwilightTooFewForTheOnlyWraithCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var lemenya = scn.GetShadowCard("lemenya");
		var sentinel = scn.GetShadowCard("sentinel");
		var nertea = scn.GetShadowCard("nertea");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya, sentinel);
		scn.AttachCardsTo(lemenya, blade);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(jail);
		scn.MoveCardsToDiscard(nertea);
		assertAttachedTo(blade, lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel);
		assertInZone(Zone.DECK, jail);
		assertInZone(Zone.DISCARD, nertea);

		scn.SkipToPhase(Phase.MANEUVER);
		// Nertëa costs 4, and her site number of 5 - 3 does not make her roam at site 2.
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blade));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(blade));
	}
}
