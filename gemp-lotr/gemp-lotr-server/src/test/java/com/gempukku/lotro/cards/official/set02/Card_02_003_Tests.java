package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bracers", "2_3");
					put("bracers2", "2_3");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3; a fellowship ability only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, vitality 2; to play, spot a Dwarf
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion; an archery ability only
					put("marksman", "1_176");   // Goblin Marksman: an archer minion, no other text
					// Uruk-hai Raiding Party: strength 9, damage +1, so the companion it beats takes two wounds
					put("raiders", "1_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenBracersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Dwarven Bracers
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Bracers
		 * Strength: 1
		 * Game Text: Bearer must be a Dwarf.<br><b>Response:</b> If bearer is about to take a wound, discard this possession to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bracers");

		assertEquals("Dwarven Bracers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.BRACERS));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeADwarfCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bracers = scn.GetFreepsCard("bracers");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(bracers);
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(bracers));
		scn.FreepsPlayCard(bracers);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);
		assertAttachedTo(bracers, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseDiscardsItToPreventAnArcheryWoundOnBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bracers = scn.GetFreepsCard("bracers");
		var gimli = scn.GetFreepsCard("gimli");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, bracers);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		// The Marksman's one archery wound is placed on Gimli.
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(bracers, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bracers));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, bracers);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnlyBearersCopyAnswersAndItPreventsOneOfTwoWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bracers = scn.GetFreepsCard("bracers");
		var bracers2 = scn.GetFreepsCard("bracers2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, bracers);
		scn.AttachCardsTo(guard, bracers2);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, raiders);
		scn.FreepsResolveSkirmish(gimli);
		// Gimli (6 + 1) loses to the Raiding Party (9, damage +1) and is to take two wounds.
		scn.PassCurrentPhaseActions();

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertAttachedTo(bracers, gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bracers));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bracers2));
		scn.FreepsAcceptOptionalTrigger();
		// The first wound is prevented and the Bracers are gone; the second lands.
		assertInZone(Zone.DISCARD, bracers);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertAttachedTo(bracers2, guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
