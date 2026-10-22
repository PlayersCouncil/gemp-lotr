package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("airs", "10_17");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: Wizard companion, vitality 4
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ally, vitality 4; a fellowship ability
					put("ent", "6_27");         // Ent Avenger: [gandalf] companion who is no Wizard, vitality 4

					put("saruman", "3_69");     // Saruman, Servant of the Eye: [isengard] Wizard minion, vitality 4; may not be assigned
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man with no text
					put("wreathed", "1_238");   // Wreathed in Shadow: [wraith] condition, support area; an archery ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OutoftheHighAirsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Out of the High Airs
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the fellowship moves during the regroup phase, exert your Wizard to discard each minion.
		*/

		var scn = GetScenario();

		var airs = scn.GetFreepsCard("airs");

		assertEquals("Out of the High Airs", airs.getBlueprint().getTitle());
		assertNull(airs.getBlueprint().getSubtitle());
		assertFalse(airs.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, airs.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, airs.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, airs.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(airs, Timeword.RESPONSE));
		assertEquals(3, airs.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARegroupMoveLetsYourWizardExertToDiscardEachMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var airs = scn.GetFreepsCard("airs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var ent = scn.GetFreepsCard("ent");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable(gandalf, ent);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(airs);
		scn.MoveCardsToSupportArea(wreathed);
		assertInHand(airs);
		assertInZone(Zone.SUPPORT, wreathed);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// The move of the fellowship phase does not offer it, though a minion is on the table.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(airs));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		scn.MoveMinionsToTable(saruman, savage, brigand);
		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, saruman, troop, savage, brigand);
		assertInZone(Zone.SUPPORT, wreathed);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(ent));
		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(airs));
		scn.FreepsChooseOptionalTrigger(airs);

		// A Wizard companion and a Wizard ally; not the [gandalf] companion, not the Shadow player's Wizard.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ent, saruman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(ent));
		// Minions of three cultures and four races, the Wizard Saruman among them; the Shadow condition stays.
		assertInDiscard(saruman, troop, savage, brigand);
		assertInZone(Zone.SUPPORT, wreathed);
		assertInDiscard(airs);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMinionTheEventIsStillPlayedAndTheWizardExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var airs = scn.GetFreepsCard("airs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(airs);
		scn.MoveCardsToSupportArea(wreathed);
		assertInHand(airs);
		assertInZone(Zone.SUPPORT, wreathed);

		scn.StartGame();
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsChooseToMove();

		// An event is played whatever its effect will do. Gandalf, the one Wizard, exerts unasked.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(airs));
		scn.FreepsChooseOptionalTrigger(airs);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SUPPORT, wreathed);
		assertInDiscard(airs);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenYourWizardCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var airs = scn.GetFreepsCard("airs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, ent);
		// Vitality 4 with 3 wounds: Gandalf cannot exert. The Ent Avenger and the Shadow player's Saruman can.
		scn.AddWoundsToChar(gandalf, 3);
		scn.MoveCardsToHand(airs);
		assertInHand(airs);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(ent));
		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(airs));
		assertInHand(airs);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
