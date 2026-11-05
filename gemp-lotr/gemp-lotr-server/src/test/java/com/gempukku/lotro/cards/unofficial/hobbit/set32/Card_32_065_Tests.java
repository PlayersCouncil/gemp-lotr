package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_065_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "32_65");

					// Minions whose site number is read
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, site 4; its text needs 2 wounds on the Ring-bearer
					put("cantea", "32_60");     // Úlairë Cantëa, Revived: a [wraith] Nazgûl, cost 5, site 5; her text answers a follower transferred
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, site 3; his text needs 3 or 5 burdens
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: a [gundabad] Orc, site 3; a maneuver ability not used

					// Companions bearing a follower, and not
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion; his texts reach no test here
					put("thrush", "31_42");     // Old Thrush, Messenger Bird: [shire] follower, "Aid - Exert Bilbo"; a maneuver ability not used
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower; his trigger wants a fierce skirmish won
					put("acorn", "31_38");      // An Acorn from Beorn's house: [shire] possession on Bilbo; a regroup ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Úlairë Otsëa, Revived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * The site number of each [wraith] minion is -3.
		 * While you can spot a companion bearing a follower, each [wraith] minion is strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("otsea");

		assertEquals("Úlairë Otsëa", card.getBlueprint().getTitle());
		assertEquals("Revived", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedAtHerOwnSiteNumberAndOnceInPlayMakesEachWraithMinionSiteNumberMinusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var hound = scn.GetShadowCard("hound");
		var cantea = scn.GetShadowCard("cantea");
		var khamul = scn.GetShadowCard("khamul");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToHand(otsea, cantea);
		assertInHand(otsea, cantea);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, khamul, yazneg);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInHand(otsea, cantea);
		assertEquals(4, scn.GetMinionSiteNumber(hound));
		assertEquals(3, scn.GetMinionSiteNumber(khamul));
		assertEquals(3, scn.GetMinionSiteNumber(yazneg));
		assertTrue(scn.HasKeyword(hound, Keyword.ROAMING));

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(otsea);
		// 4 to play + 2 roaming.  Her text does not name her, so it is not active while she is played (CR 4.2
		// "modifier"): at site 2 her site number is still 5.  Had her -3 applied she would have cost 4.
		assertEquals(pool - 6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);

		// Once in play it applies to each [wraith] minion, herself included, of either race: 5 - 3 and 4 - 3.
		assertEquals(2, scn.GetMinionSiteNumber(otsea));
		assertFalse(scn.HasKeyword(otsea, Keyword.ROAMING));
		assertEquals(1, scn.GetMinionSiteNumber(hound));
		assertFalse(scn.HasKeyword(hound, Keyword.ROAMING));
		// The [raider] Nazgûl and the [gundabad] Orc are where they were.
		assertEquals(3, scn.GetMinionSiteNumber(khamul));
		assertEquals(3, scn.GetMinionSiteNumber(yazneg));
		assertTrue(scn.HasKeyword(khamul, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(yazneg, Keyword.ROAMING));

		// Cantëa in hand is a [wraith] minion too: site number 5 - 3 = 2, so at site 2 she is not roaming and
		// costs 5, not 5 + 2.
		pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(cantea));
		scn.ShadowPlayCard(cantea);
		assertEquals(pool - 5, scn.GetTwilight());     // lint-ok: Otsëa's -3 takes the roaming penalty off Cantëa's cost
		assertInZone(Zone.SHADOW_CHARACTERS, cantea);
		assertEquals(2, scn.GetMinionSiteNumber(cantea));
		assertFalse(scn.HasKeyword(cantea, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}


	@Test
	public void AFollowerTransferredToACompanionMakesEachWraithMinionStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var yazneg = scn.GetShadowCard("yazneg");
		var thrush = scn.GetFreepsCard("thrush");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveCardsToSupportArea(thrush);
		scn.MoveMinionsToTable(otsea, hound, khamul, yazneg);
		assertInZone(Zone.SUPPORT, thrush);
		scn.SkipToPhase(Phase.MANEUVER);

		// Old Thrush lies in the support area, borne by nobody.
		assertEquals(9, scn.GetStrength(otsea));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(yazneg));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(thrush, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		// Bilbo, the one companion, pays the Aid and bears him.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertAttachedTo(thrush, bilbo);

		// Each [wraith] minion, of either race, Otsëa herself included; not the [raider] Nazgûl nor the [gundabad] Orc.
		assertEquals(10, scn.GetStrength(otsea));
		assertEquals(6, scn.GetStrength(hound));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(yazneg));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void APossessionOnACompanionOrAnUnborneFollowerGivesNothingButAFollowerOnAnotherCompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var hound = scn.GetShadowCard("hound");
		var thrush = scn.GetFreepsCard("thrush");
		var bifur = scn.GetFreepsCard("bifur");
		var acorn = scn.GetFreepsCard("acorn");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, acorn);
		scn.MoveCardsToSupportArea(thrush);
		scn.MoveMinionsToTable(otsea, hound);
		assertAttachedTo(acorn, bilbo);
		assertInZone(Zone.SUPPORT, thrush);

		// Bilbo bears a possession, and Old Thrush is in the support area.
		assertEquals(9, scn.GetStrength(otsea));
		assertEquals(5, scn.GetStrength(hound));

		scn.AttachCardsTo(thorin, bifur);
		assertAttachedTo(bifur, thorin);
		// A [dwarven] follower on a Dwarf companion: each [wraith] minion is +1.
		assertEquals(10, scn.GetStrength(otsea));
		assertEquals(6, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
