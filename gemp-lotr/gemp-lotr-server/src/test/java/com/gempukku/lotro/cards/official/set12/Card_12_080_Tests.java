package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("whip", "12_80");
					// The Balrog, The Terror of Khazad-dum: his own text acts only at an underground site, and none
					// of these tests leaves the first two sites
					put("balrog", "12_79");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion of another title, no text
					// The Balrog's Sword: a second [moria] artifact for The Balrog, borne beside another hand weapon
					put("sword", "2_50");

					// Free Peoples cards of twilight cost 0, 1 and 2, and a Shadow card of cost 1, for the hand
					put("defend", "4_115");     // Defend It and Hope: a [gondor] Maneuver event, cost 0
					put("mount", "4_287");      // Rider's Mount: a [rohan] possession, cost 1
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] companion, cost 1
					put("guard", "5_83");       // Household Guard: a [rohan] companion, cost 2, valiant and nothing else
					put("goblin", "11_137");    // Scurrying Goblin: an [orc] minion, cost 1
					put("defend2", "4_115");
					// In play: a second Guard bearing a second Rider's Mount, a cost-1 possession that is not in hand
					put("guard2", "5_83");
					put("mount2", "4_287");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhipofManyThongsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Whip of Many Thongs, Weapon of Flame and Shadow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be The Balrog.<br>It is <b>fierce</b>.<br>When you play this artifact, the Free Peoples player reveals his or her hand and discards all Free Peoples cards from hand that have a twilight cost of 1 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("whip");

		assertEquals("Whip of Many Thongs", card.getBlueprint().getTitle());
		assertEquals("Weapon of Flame and Shadow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnTheBalrogAndOnNoOtherMoriaMinionAndMakesHimFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whip = scn.GetShadowCard("whip");
		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(whip);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The Shadow phase at Ettenmoors, site 2: no underground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(whip));

		scn.MoveMinionsToTable(balrog);
		scn.ShadowRefreshDecision();
		assertFalse(scn.HasKeyword(balrog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		assertTrue(scn.ShadowPlayAvailable(whip));
		// The Balrog is unique, so he is the one bearer there can be, and the bearer is not asked for.  The Free
		// Peoples hand is empty: the reveal shows nothing.
		scn.ShadowPlayCard(whip);

		assertAttachedTo(whip, balrog);
		assertTrue(scn.HasKeyword(balrog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedTheFreePeoplesHandIsShownAndItsFreePeoplesCardsOfCostOneOrLessAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defend = scn.GetFreepsCard("defend");
		var mount = scn.GetFreepsCard("mount");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var goblin = scn.GetFreepsCard("goblin");
		var defend2 = scn.GetFreepsCard("defend2");
		var guard2 = scn.GetFreepsCard("guard2");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard2);
		scn.AttachCardsTo(guard2, mount2);

		var whip = scn.GetShadowCard("whip");
		var sword = scn.GetShadowCard("sword");
		var balrog = scn.GetShadowCard("balrog");
		var shadowDefend = scn.GetShadowCard("defend");
		scn.MoveCardsToHand(whip, sword, shadowDefend);

		scn.StartGame();
		scn.MoveCardsToHand(defend, mount, merry, guard, goblin);
		scn.MoveCardsToDiscard(defend2);
		assertAttachedTo(mount2, guard2);
		assertInZone(Zone.HAND, defend, mount, merry, guard, goblin, shadowDefend);
		assertEquals(5, scn.GetFreepsHandCount());
		scn.MoveMinionsToTable(balrog);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(whip);
		assertAttachedTo(whip, balrog);

		// The whole hand is shown to the Shadow player, and its owner is shown nothing.
		assertEquals(5, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(defend, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(mount, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// Gone: the event of cost 0, the possession and the companion of cost 1.  Kept: the companion of cost
		// 2, the Shadow card of cost 1, the cost-1 possession in play, and the cost-0 Free Peoples card in the
		// Shadow player's own hand.
		assertInZone(Zone.DISCARD, defend, mount, merry);
		assertInZone(Zone.HAND, guard, goblin, shadowDefend);
		assertEquals(2, scn.GetFreepsHandCount());
		assertAttachedTo(mount2, guard2);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another artifact played on The Balrog afterwards sets nothing off: a cost-0 card back in hand stays.
		scn.MoveCardsToHand(defend2);
		assertInZone(Zone.HAND, defend2);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, balrog);
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertInZone(Zone.HAND, defend2, guard, goblin);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
