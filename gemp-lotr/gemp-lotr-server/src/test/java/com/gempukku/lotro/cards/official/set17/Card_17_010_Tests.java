package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_010_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("namarie1", "17_10");
					put("namarie2", "17_10");

					// Hunters of three cultures.
					put("gimli", "15_5");       // Gimli, Eager Hunter: [dwarven], Damage +1 and Hunter 3, no other text
					put("aragorn", "15_54");    // Aragorn, Swift Hunter: [gondor] Man, Hunter 1; a skirmish ability used only on purpose
					put("legolas", "15_18");    // Legolas, Fleet-footed Hunter: [elven], Hunter 1, cost 2; a regroup ability used only on purpose
					put("guardian", "17_7");    // Elven Guardian: [elven], Hunter 1; a maneuver heal used only on purpose
					put("follower", "15_160");  // Following Uruk: [uruk-hai] minion, Fierce, Hunter 1; its trigger needs a skirmish won
					put("tracker", "15_171");   // Tracking Uruk: [uruk-hai] minion, Hunter 1 is its whole text
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
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this, add an [elven] token here for each hunter you can spot.
		 * Maneuver: Discard this or remove a token from here and spot an [elven] hunter to discard a condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("namarie1");

		assertEquals("Namárië", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysWithATokenHereForEachHunterAndAHunterPlayedLaterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie1 = scn.GetFreepsCard("namarie1");
		var namarie2 = scn.GetFreepsCard("namarie2");
		var focus = scn.GetFreepsCard("focus");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable("gimli", "aragorn", "elf");
		scn.MoveCardsToSupportArea(focus);
		scn.MoveCardsToHand(namarie1, namarie2, legolas);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(namarie1));
		assertTrue(scn.FreepsPlayAvailable(namarie1));
		scn.FreepsPlayCard(namarie1);

		assertInZone(Zone.SUPPORT, namarie1);
		// Gimli and Aragorn are hunters. Lórien Elf, an [elven] companion, and Frodo are not.
		assertEquals(2, scn.GetCultureTokensOn(namarie1));
		assertEquals(0, scn.GetCultureTokensOn(focus));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The hunters are counted when Namárië is played: one who arrives afterwards adds nothing.
		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertEquals(2, scn.GetCultureTokensOn(namarie1));

		// A second copy counts three hunters now, and puts the tokens on itself alone.
		scn.FreepsPlayCard(namarie2);
		assertInZone(Zone.SUPPORT, namarie2);
		assertEquals(3, scn.GetCultureTokensOn(namarie2));
		assertEquals(2, scn.GetCultureTokensOn(namarie1));
		assertEquals(0, scn.GetCultureTokensOn(focus));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedFromTheDiscardPileInTheRegroupPhaseItCountsTheShadowPlayersHunterToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie1");
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
		assertEquals(1, scn.GetCultureTokensOn(focus));
		assertInDiscard(namarie);

		scn.StartGame();
		scn.MoveMinionsToTable(follower, runner);
		scn.SkipToPhase(Phase.REGROUP);

		assertInDiscard(namarie);
		assertTrue(scn.FreepsActionAvailable(gilgalad));
		scn.FreepsUseCardAction(gilgalad);

		assertEquals(0, scn.GetCultureTokensOn(focus));
		assertInZone(Zone.SUPPORT, namarie);
		// Gimli and the Following Uruk are hunters. Gil-galad, Frodo and the Goblin Runner are not.
		assertEquals(2, scn.GetCultureTokensOn(namarie));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithBothCostsPayableThePlayerIsAskedWhichAndAnyConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie1 = scn.GetFreepsCard("namarie1");
		var namarie2 = scn.GetFreepsCard("namarie2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var stairs = scn.GetFreepsCard("stairs");
		var alliance = scn.GetFreepsCard("alliance");
		var athelas = scn.GetFreepsCard("athelas");
		var fear = scn.GetShadowCard("fear");
		var weight = scn.GetShadowCard("weight");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(namarie1, namarie2, stairs, fear);
		scn.AttachCardsTo(aragorn, alliance, weight, athelas);
		scn.AddTokensToCard(namarie1, 2);
		scn.AddTokensToCard(namarie2, 1);
		assertEquals(2, scn.GetCultureTokensOn(namarie1));
		assertEquals(1, scn.GetCultureTokensOn(namarie2));
		assertInZone(Zone.SUPPORT, namarie1, namarie2, stairs, fear);
		assertAttachedTo(alliance, aragorn);
		assertAttachedTo(weight, aragorn);
		assertAttachedTo(athelas, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(namarie1));
		scn.FreepsUseCardAction(namarie1);

		assertEquals(List.of("Discard this", "Remove a token from here"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Remove a token from here");
		// From here: the other copy keeps its token.
		assertEquals(1, scn.GetCultureTokensOn(namarie1));
		assertEquals(1, scn.GetCultureTokensOn(namarie2));
		assertInZone(Zone.SUPPORT, namarie1);

		// Any condition: either player's, in a support area or on a companion, either copy of Namárië. Not a possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, alliance, fear, weight, namarie1, namarie2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(athelas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(weight);

		assertInDiscard(weight);
		assertInZone(Zone.SUPPORT, namarie1, namarie2, stairs, fear);
		assertAttachedTo(alliance, aragorn);
		assertAttachedTo(athelas, aragorn);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The other cost this time: Namárië discards itself, and is then no candidate.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(namarie1));
		scn.FreepsUseCardAction(namarie1);
		scn.FreepsChoose("Discard this");
		assertInDiscard(namarie1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, alliance, fear, namarie2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(namarie1, athelas));
		scn.FreepsChooseCard(fear);

		assertInDiscard(fear);
		assertInZone(Zone.SUPPORT, namarie2, stairs);
		assertEquals(1, scn.GetCultureTokensOn(namarie2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoTokenHereItDiscardsItselfWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie1 = scn.GetFreepsCard("namarie1");
		var namarie2 = scn.GetFreepsCard("namarie2");
		var guardian = scn.GetFreepsCard("guardian");
		var stairs = scn.GetFreepsCard("stairs");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(guardian);
		scn.MoveCardsToSupportArea(namarie1, namarie2, stairs, fear);
		// The tokens are on the other copy, not here.
		scn.AddTokensToCard(namarie2, 2);
		assertInZone(Zone.SUPPORT, namarie1, namarie2, stairs, fear);
		assertEquals(0, scn.GetCultureTokensOn(namarie1));
		assertEquals(2, scn.GetCultureTokensOn(namarie2));

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(namarie1));
		scn.FreepsUseCardAction(namarie1);

		// No token to remove here: Namárië is discarded, and the next question is the condition.
		assertInDiscard(namarie1);
		assertEquals(2, scn.GetCultureTokensOn(namarie2));
		assertTrue(scn.FreepsHasCardChoiceAvailable(namarie2, stairs, fear));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stairs);

		assertInDiscard(stairs);
		assertInZone(Zone.SUPPORT, namarie2, fear);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedUntilAnElvenHunterCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie1");
		var guardian = scn.GetFreepsCard("guardian");
		var fear = scn.GetShadowCard("fear");
		var tracker = scn.GetShadowCard("tracker");
		// A [dwarven] hunter and an [elven] Elf who is no hunter.
		scn.MoveCompanionsToTable("gimli", "elf");
		scn.MoveCardsToSupportArea(namarie, fear);
		scn.AddTokensToCard(namarie, 2);
		assertEquals(2, scn.GetCultureTokensOn(namarie));

		scn.StartGame();
		// The Shadow player's hunter is no [elven] one.
		scn.MoveMinionsToTable(tracker);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(namarie));

		scn.MoveCompanionsToTable(guardian);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(namarie));
	}

	@Test
	public void WithNoOtherConditionInPlayItDiscardsItselfForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var namarie = scn.GetFreepsCard("namarie1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(namarie);
		scn.AttachCardsTo(aragorn, athelas);
		assertAttachedTo(athelas, aragorn);
		assertInZone(Zone.SUPPORT, namarie);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(namarie));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(namarie));
		scn.FreepsUseCardAction(namarie);

		assertInDiscard(namarie);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertAttachedTo(athelas, aragorn);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
