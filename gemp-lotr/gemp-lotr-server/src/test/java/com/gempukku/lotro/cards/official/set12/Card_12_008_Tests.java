package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_008_Tests
{

	// The Fellowship path: site 2 is Ettenmoors 1_331, a plains (its skirmish ability is used only on purpose), site 4
	// Balin's Tomb 1_343, underground (its maneuver ability needs a tale), and no site is a mountain.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charge", "12_8");
					put("charge2", "12_8");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, resistance 6; "To play, spot a Dwarf" is its whole text
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, resistance 6; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose
					put("handaxe", "2_10");     // Hand Axe: [dwarven] possession, strength +1; an archery ability used only on purpose
					put("ring", "17_4");        // Ring of Artifice: [dwarven] artifact; damage +1, and strength +1 for each [dwarven] artifact in the discard pile (none)
					put("rider", "12_161");     // Black Rider: fierce [wraith] Nazgul, strength 8 +1 for each companion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, not fierce, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Pass of Caradhras 1_348 as site 4: a mountain and not underground (its Shadow ability
	// needs an [isengard] minion, and none is laid).
	protected VirtualTableScenario GetMountainScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charge", "12_8");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, resistance 6; "To play, spot a Dwarf" is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, not fierce, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path: site 4 is Balin's Tomb 1_343, underground (its maneuver ability needs a tale).  Thrarin's
	// home, site 3, is Rivendell Valley 1_341, a sanctuary and no more.  No Dwarf ally has a home that is
	// underground: each is of site 3 of the Fellowship path.  Aragorn fights the skirmish; the Dwarves are in none.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charge", "12_8");
					put("charge2", "12_8");
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5, resistance 6; +2 while skirmishing an Orc
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home 3F, strength 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HisFathersChargeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: His Father's Charge
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Discard this condition to make a Dwarf strength +1 for each of the following that is true: he is at a mountain or underground site; he has resistance 3 or more; he is bearing a possession; he is skirmishing a fierce minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("charge");

		assertEquals("His Father's Charge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AnAllyWhoseHomeIsNotUndergroundGetsNothingForTheFellowshipUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var charge = scn.GetFreepsCard("charge");
		var charge2 = scn.GetFreepsCard("charge2");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(charge, charge2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// The companion: underground, with the fellowship, and resistance 6: 5 + 2.
		scn.FreepsUseCardAction(charge);
		scn.FreepsChooseCard(farin);
		assertEquals(7, scn.GetStrength(farin));

		// Thrarin is at his home site, Rivendell Valley (CR 4.2 "ally"), and is true of nothing else listed: 4.
		scn.ShadowPass();
		scn.FreepsUseCardAction(charge2);
		scn.FreepsChooseCard(thrarin);
		assertEquals(4, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtAnUndergroundSiteWithResistance3APossessionAndAFierceMinionTheDwarfIsStrengthPlus4ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var charge = scn.GetFreepsCard("charge");
		var charge2 = scn.GetFreepsCard("charge2");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(guard, handaxe);
		scn.MoveCardsToSupportArea(charge, charge2);

		var rider = scn.GetShadowCard("rider");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, charge, charge2);
		scn.SkipToSite(3);
		// Each burden takes 1 from every companion's resistance: the Guard's 6 is exactly 3.
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(guard));
		assertInZone(Zone.ATTACHED, handaxe);
		scn.MoveMinionsToTable(rider, troop);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, rider}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(guard);

		// 4 + 1 for the Hand Axe
		assertEquals(5, scn.GetStrength(guard));
		assertTrue(scn.FreepsActionAvailable(charge));
		scn.FreepsUseCardAction(charge);

		// Any Dwarf, in this skirmish or not, companion or ally; no one who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(guard);

		// The copy used is discarded; the other stays.
		assertInZone(Zone.DISCARD, charge);
		assertInZone(Zone.SUPPORT, charge2);
		// 5 + 1 underground + 1 resistance 3 + 1 the Hand Axe + 1 the fierce Black Rider
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The bonus lasted the Guard's skirmish only: in Aragorn's it is gone.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtAMountainSiteTheSiteCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMountainScenario();

		var charge = scn.GetFreepsCard("charge");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(charge);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, charge);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Pass of Caradhras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetResistance(guard));
		assertEquals(4, scn.GetStrength(guard));
		scn.FreepsUseCardAction(charge);

		// 4 + 1 at a mountain + 1 resistance 6; nothing borne, and the Troop is not fierce.
		assertEquals(6, scn.GetStrength(guard));
		assertInZone(Zone.DISCARD, charge);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtAPlainsSiteTheSiteDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var guard = scn.GetFreepsCard("guard");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, handaxe);
		scn.MoveCardsToSupportArea(charge);

		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		assertInZone(Zone.ATTACHED, handaxe);
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();
		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		scn.FreepsAssignToMinions(guard, rider);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(5, scn.GetStrength(guard));
		scn.FreepsUseCardAction(charge);

		// 5 + 1 resistance 6 + 1 the Hand Axe + 1 the fierce Black Rider; nothing for the plains.
		assertEquals(8, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithResistance2TheResistanceDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var guard = scn.GetFreepsCard("guard");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, handaxe);
		scn.MoveCardsToSupportArea(charge);

		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(4);
		assertEquals(2, scn.GetResistance(guard));
		assertInZone(Zone.ATTACHED, handaxe);
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(guard, rider);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(5, scn.GetStrength(guard));
		scn.FreepsUseCardAction(charge);

		// 5 + 1 underground + 1 the Hand Axe + 1 the fierce Black Rider; nothing for resistance 2.
		assertEquals(8, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void BearingAnArtifactAndNoPossessionDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var guard = scn.GetFreepsCard("guard");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, ring);
		scn.MoveCardsToSupportArea(charge);

		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.ATTACHED, ring);
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(guard, rider);
		scn.FreepsResolveSkirmish(guard);

		// The Ring of Artifice changes no strength with no [dwarven] artifact in the discard pile.
		assertEquals(4, scn.GetStrength(guard));
		scn.FreepsUseCardAction(charge);

		// 4 + 1 underground + 1 resistance 6 + 1 the fierce Black Rider; nothing for the artifact.
		assertEquals(7, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionThatIsNotFierceOrAFierceOneWhoseSkirmishIsStillToComeDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var charge2 = scn.GetFreepsCard("charge2");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, handaxe);
		scn.MoveCardsToSupportArea(charge, charge2);

		var rider = scn.GetShadowCard("rider");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.ATTACHED, handaxe);
		scn.MoveMinionsToTable(rider, troop);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{gimli, rider});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(5, scn.GetStrength(guard));
		scn.FreepsUseCardAction(charge);
		scn.FreepsChooseCard(guard);
		// 5 + 1 underground + 1 resistance 6 + 1 the Hand Axe; the Troop is not fierce.
		assertEquals(8, scn.GetStrength(guard));

		scn.ShadowPass();
		// Gimli is assigned to the fierce Black Rider, but their skirmish has not begun: he is skirmishing nobody.
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(charge2);
		scn.FreepsChooseCard(gimli);
		// 6 + 1 underground + 1 resistance 6
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoDwarfInPlayItIsStillOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var charge2 = scn.GetFreepsCard("charge2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(charge, charge2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, charge, charge2);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn is the only companion and no Dwarf is in play: nothing could be chosen.
		int strength = scn.GetStrength(aragorn);
		assertTrue(scn.FreepsActionAvailable(charge));
		scn.FreepsUseCardAction(charge);

		assertInZone(Zone.DISCARD, charge);
		assertInZone(Zone.SUPPORT, charge2);
		assertEquals(strength, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhaseAndIsInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(charge);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(charge));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(charge));
	}
}
