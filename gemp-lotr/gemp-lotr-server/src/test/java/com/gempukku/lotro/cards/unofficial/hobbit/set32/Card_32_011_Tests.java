package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_011_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Site 5 is Mirkwood
	// (Shadow number 6), site 6 Esgaroth (3, a sanctuary that offers an ally at the start of the regroup phase),
	// site 7 Erebor (6), site 8 Smaug's Den (8) and site 9 Battle of the Five Armies (9).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("key", "32_11");

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, vitality 4; a Fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability not used
					put("dain", "32_2");        // Dain Ironfoot: a [dwarven] ally; Thorin is strength +1, a start-of-regroup offer that needs a follower

					put("bats", "32_30");       // Dark Bats: [moria] minion, a Creature; a Shadow ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc; an Assignment ability not used
					put("sack", "31_57");       // Caught In A Sack: a [troll] condition in the support area; no Troll, and Bilbo wins nothing
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThrorsKeyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Thrór's Key
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Game Text: Bearer must be a [dwarven] companion.<br>Each time the fellowship moves from site 6 or 7, remove (2).<br><b>Regroup:</b> If the fellowship is at site 6 or lower, exert bearer or discard this possession to discard a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("key");

		assertEquals("Thrór's Key", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnADwarvenCompanionAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var key = scn.GetFreepsCard("key");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToHand(key);

		scn.StartGame();
		assertInHand(key);
		assertInZone(Zone.SUPPORT, dain);

		assertTrue(scn.FreepsPlayAvailable(key));
		scn.FreepsPlayCard(key);
		// Two [dwarven] companions; not Bilbo, a [shire] companion, nor Dain, a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);

		assertAttachedTo(key, balin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveFromSite6OrSite7RemovesTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var key = scn.GetFreepsCard("key");
		var thorin = scn.GetFreepsCard("thorin");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, key);
		assertAttachedTo(key, thorin);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		assertEquals(6, scn.GetCurrentSiteNumber());

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		// 3 set - 2 for the move from site 6 + 6 for Erebor + 2 companions (Bilbo and Thorin).
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minion, so no skirmish: on to the regroup phase at Erebor, and a second move from there.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(7, scn.GetCurrentSiteNumber());
		int pool = scn.GetTwilight();
		scn.PassRegroupActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		assertEquals(8, scn.GetCurrentSiteNumber());
		// The pool at the regroup move - 2 for the move from site 7 + 8 for Smaug's Den + 2 companions.
		assertEquals(pool - 2 + 8 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoveFromSite5RemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var key = scn.GetFreepsCard("key");
		var thorin = scn.GetFreepsCard("thorin");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, key);
		assertAttachedTo(key, thorin);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		// 3 set + 3 for Esgaroth + 2 companions, nothing removed.
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoveFromSite8RemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var key = scn.GetFreepsCard("key");
		var thorin = scn.GetFreepsCard("thorin");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, key);
		assertAttachedTo(key, thorin);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		// 3 set + 9 for Battle of the Five Armies + 2 companions, nothing removed.
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAtSite6ExertsBearerToDiscardAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var key = scn.GetFreepsCard("key");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bats = scn.GetShadowCard("bats");
		var aggressor = scn.GetShadowCard("aggressor");
		var sack = scn.GetShadowCard("sack");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, key);
		assertAttachedTo(key, thorin);
		// Dáin, a [dwarven] ally, laid in the draw deck: Esgaroth's start-of-regroup offer is made only with such an
		// ally there (F-0804).
		scn.MoveCardsToBottomOfDeck(scn.GetFreepsCard("dain"));
		scn.FreepsPass();
		// The Shadow phase at Esgaroth (site 6): the minions live through an assignment phase in which nobody is
		// assigned.
		scn.MoveMinionsToTable(bats, aggressor);
		scn.MoveCardsToSupportArea(sack);
		assertInZone(Zone.SHADOW_CHARACTERS, bats, aggressor);
		assertInZone(Zone.SUPPORT, sack);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(6, scn.GetCurrentSiteNumber());
		// Esgaroth's start-of-regroup offer, declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));

		assertTrue(scn.FreepsActionAvailable(key));
		scn.FreepsUseCardAction(key);
		// Both costs can be paid, so he is asked which.
		assertEquals(List.of("Exert bearer", "Discard this possession"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert bearer");
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertAttachedTo(key, thorin);

		// Minions of two cultures and races; not the Shadow condition, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bats, aggressor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sack, thorin, balin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aggressor);

		assertInDiscard(aggressor);
		assertInZone(Zone.SHADOW_CHARACTERS, bats);
		assertInZone(Zone.SUPPORT, sack);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedAtSite7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var key = scn.GetFreepsCard("key");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, key);
		assertAttachedTo(key, thorin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		assertInZone(Zone.SHADOW_CHARACTERS, bats);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(7, scn.GetCurrentSiteNumber());
		// Thorin can exert, and there is a minion to discard.
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertInZone(Zone.SHADOW_CHARACTERS, bats);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(key));
	}

	@Test
	public void AtSite2WithBearerExhaustedTheKeyIsDiscardedToDiscardAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var key = scn.GetFreepsCard("key");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, key);
		// Vitality 4 with 3 wounds: Thorin cannot exert.
		scn.AddWoundsToChar(thorin, 3);
		assertAttachedTo(key, thorin);
		assertEquals(3, scn.GetWoundsOn(thorin));
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		assertInZone(Zone.SHADOW_CHARACTERS, bats);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(key));
		scn.FreepsUseCardAction(key);

		// Only the discard can be paid: nothing is asked. The one minion is discarded unasked.
		assertInDiscard(key, bats);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var key = scn.GetFreepsCard("key");
		var thorin = scn.GetFreepsCard("thorin");
		var sack = scn.GetShadowCard("sack");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, key);
		assertAttachedTo(key, thorin);
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(sack);
		assertInZone(Zone.SUPPORT, sack);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(key));
		scn.FreepsUseCardAction(key);
		scn.FreepsChoose("Exert bearer");

		assertEquals(1, scn.GetWoundsOn(thorin));
		assertAttachedTo(key, thorin);
		assertInZone(Zone.SUPPORT, sack);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
