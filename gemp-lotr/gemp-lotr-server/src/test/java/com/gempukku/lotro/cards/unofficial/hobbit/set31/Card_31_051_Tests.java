package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_051_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  A doubt is the
	// Hobbit Draft Game's burden.  The Shadow phase of turn 1 is at The Troll Hoard 30_50 (site 2), where a minion
	// of site 5 roams.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fat", "31_51");
					put("fat2", "31_51");       // a second copy: the card is not unique and says "this minion"

					put("wicked", "31_55");     // Wicked Spider: [spider] Spider, cost 2; a Skirmish ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void FatSpiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Fat Spider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Spider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * The twilight cost of this minion is -1 for each doubt.
		 * <b>Regroup:</b> Discard this minion to add a doubt.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fat");

		assertEquals("Fat Spider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SPIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsOneLessForEachDoubtDownToNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fat = scn.GetShadowCard("fat");
		var fat2 = scn.GetShadowCard("fat2");
		var wicked = scn.GetShadowCard("wicked");
		scn.MoveCardsToHand(fat, fat2, wicked);

		scn.StartGame();
		assertInHand(fat, fat2, wicked);

		// No doubt: the printed 3.
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetTwilightCost(fat));
		assertEquals(3, scn.GetTwilightCost(fat2));
		assertEquals(2, scn.GetTwilightCost(wicked));

		// Each copy is lowered by its own text alone; the other Spider in hand not at all.
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetTwilightCost(fat));
		assertEquals(2, scn.GetTwilightCost(fat2));
		assertEquals(2, scn.GetTwilightCost(wicked));

		scn.AddBurdens(2);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetTwilightCost(fat));
		assertEquals(2, scn.GetTwilightCost(wicked));

		// A fourth doubt: a cost never reads below 0.
		scn.AddBurdens(1);
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetTwilightCost(fat));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedAtTwoDoubtsItCostsOneAndItsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fat = scn.GetShadowCard("fat");
		scn.MoveCardsToHand(fat);

		scn.StartGame();
		assertInHand(fat);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(fat);

		// 10 - (3 - 2 for the two doubts) - 2 for roaming at site 2.
		assertInZone(Zone.SHADOW_CHARACTERS, fat);
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayersOwnDoubtsDoNotLowerIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// The first player's copy, which he plays as the Shadow player on the second player's turn.
		var fat = scn.GetFreepsCard("fat");

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.FreepsGetBurdens());

		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(fat);
		assertInHand(fat);

		// The doubts on the first player's own Bilbo are not the fellowship's that moves now.
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertEquals(3, scn.GetTwilightCost(fat));

		// A doubt on the second player's Bilbo, the Ring-bearer of the fellowship whose turn it is, does.
		scn.AddBurdens(1);
		assertEquals(1, scn.ShadowGetBurdens());
		assertEquals(2, scn.GetTwilightCost(fat));
	}

	@Test
	public void RegroupAbilityDiscardsThisCopyToAddADoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fat = scn.GetShadowCard("fat");
		var fat2 = scn.GetShadowCard("fat2");

		scn.StartGame();

		// The minions that lived through the skirmishes stand at the start of the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(fat, fat2);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, fat, fat2);
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(fat));
		scn.ShadowUseCardAction(fat);

		// The copy used is discarded, the other stays; the doubt goes on the Free Peoples player's Bilbo.
		assertInDiscard(fat);
		assertInZone(Zone.SHADOW_CHARACTERS, fat2);
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fat = scn.GetShadowCard("fat");

		scn.StartGame();
		scn.MoveMinionsToTable(fat);

		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, fat);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fat));
		assertEquals(0, scn.FreepsGetBurdens());
	}
}
