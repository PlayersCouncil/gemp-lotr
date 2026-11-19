package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "5_115");
					// Sméagol, Poor Creature: [gollum] companion, Ring-bound; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion of another title, strength 3 (+2 only
					// while he bears a weapon, and he bears none here)
					put("merry", "1_303");
					// Gollum, Nasty Treacherous Creature: a [gollum] minion of another title; a regroup ability used only on purpose
					put("gollum", "5_24");
					put("brigand1", "4_10");        // Dunlending Brigand: strength 8, vitality 1, no text
					put("brigand2", "4_10");
					put("troop", "1_177");          // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, vitality 4
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "5_115");
					// Sméagol, Poor Creature: [gollum] companion, Ring-bound; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability that wants no companion of
					// another culture, so it is never offered beside Galadriel
					put("oncelikeus", "103_116");
					// Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play
					// beside another Ring-bearer; his texts want more minions than companions, or a skirmish he is not in
					put("frodo", "13_149");
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion who is no Frodo
					put("merry", "1_303");
					put("brigand", "4_10");         // Dunlending Brigand: strength 8, vitality 1, no text
					put("troop", "1_177");          // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Sam, Nice Sensible Hobbit
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Aragorn
		 * Game Text: <b>Ring-bound</b>.<br>While you can spot Frodo and Sméagol, Sam may not be overwhelmed unless his strength is tripled.<br><b>Response:</b> If Frodo is killed, make Sam the <b>Ring-bearer (resistance 5)</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Nice Sensible Hobbit", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void WithFrodoAndSmeagolSpottedSamIsNotOverwhelmedAtDoubleStrengthAndAnotherHobbitIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(sam, smeagol, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, brigand1}, new PhysicalCardImpl[]{merry, brigand2});

		scn.FreepsResolveSkirmish(sam);
		assertEquals("Frodo", frodo.getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		// 8 against 3: at least double, one short of triple.
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		// Spared the overwhelm: he takes the one wound for losing.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);

		// Merry, a Hobbit who is not Sam, loses to the same 8 on the same board and is overwhelmed.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(brigand2));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFrodoWhoIsNotTheRingBearerAndTheShireSmeagolAreSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var oncelikeus = scn.GetFreepsCard("oncelikeus");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(sam, frodo, oncelikeus);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, brigand);

		scn.FreepsResolveSkirmish(sam);
		assertNotSame(frodo, scn.GetRingBearer());
		assertEquals(Culture.SHIRE, oncelikeus.getBlueprint().getCulture());
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFrodoButNoSmeagolSamIsOverwhelmedThoughGollumIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var brigand1 = scn.GetShadowCard("brigand1");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, brigand1);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(sam);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(brigand1));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithSmeagolButNoFrodoSamIsOverwhelmedThoughAnotherHobbitIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(sam, smeagol, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, brigand);

		scn.FreepsResolveSkirmish(sam);
		assertEquals("Galadriel", scn.GetRingBearer().getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, smeagol, merry);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(brigand));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFrodoAndSmeagolSpottedSamIsOverwhelmedAtTripleStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(sam, smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1);
		// A strength that only arranges the board: 8 + 1, exactly triple Sam's 3.
		scn.ApplyAdHocModifier(new StrengthModifier(null, brigand1, null, 1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, brigand1);

		scn.FreepsResolveSkirmish(sam);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(9, scn.GetStrength(brigand1));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenFrodoIsOverwhelmedSamCanBecomeTheRingBearerWithNoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertSame(frodo, scn.GetRingBearer());
		assertAttachedTo(ring, frodo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(sam));
		// 5 less 2 burdens
		assertEquals(3, scn.GetResistance(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertAttachedTo(ring, sam);
		// 3 + 1 for the Ring
		assertEquals(4, scn.GetStrength(sam));
		// The burdens went with Frodo.
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenFrodoIsKilledByWoundsSamCanBecomeTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertSame(frodo, scn.GetRingBearer());
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Vitality 4: he is killed when the game next checks.
		scn.AddWoundsToChar(frodo, 4);
		assertEquals(4, scn.GetWoundsOn(frodo));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheResponseWhenFrodoIsKilledLosesTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertFalse(scn.GameIsFinished());

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnotherCompanionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, troop);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// 3 against 13: Merry is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(frodo, scn.GetRingBearer());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenARingBearerWhoIsNotFrodoIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(galadriel, troop);
		assertFalse(scn.GameIsFinished());

		// 3 + 1 for the Ring against 13: Galadriel is overwhelmed, and Sam may not take her place.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, galadriel);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFrodoWhoIsNotTheRingBearerIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, frodo);

		scn.StartGame();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertInZone(Zone.FREE_CHARACTERS, frodo);

		// 3 against 13: this Frodo is overwhelmed, but he was not the Ring-bearer.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(galadriel, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
