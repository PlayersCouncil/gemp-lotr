package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_10_072_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "60_72");

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4
					put("patrol", "1_177");         // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("raiders", "1_158");        // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; damage +1 and nothing else
					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("denizen", "13_119");       // Underdeeps Denizen: [orc] Orc, vitality 1; "While this minion is at an underground site, it cannot take wounds."
					put("fear", "1_278");           // Strength Born of Fear: a Shadow card that is no minion (a support-area condition; its ability costs (3))
				}},
				// Site 4, Balin's Tomb, is underground.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Eowyn, Lady of Ithilien
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
		 * Game Text: Skirmish: Exert Eowyn to make an opponent wound or hinder a minion for each wound on each
		 * minion skirmishing Eowyn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Eowyn", card.getBlueprint().getTitle());
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
	public void PlayedFromHandForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(eowyn);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(eowyn));

		scn.FreepsPlayCard(eowyn);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eowyn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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
		// The Raiding Party fights nobody: its 2 wounds count for nothing.
		scn.AddWoundsToChar(raiders, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, patrol);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsActionAvailable(eowyn));

		scn.FreepsUseCardAction(eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));

		// The opponent is the one who chooses: any minion, fighting Eowyn or not, and nothing that is no minion.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fear, eowyn, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(patrol);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowChoiceAvailable("Wound"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));

		scn.ShadowChooseOption("Wound");
		assertEquals(2, scn.GetWoundsOn(patrol));
		assertFalse(scn.IsHindered(patrol));
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertFalse(scn.IsHindered(raiders));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// One wound was on the Troop when Eowyn exerted, so that was the one choice: the wound just added
		// does not bring another.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Vitality 3 with 2 wounds: exhausted, with a wounded minion still in front of her.
		scn.ShadowPass();
		assertTrue(scn.IsCharSkirmishingAgainst(eowyn, patrol));
		assertFalse(scn.FreepsActionAvailable(eowyn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayHinderAMinionThatIsInNoSkirmishInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);

		var patrol = scn.GetShadowCard("patrol");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, raiders);
		scn.AddWoundsToChar(patrol, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, patrol);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertFalse(scn.IsHindered(raiders));
		assertFalse(scn.IsHindered(patrol));

		scn.FreepsUseCardAction(eowyn);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, raiders));

		scn.ShadowChooseCard(raiders);
		scn.ShadowChooseOption("Hinder");
		assertTrue(scn.IsHindered(raiders));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertFalse(scn.IsHindered(patrol));
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThreeWoundsOnTwoMinionsSkirmishingEowynAreThreeChoicesEachOfAMinionAndOfWoundOrHinder() throws DecisionResultInvalidException, CardNotFoundException {
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

		scn.FreepsUseCardAction(eowyn);
		assertEquals(1, scn.GetWoundsOn(eowyn));

		// 1 wound on the Troop + 2 on the Raiding Party: three choices.  First, the Raiding Party is hindered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, raiders, savage));
		scn.ShadowChooseCard(raiders);
		scn.ShadowChooseOption("Hinder");
		assertTrue(scn.IsHindered(raiders));

		// Second: a hindered minion is no longer a minion to choose.  The Savage is wounded.
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);
		scn.ShadowChooseOption("Wound");
		assertEquals(1, scn.GetWoundsOn(savage));

		// Third: the same minion may be wounded again.
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, savage));
		scn.ShadowChooseCard(savage);
		scn.ShadowChooseOption("Wound");
		assertEquals(2, scn.GetWoundsOn(savage));

		// And no fourth.
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertFalse(scn.IsHindered(patrol));
		assertFalse(scn.IsHindered(savage));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WoundsOnMinionsThatAreNotSkirmishingEowynBringNoChoice() throws DecisionResultInvalidException, CardNotFoundException {
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

		// Aragorn's skirmish.  The Troop he is fighting has 2 wounds and the Raiding Party assigned to Eowyn
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
		assertFalse(scn.IsHindered(patrol));
		assertFalse(scn.IsHindered(raiders));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Troop's 13 beats Aragorn's 8.  Then Eowyn's own skirmish: the Raiding Party's 1 wound is the
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
		assertTrue(scn.ShadowChoiceAvailable("Wound"));
		scn.ShadowChooseOption("Wound");
		assertInZone(Zone.DISCARD, patrol);
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertFalse(scn.IsHindered(raiders));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionThatCannotTakeWoundsIsHinderedWithoutAskingWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);

		var patrol = scn.GetShadowCard("patrol");
		var savage = scn.GetShadowCard("savage");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(patrol, savage, denizen);
		scn.AddWoundsToChar(patrol, 2);

		scn.SkipToAssignments();
		// Balin's Tomb: here the Underdeeps Denizen cannot take wounds.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(eowyn, patrol);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);

		scn.FreepsUseCardAction(eowyn);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, savage, denizen));

		// 2 wounds on the Troop: two choices.  The Denizen can only be hindered, so it is, with nothing asked:
		// the next thing asked is the second minion.
		scn.ShadowChooseCard(denizen);
		assertTrue(scn.IsHindered(denizen));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(denizen));

		// A minion that can take a wound is still a choice of the two.
		scn.ShadowChooseCard(savage);
		assertTrue(scn.ShadowChoiceAvailable("Wound"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));
		scn.ShadowChooseOption("Wound");
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.IsHindered(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
