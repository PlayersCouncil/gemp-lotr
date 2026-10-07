package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_050_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "51_50");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally; a maneuver ability used only on purpose
					put("bow", "1_41");         // Elven Bow: ranged weapon, bearer must be an Elf; bearer is an archer
					put("galadhrim", "1_33");   // Bow of the Galadhrim: ranged weapon, bearer must be Legolas; answers a skirmish he wins
					put("knives", "3_21");      // Long-knives of Legolas: hand weapon, bearer must be Legolas

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Legolas, Greenleaf
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Archer.
		 * Archery: Exert Legolas to wound a minion. Exert him again unless he bears a ranged weapon or you can
		 * spot more minions than companions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Greenleaf", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void IsPlayedFromHandForTwoAsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(legolas);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(legolas));

		scn.FreepsPlayCard(legolas);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryActionExertsLegolasToWoundAMinionAndExertsHimAgainWithAsManyMinionsAsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		// Two minions against two companions, Frodo and Legolas: not more.
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		// The cost
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		// Exerted again, without being asked
		assertEquals(2, scn.GetWoundsOn(legolas));
		// He still counts toward the fellowship archery total.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// Vitality 3 with 2 wounds: exhausted, so he can no longer exert.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
	}

	@Test
	public void BearingARangedWeaponLegolasIsNotExertedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var galadhrim = scn.GetFreepsCard("galadhrim");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, bow);

		scn.StartGame();
		// Two minions against two companions: only the bow spares him.
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// The same again with another ranged weapon, from no wounds.
		scn.MoveCardsToDiscard(bow);
		scn.AttachCardsTo(legolas, galadhrim);
		scn.RemoveWoundsFromChar(legolas, 1);
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// A third use: the cost alone takes him from 1 wound to 2, and then he cannot pay it.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(legolas));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
	}

	@Test
	public void AnotherCompanionsRangedWeaponAndLegolasOwnHandWeaponDoNotSpareHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		var knives = scn.GetFreepsCard("knives");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(arwen, bow);
		scn.AttachCardsTo(legolas, knives);

		scn.StartGame();
		// Three minions against three companions.
		scn.MoveMinionsToTable(troop, savage, raiders);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage, raiders));
		scn.FreepsChooseCard(raiders);
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithMoreMinionsThanCompanionsLegolasIsNotExertedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var thrarin = scn.GetFreepsCard("thrarin");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(legolas);
		// An ally is no companion: with him counted it would be three against three.
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		// Three minions against two companions.
		scn.MoveMinionsToTable(troop, savage, raiders);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(savage);
		// Vitality 3: the Savage is wounded and still there, so the minions are still three.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AMinionKilledByTheWoundIsNoLongerCountedWhenTheSecondExertionIsDecided() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		// Three minions against two companions, until the wound kills the Runner (vitality 1).
		scn.MoveMinionsToTable(troop, savage, runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		// Two minions are left against two companions: no longer more, so he is exerted again.
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithOneWoundTheActionIsStillOfferedAndTheSecondExertionCannotBeMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);
		scn.AddWoundsToChar(legolas, 1);

		scn.StartGame();
		// Two minions against two companions and no ranged weapon: nothing spares him the second exertion.
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		// The second exertion is no part of the price: one exertion is all he needs to be able to make.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		// Exhausted, he cannot exert again, and an exertion does not kill.
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
