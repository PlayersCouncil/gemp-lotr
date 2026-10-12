package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_157_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armory1", "51_157");
					put("armory2", "51_157");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("assassin", "1_131");   // Orthanc Assassin: [isengard] Uruk-hai, cost 5, site 5; Archer and Damage +1 are its whole text
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a regroup ability only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2; Damage +1 is its whole text
					put("orcsoldier", "1_271"); // Orc Soldier: [sauron] Orc, a bearer for the Scimitar; a skirmish ability used only on purpose

					put("sword", "1_160");      // Uruk-hai Sword: [isengard] possession, cost 1, bearer must be an Uruk-hai
					put("spear", "4_196");      // Uruk Spear: [isengard] possession, cost 1, bearer must be an Uruk-hai
					put("broadsword", "4_142"); // Broad-bladed Sword: [isengard] possession, cost 1, bearer must be an Uruk-hai
					put("rampage", "1_159");    // Uruk-hai Rampage: [isengard] condition, cost 0, to play spot an Uruk-hai
					put("scimitar", "1_269");   // Orc Scimitar: [sauron] possession, cost 1, bearer must be a [sauron] Orc

					put("legolas", "1_50");     // Legolas, Greenleaf: archer companion; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, strength 6, Damage +1; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiArmoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk-hai Armory
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot an Uruk-hai, the fellowship archery total is -1.
		 * <b>Shadow:</b> Discard this condition to play an [Isengard] possession from your discard pile.
		 * <b>Response:</b> If your [Isengard] minion is about to take a wound, remove (1) and discard this condition to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armory1");

		assertEquals("Uruk-hai Armory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipArcheryTotalIsMinusOneOnceAnUrukHaiIsPlayedAndNotForAnIsengardOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory1");
		var servant = scn.GetShadowCard("servant");
		var assassin = scn.GetShadowCard("assassin");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		// A second archer, so that -1 is told from a total set to 0.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aragorn, null, Keyword.ARCHER));
		scn.MoveCardsToHand(assassin);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// An [isengard] Orc is not an Uruk-hai.
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.ShadowPlayCard(assassin);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		// The Assassin's own archery, untouched.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnyCultureCountsAndASecondDoesNotCountAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory1");
		var sentry = scn.GetShadowCard("sentry");
		var assassin = scn.GetShadowCard("assassin");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aragorn, null, Keyword.ARCHER));
		scn.MoveCardsToHand(assassin);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(sentry);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [sauron] Uruk-hai is an Uruk-hai.
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.ShadowPlayCard(assassin);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowDiscardsThisConditionToPlayAnIsengardPossessionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory1 = scn.GetShadowCard("armory1");
		var armory2 = scn.GetShadowCard("armory2");
		var savage = scn.GetShadowCard("savage");
		var orcsoldier = scn.GetShadowCard("orcsoldier");
		var sword = scn.GetShadowCard("sword");
		var spear = scn.GetShadowCard("spear");
		var rampage = scn.GetShadowCard("rampage");
		var scimitar = scn.GetShadowCard("scimitar");
		var broadsword = scn.GetShadowCard("broadsword");
		var fpBroadsword = scn.GetFreepsCard("broadsword");

		scn.StartGame();
		scn.MoveCardsToSupportArea(armory1, armory2);
		scn.MoveMinionsToTable(savage, orcsoldier);
		scn.MoveCardsToDiscard(sword, spear, rampage, scimitar);
		scn.MoveCardsToHand(broadsword);
		scn.MoveCardsToDiscard(fpBroadsword);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(armory1));
		scn.ShadowUseCardAction(armory1);

		assertInZone(Zone.DISCARD, armory1);
		assertInZone(Zone.SUPPORT, armory2);
		// Both [isengard] possessions in the discard pile; not the [isengard] condition, the [sauron] possession,
		// the [isengard] possession in hand, or the one in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, spear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rampage, scimitar, broadsword, fpBroadsword));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCardBPFromSelection(spear);
		// The Spear's own cost of 1.
		assertEquals(twilight - 1, scn.GetTwilight());
		assertAttachedTo(spear, savage);
		assertInZone(Zone.DISCARD, sword, rampage, scimitar);
		assertInZone(Zone.HAND, broadsword);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoIsengardPossessionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory1");
		var savage = scn.GetShadowCard("savage");
		var orcsoldier = scn.GetShadowCard("orcsoldier");
		var rampage = scn.GetShadowCard("rampage");
		var scimitar = scn.GetShadowCard("scimitar");
		var broadsword = scn.GetShadowCard("broadsword");

		scn.StartGame();
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(savage, orcsoldier);
		scn.MoveCardsToDiscard(rampage, scimitar);
		scn.MoveCardsToHand(broadsword);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(armory));
		assertInZone(Zone.SUPPORT, armory);
	}

	@Test
	public void NotOfferedWhenThePossessionsTwilightCannotBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory1");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");

		scn.StartGame();
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToDiscard(sword);
		scn.FreepsPass();

		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(armory));

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(armory));
		assertInZone(Zone.SUPPORT, armory);
		assertInZone(Zone.DISCARD, sword);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseRemovesOneAndDiscardsThisConditionToPreventOneWoundToAnIsengardUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory1 = scn.GetShadowCard("armory1");
		var armory2 = scn.GetShadowCard("armory2");
		var savage = scn.GetShadowCard("savage");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armory1, armory2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(1);

		// Gimli, 6 with damage +1, beats the Savage, 5: two wounds, one at a time.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory2));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.ShadowChooseOptionalTrigger(armory1);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, armory1);
		assertInZone(Zone.SUPPORT, armory2);

		// The second wound: with the pool empty the other copy cannot pay, and the wound is taken.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armory2));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SUPPORT, armory2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseSavesAnIsengardOrcButNotASauronUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory1");
		var servant = scn.GetShadowCard("servant");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		// Aragorn 10 beats the Sentry, 8; the Guard 8 beats the Servant, 7. Neither companion has a damage bonus.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard, null, 4));

		scn.StartGame();
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(servant, sentry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, sentry}, new PhysicalCardImpl[]{guard, servant});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);

		// The [sauron] Uruk-hai loses: not offered, and the wound is taken.
		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armory));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertInZone(Zone.SUPPORT, armory);
		assertEquals(2, scn.GetTwilight());

		// The [isengard] Orc loses: offered.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory));
		scn.ShadowChooseOptionalTrigger(armory);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, armory);
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
