package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_129_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bold", "7_129");
					put("soldier", "4_254");    // Southron Soldier: a [raider] Southron Man, strength 4, vitality 1; ambush (2) only
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Easterling Man; stronger and fierce at 2 burdens
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul who is no Man; his texts read burdens only
					put("attea", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text

					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, strength 6, vitality 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability only
					put("gandalf", "0_64");     // Gandalf, Stormcrow: a Wizard companion, strength 8, vitality 4, no text
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, no text but Knight
					// Thrarin, Dwarven Smith: a Dwarf ally; "Maneuver: Exert Thrarin to allow him to participate in archery
					// fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");

					// Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					put("party1", "1_158");
					put("party2", "1_158");
					// Goblin Runner: a [moria] Orc, strength 5, vitality 1; its text answers its own play only
					put("runner", "1_178");
					// Greed: another Shadow condition in the support area; its maneuver ability wants 6 companions
					put("greed", "1_125");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoldMenandGrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Bold Men and Grim
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [raider] Men and add a threat.
		 * Each time a companion is about to be killed, you may remove a threat to discard a companion (except the Ring-bearer).
		 * Discard this condition at the start of the regroup phase.
		*/

		var scn = GetScenario();

		var bold = scn.GetFreepsCard("bold");

		assertEquals("Bold Men and Grim", bold.getBlueprint().getTitle());
		assertNull(bold.getBlueprint().getSubtitle());
		assertTrue(bold.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, bold.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, bold.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, bold.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(bold, Keyword.SUPPORT_AREA));
		assertEquals(1, bold.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithOneRaiderManBesideARaiderNazgulAndADunlandManThenAddsAThreatOnceASecondIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var soldier = scn.GetShadowCard("soldier");
		var axeman = scn.GetShadowCard("axeman");
		var attea = scn.GetShadowCard("attea");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(bold, axeman);
		assertInHand(bold, axeman);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, attea, brigand);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// One [raider] Man: a [raider] card that is no Man and a Man of another culture do not make up the two.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(bold));

		// A second [raider] Man, of another title, played from hand.
		scn.ShadowPlayCard(axeman);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.ShadowPlayAvailable(bold));

		scn.ShadowPlayCard(bold);
		assertInZone(Zone.SUPPORT, bold);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var soldier = scn.GetShadowCard("soldier");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(bold);
		assertInHand(bold);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, axeman);
		// Two companions and two threats: no room for another.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(bold));

		scn.RemoveThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(bold));

		scn.ShadowPlayCard(bold);
		assertInZone(Zone.SUPPORT, bold);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeACompanionIsAboutToBeKilledRemovesAThreatToDiscardACompanionButNotTheRingBearerOrAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		scn.MoveCompanionsToTable(gimli, aragorn, gandalf, knight);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(bold);
		// Each dies of the first wound of a lost skirmish.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, party1}, new PhysicalCardImpl[]{gandalf, party2});

		assertEquals(2, scn.GetThreats());
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli is about to be killed.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bold));
		scn.ShadowChooseOptionalTrigger(bold);
		assertEquals(1, scn.GetThreats());

		// Any companion but the Ring-bearer, the one about to be killed included; no ally, no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, aragorn, gandalf, knight));
		assertFalse(scn.ShadowHasCardChoiceAvailable(frodo));
		assertFalse(scn.ShadowHasCardChoiceAvailable(thrarin));
		assertFalse(scn.ShadowHasCardChoiceAvailable(party1));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);
		assertInDiscard(aragorn);

		// Aragorn was discarded, not killed: no second offer, though a threat is left to pay with.  Gimli is killed
		// and the threat left becomes a wound.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bold));
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));

		// Each time: a threat again, and Gandalf about to be killed in the next skirmish.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bold));
		scn.ShadowChooseOptionalTrigger(bold);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, knight));
		assertFalse(scn.ShadowHasCardChoiceAvailable(frodo));
		scn.ShadowChooseCard(knight);
		assertInDiscard(knight);
		assertInZone(Zone.DEAD, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningDiscardsNoCompanionAndBothThreatsBecomeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(gimli, aragorn, knight);
		scn.MoveCardsToSupportArea(bold);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party1);

		assertEquals(2, scn.GetThreats());
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bold));
		scn.ShadowDeclineOptionalTrigger();

		// Nobody is discarded, and both threats are there to become wounds as Gimli is killed.
		assertInPlay(aragorn, knight);
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAMinionOrAnAllyIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		var runner = scn.GetShadowCard("runner");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(bold);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, party1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes: vitality 2, one left.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{thrarin, party1});

		// Aragorn (8) kills the Goblin Runner (5, vitality 1): a minion killed.
		assertEquals(1, scn.GetThreats());
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bold));
		assertEquals(1, scn.GetThreats());

		// Thrarin loses to the Raiding Party: an ally killed.  The threat becomes a wound on a companion.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bold));
		assertInZone(Zone.DEAD, thrarin);
		assertInPlay(aragorn, knight);
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(bold);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party1);

		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bold));
		assertInZone(Zone.DEAD, gimli);
		assertInPlay(aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsDiscardedAtTheStartOfTheRegroupPhaseAndNotBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bold = scn.GetShadowCard("bold");
		var greed = scn.GetShadowCard("greed");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(bold, greed);
		assertInZone(Zone.SUPPORT, bold, greed);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		// Past the starts of the maneuver, archery and assignment phases, and into the skirmish.
		assertInZone(Zone.SUPPORT, bold);
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertInZone(Zone.SUPPORT, bold);
		scn.PassSkirmishActions();

		// At the start of the regroup phase it goes, and the other Shadow condition stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(bold);
		assertInZone(Zone.SUPPORT, greed);
	}
}
