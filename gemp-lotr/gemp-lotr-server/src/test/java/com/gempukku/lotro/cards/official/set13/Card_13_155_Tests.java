package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_155_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("phial", "13_155");
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit who is not the Ring-bearer
					put("merry", "1_302");        // Merry, Friend to Sam: an unbound [shire] Hobbit companion
					put("smeagol", "103_116");    // Smeagol, Once Like Us: a Ring-bound [shire] companion who is no Hobbit
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit companion
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man companion
					put("bounder", "1_286");      // Bounder: a [shire] Hobbit ally
					put("worker", "15_135");      // Rohan Worker: a follower, which is no character
					put("troop", "1_177");        // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PhialofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Phial of Galadriel, The Light of Eärendil
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Item Class: Phial
		 * Resistance: +1
		 * Game Text: Bearer must be a Ring-bound Hobbit.
		 * 	While bearer is in region 1, bearer gains <b>muster</b>.
		 * 	While bearer is in region 2, each of your [shire] companions gains <b>muster</b>.
		 * 	While bearer is in region 3, each character gains <b>muster</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("phial");

		assertEquals("Phial of Galadriel", card.getBlueprint().getTitle());
		assertEquals("The Light of Eärendil", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PHIAL));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysOnARingBoundHobbitWhoGainsMusterInRegionOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, merry, smeagol);
		scn.MoveCardsToHand(phial);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(sam, Keyword.MUSTER));
		assertTrue(scn.FreepsPlayAvailable(phial));
		scn.FreepsPlayCard(phial);

		// The Ring-bearer and Sam are Ring-bound Hobbits; Merry is unbound, and Smeagol is Ring-bound and no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(phial, sam);
		// Site 1 is in region 1: the bearer gains muster, and no other [shire] companion does.
		assertTrue(scn.HasKeyword(sam, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(merry, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(smeagol, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MovingIntoRegionTwoEachShireCompanionGainsMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, merry, smeagol, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(3);
		// Laid here: a bearer of muster on the way would be asked at each regroup phase.
		scn.AttachCardsTo(frodo, phial);

		// Site 3 is the last of region 1: the bearer alone has muster.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(sam, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(merry, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(smeagol, Keyword.MUSTER));

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable("troop");
		var troop = scn.GetShadowCard("troop");

		// Every [shire] companion, Hobbit or not; no companion of another culture, no ally and no minion.
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(sam, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(merry, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(smeagol, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(pippin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(bounder, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(troop, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingIntoRegionThreeEachCharacterGainsMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToSupportArea(worker);

		scn.StartGame();
		scn.SkipToSite(6);
		// Laid here: a bearer of muster on the way would be asked at each regroup phase.
		scn.AttachCardsTo(frodo, phial);

		// Site 6 is the last of region 2: [shire] companions only.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(pippin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		// Laid once the fellowship is at Anduin Confluence, whose move discards every ally.
		scn.MoveAlliesToTable(bounder);
		scn.MoveMinionsToTable("troop");
		var troop = scn.GetShadowCard("troop");

		// Companions of any culture, an ally and the opponent's minion; not a follower, which is no character.
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(pippin, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(aragorn, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(bounder, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(troop, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(worker, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
