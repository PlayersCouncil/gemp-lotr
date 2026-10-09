package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_016_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "5_28");         // Sméagol, Old Noser: [gollum] companion, strength 3, vitality 4; Ring-bound
					put("shireSmeagol", "103_116"); // Smeagol, Once Like Us: [shire] companion, strength 3, vitality 4; Ring-bound
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4
					put("wraithGollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, strength 5, vitality 4
					put("shelob", "8_25");          // Shelob, Eater of Light: [gollum] Spider minion, strength 8, vitality 8; fierce
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; Ring-bound
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4
					put("legolas", "1_50");         // Legolas, Greenleaf: [elven] Elf companion, strength 6, vitality 3; Archer
					put("lurtz", "1_127");          // Lurtz, Servant of Isengard: [isengard] Uruk-hai, strength 13, vitality 3; Archer, Damage +1
					put("savage1", "1_151");        // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_16", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "5_28");         // Sméagol, Old Noser: [gollum] companion, strength 3, vitality 4; Ring-bound
					put("shireSmeagol", "103_116"); // Smeagol, Once Like Us: [shire] companion, strength 3, vitality 4; Ring-bound
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4
					put("wraithGollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, strength 5, vitality 4
					put("shelob", "8_25");          // Shelob, Eater of Light: [gollum] Spider minion, strength 8, vitality 8; fierce
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; Ring-bound
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4
					put("legolas", "1_50");         // Legolas, Greenleaf: [elven] Elf companion, strength 6, vitality 3; Archer
					put("lurtz", "1_127");          // Lurtz, Servant of Isengard: [isengard] Uruk-hai, strength 13, vitality 3; Archer, Damage +1
					put("savage1", "1_151");        // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_16"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_16
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Gollum and Smeagol cannot take wounds.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_16", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void GollumAndSmeagolImmuneToArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		// Legolas (FP archer) and Lurtz (Shadow archer) each deal 1 archery wound.
		// Smeagol should be immune to the Shadow archery wound.
		// Gollum should be immune to the FP archery wound.
		// Legolas's arrow must go to Lurtz (only valid shadow target).
		// Lurtz's arrow can go to Frodo or Legolas but NOT Smeagol.
		var scn = GetShadowScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var legolas = scn.GetFreepsCard("legolas");
		var gollum = scn.GetShadowCard("gollum");
		var lurtz = scn.GetShadowCard("lurtz");

		scn.MoveCompanionsToTable(smeagol, legolas);
		scn.MoveMinionsToTable(gollum, lurtz);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		// FP archery total: 1 (Legolas). Shadow archery total: 1 (Lurtz).
		// Gollum can't take wounds, so Legolas's arrow auto-goes to Lurtz.
		// Lurtz's arrow: Smeagol can't take wounds, so only Frodo/Legolas are valid.
		// We should be prompted to assign Lurtz's arrow to Frodo or Legolas.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
		assertFalse(scn.FreepsHasCardChoiceAvailable(smeagol));

		scn.FreepsChooseCard(legolas);

		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void SmeagolOfTheShireLosesASkirmishUnwoundedWhenHisOwnerHasTheModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var smeagol = scn.GetFreepsCard("shireSmeagol");
		var sam = scn.GetFreepsCard("sam");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.MoveCompanionsToTable(smeagol, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, savage1}, new PhysicalCardImpl[]{sam, savage2});

		// Each has strength 3 and loses to an Uruk Savage's 5 without being overwhelmed;
		// Damage +1 makes the loss 2 wounds.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(smeagol));

		// Sam, a [shire] Ring-bound companion as Smeagol is, takes both.
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GollumOfTheWraithCultureTakesNoArrowAndNoSkirmishWoundWhenTheFreePeoplesPlayerHasTheModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gollum = scn.GetShadowCard("wraithGollum");
		var shelob = scn.GetShadowCard("shelob");
		var savage = scn.GetShadowCard("savage1");

		scn.MoveCompanionsToTable(aragorn, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob, savage);

		// Legolas's arrow: Shelob, of Gollum's old culture and not Gollum, and the Savage may take it.
		scn.SkipToArcheryWounds();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gollum));
		scn.ShadowChooseCard(shelob);
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(gollum));

		// Shelob is left out of the skirmishes.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{legolas, savage});
		scn.ShadowDeclineAssignments();

		// Aragorn's 8 beats Gollum's 5 without overwhelming him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(gollum));

		// Legolas's 6 beats the Savage's 5, and the Savage is wounded as a loser is.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(legolas);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(gollum));

		// Shelob is fierce: the fierce assignments are next.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
