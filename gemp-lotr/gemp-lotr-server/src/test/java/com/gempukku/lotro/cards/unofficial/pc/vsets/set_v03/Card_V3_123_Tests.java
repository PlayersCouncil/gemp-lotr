package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

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

public class Card_V3_123_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo, who bears The Ruling Ring.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man, strength 8
					put("gimli", "1_13");          // Gimli, Son of Gloin: a Dwarf, to wear the armor
					put("bilbo", "1_284");         // Bilbo, Retired Adventurer: [shire] ally
					put("army", "18_47");          // Elendil's Army: a follower, which is not a character

					// Free Peoples weapons
					put("sting", "1_313");         // Sting: possession, hand weapon
					put("sword", "1_112");         // Ranger's Sword: possession, hand weapon, strength +2
					put("anduril", "7_80");        // Anduril, King's Blade: artifact, hand weapon, strength +2
					put("aragornsBow", "1_90");    // Aragorn's Bow: possession, ranged weapon
					put("elvenBow", "1_41");       // Elven Bow: possession, ranged weapon
					// Free Peoples items that are not weapons
					put("athelas", "1_94");        // Athelas: possession of no class
					put("armor", "1_8");           // Dwarven Armor: possession, armor

					// Shadow weapons; their bearers are named, so each is put on the Goblin Runner by the rig
					put("paleBlade", "1_221");     // The Pale Blade: possession, hand weapon
					put("raiderBow", "7_155");     // Raider Bow: possession, ranged weapon
					put("whip", "2_74");           // Whip of Many Thongs: artifact, hand weapon
					// Shadow items that are not weapons
					put("steed", "1_208");         // Black Steed: possession, mount
					put("hides", "4_19");          // Hides: possession in the support area

					put("runner", "1_178");        // Goblin Runner: [moria] Orc
					put("savage", "1_151");        // Uruk Savage: [isengard] Uruk-hai
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "103_123");       // Gorgoroth Highway
					put("site9", "7_360");         // Dagorlad: adds 3 threats when the fellowship moves there
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothHighwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gorgoroth Highway
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 8
		 * Shadow Number: 7
		 * Direction: Left
		 * Game Text: At the start of the maneuver phase, hinder all weapons. All characters gain <b>enduring</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Gorgoroth Highway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseEveryWeaponIsHinderedAndNothingElseIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sting = scn.GetFreepsCard("sting");
		var anduril = scn.GetFreepsCard("anduril");
		var aragornsBow = scn.GetFreepsCard("aragornsBow");
		var elvenBow = scn.GetFreepsCard("elvenBow");
		var athelas = scn.GetFreepsCard("athelas");
		var armor = scn.GetFreepsCard("armor");

		var runner = scn.GetShadowCard("runner");
		var paleBlade = scn.GetShadowCard("paleBlade");
		var raiderBow = scn.GetShadowCard("raiderBow");
		var whip = scn.GetShadowCard("whip");
		var steed = scn.GetShadowCard("steed");
		var hides = scn.GetShadowCard("hides");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bilbo);
		// Weapons: a possession and an artifact, hand and ranged, on two companions and (the Elven Bow, put
		// there by the rig) on an ally. Beside them a possession of no class, an armor and The One Ring.
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(aragorn, anduril, aragornsBow, athelas);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(bilbo, elvenBow);
		// The Shadow player's weapons on a minion, with a mount beside them and a possession in his
		// support area.
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, paleBlade, raiderBow, whip, steed);
		scn.MoveCardsToSupportArea(hides);

		scn.FreepsPass();
		assertEquals("Gorgoroth Highway", scn.GetCurrentSite().getBlueprint().getTitle());

		// The Shadow phase at this site: nothing is hindered yet.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.IsHindered(sting));
		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(aragornsBow));
		assertFalse(scn.IsHindered(elvenBow));
		assertFalse(scn.IsHindered(paleBlade));
		assertFalse(scn.IsHindered(raiderBow));
		assertFalse(scn.IsHindered(whip));
		// 8 + 2 for Anduril
		assertEquals(10, scn.GetStrength(aragorn));

		scn.ShadowPass();

		assertTrue(scn.IsHindered(sting));          // Free Peoples possession, hand weapon, on the Ring-bearer
		assertTrue(scn.IsHindered(anduril));        // Free Peoples artifact, hand weapon
		assertTrue(scn.IsHindered(aragornsBow));    // Free Peoples possession, ranged weapon
		assertTrue(scn.IsHindered(elvenBow));       // ranged weapon on an ally
		assertTrue(scn.IsHindered(paleBlade));      // Shadow possession, hand weapon
		assertTrue(scn.IsHindered(raiderBow));      // Shadow possession, ranged weapon
		assertTrue(scn.IsHindered(whip));           // Shadow artifact, hand weapon
		// A hindered weapon gives its bearer nothing.
		assertEquals(8, scn.GetStrength(aragorn));

		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(ring));
		assertFalse(scn.IsHindered(steed));
		assertFalse(scn.IsHindered(hides));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(bilbo));
		assertFalse(scn.IsHindered(runner));

		// Nobody was asked anything on the way.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WeaponsAreHinderedAtTheStartOfTheManeuverPhaseAndNotAgainAtTheStartOfLaterPhases() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass();
		assertEquals("Gorgoroth Highway", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.IsHindered(sword));
		// 8 + 2 for the Ranger's Sword
		assertEquals(10, scn.GetStrength(aragorn));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.IsHindered(sword));
		assertEquals(8, scn.GetStrength(aragorn));

		// The sword is restored during the maneuver phase: the phases that follow do not hinder it again.
		scn.RestoreCard(sword);
		assertFalse(scn.IsHindered(sword));
		scn.PassManeuverActions();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.IsHindered(sword));
		scn.PassArcheryActions();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsHindered(sword));
		assertEquals(10, scn.GetStrength(aragorn));
	}

	@Test
	public void EveryCharacterHasEnduringWhileTheFellowshipIsHereAndNothingElseDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var army = scn.GetFreepsCard("army");
		var athelas = scn.GetFreepsCard("athelas");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(army);
		scn.AttachCardsTo(aragorn, athelas);
		scn.MoveMinionsToTable(runner, savage);
		scn.AddWoundsToChar(aragorn, 1);

		// At site 7 nobody has it, and Aragorn's wound gives him nothing.
		assertFalse(scn.HasKeyword(frodo, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(bilbo, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(runner, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(savage, Keyword.ENDURING));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPass();
		assertEquals("Gorgoroth Highway", scn.GetCurrentSite().getBlueprint().getTitle());

		// From the first phase here: the Ring-bearer, a companion, an ally, and minions of two cultures.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(frodo, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(aragorn, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(bilbo, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(runner, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(savage, Keyword.ENDURING));
		// 8 + 2 for his one wound
		assertEquals(10, scn.GetStrength(aragorn));

		// A follower is not a character, and neither is a possession.
		assertFalse(scn.HasKeyword(army, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(athelas, Keyword.ENDURING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnNobodyHasEnduring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);

		scn.FreepsPass();
		assertEquals("Gorgoroth Highway", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(frodo, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(aragorn, Keyword.ENDURING));
		// 8 + 2 for his one wound
		assertEquals(10, scn.GetStrength(aragorn));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(9, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(runner);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(frodo, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(runner, Keyword.ENDURING));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
