package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("invaders", "6_81");
					put("invaders2", "6_81");

					put("desert", "4_220");     // Desert Soldier: another Southron to spot; his trigger is at the start of a skirmish
					put("soldier", "4_254");    // Southron Soldier: [raider] Southron Man, ambush (2), cost 2, no other text
					put("dunlending", "11_79"); // Fearsome Dunlending: [men] Man, ambush (1), cost 3
					put("horror", "14_13");     // Horror of Harad: [orc] Half-troll, ambush (1), cost 4
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no Southron and has no ambush, cost 2
					put("scout", "4_252");      // Southron Scout: an ambush minion kept in hand, cost 1
					put("warrior", "11_103");   // Warrior of Dunland: an ambush minion; only the Free Peoples player's copy is used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronInvadersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Southron Invaders
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (1)</b>.<br>When you play this minion, you may spot another Southron to play a minion with ambush from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("invaders");

		assertEquals("Southron Invaders", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedWithAnotherSouthronItMayPlayAMinionWithAmbushFromItsOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invaders = scn.GetShadowCard("invaders");
		var desert = scn.GetShadowCard("desert");
		var soldier = scn.GetShadowCard("soldier");
		var dunlending = scn.GetShadowCard("dunlending");
		var horror = scn.GetShadowCard("horror");
		var axeman = scn.GetShadowCard("axeman");
		var scout = scn.GetShadowCard("scout");
		var warrior = scn.GetFreepsCard("warrior");
		scn.MoveCardsToHand(invaders, scout);
		scn.MoveCardsToDiscard(soldier, dunlending, horror, axeman, warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(desert);
		assertInDiscard(soldier, dunlending, horror, axeman, warrior);
		assertInHand(invaders, scout);
		scn.FreepsPass();
		// At site 2 every minion here is roaming: Invaders 5 + 2, then 6 left, enough for the dearest
		// candidate (Horror of Harad, 4 + 2) and for the Axeman (2 + 2).
		scn.SetTwilight(13);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(invaders);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invaders));
		scn.ShadowAcceptOptionalTrigger();

		// Minions with ambush of three cultures and two races in his own discard pile; not a minion with no
		// ambush there, one in his hand, or one in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, dunlending, horror));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, scout, warrior));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(soldier);

		assertInPlay(soldier);
		assertInDiscard(dunlending, horror, axeman, warrior);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherSouthronToSpotItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invaders = scn.GetShadowCard("invaders");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(invaders);
		scn.MoveCardsToDiscard(soldier);

		scn.StartGame();
		// A [raider] Man who is no Southron; Invaders is a Southron itself, but not "another".
		scn.MoveMinionsToTable(axeman);
		assertInDiscard(soldier);
		scn.FreepsPass();
		scn.SetTwilight(13);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(invaders);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(invaders));
		assertInDiscard(soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherSouthronPlayedIsNoCauseAndACopyInPlayDoesNotAnswerTheSecondCopysPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invaders = scn.GetShadowCard("invaders");
		var invaders2 = scn.GetShadowCard("invaders2");
		var soldier = scn.GetShadowCard("soldier");
		var dunlending = scn.GetShadowCard("dunlending");
		scn.MoveCardsToHand(invaders2, soldier);
		scn.MoveCardsToDiscard(dunlending);

		scn.StartGame();
		scn.MoveMinionsToTable(invaders);
		assertInDiscard(dunlending);
		assertInHand(invaders2, soldier);
		scn.FreepsPass();
		// Soldier 2 + 2, Invaders 5 + 2, and 5 left for the Dunlending (3 + 2).
		scn.SetTwilight(16);
		scn.ShadowRefreshDecision();

		// A Southron played while Invaders is in play, with a Southron to spot and a minion with ambush that
		// could be paid for in the pile.
		scn.ShadowPlayCard(soldier);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(invaders));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(invaders2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invaders2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(invaders));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(dunlending);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheMinionInTheDiscardPileAndThePoolAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invaders = scn.GetShadowCard("invaders");
		var desert = scn.GetShadowCard("desert");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(invaders);
		scn.MoveCardsToDiscard(soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(desert);
		assertInDiscard(soldier);
		scn.FreepsPass();
		// Invaders 5 + 2, and exactly the Soldier's 2 + 2 left.
		scn.SetTwilight(11);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(invaders);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invaders));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(soldier);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenThePoolCannotPayForTheMinionWithAmbush() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invaders = scn.GetShadowCard("invaders");
		var desert = scn.GetShadowCard("desert");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(invaders);
		scn.MoveCardsToDiscard(soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(desert);
		assertInDiscard(soldier);
		scn.FreepsPass();
		// Invaders 5 + 2, and 3 left: one short of the Soldier's 2 + 2.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(invaders);
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(invaders));
		assertInDiscard(soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
