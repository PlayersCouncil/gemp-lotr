package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_357_Tests
{

	// The Towers sites, with King's Room as site 7 in place of Helm's Tower.  No minion is laid, so each move goes
	// from the Shadow phase straight to the regroup phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, an unbound companion; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf, an unbound companion; an archery ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally, no companion; a fellowship ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; valiant and nothing else
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit and no Man (he is one only beside 3 [rohan]
					// companions or Eowyn); his trigger answers another [rohan] Man exerting
					put("merry", "103_85");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site7", "4_357");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingsRoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: King's Room
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: When the fellowship moves to King's Room without a [rohan] companion, each unbound companion must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("King's Room", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void MovingToKingsRoomWithoutARohanCompanionExertsEachUnboundCompanionAndMovingOnExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var guma = scn.GetFreepsCard("guma");
		// The other player's [rohan] companion, an unbound one, in his own fellowship: not in the one that moves.
		var shadowGuard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(gimli, legolas, sam, shadowGuard);
		scn.MoveAlliesToTable(guma);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(shadowGuard));

		scn.FreepsPass();

		// The unbound companions exert: a Dwarf and an Elf of two cultures.  Frodo and Sam are Ring-bound; Guma is
		// a [rohan] ally, no companion, and neither stops it nor exerts; the other player's companion is not of
		// this fellowship.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals("King's Room", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(shadowGuard));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on from King's Room, still without a [rohan] companion.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithARohanManCompanionNobodyExertsMovingToKingsRoom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(guard, gimli);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithARohanHobbitCompanionNobodyExertsMovingToKingsRoom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		// A [rohan] companion who is a Hobbit, where Household Guard is a Man.
		var merry = scn.GetFreepsCard("merry");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(merry, legolas);

		assertEquals(6, scn.GetCurrentSiteNumber());
		// The only [rohan] companion, so no Man on this board.
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
