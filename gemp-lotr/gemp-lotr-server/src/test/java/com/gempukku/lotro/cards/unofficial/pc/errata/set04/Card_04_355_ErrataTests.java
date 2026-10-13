package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_355_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: companion, vitality 3; "Skirmish: Exert Gimli to make him strength +2."
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; a Maneuver ability only
					put("axe", "1_3");          // Axe Strike: Skirmish event, cost 0, "Make a Dwarf strength +2"
					put("troop", "1_177");      // Goblin Patrol Troop: minion with no text, vitality 3
					put("troop2", "1_177");
					put("slayer", "1_153");     // Uruk Slayer: minion, vitality 2; "Skirmish: Remove (1) to make this minion strength +1 (limit +3)."
					put("savage", "1_151");     // Uruk Savage: minion, strength 5, vitality 3; Damage +1 is its whole text
					put("herdsman", "7_247");   // Rohirrim Herdsman: Man ally, strength 4, vitality 2, home site 3T; a Regroup ability only
				}},
				// Each player's adventure deck has Cavern Entrance in place of site 7; the tests put the Shadow
				// player's copy on the path as the site the fellowship moves to next.
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site7", "54_355");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CavernEntranceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Cavern Entrance
		 * Unique: False
		 * Side:
		 * Culture:
		 * Shadow Number: 7
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 7T
		 * Game Text: At the start of each skirmish, you may exert your character in that skirmish to prevent special abilities being used.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Cavern Entrance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		// The original was Underground; the erratum's text has no keyword.
		assertFalse(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void AtTheStartOfASkirmishTheFreePeoplesPlayerMayExertHisCharacterInItToPreventSpecialAbilitiesButNotEvents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var troop = scn.GetShadowCard("troop");
		var slayer = scn.GetShadowCard("slayer");
		var cavern = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();
		assertEquals(cavern, scn.GetCurrentSite());

		scn.MoveMinionsToTable(troop, slayer);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The start of the assignment phase asks nothing.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{aragorn, slayer});
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseYes();
		// Gimli, the Free Peoples player's character in this skirmish, exerts; Aragorn, assigned to a skirmish
		// that has not begun, does not.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Shadow player is asked as well, for his own minion in the skirmish.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert"));
		scn.ShadowChooseNo();
		assertEquals(0, scn.GetWoundsOn(troop));

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Gimli could exert again, but his special ability cannot be used.  A Skirmish event is not a special ability.
		assertFalse(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);
		assertInDiscard(axe);

		// Nor can the Shadow player's: the pool holds the (1) it would cost.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.GetTwilight() >= 1);
		assertFalse(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void TheShadowPlayerMayExertOneOfHisMinionsInTheSkirmishToPreventSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var slayer = scn.GetShadowCard("slayer");
		var cavern = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();

		scn.MoveMinionsToTable(troop, troop2, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{aragorn, troop2});
		// The Shadow player assigns the minion left over to Gimli as well.
		scn.ShadowAssignToMinions(gimli, slayer);
		scn.FreepsResolveSkirmish(gimli);

		// The Free Peoples player declines, and nothing is exerted.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();
		assertEquals(0, scn.GetWoundsOn(gimli));

		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert"));
		scn.ShadowChooseYes();
		// Either minion in this skirmish; not the one assigned to Aragorn, and no Free Peoples character.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, slayer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop2, gimli, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(slayer);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troop2));

		// The Shadow player's prevention stops the Free Peoples player's special abilities too.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gimli));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void TheFreePeoplesPlayerMayExertAnAllyInTheSkirmishToPreventSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herdsman = scn.GetFreepsCard("herdsman");
		var troop = scn.GetShadowCard("troop");
		var cavern = scn.GetShadowSite(7);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();
		scn.SkipToSite(2);
		// Cavern Entrance is a Towers site, and the Herdsman's home is site 3 of that block.
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();
		assertEquals(cavern, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(herdsman, troop);
		scn.FreepsResolveSkirmish(herdsman);

		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseYes();
		// An ally is a character too.
		assertEquals(1, scn.GetWoundsOn(herdsman));

		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert"));
		scn.ShadowChooseNo();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ThePreventionStopsTheOneRingsResponseToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");
		var cavern = scn.GetShadowSite(7);

		scn.StartGame();
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseYes();
		scn.ShadowChooseNo();
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsPass();
		scn.ShadowPass();
		// Frodo (3, and 1 more from the Ruling Ring) loses to the Uruk Savage (5) and takes a wound, and one
		// more for Damage +1.  The Ring's Response to the first of them is a special ability, so it is not offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ring));
		assertEquals(3, scn.GetWoundsOn(frodo)); // 1 exerted + 1 wound + 1 damage
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOneRingsResponseIsOfferedWhenBothPlayersDeclineToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");
		var cavern = scn.GetShadowSite(7);

		scn.StartGame();
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		scn.FreepsChooseNo();
		scn.ShadowChooseNo();
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPass();
		scn.ShadowPass();
		// The same skirmish as in the test above, with nothing prevented: Frodo is about to take his wound, and
		// the Ruling Ring's Response is offered; and again for the second wound that Damage +1 adds.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThePreventionLastsForItsSkirmishAndEachLaterSkirmishAsksAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var slayer = scn.GetShadowCard("slayer");
		var cavern = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();

		scn.MoveMinionsToTable(troop, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{aragorn, slayer});
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsChooseYes();
		scn.ShadowChooseNo();
		assertEquals(1, scn.GetWoundsOn(gimli));

		// In Gimli's skirmish the Uruk Slayer's ability is prevented, though it is not in that skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer));
		scn.ShadowPass();

		// Gimli loses to the Troop (6 against 13).
		scn.FreepsResolveSkirmish(aragorn);

		// The second skirmish asks again, each player for his own character in it.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();
		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert"));
		scn.ShadowChooseNo();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(slayer));

		// Both declined, and the first skirmish's prevention is over: the Uruk Slayer's ability can be used.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slayer));
		int strength = scn.GetStrength(slayer);
		scn.ShadowUseCardAction(slayer);
		assertEquals(strength + 1, scn.GetStrength(slayer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void APlayerWhoseCharacterInTheSkirmishIsExhaustedIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var slayer = scn.GetShadowCard("slayer");
		var cavern = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.MoveCardToAdventurePath(cavern);
		scn.FreepsPass();

		scn.MoveMinionsToTable(troop, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{aragorn, slayer});
		// Vitality 3 with 2 wounds: Gimli cannot exert.  Aragorn, who could, is in another skirmish.
		scn.AddWoundsToChar(gimli, 2);
		scn.FreepsResolveSkirmish(gimli);

		// Only the Shadow player is asked.
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert"));
		scn.ShadowChooseNo();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
