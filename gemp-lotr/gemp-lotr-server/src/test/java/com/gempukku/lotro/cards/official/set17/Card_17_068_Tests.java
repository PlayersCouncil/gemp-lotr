package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("clash", "17_68");

					// The Beckoning Shadow: an [orc] condition, no minion; to play, exert an [orc] minion (cheated in);
					// its corruption needs 5 burdens and an empty Free Peoples draw deck
					put("beckoning", "12_83");
					// Half-troll of Far Harad: an [orc] minion who is no Orc, cost 3, strength 8, site 4; its
					// start-of-assignment trigger is not reached
					put("halftroll", "14_12");
					// Mordor Brute: a [sauron] Orc, cost 3, strength 9, site 6; a [sauron] Orc bearing a possession
					// takes no wounds outside skirmishes
					put("brute", "10_90");
					// Denizen of Khazad-dum: an [orc] Orc, strength 9; +2 at an underground site only
					put("denizen", "11_115");
					// Orc Spear: an [orc] possession, cost 1, for an [orc] minion; adds (2) only when played on a
					// minion of cost 4 or more
					put("spear", "12_96");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8; his Maneuver ability is not used
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6; his Skirmish ability is not used
					put("gimli", "1_13");
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4; his Maneuver ability is not used
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChaoticClashStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Chaotic Clash
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot no minions.<br>Each time you play a minion, it is twilight cost +1.<br>While you can spot an [orc] minion, each companion is strength -1.
		*/

		var scn = GetScenario();

		var clash = scn.GetFreepsCard("clash");

		assertEquals("Chaotic Clash", clash.getBlueprint().getTitle());
		assertNull(clash.getBlueprint().getSubtitle());
		assertFalse(clash.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, clash.getBlueprint().getSide());
		assertEquals(Culture.ORC, clash.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, clash.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(clash, Keyword.SUPPORT_AREA));
		assertEquals(0, clash.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayableWithNoMinionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clash = scn.GetShadowCard("clash");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(clash);
		scn.MoveCardsToSupportArea(beckoning);
		assertInHand(clash);
		assertInZone(Zone.SUPPORT, beckoning);

		scn.StartGame();
		scn.FreepsPass();

		// An [orc] condition in play, and no minion.
		assertTrue(scn.ShadowPlayAvailable(clash));
		scn.ShadowPlayCard(clash);

		assertInZone(Zone.SUPPORT, clash);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableOnceAMinionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clash = scn.GetShadowCard("clash");
		var brute = scn.GetShadowCard("brute");
		scn.MoveCardsToHand(clash, brute);
		assertInHand(clash, brute);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(clash));
		// A [sauron] Orc
		scn.ShadowPlayCard(brute);
		assertInZone(Zone.SHADOW_CHARACTERS, brute);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(clash));
	}

	@Test
	public void NotPlayableWhileAnOrcMinionWhoIsNoOrcIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clash = scn.GetShadowCard("clash");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(clash);
		assertInHand(clash);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(clash));
	}

	@Test
	public void EachMinionPlayedCostsOneMoreAndAPossessionDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clash = scn.GetShadowCard("clash");
		var halftroll = scn.GetShadowCard("halftroll");
		var brute = scn.GetShadowCard("brute");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToSupportArea(clash);
		scn.MoveCardsToHand(halftroll, brute, spear);
		assertInZone(Zone.SUPPORT, clash);
		assertInHand(halftroll, brute, spear);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// In hand, roaming apart: printed 3, and 1 more.
		assertEquals(4, scn.GetTwilightCost(halftroll));
		assertEquals(4, scn.GetTwilightCost(brute));
		assertEquals(1, scn.GetTwilightCost(spear));

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(halftroll);
		// 3 printed + 1 + 2 roaming (site 4 minion at site 2)
		assertEquals(pool - 6, scn.GetTwilight());

		pool = scn.GetTwilight();
		scn.ShadowPlayCard(brute);
		// The second minion, of another culture and race: 3 printed + 1 + 2 roaming (site 6 minion at site 2)
		assertEquals(pool - 6, scn.GetTwilight());

		pool = scn.GetTwilight();
		scn.ShadowPlayCard(spear);
		// A possession: its printed 1 alone.
		assertEquals(pool - 1, scn.GetTwilight());
		assertAttachedTo(spear, halftroll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOtherPlayersMinionInHandIsNotMadeDearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clash = scn.GetShadowCard("clash");
		var shadowhalftroll = scn.GetShadowCard("halftroll");
		var freepshalftroll = scn.GetFreepsCard("halftroll");
		scn.MoveCardsToSupportArea(clash);
		scn.MoveCardsToHand(shadowhalftroll, freepshalftroll);
		assertInZone(Zone.SUPPORT, clash);
		assertInHand(shadowhalftroll, freepshalftroll);

		scn.StartGame();

		// "Each time YOU play a minion": the Shadow player's Half-troll is dearer, the Free Peoples player's is not.
		assertEquals(4, scn.GetTwilightCost(shadowhalftroll));
		assertEquals(3, scn.GetTwilightCost(freepshalftroll));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileAnOrcMinionCanBeSpottedEachCompanionIsStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var clash = scn.GetShadowCard("clash");
		var halftroll = scn.GetShadowCard("halftroll");
		var brute = scn.GetShadowCard("brute");
		scn.MoveCardsToSupportArea(clash);
		scn.MoveCardsToHand(halftroll);
		assertInZone(Zone.SUPPORT, clash);
		assertInHand(halftroll);

		scn.StartGame();
		scn.MoveMinionsToTable(brute);
		scn.SetTwilight(20);

		// A [sauron] Orc and this [orc] condition: no [orc] minion.  Frodo is 3 and the Ring's +1.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(9, scn.GetStrength(brute));

		scn.FreepsPass();
		scn.ShadowPlayCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);

		// An [orc] minion who is no Orc: each companion, of three cultures and races, -1; not the ally or the
		// minions.
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(9, scn.GetStrength(brute));
		assertEquals(8, scn.GetStrength(halftroll));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcOrcIsAnOrcMinionToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var clash = scn.GetShadowCard("clash");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToSupportArea(clash);
		assertInZone(Zone.SUPPORT, clash);

		scn.StartGame();
		assertEquals(8, scn.GetStrength(aragorn));
		scn.MoveMinionsToTable(denizen);

		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(denizen));
	}
}
