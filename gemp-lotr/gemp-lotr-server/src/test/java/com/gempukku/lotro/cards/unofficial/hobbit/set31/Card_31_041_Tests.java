package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_041_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 (strength 3, vitality 4) as the
	// Ring-bearer; his skirmish ability, played on a [dwarven] skirmish event from hand, is not used. The
	// skirmishes of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("contract", "31_41");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, 7/4; damage +1, a fellowship ability
					put("thorin", "30_19");
					// Fili, Brother of Kili: [dwarven] Dwarf companion, 6/3; a response to a [dwarven] companion
					// about to take a wound, never reached
					put("fili", "30_10");
					// Gandalf, Leader of Dwarves: [gandalf] Wizard companion, 7/4; skirmish abilities that need an
					// event in hand (none), and a reconcile trigger never reached
					put("gandalf", "30_25");
					// Master Worrywort, Hobbit Farmer: a [shire] Hobbit ally, 2/3, who is no Bilbo; his texts answer
					// Bilbo winning and a skirmish event played
					put("worrywort", "33_53");

					// Goblin Runner: [moria] Orc, 5/1; a text on being played, cheated in so it does not fire
					put("runner", "31_30");
					// Dark Bats: [moria] Creature, 4/2; a Shadow ability not used
					put("bats", "32_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BurglarsContractStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Burglar's Contract
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Resistance: +1
		 * Game Text: Bearer must be Bilbo.
		 * 	Skirmish: Add a doubt and exert Bilbo to make another companion strength +2 (limit +4).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("contract");

		assertEquals("Burglar's Contract", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysOnlyOnBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contract = scn.GetFreepsCard("contract");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var worrywort = scn.GetFreepsCard("worrywort");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(worrywort);

		scn.StartGame();
		scn.MoveCardsToHand(contract);
		scn.FreepsRefreshDecision();
		assertInHand(contract);

		assertTrue(scn.FreepsPlayAvailable(contract));
		scn.FreepsPlayCard(contract);

		// Neither the Dwarf companion nor the Hobbit ally may bear it: Bilbo is the one bearer, and nothing is asked.
		assertAttachedTo(contract, bilbo);
		assertTrue(scn.GetAttachedCards(thorin).isEmpty());
		assertTrue(scn.GetAttachedCards(worrywort).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionAddsADoubtAndExertsBilboToMakeAnotherCompanionStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contract = scn.GetFreepsCard("contract");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var worrywort = scn.GetFreepsCard("worrywort");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(thorin, gandalf);
		scn.MoveAlliesToTable(worrywort);
		scn.AttachCardsTo(bilbo, contract);

		scn.StartGame();
		assertAttachedTo(contract, bilbo);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		// Gandalf and Bilbo are left unassigned.
		scn.FreepsAssignToMinions(thorin, runner);
		scn.FreepsResolveSkirmish(thorin);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(worrywort));
		assertEquals(7, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.FreepsActionAvailable(contract));
		scn.FreepsUseCardAction(contract);

		// Companions of two cultures and races, in the skirmish and out of any; not Bilbo himself, the Hobbit ally,
		// nor the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, worrywort, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(thorin);

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(worrywort));
		assertEquals(9, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheLimitOfPlus4IsTheCardsAcrossCompanionsAndAThirdUseIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contract = scn.GetFreepsCard("contract");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(thorin, gandalf);
		scn.AttachCardsTo(bilbo, contract);

		scn.StartGame();
		assertAttachedTo(contract, bilbo);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, runner);
		scn.FreepsResolveSkirmish(thorin);

		scn.FreepsUseCardAction(contract);
		scn.FreepsChooseCard(thorin);
		scn.ShadowPass();
		scn.FreepsUseCardAction(contract);
		scn.FreepsChooseCard(gandalf);
		scn.ShadowPass();
		// +2 to each of two companions: the card's +4 for this skirmish phase is given.
		assertEquals(9, scn.GetStrength(thorin));
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(bilbo));

		// A third use is still offered, its doubt and exertion paid, and Thorin, who has had only +2, gets nothing.
		assertTrue(scn.FreepsActionAvailable(contract));
		scn.FreepsUseCardAction(contract);
		scn.FreepsChooseCard(thorin);

		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(9, scn.GetStrength(thorin));
		assertEquals(9, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsItsSkirmishAndTheLimitStartsAgainInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contract = scn.GetFreepsCard("contract");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var fili = scn.GetFreepsCard("fili");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin, fili);
		scn.AttachCardsTo(bilbo, contract);

		scn.StartGame();
		assertAttachedTo(contract, bilbo);
		scn.MoveMinionsToTable(runner);
		scn.MoveMinionsToTable(bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, runner}, new PhysicalCardImpl[]{fili, bats});
		scn.FreepsResolveSkirmish(thorin);

		// The whole +4 on one companion.
		scn.FreepsUseCardAction(contract);
		scn.FreepsChooseCard(thorin);
		scn.ShadowPass();
		scn.FreepsUseCardAction(contract);
		scn.FreepsChooseCard(thorin);
		assertEquals(11, scn.GetStrength(thorin));
		assertEquals(6, scn.GetStrength(fili));
		// Thorin wins his skirmish at 11.
		scn.PassSkirmishActions();

		// Fili's skirmish is a phase of its own: Thorin is back to 7, and the limit gives +2 again.
		scn.FreepsResolveSkirmish(fili);
		assertEquals(7, scn.GetStrength(thorin));
		assertEquals(6, scn.GetStrength(fili));
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsActionAvailable(contract));
		scn.FreepsUseCardAction(contract);
		scn.FreepsChooseCard(fili);

		assertEquals(8, scn.GetStrength(fili));
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileBilboIsExhaustedThoughAnotherHobbitCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var contract = scn.GetFreepsCard("contract");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var worrywort = scn.GetFreepsCard("worrywort");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(worrywort);
		scn.AttachCardsTo(bilbo, contract);
		// Bilbo, vitality 4, is exhausted.
		scn.AddWoundsToChar(bilbo, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(worrywort));
		assertAttachedTo(contract, bilbo);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, runner);
		scn.FreepsResolveSkirmish(thorin);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(contract));
		assertEquals(0, scn.GetWoundsOn(worrywort));
		assertEquals(0, scn.GetBurdens());
	}
}
