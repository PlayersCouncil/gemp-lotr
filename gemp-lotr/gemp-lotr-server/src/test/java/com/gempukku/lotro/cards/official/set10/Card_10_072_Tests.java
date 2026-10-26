package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_10_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "10_72");

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("patrol", "1_177");         // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("raiders", "1_158");        // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; damage +1 and nothing else
					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("fear", "1_278");           // Strength Born of Fear: a Shadow card that is no minion (a support-area condition; its ability costs (3))
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Éowyn, Lady of Ithilien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Skirmish:</b> Exert Éowyn to choose an opponent. That opponent must wound a minion for each
		 * wound on each minion skirmishing Éowyn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", card.getBlueprint().getTitle());
		assertEquals("Lady of Ithilien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void ExertsEowynToMakeTheShadowPlayerWoundAMinionOnceForOneWoundUntilSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eowyn, aragorn);

		var patrol = scn.GetShadowCard("patrol");
		var raiders = scn.GetShadowCard("raiders");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, raiders);
		scn.MoveCardsToSupportArea(fear);
		scn.AddWoundsToChar(eowyn, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(patrol, 1);
		// The Raiding Party fights nobody: its wound counts for nothing.
		scn.AddWoundsToChar(raiders, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, patrol);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsActionAvailable(eowyn));

		scn.FreepsUseCardAction(eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));

		// The opponent is the one who chooses: any minion, fighting Éowyn or not, and nothing that is no minion.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fear, eowyn, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		// The minion in no skirmish is the one wounded.
		scn.ShadowChooseCard(raiders);
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// One wound was on the Troop when Éowyn exerted, so that was the one choice.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Vitality 3 with 2 wounds: exhausted, with a wounded minion still in front of her.
		scn.ShadowPass();
		assertTrue(scn.IsCharSkirmishingAgainst(eowyn, patrol));
		assertFalse(scn.FreepsActionAvailable(eowyn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ThreeWoundsOnTwoMinionsSkirmishingEowynAreThreeWoundsEachOnAMinionTheShadowPlayerChooses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);

		var patrol = scn.GetShadowCard("patrol");
		var raiders = scn.GetShadowCard("raiders");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, raiders, savage);
		scn.AddWoundsToChar(patrol, 1);
		scn.AddWoundsToChar(raiders, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, patrol);
		// The Shadow player sends the Raiding Party against her as well and keeps the Savage back.
		scn.ShadowAssignToMinions(eowyn, raiders);
		scn.FreepsResolveSkirmish(eowyn);

		assertTrue(scn.IsCharSkirmishingAgainst(eowyn, patrol));
		assertTrue(scn.IsCharSkirmishingAgainst(eowyn, raiders));
		assertFalse(scn.IsCharSkirmishing(savage));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsUseCardAction(eowyn);
		assertEquals(1, scn.GetWoundsOn(eowyn));

		// 1 wound on the Troop + 2 on the Raiding Party: three wounds.  First, the Troop, which is skirmishing
		// her: the wound it takes brings no fourth.
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, raiders, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(patrol);
		assertEquals(2, scn.GetWoundsOn(patrol));

		// Second: the Savage, in no skirmish.
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, raiders, savage));
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// Third: the same minion may be wounded again (CR 4.2 "for each").
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, raiders, savage));
		scn.ShadowChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));

		// And no fourth.
		assertEquals(2, scn.GetWoundsOn(patrol));
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WoundsOnMinionsThatAreNotSkirmishingEowynBringNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eowyn, aragorn);

		var patrol = scn.GetShadowCard("patrol");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, raiders);
		scn.AddWoundsToChar(patrol, 2);
		scn.AddWoundsToChar(raiders, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, patrol}, new PhysicalCardImpl[]{eowyn, raiders});
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's skirmish.  The Troop he is fighting has 2 wounds and the Raiding Party assigned to Éowyn
		// has 1, but no minion is skirmishing her yet.  She may still exert, for nothing.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, patrol));
		assertTrue(scn.IsCharAssignedAgainst(eowyn, raiders));
		assertFalse(scn.IsCharSkirmishing(eowyn));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsActionAvailable(eowyn));

		scn.FreepsUseCardAction(eowyn);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetWoundsOn(patrol));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Troop's 13 beats Aragorn's 8.  Then Éowyn's own skirmish: the Raiding Party's 1 wound is the
		// only one that counts, whatever is on the Troop, on Aragorn and on her.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.IsCharSkirmishingAgainst(eowyn, raiders));

		scn.FreepsUseCardAction(eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, raiders));

		// A wound and no exertion: the Troop, vitality 3 with 2 wounds, may be chosen for it and dies of it.
		scn.ShadowChooseCard(patrol);
		assertInZone(Zone.DISCARD, patrol);
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
