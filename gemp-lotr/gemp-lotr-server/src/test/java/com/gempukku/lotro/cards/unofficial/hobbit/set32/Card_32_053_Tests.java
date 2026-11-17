package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_053_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Legend is the
	// Shadow player's.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legend", "32_53");
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, vitality 4; his ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, vitality 3; his ability not used
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: [gandalf] Wizard companion, vitality 4; his ability not used
					put("boromir", "0_65");     // Boromir, Steward's Heir: [gondor] Man companion; keywords only
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon; bearer a [dwarven] character
					put("sting", "30_47");      // Sting: [shire] artifact, hand weapon; bearer Bilbo; its trigger needs a [dwarven] event played
					put("arrow", "32_16");      // Black Arrow: [esgaroth] possession, ranged weapon; bearer Bard; its Archery ability not used
					put("map", "32_12");        // Thrór's Map: [dwarven] possession of no class (no weapon); bearer a [dwarven] companion; not used
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally; Regroup: exert Bard to wound a minion
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal and a Regroup ability, not used
					put("bain", "32_15");       // Baïn, Son of Bard: [esgaroth] Man ally, vitality 3; a start-of-turn heal and a Maneuver ability, not used
					put("betsy", "33_19");      // Betsy Butterbur: [gandalf] Man ally; cannot take wounds; a Fellowship ability, not used
					put("worrywort", "33_53");  // Master Worrywort: [shire] Hobbit ally, home site 1; texts that need Bilbo to win or a skirmish
					put("runner", "31_30");     // Goblin Runner: [moria] Orc minion; a "when you play" trigger, not reached
					put("scimitar", "31_31");   // Goblin Scimitar: [moria] possession, hand weapon; bearer an Orc
					put("marksman1", "1_176");  // Goblin Marksman: an archer minion, vitality 1; Archer is its whole text
					put("marksman2", "1_176");
					put("bats", "32_30");       // Dark Bats: [moria] Creature minion; a Shadow ability, not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc minion; an Assignment ability, not used
					put("alfrid", "32_25");     // Alfrid, Lickspittle: [men] Man minion; texts that need an [esgaroth] ally assigned, or a maneuver or skirmish
					put("ferny", "33_22");      // Bill Ferny Sr.: [gundabad] Man minion; Regroup: kill an ally whose home is site 1
					put("axeman1", "4_224");    // Easterling Axeman: [raider] Man minion; a bonus that needs 2 burdens
					put("axeman2", "4_224");
					put("despair", "7_148");    // Fierce in Despair: [raider] Regroup event, cost 7: spot 2 [raider] Men to discard a companion (except the Ring-bearer)
					put("smaug", "30_66");      // Smaug, The Golden: [smaug] Dragon minion, strength 17, fierce; discarded if not at a mountain; a Regroup ability, not used
					put("malice", "32_51");     // Dragon's Malice: [smaug] condition, support area: a Shadow card that is no minion; its texts need Smaug
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TerrifyingLegendStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Terrifying Legend
		 * Unique: True
		 * Side: Shadow
		 * Culture: Smaug
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a companion is killed, you may discard a weapon (and exert each ally if you spot Smaug).<br><b>Regroup:</b> Discard a minion from play and remove (3) to discard a Man ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legend");

		assertEquals("Terrifying Legend", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeACompanionIsKilledTheShadowPlayerMayDiscardAWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var arrow = scn.GetFreepsCard("arrow");
		var map = scn.GetFreepsCard("map");
		var bard = scn.GetFreepsCard("bard");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var marksman = scn.GetShadowCard("marksman1");
		scn.MoveCardsToSupportArea(legend);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(bard);
		scn.AttachCardsTo(thorin, axe, map);
		scn.AttachCardsTo(bilbo, sting);
		scn.AttachCardsTo(bard, arrow);
		// Vitality 3 with 2 wounds: one more kills Balin.
		scn.AddWoundsToChar(balin, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, legend);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(map, thorin);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(arrow, bard);
		assertEquals(2, scn.GetWoundsOn(balin));
		scn.MoveMinionsToTable(marksman, runner);
		scn.AttachCardsTo(runner, scimitar);
		assertAttachedTo(scimitar, runner);

		// The Marksman's archery wound kills Balin.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(balin);
		assertInZone(Zone.DEAD, balin);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legend));
		scn.ShadowAcceptOptionalTrigger();

		// A hand weapon artifact, a hand weapon possession, a ranged weapon on an ally and a Shadow weapon on a
		// minion; not a possession that is no weapon.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sting, axe, arrow, scimitar));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(map));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axe);

		assertInDiscard(axe);
		assertAttachedTo(map, thorin);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(arrow, bard);
		assertAttachedTo(scimitar, runner);
		// No Smaug: Bard is not exerted.
		assertEquals(0, scn.GetWoundsOn(bard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningLeavesEveryWeaponWhereItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var marksman = scn.GetShadowCard("marksman1");
		scn.MoveCardsToSupportArea(legend);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, axe);
		scn.AttachCardsTo(bilbo, sting);
		scn.AddWoundsToChar(balin, 2);

		scn.StartGame();
		assertAttachedTo(axe, thorin);
		assertAttachedTo(sting, bilbo);
		assertEquals(2, scn.GetWoundsOn(balin));
		scn.MoveMinionsToTable(marksman);

		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(balin);
		assertInZone(Zone.DEAD, balin);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legend));
		scn.ShadowDeclineOptionalTrigger();

		assertAttachedTo(axe, thorin);
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TwoCompanionsKilledOfferItTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bilbo = scn.GetRingBearer();
		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCardsToSupportArea(legend);
		scn.MoveCompanionsToTable(thorin, balin, gandalf);
		scn.AttachCardsTo(thorin, axe);
		scn.AttachCardsTo(bilbo, sting);
		// One wound from death each: a [dwarven] Dwarf and a [gandalf] Wizard.
		scn.AddWoundsToChar(balin, 2);
		scn.AddWoundsToChar(gandalf, 3);

		scn.StartGame();
		assertAttachedTo(axe, thorin);
		assertAttachedTo(sting, bilbo);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.MoveMinionsToTable(marksman1, marksman2);

		// Two archery wounds: one kills Balin, the other Gandalf.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(balin);
		assertInZone(Zone.DEAD, balin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legend));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(axe);
		assertInDiscard(axe);
		assertAttachedTo(sting, bilbo);

		scn.FreepsChooseCard(gandalf);
		assertInZone(Zone.DEAD, gandalf);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legend));
		scn.ShadowAcceptOptionalTrigger();
		// Sting, the one weapon left, goes unasked.
		assertInDiscard(sting);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoWeaponToDiscardAndNoSmaug() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bard = scn.GetFreepsCard("bard");
		var marksman = scn.GetShadowCard("marksman1");
		scn.MoveCardsToSupportArea(legend);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(bard);
		scn.AddWoundsToChar(balin, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(balin));
		scn.MoveMinionsToTable(marksman);

		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(balin);
		assertInZone(Zone.DEAD, balin);

		// Nothing to discard and nothing to exert: the offer would do nothing.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legend));
		assertEquals(0, scn.GetWoundsOn(bard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionIsDiscardedOrAnAllyOrAMinionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var bard = scn.GetFreepsCard("bard");
		var worrywort = scn.GetFreepsCard("worrywort");
		var marksman = scn.GetShadowCard("marksman1");
		var ferny = scn.GetShadowCard("ferny");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		var despair = scn.GetShadowCard("despair");
		scn.MoveCardsToSupportArea(legend);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(bard, worrywort);
		scn.AttachCardsTo(thorin, axe);
		scn.MoveCardsToHand(despair);

		scn.StartGame();
		assertAttachedTo(axe, thorin);
		assertInHand(despair);
		scn.MoveMinionsToTable(marksman, ferny, axeman1, axeman2);

		// The Marksman's archery wound goes on Thorin, who lives.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(thorin);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(7);
		assertEquals(0, scn.GetWoundsOn(bard));

		// A minion killed: Bard's wound on the Marksman (vitality 1).
		scn.FreepsUseCardAction(bard);
		scn.FreepsChooseCard(marksman);
		assertInDiscard(marksman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legend));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// An ally killed: Bill Ferny Sr. kills Master Worrywort, whose home is site 1.
		scn.ShadowUseCardAction(ferny);
		assertInZone(Zone.DEAD, worrywort);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A companion discarded, not killed.
		scn.FreepsPass();
		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(thorin, balin));
		scn.ShadowChooseCard(balin);
		assertInDiscard(balin);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legend));
		assertAttachedTo(axe, thorin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithSmaugSpottedEachAllyExertsAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var axe = scn.GetFreepsCard("axe");
		var bard = scn.GetFreepsCard("bard");
		var elrond = scn.GetFreepsCard("elrond");
		var bain = scn.GetFreepsCard("bain");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(legend);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(balin, axe);
		scn.MoveAlliesToTable(bard, elrond, bain);
		// Vitality 3 with 2 wounds: Baïn is exhausted and cannot exert.
		scn.AddWoundsToChar(bain, 2);
		assertAttachedTo(axe, balin);
		assertEquals(2, scn.GetWoundsOn(bain));

		// Erebor, site 7, a mountain, where Smaug stays.
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(smaug);
		scn.ShadowRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, smaug);
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thorin);

		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legend));
		scn.ShadowAcceptOptionalTrigger();

		// The Axe, the one weapon, goes unasked; then each ally that can exert does, and no one else.
		assertInDiscard(axe);
		assertEquals(1, scn.GetWoundsOn(bard));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(bain));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(smaug));
		// Smaug is fierce: the fierce assignment phase comes next.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithSmaugSpottedAndNoWeaponTheAlliesStillExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bard = scn.GetFreepsCard("bard");
		var elrond = scn.GetFreepsCard("elrond");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(legend);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(bard, elrond);

		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(smaug);
		scn.ShadowRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, smaug);
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thorin);

		// No weapon in play: the discard does nothing, the exertions still can.
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legend));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(bard));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(balin));
		// Smaug is fierce: the fierce assignment phase comes next.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsAMinionAndRemovesThreeToDiscardAManAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var malice = scn.GetShadowCard("malice");
		var boromir = scn.GetFreepsCard("boromir");
		var bard = scn.GetFreepsCard("bard");
		var betsy = scn.GetFreepsCard("betsy");
		var elrond = scn.GetFreepsCard("elrond");
		var bats = scn.GetShadowCard("bats");
		var aggressor = scn.GetShadowCard("aggressor");
		var alfrid = scn.GetShadowCard("alfrid");
		scn.MoveCardsToSupportArea(legend, malice);
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(bard, betsy, elrond);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, legend, malice, bard, betsy, elrond);
		scn.MoveMinionsToTable(bats, aggressor, alfrid);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(legend));
		scn.ShadowUseCardAction(legend);

		// Minions of three cultures and races; not a Shadow card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bats, aggressor, alfrid));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(malice, legend));
		scn.ShadowChooseCard(bats);
		assertInDiscard(bats);
		assertEquals(0, scn.GetTwilight());

		// Man allies of two cultures; not an Elf ally, a Man companion or a Man minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bard, betsy));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(elrond, boromir, alfrid));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bard);

		assertInDiscard(bard);
		assertInZone(Zone.SUPPORT, betsy, elrond);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, alfrid);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var bard = scn.GetFreepsCard("bard");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToSupportArea(legend);
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, legend, bard);
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(legend));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoMinionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var malice = scn.GetShadowCard("malice");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveCardsToSupportArea(legend, malice);
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, legend, malice, bard);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(legend));
	}

	@Test
	public void WithNoManAllyTheRegroupAbilityIsPaidForAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legend = scn.GetShadowCard("legend");
		var elrond = scn.GetFreepsCard("elrond");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToSupportArea(legend);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, legend, elrond);
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(legend));
		scn.ShadowUseCardAction(legend);

		assertInDiscard(bats);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
