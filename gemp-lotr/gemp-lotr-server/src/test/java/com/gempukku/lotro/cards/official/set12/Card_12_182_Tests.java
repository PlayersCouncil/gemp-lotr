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

public class Card_12_182_Tests
{

	// The Towers path: site 2 is Eastfold 4_331, a plains and no battleground or forest (its Shadow ability needs
	// three [isengard] trackers), site 4 White Rocks 4_346, a battleground and no forest.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unimpeded", "12_182");
					put("unimpeded2", "12_182");
					put("unimpeded3", "12_182");
					put("unimpeded4", "12_182");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: resistance 6, strength 8; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: resistance 5; his abilities are used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: ally (resistance 0) who may exert to take part in skirmishes
					put("rider", "12_161");     // Black Rider: fierce [wraith] Nazgul, strength 8 +1 for each companion
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: fierce [raider] Nazgul, strength 12; his text needs 3 burdens
					put("nelya", "1_233");      // Ulaire Nelya: fierce [wraith] Nazgul, strength 10; her Shadow ability is used only on purpose
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, no Nazgul, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("steed", "1_208");      // Black Steed: possession on a Nazgul, strength +2 (+2 more at a plains)
					put("ring", "17_143");      // Ring of Terror: artifact on a Nazgul, damage +1 only
					put("twilight", "19_34");   // In Twilight: condition on a [wraith] minion, strength +2
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Derndingle 4_330 as site 2: a forest and no battleground (its move limit needs an
	// unbound Hobbit, and none is laid).
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unimpeded", "12_182");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: resistance 6, strength 8; a maneuver ability used only on purpose
					put("rider", "12_161");     // Black Rider: fierce [wraith] Nazgul, strength 8 +1 for each companion
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_330");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnimpededStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Unimpeded
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Discard this condition to make a Nazgûl strength +1 for each of the following that is true: he is at a battleground or forest site; he is skirmishing a companion who has resistance 4 or less; he is bearing a possession; he is in a fierce skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("unimpeded");

		assertEquals("Unimpeded", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtABattlegroundAgainstResistance4BearingAPossessionANazgulIsPlus3AndPlus4InTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var unimpeded = scn.GetShadowCard("unimpeded");
		var unimpeded2 = scn.GetShadowCard("unimpeded2");
		var unimpeded3 = scn.GetShadowCard("unimpeded3");
		var rider = scn.GetShadowCard("rider");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCardsToSupportArea(unimpeded, unimpeded2, unimpeded3);

		scn.StartGame();
		scn.SkipToSite(3);
		// Each burden takes 1 from every companion's resistance: Aragorn's 6 is exactly 4.
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(rider, khamul, hound);
		scn.AttachCardsTo(rider, steed);
		assertInZone(Zone.ATTACHED, steed);
		assertInZone(Zone.SUPPORT, unimpeded, unimpeded2, unimpeded3);
		scn.SkipToAssignments();
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.FreepsAssignToMinions(aragorn, rider);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 8 + 2 companions + 2 the Steed
		assertEquals(12, scn.GetStrength(rider));
		assertTrue(scn.ShadowActionAvailable(unimpeded));
		scn.ShadowUseCardAction(unimpeded);
		// A Nazgul of either culture, in this skirmish or in none; not the [wraith] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(rider);

		// The copy used is discarded; the others stay.
		assertInZone(Zone.DISCARD, unimpeded);
		assertInZone(Zone.SUPPORT, unimpeded2, unimpeded3);
		// 12 + 1 a battleground + 1 resistance 4 + 1 the Steed; this is no fierce skirmish.
		assertEquals(15, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 15 against 8: Aragorn is wounded, not overwhelmed, and the fierce Rider fights again.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		// The bonus lasted the Rider's first skirmish only.
		assertEquals(12, scn.GetStrength(rider));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Khamul, in no skirmish, is not in the fierce skirmish going on: 12 + 1 a battleground.
		assertEquals(12, scn.GetStrength(khamul));
		scn.ShadowUseCardAction(unimpeded2);
		scn.ShadowChooseCard(khamul);
		assertEquals(13, scn.GetStrength(khamul));

		scn.FreepsPass();
		scn.ShadowUseCardAction(unimpeded3);
		scn.ShadowChooseCard(rider);
		// 12 + 1 a battleground + 1 resistance 4 + 1 the Steed + 1 the fierce skirmish
		assertEquals(16, scn.GetStrength(rider));
		assertInZone(Zone.DISCARD, unimpeded2, unimpeded3);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ResistanceOf5AnAllyAnArtifactAConditionAndASkirmishStillToComeDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(thrarin);

		var unimpeded = scn.GetShadowCard("unimpeded");
		var unimpeded2 = scn.GetShadowCard("unimpeded2");
		var unimpeded3 = scn.GetShadowCard("unimpeded3");
		var unimpeded4 = scn.GetShadowCard("unimpeded4");
		var rider = scn.GetShadowCard("rider");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		var ring = scn.GetShadowCard("ring");
		var twilight = scn.GetShadowCard("twilight");
		scn.MoveCardsToSupportArea(unimpeded, unimpeded2, unimpeded3, unimpeded4);

		scn.StartGame();
		// One burden: Sam's 5 is exactly 4 and Aragorn's 6 is 5.
		scn.AddBurdens(1);
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetResistance(thrarin));
		scn.MoveMinionsToTable(rider, khamul, nelya);
		scn.AttachCardsTo(rider, ring);
		scn.AttachCardsTo(nelya, twilight);
		assertInZone(Zone.ATTACHED, ring, twilight);
		assertInZone(Zone.SUPPORT, unimpeded, unimpeded2, unimpeded3, unimpeded4);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Eastfold", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		scn.FreepsUseCardAction(thrarin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, rider},
				new PhysicalCardImpl[]{sam, khamul},
				new PhysicalCardImpl[]{thrarin, nelya});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 8 + 3 companions; Aragorn has resistance 5, and the Ring of Terror is an artifact.
		assertEquals(11, scn.GetStrength(rider));
		scn.ShadowUseCardAction(unimpeded);
		scn.ShadowChooseCard(rider);
		assertEquals(11, scn.GetStrength(rider));

		// Khamul is assigned to Sam, of resistance 4, but their skirmish has not begun: he is skirmishing nobody.
		scn.FreepsPass();
		assertEquals(12, scn.GetStrength(khamul));
		scn.ShadowUseCardAction(unimpeded2);
		scn.ShadowChooseCard(khamul);
		assertEquals(12, scn.GetStrength(khamul));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();
		// Now he is skirmishing her: 12 + 1 resistance 4.
		scn.ShadowUseCardAction(unimpeded3);
		scn.ShadowChooseCard(khamul);
		assertEquals(13, scn.GetStrength(khamul));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();
		// 10 + 2 In Twilight; Thrarin is an ally of resistance 0, no companion, and In Twilight is a condition.
		assertEquals(12, scn.GetStrength(nelya));
		scn.ShadowUseCardAction(unimpeded4);
		scn.ShadowChooseCard(nelya);
		assertEquals(12, scn.GetStrength(nelya));
		assertInZone(Zone.DISCARD, unimpeded, unimpeded2, unimpeded3, unimpeded4);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtAForestTheSiteCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var unimpeded = scn.GetShadowCard("unimpeded");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToSupportArea(unimpeded);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, unimpeded);
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();
		assertEquals("Derndingle", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 8 + 2 companions
		assertEquals(10, scn.GetStrength(rider));
		scn.ShadowUseCardAction(unimpeded);
		// + 1 at a forest; Aragorn has resistance 6, the Rider bears nothing, and this is no fierce skirmish.
		assertEquals(11, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhaseAndWithNoNazgulIsUsedForNothingInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var unimpeded = scn.GetShadowCard("unimpeded");
		var unimpeded2 = scn.GetShadowCard("unimpeded2");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(unimpeded, unimpeded2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, unimpeded, unimpeded2);
		scn.MoveMinionsToTable(hound);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(unimpeded));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, hound);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Hound is the only minion, and no Nazgul: nothing can be chosen.
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(unimpeded));
		scn.ShadowUseCardAction(unimpeded);

		assertInZone(Zone.DISCARD, unimpeded);
		assertInZone(Zone.SUPPORT, unimpeded2);
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
