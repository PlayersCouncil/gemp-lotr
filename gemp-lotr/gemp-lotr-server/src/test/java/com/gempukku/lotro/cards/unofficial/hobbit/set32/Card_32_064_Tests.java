package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_064_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "32_64");

					// Minions whose site number is read
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, site 4; its text needs 2 wounds on the Ring-bearer
					put("cantea", "32_60");     // Úlairë Cantëa, Revived: a [wraith] Nazgûl, cost 5, site 5; her text answers a follower transferred
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, site 3; his text needs 3 or 5 burdens
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: a [gundabad] Orc, site 3; a maneuver ability not used

					// Cards in the discard pile, to be played and not
					put("witchking", "32_67");  // The Witch-King, Revived: a [wraith] Nazgûl, cost 8, site 5; a cost discount for Wise characters (none here), a skirmish ability not used
					put("blade", "32_56");      // Dol Guldur Blade: a [wraith] possession that a [wraith] minion may bear; "When you play this possession, add (2)"
					put("clash", "17_68");      // Chaotic Clash: [orc] support-area condition, "Each time you play a minion, it is twilight cost +1"; its strength text wants an [orc] minion

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, the Dwarf who pays Ancestral Knowledge

					// Discards of a condition, and of a card that is none
					put("knowledge", "30_1");   // Ancestral Knowledge: [dwarven] Maneuver event, exert a Dwarf character to discard a condition
					put("acorn", "31_38");      // An Acorn from Beorn's house: [shire] possession on Bilbo, "Regroup: Discard this possession to remove a doubt and discard a Shadow condition"
					put("hatred", "30_34");     // Hatred Rekindled: [gundabad] support-area condition; a Shadow ability not used
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] support-area condition, "Skirmish: Discard this condition to make an Uruk-hai strength +2"
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Úlairë Nertëa, Revived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * The site number of each [wraith] minion is -3.
		 * <b>Response:</b> If the Free Peoples player discards a condition, exert Úlairë Nertëa to play a [wraith] minion from your discard pile; its twilight cost is -8.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
		assertEquals("Revived", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedAtHerOwnSiteNumberAndOnceInPlayMakesEachWraithMinionSiteNumberMinusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var hound = scn.GetShadowCard("hound");
		var cantea = scn.GetShadowCard("cantea");
		var khamul = scn.GetShadowCard("khamul");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToHand(nertea, cantea);
		assertInHand(nertea, cantea);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, khamul, yazneg);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInHand(nertea, cantea);
		assertEquals(4, scn.GetMinionSiteNumber(hound));
		assertEquals(3, scn.GetMinionSiteNumber(khamul));
		assertEquals(3, scn.GetMinionSiteNumber(yazneg));
		assertTrue(scn.HasKeyword(hound, Keyword.ROAMING));

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(nertea);
		// 4 to play + 2 roaming.  Her text does not name her, so it is not active while she is played (CR 4.2
		// "modifier"): at site 2 her site number is still 5.  Had her -3 applied she would have cost 4.
		assertEquals(pool - 6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);

		// Once in play it applies to each [wraith] minion, herself included, of either race: 5 - 3 and 4 - 3.
		assertEquals(2, scn.GetMinionSiteNumber(nertea));
		assertFalse(scn.HasKeyword(nertea, Keyword.ROAMING));
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
		assertEquals(pool - 5, scn.GetTwilight());     // lint-ok: Nertëa's -3 takes the roaming penalty off Cantëa's cost
		assertInZone(Zone.SHADOW_CHARACTERS, cantea);
		assertEquals(2, scn.GetMinionSiteNumber(cantea));
		assertFalse(scn.HasKeyword(cantea, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}


	@Test
	public void WhenAFreePeoplesEventDiscardsAShadowConditionExertingNerteaPlaysAWraithMinionFromTheDiscardPileForEightLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var yazneg = scn.GetShadowCard("yazneg");
		var blade = scn.GetShadowCard("blade");
		var clash = scn.GetShadowCard("clash");
		var hatred = scn.GetShadowCard("hatred");
		var knowledge = scn.GetFreepsCard("knowledge");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveCardsToDiscard(witchking, hound, khamul, yazneg, blade);
		scn.MoveCardsToSupportArea(hatred, clash);
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		assertInDiscard(witchking, hound, khamul, yazneg, blade);
		assertInZone(Zone.SUPPORT, hatred);
		assertInZone(Zone.SUPPORT, clash);
		assertEquals(0, scn.GetWoundsOn(nertea));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// Thorin is the one Dwarf to exert; of the two Shadow conditions, Hatred Rekindled goes.
		scn.FreepsPlayCard(knowledge);
		scn.FreepsChooseCard(hatred);
		assertInZone(Zone.DISCARD, hatred);
		assertInZone(Zone.SUPPORT, clash);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		int pool = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(nertea));
		// [wraith] minions of two races; not the [raider] Nazgûl, the [gundabad] Orc, nor the [wraith] possession,
		// each of which could be paid for and borne.
		assertTrue(scn.ShadowHasCardChoiceAvailable(witchking, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, yazneg, blade));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(witchking);
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		// 8 to play + 1 Chaotic Clash - 8; site number 5 - 3 = 2 at site 2, so no roaming penalty.
		assertEquals(pool - 1, scn.GetTwilight());
		assertFalse(scn.HasKeyword(witchking, Keyword.ROAMING));
		assertInDiscard(hound, khamul, yazneg, blade);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerDiscardingHisOwnConditionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var ambition = scn.GetShadowCard("ambition");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveCardsToDiscard(witchking);
		scn.MoveCardsToSupportArea(ambition);
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		assertInDiscard(witchking);
		assertInZone(Zone.SUPPORT, ambition);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, nertea);
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		// No Uruk-hai is in play: the condition is discarded for nothing.
		scn.ShadowUseCardAction(ambition);
		assertInZone(Zone.DISCARD, ambition);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertInDiscard(witchking);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDiscardingAPossessionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var acorn = scn.GetFreepsCard("acorn");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.AttachCardsTo(bilbo, acorn);
		scn.SkipToPhase(Phase.REGROUP);
		// Nertëa lived through the skirmishes; no Shadow condition is in play for the Acorn to discard.
		scn.MoveMinionsToTable(nertea);
		scn.MoveCardsToDiscard(witchking);
		scn.SetTwilight(20);
		scn.AddBurdens(1);
		assertAttachedTo(acorn, bilbo);
		assertInDiscard(witchking);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(acorn);
		assertInZone(Zone.DISCARD, acorn);
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ExhaustedNerteaCannotRespond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var hatred = scn.GetShadowCard("hatred");
		var knowledge = scn.GetFreepsCard("knowledge");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveCardsToDiscard(witchking);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		// Vitality 2: one wound exhausts her.
		scn.AddWoundsToChar(nertea, 1);
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertInDiscard(witchking);
		assertInZone(Zone.SUPPORT, hatred);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(knowledge);
		assertInZone(Zone.DISCARD, hatred);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertInDiscard(witchking);
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoWraithMinionInTheDiscardPileNerteaCannotRespond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var khamul = scn.GetShadowCard("khamul");
		var yazneg = scn.GetShadowCard("yazneg");
		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var hatred = scn.GetShadowCard("hatred");
		var knowledge = scn.GetFreepsCard("knowledge");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveCardsToDiscard(khamul, yazneg, blade);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(nertea);
		scn.MoveCardsToHand(witchking);
		scn.SetTwilight(20);
		assertInDiscard(khamul, yazneg, blade);
		assertInHand(witchking);
		assertInZone(Zone.SUPPORT, hatred);
		assertEquals(0, scn.GetWoundsOn(nertea));
		scn.SkipToPhase(Phase.MANEUVER);

		// A [wraith] minion in hand is not in the discard pile.
		scn.FreepsPlayCard(knowledge);
		assertInZone(Zone.DISCARD, hatred);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertInHand(witchking);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
