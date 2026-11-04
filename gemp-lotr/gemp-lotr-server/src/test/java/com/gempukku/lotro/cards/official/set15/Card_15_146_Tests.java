package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_146_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brewer", "15_146");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; his ability is used only on purpose
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit, strength 3, vitality 4 (to play,
					// add a burden: cheated in); an assignment ability only.
					put("smeagol", "103_116");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; his ability is used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("troop2", "1_177");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brewer", "15_146");
					// Bounder: a [shire] Hobbit ally (home 2F); a skirmish ability used only on purpose.
					put("bounder", "1_286");
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated
					// in); a skirmish ability used only on purpose.
					put("pippin", "103_31");
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit
					// Rohan Worker: a follower (no race); its texts answer a transfer and a skirmish won.
					put("worker", "15_135");
				}},
				VirtualTableScenario.FellowshipSites,
				// Gimli, Bearer of Grudges: a Dwarf Ring-bearer, so that no Hobbit is in play unless one is laid.
				// His text acts at the start of a skirmish only.
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitonBrewerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Hobbiton Brewer, Maker of Fine Ales
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Follower
		 * Subtype: 
		 * Strength: +2
		 * Game Text: <b>Aid</b> – Add 2 burdens. <helper>(At the start of the maneuver phase, you may add 2 burdens to transfer this to a companion.)</helper><br>To play, spot a Hobbit.<br><b>Response:</b> If bearer is about to take a wound, transfer Hobbiton Brewer to your support area to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brewer");

		assertEquals("Hobbiton Brewer", card.getBlueprint().getTitle());
		assertEquals("Maker of Fine Ales", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void AidAddsTwoBurdensToTransferHimToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brewer = scn.GetFreepsCard("brewer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(brewer);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));

		assertInZone(Zone.SUPPORT, brewer);
		assertEquals(0, scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brewer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(brewer, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAHobbitUntilAHobbitAllyIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var brewer = scn.GetFreepsCard("brewer");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCardsToHand(brewer, bounder);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(worker);

		scn.StartGame();

		// Near misses: a [shire] companion who is no Hobbit, and a follower, which has no race.
		assertFalse(scn.IsRace(smeagol, Race.HOBBIT));
		assertInZone(Zone.SUPPORT, worker);
		assertTrue(scn.FreepsPlayAvailable(bounder));
		assertFalse(scn.FreepsPlayAvailable(brewer));

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		// A Hobbit who is an ally will do.
		assertTrue(scn.FreepsPlayAvailable(brewer));

		scn.FreepsPlayCard(brewer);
		assertInZone(Zone.SUPPORT, brewer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitCompanionOfAnotherCultureIsSpottedToPlayHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var brewer = scn.GetFreepsCard("brewer");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(brewer);
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();

		assertEquals(Culture.GONDOR, pippin.getBlueprint().getCulture());
		assertTrue(scn.FreepsPlayAvailable(brewer));

		scn.FreepsPlayCard(brewer);
		assertInZone(Zone.SUPPORT, brewer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenBearerIsAboutToTakeAWoundHeGoesToTheSupportAreaToPreventItAndAnotherCompanionsWoundIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brewer = scn.GetFreepsCard("brewer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, brewer);
		scn.MoveMinionsToTable(troop, troop2);

		assertAttachedTo(brewer, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{boromir, troop2});

		// Boromir, who does not bear him, loses (7 against 13, short of double) and takes his wound unanswered.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brewer));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertAttachedTo(brewer, aragorn);

		// Aragorn loses (8 + 2 against 13).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brewer));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.SUPPORT, brewer);
		assertNotAttachedTo(brewer, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfTwoWoundsFromADamagePlus1MinionOnlyTheFirstIsPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brewer = scn.GetFreepsCard("brewer");
		var smeagol = scn.GetFreepsCard("smeagol");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.AttachCardsTo(smeagol, brewer);
		scn.MoveMinionsToTable(raiders);

		assertAttachedTo(brewer, smeagol);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, raiders);

		// 3 + 2 against 9: Smeagol loses, and is to take two wounds.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brewer));
		scn.FreepsAcceptOptionalTrigger();

		// The first wound is prevented; Smeagol bears him no longer, so the second is taken.
		assertInZone(Zone.SUPPORT, brewer);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brewer));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
