package com.gempukku.lotro.cards.unofficial.pc.errata.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_232_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, strength 6, vitality 3; "Skirmish: Exert Gimli to make him strength +2."
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; a Maneuver ability only
					put("bounder", "1_286");    // Bounder: ally, strength 2, vitality 2; a Skirmish ability that costs an exertion
					put("axe", "1_3");          // Axe Strike: Skirmish event, cost 0, "Make a Dwarf strength +2"
					put("follow", "1_109");     // One Whom Men Would Follow: Maneuver event; exert Aragorn, and an ally takes part in skirmishes until the regroup phase

					put("troop", "1_177");      // Goblin Patrol Troop: minion with no text, strength 13, vitality 3
					put("troop2", "1_177");
					put("slayer", "1_153");     // Uruk Slayer: minion, vitality 2; "Skirmish: Remove (1) to make this minion strength +1 (limit +3)."
					put("savage", "1_151");     // Uruk Savage: minion, strength 5, vitality 3; Damage +1 is its whole text
					put("fear", "1_278");       // Strength Born of Fear: support-area condition; "Skirmish: Remove (3) to make a [sauron] Orc strength +1."
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("cavern", "61_232");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void CavernEntranceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Cavern Entrance
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: At the start of each skirmish, you may exert your character in that skirmish to prevent
		 * 	special abilities being used.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("cavern");

		assertEquals("Cavern Entrance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
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
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(axe);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		// The Shadow player lays Cavern Entrance as site 2.
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("cavern"));
		assertEquals("Cavern Entrance", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop, slayer);
		scn.SetTwilight(10);
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
		// Gimli could exert again, but his special ability cannot be used. A Skirmish event is not a special ability.
		assertFalse(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.FreepsPlayAvailable(axe));
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsPlayCard(axe);
		assertEquals(8, scn.GetStrength(gimli));
		assertInDiscard(axe);

		// Nor can the Shadow player's, a minion's or a condition's: the pool holds what each would cost.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(slayer));
		assertFalse(scn.ShadowActionAvailable(fear));
	}

	@Test
	public void TheShadowPlayerMayExertOneOfHisMinionsInTheSkirmishToPreventSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var slayer = scn.GetShadowCard("slayer");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("cavern"));

		scn.MoveMinionsToTable(troop, troop2, slayer);
		scn.SetTwilight(10);
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
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(slayer));
		assertFalse(scn.ShadowActionAvailable(fear));
	}

	@Test
	public void TheFreePeoplesPlayerMayExertAnAllyInTheSkirmishToPreventSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var follow = scn.GetFreepsCard("follow");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(follow);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("cavern"));

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		// No ally's home is a Shadows-block site: One Whom Men Would Follow has the Bounder take part in
		// skirmishes, for an exertion of Aragorn.
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, savage);
		scn.FreepsResolveSkirmish(bounder);

		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseYes();
		// An ally is a character too. Aragorn is in no skirmish.
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert"));
		scn.ShadowChooseNo();
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ThePreventionStopsTheOneRingsResponseToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("cavern"));

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
		// more for Damage +1. The Ring's Response to the first of them is a special ability, so it is not offered.
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

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("cavern"));

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
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("cavern"));

		scn.MoveMinionsToTable(troop, slayer);
		scn.SetTwilight(10);
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
		assertFalse(scn.ShadowActionAvailable(fear));
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

		// Both declined, and the first skirmish's prevention is over: the Shadow player's abilities can be used.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(fear));
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
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("cavern"));

		scn.MoveMinionsToTable(troop, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{aragorn, slayer});
		// Vitality 3 with 2 wounds: Gimli cannot exert. Aragorn, who could, is in another skirmish.
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
