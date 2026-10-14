package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_123_Tests
{

	// The Fellowship-block path: sites 4 (Balin's Tomb) and 5 (The Bridge of Khazad-dûm) are underground, sites 3
	// (Rivendell Valley) and 6 (Galadriel's Glade) are not.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hordes", "11_123");
					// Scurrying Goblin: an [orc] Orc, cost 1.  When played at an underground site beside another [orc]
					// minion, may add (2); played here away from one.
					put("goblin", "11_137");
					// Half-troll of Far Harad: an [orc] minion who is no Orc.  Its start-of-assignment trigger is
					// never reached.
					put("halftroll", "14_12");
					// Champion Orc: an [orc] Orc with toil 2 and no other text
					put("champion", "11_111");
					// The Beckoning Shadow: an [orc] condition, no minion.  Cheated into play (to play, exert an [orc]
					// minion); its other text needs 5 burdens.
					put("beckoning", "12_83");
					// Goblin Runner: a [moria] Orc, cheated into play so that its "may add (2)" is not asked
					put("runner", "1_178");
					// Denizen of Moria: an [orc] Orc, the first player's, in his own discard pile
					put("denizen", "11_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinHordesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Goblin Hordes
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [orc] minion.<br>Each time the fellowship moves from an underground site, you may take an [orc] minion from your discard pile into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hordes");

		assertEquals("Goblin Hordes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hordes = scn.GetShadowCard("hordes");
		var goblin = scn.GetShadowCard("goblin");
		var beckoning = scn.GetShadowCard("beckoning");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(hordes, goblin);

		scn.StartGame();
		// An [orc] card that is no minion, and an Orc minion of another culture.
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, beckoning);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.ShadowPlayAvailable(hordes));

		// The Scurrying Goblin, an [orc] Orc, is played, and the Hordes can be.
		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertTrue(scn.ShadowPlayAvailable(hordes));

		scn.ShadowPlayCard(hordes);
		assertInZone(Zone.SUPPORT, hordes);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcMinionWhoIsNoOrcIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hordes = scn.GetShadowCard("hordes");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(hordes);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.ShadowPlayAvailable(hordes));

		scn.ShadowPlayCard(hordes);
		assertInZone(Zone.SUPPORT, hordes);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveFromAnUndergroundSiteOffersAnOrcMinionFromHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hordes = scn.GetShadowCard("hordes");
		var halftroll = scn.GetShadowCard("halftroll");
		var goblin = scn.GetShadowCard("goblin");
		var champion = scn.GetShadowCard("champion");
		var beckoning = scn.GetShadowCard("beckoning");
		var runner = scn.GetShadowCard("runner");
		var denizen = scn.GetFreepsCard("denizen");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(hordes);
		// Three [orc] minions, an [orc] card that is no minion and a [moria] Orc in his discard pile; an [orc] Orc in
		// the first player's.
		scn.MoveCardsToDiscard(halftroll, goblin, champion, beckoning, runner, denizen);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertInZone(Zone.DISCARD, halftroll, goblin, champion, beckoning, runner, denizen);
		int hand = scn.GetShadowHandCount();

		// The fellowship moves from Balin's Tomb, site 4.
		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hordes));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(halftroll, goblin, champion));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(beckoning, runner, denizen));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(halftroll);

		assertInZone(Zone.HAND, halftroll);
		assertInZone(Zone.DISCARD, goblin, champion, beckoning, runner, denizen);
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second move in the turn, from The Bridge of Khazad-dûm, site 5: offered again.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hordes));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, champion));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(beckoning, runner, denizen));
		scn.ShadowChooseCardBPFromSelection(goblin);

		assertInZone(Zone.HAND, halftroll, goblin);
		assertInZone(Zone.DISCARD, champion, beckoning, runner, denizen);
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheDiscardPileAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hordes = scn.GetShadowCard("hordes");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(hordes);
		scn.MoveCardsToDiscard(halftroll);

		assertInZone(Zone.DISCARD, halftroll);
		int hand = scn.GetShadowHandCount();

		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hordes));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, halftroll);
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToAnUndergroundSiteOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hordes = scn.GetShadowCard("hordes");
		var halftroll = scn.GetShadowCard("halftroll");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(hordes);
		scn.MoveCardsToDiscard(halftroll, goblin);

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertInZone(Zone.DISCARD, halftroll, goblin);
		int hand = scn.GetShadowHandCount();

		// From Rivendell Valley, site 3, to Balin's Tomb, an underground site.
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, halftroll, goblin);
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOrcMinionInHisOwnDiscardPileNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hordes = scn.GetShadowCard("hordes");
		var beckoning = scn.GetShadowCard("beckoning");
		var runner = scn.GetShadowCard("runner");
		var denizen = scn.GetFreepsCard("denizen");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(hordes);
		// Only an [orc] card that is no minion and a [moria] Orc in his discard pile; an [orc] Orc in the other
		// player's.
		scn.MoveCardsToDiscard(beckoning, runner, denizen);

		assertInZone(Zone.DISCARD, beckoning, runner, denizen);
		int hand = scn.GetShadowHandCount();

		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, beckoning, runner, denizen);
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
