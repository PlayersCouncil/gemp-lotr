package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_142_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "4_142");

					// Minions
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 only
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] Wizard; each companion resistance -1, a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 only

					// Free Peoples cards
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("defend", "4_115");     // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: only spotted, for Long I Fell
					put("fell", "4_97");        // Long I Fell: Skirmish: the Shadow player may make you wound a minion of your choice
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BroadbladedSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Broad-bladed Sword
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be an Uruk-hai.
		 * 	Bearer may not take wounds (except during skirmish phases).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Broad-bladed Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnAnUrukHaiOfAnyCultureAndNotOnAnotherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, guard, saruman);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);
		// Uruk-hai of [isengard] and [sauron]; not an [uruk-hai] minion who is a Wizard.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertAttachedTo(sword, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerCannotBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, party);
		scn.AttachCardsTo(savage, sword);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(sword, savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(party));

		scn.FreepsPlayCard(defend);
		// The other two minions, an Uruk-hai among them, may be wounded; the bearer may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, party));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage));
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void BearerTakesTheWoundOfASkirmishItLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, sword);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(savage));

		// Aragorn, 8, beats the Savage, 5 + 2 for the sword.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerNotInTheSkirmishTakesAWoundDuringAnothersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(fell);

		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, party);
		scn.AttachCardsTo(savage, sword);
		scn.SkipToAssignments();
		// Aragorn skirmishes the Troop; the bearer is in no skirmish at all.
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(party));

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		// The wound is placed during a skirmish phase, so the bearer is a candidate beside the other minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, party, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
