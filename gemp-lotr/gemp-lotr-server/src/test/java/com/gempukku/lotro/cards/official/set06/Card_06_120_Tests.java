package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_120_Tests
{

	// The Towers path with Saruman's Laboratory as its site 9.  The tests stand at site 8 and move there; the
	// fellowship has not survived site 9 yet, so the game goes on through its maneuver and archery phases.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability used only on purpose
					// Defend It and Hope: Maneuver event, cost 0: exert a [gondor] Man to wound a minion once (or
					// twice if that minion is an Uruk-hai)
					put("defend1", "4_115");
					put("defend2", "4_115");

					put("servant", "3_58");     // Isengard Servant: an [isengard] Orc, vitality 3; a regroup ability only
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 only
					// Uruk Crossbowman: an archer; "Archery: Exert this minion to make the Free Peoples player wound an
					// unbound companion; this minion does not add to the minion archery total."
					put("crossbowman", "4_183");

					// Cards for the Shadow player's hand.
					put("troop", "1_177");      // Goblin Patrol Troop
					put("armor", "1_8");        // Dwarven Armor
					put("savage", "1_151");     // Uruk Savage
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site9", "6_120");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same table in the Movie block's PC format, in which the game is not won until the regroup phase at site 9
	// has been played: a skirmish lost at site 9 goes on to its wounds, which the default format never reaches.
	protected VirtualTableScenario GetPCScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("servant", "3_58");     // Isengard Servant: an [isengard] Orc, vitality 3; a regroup ability only
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 only

					// Cards for the Shadow player's hand.
					put("troop", "1_177");      // Goblin Patrol Troop
					put("armor", "1_8");        // Dwarven Armor
					put("savage", "1_151");     // Uruk Savage
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site9", "6_120");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.PCMovie
		);
	}

	@Test
	public void SarumansLaboratoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Saruman's Laboratory
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 9T
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Response:</b> If your minion is about to take a wound, discard 2 cards from hand to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Saruman's Laboratory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void DiscardingTwoCardsFromHandPreventsTheWoundToAnIsengardOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");
		var armor = scn.GetShadowCard("armor");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(servant);
		scn.SkipToPhase(Phase.MANEUVER);

		var site = scn.GetCurrentSite();
		assertEquals("Saruman's Laboratory", site.getBlueprint().getTitle());
		scn.MoveCardsToHand(defend1);
		scn.SetShadowHand(troop, armor, savage);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetShadowHandCount());
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(0, scn.GetWoundsOn(servant));

		scn.FreepsPlayCard(defend1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The Servant, the only minion, is about to take the wound.  The Shadow player is offered the site, and
		// the Free Peoples player, who has no minion, is not.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(site));
		scn.ShadowChooseOptionalTrigger(site);

		// Two cards of the three in hand, chosen.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, armor);

		assertInDiscard(troop, armor);
		assertEquals(shadowDiscard + 2, scn.GetShadowDiscardCount());
		assertInHand(savage);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(servant));
		assertInDiscard(defend1);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnUrukHaiWoundedTwiceIsSavedFromTheFirstWoundAndTakesTheSecondWithOneCardLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var armor = scn.GetShadowCard("armor");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(guard);
		scn.SkipToPhase(Phase.MANEUVER);

		var site = scn.GetCurrentSite();
		assertEquals("Saruman's Laboratory", site.getBlueprint().getTitle());
		scn.MoveCardsToHand(defend1);
		scn.SetShadowHand(troop, armor, savage);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(defend1);
		// The [sauron] Uruk-hai is to be wounded twice.  The first wound is prevented for two cards.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowChooseOptionalTrigger(site);
		scn.ShadowChooseCards(troop, armor);
		assertInDiscard(troop, armor);
		assertEquals(1, scn.GetShadowHandCount());

		// The second has its own window, and two cards cannot be discarded from a hand of one: taken.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(site));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		assertInHand(savage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NeitherPlayerIsOfferedTheSiteForACompanionAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var crossbowman = scn.GetShadowCard("crossbowman");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");
		var armor = scn.GetShadowCard("armor");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(crossbowman, servant);
		scn.SkipToPhase(Phase.ARCHERY);

		var site = scn.GetCurrentSite();
		assertEquals("Saruman's Laboratory", site.getBlueprint().getTitle());
		scn.SetShadowHand(troop, armor, savage);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(crossbowman));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowUseCardAction(crossbowman);
		assertEquals(1, scn.GetWoundsOn(crossbowman));

		// Aragorn, the only unbound companion, is about to take the wound: the Shadow player, who could pay, is
		// not offered the site, nor is the Free Peoples player for his own companion.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(site));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(site));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithOneCardInHandTheSiteIsNotOfferedAndTheWoundIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(servant);
		scn.SkipToPhase(Phase.MANEUVER);

		var site = scn.GetCurrentSite();
		assertEquals("Saruman's Laboratory", site.getBlueprint().getTitle());
		scn.MoveCardsToHand(defend1);
		scn.SetShadowHand(troop);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(servant));

		scn.FreepsPlayCard(defend1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(site));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertInHand(troop);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhenTwoMinionsLoseOneSkirmishOnlyTheWoundOfTheOneChosenIsPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPCScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn, 8 + 14 = 22, beats the two together, 7 + 9 = 16, and is short of doubling them (32): nobody is
		// overwhelmed, and the two lose together and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 14));

		var servant = scn.GetShadowCard("servant");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var armor = scn.GetShadowCard("armor");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveMinionsToTable(servant, guard);
		scn.SkipToAssignments();

		var site = scn.GetCurrentSite();
		assertEquals("Saruman's Laboratory", site.getBlueprint().getTitle());
		scn.SetShadowHand(troop, armor, savage);
		assertEquals(3, scn.GetShadowHandCount());
		scn.FreepsAssignToMinions(aragorn, servant);
		scn.ShadowAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(22, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.PassSkirmishActions();

		// Both are about to take a wound; the Shadow player is offered the site once, and pays two of his three cards.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(site));
		scn.ShadowChooseOptionalTrigger(site);
		scn.ShadowChooseCards(troop, armor);
		assertInDiscard(troop, armor);

		// "That wound" is one minion's: he is asked which, of the two.
		assertTrue(scn.ShadowHasCardChoiceAvailable(servant, guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(servant);

		// The other takes his wound: with one card left in hand, the site is not offered again.
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInHand(savage);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
