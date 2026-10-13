package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Strength deciding a skirmish.
 *
 * Comprehensive Rules 4.2, "skirmish phase(s)": "If the total strength of one side is more than the strength of the
 * other side (but less than double), the side with the most strength wins that skirmish.  (If there is a tie, the
 * Shadow side wins.)  Place one wound on each character on the losing side."  Its example: "If Aragorn, with
 * strength of 8, faces two Orcs, each with strength of 3 (total strength of 6), then Aragorn wins that skirmish and
 * each losing Orc takes one wound."  "Overwhelmed": "A side with a total strength greater than zero will overwhelm a
 * side whose total strength is zero.  If the strength of both sides is zero, the Shadow side wins the skirmish (but
 * does not overwhelm)."  "Bearer": a card played on a bearer shows "any attribute bonuses it has"; "Ally": "When
 * the fellowship is at an ally's home site ..., that ally may be assigned to a skirmish in the same way that
 * companions are assigned".
 *
 * This class is what lets a card's own tests take its printed strength as read (Ketura on Q-0279, 2026-10-12): a
 * card whose text says nothing about its strength is not put in a skirmish in its own file to show the number
 * deciding it.
 *
 * What is in another class and is not gone over again here:
 *   double and triple, and a side spared from it by its own text: OverwhelmTests;
 *   the wounds a damage bonus adds to the one for losing, and the rounds they come in: DamageBonusTests;
 *   strength a keyword adds: HunterTests, EnduringTests;
 *   an item's bonus reaching its bearer at all, and leaving with it: BorneItemsTests;
 *   who may be assigned an ally, and where: AllyHomeTests;
 *   a skirmish whose characters are taken out of it before it is resolved: HinderedCharacterCannotSkirmishTests;
 *   where the killed go: KilledAtZeroVitalityTests.
 *
 * A strength is never read below 0 (ModifiersLogic.getStrength): Sam here is 3 with exactly 3 taken off.
 *
 * The table is the Fellowship block's; the skirmishes are at Ettenmoors (site 2), whose own skirmish ability is
 * never used, or at Rivendell Valley (site 3), Erland's home.  Frodo, Son of Drogo is the Ring-bearer and is in no
 * skirmish.
 */
