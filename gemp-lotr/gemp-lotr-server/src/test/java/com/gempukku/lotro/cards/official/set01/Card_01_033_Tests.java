package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "1_33");
					put("legolas", "11_21");    // Legolas, Companion of the Ring: [elven] Elf companion, strength 6, vitality 3; no archer
					                            // (an archer would stop the skippers), and his bonus wants a battleground or forest, which site 2 is not
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf companion who is not Legolas
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("pursuer", "4_190");    // Uruk Pursuer: strength 5, vitality 3, printed fierce
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1 (cheated in, so its play trigger never fires)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "1_33");
					put("legolas", "31_9");     // Legolas, Prince of Mirkwood: the Legolas who is an ally ([elven] Elf, home 5H)
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf companion who is not Legolas
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BowoftheGaladhrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bow of the Galadhrim
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: +1
		 * Game Text: Bearer must be Legolas.
		 * Each time Legolas wins a skirmish, you may wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Bow of the Galadhrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedOnAnotherElfCompanionAndIsPlayedOnLegolasOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(bow, legolas);

		scn.StartGame();

		// Arwen is an [elven] Elf companion and Aragorn a companion: neither is Legolas.
		assertFalse(scn.FreepsPlayAvailable(bow));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(legolas);
		assertTrue(scn.FreepsPlayAvailable(bow));

		// Legolas is the one bearer it may go on, so it goes on him unasked.
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, legolas);
		assertNotAttachedTo(bow, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedOnLegolasWhoIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveAlliesToTable(legolas);
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, legolas);
		assertNotAttachedTo(bow, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeLegolasWinsASkirmishHeMayWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(legolas, bow);

		var pursuer = scn.GetShadowCard("pursuer");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, troop);
		scn.SkipToAssignments();
		// The Troop is assigned to nobody: a minion outside Legolas's skirmish.
		scn.FreepsAssignToMinions(legolas, pursuer);
		scn.ShadowDeclineAssignments();

		// Legolas 6 + 1 for the bow against the Pursuer's 5: he wins, and the Pursuer takes the wound for losing.
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(legolas));

		// The fierce round: Legolas beats the Pursuer a second time, and is offered the wound again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(legolas, pursuer);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));

		// Declined, nothing more is wounded.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAnotherCompanionWinsOrWhenLegolasLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(legolas, bow);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{legolas, troop});

		// Aragorn 8 against the Savage's 5: another companion wins, with the Troop still there to be wounded.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Legolas 7 against the Troop's 13: he loses (not overwhelmed, 13 is short of 14), with the Savage there to be wounded.
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheMinionLegolasBeatIsStillThereToWoundAsTheBowIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, bow);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, runner);

		// Legolas 7 against the Runner's 5: he wins.  What answers the win comes before the loser loses (engine
		// pass 54; until then the Runner, vitality 1, was dead of its wound by now and the Bow was not offered).
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));

		// The Runner is the one minion to wound, and the wound kills it.  Its side gone, there is no wound for
		// losing to place (step 5A).
		scn.FreepsChooseOptionalTrigger(bow);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
