package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tongue", "4_96");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: Frodo signet; a strength bonus and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], Gandalf signet, strength 8
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven], Gandalf signet
					put("boromir", "1_97");     // Boromir, Son of Denethor: Frodo signet
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion with no signet
					put("guardian", "4_76");    // Lórien Guardian: Elf companion with no signet
					put("border", "1_32");      // Border Defenses: [elven] skirmish event, cost 1, "make an Elf strength +2"
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
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
					put("tongue", "4_96");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: spotted; his own signet is Frodo's
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Gandalf signet, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: Gandalf signet
					// Uruk-hai Raiding Party: strength 9, Damage +1 its whole text.  It beats Aragorn by one
					// and deals two wounds.
					put("raiders", "1_158");
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
					put("tongue", "4_96");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: Gandalf signet, strength 7; a fellowship ability used only on purpose
					put("enquea", "1_231");     // Úlairë Enquëa, Lieutenant of Morgul: fierce, strength 11; a maneuver ability that needs 6 companions
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
		 * Game Text: <b>Skirmish:</b> Spot Gandalf and 3 twilight tokens to prevent all wounds to a companion who has the Gandalf signet.
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
	public void PreventsEveryWoundToACompanionWithTheGandalfSignetInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var tongue = scn.GetFreepsCard("tongue");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(gandalf, aragorn, gimli);
		scn.MoveCardsToHand(tongue);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		scn.FreepsResolveSkirmish(aragorn);

		// The move added 4 for the companions and 2 for Ettenmoors: more than the 3 to be spotted.
		assertEquals(6, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(raiders));
		assertTrue(scn.FreepsPlayAvailable(tongue));

		scn.FreepsPlayCard(tongue);
		// Gandalf himself and Frodo have the Frodo signet.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));

		scn.FreepsChooseCard(aragorn);
		// 1 for the event; the 3 tokens are spotted and stay.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.DISCARD, tongue);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.ShadowPass();
		scn.FreepsPass();
		// Aragorn loses 8 to 9 and Damage +1 makes it two wounds: still 1, where one wound prevented
		// would read 2 and none prevented 3.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NeedsThreeTwilightTokensAndOffersOnlyCompanionsWithTheGandalfSignet() throws DecisionResultInvalidException, CardNotFoundException {
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

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
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
		// 3 + 1 for the event: the 3 spotted are not removed.
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

		// Ent Avenger is a [gandalf] companion who is no Gandalf.
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(tongue));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoCompanionWithTheGandalfSignetItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tongue = scn.GetFreepsCard("tongue");
		var gandalf = scn.GetFreepsCard("gandalf");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, boromir);
		scn.MoveCardsToHand(tongue);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.SetTwilight(5);
		scn.FreepsResolveSkirmish(boromir);

		// Gandalf, Boromir and Frodo all have the Frodo signet: nothing to choose, and the event is still
		// played (an event whose target finds nothing: F-0665).
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsPlayAvailable(tongue));
		scn.FreepsPlayCard(tongue);
		assertInDiscard(tongue);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		// Boromir (7) loses to the troop (13) and takes his wound.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheCompanionTakesWoundsAgainInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(tongue);
		assertInDiscard(tongue);
		scn.PassSkirmishActions();
		// Gandalf (7) loses to Enquea (11) and is not wounded.
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// The fierce skirmish is a skirmish phase of its own: the prevention is gone.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, enquea);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
