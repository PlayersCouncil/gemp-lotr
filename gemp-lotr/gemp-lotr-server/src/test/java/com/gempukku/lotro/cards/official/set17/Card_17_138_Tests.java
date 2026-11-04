package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_138_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pain", "17_138");
					// Southron Explorer: [raider] Man, vitality 3. "Regroup: Exert this minion and remove (5) to take
					// control of a site."
					put("explorer1", "4_250");
					put("explorer2", "4_250");
					// White Hand Butcher: [uruk-hai] Uruk-hai, strength 5; hunter 6 is its whole text
					put("butcher1", "17_122");
					put("butcher2", "17_122");
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard); each
					// companion is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; damage +1 is its whole text
					put("guard", "10_81");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, unbound, vitality 3
					put("boromir", "1_97");     // Boromir, Son of Denethor: unbound, vitality 3
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteTakenByPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pain", "17_138");
					// White Hand Intruder: [uruk-hai] Uruk-hai, strength 9, vitality 2, damage +1. "Each time this
					// minion wins a skirmish, if you do not control a site, you may take control of a site."
					put("intruder", "17_127");
					// White Hand Aggressor: [uruk-hai] Uruk-hai, strength 13, vitality 1, damage +1; while its
					// owner controls a site it takes wounds only in the skirmish phase
					put("aggressor1", "17_119");
					put("aggressor2", "17_119");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound, vitality 3
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YouDoNotKnowPainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: You Do Not Know Pain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time you take control of a site, you may spot 3 [uruk-hai] minions to exert an unbound companion.<br><b>Skirmish:</b> Liberate a site to make an [uruk-hai] minion <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pain");

		assertEquals("You Do Not Know Pain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TakingControlOfASiteWithThreeUrukHaiMinionsSpottedMayExertAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteTakenByPlayScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);

		var pain = scn.GetShadowCard("pain");
		var intruder = scn.GetShadowCard("intruder");
		var aggressor1 = scn.GetShadowCard("aggressor1");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		scn.MoveCardsToSupportArea(pain);

		// Both markers are at site 2 when the fellowship moves on to site 3: site 1 can be controlled.
		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(intruder, aggressor1, aggressor2);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, intruder);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// The Intruder, 9, beats Aragorn, 8: a wound and one more for damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pain));

		// The control is taken by the Intruder's own text, and that is what this condition answers.
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pain));

		// The unbound companions may be chosen, the one already wounded among them; the Ring-bearer and Sam,
		// who are Ring-bound, may not.
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Ketura on Q-0034: an optional trigger with no cost whose whole effect would do nothing is not offered, and
	// a spot pays nothing.  With no unbound companion who can exert, a yes would do nothing.  The game used to
	// offer it all the same (F-0377).
	@Test
	public void WithNoUnboundCompanionAbleToExertItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteTakenByPlayScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var pain = scn.GetShadowCard("pain");
		var intruder = scn.GetShadowCard("intruder");
		var aggressor1 = scn.GetShadowCard("aggressor1");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		scn.MoveCardsToSupportArea(pain);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(intruder, aggressor1, aggressor2);
		// One wound already, and two more from the skirmish he is about to lose: Aragorn, the only unbound
		// companion, will be exhausted.
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, intruder);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetVitality(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));

		// A site is taken and three [uruk-hai] minions can be spotted, but the exertion could reach nobody: the
		// one unbound companion cannot exert, and Frodo and Sam are Ring-bound.
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pain));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachSiteHeTakesIsAnsweredByAnOfferToExertAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveAlliesToTable(barliman);

		var pain = scn.GetShadowCard("pain");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var butcher1 = scn.GetShadowCard("butcher1");
		var butcher2 = scn.GetShadowCard("butcher2");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(pain);

		scn.StartGame();
		// Both markers are at site 3 as the fellowship moves on to site 4: sites 1 and 2 can be controlled.
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that came through the skirmishes: two Explorers to take two sites, and three [uruk-hai]
		// minions, one of them no Uruk-hai.  Boromir is exhausted.
		scn.MoveMinionsToTable(explorer1, explorer2, butcher1, butcher2, saruman);
		scn.AddWoundsToChar(boromir, 2);
		scn.SetTwilight(10);

		assertInZone(Zone.SUPPORT, pain);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer1);

		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pain));
		scn.ShadowAcceptOptionalTrigger();

		// Unbound companions of two cultures; not the exhausted Boromir, nor the Ring-bound Frodo and Sam, nor an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, frodo, sam, barliman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(2, scn.GetWoundsOn(boromir));

		// A second site taken in the same phase is answered again; declined, it exerts nobody.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer2);

		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pain));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoUrukHaiMinionsTakingASiteOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var pain = scn.GetShadowCard("pain");
		var explorer1 = scn.GetShadowCard("explorer1");
		var butcher1 = scn.GetShadowCard("butcher1");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(pain);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);
		// Two [uruk-hai] minions; beside them a [sauron] Uruk-hai, a [raider] minion and this [uruk-hai] condition.
		scn.MoveMinionsToTable(explorer1, butcher1, saruman, guard);
		scn.SetTwilight(10);

		assertInZone(Zone.SUPPORT, pain);
		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer1);

		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityLiberatesTheSiteOfHisOwnHeChoosesToMakeAnUrukHaiMinionDamagePlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var pain = scn.GetShadowCard("pain");
		var butcher1 = scn.GetShadowCard("butcher1");
		var butcher2 = scn.GetShadowCard("butcher2");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(pain);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.MoveMinionsToTable(butcher1, butcher2, saruman, guard);

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(0, scn.GetKeywordCount(butcher1, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, butcher1}, new PhysicalCardImpl[]{gimli, guard});
		// The second Butcher and Saruman are left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(pain));
		scn.ShadowUseCardAction(pain);

		// Both of his own sites are offered, and the one he chooses goes back on the path, the lower one here
		// (Ketura's ruling on Q-0370: his choice, never the opponent's site).
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertTrue(scn.ShadowControls(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// [uruk-hai] minions in this skirmish and in none, an Uruk-hai and a Wizard; not the [sauron] Uruk-hai,
		// nor this [uruk-hai] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher1, butcher2, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, pain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(butcher1);

		assertEquals(1, scn.GetKeywordCount(butcher1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(butcher2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		// A site liberated is no site taken: three [uruk-hai] minions and an unbound companion who can exert,
		// and nothing is offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();

		// The Butcher, 5 + 6 for hunter, beats Aragorn: a wound and one for damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The bonus lasted the skirmish phase it was made in: the next skirmish is a phase of its own.
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetKeywordCount(butcher1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoSiteControlledTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var pain = scn.GetShadowCard("pain");
		var butcher1 = scn.GetShadowCard("butcher1");
		scn.MoveCardsToSupportArea(pain);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(butcher1);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, butcher1);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pain));
	}

	@Test
	public void WithOnlyTheFreePeoplesPlayerControllingASiteTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var pain = scn.GetShadowCard("pain");
		var butcher1 = scn.GetShadowCard("butcher1");
		scn.MoveCardsToSupportArea(pain);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		scn.MoveMinionsToTable(butcher1);

		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, butcher1);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// As built (Q-0370): a bare "liberate a site" on his card takes a site of his own, and he has none.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pain));
		assertTrue(scn.FreepsControls(site1));
	}
}
