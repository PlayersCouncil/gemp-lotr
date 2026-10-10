package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_121_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "55_121");
					put("farin", "1_11");      // Farin: Dwarf companion, vitality 3; strength +2 only while skirmishing an Orc
					put("haldir", "1_48");     // Haldir: Elf companion, vitality 3; strength +2 only at sites 6 to 8
					put("grimir", "1_17");     // Grimir: Dwarf ally, a fellowship ability used only on purpose
					put("silinde", "1_60");    // Silinde: Elf ally; copies site 3's text only while site 3 can be spotted
					put("knight", "5_35");     // Gondorian Knight: [gondor] Man companion with no text but Knight: a companion who is neither a Dwarf nor an Elf

					put("savage1", "1_151");   // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
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
		 * Game Text: <b>Archer</b>. Valiant.
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
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void LegolasIsPlayedFromHandForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(legolas);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(legolas));
		scn.FreepsPlayCard(legolas);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		// The Knight is a wounded companion who is neither a Dwarf nor an Elf.
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(farin, 2);
		scn.AddWoundsToChar(haldir, 2);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(silinde, 1);
		scn.AddWoundsToChar(knight, 1);

		scn.MoveMinionsToTable(savage);
		// Fierce, so that Legolas can win a second skirmish against it in the same turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));

		// Legolas is an archer: his archery wound goes on the Savage (vitality 3).
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsAssignAndResolve(legolas, savage);
		scn.PassSkirmishActions();

		// Legolas 6 against 5: he wins.
		assertEquals(2, scn.GetWoundsOn(savage));
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
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassFierceSkirmishActions();

		assertInDiscard(savage);
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

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.AddWoundsToChar(farin, 2);
		scn.AddWoundsToChar(haldir, 2);
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
	public void AnotherElfWinningOrLegolasLosingDoesNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var farin = scn.GetFreepsCard("farin");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(legolas, farin, haldir);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.AddWoundsToChar(farin, 2);
		scn.MoveMinionsToTable(savage1, savage2);
		// Haldir 8 beats a Savage at 5; Legolas 6 loses to a Savage at 10 without being overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, haldir, null, 3));
		scn.ApplyAdHocModifier(new StrengthModifier(null, savage1, null, 5));

		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(savage2);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, savage1}, new PhysicalCardImpl[]{haldir, savage2});

		scn.FreepsResolveSkirmish(haldir);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legolas));

		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(legolas)); // the Savage's damage +1
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

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		// Only Legolas himself, a Dwarf ally and a Man companion are wounded: nothing the trigger could heal.
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(knight, 1);
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(legolas, savage);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legolas));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
