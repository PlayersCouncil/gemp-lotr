package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_009_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "101_9");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					// Aragorn's Bow: Aragorn is an archer.  Archery: exert Aragorn to wound a minion, and he
					// then does not add to the fellowship archery total.
					put("gornbow", "1_90");
					// Arwen, Daughter of Elrond: Elf, strength 6, vitality 3.  Her +3 is only against a Nazgul.
					put("arwen", "1_30");
					put("bow", "1_41");         // Elven Bow: bearer is an archer, and nothing else
					// Foes of Mordor: Maneuver: exert a [gondor] character to wound a [sauron] minion.
					put("foes", "1_105");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard], strength 5, vitality 3, damage +1
					put("marksman", "1_176");   // Goblin Marksman: [moria] archer, strength 7, vitality 1
					// Orc Soldier: [sauron], strength 7, vitality 2.  Its skirmish ability is never used here.
					put("soldier", "1_271");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Legolas, Keen-eyed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: Archer.
		 * Each time a minion takes a wound during the archery phase, make Legolas strength +1 until the regroup
		 * phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Keen-eyed", card.getBlueprint().getSubtitle());
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
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void EachWoundAMinionTakesInTheArcheryPhaseMakesLegolasStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gornbow = scn.GetFreepsCard("gornbow");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(legolas, aragorn, arwen);
		scn.AttachCardsTo(aragorn, gornbow);
		scn.AttachCardsTo(arwen, bow);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, savage, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(6, scn.GetStrength(legolas));

		// A wound from an archery special ability, on an [isengard] minion.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsUseCardAction(gornbow);
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(7, scn.GetStrength(legolas));

		scn.ShadowPass();
		scn.FreepsPass();

		// Archery fire.  The Goblin Marksman's arrow goes to a companion, which is not a minion taking a wound.
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(7, scn.GetStrength(legolas));

		// Fellowship archery total 2: Legolas and Arwen with her bow.  Aragorn used his bow's ability instead.
		// The first arrow goes to the Uruk Savage again: a second wound on the same minion in the same phase
		// counts as much as the first.  The second arrow kills the [moria] Marksman.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		scn.ShadowChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(8, scn.GetStrength(legolas));

		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		scn.ShadowChooseCard(marksman);
		// The wound sets off both the Marksman's death and Legolas's text, and their owner orders the two.
		// The death goes first here, so the minion has left play by the time Legolas's text is carried out.
		assertEquals(8, scn.GetStrength(legolas));
		scn.FreepsResolveRuleFirst();
		assertInDiscard(marksman);
		assertEquals(9, scn.GetStrength(legolas));
		// Three wounds on two minions: +3.  The Goblin Patrol Troop took none.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WoundAMinionTakesInTheManeuverPhaseDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var foes = scn.GetFreepsCard("foes");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToSupportArea(foes);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(6, scn.GetStrength(legolas));

		// Foes of Mordor exerts Aragorn, the only [gondor] character, to wound the only [sauron] minion.
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.FreepsUseCardAction(foes);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(6, scn.GetStrength(legolas));

		// On the same board, Legolas's arrow in the archery phase that follows does count.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(6, scn.GetStrength(legolas));
		scn.PassArcheryActions();
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(7, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void BonusLastsThroughTheSkirmishesAndEndsWhenTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(6, scn.GetStrength(legolas));
		scn.PassArcheryActions();
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(7, scn.GetStrength(legolas));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, savage}, new PhysicalCardImpl[]{aragorn, troop});

		// Legolas, 7 against 5, wins: the Uruk Savage takes a wound, but in a skirmish phase.
		scn.FreepsResolveSkirmish(legolas);
		assertEquals(7, scn.GetStrength(legolas));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// Aragorn's skirmish is the last thing before the regroup phase, and Legolas is still at 7, not 8.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(legolas));

		// Aragorn, 8 against 13, loses and takes a wound.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(legolas));
	}
}
