package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_033_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  The only battleground of the path is its site 9, Battle of the Five Armies 30_57; elsewhere Watchful
	// Orc's Shadow ability makes the sites battlegrounds for the turn.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("worms", "32_33");
					// [moria] minions of two races, and a minion of another culture, to win skirmishes.
					put("goblin", "31_36");      // The Great Goblin, Chieftain of the Misty Mountains: [moria] Orc, fierce, strength 10; a Maneuver ability, not used
					put("bridging", "33_36");    // Bridging Troll: [moria] Troll, strength 10, vitality 3; a Skirmish ability, not used
					put("marauder", "30_38");    // Orkish Marauder: [gundabad] Orc, strength 8; texts that need 4 followers or 7 companions
					// Exerts twice to make each site on the path gain battleground until the end of the turn.
					put("watchful", "30_40");    // Watchful Orc: [gundabad] Orc, vitality 3; its "When you play" text is never met (cheated in)
					// Companions for the skirmishes, each of strength 7 and vitality 3.
					put("balin", "30_2");        // Balin, Brother of Dwalin: a Fellowship ability, not used
					put("dwalin", "30_9");       // Dwalin, Brother of Balin: damage +1; a Maneuver ability, not used
					put("nori", "30_15");        // Nori, Of the Blue Mountains: a Maneuver ability that needs a follower, not used
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WereWormsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Were-worms
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a [moria] minion wins a skirmish, you may place a [moria] token here.<br><b>Shadow:</b>
		 * If the fellowship is at a battleground, discard this condition to add (2) for each [moria] token here
		 * (limit (8)).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("worms");

		assertEquals("Were-worms", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeAMoriaMinionWinsASkirmishATokenMayBePlacedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worms = scn.GetShadowCard("worms");
		var goblin = scn.GetShadowCard("goblin");
		var bridging = scn.GetShadowCard("bridging");
		var marauder = scn.GetShadowCard("marauder");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCardsToSupportArea(worms);
		scn.MoveCompanionsToTable(balin, dwalin, nori);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, bridging, marauder);
		assertInZone(Zone.SUPPORT, worms);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin, bridging, marauder);
		assertEquals(0, scn.GetCultureTokensOn(worms));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, goblin}, new PhysicalCardImpl[]{nori, marauder},
				new PhysicalCardImpl[]{dwalin, bridging});

		// The Great Goblin, a [moria] Orc (10), beats Balin (7).
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worms));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(worms));

		// A [gundabad] Orc (8) beats Nori (7): nothing offered.
		scn.FreepsResolveSkirmish(nori);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(nori));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(worms));

		// A [moria] Troll (10) beats Dwalin (7): offered, and declined.
		scn.FreepsResolveSkirmish(dwalin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worms));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(worms));

		// The Great Goblin is fierce and wins a second time: offered again.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(dwalin, goblin);
		scn.FreepsResolveSkirmish(dwalin);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worms));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(worms));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenAMoriaMinionLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worms = scn.GetShadowCard("worms");
		var bridging = scn.GetShadowCard("bridging");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCardsToSupportArea(worms);
		scn.MoveCompanionsToTable(balin);
		// Balin 7 + 4 = 11 beats the Troll (10), who lives through the one wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 4));

		scn.StartGame();
		scn.MoveMinionsToTable(bridging);
		assertInZone(Zone.SUPPORT, worms);
		assertEquals(0, scn.GetCultureTokensOn(worms));
		assertEquals(0, scn.GetWoundsOn(bridging));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, bridging);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(bridging));
		assertInZone(Zone.SHADOW_CHARACTERS, bridging);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(worms));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowAtABattlegroundDiscardsItToAdd2ForEachToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worms = scn.GetShadowCard("worms");
		var watchful = scn.GetShadowCard("watchful");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(worms);
		scn.AddTokensToCard(worms, 3);
		scn.MoveMinionsToTable(watchful);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, worms);
		assertEquals(3, scn.GetCultureTokensOn(worms));

		// The Troll Hoard (site 2: forest, underground) is made a battleground.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(10, scn.GetTwilight());

		assertTrue(scn.ShadowActionAvailable(worms));
		scn.ShadowUseCardAction(worms);
		assertInZone(Zone.DISCARD, worms);
		// 10 + 2 x 3
		assertEquals(16, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoTokenItIsOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worms = scn.GetShadowCard("worms");
		var watchful = scn.GetShadowCard("watchful");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(worms);
		scn.MoveMinionsToTable(watchful);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, worms);
		assertEquals(0, scn.GetCultureTokensOn(worms));

		scn.ShadowUseCardAction(watchful);
		scn.ShadowChoose("Battleground");
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		assertTrue(scn.ShadowActionAvailable(worms));
		scn.ShadowUseCardAction(worms);
		assertInZone(Zone.DISCARD, worms);
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtBattleOfTheFiveArmiesFiveTokensAddNoMoreThan8() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worms = scn.GetShadowCard("worms");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(worms);
		scn.AddTokensToCard(worms, 5);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, worms);
		assertEquals(5, scn.GetCultureTokensOn(worms));

		// Site 9, a printed battleground (and mountain).
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		assertTrue(scn.ShadowActionAvailable(worms));
		scn.ShadowUseCardAction(worms);
		assertInZone(Zone.DISCARD, worms);
		// 10 + 8: 2 x 5 is 10, held to the limit of 8
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowIsNotOfferedAwayFromABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worms = scn.GetShadowCard("worms");
		var watchful = scn.GetShadowCard("watchful");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(worms);
		scn.AddTokensToCard(worms, 3);
		scn.MoveMinionsToTable(watchful);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, worms);
		assertEquals(3, scn.GetCultureTokensOn(worms));

		// Watchful Orc's ability is not used: The Troll Hoard is no battleground.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(worms));
	}
}
