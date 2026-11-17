package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "102_16");
					put("guard", "5_83");       // Household Guard: [rohan] Man, strength 6, vitality 3; "Valiant." is its whole text
					put("haldir", "102_8");     // Haldir, Naith Commander: a valiant [elven] Elf, strength 6; his text needs a ranged weapon or an [elven] possession
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man, strength 5, not valiant; his ability needs a mount
					// Bounder: a [shire] Hobbit ally whose home is site 2, where the skirmishes here are fought.
					// Strength 2, vitality 2; his own skirmish ability is never used.
					put("bounder", "1_286");
					// Royal Guard: a [rohan] ally.  "Skirmish: Exert this ally to add his strength to a
					// skirmishing Hobbit and make that Hobbit gain valiant until the regroup phase (limit once
					// per phase)."  Strength 3, vitality 3.  How an ally comes to be valiant: none is in print.
					put("royalguard", "102_54");

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Aragorn, Last Hope of Men
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: Valiant.
		 * 	At the start of each turn you may spot another valiant companion to draw a card.
		 * 	Skirmish: Exert Aragorn to make a skirmishing valiant companion strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Last Hope of Men", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void AtTheStartOfTheTurnMaySpotAnotherValiantCompanionToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDrawMayBeDeclinedAndAValiantElfIsSpottedAsWellAsAValiantMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(aragorn, haldir);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedTheDrawWithNoOtherValiantCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, rider);

		scn.StartGame();

		// Aragorn is valiant and is not "another"; the Rider is another companion and is not valiant.
		assertTrue(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsOfferedTheDrawAtTheStartOfEachOfItsOwnersTurnsAndNotOfTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// On the opponent's turn Aragorn is not active, and nothing is asked of his owner.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// His owner's next turn: the offer is made before the fellowship phase begins.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));

		// Reconciling has drawn the small deck away, so a card is put back for the draw to take.
		scn.MoveCardsToTopOfDeck(haldir);
		int hand = scn.GetFreepsHandCount();
		assertEquals(1, scn.GetFreepsDeckCount());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionExertsAragornToMakeTheSkirmishingValiantCompanionStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(guard, haldir);

		var savage = scn.GetShadowCard("savage");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		// Aragorn goes down after the start of the turn, so that his draw is not in the way.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage, runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, savage },
				new PhysicalCardImpl[] { aragorn, runner1 },
				new PhysicalCardImpl[] { haldir, runner2 }
		);

		// The Household Guard's skirmish. Aragorn and Haldir are valiant and assigned, and not yet skirmishing.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(haldir));

		// The Guard is the one skirmishing valiant companion, so he is taken without asking.
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still +2 at the last decision of the skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(guard));

		// 8 beats the Savage's 5 without doubling it: one wound. The +2 ends with the skirmish.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(6, scn.GetStrength(guard));

		// Aragorn's own skirmish: the text does not say "another", and he is valiant.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(haldir));
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(aragorn));

		// Haldir's skirmish: a valiant Elf of another culture.
		scn.FreepsResolveSkirmish(haldir);
		scn.FreepsUseCardAction(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));

		// Vitality 4 with 3 wounds: exhausted, so he can no longer exert.
		scn.ShadowPass();
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionStrengthensNoOneWhenTheOneSkirmishingIsAValiantAllyOrACompanionWhoIsNotValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var bounder = scn.GetFreepsCard("bounder");
		var royalguard = scn.GetFreepsCard("royalguard");
		scn.MoveCompanionsToTable(guard, rider);
		scn.MoveAlliesToTable(bounder, royalguard);

		var savage = scn.GetShadowCard("savage");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		// Aragorn goes down after the start of the turn, so that his draw is not in the way.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage, runner1, runner2);

		// Site 2 is the Bounder's home, so he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { bounder, runner2 },
				new PhysicalCardImpl[] { rider, savage },
				new PhysicalCardImpl[] { guard, runner1 }
		);

		// Only companions are printed valiant.  The Royal Guard makes the skirmishing Bounder valiant:
		// someone in the skirmish who is valiant and is no companion.
		scn.FreepsResolveSkirmish(bounder);
		assertFalse(scn.HasKeyword(bounder, Keyword.VALIANT));
		assertEquals(2, scn.GetStrength(bounder));
		scn.FreepsUseCardAction(royalguard);
		assertEquals(1, scn.GetWoundsOn(royalguard));
		assertTrue(scn.HasKeyword(bounder, Keyword.VALIANT));
		assertEquals(2 + 3, scn.GetStrength(bounder));
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The cost is paid and the effect finds no one: not the valiant ally who is skirmishing, nor the
		// valiant companions who are not.
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(5, scn.GetStrength(bounder));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Bounder's 5 does not beat the Runner's 5: one wound of his two.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bounder));

		// The Rider of Rohan is skirmishing and is not valiant. The Guard is valiant and his skirmish has
		// not begun; Aragorn is valiant and in no skirmish; the Bounder is still valiant and his is over.
		scn.FreepsResolveSkirmish(rider);
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(bounder, Keyword.VALIANT));
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));

		// The cost is paid and the effect finds no one.
		scn.FreepsUseCardAction(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
