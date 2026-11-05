package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_055_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  The skirmishes are at Erebor 30_55 (site 7, a mountain, where Smaug stays in play), reached by
	// SkipToSite(6) and the move; the minions and the condition are laid in its Shadow phase.  Smaug is 17 with
	// damage +2: he overwhelms a character of strength 8 or less, and one who loses to him and is not overwhelmed
	// takes 3 wounds.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wrath", "32_55");
					// Smaug, The Golden: [smaug] Dragon minion, 17/5, damage +2, fierce; discarded if not at a mountain;
					// a Regroup ability, not used.
					put("smaug", "30_66");
					// Orcs for the discard pile, of two cultures, each cheap; and a minion that is no Orc.
					put("sentinel", "33_49");    // Orc Sentinel: [sauron] Orc, cost 1; [sauron] Orcs' site number -1, a Shadow ability, not used
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc, cost 2; an Assignment ability, not used
					put("bats", "32_30");        // Dark Bats: [moria] Creature, cost 1; a Shadow ability, not used
					// An Orc for the other player's own discard pile, of a blueprint this player's piles do not hold.
					put("malevolent", "33_47");  // Malevolent Orc: [sauron] Orc
					// Goblin Patrol Troop: [moria] Orc, 13/3, no text: a minion that kills in a skirmish of its own.
					put("troop", "1_177");
					// Orkish Skirmisher: [orc] Orc, 9/3: "Each time this minion wins a skirmish, you may exert him twice
					// to wound a companion."
					put("skirmisher", "18_90");

					// Shadow conditions of two cultures, each cost 2 and with no requirement to play.
					put("jail", "32_39");        // Jail: [sauron]; a Regroup ability, not used
					put("river", "31_50");       // Enchanted River: [spider], unique; a trigger at site 5 and a Shadow ability, not used
					// A Free Peoples condition, which the Shadow player's piles hold too.
					put("lord", "1_21");         // Lord of Moria: [dwarven] support-area condition, each Dwarf damage +1
					// A Shadow condition for the other player's own discard pile, of a blueprint this player's piles do not hold.
					put("swarms", "31_34");      // Goblin Swarms: [moria] condition

					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a Fellowship ability, not used.
					put("balin", "30_2");
					// Dwalin, Brother of Balin: [dwarven] Dwarf companion, 7/3, damage +1; a Maneuver ability, not used.
					put("dwalin", "30_9");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, 7/4, damage +1; +1 for each [dwarven] follower he
					// bears (none) and a Fellowship ability, not used.
					put("thorin", "30_19");
					// Thrarin, Dwarven Smith: [dwarven] Dwarf ally, 4/2: "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WrathoftheDragonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Wrath of the Dragon
		 * Unique: True
		 * Side: Shadow
		 * Culture: Smaug
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time Smaug kills a character, you may play an Orc from your discard pile.<br><b>Skirmish:</b>
		 * Discard this condition or exert Smaug twice to play a Shadow condition from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wrath");

		assertEquals("Wrath of the Dragon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SmaugOverwhelmingACompanionLetsYouPlayAnOrcFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var smaug = scn.GetShadowCard("smaug");
		var sentinel = scn.GetShadowCard("sentinel");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		var malevolent = scn.GetFreepsCard("malevolent");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCardsToDiscard(sentinel, aggressor, bats, malevolent);
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.DISCARD, sentinel, aggressor, bats, malevolent);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(10);

		// Balin, 7, is overwhelmed by Smaug's 17 and killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, balin);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wrath));
		scn.ShadowAcceptOptionalTrigger();
		// Orcs of two cultures; not the Bats, a Creature, nor the other player's Orc in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sentinel, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, malevolent));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentinel);

		assertInZone(Zone.SHADOW_CHARACTERS, sentinel, smaug);
		assertInZone(Zone.DISCARD, aggressor, bats, malevolent);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachTimeSmaugKillsACharacterItIsOfferedAgainAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var smaug = scn.GetShadowCard("smaug");
		var sentinel = scn.GetShadowCard("sentinel");
		var aggressor = scn.GetShadowCard("aggressor");
		var balin = scn.GetFreepsCard("balin");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInZone(Zone.SUPPORT, thrarin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCardsToDiscard(sentinel, aggressor);
		// Balin 10 by hand: not overwhelmed by Smaug, he is killed by the 3 wounds of losing to him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 3));
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.DISCARD, sentinel, aggressor);

		// The ally is made able to skirmish, and is left out of the first assignments.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(10);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, balin);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wrath));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(aggressor);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);

		// The fierce skirmish: Smaug overwhelms the ally, a character too.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(thrarin, smaug);
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassFierceSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wrath));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, sentinel);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AKillByAnotherMinionInAnotherSkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var smaug = scn.GetShadowCard("smaug");
		var troop = scn.GetShadowCard("troop");
		var sentinel = scn.GetShadowCard("sentinel");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCompanionsToTable(balin, dwalin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin, dwalin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, troop);
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCardsToDiscard(sentinel);
		// Balin 18 by hand beats Smaug's 17; Dwalin, with 1 vitality left, is killed by the Troop's wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 11));
		scn.AddWoundsToChar(dwalin, 2);
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.DISCARD, sentinel);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, smaug}, new PhysicalCardImpl[]{dwalin, troop});
		scn.SetTwilight(10);

		scn.FreepsResolveSkirmish(dwalin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, dwalin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wrath));

		// Smaug loses his own skirmish and kills nobody.
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(smaug));
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wrath));
		assertInZone(Zone.DISCARD, sentinel);
		assertTrue(scn.InFierceRound());
	}

	@Test
	public void AnotherMinionsOwnWoundInSmaugsSkirmishIsNoKillOfSmaugs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var smaug = scn.GetShadowCard("smaug");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var sentinel = scn.GetShadowCard("sentinel");
		var thorin = scn.GetFreepsCard("thorin");
		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCompanionsToTable(thorin, dwalin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, thorin, dwalin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, skirmisher);
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCardsToDiscard(sentinel);
		// Thorin 19 by hand against 17 + 9: he loses without being overwhelmed and lives through 3 wounds.  Dwalin,
		// in no skirmish, has 1 vitality left.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thorin, null, 12));
		scn.AddWoundsToChar(dwalin, 2);
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.DISCARD, sentinel);

		scn.SkipToAssignments();
		// The Free Peoples player assigns Thorin to Smaug, and the Shadow player puts the Skirmisher in with them.
		scn.FreepsAssignToMinions(thorin, smaug);
		scn.ShadowAssignToMinions(thorin, skirmisher);
		assertTrue(scn.IsCharAssignedAgainst(thorin, skirmisher));
		scn.FreepsResolveSkirmish(thorin);
		scn.SetTwilight(10);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(thorin));

		// The Skirmisher's own trigger wounds Dwalin, who dies of it: a kill of the Skirmisher's, not of Smaug's.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(dwalin);
		assertInZone(Zone.DEAD, dwalin);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wrath));
		assertInZone(Zone.DISCARD, sentinel);
	}

	@Test
	public void WithNoOrcInTheDiscardPileNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var smaug = scn.GetShadowCard("smaug");
		var bats = scn.GetShadowCard("bats");
		var malevolent = scn.GetFreepsCard("malevolent");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(wrath);
		// No Orc in this player's discard pile; one in the other player's.
		scn.MoveCardsToDiscard(bats, malevolent);
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.DISCARD, bats, malevolent);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(10);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, balin);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wrath));
		assertInZone(Zone.DISCARD, bats, malevolent);
		assertTrue(scn.InFierceRound());
	}

	@Test
	public void SkirmishActionExertsSmaugTwiceOrDiscardsThisToPlayAShadowConditionFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var smaug = scn.GetShadowCard("smaug");
		var jail = scn.GetShadowCard("jail");
		var river = scn.GetShadowCard("river");
		var lord = scn.GetShadowCard("lord");
		var sentinel = scn.GetShadowCard("sentinel");
		var swarms = scn.GetFreepsCard("swarms");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCardsToDiscard(jail, river, lord, sentinel, swarms);
		// Balin 18 by hand, so that the skirmish kills nobody.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 11));
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.DISCARD, jail, river, lord, sentinel, swarms);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(smaug));
		assertTrue(scn.ShadowActionAvailable(wrath));

		scn.ShadowUseCardAction(wrath);
		// Both costs can be paid: the player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("Discard this condition"));
		assertTrue(scn.ShadowChoiceAvailable("Exert Smaug twice"));
		scn.ShadowChoose("Exert Smaug twice");
		assertEquals(2, scn.GetWoundsOn(smaug));
		assertInZone(Zone.SUPPORT, wrath);

		// Shadow conditions of two cultures; not a Shadow card that is no condition, a Free Peoples condition, or the
		// other player's condition in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(jail, river));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentinel, lord, swarms));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(jail);

		assertInZone(Zone.SUPPORT, jail, wrath);
		assertInZone(Zone.DISCARD, river, lord, sentinel, swarms);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithSmaugUnableToExertTwiceThisConditionIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var smaug = scn.GetShadowCard("smaug");
		var jail = scn.GetShadowCard("jail");
		var river = scn.GetShadowCard("river");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCardsToDiscard(jail, river);
		// Smaug, vitality 5, with 3 wounds: 2 left, too few to exert twice.
		scn.AddWoundsToChar(smaug, 3);
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 11));
		assertEquals(3, scn.GetWoundsOn(smaug));
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.DISCARD, jail, river);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(wrath));

		scn.ShadowUseCardAction(wrath);
		assertInZone(Zone.DISCARD, wrath);
		assertEquals(3, scn.GetWoundsOn(smaug));
		assertTrue(scn.ShadowHasCardChoiceAvailable(jail, river));
		scn.ShadowChooseCard(river);

		assertInZone(Zone.SUPPORT, river);
		assertInZone(Zone.DISCARD, jail, wrath);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ThisConditionDiscardedForTheCostIsNotPlayedBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var smaug = scn.GetShadowCard("smaug");
		var jail = scn.GetShadowCard("jail");
		var river = scn.GetShadowCard("river");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCardsToDiscard(jail, river);
		scn.AddWoundsToChar(smaug, 3);
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 11));
		assertEquals(3, scn.GetWoundsOn(smaug));
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.DISCARD, jail, river);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(wrath);
		assertInZone(Zone.DISCARD, wrath);
		// It was not in the discard pile when the action began.
		assertTrue(scn.ShadowHasCardChoiceAvailable(jail, river));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(wrath));
		scn.ShadowChooseCard(river);
		assertInZone(Zone.SUPPORT, river);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithNoShadowConditionInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetShadowCard("wrath");
		var smaug = scn.GetShadowCard("smaug");
		var lord = scn.GetShadowCard("lord");
		var sentinel = scn.GetShadowCard("sentinel");
		var swarms = scn.GetFreepsCard("swarms");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(wrath);
		// A Free Peoples condition and a minion in this player's discard pile; a Shadow condition in the other's.
		scn.MoveCardsToDiscard(lord, sentinel, swarms);
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 11));
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.DISCARD, lord, sentinel, swarms);
		assertEquals(0, scn.GetWoundsOn(smaug));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wrath));
	}
}
