package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_067_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("palantir", "53_67");
					put("troop", "1_143");      // Troop of Uruk-hai: an [isengard] Uruk-hai, strength 9, vitality 4
					put("servant", "3_58");     // Isengard Servant: an [isengard] Orc, strength 7, vitality 3; regroup ability used only on purpose
					put("servant2", "3_58");
					put("raiding", "1_158");    // Uruk-hai Raiding Party: an [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("patrol", "1_177");     // Goblin Patrol Troop: a [moria] minion, no text
					put("aragorn", "1_89");     // Free Peoples cards for the Free Peoples player's hand, or on the table
					put("sam", "1_311");
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, vitality 2
					put("guard2", "1_7");
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, strength 6, vitality 3; a skirmish ability used only on purpose
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession, so that not every Free Peoples card is a companion
					put("runner", "1_178");     // and a Shadow card beside them
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePalantirofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Palantir of Orthanc
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Palantir
		 * Game Text: Plays to your support area.
		 * 	<b>Shadow:</b> Exert an [isengard] minion.  The Free Peoples player may place any number of cards from
		 * 	hand on top of their draw deck, then reveals their hand.  For each Free Peoples card revealed, make that
		 * 	minion strength +1 until the regroup phase.  Limit once per phase.
		 */

		var scn = GetScenario();

		var palantir = scn.GetFreepsCard("palantir");

		assertEquals("The Palantir of Orthanc", palantir.getBlueprint().getTitle());
		assertNull(palantir.getBlueprint().getSubtitle());
		assertTrue(palantir.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, palantir.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, palantir.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, palantir.getBlueprint().getCardType());
		assertTrue(palantir.getBlueprint().getPossessionClasses().contains(PossessionClass.PALANTIR));
		assertTrue(scn.HasKeyword(palantir, Keyword.SUPPORT_AREA));
		assertEquals(0, palantir.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysToTheSupportAreaForNothingWithNoMinionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		scn.MoveCardsToHand(palantir);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		// 5 set + 1 companion + 2 for Ettenmoors
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(palantir));

		scn.ShadowPlayCard(palantir);
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, palantir);
		assertNotAttachedTo(palantir, scn.GetRingBearer());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertsAnIsengardMinionAndMakesItStrongerForEachFreePeoplesCardLeftInTheRevealedHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var servant = scn.GetShadowCard("servant");
		var servant2 = scn.GetShadowCard("servant2");
		var raiding = scn.GetShadowCard("raiding");
		var patrol = scn.GetShadowCard("patrol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCardsToSupportArea(palantir);
		// Five Free Peoples cards (four companions and a possession) and a Shadow card in the Free Peoples player's hand.
		scn.MoveCardsToHand(aragorn, sam, guard, gimli, armor, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, servant2, raiding, patrol);
		// Vitality 3 with 2 wounds: exhausted, cannot exert.
		scn.AddWoundsToChar(servant2, 2);
		scn.FreepsPass();

		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(raiding));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(7, scn.GetStrength(servant));
		assertEquals(9, scn.GetStrength(raiding));
		assertEquals(7, scn.GetStrength(servant2));
		assertTrue(scn.ShadowActionAvailable(palantir));

		scn.ShadowUseCardAction(palantir);
		assertTrue(scn.ShadowHasCardChoiceAvailable(servant, raiding));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant2, patrol));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(servant);
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(raiding));

		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam, guard, gimli, armor, runner));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(6, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gimli);
		assertEquals(gimli, scn.GetFreepsTopOfDeck());
		assertEquals(5, scn.GetFreepsHandCount());

		// The hand is shown to the Shadow player alone, without the card put back on the deck.
		assertEquals(5, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(gimli, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// Aragorn, Sam, Dwarf Guard and Dwarven Armor (a possession, no companion): 7 + 4.  Goblin Runner is a Shadow card.
		assertEquals(11, scn.GetStrength(servant));
		assertEquals(9, scn.GetStrength(raiding));
		assertEquals(7, scn.GetStrength(servant2));
		assertInHand(aragorn, sam, guard, armor, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedAgainInThePhaseItWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var troop = scn.GetShadowCard("troop");
		var raiding = scn.GetShadowCard("raiding");
		scn.MoveCardsToSupportArea(palantir);
		// Two Free Peoples cards and a Shadow card in the Free Peoples player's hand.
		scn.MoveCardsToHand(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"), scn.GetFreepsCard("runner"));

		scn.StartGame();
		scn.MoveMinionsToTable(troop, raiding);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(3, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.ShadowUseCardAction(palantir);
		scn.ShadowChooseCard(troop);
		// The Free Peoples player places nothing.
		scn.FreepsChooseNo();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(troop));
		// Aragorn and Sam: 9 + 2.
		assertEquals(11, scn.GetStrength(troop));

		// Limit once per phase: a limit on uses, so the ability is not offered again, though either minion
		// could still exert (Ketura on Q-0093).
		assertFalse(scn.ShadowActionAvailable(palantir));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(raiding));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheWholeHandPutOnTheDeckNothingIsRevealedAndTheMinionGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var servant = scn.GetShadowCard("servant");
		var raiding = scn.GetShadowCard("raiding");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(palantir);
		scn.MoveCardsToHand(aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, raiding);
		scn.FreepsPass();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(7, scn.GetStrength(servant));

		scn.ShadowUseCardAction(palantir);
		scn.ShadowChooseCard(servant);
		assertEquals(1, scn.GetWoundsOn(servant));

		scn.FreepsChooseYes();
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, sam);
		// The Free Peoples player then puts them on the deck in the order he likes.
		scn.FreepsChooseCardBPFromSelection(aragorn);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(Set.of(aragorn, sam), Set.of(scn.GetFromTopOfFreepsDeck(1), scn.GetFromTopOfFreepsDeck(2)));

		// An empty hand is revealed: the Shadow player is shown no cards.
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertEquals(7, scn.GetStrength(servant));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(9, scn.GetStrength(raiding));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StrengthBonusLastsThroughTheSkirmishesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var servant = scn.GetShadowCard("servant");
		var raiding = scn.GetShadowCard("raiding");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToSupportArea(palantir);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(sam, guard2);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, raiding);
		scn.FreepsPass();

		scn.ShadowUseCardAction(palantir);
		scn.ShadowChooseCard(servant);
		scn.FreepsChooseNo();
		scn.ShadowDismissRevealedCards();
		// Sam and Dwarf Guard: 7 + 2.
		assertEquals(9, scn.GetStrength(servant));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiding}, new PhysicalCardImpl[]{gimli, servant});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Aragorn 8 against 9: one wound, and one more for damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, servant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(servant));
		scn.PassSkirmishActions();
		// Gimli 6 against 9 (short of overwhelming him): one wound.
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(servant));
	}

	@Test
	public void NotOfferedWithOnlyAnExhaustedIsengardMinionNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var servant = scn.GetShadowCard("servant");
		var raiding = scn.GetShadowCard("raiding");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCardsToSupportArea(palantir);
		scn.MoveCardsToHand(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"));

		scn.StartGame();
		scn.MoveMinionsToTable(servant, patrol);
		// Vitality 3 with 2 wounds: exhausted.  The [moria] minion beside him could exert.
		scn.AddWoundsToChar(servant, 2);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(servant));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(palantir));

		// A fresh [isengard] minion, for the maneuver phase.
		scn.MoveMinionsToTable(raiding);
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(raiding));
		assertFalse(scn.ShadowActionAvailable(palantir));
	}

	@Test
	public void OfferedAgainInItsOwnersNextShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var servant = scn.GetShadowCard("servant");
		var raiding = scn.GetShadowCard("raiding");
		scn.MoveCardsToSupportArea(palantir);
		scn.MoveCardsToHand(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"));

		scn.StartGame();
		scn.MoveMinionsToTable(servant);
		scn.FreepsPass();

		// The only [isengard] minion is chosen for the exertion without asking.
		scn.ShadowUseCardAction(palantir);
		scn.FreepsChooseNo();
		scn.ShadowDismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(servant));
		assertFalse(scn.ShadowActionAvailable(palantir));

		// No minion left, so no combat: on to the turn of the other player, and back.
		scn.MoveCardsToDiscard(servant);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.MoveMinionsToTable(raiding);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(raiding));
		assertTrue(scn.ShadowActionAvailable(palantir));
	}
}
