package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_096_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "68_96");
					put("guard", "5_83");         // Household Guard: [rohan] Man, strength 6, vitality 3; Valiant is its whole text
					put("guard2", "5_83");
					put("eomer", "4_267");        // Eomer, Third Marshal of Riddermark: [rohan] Man, strength 7; stronger only against a wounded minion
					put("erkenbrand", "0_59");    // Erkenbrand, Master of Westfold: [rohan] Man, strength 7; his texts are about playing him
					put("guma", "4_277");         // Guma, Plains Farmer: [rohan] Man ally; a fellowship ability used only on purpose
					put("merry", "103_85");       // Merry, Master Holbytla: [rohan] Hobbit, a Man only beside 3 [rohan] companions or Eowyn
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man

					put("worker", "15_135");      // Rohan Worker: [rohan] follower, cost 1, changes no number
					put("worker2", "15_135");
					put("stablemaster", "15_134"); // Rohan Stable Master: [rohan] follower, cost 2; to play, spot a [rohan] Man
					put("servant", "18_52");      // Gondorian Servant: [gondor] follower, cost 0, changes no strength

					put("saruman", "17_38");      // Saruman, Servant of Sauron: the Shadow player's [isengard] follower
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");       // Goblin Runner: strength 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErkenbrandsHornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Erkenbrand's Horn
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	Fellowship: Exert bearer to play a [rohan] follower from your draw deck.
		 * 	Skirmish: Discard your follower to make bearer strength +4 (or +5 if bearer is Erkenbrand).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horn");

		assertEquals("Erkenbrand's Horn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilARohanManIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var eomer = scn.GetFreepsCard("eomer");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		// A [rohan] Hobbit and a [gondor] Man.
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(horn, eomer);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(horn));

		scn.FreepsPlayCard(eomer);
		assertTrue(scn.FreepsPlayAvailable(horn));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(horn);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.ATTACHED, horn);
		assertEquals(eomer, horn.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayBePlayedOnARohanManWhoIsACompanionOrAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eomer, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(horn);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(horn));

		scn.FreepsPlayCard(horn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, frodo));

		scn.FreepsChooseCard(guma);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.ATTACHED, horn);
		assertEquals(guma, horn.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityExertsBearerToPlayARohanFollowerFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var guard = scn.GetFreepsCard("guard");
		var eomer = scn.GetFreepsCard("eomer");
		var worker = scn.GetFreepsCard("worker");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		var servant = scn.GetFreepsCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, eomer);
		scn.AttachCardsTo(guard, horn);

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(horn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, stablemaster);
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsUseCardAction(horn);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(eomer));

		// The deck is shown, and then the [rohan] followers in it are offered: not the [gondor] follower, and not
		// the [rohan] and [gondor] companions (the second Household Guard, Aragorn).
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, stablemaster));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(servant, guard, aragorn));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(stablemaster);
		assertInZone(Zone.SUPPORT, stablemaster);
		// The Stable Master costs 2.
		assertEquals(2, scn.GetTwilight());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, worker, servant);
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityMayPlayNothingAndIsNotOfferedOnceBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var guard = scn.GetFreepsCard("guard");
		var eomer = scn.GetFreepsCard("eomer");
		var worker = scn.GetFreepsCard("worker");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		scn.MoveCompanionsToTable(guard, eomer);
		scn.AttachCardsTo(guard, horn);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(horn));
		assertEquals(1, scn.GetWoundsOn(guard));
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(horn);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, stablemaster));
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsDeclineChoosing();
		assertEquals(2, scn.GetWoundsOn(guard));
		assertInZone(Zone.DECK, worker, stablemaster);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());

		// Vitality 3 with 2 wounds: the bearer cannot exert, though the other [rohan] Man could.
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertFalse(scn.FreepsActionAvailable(horn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsOfferedAndPaidForWithNoRohanFollowerInTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var guard = scn.GetFreepsCard("guard");
		var servant = scn.GetFreepsCard("servant");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, horn);
		// The [gondor] follower and a [rohan] companion stay in the deck.
		scn.MoveCardsToFreepsDiscard("worker", "worker2", "stablemaster");

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(horn));
		assertEquals(0, scn.GetWoundsOn(guard));
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(horn);
		// Shown the deck, with nothing in it to play.
		scn.FreepsDismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.DECK, servant);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsYourFollowerToMakeBearerStrengthPlus4ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var guard = scn.GetFreepsCard("guard");
		var eomer = scn.GetFreepsCard("eomer");
		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var worker = scn.GetFreepsCard("worker");
		var worker2 = scn.GetFreepsCard("worker2");
		var servant = scn.GetFreepsCard("servant");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard, eomer, erkenbrand);
		// The Household Guard bears the Horn.  Followers of the Free Peoples player's: one on the bearer, one on
		// another companion, one in the support area; the Shadow player's follower in his.
		scn.AttachCardsTo(guard, horn, worker);
		scn.AttachCardsTo(eomer, servant);
		scn.MoveCardsToSupportArea(worker2, saruman);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eomer, savage}, new PhysicalCardImpl[]{guard, troop});

		// Eomer's skirmish: the bearer is not in it.
		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.FreepsActionAvailable(horn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(7, scn.GetStrength(erkenbrand));

		scn.FreepsUseCardAction(horn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, worker2, servant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman, horn, guard, eomer, erkenbrand));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(servant);
		assertInDiscard(servant);
		assertInZone(Zone.ATTACHED, worker);
		assertInZone(Zone.SUPPORT, worker2, saruman);
		// Erkenbrand is in play, and is not the bearer: +4.
		assertEquals(10, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(7, scn.GetStrength(erkenbrand));

		// The next skirmish is another phase: the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsUseCardAction(horn);
		scn.FreepsChooseCard(worker2);
		assertInDiscard(worker2);
		assertInZone(Zone.ATTACHED, worker);
		assertEquals(10, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OnErkenbrandItIsPlus5AndIsNotOfferedWithNoFollowerOfYoursLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var worker = scn.GetFreepsCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(erkenbrand);
		scn.AttachCardsTo(erkenbrand, horn);
		scn.MoveCardsToSupportArea(worker, saruman);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(erkenbrand, runner);
		scn.FreepsResolveSkirmish(erkenbrand);

		assertTrue(scn.FreepsActionAvailable(horn));
		assertEquals(7, scn.GetStrength(erkenbrand));
		assertInZone(Zone.SUPPORT, worker);

		// His one follower is the one discarded.
		scn.FreepsUseCardAction(horn);
		assertInDiscard(worker);
		assertEquals(12, scn.GetStrength(erkenbrand));

		// The Shadow player's follower is the only one left in play.
		scn.ShadowPass();
		assertInZone(Zone.SUPPORT, saruman);
		assertFalse(scn.FreepsActionAvailable(horn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