public class SkirmishStrengthTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "0_64");     // Gandalf, Stormcrow: companion, strength 8, vitality 4, no text at all
					put("knight", "5_35");      // Gondorian Knight: companion, strength 6, vitality 3; "Knight." is its whole text
					put("guard", "5_83");       // Household Guard: companion, strength 6, vitality 3; "Valiant." is its whole text
					put("pippin", "1_306");     // Pippin, Friend to Frodo: companion, strength 3, vitality 4; his text is about tales
					// Sam, Son of Hamfast: companion, strength 3, vitality 4, unbound.  A fellowship ability that removes a
					// burden, used only on purpose, and a response to Frodo's death.
					put("sam", "1_311");
					put("sword", "1_299");      // Hobbit Sword: hand weapon, strength +2; "Bearer must be a Hobbit." is its whole text
					// Erland, Advisor to Brand: ally, strength 5, vitality 2, home site 3 of the Fellowship path.  "Shadow
					// players may not look at or reveal cards in your hand."
					put("erland", "2_21");

					// Three Shadow conditions, each printed strength -1, that a companion other than the Ring-bearer
					// may bear.  Our Foes Are Weak 11_196 is also resistance -3 and has no other text; Many Riddles
					// 4_159 has an assignment ability that needs an [isengard] tracker (there is none); Desertion
					// 6_61 has one that assigns an [isengard] minion of less strength than its bearer, never used.
					put("foes", "11_196");
					put("riddles", "4_159");
					put("desertion", "6_61");

					// Gríma, Wormtongue: [isengard] Man, strength 4, vitality 3.  His maneuver ability needs an unbound
					// companion bearing 3 or more cards, and is never used.
					put("grima", "4_154");
					// Mordor Soldier: [sauron] Orc, strength 3, vitality 2.  His skirmish ability removes a threat,
					// and there is none.
					put("orc", "7_290");
					put("soldier", "7_199");    // Morgul Soldier: [wraith] Orc, strength 6, vitality 2; "Nazgul are strength +1."
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text at all
					// Southron Scout: [raider] Man, strength 3, vitality 1; "Southron.  Ambush (2)."  Only the Shadow
					// player assigns him here, so his ambush is never set off.
					put("scout", "4_252");
					// City Wall: [gondor] Fortification condition, printed strength -3.  Its text has it transferred
					// from the support area to a minion; here it is laid on the Scout.
					put("wall", "5_33");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheSideWithMoreStrengthWinsAndTheLoserTakesOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, guard);

		var grima = scn.GetShadowCard("grima");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, brigand);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, grima}, new PhysicalCardImpl[]{guard, brigand});

		// The Knight, 6, beats Gríma, 4: one wound on the minion.
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(4, scn.GetStrength(grima));
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(grima));
		assertInZone(Zone.SHADOW_CHARACTERS, grima);

		// The Brigand, 8, beats the Guard, 6, who is under half of it and so is not overwhelmed: one wound on him.
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(brigand));
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ATieIsWonByTheShadowSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(knight, soldier);
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(soldier));
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheMinionsOfOneSkirmishAreTotalledAndBeatACharacterNoneOfThemBeatsAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var grima = scn.GetShadowCard("grima");
		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, orc);
		scn.SkipToAssignments();

		// The Free Peoples player assigns the Knight to Gríma, and the Shadow player gives him the Orc as well.
		scn.FreepsAssignToMinions(knight, grima);
		scn.ShadowAssignToMinions(knight, orc);
		assertTrue(scn.IsCharAssignedAgainst(knight, grima));
		assertTrue(scn.IsCharAssignedAgainst(knight, orc));

		// 4 + 3 against 6.  Each minion alone is weaker than the Knight.
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(4, scn.GetStrength(grima));
		assertEquals(3, scn.GetStrength(orc));
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(orc));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACharacterWhoBeatsTheMinionsTotalWoundsEachOfThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var grima = scn.GetShadowCard("grima");
		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, orc);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(gandalf, grima);
		scn.ShadowAssignToMinions(gandalf, orc);

		// The rules' own example: 8 against 4 + 3, which is more and less than double.
		assertEquals(8, scn.GetStrength(gandalf));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(grima));
		assertEquals(1, scn.GetWoundsOn(orc));
		assertInZone(Zone.SHADOW_CHARACTERS, grima, orc);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASideOfStrengthZeroIsOverwhelmedByAnySideStrongerThanZero() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var foes = scn.GetShadowCard("foes");
		var riddles = scn.GetShadowCard("riddles");
		var desertion = scn.GetShadowCard("desertion");
		scn.MoveCompanionsToTable(sam, pippin);
		scn.AttachCardsTo(sam, foes, riddles, desertion);

		var grima = scn.GetShadowCard("grima");
		var scout = scn.GetShadowCard("scout");
		var wall = scn.GetFreepsCard("wall");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, scout);
		scn.AttachCardsTo(scout, wall);
		scn.SkipToAssignments();

		// 3 - 1 - 1 - 1 and 3 - 3.
		assertEquals(0, scn.GetStrength(sam));
		assertEquals(0, scn.GetStrength(scout));
		assertEquals(3, scn.GetStrength(pippin));

		scn.FreepsAssignToMinions(sam, grima);
		scn.ShadowAssignToMinions(pippin, scout);

		// Gríma's 4 against 0: Sam is killed with all four of his vitality left.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetWoundsOn(grima));

		// Pippin's 3 against 0: the Scout is killed, as he would not be by 3 against his own 3.
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, scout);
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ZeroAgainstZeroIsWonByTheShadowSideAndOverwhelmsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var foes = scn.GetShadowCard("foes");
		var riddles = scn.GetShadowCard("riddles");
		var desertion = scn.GetShadowCard("desertion");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, foes, riddles, desertion);

		var scout = scn.GetShadowCard("scout");
		var wall = scn.GetFreepsCard("wall");

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.AttachCardsTo(scout, wall);
		scn.SkipToAssignments();

		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(sam, scout);

		assertEquals(0, scn.GetStrength(sam));
		assertEquals(0, scn.GetStrength(scout));
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// One wound for losing, and no more.
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.SHADOW_CHARACTERS, scout);
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AStrengthBonusPrintedOnABorneCardIsCountedInTheTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(pippin);
		scn.AttachCardsTo(pippin, sword);

		var grima = scn.GetShadowCard("grima");

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToAssignments();

		// 3 + 2 against 4: without the Sword he would lose.
		assertEquals(5, scn.GetStrength(pippin));
		scn.FreepsAssignToMinions(pippin, grima);
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(grima));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyAtHisHomeSiteFightsAtHisOwnStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		scn.MoveAlliesToTable(erland);

		var grima = scn.GetShadowCard("grima");
		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(grima, orc);

		// The move takes the fellowship to site 3, Erland's home.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		// 5 against 4: Erland wins and Gríma takes the wound.
		assertEquals(5, scn.GetStrength(erland));
		scn.FreepsAssignToMinions(erland, grima);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(erland);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(erland));
		assertEquals(1, scn.GetWoundsOn(grima));
		assertInZone(Zone.SUPPORT, erland);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
