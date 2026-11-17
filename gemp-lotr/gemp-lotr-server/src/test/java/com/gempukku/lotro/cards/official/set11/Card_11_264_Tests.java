package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_264_Tests
{

	// The turn-skippers walk a Fellowship-block path, which they cannot do on a Shadows-block one.  Each adventure
	// deck has Westemnet Village in place of site 9, and the tests put the Shadow player's copy on the path as the
	// site the fellowship moves to next.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion, vitality 3; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion, vitality 3; archery is never reached
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, home 3; a fellowship ability only
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally, home 6; a fellowship ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("westemnet", "11_264");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WestemnetVillageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Westemnet Village
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Dwelling</b>. When the fellowship moves to this site, the Free Peoples player wounds a companion for each ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("westemnet");

		assertEquals("Westemnet Village", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.DWELLING));
	}

	@Test
	public void MovingThereWithOneAllyTheFreePeoplesPlayerWoundsOneCompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveAlliesToTable(grimir);
		// The other player's own ally, in his support area: not active on this turn.
		var shadowCeleborn = scn.GetShadowCard("celeborn");
		scn.MoveAlliesToTable(shadowCeleborn);

		var westemnet = scn.GetShadowSite("westemnet");

		scn.StartGame();
		// The second player's Westemnet Village is the next site.
		scn.MoveCardToAdventurePath(westemnet);

		assertInZone(Zone.SUPPORT, grimir, shadowCeleborn);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(grimir));

		scn.FreepsPass();

		// One ally of his: one wound, placed by the Free Peoples player on a companion of his choice.  The ally is
		// no companion.
		assertSame(westemnet, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, shadowCeleborn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(shadowCeleborn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingThereInTheRegroupPhaseWithTwoAlliesOneCompanionMayTakeBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(gimli, legolas);
		// Two allies of two races and cultures, with homes at site 3 and site 6.
		scn.MoveAlliesToTable(grimir, celeborn);

		var troop = scn.GetShadowCard("troop");
		var westemnet = scn.GetShadowSite("westemnet");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// A minion still in play in the regroup phase, as one that has lived through its skirmish is.
		scn.MoveMinionsToTable(troop);
		// The second player's Westemnet Village is the next site, site 3.
		scn.MoveCardToAdventurePath(westemnet);

		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsChooseToMove();

		// Two allies: two wounds, each on a companion of his choice, and the second may go on the one who took the
		// first.  Neither ally nor the minion is a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, celeborn, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, celeborn, troop));
		scn.FreepsChooseCard(gimli);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertSame(westemnet, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoAllyMovingThereWoundsNobodyAndMovingOnFromItWithTwoAlliesWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(gimli, legolas);

		var westemnet = scn.GetShadowSite("westemnet");

		scn.StartGame();
		scn.MoveCardToAdventurePath(westemnet);

		// No ally: the move there wounds nobody and asks nothing.
		scn.FreepsPass();
		assertSame(westemnet, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Two allies in play for the move on from it, to site 3.
		scn.MoveAlliesToTable(grimir, celeborn);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		assertInZone(Zone.SUPPORT, grimir, celeborn);
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
