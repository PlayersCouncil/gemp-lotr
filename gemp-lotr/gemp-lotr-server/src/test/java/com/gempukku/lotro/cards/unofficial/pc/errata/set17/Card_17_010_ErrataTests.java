package com.gempukku.lotro.cards.unofficial.pc.errata.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_010_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("namarie", "67_10");

					// Hunters of three cultures.
					put("gimli", "15_5");       // Gimli, Eager Hunter: [dwarven], Damage +1 and Hunter 3, no other text
					put("aragorn", "15_54");    // Aragorn, Swift Hunter: [gondor] Man, Hunter 1; a skirmish ability used only on purpose
					put("legolas", "15_18");    // Legolas, Fleet-footed Hunter: [elven], Hunter 1, cost 2; a regroup ability used only on purpose
					put("mablung", "15_63");    // Mablung, Ranger of Ithilien: [gondor], Hunter 1; a skirmish ability used only on purpose
					put("follower", "15_160");  // Following Uruk: [uruk-hai] minion, Fierce, Hunter 1; its trigger needs a skirmish won
					// [elven] cards that are no hunters.
					put("elf", "1_53");         // Lórien Elf: [elven] companion with no keyword
					put("gilgalad", "18_12");   // Gil-galad, High King of the Noldor: "Regroup: Remove an [elven] token to play an [elven] condition from your discard pile."
					put("focus", "15_15");      // Focus: [elven] support-area condition; its texts need Legolas to win a skirmish, or a skirmish

					// Conditions of both sides, in a support area and on a companion, and a possession beside them.
					put("stairs", "1_24");      // Stairs of Khazad-dûm: [dwarven] support-area condition; only touches a Dwarf's skirmish
					put("alliance", "1_49");    // The Last Alliance of Elves and Men: [elven] condition borne by a [gondor] Man
					put("fear", "1_278");       // Strength Born of Fear: [sauron] support-area condition; a skirmish ability
					put("weight", "1_282");     // The Weight of a Legacy: [sauron] condition borne by Aragorn, strength -1 and nothing else
					put("athelas", "1_94");     // Athelas: [gondor] possession borne by a [gondor] Man; a fellowship ability

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("runner", "1_178");     // Goblin Runner: a minion that is no hunter
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NamarieStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Namárië
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this, add an [elven] token here for each hunter you can spot (limit 3).
		 * Maneuver: Discard this condition (or remove a token from here) and discard a card from hand to discard a condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("namarie");

		assertEquals("Namárië", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysForTwoToTheSupportAreaWithATokenForEachHunterAndAHunterPlayedLaterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		var focus = scn.GetFreepsCard("focus");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable("gimli", "aragorn", "elf");
		scn.MoveCardsToSupportArea(focus);
		scn.MoveCardsToHand(namarie, legolas);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(namarie));
		assertTrue(scn.FreepsPlayAvailable(namarie));
		scn.FreepsPlayCard(namarie);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, namarie);
		assertNull(namarie.getAttachedTo());
		// Gimli and Aragorn are hunters. Lórien Elf, an [elven] companion, and Frodo are not.
		assertEquals(2, scn.GetCultureTokensOn(namarie));
		assertEquals(0, scn.GetCultureTokensOn(focus));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The hunters are counted when Namárië is played: one who arrives afterwards adds nothing.
		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertEquals(2, scn.GetCultureTokensOn(namarie));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FourHuntersAddOnlyThreeTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "mablung");
		scn.MoveCardsToHand(namarie);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(namarie));
		scn.FreepsPlayCard(namarie);

		assertInZone(Zone.SUPPORT, namarie);
		assertEquals(3, scn.GetCultureTokensOn(namarie));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedFromTheDiscardPileInTheRegroupPhaseItCountsTheShadowPlayersHunterToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var focus = scn.GetFreepsCard("focus");
		var follower = scn.GetShadowCard("follower");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable("gimli");
		scn.MoveCompanionsToTable(gilgalad);
		scn.MoveCardsToSupportArea(focus);
		// The [elven] token that Gil-galad's ability costs.
		scn.AddTokensToCard(focus, 1);
		scn.MoveCardsToDiscard(namarie);

		scn.StartGame();
		scn.MoveMinionsToTable(follower, runner);
		scn.SkipToPhase(Phase.REGROUP);

		// 2 for Ettenmoors + 3 companions
		assertEquals(5, scn.GetTwilight());
		assertInDiscard(namarie);
		assertTrue(scn.FreepsActionAvailable(gilgalad));
		scn.FreepsUseCardAction(gilgalad);

		assertEquals(0, scn.GetCultureTokensOn(focus));
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, namarie);
		// Gimli and the Following Uruk are hunters. Gil-galad, Frodo and the Goblin Runner are not.
		assertEquals(2, scn.GetCultureTokensOn(namarie));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ManeuverActionRemovesATokenAndDiscardsACardFromHandToDiscardAnyOneCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		var alliance = scn.GetFreepsCard("alliance");
		var athelas = scn.GetFreepsCard("athelas");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var fear = scn.GetShadowCard("fear");
		var weight = scn.GetShadowCard("weight");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(namarie, stairs, fear);
		scn.AttachCardsTo(aragorn, alliance, weight, athelas);
		scn.AddTokensToCard(namarie, 2);
		scn.MoveCardsToHand(elf, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCultureTokensOn(namarie));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(namarie));
		scn.FreepsUseCardAction(namarie);

		// With a token here the token is the price, and the player is not asked: the next question is the card from hand.
		assertEquals(1, scn.GetCultureTokensOn(namarie));
		assertInZone(Zone.SUPPORT, namarie);
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elf);
		assertInDiscard(elf);
		assertInHand(legolas);
		assertEquals(1, scn.GetFreepsHandCount());

		// Any condition: either player's, in a support area or on a companion, and Namárië itself. Not a possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, alliance, fear, weight, namarie));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(athelas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(weight);

		assertInDiscard(weight);
		assertInZone(Zone.SUPPORT, namarie, stairs, fear);
		assertAttachedTo(alliance, aragorn);
		assertAttachedTo(athelas, aragorn);
		assertEquals(1, scn.GetCultureTokensOn(namarie));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A second use takes the last token and the last card in hand, and may discard Namárië itself.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(namarie));
		scn.FreepsUseCardAction(namarie);

		assertEquals(0, scn.GetCultureTokensOn(namarie));
		assertInDiscard(legolas);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, alliance, fear, namarie));
		scn.FreepsChooseCard(namarie);

		assertInDiscard(namarie);
		assertInZone(Zone.SUPPORT, stairs, fear);
		assertAttachedTo(alliance, aragorn);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoTokenTheManeuverActionDiscardsNamarieItselfAndACardFromHandToDiscardACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		var stairs = scn.GetFreepsCard("stairs");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(namarie, stairs, fear);
		scn.MoveCardsToHand(elf, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(namarie));
		assertInZone(Zone.SUPPORT, namarie);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(namarie));
		scn.FreepsUseCardAction(namarie);

		// No token to remove: Namárië is discarded, and then a card from hand.
		assertInDiscard(namarie);
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);
		assertInDiscard(legolas);
		assertInHand(elf);

		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, fear));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(namarie));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stairs);

		// Namárië, Legolas and the Stairs
		assertInDiscard(stairs);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedOnceTheHandIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		var stairs = scn.GetFreepsCard("stairs");
		var elf = scn.GetFreepsCard("elf");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(namarie, stairs, fear);
		scn.AddTokensToCard(namarie, 2);
		scn.MoveCardsToHand(elf);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(namarie));
		scn.FreepsUseCardAction(namarie);
		// The only card in hand goes without a choice.
		assertInDiscard(elf);
		scn.FreepsChooseCard(fear);
		assertInDiscard(fear);
		scn.ShadowPass();

		// A token is left to remove and conditions are left to discard, but there is no card in hand to discard.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(namarie));
		assertInZone(Zone.SUPPORT, namarie, stairs);
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(namarie));
	}

	@Test
	public void WithNoOtherConditionInPlayBothCostsArePaidAndNothingElseIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(namarie);
		scn.AttachCardsTo(aragorn, athelas);
		scn.MoveCardsToHand(elf);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(namarie));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(namarie));
		scn.FreepsUseCardAction(namarie);

		assertInDiscard(namarie, elf);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertAttachedTo(athelas, aragorn);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
