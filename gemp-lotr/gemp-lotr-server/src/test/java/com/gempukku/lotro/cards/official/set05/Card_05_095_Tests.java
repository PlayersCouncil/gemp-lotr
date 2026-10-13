package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marshes", "5_95");
					put("soldier1", "1_271");     // Orc Soldier: [sauron] Orc
					put("soldier2", "1_271");
					put("soldier3", "1_271");
					put("shadow", "1_252");       // The Irresistible Shadow: [sauron] condition, cost 0; to play, exert a [sauron] Orc
					put("armory", "1_173");       // Goblin Armory: [moria] condition, cost 0
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 4 is White Rocks 4_346, a battleground whose text reads mounted companions only.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marshes", "5_95");
					put("trooper1", "5_98");      // Gate Trooper: [sauron] Orc, strength 5; its text answers only its own play
					put("trooper2", "5_98");
					put("trooper3", "5_98");
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, damage +1 and nothing else
					put("hound", "7_192");        // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("faramir", "4_117");      // Faramir, Son of Denethor: Ring-bound [gondor] Man, strength 7
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound companion, strength 8
					put("strength", "1_278");     // Strength Born of Fear: [sauron] support-area condition, cost 1
					put("banner", "1_263");       // Orc Banner: [sauron] support-area condition, cost 1
					put("evil", "1_283");         // You Bring Great Evil: [sauron] condition on a companion (not an Elf), cost 1; to play, spot a [sauron] Orc
					put("seeking", "1_276");      // Seeking Its Master: [sauron] support-area condition, cost 1 (the Free Peoples player's copy, in his own pile)
					put("armory", "1_173");       // Goblin Armory: [moria] support-area condition, cost 0
					put("chieftain", "1_266");    // Orc Chieftain: a [sauron] card that is no condition (a minion, cost 2)
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeadMarshesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Dead Marshes
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * While the fellowship is at site 4[T], Ring-bound companions skirmishing [sauron] Orcs are strength -1.
		 * Maneuver: Spot 2 [sauron] Orcs to play up to 2 [sauron] conditions from your discard pile. Discard this condition.
		*/

		var scn = GetScenario();

		var marshes = scn.GetFreepsCard("marshes");

		assertEquals("Dead Marshes", marshes.getBlueprint().getTitle());
		assertNull(marshes.getBlueprint().getSubtitle());
		assertTrue(marshes.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, marshes.getBlueprint().getSide());
		assertEquals(Culture.SAURON, marshes.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, marshes.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(marshes, Keyword.SUPPORT_AREA));
		assertEquals(0, marshes.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite4OfTheTowersPathEachRingBoundCompanionIsStrengthMinus1OnlyWhileHeIsSkirmishingASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);

		var marshes = scn.GetShadowCard("marshes");
		var trooper1 = scn.GetShadowCard("trooper1");
		var trooper2 = scn.GetShadowCard("trooper2");
		var trooper3 = scn.GetShadowCard("trooper3");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(marshes);
		scn.MoveMinionsToTable(trooper1, trooper2, trooper3);

		// At site 3, in no skirmish: Frodo is 3 + 1 for The Ruling Ring.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, trooper1},
				new PhysicalCardImpl[]{frodo, trooper2},
				new PhysicalCardImpl[]{aragorn, trooper3});
		// Assigned, and no skirmish yet: nobody is skirmishing.
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(4, scn.GetStrength(frodo));

		scn.FreepsResolveSkirmish(faramir);
		// Faramir, a Ring-bound Man, is skirmishing a [sauron] Orc; Frodo is assigned to one and not yet skirmishing.
		assertEquals(6, scn.GetStrength(faramir));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// Faramir, 6, beats the Trooper, 5; the bonus is his only while he skirmishes.
		assertEquals(7, scn.GetStrength(faramir));
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn is unbound.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(frodo);
		// The Ring-bearer, Ring-bound by rule, in his own skirmish against a [sauron] Orc.
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARingBoundCompanionSkirmishingASauronUrukHaiAndAWraithOrcIsNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);

		var marshes = scn.GetShadowCard("marshes");
		var trooper1 = scn.GetShadowCard("trooper1");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(marshes);
		scn.MoveMinionsToTable(trooper1, guard, hound);

		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, guard},
				new PhysicalCardImpl[]{frodo, trooper1});
		scn.ShadowAssignToMinions(faramir, hound);

		scn.FreepsResolveSkirmish(faramir);
		// A [sauron] minion who is no Orc and an Orc who is not [sauron]: neither is a [sauron] Orc.
		assertEquals(7, scn.GetStrength(faramir));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(frodo);
		// The same table's Ring-bearer against a [sauron] Orc.
		assertEquals(3, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite5OfTheTowersPathRingBoundCompanionsSkirmishingSauronOrcsAreNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);

		var marshes = scn.GetShadowCard("marshes");
		var trooper1 = scn.GetShadowCard("trooper1");
		var trooper2 = scn.GetShadowCard("trooper2");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(marshes);
		scn.MoveMinionsToTable(trooper1, trooper2);

		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, trooper1},
				new PhysicalCardImpl[]{frodo, trooper2});

		scn.FreepsResolveSkirmish(faramir);
		assertEquals(7, scn.GetStrength(faramir));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(frodo);
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite4OfTheFellowshipPathTheRingBearerSkirmishingASauronOrcIsNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var marshes = scn.GetShadowCard("marshes");
		var soldier1 = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(marshes);
		scn.MoveMinionsToTable(soldier1);

		scn.SkipToAssignments();
		// Balin's Tomb: a site 4, of the Fellowship block.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, soldier1);

		scn.FreepsResolveSkirmish(frodo);
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ManeuverActionSpotsTwoSauronOrcsToPlayUpToTwoSauronConditionsFromTheDiscardPileAndDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var seeking = scn.GetFreepsCard("seeking");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(seeking);

		var marshes = scn.GetShadowCard("marshes");
		var trooper1 = scn.GetShadowCard("trooper1");
		var soldier = scn.GetShadowCard("soldier");
		var strength = scn.GetShadowCard("strength");
		var banner = scn.GetShadowCard("banner");
		var evil = scn.GetShadowCard("evil");
		var armory = scn.GetShadowCard("armory");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToDiscard(strength, banner, evil, armory, chieftain);

		scn.StartGame();
		scn.MoveCardsToSupportArea(marshes);
		// Two [sauron] Orcs of two titles.
		scn.MoveMinionsToTable(trooper1, soldier);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(marshes));
		scn.ShadowUseCardAction(marshes);

		// The [sauron] conditions of his own discard pile, of either kind: to the support area and onto a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(strength, banner, evil));
		// Not a condition of another culture, a [sauron] card that is no condition, or one in the other player's pile.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, chieftain, seeking));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(evil);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		scn.ShadowChooseCard(aragorn);
		assertAttachedTo(evil, aragorn);

		assertTrue(scn.ShadowHasCardChoiceAvailable(strength, banner));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(banner);

		// Two and no more: the third stays where it was.
		assertInZone(Zone.SUPPORT, banner);
		assertInDiscard(strength, armory, chieftain, marshes);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedUntilTwoSauronOrcsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var marshes = scn.GetShadowCard("marshes");
		var trooper1 = scn.GetShadowCard("trooper1");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var strength = scn.GetShadowCard("strength");
		scn.MoveCardsToDiscard(strength);

		scn.StartGame();
		scn.MoveCardsToSupportArea(marshes);
		// One [sauron] Orc, a [sauron] Uruk-hai and a [wraith] Orc.
		scn.MoveMinionsToTable(trooper1, guard, hound);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marshes));

		// A second [sauron] Orc.
		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(marshes));
	}

	@Test
	public void ManeuverActionIsNotOfferedUnlessASauronConditionInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var marshes = scn.GetShadowCard("marshes");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var soldier3 = scn.GetShadowCard("soldier3");
		var shadow = scn.GetShadowCard("shadow");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(marshes);
		//A condition that could be played, but is not a [sauron] one
		scn.MoveCardsToDiscard(armory);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2);
		//Vitality 2 with 1 wound: both Orcs are exhausted, and are spotted all the same
		scn.AddWoundsToChar(soldier1, 1);
		scn.AddWoundsToChar(soldier2, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		//Playing from the discard pile is treated as a cost: with nothing to play, the ability is withheld
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marshes));

		//A [sauron] condition, but its play exerts a [sauron] Orc and neither Orc can
		scn.MoveCardsToDiscard(shadow);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marshes));

		//A third Orc, who can exert, lets it be played
		scn.MoveMinionsToTable(soldier3);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(marshes));
	}
}
