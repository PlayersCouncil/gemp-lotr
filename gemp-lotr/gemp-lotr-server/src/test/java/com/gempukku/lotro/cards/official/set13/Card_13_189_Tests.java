package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_189_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] companion, vitality 4; abilities only used on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, vitality 4; a maneuver ability never used here
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ALLY, vitality 2; home 4T, so never discarded here
					put("dwarf", "1_7");        // Dwarf Guard: [dwarven] companion, vitality 2; its only text is a requirement to play
					put("elf", "1_53");         // Lórien Elf: [elven] companion, vitality 2; its only text is a requirement to play
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion, vitality 4; a strength bonus is his only text
					put("smeagol", "5_29");     // Sméagol, Slinker: [gollum] companion, vitality 4; a skirmish ability never used here
					put("rider", "4_265");      // Elite Rider: [rohan] companion, vitality 3; strength bonuses are his only text
				}},
				new HashMap<>()
				{{
					// The Fellowship-block path, with the Crossroads in place of site 9, which no test reaches: the
					// Shadow player's Crossroads is put on the path as the next site wherever a test wants it.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("crossroads", "13_189");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrossroadsoftheFallenKingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Crossroads of the Fallen Kings
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: Forest. When the fellowship moves to this site during the regroup phase, the Free Peoples
		 * 	player may heal a companion of each culture.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("crossroads");

		assertEquals("Crossroads of the Fallen Kings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void RegroupMoveToCrossroadsHealsOneCompanionOfEachCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		var dwarf = scn.GetFreepsCard("dwarf");
		var elf = scn.GetFreepsCard("elf");
		var gandalf = scn.GetFreepsCard("gandalf");
		var smeagol = scn.GetFreepsCard("smeagol");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(sam, aragorn, dwarf, elf, gandalf, smeagol, rider);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// The Shadow player's Crossroads: the site's owner is not the Free Peoples player.
		var crossroads = scn.GetShadowSite("crossroads");
		scn.MoveCardToAdventurePath(crossroads);
		scn.SkipToMovementDecision();

		// Two [shire] companions and one companion of each of six other cultures, and a [rohan] ally beside the
		// [rohan] companion, each with one wound.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(dwarf, 1);
		scn.AddWoundsToChar(elf, 1);
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(rider, 1);
		scn.AddWoundsToChar(guma, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(dwarf));
		assertEquals(1, scn.GetWoundsOn(elf));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(guma));

		scn.FreepsChooseToMove();
		assertSame(crossroads, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(crossroads));
		scn.FreepsAcceptOptionalTrigger();

		// Each other culture has one wounded companion and is healed without a question, the [rohan] ally
		// being no candidate beside the [rohan] companion. Of the [shire] companions, one is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(dwarf));
		assertEquals(0, scn.GetWoundsOn(elf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipMoveToCrossroadsAndRegroupMoveFromItOfferNoHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		var crossroads = scn.GetShadowSite("crossroads");
		scn.MoveCardToAdventurePath(crossroads);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);

		// The fellowship-phase move TO the Crossroads.
		scn.FreepsPass();
		assertSame(crossroads, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(crossroads));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The regroup move FROM the Crossroads.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(crossroads));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupMoveToCrossroadsWithOnlyAnAllyWoundedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.FreepsPass();
		var crossroads = scn.GetShadowSite("crossroads");
		scn.MoveCardToAdventurePath(crossroads);
		scn.SkipToMovementDecision();
		scn.AddWoundsToChar(guma, 1);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guma));

		scn.FreepsChooseToMove();
		assertSame(crossroads, scn.GetCurrentSite());
		// No companion has a wound to heal, so the offer is not made.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(crossroads));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreePeoplesPlayerMayDeclineAndTheShadowPlayerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		var crossroads = scn.GetShadowSite("crossroads");
		scn.MoveCardToAdventurePath(crossroads);
		scn.SkipToMovementDecision();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.FreepsChooseToMove();
		assertSame(crossroads, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(crossroads));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The site is the Shadow player's, but the heal is the Free Peoples player's alone.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
