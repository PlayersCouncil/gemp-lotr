package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("heirs", "13_69");
					// A Path Appointed: [gondor] support-area condition that gives itself [gondor] tokens; its
					// abilities are fellowship and regroup ones, never used here
					put("path", "8_42");
					// Support of the City: [gondor] support-area condition, cost 0; fellowship abilities never used
					put("city", "7_123");

					// Gondorian Knight: [gondor] Man companion, strength 6, no text but Knight
					put("knight", "5_35");
					// Narsil, Blade of the Faithful: [gondor] ARTIFACT, a hand weapon, on a [gondor] Man; vitality +1;
					// its strength bonus is only for Elendil or Isildur, and its Response is not reached
					put("narsil", "9_34");
					// Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3 (5 with the Hobbit Sword); his skirmish ability is never used
					put("pippin", "103_31");
					// Hobbit Sword: [shire] possession, a hand weapon, strength +2, "Bearer must be a Hobbit"
					put("hsword1", "1_299");
					put("hsword2", "1_299");
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8; his maneuver ability is never used
					put("aragorn", "1_89");
					// Bow of Minas Tirith: [gondor] RANGED weapon, strength +1; its archery trigger wants a battleground
					put("bow", "11_58");
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion
					// Gondorian Merchant: the [gondor] Man ALLY; a maneuver ability that needs initiative
					put("merchant", "7_97");
					// Gondorian Sword: [gondor] hand weapon, strength +2; a skirmish ability never used
					put("gsword", "7_98");

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeirsofGondorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Heirs of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, add a [gondor] token here.<br><b>Skirmish:</b> Discard this from play or remove a token from here to make a [gondor] companion bearing a hand weapon strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("heirs");

		assertEquals("Heirs of Gondor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedAddsAGondorTokenHereAndAnotherGondorConditionPlayedLaterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirs = scn.GetFreepsCard("heirs");
		var path = scn.GetFreepsCard("path");
		var city = scn.GetFreepsCard("city");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(path);
		scn.MoveCardsToHand(heirs, city);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertInZone(Zone.HAND, heirs, city);

		scn.FreepsPlayCard(heirs);
		assertInZone(Zone.SUPPORT, heirs);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		// "Here": the [gondor] condition beside it gets none.
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [gondor] condition played once this one is in play is not "this".
		scn.FreepsPlayCard(city);
		assertInZone(Zone.SUPPORT, city);
		assertEquals(0, scn.GetCultureTokensOn(city));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesATokenOrDiscardsThisToMakeAGondorCompanionBearingAHandWeaponStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirs = scn.GetFreepsCard("heirs");
		var path = scn.GetFreepsCard("path");
		var knight = scn.GetFreepsCard("knight");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var hsword1 = scn.GetFreepsCard("hsword1");
		var hsword2 = scn.GetFreepsCard("hsword2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var sam = scn.GetFreepsCard("sam");
		var merchant = scn.GetFreepsCard("merchant");
		var gsword = scn.GetFreepsCard("gsword");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(knight, pippin, aragorn, sam);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(heirs, path);
		// Valid: a [gondor] Man bearing an artifact hand weapon, and a [gondor] Hobbit bearing a possession one.
		scn.AttachCardsTo(knight, narsil);
		scn.AttachCardsTo(pippin, hsword1);
		// Invalid: a [gondor] companion bearing a ranged weapon, a [shire] companion bearing a hand weapon, and the
		// [gondor] ally bearing a hand weapon.
		scn.AttachCardsTo(aragorn, bow);
		scn.AttachCardsTo(sam, hsword2);
		scn.AttachCardsTo(merchant, gsword);

		scn.StartGame();
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(path, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertInZone(Zone.SUPPORT, heirs, path);
		assertAttachedTo(narsil, knight);
		assertAttachedTo(hsword1, pippin);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(hsword2, sam);
		assertAttachedTo(gsword, merchant);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		// Pippin, Sam and the Merchant are left out of the skirmishes.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight, runner1},
				new PhysicalCardImpl[]{aragorn, runner2}
		);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(6, scn.GetStrength(knight));
		assertEquals(5, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(heirs));

		scn.FreepsUseCardAction(heirs);
		// Both costs can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this from play"));
		assertTrue(scn.FreepsChoiceAvailable("Remove a token from here"));
		scn.FreepsChooseOption("Remove a token from here");
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertInZone(Zone.SUPPORT, heirs);
		assertEquals(1, scn.GetCultureTokensOn(path));

		// The Knight in his skirmish and Pippin in none; not Aragorn (a bow), Sam ([shire]) or the Merchant (an ally).
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, sam, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);
		assertEquals(8, scn.GetStrength(knight));
		assertEquals(5, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		// A skirmish ability lasts for the skirmish phase it is used in; this is the next one.
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(heirs));

		// No token here now (the one on A Path Appointed is not "here"): the discard is carried out unasked.
		scn.FreepsUseCardAction(heirs);
		assertInDiscard(heirs);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, sam, merchant));
		// The text names no skirmish: Pippin, in none, may be chosen.
		scn.FreepsChooseCard(pippin);
		assertEquals(7, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGondorCompanionBearingAHandWeaponTheSkirmishAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirs = scn.GetFreepsCard("heirs");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var sam = scn.GetFreepsCard("sam");
		var hsword2 = scn.GetFreepsCard("hsword2");
		var merchant = scn.GetFreepsCard("merchant");
		var gsword = scn.GetFreepsCard("gsword");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(heirs);
		scn.AttachCardsTo(aragorn, bow);
		scn.AttachCardsTo(sam, hsword2);
		scn.AttachCardsTo(merchant, gsword);

		scn.StartGame();
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(hsword2, sam);
		assertAttachedTo(gsword, merchant);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner1);

		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(heirs));

		// An ability with a cost is offered whatever its effect can do: the token is paid, and nobody is stronger.
		scn.FreepsUseCardAction(heirs);
		scn.FreepsChooseOption("Remove a token from here");
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirs = scn.GetFreepsCard("heirs");
		var knight = scn.GetFreepsCard("knight");
		var narsil = scn.GetFreepsCard("narsil");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(heirs);
		scn.AttachCardsTo(knight, narsil);

		scn.StartGame();
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertAttachedTo(narsil, knight);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(heirs));
	}
}
