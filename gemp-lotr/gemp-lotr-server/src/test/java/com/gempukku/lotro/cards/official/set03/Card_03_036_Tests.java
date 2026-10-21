package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("perils", "3_36");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3
					// Bounder: [shire] Hobbit ally, home site 2 of the Fellowship path, where he may be assigned; his
					// skirmish ability is used only on purpose.
					put("bounder", "1_286");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");     // Goblin Runner: strength 5; its trigger is on being played
					put("scout", "1_191");      // Moria Scout: strength 6, vitality 2; its trigger is on being played
					// Ted Sandyman, Chief's Men's Ally: a minion of strength 3; his abilities are used only on purpose.
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnknownPerilsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Unknown Perils
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Response:</b> If a companion is about to take a wound, spot 4 twilight tokens and exert Gandalf to prevent that wound.
		*/

		var scn = GetScenario();

		var perils = scn.GetFreepsCard("perils");

		assertEquals("Unknown Perils", perils.getBlueprint().getTitle());
		assertNull(perils.getBlueprint().getSubtitle());
		assertFalse(perils.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, perils.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, perils.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, perils.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(perils, Keyword.SUPPORT_AREA));
		assertEquals(2, perils.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFourTwilightTokensGandalfExertsToPreventTheWoundAboutToBeTakenAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var perils = scn.GetFreepsCard("perils");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveCardsToSupportArea(perils);
		assertInZone(Zone.SUPPORT, perils);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, party);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// 9 against 8: Aragorn loses, and the damage +1 winner gives him two wounds, one at a time.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(perils));
		scn.FreepsAcceptOptionalTrigger();
		// Gandalf pays, not the other [gandalf] companion; the 4 tokens are spotted, not removed.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(4, scn.GetTwilight());

		// "That wound" is the one wound: the second is answered afresh, and declined it lands.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(perils));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeTwilightTokensItIsNotOfferedAndTheWoundLands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var perils = scn.GetFreepsCard("perils");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(perils);
		assertInZone(Zone.SUPPORT, perils);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// 13 against 8: a loss, not an overwhelming.
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(perils));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithGandalfExhaustedItIsNotOfferedAndTheWoundLands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var perils = scn.GetFreepsCard("perils");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveCardsToSupportArea(perils);
		assertInZone(Zone.SUPPORT, perils);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Gandalf is exhausted; the Ent Avenger could exert, but is no Gandalf.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(perils));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyACompanionsWoundIsPreventedAndNotAnAllysOrAMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var perils = scn.GetFreepsCard("perils");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(gandalf, aragorn, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(perils);
		assertInZone(Zone.SUPPORT, perils);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, runner, ted);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scout}, new PhysicalCardImpl[]{sam, runner},
				new PhysicalCardImpl[]{bounder, ted});
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(sam));

		// 8 against 6: the minion loses and takes the wound; no offer.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(perils));

		// 2 against 3: the ally at his home site loses and takes the wound; no offer.
		scn.FreepsResolveSkirmish(bounder);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(perils));

		// 3 against 5: the [shire] Hobbit companion loses, and his wound is prevented.
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(perils));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
