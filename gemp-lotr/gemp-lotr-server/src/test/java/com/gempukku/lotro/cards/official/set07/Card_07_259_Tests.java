package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_259_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "7_259");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, Valiant and nothing else
					put("guard2", "5_83");
					// Rider's Mount: [rohan] mount, "Bearer must be a [rohan] Man"; at the start of a skirmish of its
					// bearer each minion skirmishing him exerts (the Goblin Runners cannot)
					put("mount", "4_287");
					put("chain", "4_278");      // Heavy Chain: a [rohan] possession that is no mount, borne by a Man
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, strength 4; a fellowship ability only
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, strength 6, Knight and nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: the [rohan] Hobbit, a Man only beside 3 [rohan] companions or Eowyn
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc minion, strength 5, vitality 1; its text answers its own play
					put("runner2", "1_178");
					// The Shadow hand: three different cards, so that which one went is seen.
					put("troop", "1_177");      // Goblin Patrol Troop
					put("savage", "1_151");     // Uruk Savage
					put("wildmen", "4_4");      // Band of Wild Men
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetMountedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind1", "7_259");
					put("wind2", "7_259");
					put("rider", "5_83");       // Household Guard: a [rohan] Man, strength 6; Valiant is its whole text
					put("footman", "5_83");     // a second Household Guard, who bears no mount
					put("mount", "4_287");      // Rider's Mount: on a [rohan] Man; each minion skirmishing bearer exerts as the skirmish starts
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man who is not [rohan]
					put("savage", "1_151");     // Uruk Savage: vitality 3, so the mount's exertion leaves it standing
					put("runner", "1_178");     // Goblin Runner: the one card of the Shadow hand
					put("troop", "1_177");      // Goblin Patrol Troop: the top card of the Shadow draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WindinHisFaceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Wind in His Face
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [rohan] Man strength +2. If that Man is mounted, choose an opponent who must place a random card from hand beneath his or her draw deck.
		*/

		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");

		assertEquals("Wind in His Face", wind.getBlueprint().getTitle());
		assertNull(wind.getBlueprint().getSubtitle());
		assertFalse(wind.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, wind.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, wind.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, wind.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(wind, Timeword.SKIRMISH));
		assertEquals(1, wind.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenTheManIsMountedTheOpponentPlacesARandomCardFromHandBeneathHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMountedScenario();

		var wind1 = scn.GetFreepsCard("wind1");
		var wind2 = scn.GetFreepsCard("wind2");
		var rider = scn.GetFreepsCard("rider");
		var footman = scn.GetFreepsCard("footman");
		var mount = scn.GetFreepsCard("mount");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(rider, footman, aragorn);
		scn.AttachCardsTo(rider, mount);
		scn.MoveCardsToHand(wind1, wind2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// One card in the Shadow hand, so the card taken at random is that one.
		scn.MoveCardsToHand(runner);
		scn.MoveCardsToTopOfDeck(troop);
		assertInZone(Zone.HAND, runner);
		assertInZone(Zone.DECK, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, savage);
		scn.FreepsResolveSkirmish(rider);

		int deck = scn.GetShadowDeckCount();
		assertEquals(6, scn.GetStrength(rider));
		assertEquals(6, scn.GetStrength(footman));
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, runner);
		assertEquals(troop, scn.GetShadowTopOfDeck());

		// On the Man with no mount: strength +2, and no card of the opponent's is moved.
		scn.FreepsPlayCard(wind1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(footman);
		assertInZone(Zone.DISCARD, wind1);
		assertEquals(8, scn.GetStrength(footman));
		assertEquals(6, scn.GetStrength(rider));
		assertInZone(Zone.HAND, runner);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// On the mounted Man: strength +2, and the card in the Shadow hand goes beneath the Shadow draw deck.
		scn.ShadowPass();
		scn.FreepsPlayCard(wind2);
		scn.FreepsChooseCard(rider);
		assertInZone(Zone.DISCARD, wind2);
		assertEquals(8, scn.GetStrength(rider));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(runner, scn.GetShadowBottomOfDeck());
		assertEquals(troop, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MakesARohanManStrengthPlus2AndOnAMountedOneARandomCardOfTheShadowHandGoesBeneathHisDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var guma = scn.GetFreepsCard("guma");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(guard, knight, merry);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(wind);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var wildmen = scn.GetShadowCard("wildmen");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		scn.SetShadowHand(troop, savage, wildmen);
		// A card of his own in the Free Peoples hand, which nothing should touch.
		scn.MoveCardsToHand(chain);
		assertInHand(wind, chain);
		assertEquals(3, scn.GetShadowHandCount());
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner1}, new PhysicalCardImpl[]{knight, runner2});
		scn.FreepsResolveSkirmish(guard);

		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.FreepsPlayAvailable(wind));
		scn.FreepsPlayCard(wind);

		// A [rohan] Man companion in this skirmish and a [rohan] Man ally in none; not a [gondor] Man, the [rohan]
		// Hobbit (only two [rohan] companions in play, so no Man) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// He is mounted: one card of the Shadow hand, at random and with nobody asked, is placed beneath the Shadow
		// draw deck, and the other two are still in hand.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		var bottom = scn.GetShadowBottomOfDeck();
		assertTrue(List.of(troop, savage, wildmen).contains(bottom));
		for (var card : new PhysicalCardImpl[]{troop, savage, wildmen}) {
			if (card == bottom)
				assertInZone(Zone.DECK, card);
			else
				assertInHand(card);
		}
		// The Free Peoples hand and deck are untouched.
		assertInHand(chain);
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertInDiscard(wind);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of the skirmish; gone in the next skirmish.
		scn.ShadowPass();
		assertEquals(8, scn.GetStrength(guard));
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(knight);
		assertInPlay(guard);
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnAManBearingAPossessionThatIsNoMountNoCardIsPlacedThoughAnotherManIsMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var mount = scn.GetFreepsCard("mount");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(guard, guard2);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(guard2, chain);
		scn.MoveCardsToHand(wind);

		var runner1 = scn.GetShadowCard("runner1");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(chain, guard2);
		scn.SetShadowHand(troop, savage);
		assertEquals(2, scn.GetShadowHandCount());
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard2, runner1);
		scn.FreepsResolveSkirmish(guard2);

		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(6, scn.GetStrength(guard2));
		scn.FreepsPlayCard(wind);
		scn.FreepsChooseCard(guard2);

		assertEquals(8, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(guard));
		assertInHand(troop, savage);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OnAMountedManWithTheShadowHandEmptyItIsStrengthPlus2Alone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(wind);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner1);
		scn.FreepsResolveSkirmish(guard);

		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.FreepsPlayAvailable(wind));
		scn.FreepsPlayCard(wind);

		assertEquals(8, scn.GetStrength(guard));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInDiscard(wind);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanManItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(knight, merry);
		scn.MoveCardsToHand(wind);

		var runner1 = scn.GetShadowCard("runner1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SetShadowHand(troop);
		assertInHand(troop);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, runner1);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(merry));
		// A [gondor] Man and the [rohan] Hobbit: nobody to choose.
		assertTrue(scn.FreepsPlayAvailable(wind));
		scn.FreepsPlayCard(wind);

		assertInDiscard(wind);
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(merry));
		assertInHand(troop);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(wind);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(wind);
		assertFalse(scn.FreepsPlayAvailable(wind));
	}
}
