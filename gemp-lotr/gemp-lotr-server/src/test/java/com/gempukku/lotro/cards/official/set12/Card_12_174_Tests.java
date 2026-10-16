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

public class Card_12_174_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "12_174");
					put("steed", "1_208");      // Black Steed: a [wraith] possession, the other [wraith] card; +2, +2 more at plains
					// Five burdens take a companion of resistance 5 to 0, one of 6 to 1, and Frodo's 10 to 5.
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, resistance 5
					put("boromir", "11_57");    // Boromir, Hero of Osgiliath: [gondor] Man, resistance 5
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: resistance 6
					put("guard1", "1_7");       // Dwarf Guard: resistance 6, no text once in play
					put("guard2", "1_7");
					// Thrarin, Dwarven Smith: an ally (resistance 0 by rule) who can be made to take part in skirmishes
					put("thrarin", "1_27");
					put("visitor", "7_126");    // Unexpected Visitor: gives each minion "Assignment: Assign this minion to bearer"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "12_174");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a [wraith] minion; strength 9
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion, strength 13, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3
					// Dwarf Guard: a companion with no text once in play
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					// Thrarin, Dwarven Smith: an ally who can be made to take part in skirmishes
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKillScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "12_174");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: the other [wraith] card; strength 9
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3
					// Dwarf Guard: a companion with no text once in play
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Úlairë Cantëa, Black Assassin
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br><b>Assignment:</b> Assign Úlairë Cantëa to a companion who has resistance 0.<br><b>Skirmish:</b> Spot 6 companions and another [wraith] card to kill a companion Úlairë Cantëa is skirmishing.
		*/

		var scn = GetScenario();

		var cantea = scn.GetFreepsCard("cantea");

		assertEquals("Úlairë Cantëa", cantea.getBlueprint().getTitle());
		assertEquals("Black Assassin", cantea.getBlueprint().getSubtitle());
		assertTrue(cantea.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, cantea.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, cantea.getBlueprint().getCulture());
		assertEquals(CardType.MINION, cantea.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, cantea.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(cantea, Keyword.FIERCE));
		assertEquals(5, cantea.getBlueprint().getTwilightCost());
		assertEquals(10, cantea.getBlueprint().getStrength());
		assertEquals(3, cantea.getBlueprint().getVitality());
		assertEquals(3, cantea.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered (a spot is no
	// cost).  In a skirmish Úlairë Cantëa is not part of there is no "companion Úlairë Cantëa is skirmishing".
	@Test
	public void TheKillIsOfferedInCanteasOwnSkirmishAndNotInAnothers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKillScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// Six companions with Frodo
		scn.MoveCompanionsToTable(aragorn, gimli, scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"));

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, cantea});

		// Aragorn's skirmish is against Úlairë Lemenya: the six companions and the other [wraith] card are there
		// to spot, and Úlairë Cantëa is skirmishing nobody.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));
		scn.ShadowPass();

		// Gimli's is against her.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, cantea));
		assertTrue(scn.ShadowActionAvailable(cantea));

		scn.ShadowUseCardAction(cantea);
		assertInZone(Zone.DEAD, gimli);
		// That was the last skirmish; she is fierce, and the fierce round begins with its assignment actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignsHimselfToACompanionAtResistanceZeroAndThenKillsHimBesideACardHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Six companions with Frodo
		scn.MoveCompanionsToTable(sam, boromir, aragorn, guard1, guard2);
		scn.MoveAlliesToTable(thrarin);
		var frodo = scn.GetRingBearer();

		var cantea = scn.GetShadowCard("cantea");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.AttachCardsTo(cantea, steed);
		scn.AddBurdens(5);

		// Thrarin takes part in skirmishes from now to the regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(boromir));
		assertEquals(1, scn.GetResistance(aragorn));
		assertEquals(1, scn.GetResistance(guard1));
		assertEquals(5, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(thrarin));
		assertFalse(scn.IsCharAssigned(cantea));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);
		// The two companions at resistance 0, a [shire] Hobbit and a [gondor] Man; not the companions at 1 or 5, nor
		// the ally, who has resistance 0 and can be assigned.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, guard1, guard2, frodo, thrarin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(boromir);
		assertTrue(scn.IsCharAssignedAgainst(boromir, cantea));
		assertFalse(scn.IsCharAssigned(sam));

		scn.PassAssignmentActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		// Six companions, and Black Steed is another [wraith] card.
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, cantea));
		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);
		assertInZone(Zone.DEAD, boromir);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.ATTACHED, steed);
		// His skirmish was the only one; he is fierce, and the fierce round begins with its assignment actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoCompanionBelowResistanceOneTheAssignmentIsNotOfferedThoughAnAllyHasResistanceZero() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam, boromir);
		scn.MoveAlliesToTable(thrarin);

		var cantea = scn.GetShadowCard("cantea");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.AddBurdens(4);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(1, scn.GetResistance(sam));
		assertEquals(1, scn.GetResistance(boromir));
		assertEquals(0, scn.GetResistance(thrarin));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));
	}

	@Test
	public void OnceHeIsAssignedHisAssignmentIsNotOfferedThoughACompanionAtResistanceZeroIsUnassigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var cantea = scn.GetShadowCard("cantea");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.AddBurdens(5);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetResistance(sam));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(cantea, "Unexpected Visitor"));
		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea, "Unexpected Visitor");
		assertTrue(scn.IsCharAssignedAgainst(aragorn, cantea));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.ShadowActionAvailable(cantea));
	}

	@Test
	public void WithFiveCompanionsBesideAnAllyTheKillIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Five companions with Frodo, and an ally
		scn.MoveCompanionsToTable(aragorn, gimli, scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		scn.MoveAlliesToTable(thrarin);

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, cantea});

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, cantea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));
	}

	@Test
	public void WithNoOtherWraithCardTheKillIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// Six companions with Frodo
		scn.MoveCompanionsToTable(aragorn, gimli, scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"));

		var cantea = scn.GetShadowCard("cantea");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, cantea});

		// The other minion is [moria]; Úlairë Cantëa is the only [wraith] card.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, cantea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));
	}

	@Test
	public void AnAllyHeIsSkirmishingIsNoCompanionAndTheKillIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Six companions with Frodo, and an ally
		scn.MoveCompanionsToTable(aragorn, gimli, scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"));
		scn.MoveAlliesToTable(thrarin);

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{thrarin, cantea});

		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, cantea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));
	}
}
