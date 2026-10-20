package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_179_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scavengers", "1_179");
					put("scavengers2", "1_179");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1, site 4; adds (2), unasked, when played
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, no Orc (cheated in; its text is about its play)
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc; strength +4 and fierce only at 2 wounds on the Ring-bearer
					put("spear", "1_182");      // Goblin Spear: [moria] possession, hand weapon, cost 1, on a [moria] MINION
					put("bow", "33_40");        // Goblin Bow: [moria] possession, ranged weapon, cost 0, on a [moria] Orc
					put("warg", "32_32");       // Vicious Warg: [moria] possession, a mount and no weapon, cost 3, on an Orc
					put("axe", "1_190");        // Moria Axe: [moria] possession, hand weapon, cost 1, on a [moria] Orc
					put("goblinscimitar", "1_180"); // Goblin Scimitar: [moria] hand weapon, cost 0, on a [moria] Orc
					// Goblin Scimitar (The Hobbit): [moria] hand weapon, cost 0, on ANY Orc (to play, spot a [moria]
					// card); when played, its owner may draw a card.
					put("orcscimitar", "31_31");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinScavengersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Scavengers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may play a weapon from your discard pile on your [moria] Orc.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scavengers");

		assertEquals("Goblin Scavengers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMayPlayAWeaponFromItsOwnersDiscardPileOnAMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavengers = scn.GetShadowCard("scavengers");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		var spear = scn.GetShadowCard("spear");
		var bow = scn.GetShadowCard("bow");
		var warg = scn.GetShadowCard("warg");
		var axe = scn.GetShadowCard("axe");
		// A weapon in the other player's discard pile, of a title in none of the Shadow player's piles
		var freepsScimitar = scn.GetFreepsCard("goblinscimitar");
		scn.MoveCardsToHand(scavengers, axe);
		scn.MoveCardsToDiscard(spear, bow, warg, freepsScimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troll);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// 5 set + 1 companion + 2 for Ettenmoors
		assertEquals(8, scn.GetTwilight());
		scn.ShadowPlayCard(scavengers);

		// 8 - 3 for the Scavengers - 2 roaming (a site 4 minion at site 2): 3 left, enough for the Warg too.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavengers));
		scn.ShadowAcceptOptionalTrigger();

		// A hand weapon and a ranged weapon from his own discard pile; not the mount, not the Axe in his hand, not
		// the weapon in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, bow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warg, axe, freepsScimitar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(spear);

		// The Spear may be borne by any [moria] minion: the two [moria] Orcs are offered, the [moria] Troll is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scavengers, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll));
		scn.ShadowChooseCard(runner);

		assertAttachedTo(spear, runner);
		assertEquals(0, scn.GetAttachedCards(scavengers).size());
		assertInZone(Zone.DISCARD, bow, warg);
		assertInZone(Zone.HAND, axe);
		assertInZone(Zone.DISCARD, freepsScimitar);
		// 3 - 1 for the Spear
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcOfAnotherCultureIsNotOfferedAsBearerOfAWeaponHeCouldBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavengers = scn.GetShadowCard("scavengers");
		var runner = scn.GetShadowCard("runner");
		var hound = scn.GetShadowCard("hound");
		var orcscimitar = scn.GetShadowCard("orcscimitar");
		scn.MoveCardsToHand(scavengers);
		scn.MoveCardsToDiscard(orcscimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, hound);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(scavengers);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavengers));
		scn.ShadowAcceptOptionalTrigger();

		// This Goblin Scimitar may be borne by any Orc, the [wraith] Morgul Hound among them: only the text keeps
		// him out.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scavengers, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(scavengers);

		assertAttachedTo(orcscimitar, scavengers);
		assertEquals(0, scn.GetAttachedCards(hound).size());
		// The Scimitar's own "you may draw a card" as it is played
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(orcscimitar));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheWeaponInTheDiscardPileAndThePoolAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavengers = scn.GetShadowCard("scavengers");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(scavengers);
		scn.MoveCardsToDiscard(spear);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(scavengers);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavengers));

		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, spear);
		assertEquals(0, scn.GetAttachedCards(scavengers).size());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherOrcPlayedIsNoCauseAndAScavengersInPlayDoesNotAnswerTheSecondCopysPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavengers = scn.GetShadowCard("scavengers");
		var scavengers2 = scn.GetShadowCard("scavengers2");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(runner, scavengers2);
		scn.MoveCardsToDiscard(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(scavengers);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 companion + 2 for Ettenmoors
		assertEquals(11, scn.GetTwilight());
		scn.ShadowPlayCard(runner);
		// 11 - 1 - 2 roaming + 2 the Runner adds: nothing is offered for a [moria] Orc that is not this minion.
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(scavengers2);
		// 10 - 3 - 2 roaming: only the copy played makes the offer.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scavengers2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scavengers));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, spear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenThePoolCannotPayForAnyWeaponInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scavengers = scn.GetShadowCard("scavengers");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(scavengers);
		scn.MoveCardsToDiscard(spear);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 companion + 2 for Ettenmoors: exactly the Scavengers' 3 and its 2 roaming
		assertEquals(5, scn.GetTwilight());
		scn.ShadowPlayCard(scavengers);

		// Nothing left for the Spear's 1: playing it is the effect's price, and the offer is not made.
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, spear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
