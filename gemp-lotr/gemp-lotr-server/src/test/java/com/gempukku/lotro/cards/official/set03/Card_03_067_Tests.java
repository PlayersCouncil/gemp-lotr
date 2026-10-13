package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("palantir", "3_67");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; Damage +1 is its whole text
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, cost 2, site 4; a regroup ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					// Saruman's Ambition: an [isengard] condition in the support area.  Its texts are about the cost
					// of [isengard] events and a skirmish.
					put("ambition", "1_133");

					// Six different cards for the Free Peoples hand, of several types; none is played here.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("sam", "1_311");        // Sam, Son of Hamfast
					put("guard", "1_7");        // Dwarf Guard
					put("armor", "1_8");        // Dwarven Armor, a possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePalantirofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Palantír of Orthanc
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Palantir
		 * Game Text: To play, spot an [isengard] minion. Plays to your support area.
		 * 	<b>Shadow:</b> Spot an [isengard] minion and remove (1) to reveal a card at random from the Free Peoples player's hand. Place that card on top of that player's draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("palantir");

		assertEquals("The Palantír of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PALANTIR));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PalantirRequires1IsengardMinionToPlayAnd1ToActivate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToHand(palantir, savage);
		// An [isengard] card that is no minion, and a minion that is not [isengard].
		scn.MoveCardsToSupportArea(ambition);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(savage));
		assertFalse(scn.ShadowPlayAvailable(palantir));

		scn.ShadowPlayCard(savage);
		// 2 to play + 2 roaming (a site 5 minion at site 2)
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(palantir));

		scn.ShadowPlayCard(palantir);
		// The Palantir costs nothing.
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, palantir);
		assertNotAttachedTo(palantir, savage);
		assertNotAttachedTo(palantir, troop);
		assertTrue(scn.ShadowActionAvailable(palantir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PalantirRemoves1ToRevealCardFromFreepsHandAndPlacesOnDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		// Six different cards, so that a card other than the one revealed is most often a different card.
		scn.MoveCardsToFreepsHand("aragorn", "legolas", "gimli", "sam", "guard", "armor");

		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(palantir);
		// The Shadow player's own hand and draw deck are not what the text names.
		scn.MoveCardsToShadowHand("aragorn", "legolas");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		var freepsDeck = scn.GetFreepsDeckCount();
		var shadowTop = scn.GetShadowTopOfDeck();
		assertTrue(scn.ShadowActionAvailable(palantir));

		scn.ShadowUseCardAction(palantir);
		assertEquals(2, scn.GetTwilight());
		// One card of the six is shown to the Shadow player, and nothing to the hand's owner.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		// Which card is shown is chance; what is read is which it was.
		var aragornShown = scn.ShadowHasCardChoiceAvailable(aragorn, false);
		var legolasShown = scn.ShadowHasCardChoiceAvailable(legolas, false);
		var gimliShown = scn.ShadowHasCardChoiceAvailable(gimli, false);
		var samShown = scn.ShadowHasCardChoiceAvailable(sam, false);
		var guardShown = scn.ShadowHasCardChoiceAvailable(guard, false);
		var armorShown = scn.ShadowHasCardChoiceAvailable(armor, false);
		scn.ShadowDismissRevealedCards();

		// The card placed on the deck is the card that was shown, whichever it was, and no other.
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(freepsDeck + 1, scn.GetFreepsDeckCount());
		var top = scn.GetFreepsTopOfDeck();
		if (aragornShown) assertSame(aragorn, top); else assertInHand(aragorn);
		if (legolasShown) assertSame(legolas, top); else assertInHand(legolas);
		if (gimliShown) assertSame(gimli, top); else assertInHand(gimli);
		if (samShown) assertSame(sam, top); else assertInHand(sam);
		if (guardShown) assertSame(guard, top); else assertInHand(guard);
		if (armorShown) assertSame(armor, top); else assertInHand(armor);
		// "That player's" draw deck: nothing of the Shadow player's has moved.
		assertEquals(2, scn.GetShadowHandCount());
		assertSame(shadowTop, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The text has no limit: a second and a third card follow for (1) each ...
		scn.ShadowUseCardAction(palantir);
		scn.ShadowDismissRevealedCards();
		scn.ShadowUseCardAction(palantir);
		scn.ShadowDismissRevealedCards();
		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(freepsDeck + 3, scn.GetFreepsDeckCount());
		// ... and the first card placed now lies third from the top.
		assertSame(top, scn.GetFromTopOfFreepsDeck(3));

		// With an empty pool there is nothing to remove, though the Uruk Savage is still there to spot and three
		// cards are still in that hand.
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowActionAvailable(palantir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilAnIsengardMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(guard);

		var palantir = scn.GetShadowCard("palantir");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		var ambition = scn.GetShadowCard("ambition");
		// The Palantir outlasts the minion it was played beside.
		scn.MoveCardsToSupportArea(palantir, ambition);
		scn.MoveCardsToHand(worker);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 companion + 2 for Ettenmoors: enough to remove (1).  The Palantir and the Ambition are
		// [isengard] and no minions; the Troop is a minion and not [isengard].
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(palantir));

		// An [isengard] Orc is as good as an Uruk-hai.
		scn.ShadowPlayCard(worker);
		// 2 to play + 2 roaming (a site 4 minion at site 2)
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(palantir));

		scn.ShadowUseCardAction(palantir);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, false));
		scn.ShadowDismissRevealedCards();
		assertSame(guard, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedBesideAnIsengardOrcForNothingToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(palantir);

		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		scn.FreepsPass();

		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(palantir));

		scn.ShadowPlayCard(palantir);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, palantir);
		assertNotAttachedTo(palantir, worker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnEmptyFreePeoplesHandTheTwilightIsRemovedAndNothingIsRevealedOrPlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(palantir);
		// The Shadow player holds cards; the Free Peoples player holds none.
		scn.MoveCardsToShadowHand("aragorn", "legolas");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		var freepsDeck = scn.GetFreepsDeckCount();
		var freepsTop = scn.GetFreepsTopOfDeck();
		var shadowTop = scn.GetShadowTopOfDeck();
		// The ability has a cost that can be paid, so it is offered all the same.
		assertTrue(scn.ShadowActionAvailable(palantir));

		scn.MarkGameLog();
		scn.ShadowUseCardAction(palantir);
		assertEquals(2, scn.GetTwilight());
		// Nobody is shown anything, and the log says why.
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.GameLogSinceMarkSays("no cards in " + scn.P1() + "'s hand"));
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertSame(freepsTop, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetShadowHandCount());
		assertSame(shadowTop, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
