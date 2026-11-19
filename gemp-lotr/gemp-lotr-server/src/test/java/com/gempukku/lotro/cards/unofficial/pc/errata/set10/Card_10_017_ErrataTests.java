package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_10_017_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("airs", "60_17");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: Wizard companion, vitality 4
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ally, vitality 4; a fellowship ability
					put("ent", "6_27");         // Ent Avenger: [gandalf] companion who is no Wizard, vitality 4

					put("saruman", "3_69");     // Saruman, Servant of the Eye: [isengard] Wizard minion, vitality 4; may not be assigned
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion with no text
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, damage +1
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] minion with no text
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
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the fellowship moves during the regroup phase, exert your Wizard X times to hinder X minions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("airs");

		assertEquals("Out of the High Airs", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARegroupMoveLetsYourWizardExertTwiceToHinderTwoMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var airs = scn.GetFreepsCard("airs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveCompanionsToTable(gandalf, ent);
		scn.MoveCardsToHand(airs);
		scn.MoveCardsToSupportArea(wreathed);

		scn.StartGame();

		// The move of the fellowship phase does not offer it.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(saruman, troop, savage, brigand);
		scn.SkipToMovementDecision();
		// Hindered already (after the Shadow player's regroup reconcile, which would restore it): no longer a
		// minion to hinder.
		scn.HinderCard(brigand);
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(airs));
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// Gandalf is the one Wizard of the Free Peoples player's, so he is not asked which: not the Ent Avenger,
		// a [gandalf] companion, and not the Shadow player's Saruman.
		scn.FreepsChooseOptionalTrigger(airs);
		// Vitality 4: he can exert once, twice or three times, whatever the number of minions.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(ent));
		assertEquals(0, scn.GetWoundsOn(saruman));

		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brigand, wreathed, gandalf, ent));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertFalse(scn.IsHindered(saruman));
		assertFalse(scn.IsHindered(troop));

		scn.FreepsChooseCards(saruman, troop);
		assertTrue(scn.IsHindered(saruman));
		assertTrue(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(savage));
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(gandalf));
		assertInDiscard(airs);
		// 3 for the event; then the move to Rivendell Valley: 0 for its Shadow number + 1 for each of 3 companions
		assertEquals(twilight + 3 + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EitherOfYourWizardsMayBeChosenAndOneExertionHindersOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var airs = scn.GetFreepsCard("airs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var ent = scn.GetFreepsCard("ent");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, ent);
		scn.MoveAlliesToTable(radagast);
		scn.AddWoundsToChar(radagast, 1);
		scn.MoveCardsToHand(airs);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(airs));
		scn.FreepsChooseOptionalTrigger(airs);
		// A Wizard companion and a Wizard ally; not the [gandalf] companion, not the Shadow player's Wizard.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ent, saruman));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(radagast);
		// Vitality 4 with 1 wound: once or twice.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		assertEquals(2, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(troop);
		assertTrue(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(saruman));
		assertInDiscard(airs);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void XIsNotHeldToTheNumberOfMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var airs = scn.GetFreepsCard("airs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(airs);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(airs));
		scn.FreepsChooseOptionalTrigger(airs);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		// Three exertions with two minions on the table: both are hindered.
		scn.FreepsChoose("3");
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.IsHindered(savage));
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

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// An event is played whatever its effect will do.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(airs));
		int twilight = scn.GetTwilight();
		scn.FreepsChooseOptionalTrigger(airs);
		// Vitality 4: up to 3 exertions, though there is no minion for them to buy.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(gandalf));
		assertInDiscard(airs);
		// 3 for the event; then the move to Rivendell Valley: 0 for its Shadow number + 1 for each of 2 companions
		assertEquals(twilight + 3 + 2, scn.GetTwilight());
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

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(ent));
		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.HAND, airs);
		assertFalse(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
