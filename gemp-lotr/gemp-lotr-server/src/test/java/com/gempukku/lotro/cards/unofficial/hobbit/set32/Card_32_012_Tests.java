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

public class Card_32_012_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Site 2 is The Troll
	// Hoard, site 3 Rivendell (a sanctuary, no mountain), site 6 Esgaroth and site 7 Erebor (a mountain and a river).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("map", "32_12");

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, vitality 4; a Fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion; a Maneuver ability not used
					put("dain", "32_2");        // Dain Ironfoot: a [dwarven] ally; Thorin is strength +1, a start-of-regroup offer that needs a follower

					put("bats", "32_30");       // Dark Bats: [moria] minion; a Shadow ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	// The Hobbit path with Wooded Steep Cliff 30_61 (a mountain and a forest) as site 4 in place of Goblin Town; its
	// move trigger is not reached, as nobody moves there.
	protected VirtualTableScenario GetCliffScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("map", "32_12");

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, vitality 4; a Fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability not used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site4", "30_61");
				}},
				null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThrorsMapStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Thrór's Map
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype:
		 * Game Text: Bearer must be a [dwarven] companion.<br><b>Fellowship or Regroup:</b> Exert bearer or discard this possession to play the fellowship's next site (replacing opponent's site if necessary). Heal a [dwarven] companion if you play a mountain site.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("map");

		assertEquals("Thrór's Map", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnADwarvenCompanionAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToHand(map);

		scn.StartGame();
		assertInHand(map);
		assertInZone(Zone.SUPPORT, dain);

		assertTrue(scn.FreepsPlayAvailable(map));
		scn.FreepsPlayCard(map);
		// Two [dwarven] companions; not Bilbo, a [shire] companion, nor Dain, a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(thorin);

		assertAttachedTo(map, thorin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionReplacesTheOpponentsEreborWithHisOwnAndHealsADwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		var freepsErebor = scn.GetFreepsSite(7);
		var shadowErebor = scn.GetShadowSite(7);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(thorin, balin, dwalin);
		scn.MoveAlliesToTable(dain);
		scn.AttachCardsTo(balin, map);
		scn.AddWoundsToChar(thorin, 2);
		scn.AddWoundsToChar(dwalin, 1);
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(dain, 1);
		// The Shadow player has moved on ahead: his Erebor lies next.
		scn.MoveCardToAdventurePath(shadowErebor);
		scn.FreepsRefreshDecision();
		assertAttachedTo(map, balin);
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertSame(shadowErebor, scn.GetSite(7));
		assertInZone(Zone.ADVENTURE_DECK, freepsErebor);

		assertTrue(scn.FreepsActionAvailable(map));
		scn.FreepsUseCardAction(map);
		// Both costs can be paid, so he is asked which.
		assertEquals(List.of("Exert bearer", "Discard this possession"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert bearer");
		assertEquals(1, scn.GetWoundsOn(balin));
		// Thorin, a [dwarven] companion who is not the bearer, does not exert.
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertAttachedTo(map, balin);

		// His own Erebor in place of the opponent's, which goes back to that player's adventure deck.
		assertSame(freepsErebor, scn.GetSite(7));
		assertInZone(Zone.ADVENTURE_PATH, freepsErebor);
		assertInZone(Zone.ADVENTURE_DECK, shadowErebor);
		assertEquals(6, scn.GetCurrentSiteNumber());

		// A mountain was played: the wounded [dwarven] companions are offered, the bearer among them; not Bilbo, a
		// [shire] companion, nor Dain, a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, balin, dwalin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		scn.FreepsChooseCard(thorin);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Used again, with his own Erebor already next: no site is played, and so nobody is healed.
		assertTrue(scn.FreepsActionAvailable(map));
		scn.FreepsUseCardAction(map);
		scn.FreepsChoose("Discard this possession");
		assertInDiscard(map);
		assertSame(freepsErebor, scn.GetSite(7));
		assertInZone(Zone.ADVENTURE_DECK, shadowErebor);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsTheMapToPlayRivendellAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var freepsRivendell = scn.GetFreepsSite(3);
		var shadowRivendell = scn.GetShadowSite(3);
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, map);
		scn.AddWoundsToChar(balin, 1);
		assertAttachedTo(map, thorin);
		assertEquals(1, scn.GetWoundsOn(balin));

		// No minion, so the turn goes from the move to The Troll Hoard (site 2) to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, freepsRivendell, shadowRivendell);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertTrue(scn.FreepsActionAvailable(map));
		scn.FreepsUseCardAction(map);
		assertEquals(List.of("Exert bearer", "Discard this possession"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Discard this possession");
		assertInDiscard(map);
		assertEquals(0, scn.GetWoundsOn(thorin));

		assertSame(freepsRivendell, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, shadowRivendell);
		// Rivendell is no mountain: Balin is not healed.
		assertFalse(scn.HasKeyword(freepsRivendell, Keyword.MOUNTAIN));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithBearerExhaustedTheMapIsDiscardedToPlayWoodedSteepCliffAndHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCliffScenario();

		var map = scn.GetFreepsCard("map");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var freepsCliff = scn.GetFreepsSite("site4");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, map);
		// Vitality 4 with 3 wounds: Thorin cannot exert.
		scn.AddWoundsToChar(thorin, 3);
		scn.AddWoundsToChar(balin, 2);
		scn.FreepsRefreshDecision();
		assertAttachedTo(map, thorin);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertNull(scn.GetSite(4));

		assertTrue(scn.FreepsActionAvailable(map));
		scn.FreepsUseCardAction(map);
		// Only the discard can be paid: nothing is asked.
		assertInDiscard(map);
		assertEquals(3, scn.GetWoundsOn(thorin));

		assertSame(freepsCliff, scn.GetSite(4));
		assertTrue(scn.HasKeyword(freepsCliff, Keyword.MOUNTAIN));
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, balin));
		scn.FreepsChooseCard(balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var map = scn.GetFreepsCard("map");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, map);
		assertAttachedTo(map, thorin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		assertInZone(Zone.SHADOW_CHARACTERS, bats);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(map));
	}
}
