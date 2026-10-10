package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_071_ErrataTests
{
	// Towers sites: White Rocks (4), Deeping Wall (5) and Helm's Tower (7) are battlegrounds; Caves of Aglarond (6) is not.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "54_71");
					put("lorien", "1_53");      // Lorien Elf: Elf companion, vitality 2, no text in play
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, vitality 3; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4: a companion who could exert and is no Elf
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HaldirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Haldir, Emissary of the Galadhrim
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Valiant.
		 * While no opponent controls a site, Haldir is strength +2.
		 * Regroup: Exert Haldir at a battleground and exert another Elf to liberate a site.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haldir");

		assertEquals("Haldir", card.getBlueprint().getTitle());
		assertEquals("Emissary of the Galadhrim", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCardsToHand(haldir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(haldir));
		scn.FreepsPlayCard(haldir);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StrengthPlusTwoWhileNoOpponentControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(haldir);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);

		// 5 + 2
		assertEquals(7, scn.GetStrength(haldir));

		// A site his own player controls is not one an opponent controls.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(7, scn.GetStrength(haldir));

		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site2));
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsHimAndAnotherElfToLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var lorien = scn.GetFreepsCard("lorien");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(haldir, lorien, aragorn);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		scn.ShadowTakeControlOfSite(site3);
		// The fellowship moves to Deeping Wall, site 5, a battleground.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(haldir));

		scn.FreepsUseCardAction(haldir);
		assertEquals(1, scn.GetWoundsOn(haldir));
		// Another Elf, companion or ally; not Aragorn, who could exert and is no Elf, and not Haldir himself.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorien, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, haldir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(lorien);
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Site 3 is the highest-numbered site an opponent controls: back on the adventure path.
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertFalse(scn.IsSiteControlled(site3));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		scn.ShadowPass();
		// Lorien Elf is exhausted, so Orophin is the one Elf left to exert.
		scn.FreepsUseCardAction(haldir);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(orophin));

		// Site 2 is his own player's, so site 1 is liberated, and no opponent controls a site.
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.FreepsControls(site2));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(7, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedAwayFromABattlegroundThoughAnOpponentControlsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(haldir, lorien);

		scn.StartGame();
		scn.SkipToSite(5);
		// White Rocks, site 4, is a battleground, and keeps the keyword once controlled.
		var site4 = scn.GetSite(4);
		scn.ShadowTakeControlOfSite(site4);
		// The fellowship moves to Caves of Aglarond, site 6, which is no battleground.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowControls(site4));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(lorien));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(haldir));
	}

	@Test
	public void NotOfferedWhenHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(haldir, lorien);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite(scn.GetSite(1));
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(haldir, 2);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(lorien));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(haldir));
	}

	@Test
	public void NotOfferedWhenNoOtherElfCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var lorien = scn.GetFreepsCard("lorien");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(haldir, lorien, aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite(scn.GetSite(1));
		// Vitality 2 with 1 wound: Lorien Elf cannot exert. Haldir himself could exert twice, but he is not another Elf,
		// and Aragorn could exert, but he is no Elf.
		scn.AddWoundsToChar(lorien, 1);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(haldir));
	}
}
