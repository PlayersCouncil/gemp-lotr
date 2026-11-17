package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_081_Tests
{

	// The Towers path: site 1 (Eastemnet Gullies) has no terrain, site 2 (Eastfold) is a plains and nothing more,
	// site 3 (Westemnet Plains) a plains and a sanctuary, site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fletcher", "11_81");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; his abilities are used only on purpose
					put("ted", "18_75");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion with no text
					// Goaded to War: a [men] condition that is no minion; its move trigger adds (1) and reaches no reading
					put("goaded", "12_67");

					put("sam", "1_311");        // Sam, Son of Hamfast: his abilities are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: his ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf the Dwarf Guards' play spots
					put("guard1", "1_7");       // Dwarf Guard: "To play, spot a Dwarf", nothing more; not unique
					put("guard2", "1_7");
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally, a Free Peoples character who is no companion
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					// Westemnet Plains: Plains. Sanctuary. Its bar on [dunland] Men's archery wounds reaches no test.
					put("site3", "4_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FletcherofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Fletcher of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While this minion is at a plains site, it is an <b>archer</b>.<br>While you can spot 6 companions, each [men] minion is an <b>archer</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fletcher");

		assertEquals("Fletcher of Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsAnArcherOnlyOnceTheFellowshipIsAtAPlainsSiteAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fletcher = scn.GetShadowCard("fletcher");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1, to be read before and after the move.
		scn.MoveMinionsToTable(fletcher, legion);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(fletcher, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(legion, Keyword.ARCHER));

		// The fellowship moves to Eastfold.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(fletcher, Keyword.ARCHER));
		// Another [men] Man at the same plains is not made an archer (Frodo is the only companion).
		assertFalse(scn.HasKeyword(legion, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsAnArcherAtAPlainsThatIsASanctuaryAndNotAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fletcher = scn.GetShadowCard("fletcher");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(fletcher, legion);

		// Westemnet Plains.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(fletcher, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(legion, Keyword.ARCHER));

		// The fellowship moves to White Rocks, a battleground and no plains.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(fletcher, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSixthCompanionPlayedMakesEachMenMinionAnArcherAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fletcher = scn.GetShadowCard("fletcher");
		var legion = scn.GetShadowCard("legion");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");
		var goaded = scn.GetShadowCard("goaded");

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, guard1);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(guard2);

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1 (no plains), to be read before and after the play.
		scn.MoveMinionsToTable(fletcher, legion, ted, troop);
		scn.MoveCardsToSupportArea(goaded);

		// Five companions (Frodo among them) and an ally, who is a Free Peoples character but no companion.
		assertInZone(Zone.FREE_CHARACTERS, scn.GetRingBearer(), sam, aragorn, gimli, guard1);
		assertInZone(Zone.SUPPORT, thrarin);
		assertInZone(Zone.HAND, guard2);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(fletcher, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(legion, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(ted, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(troop, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(goaded, Keyword.ARCHER));

		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(guard2);
		assertInZone(Zone.FREE_CHARACTERS, guard2);
		// Each [men] minion, a Man and a Hobbit, and the Fletcher itself away from any plains.
		assertTrue(scn.HasKeyword(fletcher, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(legion, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(ted, Keyword.ARCHER));
		// A minion of another culture, and a [men] card that is no minion, are not.
		assertFalse(scn.HasKeyword(troop, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(goaded, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
