package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_027_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow phase
	// is at The Troll Hoard 30_50 (site 2), where the Patrol (site 6) roams.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol", "33_27");
					put("patrol2", "33_27");

					put("footman", "30_42");    // Goblin Footman: [moria] Orc; a "when you play" offer of a possession from the discard pile
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2; an Assignment ability not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc; a Shadow ability not used

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7; +1 for each [dwarven] follower (none laid)
					put("orcrist", "30_17");    // Orcrist: [dwarven] artifact hand weapon, strength +2, on Thorin; he is damage +1
					put("oakenshield", "31_6"); // Oakenshield: [dwarven] shield (no weapon), strength +1, on Thorin; a response not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability not used
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession hand weapon, strength +2, on a [dwarven] character
					put("axe2", "31_1");
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] ally, strength 7; "Thorin is strength +1"
					put("gandalf", "30_25");    // Gandalf, Leader of Dwarves: [gandalf] companion, strength 7; skirmish abilities not used
					put("glamdring", "30_29");  // Glamdring: [gandalf] artifact hand weapon, strength +2, on Gandalf
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void LaketownPatrolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Laketown Patrol
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Fierce.
		 * 	When you play this minion, you may place an Orc from your discard pile beneath your draw deck.
		 * 	Each [dwarven] companion loses all strength bonuses from weapons.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("patrol");

		assertEquals("Laketown Patrol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedYouMayPlaceOneOrcOfEitherCultureFromYourDiscardPileBeneathYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(patrol);
		scn.MoveCardsToDiscard(footman, aggressor, bats);

		scn.StartGame();
		assertInZone(Zone.DISCARD, footman, aggressor, bats);
		// Enough for the Patrol, roaming; not read.
		scn.SetTwilight(10);
		scn.FreepsPass();

		var deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(patrol);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(patrol));
		scn.ShadowAcceptOptionalTrigger();

		// "An Orc": one, of either culture; the Bats are no Orc.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		scn.ShadowChooseCards(aggressor);

		assertEquals(aggressor, scn.GetShadowBottomOfDeck());
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, footman, bats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheDiscardPileAndTheDrawDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(patrol);
		scn.MoveCardsToDiscard(footman, aggressor);

		scn.StartGame();
		assertInZone(Zone.DISCARD, footman, aggressor);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var deck = scn.GetShadowDeckCount();
		var bottom = scn.GetShadowBottomOfDeck();
		scn.ShadowPlayCard(patrol);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(patrol));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, footman, aggressor);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(bottom, scn.GetShadowBottomOfDeck());
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnswersItsOwnPlayOnlyAndNotAnotherOrcsOrAnotherCopys() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var patrol2 = scn.GetShadowCard("patrol2");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToHand(aggressor, patrol2);
		scn.MoveCardsToDiscard(footman);

		scn.StartGame();
		assertInZone(Zone.DISCARD, footman);
		scn.MoveMinionsToTable(patrol);
		// Enough for the Aggressor and the second Patrol, each roaming at site 2; not read.
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		// Another minion played: the Patrol in play makes no offer, though the Footman lies in the discard pile.
		scn.ShadowPlayCard(aggressor);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played: its own offer only.
		scn.ShadowPlayCard(patrol2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(patrol2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(patrol));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, footman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoOrcInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(patrol);
		scn.MoveCardsToDiscard(bats);

		scn.StartGame();
		assertInZone(Zone.DISCARD, bats);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(patrol);

		// Only the Bats, which is no Orc: nothing to place, and no offer.
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, bats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachDwarvenCompanionLosesItsStrengthBonusesFromWeaponsAndNoOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var oakenshield = scn.GetFreepsCard("oakenshield");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var dain = scn.GetFreepsCard("dain");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(thorin, balin, gandalf);
		scn.MoveAlliesToTable(dain);
		scn.AttachCardsTo(thorin, orcrist, oakenshield);
		scn.AttachCardsTo(balin, axe);
		scn.AttachCardsTo(dain, axe2);
		scn.AttachCardsTo(gandalf, glamdring);

		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCardsToHand(patrol);

		scn.StartGame();
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(oakenshield, thorin);
		assertAttachedTo(axe, balin);
		assertAttachedTo(axe2, dain);
		assertAttachedTo(glamdring, gandalf);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Thorin: 7 + 2 Orcrist + 1 Oakenshield + 1 Dain.  Thorin is damage +1, and +1 more from Orcrist.
		assertEquals(11, scn.GetStrength(thorin));
		assertEquals(2, scn.GetKeywordCount(thorin, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(balin));
		assertEquals(9, scn.GetStrength(dain));
		assertEquals(9, scn.GetStrength(gandalf));

		scn.ShadowPlayCard(patrol);
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);

		// Thorin keeps the shield's +1 and Dain's +1, and Orcrist's damage +1: 7 + 1 + 1.
		assertEquals(9, scn.GetStrength(thorin));
		assertEquals(2, scn.GetKeywordCount(thorin, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(balin));
		// An ally is no companion, and Gandalf is no [dwarven] companion.
		assertEquals(9, scn.GetStrength(dain));
		assertEquals(9, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
