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

public class Card_07_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "7_90");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man companion, strength 7; text only on being overwhelmed
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("horseman", "7_226");   // Enraged Horseman: [rohan] Man companion; text only in a skirmish

					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; text about unbound Hobbits
					put("shelob", "8_25");      // Shelob, Eater of Light: [gollum] Spider, fierce, strength 8, vitality 8; cheated in
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5, vitality 2; fierce only at 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Faramir, Stout Captain
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: Ranger. To play, spot a [gondor] Man.
		 * Each time Faramir wins a skirmish involving a fierce minion, you may exert Faramir to discard that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", card.getBlueprint().getTitle());
		assertEquals("Stout Captain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilAGondorManIsSpottedAndAGondorHobbitOrRohanManDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var horseman = scn.GetFreepsCard("horseman");
		scn.MoveCompanionsToTable(pippin, horseman);
		scn.MoveCardsToHand(faramir, boromir);
		assertInHand(faramir, boromir);

		scn.StartGame();

		// A [gondor] Hobbit and a [rohan] Man: no [gondor] Man.
		assertFalse(scn.FreepsPlayAvailable(faramir));

		scn.FreepsPlayCard(boromir);
		assertTrue(scn.FreepsPlayAvailable(faramir));
		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheGondorManAllyIsSpottedToPlayHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(faramir);
		assertInHand(faramir);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, merchant);
		assertTrue(scn.FreepsPlayAvailable(faramir));
		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BeatingAFierceMinionOffersToExertHimToDiscardItAndOffersAgainInTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(faramir);

		scn.StartGame();

		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, pursuer);
		scn.FreepsResolveSkirmish(faramir);
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(pursuer));

		// Faramir, strength 7, beats the Pursuer, strength 5, who takes a wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faramir));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);

		// The fierce skirmish: he wins a second time, and is asked again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(faramir, pursuer);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faramir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertInDiscard(pursuer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenHeBeatsAMinionThatIsNotFierceOrWhenAnotherCompanionBeatsAFierceOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var pursuer = scn.GetShadowCard("pursuer");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(faramir, boromir);

		scn.StartGame();

		scn.MoveMinionsToTable(pursuer, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, hound},
				new PhysicalCardImpl[]{boromir, pursuer});

		// Faramir, strength 7, beats the Hound, strength 5, which is not fierce (no wound on the Ring-bearer).
		scn.FreepsResolveSkirmish(faramir);
		assertFalse(scn.HasKeyword(hound, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(hound));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(faramir));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Boromir, strength 7, beats the fierce Pursuer.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(faramir));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer, hound);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenHeLosesToAFierceMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(faramir);

		scn.StartGame();

		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, shelob);
		scn.FreepsResolveSkirmish(faramir);
		assertEquals(0, scn.GetWoundsOn(faramir));

		// Shelob, strength 8, beats Faramir, strength 7.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(faramir));
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OfTwoMinionsHeBeatsInOneSkirmishOnlyTheFierceOneIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var shelob = scn.GetShadowCard("shelob");
		var hound = scn.GetShadowCard("hound");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(faramir, boromir);

		scn.StartGame();

		scn.MoveMinionsToTable(shelob, hound, pursuer);
		// Strength enough to beat both: 7 + 7 = 14 against Shelob's 8 and the Hound's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, faramir, null, 7));
		scn.SkipToAssignments();
		// The fierce Pursuer is in Boromir's skirmish, not in Faramir's.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, shelob},
				new PhysicalCardImpl[]{boromir, pursuer});
		// The Hound is left over, and the Shadow player puts it on Faramir too.
		scn.ShadowAssignToMinions(faramir, hound);
		scn.FreepsResolveSkirmish(faramir);
		assertEquals(14, scn.GetStrength(faramir));
		assertEquals(0, scn.GetWoundsOn(faramir));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faramir));
		scn.FreepsAcceptOptionalTrigger();
		// "That minion" is Shelob, the only fierce minion Faramir beat; the Pursuer is fierce too but he did not beat it.
		// Nothing is asked.
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertInDiscard(shelob);
		assertInZone(Zone.SHADOW_CHARACTERS, hound, pursuer);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void NotOfferedWhenHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(faramir);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(faramir, 2);
		assertEquals(2, scn.GetWoundsOn(faramir));

		scn.StartGame();

		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, pursuer);
		scn.FreepsResolveSkirmish(faramir);
		assertEquals(0, scn.GetWoundsOn(pursuer));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(faramir));
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithTheFierceMinionKilledByTheWoundOfHisWinTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(faramir);

		scn.StartGame();

		scn.MoveMinionsToTable(pursuer);
		// Vitality 3 with 2 wounds: the wound for losing kills him.
		scn.AddWoundsToChar(pursuer, 2);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, pursuer);
		scn.FreepsResolveSkirmish(faramir);
		assertEquals(0, scn.GetWoundsOn(faramir));

		scn.PassSkirmishActions();
		assertInDiscard(pursuer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faramir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertInDiscard(pursuer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
