package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_071_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, no damage bonus
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, Damage +1 in print
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] companion
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion
					// Merry, Friend to Sam: [shire] companion, cost 1, nothing asked to play him
					put("merry", "1_302");
					// The Gaffer: [shire] ally, a character who is no companion
					put("gaffer", "1_291");
					put("armor", "1_8");        // Dwarven Armor: a possession, and so no character
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion with no text
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, Damage +1 is its whole text
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "102_71");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HelmsTowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Helm's Tower
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Left
		 * Game Text: Battleground. While you can spot 6 companions, each character is damage +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Helm's Tower", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void EachCharacterIsDamagePlusOneOnceASixthCompanionIsPlayedAtTheTower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var merry = scn.GetFreepsCard("merry");
		var gaffer = scn.GetFreepsCard("gaffer");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		// Merry is in hand from before the game begins, so that the fellowship decision at the Tower offers him.
		scn.MoveCardsToHand(merry);
		scn.StartGame();
		scn.SkipToSite(7);

		assertEquals("Helm's Tower", scn.GetCurrentSite().getBlueprint().getTitle());

		// Five companions with Frodo, and an ally beside them who is no companion.
		scn.MoveCompanionsToTable(aragorn, gimli, sam, legolas);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveMinionsToTable(troop, savage);

		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(sam, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));        // his printed Damage +1
		assertEquals(0, scn.GetKeywordCount(gaffer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));       // its printed Damage +1
		assertEquals(0, scn.GetKeywordCount(armor, Keyword.DAMAGE));

		assertTrue(scn.FreepsPlayAvailable(merry));
		scn.FreepsPlayCard(merry);

		// Six companions: every character is one higher, the Ring-bearer, the ally and the minions with the rest.
		assertEquals(1, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));        // 1 printed + 1
		assertEquals(1, scn.GetKeywordCount(gaffer, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));       // 1 printed + 1
		// A possession is no character.
		assertEquals(0, scn.GetKeywordCount(armor, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(armor, Keyword.DAMAGE));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SevenCompanionsHaveNoBonusAtTheSiteBeforeAndEachCharacterOneOnMovingToTheTower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var gaffer = scn.GetFreepsCard("gaffer");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(6);

		scn.MoveCompanionsToTable(aragorn, gimli, sam, legolas, boromir, merry);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveMinionsToTable(troop, savage);

		// Seven companions at site 6, with the Tower the next site on the path: nobody has a bonus from it.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(gaffer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		scn.FreepsPass();

		// At the Tower with one companion more than it asks for: each character is one higher and no more.
		assertEquals("Helm's Tower", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));        // 1 printed + 1
		assertEquals(1, scn.GetKeywordCount(gaffer, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));       // 1 printed + 1

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheTowerSixCompanionsGiveNoCharacterADamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var merry = scn.GetFreepsCard("merry");
		var gaffer = scn.GetFreepsCard("gaffer");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(7);

		// Six companions with Frodo, an ally and two minions, at the Tower.
		scn.MoveCompanionsToTable(aragorn, gimli, sam, legolas, merry);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveMinionsToTable(troop, savage);

		var tower = scn.GetCurrentSite();
		assertEquals("Helm's Tower", tower.getBlueprint().getTitle());
		assertEquals(1, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));        // 1 printed + 1
		assertEquals(1, scn.GetKeywordCount(gaffer, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));       // 1 printed + 1

		// The fellowship moves on to site 8 and the Tower lies behind it.
		scn.FreepsPass();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertSame(tower, scn.GetSite(7));
		// The same six companions.  The text of a site binds only while the fellowship is at it: each character
		// has the damage bonus it prints and no more.
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(gaffer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
