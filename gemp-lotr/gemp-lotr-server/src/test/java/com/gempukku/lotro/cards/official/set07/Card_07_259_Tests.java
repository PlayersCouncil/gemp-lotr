package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_259_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_259");
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

		var card = scn.GetFreepsCard("card");

		assertEquals("Wind in His Face", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
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
}
