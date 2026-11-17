package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_04_096_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tongue", "54_96");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: Frodo signet; a strength bonus and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], Gandalf signet, strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven], Gandalf signet
					put("boromir", "1_97");     // Boromir, Son of Denethor: Frodo signet
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion with no signet
					put("guardian", "4_76");    // Lorien Guardian: Elf companion with no signet
					put("border", "1_32");      // Border Defenses: Elven skirmish event, cost 1, "make an Elf strength +2"
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGreyWizardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tongue", "54_96");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: Gandalf signet, strength 7; a fellowship ability used only on purpose
					put("enquea", "1_231");     // Ulaire Enquea, Lieutenant of Morgul: fierce, strength 11; a maneuver ability that needs 6 companions
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KeepYourForkedTongueStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Keep Your Forked Tongue
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot Gandalf and 3 twilight tokens to make a companion who has the Gandalf signet unable to take wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tongue");

		assertEquals("Keep Your Forked Tongue", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGandalfAnd3TwilightMakesAGandalfSignetCompanionUnableToTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tongue = scn.GetFreepsCard("tongue");
		var border = scn.GetFreepsCard("border");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var avenger = scn.GetFreepsCard("avenger");
		var guardian = scn.GetFreepsCard("guardian");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn, gimli, boromir, avenger, guardian);
		scn.MoveCardsToHand(tongue, border);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		// One token short; set before the skirmish's first decision is made.
		scn.SetTwilight(2);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.FreepsPlayAvailable(tongue));

		scn.FreepsPlayCard(border);
		// 2 + 1 for Border Defenses
		assertEquals(3, scn.GetTwilight());
		scn.ShadowPass();

		assertTrue(scn.FreepsPlayAvailable(tongue));
		scn.FreepsPlayCard(tongue);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, boromir, avenger, guardian, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInDiscard(tongue);
		// 3 + 1 for the event
		assertEquals(4, scn.GetTwilight());

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// Aragorn (8) loses to the troop (13) and is not wounded.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tongue = scn.GetFreepsCard("tongue");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, avenger);
		scn.MoveCardsToHand(tongue);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.SetTwilight(10);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(tongue));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheCompanionCanTakeWoundsAgainInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGreyWizardScenario();

		var tongue = scn.GetFreepsCard("tongue");
		var gandalf = scn.GetFreepsCard("gandalf");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(tongue);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, enquea);
		scn.SetTwilight(5);
		scn.FreepsResolveSkirmish(gandalf);

		// Gandalf is the one companion with the Gandalf signet, and is chosen.
		scn.FreepsPlayCard(tongue);
		scn.PassSkirmishActions();
		// Gandalf (7) loses to Enquea (11) and is not wounded.
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// The fierce skirmish.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, enquea);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
