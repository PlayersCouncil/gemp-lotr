package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import com.gempukku.lotro.logic.modifiers.StrengthModifier;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wildman", "15_91");
					put("wildman2", "15_91");
					// Tracking Uruk: [uruk-hai] Uruk-hai; hunter 1 is its whole text
					put("tracking", "15_171");
					put("corps", "11_73");      // Corps of Harad: a [men] Man, vitality 3; fierce only at a battleground (none here)
					put("footman", "11_82");    // Footman of Dunland: a [men] Man, vitality 3; the current site gains plains
					// Ted Sandyman: a [men] minion who is a Hobbit, no Man, vitality 4; his abilities are not used
					put("ted", "18_75");
					// Easterling Axeman: a [raider] Man, vitality 2; strength +3 and fierce only while 2 burdens can be spotted
					put("axeman", "4_224");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, no hunter
					put("faramir", "18_48");    // Faramir, Captain of Ithilien: a companion, ranger, hunter 2
					// Rohan Worker: a Free Peoples follower, Aid (1); its texts answer a transfer and its bearer's win
					put("worker", "15_135");
					put("army", "18_47");       // Elendil's Army: a Free Peoples follower, strength +2 to its bearer
					// Saruman, Servant of Sauron: a Shadow follower; his text answers his bearer's win
					put("saruman", "17_38");
					put("barliman", "1_70");    // Barliman Butterbur: an ally, no follower
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RavagingWildManStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Ravaging Wild Man
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a hunter, this minion gains <b>hunter 1</b>.<br>Each time this minion wins a skirmish, you may exert 2 [men] Men to discard a follower.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wildman");

		assertEquals("Ravaging Wild Man", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ItSpotsItselfAndKeepsHunter1WhenTheOtherHunterLeavesPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var tracking = scn.GetShadowCard("tracking");

		scn.StartGame();
		// Alone, it has no hunter to spot.
		scn.MoveMinionsToTable(wildman);
		assertEquals(0, scn.GetKeywordCount(wildman, Keyword.HUNTER));

		scn.MoveMinionsToTable(tracking);
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));

		// The other hunter goes.  Hunter by its own text, the Wild Man is itself a hunter it can spot, and keeps
		// hunter 1 (Q-0290: "A, yeah, same deal" as Sentry Uruk 15_170, Q-0276).
		scn.MoveCardsToDiscard(tracking);
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(wildman, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHunterCompanionPlayedGivesItHunter1AndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(faramir);

		scn.StartGame();
		// The minions are laid early, to read the Wild Man before and after a companion is played.
		scn.MoveMinionsToTable(wildman, corps);
		scn.FreepsRefreshDecision();

		assertFalse(scn.HasKeyword(wildman, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(wildman, Keyword.HUNTER));

		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertEquals(2, scn.GetKeywordCount(faramir, Keyword.HUNTER));
		// Hunter 1, whatever the number of the hunter spotted; the other [men] Man gains nothing.
		assertTrue(scn.HasKeyword(wildman, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(corps, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheHunter1ItKeepsMakesItStrengthPlus1AgainstACompanionWhoIsNoHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var tracking = scn.GetShadowCard("tracking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(wildman, tracking);
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		// The other hunter goes before the skirmishes; the Wild Man spots itself.
		scn.MoveCardsToDiscard(tracking);
		assertInDiscard(tracking);
		scn.ShadowRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, wildman);
		scn.FreepsResolveSkirmish(aragorn);

		// Strength 10 + 1 for hunter 1 against Aragorn, who is no hunter.
		assertEquals(1, scn.GetKeywordCount(wildman, Keyword.HUNTER));
		assertEquals(11, scn.GetStrength(wildman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WinningASkirmishOffersToExertTwoMenMenToDiscardAFollowerOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");
		var footman = scn.GetShadowCard("footman");
		var ted = scn.GetShadowCard("ted");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetFreepsCard("worker");
		var army = scn.GetFreepsCard("army");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(wildman, corps, footman, ted, axeman);
		scn.SkipToAssignments();
		// The followers are laid after the maneuver phase, past their Aid offers: one in the Free Peoples support
		// area, one borne by Frodo, and the Shadow player's own in his support area.
		scn.MoveCardsToSupportArea(worker, saruman);
		scn.AttachCardsTo(frodo, army);
		assertInZone(Zone.SUPPORT, worker, saruman);
		assertAttachedTo(army, frodo);

		scn.FreepsAssignToMinions(aragorn, wildman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Strength 10 against 8: the Wild Man wins.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wildman));
		scn.ShadowAcceptOptionalTrigger();

		// "Exert 2 [men] Men": the Wild Man itself and the two other [men] Men; not the [men] Hobbit nor the
		// [raider] Man, each of whom could exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(wildman, corps, footman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ted, axeman));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(corps, footman);
		assertEquals(1, scn.GetWoundsOn(corps));
		assertEquals(1, scn.GetWoundsOn(footman));
		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(axeman));

		// "A follower": of either side, borne or not; the ally is no follower.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, army, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barliman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(army);

		assertInDiscard(army);
		assertInZone(Zone.SUPPORT, worker, saruman);
		assertInZone(Zone.SUPPORT, barliman);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobodyAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(wildman, corps);
		scn.SkipToAssignments();
		scn.MoveCardsToSupportArea(worker);
		assertInZone(Zone.SUPPORT, worker);

		scn.FreepsAssignToMinions(aragorn, wildman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wildman));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetWoundsOn(corps));
		assertInZone(Zone.SUPPORT, worker);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");
		var footman = scn.GetShadowCard("footman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(wildman, corps, footman);
		scn.SkipToAssignments();
		scn.MoveCardsToSupportArea(worker);
		// Aragorn made strength 11, to beat the Wild Man's 10 by one: it takes one wound and lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		assertInZone(Zone.SUPPORT, worker);
		assertEquals(11, scn.GetStrength(aragorn));

		scn.FreepsAssignToMinions(aragorn, wildman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(wildman));
		assertInZone(Zone.SHADOW_CHARACTERS, wildman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wildman));
		assertEquals(0, scn.GetWoundsOn(corps));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertInZone(Zone.SUPPORT, worker);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyOneMenManAbleToExertItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(wildman, corps, axeman);
		// Corps of Harad, vitality 3, is exhausted: the Wild Man is the only [men] Man who can exert, the [raider]
		// Man beside him able to but no [men] Man.
		scn.AddWoundsToChar(corps, 2);
		scn.SkipToAssignments();
		scn.MoveCardsToSupportArea(worker);
		assertEquals(2, scn.GetWoundsOn(corps));
		assertInZone(Zone.SUPPORT, worker);

		scn.FreepsAssignToMinions(aragorn, wildman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wildman));
		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertInZone(Zone.SUPPORT, worker);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoFollowerInPlayItIsOfferedAndTheMenExertForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");
		var footman = scn.GetShadowCard("footman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(wildman, corps, footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, wildman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wildman));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(corps, footman);

		assertEquals(1, scn.GetWoundsOn(corps));
		assertEquals(1, scn.GetWoundsOn(footman));
		assertInZone(Zone.SUPPORT, barliman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASecondWinInTheSameTurnOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");
		var footman = scn.GetShadowCard("footman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetFreepsCard("worker");
		var army = scn.GetFreepsCard("army");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(wildman, corps, footman);
		scn.SkipToAssignments();
		scn.MoveCardsToSupportArea(worker, army);
		assertInZone(Zone.SUPPORT, worker, army);

		scn.FreepsAssignToMinions(aragorn, wildman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wildman));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(corps, footman);
		scn.ShadowChooseCard(worker);
		assertInDiscard(worker);
		assertEquals(1, scn.GetWoundsOn(corps));
		assertEquals(1, scn.GetWoundsOn(footman));

		// A regroup move to site 3, and a second assignment phase with the same minions.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, wildman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wildman));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(corps, footman);
		assertEquals(2, scn.GetWoundsOn(corps));
		assertEquals(2, scn.GetWoundsOn(footman));
		// The one follower left is taken unasked.
		assertInDiscard(army);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
