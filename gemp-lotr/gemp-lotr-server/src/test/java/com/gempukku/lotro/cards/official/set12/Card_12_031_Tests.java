package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_031_Tests
{

	// The Ring-bearer is Frodo, Son of Drogo (resistance 10), who is kept out of the skirmishes.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wizard", "12_31");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: strength 8, vitality 4, no text
					// Ent Avenger: a [gandalf] companion who is no Gandalf, strength 6; +2 only beside 3 Ents
					put("avenger", "6_27");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
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
					put("wizard1", "12_31");
					put("wizard2", "12_31");
					// Gandalf, Friend of the Shirefolk: strength 6, and +1 for each of Hobbit, Dwarf, Elf and Man
					// among the companions: 7 beside the Ring-bearer alone
					put("gandalf", "1_72");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MysteriousWizardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Mysterious Wizard
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>. Make Gandalf strength +2 (or +4 if you cannot spot 5 burdens).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wizard");

		assertEquals("Mysterious Wizard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesGandalfStrengthPlus4WithFourBurdensAndPlus2WithFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var wizard1 = scn.GetFreepsCard("wizard1");
		var wizard2 = scn.GetFreepsCard("wizard2");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wizard1, wizard2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(4, scn.GetBurdens());
		// 6, and 1 for the Hobbit who bears the Ring
		assertEquals(7, scn.GetStrength(gandalf));

		// Four burdens: five cannot be spotted.
		scn.FreepsPlayCard(wizard1);
		// 7 + 4
		assertEquals(11, scn.GetStrength(gandalf));
		assertInZone(Zone.DISCARD, wizard1);

		// The fifth burden, and the second copy in the same skirmish.
		scn.ShadowPass();
		scn.AddBurdens(1);
		assertEquals(5, scn.GetBurdens());

		scn.FreepsPlayCard(wizard2);
		// 7 + 4 + 2
		assertEquals(13, scn.GetStrength(gandalf));
		assertInZone(Zone.DISCARD, wizard2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithFiveBurdensTheBonusIsGoneOnceGandalfsSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wizard);

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(8, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(wizard);

		// Five burdens can be spotted: +2.
		assertEquals(10, scn.GetStrength(gandalf));
		assertInDiscard(wizard);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 10 against 13: Gandalf loses and takes a wound, and the bonus is gone with his skirmish phase.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(8, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wizard);

		scn.StartGame();
		assertInHand(wizard);
		scn.MoveMinionsToTable(troop);

		// The phase before the skirmishes: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wizard));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.FreepsPlayAvailable(wizard));
		assertInHand(wizard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InAnotherCompanionsSkirmishItStillMakesGandalfStrongerAndNotTheSkirmishingGandalfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(wizard);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, brigand);
		scn.FreepsResolveSkirmish(avenger);

		assertEquals(0, scn.GetBurdens());
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));

		scn.FreepsPlayCard(wizard);

		// "Make Gandalf": no skirmish is named, and the [gandalf] companion who is fighting is not Gandalf.  No choice
		// is asked.  No burdens: +4.
		assertEquals(12, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertInDiscard(wizard);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGandalfInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var avenger = scn.GetFreepsCard("avenger");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(wizard);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, brigand);
		scn.FreepsResolveSkirmish(avenger);

		assertEquals(6, scn.GetStrength(avenger));
		// Its target is no requirement of its play (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(wizard));

		scn.FreepsPlayCard(wizard);

		assertEquals(6, scn.GetStrength(avenger));
		assertInDiscard(wizard);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
