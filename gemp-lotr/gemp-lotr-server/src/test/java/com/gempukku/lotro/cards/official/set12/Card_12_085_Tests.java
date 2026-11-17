package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "12_85");
					put("prowler", "11_136");   // Prowling Orc: [orc] Orc, lurker, strength 6, vitality 2; a skirmish ability that removes (2), used only on purpose
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, strength 8, vitality 2; its trigger is at the start of an assignment phase
					put("gauntlets", "11_109"); // Bladed Gauntlets: on an [orc] minion, "Bearer is a lurker"; a skirmish ability used only on purpose
					put("horror", "14_13");     // Horror of Harad: [orc] Half-troll, strength 9, vitality 3; ambush (1), damage +1, fierce, lurker, muster, toil 2
					put("column", "11_72");     // Column of Easterlings: [men] Man, lurker, strength 8, vitality 2; a skirmish ability used only on purpose
					put("denizen", "11_115");   // Denizen of Khazad-dûm: [orc] Orc, vitality 2, no lurker; +2 only at an underground site
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3, damage +1
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; needs a Dwarf to play, so cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CaveTrollofMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Cave Troll of Moria, Savage Menace
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 12
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>. <b>Toil 2</b>. <helper>(For each [orc] character you exert when playing this, its twilight cost is -2.)</helper><br>Each time an [orc] lurker wins a skirmish, you may make the Free Peoples player discard the top 2 cards of his or her draw deck.
		*/

		var scn = GetScenario();

		var troll = scn.GetFreepsCard("troll");

		assertEquals("Cave Troll of Moria", troll.getBlueprint().getTitle());
		assertEquals("Savage Menace", troll.getBlueprint().getSubtitle());
		assertTrue(troll.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, troll.getBlueprint().getSide());
		assertEquals(Culture.ORC, troll.getBlueprint().getCulture());
		assertEquals(CardType.MINION, troll.getBlueprint().getCardType());
		assertEquals(Race.TROLL, troll.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(troll, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(troll, Keyword.TOIL));
		assertEquals(12, troll.getBlueprint().getTwilightCost());
		assertEquals(15, troll.getBlueprint().getStrength());
		assertEquals(4, troll.getBlueprint().getVitality());
		assertEquals(4, troll.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachOrcCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var denizen = scn.GetShadowCard("denizen");
		var prowler = scn.GetShadowCard("prowler");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SetShadowHand(troll);
		scn.MoveMinionsToTable(denizen, prowler, troop);
		assertInHand(troll);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(prowler));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// At site 2 the Troll (site 4) roams: 12 + 2 roaming - 2 - 2 is the least it can cost, with both [orc] Orcs
		// exerted (exertions never pay the roaming penalty); the [moria] Troop does not count.
		// Toil 1 would want 12, and toil 3 (or the Troop counted) would let 8 do.
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(troll));
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(troll);
		// The pool is 2 short of its 14: one exertion is owed, and the second is the Shadow player's to make or not.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, prowler));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(denizen);
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(prowler));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 12 - (12 + 2 - 2)
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenAnOrcOrcLurkerWinsTheFreePeoplesPlayerMayBeMadeToDiscardTheTopTwoCardsOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var prowler = scn.GetShadowCard("prowler");
		var guard = scn.GetFreepsCard("guard");
		var top1 = scn.GetFreepsCard("column");
		var top2 = scn.GetFreepsCard("denizen");
		var third = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, prowler);
		scn.SetFreepsDrawDeck(top1, top2, third);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(top1, scn.GetFreepsTopOfDeck());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, prowler);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		// 6 against the Dwarf Guard's 4
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(top1, top2);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(third, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetWoundsOn(guard));
		// The fierce Troll brings a fierce assignment phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnOrcMinionMadeALurkerByAnotherCardWinningIsAnOfferTooAndDecliningLeavesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var halftroll = scn.GetShadowCard("halftroll");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var guard = scn.GetFreepsCard("guard");
		var top1 = scn.GetFreepsCard("column");
		var top2 = scn.GetFreepsCard("denizen");
		var third = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, halftroll);
		scn.AttachCardsTo(halftroll, gauntlets);
		scn.SetFreepsDrawDeck(top1, top2, third);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertTrue(scn.HasKeyword(halftroll, Keyword.LURKER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, halftroll);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		// 8 against the Dwarf Guard's 4: double, and he is killed, the Half-troll winning all the same
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertEquals(top2, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenTheOrcLurkerLosesItsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var prowler = scn.GetShadowCard("prowler");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, prowler);
		assertTrue(scn.GetFreepsDeckCount() > 2);
		int deck = scn.GetFreepsDeckCount();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, prowler);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn's 8 against 6: the Prowling Orc loses and lives, with 1 wound of 2
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(prowler));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenALurkerOfAnotherCultureOrThisTrollWhoIsNoLurkerWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var column = scn.GetShadowCard("column");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, column);
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, troll},
				new PhysicalCardImpl[]{gimli, column});
		// The Column is a lurker: the Troll's skirmish comes first.
		scn.FreepsResolveSkirmish(aragorn);
		// 15 against Aragorn's 8: the Troll, an [orc] minion who is no lurker, wins
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertEquals(deck, scn.GetFreepsDeckCount());

		scn.FreepsResolveSkirmish(gimli);
		// 8 against Gimli's 6: the Column, a [men] lurker, wins
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AFierceOrcLurkerWinningTwiceInATurnMakesTwoOffers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var horror = scn.GetShadowCard("horror");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, horror);
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 4);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, horror);
		// Ambush (1): the Shadow player's own offer, declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(horror));
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		// 9 against Gimli's 6
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		// The fierce round: the Horror against Aragorn, the Troll left unassigned
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, horror);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(horror));
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// 9 against Aragorn's 8
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenTheFreePeoplesDrawDeckIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var prowler = scn.GetShadowCard("prowler");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, prowler);
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, prowler);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		// 6 against the Dwarf Guard's 4: the [orc] lurker wins, and there is nothing to discard
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
