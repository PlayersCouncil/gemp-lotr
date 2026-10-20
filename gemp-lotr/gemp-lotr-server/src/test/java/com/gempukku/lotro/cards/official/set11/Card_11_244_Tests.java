package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_11_244_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: [gandalf] Wizard companion, strength 7
					put("ent", "6_27");           // Ent Avenger: [gandalf] Ent companion, strength 6; +2 only beside 3 Ents
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					// Radagast, The Brown: a [gandalf] Wizard who is an ALLY, strength 8; a fellowship ability used
					// only on purpose
					put("radagast", "31_17");
					// One Whom Men Would Follow: "Exert Aragorn to spot an ally. Until the regroup phase, that ally is
					// strength +2 and participates in archery fire and skirmishes."
					put("follow", "1_109");

					put("mallorn", "1_54");       // Mallorn-trees: the Free Peoples player's own condition, support area
					put("coat", "1_101");         // Coat of Mail: a Free Peoples possession borne by a Man
					put("weary", "4_212");        // Weary: a Shadow condition on a companion; its trigger needs an [isengard] tracker
					put("armory", "1_173");       // Goblin Armory: a Shadow condition in the support area
					put("poison", "8_28");        // Spider Poison: a Shadow possession in the support area

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
					put("savage2", "1_151");
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
					put("pursuer", "4_190");      // Uruk Pursuer: strength 5, vitality 3, printed fierce
				}},
				new HashMap<>()
				{{
					// Fangorn Glade: adds twilight for Free Peoples weapons when the fellowship moves to it
					put("site1", "11_239");
					put("site2", "11_244");
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
	public void HeightsofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Heights of Isengard
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: Battleground. Each time a [gandalf] companion wins a skirmish, the Free Peoples player may
		 * 	discard a condition from play.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site2");

		assertEquals("Heights of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void AGandalfCompanionWinningLetsTheFreePeoplesPlayerDiscardAConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mallorn = scn.GetFreepsCard("mallorn");
		var coat = scn.GetFreepsCard("coat");
		var weary = scn.GetShadowCard("weary");
		var armory = scn.GetShadowCard("armory");
		var poison = scn.GetShadowCard("poison");
		var savage = scn.GetShadowCard("savage");
		var heights = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(aragorn, coat, weary);
		scn.MoveCardsToSupportArea(mallorn, armory, poison);

		scn.StartGame(scn.GetFreepsSite("site1"));

		// The Shadow player lays the next site, and the fellowship moves to it.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(heights);
		assertSame(heights, scn.GetCurrentSite());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, savage);
		// 7 against 5: a [gandalf] companion wins.  The site is the Shadow player's card, and it is the Free
		// Peoples player who is asked.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(heights));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, mallorn, armory, poison);
		assertInZone(Zone.ATTACHED, weary, coat);

		scn.FreepsAcceptOptionalTrigger();
		// Conditions of both sides, in a support area or borne, are offered; possessions of either side are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(mallorn, weary, armory));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison, coat));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(weary);
		assertInDiscard(weary);
		assertInZone(Zone.SUPPORT, mallorn, armory, poison);
		assertInZone(Zone.ATTACHED, coat);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnEntAvengerWinningSetsItOffTooAndDecliningDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ent = scn.GetFreepsCard("ent");
		var mallorn = scn.GetFreepsCard("mallorn");
		var armory = scn.GetShadowCard("armory");
		var savage = scn.GetShadowCard("savage");
		var heights = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(ent);
		scn.MoveCardsToSupportArea(mallorn, armory);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(heights);
		assertSame(heights, scn.GetCurrentSite());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(ent, savage);
		// 6 against 5: a [gandalf] companion who is no Wizard wins.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(heights));
		assertInZone(Zone.SUPPORT, mallorn, armory);

		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, mallorn, armory);
		// Nor is the Shadow player offered it once the Free Peoples player has declined.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GandalfWinningHisSkirmishAndThenHisFierceSkirmishIsOfferedEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var mallorn = scn.GetFreepsCard("mallorn");
		var armory = scn.GetShadowCard("armory");
		var pursuer = scn.GetShadowCard("pursuer");
		var heights = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(mallorn, armory);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(heights);

		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, pursuer);
		// 7 against 5.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(heights));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(armory);
		assertInDiscard(armory);
		assertInZone(Zone.SUPPORT, mallorn);

		// The Pursuer, fierce and alive with one wound, is fought again.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.InFierceRound());
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// The second win sets it off again; the one condition left is discarded.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(heights));
		scn.FreepsAcceptOptionalTrigger();
		assertInDiscard(mallorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenGandalfWinsAndNoConditionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var coat = scn.GetFreepsCard("coat");
		var poison = scn.GetShadowCard("poison");
		var savage = scn.GetShadowCard("savage");
		var heights = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		// Two possessions, one of each side, and no condition of either.
		scn.AttachCardsTo(aragorn, coat);
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(heights);
		assertSame(heights, scn.GetCurrentSite());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, savage);
		// 7 against 5: a [gandalf] companion wins, but there is no condition to discard.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, poison);
		assertInZone(Zone.ATTACHED, coat);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenGandalfLosesOrWhenAGondorCompanionOrAGandalfAllyWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var radagast = scn.GetFreepsCard("radagast");
		var follow = scn.GetFreepsCard("follow");
		var mallorn = scn.GetFreepsCard("mallorn");
		var armory = scn.GetShadowCard("armory");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var troop = scn.GetShadowCard("troop");
		var heights = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(follow);
		scn.MoveCardsToSupportArea(mallorn, armory);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(heights);
		assertSame(heights, scn.GetCurrentSite());

		scn.MoveMinionsToTable(savage, savage2, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Aragorn exerts, and Radagast is strength 10 and fights.
		scn.FreepsPlayCard(follow);
		assertEquals(10, scn.GetStrength(radagast));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, savage },
				new PhysicalCardImpl[] { radagast, savage2 },
				new PhysicalCardImpl[] { gandalf, troop }
		);

		// A [gondor] companion wins: 8 against 5.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// A [gandalf] Wizard who is an ally wins: 10 against 5, which overwhelms the Savage.
		scn.FreepsResolveSkirmish(radagast);
		scn.PassSkirmishActions();
		assertInDiscard(savage2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Gandalf loses: 7 against 13.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, mallorn, armory);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
