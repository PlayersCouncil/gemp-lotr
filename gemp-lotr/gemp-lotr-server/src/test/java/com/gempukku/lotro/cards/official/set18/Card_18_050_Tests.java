package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stone", "18_50");
					put("stone2", "18_50");

					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, strength 6; Knight is his whole text
					// Gondorian Merchant: a [gondor] Man who is an ally; a maneuver ability that needs initiative
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Smeagol, Always Helps: "Assignment: Assign a minion to Smeagol and add 2 threats to exhaust
					// that minion."
					put("smeagol", "7_71");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					// Defend It and Hope: [gondor] Maneuver event, cost 0. "Exert a [gondor] Man to wound a minion
					// once (or twice if that minion is an Uruk-hai)."
					put("defend", "4_115");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6; no text
					// Goblin Runner: [moria] Orc, vitality 1. It adds (2) when played, so it is only ever put on
					// the table here.
					put("runner", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2; Damage +1 is its whole text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] minion who is a Man, cost 2; no text
					// Ulaire Attea, Khamul of the East: a Nazgul who is [raider], cost 6; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] minion who is an Orc, cost 3; its text needs 3 burdens
					put("creeper", "8_75");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, cost 4; his text needs a [wraith]
					// condition on a companion
					put("lemenya", "1_232");
					// Goblin Armory: a [moria] condition, cost 0; its texts answer a [moria] weapon played and a
					// wound to a [moria] Orc
					put("armory", "1_173");
					// Uruk Runner: [isengard] Uruk-hai, strength 8, vitality 2, fierce (and a tracker); stronger
					// only in a fierce skirmish of its own
					put("fierce1", "4_193");
					put("fierce2", "4_193");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Redhorn Pass 13_194 ("Culture tokens cannot be added, removed, or reinforced") in
	// place of a site no test reaches; the test lays it as the site after the first.
	protected VirtualTableScenario GetRedhornPassScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stone", "18_50");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2; Damage +1 is its whole text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheFaithfulStoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: The Faithful Stone
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Each time a non-[wraith] minion is played, you may spot a Man to add a [gondor] token here.
		 * 	<b>Maneuver:</b> Remove 3 [gondor] tokens from here to spot a minion. That minion cannot be assigned to
		 * 	a skirmish until the regroup phase. Any Shadow player may remove (2) to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stone");

		assertEquals("The Faithful Stone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromHandForThreeToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		scn.MoveCardsToHand(stone);

		scn.StartGame();

		assertInZone(Zone.HAND, stone);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(stone));

		scn.FreepsPlayCard(stone);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stone);
		assertNotAttachedTo(stone, scn.GetRingBearer());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionPlayedThatIsNotOfTheWraithCultureOffersATokenHereWhileAManCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(knight);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var lemenya = scn.GetShadowCard("lemenya");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(troop, savage, khamul, creeper, lemenya, armory);

		scn.StartGame();
		// Enough for all six, each minion roaming at site 2.
		scn.SetTwilight(40);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(stone));

		// A [moria] Orc.
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone));
		assertEquals(0, scn.GetCultureTokensOn(stone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stone));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [isengard] Uruk-hai: a second offer and a second token.
		scn.ShadowPlayCard(savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(stone));

		// A Nazgul who is not a [wraith] card: a third.
		scn.ShadowPlayCard(khamul);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetCultureTokensOn(stone));

		// A [wraith] minion who is an Orc: no offer.
		scn.ShadowPlayCard(creeper);
		assertInZone(Zone.SHADOW_CHARACTERS, creeper);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [wraith] Nazgul: none.
		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [moria] card that is no minion: none.
		scn.ShadowPlayCard(armory);
		assertInZone(Zone.SUPPORT, armory);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetCultureTokensOn(stone));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOfferMayBeDeclinedAndEachCopyAsksForItsOwnToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var stone2 = scn.GetFreepsCard("stone2");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(stone, stone2);
		scn.MoveCompanionsToTable(knight);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(troop, savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		// One minion played: each copy makes its own offer.
		scn.ShadowPlayCard(troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone2));

		// The second copy's is taken and the first copy's declined: the token is on the second copy only.
		scn.FreepsChooseOptionalTrigger(stone2);
		assertEquals(1, scn.GetCultureTokensOn(stone2));
		assertEquals(0, scn.GetCultureTokensOn(stone));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stone2));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(stone));
		assertEquals(1, scn.GetCultureTokensOn(stone2));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Both declined at the next minion: nothing changes.
		scn.ShadowPlayCard(savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone2));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(stone));
		assertEquals(1, scn.GetCultureTokensOn(stone2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoOfferWhileNoManCanBeSpottedAndAManWhoIsAMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToSupportArea(stone);
		// The fellowship is two Hobbits, one of them a [gondor] companion.
		scn.MoveCompanionsToTable(pippin);

		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(troop, brigand, savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(0, scn.GetCultureTokensOn(stone));

		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Dunlending Brigand is a Man, and in play by the time his being played is answered.
		scn.ShadowPlayCard(brigand);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stone));

		// He is still there to be spotted when the next minion comes.
		scn.ShadowPlayCard(savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(stone));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AManWhoIsAnAllyIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveAlliesToTable(merchant);

		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(0, scn.GetCultureTokensOn(stone));

		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stone));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stone));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhereNoCultureTokenCanBeAddedTheTokenIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornPassScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(knight);

		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(savage);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("redhorn"));
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals("Redhorn Pass", scn.GetCurrentSite().getBlueprint().getTitle());

		// A Man to spot and a minion that is not a [wraith] card, as where the token is offered.
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(stone));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverActionRemovesThreeTokensFromHereAndTheChosenMinionCannotBeAssignedByAnyone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(knight, smeagol);

		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddTokensToCard(stone, 4);
		scn.MoveMinionsToTable(savage, brigand, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		assertEquals(4, scn.GetCultureTokensOn(stone));
		assertTrue(scn.FreepsActionAvailable(stone));

		scn.FreepsUseCardAction(stone);
		assertEquals(1, scn.GetCultureTokensOn(stone));

		// The Shadow player is asked before the minion is named, and declines.
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();
		assertEquals(5, scn.GetTwilight());

		// An Uruk-hai, a Man and an Orc, of three cultures.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, brigand, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, smeagol, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetCultureTokensOn(stone));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// An assignment action that assigns "a minion": the Brigand and the Runner may be chosen, the Savage
		// may not.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertTrue(scn.FreepsHasCardChoiceAvailable(brigand, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(runner);
		assertTrue(scn.IsCharAssignedAgainst(smeagol, runner));

		scn.ShadowPass();
		scn.FreepsPass();

		// The Free Peoples player's assignments: the Brigand, and not the Savage.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertFalse(scn.FreepsCanAssign(savage));
		assertTrue(scn.FreepsCanAssign(brigand));
		scn.FreepsDeclineAssignments();

		// The Shadow player's assignment of the minions left over: the Brigand, and not the Savage.
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(brigand));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(savage));
		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertFalse(scn.ShadowCanAssign(savage));
		assertTrue(scn.ShadowCanAssign(brigand));
		scn.ShadowAssignToMinions(knight, brigand);

		assertTrue(scn.IsCharAssigned(runner));
		assertTrue(scn.IsCharAssigned(brigand));
		assertFalse(scn.IsCharAssigned(savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheMinionCannotBeAssignedInTheFierceAssignmentsEitherAndCanBeOnceTheRegroupPhaseHasBegun() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(aragorn);

		var chosen = scn.GetShadowCard("fierce1");
		var other = scn.GetShadowCard("fierce2");

		scn.StartGame();
		scn.AddTokensToCard(stone, 3);
		scn.MoveMinionsToTable(chosen, other);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(stone);
		scn.ShadowChooseNo();
		scn.FreepsChooseCard(chosen);

		scn.SkipToAssignments();
		assertFalse(scn.InFierceRound());
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(chosen));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(other));
		scn.FreepsAssignToMinions(aragorn, other);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Both Uruk Runners are fierce and neither has been killed, so there is a second round of
		// assignments, the last before the regroup phase. The other copy is offered in it to each player
		// and the chosen one to neither.
		assertTrue(scn.InFierceRound());
		assertInZone(Zone.SHADOW_CHARACTERS, chosen, other);
		scn.PassFierceAssignmentActions();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(chosen));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(other));
		assertFalse(scn.FreepsCanAssign(chosen));
		scn.FreepsDeclineAssignments();
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(chosen));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(other));
		assertFalse(scn.ShadowCanAssign(chosen));
		scn.ShadowDeclineAssignments();

		// The regroup phase has begun: by the game's rule either player may assign it again.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsCanAssign(chosen));
		assertTrue(scn.ShadowCanAssign(chosen));

		// The fellowship moves on, the minions stay, and in the next assignment phase it is offered.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertInZone(Zone.SHADOW_CHARACTERS, chosen, other);
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(chosen));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(other));
		assertTrue(scn.FreepsDecisionAvailable("assign"));
	}

	@Test
	public void TheShadowPlayerMayRemoveTwoToPreventItAndTheTokensStaySpent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(knight);

		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.AddTokensToCard(stone, 4);
		scn.MoveMinionsToTable(savage, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		assertEquals(4, scn.GetCultureTokensOn(stone));

		scn.FreepsUseCardAction(stone);
		assertEquals(1, scn.GetCultureTokensOn(stone));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("prevent"));

		scn.ShadowChooseYes();
		assertEquals(3, scn.GetTwilight());
		// The three tokens are a cost, and stay paid. No minion is named.
		assertEquals(1, scn.GetCultureTokensOn(stone));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		assertTrue(scn.FreepsCanAssign(savage));
		assertTrue(scn.ShadowCanAssign(savage));
		assertTrue(scn.FreepsDecisionAvailable("assign"));
	}

	@Test
	@Ignore("Q-0284: the Shadow player is asked whether to prevent before the minion is spotted; the text spots the minion first, and the erratum 68_50 asks in that order")
	public void TheMinionIsSpottedBeforeTheShadowPlayerIsAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(knight);

		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.AddTokensToCard(stone, 3);
		scn.MoveMinionsToTable(savage, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		scn.FreepsUseCardAction(stone);
		assertEquals(0, scn.GetCultureTokensOn(stone));

		// The minion is spotted first, and the Shadow player answers knowing which it is.
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, brigand));
		scn.FreepsChooseCard(savage);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));

		scn.ShadowChooseYes();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertTrue(scn.FreepsDecisionAvailable("assign"));
	}

	@Test
	public void ManeuverActionNeedsThreeTokensHereAndAShadowPlayerWithOneTwilightTokenIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var stone2 = scn.GetFreepsCard("stone2");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(stone, stone2);
		scn.MoveCompanionsToTable(knight);

		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Two [gondor] tokens here and three on the other copy.
		scn.AddTokensToCard(stone, 2);
		scn.AddTokensToCard(stone2, 3);
		scn.MoveMinionsToTable(savage, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetCultureTokensOn(stone));
		assertEquals(3, scn.GetCultureTokensOn(stone2));
		assertFalse(scn.FreepsActionAvailable(stone));
		assertTrue(scn.FreepsActionAvailable(stone2));

		scn.AddTokensToCard(stone, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(stone));

		scn.FreepsUseCardAction(stone);
		assertEquals(0, scn.GetCultureTokensOn(stone));
		assertEquals(3, scn.GetCultureTokensOn(stone2));

		// With (1) in the pool the Shadow player cannot remove (2) and is not asked to.
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, brigand));
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		assertTrue(scn.FreepsDecisionAvailable("assign"));
	}

	@Test
	public void WithNoMinionLeftToSpotTheTokensAreSpentForNothingAndTheShadowPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var knight = scn.GetFreepsCard("knight");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(defend);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddTokensToCard(stone, 4);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		// The only minion, with 1 vitality, is killed in the maneuver phase.
		scn.FreepsPlayCard(defend);
		assertInZone(Zone.DISCARD, runner);
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(4, scn.GetCultureTokensOn(stone));
		assertTrue(scn.FreepsActionAvailable(stone));

		scn.FreepsUseCardAction(stone);
		assertEquals(1, scn.GetCultureTokensOn(stone));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
