package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("garrison", "6_52");
					// Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text
					put("knight1", "5_35");
					put("knight2", "5_35");
					// Gondorian Merchant: the one [gondor] ally, a Man, strength 6, home 3T; a maneuver ability used only
					// on purpose
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] Hobbit companion, no Man, strength 3 ("to play, spot a [gondor]
					// companion": cheated in); a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Household Guard: a [rohan] Man companion, strength 6; Valiant is its whole text
					put("guard", "5_83");
					// A Path Appointed: another [gondor] support-area condition, not unique, which gives itself [gondor]
					// tokens; its abilities are used only on purpose
					put("path1", "8_42");
					put("path2", "8_42");
					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not reached by a cheat
					put("runner", "1_178");
					// Dunlending Brigand: [dunland] Man minion, strength 8, vitality 1, no text
					put("brigand", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Redhorn Pass 13_194 ("Culture tokens cannot be added, removed, or reinforced") in
	// place of a site no test reaches; the test lays it as site 2.
	protected VirtualTableScenario GetRedhornPassScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("garrison", "6_52");
					// Gondorian Knight: [gondor] Man companion; Knight is its whole text
					put("knight1", "5_35");
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
	public void GarrisonofOsgiliathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Garrison of Osgiliath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>. Plays to your support area.<br>When you play this condition, you may spot a [gondor] Man to place 2 [gondor] tokens here.<br><b>Skirmish:</b> Discard this condition or remove a [gondor] token from here to make a [gondor] Man strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("garrison");

		assertEquals("Garrison of Osgiliath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedBesideAGondorManCompanionMayPlaceTwoTokensHereAndAnotherCardPlayedLaterSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var knight1 = scn.GetFreepsCard("knight1");
		var path1 = scn.GetFreepsCard("path1");
		var path2 = scn.GetFreepsCard("path2");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToSupportArea(path1);
		scn.MoveCardsToHand(garrison, path2);

		scn.StartGame();
		scn.AddTokensToCard(path1, 1);

		assertEquals(1, scn.GetCultureTokensOn(path1));
		assertInZone(Zone.HAND, garrison);

		scn.FreepsPlayCard(garrison);
		assertInZone(Zone.SUPPORT, garrison);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(garrison));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		// "Here": the other [gondor] condition gets none.
		assertEquals(1, scn.GetCultureTokensOn(path1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [gondor] condition played once this one is in play is not "this condition".
		scn.FreepsPlayCard(path2);
		assertInZone(Zone.SUPPORT, path2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(path1));
		assertEquals(0, scn.GetCultureTokensOn(path2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGondorManAllyIsEnoughToSpotAndDecliningPlacesNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(garrison);

		scn.StartGame();
		scn.FreepsPlayCard(garrison);

		// The ally is the only [gondor] Man there is.
		assertInZone(Zone.SUPPORT, garrison);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(garrison));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertInZone(Zone.SUPPORT, garrison);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyARohanManAndAGondorHobbitToSpotNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(guard, pippin);
		scn.MoveCardsToHand(garrison);

		scn.StartGame();
		scn.FreepsPlayCard(garrison);

		// The Household Guard is a Man of another culture; Pippin is [gondor] and no Man.
		assertInZone(Zone.SUPPORT, garrison);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtASiteWhereNoCultureTokenCanBePlacedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornPassScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var knight1 = scn.GetFreepsCard("knight1");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToHand(garrison);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("redhorn"));
		// Turn 3: the fellowship phase at site 2, Redhorn Pass.
		scn.SkipToSite(2);

		assertEquals("Redhorn Pass", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.HAND, garrison);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(garrison);
		// A [gondor] Man is there to spot, but the tokens could not be placed: the offer is withheld.
		assertInZone(Zone.SUPPORT, garrison);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesATokenHereOrDiscardsItselfToMakeAnyGondorManStrongerForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var path1 = scn.GetFreepsCard("path1");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight1, knight2, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(garrison, path1);

		scn.StartGame();
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(path1, 1);
		assertInZone(Zone.SUPPORT, garrison);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(path1));
		scn.MoveMinionsToTable(runner, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight1, runner},
				new PhysicalCardImpl[]{knight2, brigand}
		);
		scn.FreepsResolveSkirmish(knight1);

		assertEquals(6, scn.GetStrength(knight1));
		assertEquals(6, scn.GetStrength(knight2));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(garrison));

		scn.FreepsUseCardAction(garrison);
		// Both costs can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("token from here"));
		scn.FreepsChooseOption("token from here");
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(path1));

		// Any [gondor] Man: the one skirmishing, one whose skirmish is still to come, and the ally; not a [gondor]
		// Hobbit, a [rohan] Man or a Man minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, brigand, runner, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight1);
		assertEquals(7, scn.GetStrength(knight1));
		assertEquals(6, scn.GetStrength(knight2));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(garrison);
		// One token left: both costs can still be paid.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("token from here");
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertInZone(Zone.SUPPORT, garrison);
		scn.FreepsChooseCard(knight2);
		assertEquals(7, scn.GetStrength(knight1));
		assertEquals(7, scn.GetStrength(knight2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight2);

		// A skirmish ability lasts for the skirmish phase it is used in: both bonuses are gone in the next skirmish.
		assertEquals(6, scn.GetStrength(knight1));
		assertEquals(6, scn.GetStrength(knight2));
		assertEquals(1, scn.GetCultureTokensOn(path1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(garrison));

		scn.FreepsUseCardAction(garrison);
		// No token here (the one on A Path Appointed is not "from here"): the discard is the one cost that can be
		// paid, and nothing is asked.
		assertFalse(scn.FreepsDecisionAvailable("Discard this condition"));
		assertInZone(Zone.DISCARD, garrison);
		assertEquals(1, scn.GetCultureTokensOn(path1));
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2, merchant));
		scn.FreepsChooseCard(merchant);
		assertEquals(7, scn.GetStrength(merchant));
		assertEquals(6, scn.GetStrength(knight2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var knight1 = scn.GetFreepsCard("knight1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToSupportArea(garrison);

		scn.StartGame();
		scn.AddTokensToCard(garrison, 2);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(garrison));
	}
}
