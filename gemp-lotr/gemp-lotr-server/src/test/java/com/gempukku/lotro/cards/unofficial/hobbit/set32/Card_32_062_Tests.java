package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_062_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "32_62");

					// Minions whose site number is read
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, site 4; its text needs 2 wounds on the Ring-bearer
					put("cantea", "32_60");     // Úlairë Cantëa, Revived: a [wraith] Nazgûl, cost 5, site 5; her text answers a follower transferred
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, site 3; his text needs 3 or 5 burdens
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: a [gundabad] Orc, site 3; a maneuver ability not used

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, the Dwarf who pays Ancestral Knowledge
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal with nothing to heal, a regroup ability not used
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally; a regroup ability not used

					// Discards of a condition, and of a card that is none
					put("knowledge", "30_1");   // Ancestral Knowledge: [dwarven] Maneuver event, exert a Dwarf character to discard a condition
					put("tale", "1_317");       // There and Back Again: [shire] condition on a Hobbit companion, "Maneuver: Discard this condition to ..."
					put("acorn", "31_38");      // An Acorn from Beorn's house: [shire] possession on Bilbo, "Regroup: Discard this possession to remove a doubt and discard a Shadow condition"
					put("hatred", "30_34");     // Hatred Rekindled: [gundabad] support-area condition; a Shadow ability not used
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] support-area condition, "Skirmish: Discard this condition to make an Uruk-hai strength +2"
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Úlairë Lemenya, Revived
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
		 * Each time the Free Peoples player discards a condition, you may discard an ally.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", card.getBlueprint().getTitle());
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

		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var cantea = scn.GetShadowCard("cantea");
		var khamul = scn.GetShadowCard("khamul");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToHand(lemenya, cantea);
		assertInHand(lemenya, cantea);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, khamul, yazneg);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInHand(lemenya, cantea);
		assertEquals(4, scn.GetMinionSiteNumber(hound));
		assertEquals(3, scn.GetMinionSiteNumber(khamul));
		assertEquals(3, scn.GetMinionSiteNumber(yazneg));
		assertTrue(scn.HasKeyword(hound, Keyword.ROAMING));

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(lemenya);
		// 4 to play + 2 roaming.  Her text does not name her, so it is not active while she is played (CR 4.2
		// "modifier"): at site 2 her site number is still 5.  Had her -3 applied she would have cost 4.
		assertEquals(pool - 6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);

		// Once in play it applies to each [wraith] minion, herself included, of either race: 5 - 3 and 4 - 3.
		assertEquals(2, scn.GetMinionSiteNumber(lemenya));
		assertFalse(scn.HasKeyword(lemenya, Keyword.ROAMING));
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
		assertEquals(pool - 5, scn.GetTwilight());     // lint-ok: Lemenya's -3 takes the roaming penalty off Cantëa's cost
		assertInZone(Zone.SHADOW_CHARACTERS, cantea);
		assertEquals(2, scn.GetMinionSiteNumber(cantea));
		assertFalse(scn.HasKeyword(cantea, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenAFreePeoplesEventDiscardsAShadowConditionLemenyaMayDiscardAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var hatred = scn.GetShadowCard("hatred");
		var knowledge = scn.GetFreepsCard("knowledge");
		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond, bard);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(lemenya);
		assertInZone(Zone.SUPPORT, hatred);
		assertInZone(Zone.SUPPORT, elrond);
		assertInZone(Zone.SUPPORT, bard);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// Thorin is the one Dwarf to exert and Hatred Rekindled the one condition: nothing is asked.
		scn.FreepsPlayCard(knowledge);
		assertInZone(Zone.DISCARD, hatred);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, bard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thorin, bilbo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(elrond);
		assertInZone(Zone.DISCARD, elrond);
		assertInZone(Zone.SUPPORT, bard);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningLeavesTheAlliesAndTheNextConditionDiscardedOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var hatred = scn.GetShadowCard("hatred");
		var knowledge = scn.GetFreepsCard("knowledge");
		var tale = scn.GetFreepsCard("tale");
		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, tale);
		scn.MoveAlliesToTable(elrond, bard);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(lemenya);
		assertAttachedTo(tale, bilbo);
		assertInZone(Zone.SUPPORT, hatred);
		assertInZone(Zone.SUPPORT, elrond);
		assertInZone(Zone.SUPPORT, bard);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Free Peoples player discards his own [shire] condition, as the cost of its own ability.
		scn.FreepsUseCardAction(tale);
		assertInZone(Zone.DISCARD, tale);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, elrond);
		assertInZone(Zone.SUPPORT, bard);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(knowledge);
		assertInZone(Zone.DISCARD, hatred);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(bard);
		assertInZone(Zone.DISCARD, bard);
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerDiscardingHisOwnConditionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var ambition = scn.GetShadowCard("ambition");
		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(ambition);
		scn.MoveMinionsToTable(lemenya);
		assertInZone(Zone.SUPPORT, ambition);
		assertInZone(Zone.SUPPORT, elrond);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, lemenya);
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		// No Uruk-hai is in play: the condition is discarded for nothing.
		scn.ShadowUseCardAction(ambition);
		assertInZone(Zone.DISCARD, ambition);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDiscardingAPossessionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var acorn = scn.GetFreepsCard("acorn");
		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.AttachCardsTo(bilbo, acorn);
		scn.MoveAlliesToTable(elrond);
		scn.SkipToPhase(Phase.REGROUP);
		// Lemenya lived through the skirmishes; no Shadow condition is in play for the Acorn to discard.
		scn.MoveMinionsToTable(lemenya);
		scn.AddBurdens(1);
		assertAttachedTo(acorn, bilbo);
		assertInZone(Zone.SUPPORT, elrond);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(acorn);
		assertInZone(Zone.DISCARD, acorn);
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoAllyInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var tale = scn.GetFreepsCard("tale");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, tale);
		scn.MoveMinionsToTable(lemenya);
		assertAttachedTo(tale, bilbo);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(tale);
		assertInZone(Zone.DISCARD, tale);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
