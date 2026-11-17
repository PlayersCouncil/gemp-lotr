package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hook", "18_62");
					put("hook2", "18_62");

					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10, vitality 3; fierce only at a battleground (none on this path)
					put("corps2", "11_73");
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text
					put("marksman", "1_176");   // Goblin Marksman: an archer minion, Archer its whole text
					put("stragglers", "13_99"); // Stragglers: [men] support condition that takes a [men] token as it is played
					put("fleet", "18_66");      // Fleet of Corsair Ships: [men] support possession that may take a [men] token as it is played
					put("threatening", "13_103"); // Always Threatening: [orc] support condition that carries [orc] tokens

					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, strength 4, vitality 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ALLY, strength 4, vitality 2; takes part in skirmishes by his own maneuver ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hook", "18_62");
					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10; fierce only at a battleground
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; overwhelmed by the Corps
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairGrapplingHookStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Corsair Grappling Hook
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [men] minion.
		 * 	When you play this possession you may reinforce a [men] token twice.
		 * 	Each time a companion is killed during a skirmish phase, you may control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hook");

		assertEquals("Corsair Grappling Hook", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAMenMinionAndWithNoMenTokenInPlayNoReinforcementIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hook = scn.GetShadowCard("hook");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var fleet = scn.GetShadowCard("fleet");
		var threatening = scn.GetShadowCard("threatening");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveCardsToHand(hook);
		scn.MoveMinionsToTable(corps, ted, brigand);
		// A [men] card with no token, and tokens of another culture.
		scn.MoveCardsToSupportArea(fleet, threatening);
		scn.AddTokensToCard(threatening, 2);
		scn.SetTwilight(5);
		assertInZone(Zone.HAND, hook);
		assertEquals(0, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(hook));
		scn.ShadowPlayCard(hook);
		// [men] minions of two races are offered; a Man minion of another culture is not, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(corps);

		assertAttachedTo(hook, corps);
		// Nothing a yes could reinforce: no offer (Q-0224).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hook));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
	}

	@Test
	public void WhenPlayedItMayReinforceAMenTokenTwiceBothOnTheCardChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hook = scn.GetShadowCard("hook");
		var corps = scn.GetShadowCard("corps");
		var stragglers = scn.GetShadowCard("stragglers");
		var fleet = scn.GetShadowCard("fleet");
		var threatening = scn.GetShadowCard("threatening");

		scn.StartGame();
		scn.MoveCardsToHand(hook);
		scn.MoveMinionsToTable(corps);
		scn.MoveCardsToSupportArea(stragglers, fleet, threatening);
		scn.AddTokensToCard(stragglers, 1);
		scn.AddTokensToCard(fleet, 1);
		scn.AddTokensToCard(threatening, 2);
		scn.SetTwilight(5);
		assertInZone(Zone.HAND, hook);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		scn.FreepsPass();

		// Corps of Harad is the one bearer: the Hook goes on him unasked.
		scn.ShadowPlayCard(hook);
		assertAttachedTo(hook, corps);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hook));
		scn.ShadowAcceptOptionalTrigger();

		// The first token: either [men] card with a token, and not the [orc] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stragglers, fleet));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(threatening));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(stragglers);

		// "Twice" is two tokens on the one card chosen (F-0113): nothing more is asked.
		assertEquals(3, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheReinforcementLeavesTheTokensAndAnotherHookPlayedAfterItGetsNoOfferFromIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hook = scn.GetShadowCard("hook");
		var hook2 = scn.GetShadowCard("hook2");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var stragglers = scn.GetShadowCard("stragglers");

		scn.StartGame();
		scn.MoveCardsToHand(hook, hook2);
		scn.MoveMinionsToTable(corps, ted);
		scn.MoveCardsToSupportArea(stragglers);
		scn.AddTokensToCard(stragglers, 1);
		scn.SetTwilight(6);
		assertInZone(Zone.HAND, hook, hook2);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		scn.FreepsPass();

		scn.ShadowPlayCard(hook);
		scn.ShadowChooseCard(corps);
		assertAttachedTo(hook, corps);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hook));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [men] possession played after it: only its own copy of the text answers it.
		// The Corps bears a hand weapon already, so Ted Sandyman is the one bearer and is taken unasked.
		scn.ShadowPlayCard(hook2);
		assertAttachedTo(hook2, ted);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hook2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hook));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCompanionKilledInASkirmishPhaseOffersASiteAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hook = scn.GetShadowCard("hook");
		var corps = scn.GetShadowCard("corps");
		var corps2 = scn.GetShadowCard("corps2");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// Both markers on site 4: sites 1, 2 and 3 are behind every marker as the fellowship moves on to site 5.
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveCompanionsToTable(guard, aragorn);
		// Aragorn has one vitality left.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(corps, corps2);
		scn.AttachCardsTo(corps, hook);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertAttachedTo(hook, corps);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, corps}, new PhysicalCardImpl[]{aragorn, corps2});
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// A [dwarven] Dwarf, overwhelmed by the bearer: 10 + 1 against 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hook));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// A [gondor] Man, killed by the wound for losing to the other Corps (10 against 8), who bears no Hook.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hook));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheSiteOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hook = scn.GetShadowCard("hook");
		var corps = scn.GetShadowCard("corps");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, hook);
		assertAttachedTo(hook, corps);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, corps);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hook));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledInTheArcheryPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hook = scn.GetShadowCard("hook");
		var corps = scn.GetShadowCard("corps");
		var marksman = scn.GetShadowCard("marksman");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(corps, marksman);
		scn.AttachCardsTo(corps, hook);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertAttachedTo(hook, corps);

		// The Goblin Marksman's archery wound kills the Guard in the archery phase.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hook));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void AnAllyKilledInASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hook = scn.GetShadowCard("hook");
		var corps = scn.GetShadowCard("corps");
		var thrarin = scn.GetFreepsCard("thrarin");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, hook);
		assertAttachedTo(hook, corps);

		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, corps);
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		// 10 + 1 against 4: the ally is overwhelmed, and he is no companion.
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hook));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void WithASiteThatCanBeControlledACompanionKilledInASkirmishPhaseOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var hook = scn.GetShadowCard("hook");
		var corps = scn.GetShadowCard("corps");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, hook);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, corps);
		scn.FreepsResolveSkirmish(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 10 + 1 for the Hook against 4: the Guard is overwhelmed.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hook));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledACompanionKilledInASkirmishPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var hook = scn.GetShadowCard("hook");
		var corps = scn.GetShadowCard("corps");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, hook);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, corps);
		scn.FreepsResolveSkirmish(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hook));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}
}
