package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_347_Tests
{

	// The Fellowship path with the Stairway as site 4.  Site 3 is Rivendell Valley (a sanctuary) and site 5 The
	// Bridge of Khazad-dûm, whose Shadow action plays The Balrog only.  No character laid here has a damage bonus
	// of his own, so every reading is 0 or 1.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion; Knight is his whole text
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an ARTIFACT hand weapon; its strength is only for Elendil or Isildur
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion; a fellowship ability used only on purpose
					put("hobbitsword", "1_299");// Hobbit Sword: a [shire] POSSESSION hand weapon; no other text
					put("faramir", "4_117");    // Faramir, Son of Denethor: a [gondor] Man companion; his text is about skirmishes
					put("bow", "4_131");        // Ranger's Bow: a RANGED weapon on a Ring-bound Man; a skirmish ability
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] Man ALLY; a maneuver ability used only on purpose
					put("gondorsword", "4_134");// Sword of Gondor: a hand weapon a [gondor] Man bears; damage +1 only while skirmishing
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion; abilities used only on purpose
					put("dagger", "18_69");     // Henchman's Dagger: a [men] Shadow POSSESSION hand weapon on a [men] minion
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("scimitar", "1_180");   // Goblin Scimitar: a hand weapon a [moria] Orc bears; a trigger on its own play
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_347");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoriaStairwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Moria Stairway
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 4
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: <b>Underground</b>. Each companion and minion bearing a hand weapon is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Moria Stairway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void EachCompanionAndMinionBearingAHandWeaponIsDamagePlusOneThereAndNoOtherCharacterIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var narsil = scn.GetFreepsCard("narsil");
		var sam = scn.GetFreepsCard("sam");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var faramir = scn.GetFreepsCard("faramir");
		var bow = scn.GetFreepsCard("bow");
		var merchant = scn.GetFreepsCard("merchant");
		var gondorsword = scn.GetFreepsCard("gondorsword");
		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(knight, sam, faramir);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(knight, narsil);
		scn.AttachCardsTo(sam, hobbitsword);
		scn.AttachCardsTo(faramir, bow);
		scn.AttachCardsTo(merchant, gondorsword);

		scn.StartGame();
		scn.SkipToSite(3);
		// The minions are laid early, in the fellowship phase of the turn they stand in, so that they are read at
		// Rivendell Valley as well.
		scn.MoveMinionsToTable(ted, troop);
		scn.AttachCardsTo(ted, dagger);
		scn.AttachCardsTo(troop, scimitar);

		// At Rivendell Valley.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ATTACHED, narsil, hobbitsword, bow, gondorsword, dagger, scimitar);
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(sam, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(faramir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DAMAGE));

		scn.FreepsPass();

		assertEquals("Moria Stairway", scn.GetCurrentSite().getBlueprint().getTitle());
		// Companions of two cultures and races, with an artifact and with a possession hand weapon, and minions of
		// two cultures and races with theirs.
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		// A companion with a ranged weapon, the Ring-bearer with the Ring (an item that is no weapon), and an ally
		// with a hand weapon.
		assertEquals(0, scn.GetKeywordCount(faramir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromTheStairwayTheBonusIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairway = scn.GetShadowSite(4);
		var knight = scn.GetFreepsCard("knight");
		var narsil = scn.GetFreepsCard("narsil");
		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, narsil);

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.ATTACHED, narsil);
		scn.FreepsPass();
		// The minion stays on the table when the fellowship moves on in the regroup phase. Nobody is assigned to it.
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, dagger);

		assertSame(stairway, scn.GetCurrentSite());
		assertInZone(Zone.ATTACHED, narsil, dagger);
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(ted, Keyword.DAMAGE));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("The Bridge of Khazad-dûm", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.ATTACHED, narsil, dagger);
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
