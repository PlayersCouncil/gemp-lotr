package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.TakeControlOfASiteEffect;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_031_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trail", "102_31");

					// Down to the Last Child: an [isengard] condition.  "Shadow: Remove 2 [isengard] tokens from
					// this card to take control of a site."  It gains a token when a companion loses a skirmish
					// involving an Uruk-hai, which the one test that uses it never comes to.
					put("child", "4_148");
					// Devilry of Orthanc: an [isengard] machine, a condition.  Its abilities need an Uruk-hai
					// to exert in the Shadow phase, or 8 tokens.
					put("devilry", "5_49");
					// Attack on Helm's Deep: an [isengard] condition that is no machine.  Its abilities are a
					// Shadow one that plays an Uruk-hai and a regroup one that takes 3 tokens.
					put("attack", "4_137");
					// Raiders From the East: a [raider] condition, whose tokens are [raider] tokens.  Its
					// abilities need a [raider] Man, or are used in the maneuver phase.
					put("raiders", "4_242");

					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] Uruk-hai, strength 8, vitality 2; Damage +1 is its whole text
					put("worker", "3_62");      // Isengard Worker: an [isengard] Orc, strength 5, vitality 3; a regroup ability

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; Damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A format in which a skirmish involving the Ring-bearer may be cancelled.  The default one does not allow it.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trail", "102_31");
					put("devilry", "5_49");     // Devilry of Orthanc: an [isengard] condition to hold the tokens
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void TrailofSavageryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Trail of Savagery
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each site you control gains <b>battleground</b>.
		 * Skirmish: Remove 2 [isengard] tokens to cancel a skirmish involving an Uruk-hai.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trail");

		assertEquals("Trail of Savagery", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForOneToTheSupportAreaItMakesEachSiteItsOwnerControlsABattlegroundAndNoOtherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var child = scn.GetShadowCard("child");

		// The Free Peoples player's deck is emptied, so that he draws nothing on the way to site 4 and never
		// has a card to play: the cheat that gives him a site works only when he has no action to take.
		scn.MoveCardsToFreepsDiscard("trail", "child", "devilry", "attack", "raiders", "savage", "sentry", "worker", "aragorn", "gimli");

		scn.StartGame();
		// Both fellowships are at site 4, so sites 1 to 3 may be taken.
		scn.SkipToSite(4);

		// Site 1 is from the Free Peoples player's adventure deck and sites 2 to 5 from the Shadow player's.
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);

		scn.MoveCardsToSupportArea(child);
		scn.AddTokensToCard(child, 4);
		scn.MoveCardsToHand(trail);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 set + 1 companion + 6 for site 5.
		assertEquals(17, scn.GetTwilight());
		var site5 = scn.GetCurrentSite();
		assertEquals(5, scn.GetCurrentSiteNumber());

		// The Shadow player takes site 1 before the condition is played.
		scn.ShadowUseCardAction(child);
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site4, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site5, Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowPlayAvailable(trail));

		scn.ShadowPlayCard(trail);
		assertEquals(17 - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, trail);
		assertTrue(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		// Not a site nobody controls, and not the fellowship's own.
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site4, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site5, Keyword.BATTLEGROUND));

		// A site taken while the condition is in play becomes one as it is taken.
		scn.ShadowUseCardAction(child);
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site3, Keyword.BATTLEGROUND));

		// With no minion in play the game goes to the regroup phase.  There the other player is given site 3,
		// as he might have taken it on a turn when he was the Shadow player: it is controlled, and not by
		// the owner of this card.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsExecuteAdHocEffect(new TakeControlOfASiteEffect(null, VirtualTableScenario.P1)); // lint-ok: the rig's own cheat for taking a site is the Shadow player's only
		assertTrue(scn.FreepsControls(site3));
		assertFalse(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site4, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site5, Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RemovesTwoIsengardTokensFromWhicheverCardsHoldThemToCancelASkirmishInvolvingAnUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var devilry = scn.GetShadowCard("devilry");
		var attack = scn.GetShadowCard("attack");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(trail, devilry, attack, raiders);
		scn.AddTokensToCard(devilry, 2);
		scn.AddTokensToCard(attack, 2);
		scn.AddTokensToCard(raiders, 3);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not an assignment action, with the tokens there and both Uruk-hai on the table.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(trail));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{gimli, sentry});

		// Aragorn against an [isengard] Uruk-hai.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.ShadowActionAvailable(trail));
		assertEquals(2, scn.GetCultureTokensOn(devilry));
		assertEquals(2, scn.GetCultureTokensOn(attack));
		assertEquals(3, scn.GetCultureTokensOn(raiders));

		scn.ShadowUseCardAction(trail);
		// The tokens are taken one at a time from any [isengard] card that has one, machine or not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(devilry, attack));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders, trail));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(attack);
		assertEquals(1, scn.GetCultureTokensOn(attack));
		assertTrue(scn.ShadowHasCardChoiceAvailable(devilry, attack));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders));
		scn.ShadowChooseCard(devilry);

		assertEquals(1, scn.GetCultureTokensOn(devilry));
		assertEquals(1, scn.GetCultureTokensOn(attack));
		assertEquals(3, scn.GetCultureTokensOn(raiders));
		// The skirmish is over with nobody having won or lost it.
		assertFalse(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharAssigned(savage));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(savage));
		// Gimli's is untouched.
		assertTrue(scn.IsCharAssignedAgainst(gimli, sentry));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Gimli against a [sauron] Uruk-hai, with one token left on each of two cards.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, sentry));
		assertTrue(scn.ShadowActionAvailable(trail));

		scn.ShadowUseCardAction(trail);
		scn.ShadowChooseCard(devilry);
		assertEquals(0, scn.GetCultureTokensOn(devilry));
		assertEquals(0, scn.GetCultureTokensOn(attack));
		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertFalse(scn.IsCharSkirmishing(sentry));
		assertFalse(scn.IsCharAssigned(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneIsengardTokenWhateverOtherTokensThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var devilry = scn.GetShadowCard("devilry");
		var attack = scn.GetShadowCard("attack");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(trail, devilry, attack, raiders);
		scn.AddTokensToCard(devilry, 1);
		scn.AddTokensToCard(raiders, 3);
		// The Free Peoples player's own Devilry of Orthanc lies in his support area with 3 tokens.  On his
		// turn it is not active, and tokens on it are ignored.
		var otherDevilry = scn.GetFreepsCard("devilry");
		scn.MoveCardsToSupportArea(otherDevilry);
		scn.AddTokensToCard(otherDevilry, 3);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertFalse(scn.ShadowActionAvailable(trail));

		// The skirmish is fought: Aragorn wins, 8 against 5.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetCultureTokensOn(devilry));
		assertEquals(0, scn.GetCultureTokensOn(attack));
		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertEquals(3, scn.GetCultureTokensOn(otherDevilry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InASkirmishWithNoUrukHaiTheCostIsPaidAndNothingIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var devilry = scn.GetShadowCard("devilry");
		scn.MoveCardsToSupportArea(trail, devilry);
		scn.AddTokensToCard(devilry, 2);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, worker);
		scn.SkipToAssignments();
		// An Uruk-hai is assigned to Aragorn; Gimli's skirmish with an [isengard] Orc is fought first.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{gimli, worker});
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(gimli, worker));
		assertEquals(2, scn.GetCultureTokensOn(devilry));
		// An ability with a cost is offered whenever the cost can be paid.
		assertTrue(scn.ShadowActionAvailable(trail));

		scn.ShadowUseCardAction(trail);
		assertEquals(0, scn.GetCultureTokensOn(devilry));
		// Nothing is cancelled: not this skirmish, and not the Uruk-hai's, which has not begun.
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, worker));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gimli wins, 6 against 5: one wound and one more for his damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(worker));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void InFellowshipBlockCancelsASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var trail = scn.GetShadowCard("trail");
		var devilry = scn.GetShadowCard("devilry");
		scn.MoveCardsToSupportArea(trail, devilry);
		scn.AddTokensToCard(devilry, 2);

		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, savage);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertTrue(scn.ShadowActionAvailable(trail));
		assertEquals(2, scn.GetCultureTokensOn(devilry));

		scn.ShadowUseCardAction(trail);
		assertEquals(0, scn.GetCultureTokensOn(devilry));
		assertFalse(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathPaysItsCostAndDoesNotCancelASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var devilry = scn.GetShadowCard("devilry");
		scn.MoveCardsToSupportArea(trail, devilry);
		scn.AddTokensToCard(devilry, 2);

		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, savage);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		// The ability has a cost that can be paid, so it is offered; its cancel is what the format refuses.
		assertTrue(scn.ShadowActionAvailable(trail));
		assertEquals(2, scn.GetCultureTokensOn(devilry));

		scn.ShadowUseCardAction(trail);
		assertEquals(0, scn.GetCultureTokensOn(devilry));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish is fought and has a loser: Frodo, 3 + 1 for the Ring against 5.  He takes one wound
		// and one more for the Savage's damage +1, and is offered the Ring for each.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
