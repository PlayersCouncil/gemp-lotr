package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("get", "103_17");
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: strength 8, vitality 8; enduring, fierce, and damage +1 in a fierce skirmish
					put("eater", "8_25");       // Shelob, Eater of Light: strength 8, fierce; stronger for minions stacked on [gollum] possessions, of which she has none where she is used
					put("web", "8_30");         // Web: [gollum] possession, cost 1; regroup and Shadow abilities only
					put("poison", "8_28");      // Spider Poison: [gollum] possession, cost 1; a skirmish ability only while in the support area
					put("pass", "103_12");      // Pass of the Spider: [gollum] possession, cost 2; a skirmish ability the Shadow player passes on
					put("strands", "103_14");   // Sticky Strands of Spider-silk: [gollum] possession, cost 2; a skirmish ability only
					put("larder", "8_23");      // Larder: [gollum] possession, cost 1
					put("crown", "101_39");     // Pale Crown: a [wraith] artifact, cost 1, with no requirement to play: an item that is not [gollum] (no artifact is)
					put("burden", "7_60");      // Heavy Burden: a [gollum] condition, cost 1; a regroup ability only
					put("ships", "8_65");       // Ships of Great Draught: a [raider] possession, cost 2; Shadow and regroup abilities only
					put("runner1", "1_178");    // Goblin Runner: a [moria] minion
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("savage1", "1_151");    // Uruk Savage: an [isengard] minion
					put("savage2", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] minion who is not Shelob, strength 5

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; ties with Shelob, so she wins
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; loses to Shelob
					put("glorfindel", "9_16");  // Glorfindel, Revealed in Wrath: strength 9; beats Shelob
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; loses to Gollum
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UngoliantsGetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ungoliant's Get
		 * Unique: true
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time Shelob loses a skirmish, you may play a [gollum] item from your discard pile.
		 * 	Response: If Shelob wins a skirmish and there are 4 minions stacked on [gollum] items, hinder this to make her <b>relentless</b> until the regroup phase <i>(she participates in 1 extra round of skirmishes after fierce)</i>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("get");

		assertEquals("Ungoliant's Get", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenShelobLosesASkirmishAGollumItemMayBePlayedFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var poison = scn.GetShadowCard("poison");
		var crown = scn.GetShadowCard("crown");
		var burden = scn.GetShadowCard("burden");
		var ships = scn.GetShadowCard("ships");
		var strands = scn.GetShadowCard("strands");
		var pass = scn.GetShadowCard("pass");
		var theirLarder = scn.GetFreepsCard("larder");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(get);
		scn.MoveCardsToDiscard(web, poison, crown, burden, ships, theirLarder);
		scn.MoveCardsToHand(strands);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(glorfindel, shelob);
		scn.SetTwilight(5);

		assertInZone(Zone.DECK, pass);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Glorfindel's 9 beats Shelob's 8.
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(get));
		scn.ShadowChooseOptionalTrigger(get);

		// The two [gollum] possessions. (An item is a possession or an artifact; no artifact is [gollum].)
		assertTrue(scn.ShadowHasCardChoiceAvailable(web, poison));
		// Not a [gollum] condition, a [raider] possession or a [wraith] artifact in the discard pile, nor a [gollum]
		// possession in hand, in the draw deck or in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(burden, ships, crown, strands, pass, theirLarder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(web);

		assertInZone(Zone.SUPPORT, web);
		// 5 in the pool less the Web's 1
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DISCARD, poison, crown, burden, ships);
		assertEquals(1, scn.GetWoundsOn(shelob));
		// Shelob is fierce: a second assignment phase follows.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachSkirmishShelobLosesBringsItsOwnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var eater = scn.GetShadowCard("eater");
		var web = scn.GetShadowCard("web");
		var poison = scn.GetShadowCard("poison");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(get);
		scn.MoveCardsToDiscard(web, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(eater);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(glorfindel, eater);
		scn.SetTwilight(5);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(get));
		scn.ShadowChooseOptionalTrigger(get);
		scn.ShadowChooseCardBPFromSelection(web);

		assertInZone(Zone.SUPPORT, web);
		assertInZone(Zone.DISCARD, poison);
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(eater));

		// The fierce skirmish: she loses to Glorfindel again.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.BothPass();
		scn.FreepsAssignToMinions(glorfindel, eater);
		scn.FreepsResolveSkirmish(glorfindel);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(get));
		scn.ShadowChooseOptionalTrigger(get);

		// The Poison is the one [gollum] item left in the discard pile.
		assertInZone(Zone.SUPPORT, poison);
		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(eater));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOfferToPlayAnItemMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(get);
		scn.MoveCardsToDiscard(web);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(glorfindel, shelob);
		scn.SetTwilight(5);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(get));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, web);
		assertEquals(5, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnotherGollumMinionLosingAndShelobWinningBringNoOfferToPlayAnItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var web = scn.GetShadowCard("web");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(get);
		scn.MoveCardsToDiscard(web);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, gollum },
				new PhysicalCardImpl[] { boromir, shelob }
		);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);

		// Gollum's 5 loses to Aragorn's 8, with Shelob in play.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Shelob's 8 beats Boromir's 7. No minion is stacked anywhere, so her winning brings no response either.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertInZone(Zone.DISCARD, web);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoGollumItemInTheDiscardPileThatThePoolCanPayForNoOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var eater = scn.GetShadowCard("eater");
		var strands = scn.GetShadowCard("strands");
		var burden = scn.GetShadowCard("burden");
		var ships = scn.GetShadowCard("ships");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(get);
		// The one [gollum] item costs 2. Beside it lie a [gollum] condition the pool could pay for and a
		// possession of another culture.
		scn.MoveCardsToDiscard(strands, burden, ships);

		scn.StartGame();
		scn.MoveMinionsToTable(eater);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(glorfindel, eater);
		scn.SetTwilight(1);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(eater));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, strands, burden, ships);
		assertEquals(1, scn.GetTwilight());

		// She loses the fierce skirmish too, now with 2 in the pool.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.BothPass();
		scn.FreepsAssignToMinions(glorfindel, eater);
		scn.FreepsResolveSkirmish(glorfindel);
		scn.SetTwilight(2);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(get));
		scn.ShadowChooseOptionalTrigger(get);

		assertInZone(Zone.SUPPORT, strands);
		assertInZone(Zone.DISCARD, burden, ships);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenShelobWinsWithFourMinionsStackedOnGollumItemsThisMayBeHinderedToMakeHerRelentless() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var pass = scn.GetShadowCard("pass");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(get, web, pass);
		// Minions of two cultures on two [gollum] possessions, two on each: four in all.
		scn.StackCardsOn(web, runner1, savage1);
		scn.StackCardsOn(pass, runner2, savage2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, shelob);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Strength 8 against 8: the Shadow wins a tie.
		scn.PassCurrentPhaseActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(get));
		assertFalse(scn.HasKeyword(shelob, Keyword.RELENTLESS));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(get));

		scn.ShadowChooseOptionalTrigger(get);

		assertTrue(scn.IsHindered(get));
		assertTrue(scn.HasKeyword(shelob, Keyword.RELENTLESS));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void RelentlessIsHersAloneThroughTheExtraRoundOfSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var troop = scn.GetShadowCard("troop");
		var web = scn.GetShadowCard("web");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(aragorn, boromir, glorfindel);
		scn.MoveCardsToSupportArea(get, web);
		scn.StackCardsOn(web, runner1, runner2, savage1, savage2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(get));
		scn.ShadowChooseOptionalTrigger(get);
		assertTrue(scn.HasKeyword(shelob, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(troop, Keyword.RELENTLESS));

		// The fierce round: only Shelob is fierce. In a fierce skirmish she is damage +1, so Boromir takes 2.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.BothPass();
		assertFalse(scn.FreepsCanAssign(troop));
		scn.FreepsAssignToMinions(boromir, shelob);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(boromir));

		// The extra round: a third assignment phase, in which she can be assigned and the Troop cannot.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(shelob, Keyword.RELENTLESS));
		scn.BothPass();
		assertTrue(scn.FreepsCanAssign(shelob));
		assertFalse(scn.FreepsCanAssign(troop));
		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.FreepsResolveSkirmish(glorfindel);

		assertTrue(scn.IsCharSkirmishingAgainst(glorfindel, shelob));
		assertTrue(scn.HasKeyword(shelob, Keyword.RELENTLESS));

		// Glorfindel's 9 beats her 8, and the regroup phase follows.
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(shelob));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(shelob, Keyword.RELENTLESS));
		assertTrue(scn.IsHindered(get));
	}

	@Test
	public void ThreeMinionsStackedOnGollumItemsAreNotEnoughWhateverElseIsStackedAndAFourthMakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var pass = scn.GetShadowCard("pass");
		var burden = scn.GetShadowCard("burden");
		var ships = scn.GetShadowCard("ships");
		var poison = scn.GetShadowCard("poison");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(get, web, pass, burden, ships);
		// Three minions on [gollum] items.
		scn.StackCardsOn(web, runner1, savage1);
		scn.StackCardsOn(pass, runner2);
		// A possession stacked on a [gollum] item, a minion on a [gollum] condition, a minion on a [raider] possession.
		scn.StackCardsOn(web, poison);
		scn.StackCardsOn(burden, runner3);
		scn.StackCardsOn(ships, savage2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, shelob);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(get));
		assertFalse(scn.HasKeyword(shelob, Keyword.RELENTLESS));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());

		// A fourth minion on a [gollum] item, and she wins the fierce skirmish.
		scn.StackCardsOn(pass, troop);
		scn.BothPass();
		scn.FreepsAssignToMinions(boromir, shelob);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(get));
		scn.ShadowChooseOptionalTrigger(get);

		assertTrue(scn.IsHindered(get));
		assertTrue(scn.HasKeyword(shelob, Keyword.RELENTLESS));
		// The extra round's assignment phase.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void FiveStackedMinionsStillCountAndMinionsOnAHinderedItemDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var pass = scn.GetShadowCard("pass");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(get, web, pass);
		scn.StackCardsOn(web, runner1, savage1);
		scn.StackCardsOn(pass, runner2, runner3, savage2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, shelob);
		scn.PassCurrentPhaseActions();

		// Five stacked: there are 4.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(get));
		scn.ShadowDeclineOptionalTrigger();
		assertFalse(scn.IsHindered(get));
		assertFalse(scn.HasKeyword(shelob, Keyword.RELENTLESS));

		// The Web is hindered: its two minions are stacked on a hindered Shadow card and no longer on a [gollum]
		// item, which leaves three.
		scn.HinderCard(web);
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.BothPass();
		scn.FreepsAssignToMinions(boromir, shelob);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(get));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShelobLosingOrAnotherGollumMinionWinningBringsNoResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var web = scn.GetShadowCard("web");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var guard = scn.GetFreepsCard("guard");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(guard, glorfindel);
		scn.MoveCardsToSupportArea(get, web);
		scn.StackCardsOn(web, runner1, runner2, savage1, savage2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, gollum },
				new PhysicalCardImpl[] { glorfindel, shelob }
		);

		// Gollum's 5 beats the Guard's 4, with Shelob in play and four minions stacked.
		scn.FreepsResolveSkirmish(guard);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Shelob loses to Glorfindel's 9. The discard pile holds no item, so her losing brings no offer of its own.
		scn.FreepsResolveSkirmish(glorfindel);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertFalse(scn.IsHindered(get));
		assertFalse(scn.HasKeyword(shelob, Keyword.RELENTLESS));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhileItIsHinderedShelobWinningBringsNoResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(get, web);
		scn.StackCardsOn(web, runner1, runner2, savage1, savage2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.HinderCard(get);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, shelob);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsHindered(get));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.HasKeyword(shelob, Keyword.RELENTLESS));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
