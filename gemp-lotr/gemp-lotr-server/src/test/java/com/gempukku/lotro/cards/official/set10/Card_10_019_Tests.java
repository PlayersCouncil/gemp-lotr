package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sprang", "10_19");
					// Gollum, Dark as Darkness: [gollum], cost 2, strength 5, vitality 4, site 3. His skirmish ability needs a burden.
					put("gollum", "9_28");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum, cost 2, strength 5, vitality 4, site 3. His texts
					// are about Nazgûl being played and about threats.
					put("wraithgollum", "103_66");
					put("gollum2", "9_28");     // a second Gollum, Dark as Darkness, to lie beside the [wraith] one in a pile
					put("shelob", "8_25");      // Shelob, Eater of Light: a [gollum] minion who is not Gollum
					put("clash", "17_68");      // Chaotic Clash: [orc] support-area condition, "Each time you play a minion, it is twilight cost +1."
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase

					put("sword", "1_299");      // Hobbit Sword: [shire] possession, hand weapon, cost 1, strength +2; bearer must be a Hobbit
					put("bounder", "1_286");    // Bounder: a Hobbit who is an ally; a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf, strength 6, damage +1; a skirmish ability used only on purpose
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, armor, cost 0; bearer must be a Dwarf; changes no number
					put("ring", "9_8");         // Ring of Guile: a [dwarven] ARTIFACT a Dwarf bears, cost 0
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("saga", "1_114");       // The Saga of Elendil: a CONDITION a [gondor] companion bears, cost 1
					put("eowyn", "5_122");      // Éowyn, Daughter of Éomund: strength 6; "Maneuver: Play a [rohan] possession on Éowyn to heal her."
					put("spear", "4_288");      // Rider's Spear: [rohan] possession, hand weapon, cost 1, strength +2; bearer must be a [rohan] Man
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ADarkShapeSprangStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: A Dark Shape Sprang
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If an opponent plays a possession on a companion, play Gollum at twilight cost -2 from your
		 * discard pile or hand to suspend the current phase. Begin a skirmish phase involving Gollum and that companion.
		 * When it ends, resume the suspended phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sprang");

		assertEquals("A Dark Shape Sprang", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GollumIsPlayedFromHandForTwoLessToSkirmishTheCompanionGivenThePossessionAndTheFellowshipPhaseGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sprang = scn.GetShadowCard("sprang");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(sprang, gollum);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sword, saga);

		scn.StartGame();

		scn.SetTwilight(9);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);

		assertTrue(scn.ShadowPlayAvailable(sprang));
		assertInHand(gollum);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(10, scn.GetTwilight());
		scn.ShadowPlayCard(sprang);

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		// 9 at the start + 1 for the sword = 10; Gollum costs 2 - 2, and 2 for roaming at site 1
		assertEquals(8, scn.GetTwilight());

		// A skirmish in the middle of the fellowship phase, between Gollum and the companion who was given the sword.
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, gollum));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// Frodo: 3 + 2 for the sword + 1 for the Ring, against 5.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInDiscard(sprang);

		// The fellowship phase is taken up where it was left: the Free Peoples player plays his next card.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(saga));
		scn.FreepsPlayCard(saga);
		assertAttachedTo(saga, aragorn);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GollumMayBePlayedFromTheDiscardPileInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sprang = scn.GetShadowCard("sprang");
		var gollum = scn.GetShadowCard("gollum");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var gollum2 = scn.GetShadowCard("gollum2");
		var shelob = scn.GetShadowCard("shelob");
		var clash = scn.GetShadowCard("clash");
		scn.MoveCardsToHand(sprang, gollum);
		scn.MoveCardsToDiscard(wraithgollum, gollum2, shelob);
		scn.MoveCardsToSupportArea(clash);

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		// Enough for Shelob as well (6 + 1 - 2, and 2 for roaming), so that only her title keeps her from being offered.
		scn.SetTwilight(9);
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);

		assertTrue(scn.ShadowPlayAvailable(sprang));
		scn.ShadowPlayCard(sprang);

		// A Gollum lies in each place: the Shadow player says which.
		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChoose("Play Gollum from discard");

		// Both Gollums in the discard pile, of two cultures, are offered; Shelob, a [gollum] minion of another title, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum2, wraithgollum));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(wraithgollum);

		assertInZone(Zone.SHADOW_CHARACTERS, wraithgollum);
		assertInHand(gollum);
		assertInDiscard(gollum2, shelob);
		// 9 + 0 for the armor; beside Chaotic Clash Gollum costs 2 + 1 - 2, and 2 for roaming at site 1
		assertEquals(6, scn.GetTwilight());

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, wraithgollum));
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Gimli, 6 and damage +1, beats 5.
		assertEquals(2, scn.GetWoundsOn(wraithgollum));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsOfferedOnlyWhenAPossessionIsPlayedOnACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sprang = scn.GetShadowCard("sprang");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(sprang, gollum);

		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var ring = scn.GetFreepsCard("ring");
		var saga = scn.GetFreepsCard("saga");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sword, ring, saga, armor);

		scn.StartGame();
		scn.SetTwilight(9);

		// A possession played on an ally.
		scn.FreepsPlayCard(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bounder));
		scn.FreepsChooseCard(bounder);
		assertAttachedTo(sword, bounder);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An artifact played on a companion.
		scn.FreepsPlayCard(ring);
		assertAttachedTo(ring, gimli);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A condition played on a companion.
		scn.FreepsPlayCard(saga);
		assertAttachedTo(saga, aragorn);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A possession played on a companion.
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertInHand(gollum);
		assertTrue(scn.ShadowPlayAvailable(sprang));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void BesideChaoticClashGollumCostsOneAndHisRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sprang = scn.GetShadowCard("sprang");
		// The [wraith] Gollum, so that the hand holds one of each culture across the tests; his cost is the other's.
		var gollum = scn.GetShadowCard("wraithgollum");
		var clash = scn.GetShadowCard("clash");
		scn.MoveCardsToHand(sprang, gollum);
		scn.MoveCardsToSupportArea(clash);

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(armor, sword);

		scn.StartGame();
		scn.SetTwilight(2);

		// Chaotic Clash makes a minion cost 1 more: Gollum is 2 + 1 - 2, and 2 for roaming at site 1. With 2 in
		// the pool he cannot be played, and the event is not offered.
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The sword adds 1: with 3 it is.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(sprang));
		scn.ShadowPlayCard(sprang);

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, gollum));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoGollumInHandOrDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sprang = scn.GetShadowCard("sprang");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(sprang, shelob);

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(armor);

		scn.StartGame();
		// Enough for Shelob, a [gollum] minion of cost 6, roaming.
		scn.SetTwilight(20);

		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertInHand(sprang, shelob);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InAManeuverPhaseTheSkirmishIsFoughtAndTheManeuverPhaseGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sprang = scn.GetShadowCard("sprang");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(sprang, gollum);

		var eowyn = scn.GetFreepsCard("eowyn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(runner);
		scn.AddWoundsToChar(eowyn, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		// 10 set + 2 companions + 2 for the site
		assertEquals(14, scn.GetTwilight());

		// "Maneuver: Play a [rohan] possession on Éowyn to heal her."
		scn.FreepsUseCardAction(eowyn);
		assertAttachedTo(spear, eowyn);
		assertTrue(scn.ShadowPlayAvailable(sprang));
		scn.ShadowPlayCard(sprang);

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		// 14 + 1 for the spear; Gollum costs 2 - 2, and 2 for roaming at site 2
		assertEquals(13, scn.GetTwilight());
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(eowyn, gollum));
		assertFalse(scn.IsCharSkirmishing(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Éowyn, 6 + 2 for the spear, beats 5.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(runner));

		// The maneuver phase goes on: Éowyn's ability, whose cost the spear was, heals her, and the Shadow player acts next.
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
