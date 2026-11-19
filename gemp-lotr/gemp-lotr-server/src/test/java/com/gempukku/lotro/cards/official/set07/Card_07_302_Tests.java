package com.gempukku.lotro.cards.official.set07;

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

public class Card_07_302_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("officer", "7_302");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai (no Orc), strength 9; damage +1 only
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5, or 9 and fierce while the Ring-bearer has 2 wounds
					put("dwarfguard", "1_7");   // Dwarf Guard: [dwarven] Dwarf companion, strength 4, vitality 2; a requirement to play only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3; +2 only while he bears a weapon
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally of strength 4 who may be made to skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcOfficerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Officer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Each time a companion is overwhelmed in a skirmish that involves a [sauron] Orc, add 3 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("officer");

		assertEquals("Orc Officer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachCompanionOverwhelmedInASkirmishOfASauronOrcAddsThreeBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(dwarfguard, merry);

		var officer = scn.GetShadowCard("officer");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(officer, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dwarfguard, officer}, new PhysicalCardImpl[]{merry, chieftain});
		scn.ShadowDeclineAssignments();
		assertEquals(0, scn.FreepsGetBurdens());

		// 9 against 4: the Dwarf Guard is overwhelmed by the Officer himself.
		scn.FreepsResolveSkirmish(dwarfguard);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, dwarfguard);
		assertEquals(3, scn.FreepsGetBurdens());

		// 7 against 3: Merry is overwhelmed by another [sauron] Orc, the Officer in no part of that skirmish.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, merry);
		assertEquals(6, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyOverwhelmedByTheOfficerAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var officer = scn.GetShadowCard("officer");

		scn.StartGame();
		scn.MoveMinionsToTable(officer);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, officer);
		assertEquals(0, scn.FreepsGetBurdens());

		// 9 against 4: Thrarin is overwhelmed in the Officer's skirmish, and he is an ally, no companion.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionOverwhelmedByASauronUrukHaiOrAWraithOrcAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(dwarfguard, merry);

		var officer = scn.GetShadowCard("officer");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		// Two wounds on the Ring-bearer: the Hound is strength 9.
		scn.AddWoundsToChar(scn.GetRingBearer(), 2);
		assertEquals(2, scn.GetWoundsOn(scn.GetRingBearer()));
		// The Officer is in play and in no skirmish.
		scn.MoveMinionsToTable(officer, guard, hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, guard}, new PhysicalCardImpl[]{dwarfguard, hound});
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(officer));
		assertEquals(9, scn.GetStrength(hound));
		assertEquals(0, scn.FreepsGetBurdens());

		// 9 against 3: Merry is overwhelmed by a [sauron] minion who is no Orc.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertEquals(0, scn.FreepsGetBurdens());

		// 9 against 4: the Dwarf Guard is overwhelmed by an Orc who is not [sauron].
		scn.FreepsResolveSkirmish(dwarfguard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, dwarfguard);
		assertEquals(0, scn.FreepsGetBurdens());
		// The Hound is fierce: the fierce round comes next.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AMinionOverwhelmedInASauronOrcSkirmishAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var officer = scn.GetShadowCard("officer");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(officer, chieftain);
		// A strength that only arranges the board: 8 + 6 is 14, double the Chieftain's 7.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		assertEquals(14, scn.GetStrength(aragorn));
		assertEquals(0, scn.FreepsGetBurdens());

		// The Chieftain, a [sauron] Orc, is overwhelmed: a character who is no companion.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, officer);
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledByTheWoundForLosingToTheOfficerAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var officer = scn.GetShadowCard("officer");

		scn.StartGame();
		scn.MoveMinionsToTable(officer);
		// Vitality 4 with 3 wounds: the wound for losing kills him.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, officer);
		assertEquals(0, scn.FreepsGetBurdens());

		// 9 against 8: Aragorn loses and is killed by the wound, not overwhelmed.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
