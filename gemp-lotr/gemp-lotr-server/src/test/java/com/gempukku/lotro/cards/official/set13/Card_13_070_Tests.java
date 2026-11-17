package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "13_70");

					// Gondorian Knight: [gondor] Man companion, no text but Knight
					put("knight", "5_35");
					// Gondorian Sword: [gondor] possession (hand weapon) on a [gondor] Man; a skirmish ability
					put("gsword", "7_98");
					// Heavy Chain: [rohan] possession (armor) on a Man; an overwhelm rule only
					put("chain", "4_278");
					// Gondorian Merchant: the [gondor] Man ally; a maneuver ability that needs initiative, never used
					put("merchant", "7_97");
					// Athelas: [gondor] possession on a [gondor] Man; a fellowship ability
					put("athelas", "1_94");
					// Catapult: [gondor] possession in the support area (to play, spot 2 [gondor] knights: cheated in);
					// a maneuver ability that costs 2 cards from hand, never used
					put("catapult", "8_32");
					// Aragorn, Ranger of the North: [gondor] Man companion; his maneuver ability is never used
					put("aragorn", "1_89");
					// Narsil, Blade of the Faithful: a [gondor] ARTIFACT on a [gondor] Man; its Response is not reached
					put("narsil", "9_34");
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion
					// Hobbit Sword: [shire] possession, "Bearer must be a Hobbit", no other text
					put("hsword", "1_299");

					// A Path Appointed and Support of the City: [gondor] support-area conditions that get [gondor]
					// tokens by their own abilities, which are fellowship and regroup ones
					put("path", "8_42");
					put("support", "7_123");
					// Wish For Our Kinfolk: a [dwarven] support-area condition that gets [dwarven] tokens; its
					// abilities are fellowship and skirmish ones
					put("wish", "8_8");

					// Moria Scout: [moria] Orc, vitality 2; his play trigger is not set off by a cheat
					put("scout", "1_191");
					// Moria Axe: a Shadow possession on a [moria] Orc; damage +1 and a skirmish ability
					put("axe", "1_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HopeRenewedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Hope Renewed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Discard a [gondor] possession from play to discard a possession from play and you may reinforce a [gondor] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope");

		assertEquals("Hope Renewed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAGondorPossessionToDiscardAPossessionAndMayReinforceAGondorToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var knight = scn.GetFreepsCard("knight");
		var gsword = scn.GetFreepsCard("gsword");
		var chain = scn.GetFreepsCard("chain");
		var merchant = scn.GetFreepsCard("merchant");
		var athelas = scn.GetFreepsCard("athelas");
		var catapult = scn.GetFreepsCard("catapult");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var sam = scn.GetFreepsCard("sam");
		var hsword = scn.GetFreepsCard("hsword");
		var path = scn.GetFreepsCard("path");
		var support = scn.GetFreepsCard("support");
		var wish = scn.GetFreepsCard("wish");
		var scout = scn.GetShadowCard("scout");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCompanionsToTable(knight, aragorn, sam);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(knight, gsword, chain);
		scn.AttachCardsTo(merchant, athelas);
		scn.AttachCardsTo(aragorn, narsil);
		scn.AttachCardsTo(sam, hsword);
		scn.MoveCardsToSupportArea(catapult, path, support, wish);
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(support, 1);
		scn.AddTokensToCard(wish, 1);
		assertAttachedTo(gsword, knight);
		assertAttachedTo(chain, knight);
		assertAttachedTo(athelas, merchant);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(hsword, sam);
		assertInZone(Zone.SUPPORT, catapult, path, support, wish);
		scn.MoveMinionsToTable(scout);
		scn.AttachCardsTo(scout, axe);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(axe, scout);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(support));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(hope));

		scn.FreepsPlayCard(hope);
		// The cost: a [gondor] possession borne by a companion, one an ally bears, one in the support area; not the
		// [gondor] artifact, the [rohan] or [shire] possession, the [gondor] condition or the Shadow possession.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gsword, athelas, catapult));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(narsil, chain, hsword, support, axe));
		scn.FreepsChooseCard(catapult);
		assertInDiscard(catapult);

		// The effect: any possession, of either side and any culture; not the artifact or a condition.
		assertEquals(5, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gsword, chain, athelas, hsword, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(narsil, support, path));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);
		assertInDiscard(axe);
		assertAttachedTo(gsword, knight);
		assertAttachedTo(chain, knight);
		assertAttachedTo(athelas, merchant);
		assertAttachedTo(hsword, sam);

		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseYes();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(path, support));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wish));
		scn.FreepsChooseCard(path);
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(support));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertInDiscard(hope);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningTheReinforceLeavesTheTokensAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var knight = scn.GetFreepsCard("knight");
		var gsword = scn.GetFreepsCard("gsword");
		var sam = scn.GetFreepsCard("sam");
		var hsword = scn.GetFreepsCard("hsword");
		var path = scn.GetFreepsCard("path");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(knight, sam);
		scn.AttachCardsTo(knight, gsword);
		scn.AttachCardsTo(sam, hsword);
		scn.MoveCardsToSupportArea(path);
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		assertAttachedTo(gsword, knight);
		assertAttachedTo(hsword, sam);
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetCultureTokensOn(path));

		// One [gondor] possession to pay with, and then one possession left: each taken unasked.
		scn.FreepsPlayCard(hope);
		assertInDiscard(gsword, hsword);

		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertInDiscard(hope);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoOtherPossessionLeftTheDiscardIsForNothingAndTheReinforceIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var knight = scn.GetFreepsCard("knight");
		var gsword = scn.GetFreepsCard("gsword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var path = scn.GetFreepsCard("path");
		var support = scn.GetFreepsCard("support");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.AttachCardsTo(knight, gsword);
		scn.AttachCardsTo(aragorn, narsil);
		scn.MoveCardsToSupportArea(path, support);
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		assertAttachedTo(gsword, knight);
		assertAttachedTo(narsil, aragorn);
		assertInZone(Zone.SUPPORT, path, support);
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetCultureTokensOn(path));

		// The Sword pays, and only an artifact and a condition are left: no possession to discard.
		scn.FreepsPlayCard(hope);
		assertInDiscard(gsword);
		assertAttachedTo(narsil, aragorn);
		assertInZone(Zone.SUPPORT, support);

		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseYes();
		// The one card with a [gondor] token is taken unasked.
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertInDiscard(hope);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAGondorPossessionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var knight = scn.GetFreepsCard("knight");
		var chain = scn.GetFreepsCard("chain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var sam = scn.GetFreepsCard("sam");
		var hsword = scn.GetFreepsCard("hsword");
		var support = scn.GetFreepsCard("support");
		var scout = scn.GetShadowCard("scout");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCompanionsToTable(knight, aragorn, sam);
		scn.AttachCardsTo(knight, chain);
		scn.AttachCardsTo(aragorn, narsil);
		scn.AttachCardsTo(sam, hsword);
		scn.MoveCardsToSupportArea(support);
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		assertAttachedTo(chain, knight);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(hsword, sam);
		assertInZone(Zone.SUPPORT, support);
		scn.MoveMinionsToTable(scout);
		scn.AttachCardsTo(scout, axe);
		scn.SkipToPhase(Phase.MANEUVER);
		assertAttachedTo(axe, scout);
		assertInZone(Zone.HAND, hope);

		// A [gondor] artifact, a [gondor] condition, possessions of other cultures and a Shadow possession: none pays.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hope));
	}

	@Test
	public void WithNoGondorTokenInPlayTheReinforceIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var knight = scn.GetFreepsCard("knight");
		var gsword = scn.GetFreepsCard("gsword");
		var sam = scn.GetFreepsCard("sam");
		var hsword = scn.GetFreepsCard("hsword");
		var wish = scn.GetFreepsCard("wish");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(knight, sam);
		scn.AttachCardsTo(knight, gsword);
		scn.AttachCardsTo(sam, hsword);
		scn.MoveCardsToSupportArea(wish);
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		scn.AddTokensToCard(wish, 1);
		assertAttachedTo(gsword, knight);
		assertAttachedTo(hsword, sam);
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetCultureTokensOn(wish));

		// Only a [dwarven] token in play: a yes would do nothing, so nothing is asked, and the rest is carried out.
		scn.FreepsPlayCard(hope);
		assertFalse(scn.FreepsDecisionAvailable("reinforce"));
		assertInDiscard(gsword, hsword, hope);
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
