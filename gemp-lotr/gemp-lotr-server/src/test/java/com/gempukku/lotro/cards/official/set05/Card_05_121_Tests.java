package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "5_121");
					put("farin", "1_11");      // Farin: Dwarf companion, vitality 3; strength +2 only while skirmishing an Orc
					put("haldir", "1_48");     // Haldir: Elf companion, vitality 3; strength +2 only at sites 6 to 8
					put("grimir", "1_17");     // Grimir: Dwarf ally, a fellowship ability used only on purpose
					put("silinde", "1_60");    // Silinde: Elf ally; copies site 3's text only while site 3 can be spotted
					put("knight", "5_35");     // Gondorian Knight: [gondor] Man companion with no text but Knight: a companion who is neither a Dwarf nor an Elf

					put("savage", "1_151");    // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("pursuer", "4_190");   // Uruk Pursuer: printed fierce, strength 5, vitality 3; texts about unbound Hobbits
					put("soldier", "1_271");   // Orc Soldier: strength 7, vitality 2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Legolas, Archer of Mirkwood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Archer</b>.
		 * 	Each time Legolas wins a skirmish, you may heal a Dwarf companion or another Elf companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Archer of Mirkwood", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void EachTimeLegolasWinsASkirmishHeMayHealADwarfCompanionOrAnotherElfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var farin = scn.GetFreepsCard("farin");
		var haldir = scn.GetFreepsCard("haldir");
		var grimir = scn.GetFreepsCard("grimir");
		var silinde = scn.GetFreepsCard("silinde");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(legolas, farin, haldir, knight);
		scn.MoveAlliesToTable(grimir, silinde);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		// The Knight is a wounded companion who is neither a Dwarf nor an Elf.
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(farin, 2);
		scn.AddWoundsToChar(haldir, 2);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(silinde, 1);
		scn.AddWoundsToChar(knight, 1);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(knight));

		// Fierce in print, so that Legolas can win a second skirmish against it in the same turn.
		scn.MoveMinionsToTable(pursuer);

		// Legolas is an archer: his archery wound goes on the Pursuer (vitality 3).
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		scn.FreepsAssignAndResolve(legolas, pursuer);
		scn.PassSkirmishActions();

		// Legolas 6 against 5: he wins.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(farin, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, grimir, silinde, knight));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(farin);

		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(knight));

		// The fierce skirmish: Legolas wins again and is asked again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(legolas, pursuer);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassFierceSkirmishActions();

		assertInDiscard(pursuer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(farin, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, grimir, silinde, knight));
		scn.FreepsChooseCard(haldir);

		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheHealCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var farin = scn.GetFreepsCard("farin");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(legolas, farin, haldir);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(farin, 2);
		scn.AddWoundsToChar(haldir, 2);
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(haldir));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(legolas, savage);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LegolasLosingASkirmishDoesNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(legolas, farin);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.AddWoundsToChar(farin, 2);
		assertEquals(2, scn.GetWoundsOn(farin));
		scn.MoveMinionsToTable(soldier);

		// Legolas's archery wound goes on the Soldier, which leaves him exhausted and unable to use his ability.
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsAssignAndResolve(legolas, soldier);
		scn.PassSkirmishActions();

		// Legolas 6 against 7: he loses, takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legolas));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoWoundedDwarfOrOtherElfCompanionTheTriggerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var farin = scn.GetFreepsCard("farin");
		var haldir = scn.GetFreepsCard("haldir");
		var grimir = scn.GetFreepsCard("grimir");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(legolas, farin, haldir, knight);
		scn.MoveAlliesToTable(grimir);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Only Legolas himself, a Dwarf ally and a Man companion are wounded: nothing the trigger could heal.
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(legolas, savage);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legolas));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
