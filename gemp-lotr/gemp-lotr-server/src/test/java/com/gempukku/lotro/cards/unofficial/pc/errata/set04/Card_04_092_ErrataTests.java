package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_092_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tall", "54_92");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus and nothing else
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf

					// Shadow conditions of two cultures, inert on the Free Peoples player's turn.
					put("seeking1", "1_276");   // Seeking Its Master: [sauron], support area; a Shadow ability
					put("seeking2", "1_276");
					put("seeking3", "1_276");
					put("relics1", "1_195");    // Relics of Moria: [moria], support area; a Shadow ability
					put("relics2", "1_195");
					put("relics3", "1_195");
					put("evil", "1_283");       // You Bring Great Evil: [sauron] condition borne by a companion; no healing at a sanctuary
					put("poison", "8_28");      // Spider Poison: a Shadow possession borne by a companion

					// Free Peoples conditions.
					put("walkers", "1_79");     // The Nine Walkers: [gandalf], support area; a twilight discount on companions
					put("mallorn", "1_54");     // Mallorn-trees: [elven], support area; a Fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrownSuddenlyTallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Grown Suddenly Tall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Spell</b>.
		 * 	If the fellowship moves during the regroup phase, spot Gandalf to hinder all conditions. Add (1) for each Shadow condition hindered (limit (5)).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tall");

		assertEquals("Grown Suddenly Tall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARegroupMoveHindersEveryActiveConditionAndAddsOneForEachShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tall = scn.GetFreepsCard("tall");
		var gandalf = scn.GetFreepsCard("gandalf");
		var walkers = scn.GetFreepsCard("walkers");
		var ownRelics = scn.GetFreepsCard("relics2");
		var seeking1 = scn.GetShadowCard("seeking1");
		var seeking2 = scn.GetShadowCard("seeking2");
		var relics1 = scn.GetShadowCard("relics1");
		var evil = scn.GetShadowCard("evil");
		var poison = scn.GetShadowCard("poison");
		var shadowMallorn = scn.GetShadowCard("mallorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(tall);
		// Active on this turn: the Free Peoples player's own Free Peoples condition and the Shadow player's Shadow cards.
		scn.MoveCardsToSupportArea(walkers, seeking1, seeking2, relics1);
		scn.AttachCardsTo(gandalf, evil);
		scn.AttachCardsTo(frodo, poison);
		// Not active on this turn: the Free Peoples player's own Shadow condition and the Shadow player's Free Peoples condition.
		scn.MoveCardsToSupportArea(ownRelics, shadowMallorn);

		scn.StartGame();

		// The move of the fellowship phase does not offer it.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tall));

		scn.SkipToMovementDecision();
		// Hindered already (after the Shadow player's regroup reconcile, which would restore it): no longer a
		// condition, so neither hindered again nor counted.
		scn.HinderCard(seeking2);
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tall));
		assertFalse(scn.IsHindered(walkers));
		assertFalse(scn.IsHindered(seeking1));
		assertFalse(scn.IsHindered(relics1));
		assertFalse(scn.IsHindered(evil));
		assertTrue(scn.IsHindered(seeking2));
		int twilight = scn.GetTwilight();

		scn.FreepsChooseOptionalTrigger(tall);

		assertInDiscard(tall);
		assertTrue(scn.IsHindered(walkers));
		assertTrue(scn.IsHindered(seeking1));
		assertTrue(scn.IsHindered(relics1));
		assertTrue(scn.IsHindered(evil));
		assertTrue(scn.IsHindered(seeking2));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(ownRelics));
		assertFalse(scn.IsHindered(shadowMallorn));
		assertFalse(scn.IsHindered(gandalf));
		// 3 for Seeking Its Master, Relics of Moria and You Bring Great Evil;
		// then the move to Rivendell Valley: 0 for its Shadow number + 1 for each of 2 companions
		assertEquals(twilight + 3 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwilightAddedForShadowConditionsIsLimitedToFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tall = scn.GetFreepsCard("tall");
		var gandalf = scn.GetFreepsCard("gandalf");
		var seeking1 = scn.GetShadowCard("seeking1");
		var seeking2 = scn.GetShadowCard("seeking2");
		var seeking3 = scn.GetShadowCard("seeking3");
		var relics1 = scn.GetShadowCard("relics1");
		var relics2 = scn.GetShadowCard("relics2");
		var relics3 = scn.GetShadowCard("relics3");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(tall);
		scn.MoveCardsToSupportArea(seeking1, seeking2, seeking3, relics1, relics2, relics3);

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tall));
		int twilight = scn.GetTwilight();

		scn.FreepsChooseOptionalTrigger(tall);

		assertTrue(scn.IsHindered(seeking1));
		assertTrue(scn.IsHindered(seeking2));
		assertTrue(scn.IsHindered(seeking3));
		assertTrue(scn.IsHindered(relics1));
		assertTrue(scn.IsHindered(relics2));
		assertTrue(scn.IsHindered(relics3));
		// 6 Shadow conditions hindered, limit 5; then 0 for Rivendell Valley + 1 for each of 2 companions
		assertEquals(twilight + 5 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenOnlyAGandalfCompanionWhoIsNotGandalfCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tall = scn.GetFreepsCard("tall");
		var avenger = scn.GetFreepsCard("avenger");
		var seeking1 = scn.GetShadowCard("seeking1");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(tall);
		scn.MoveCardsToSupportArea(seeking1);

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tall));
		assertFalse(scn.IsHindered(seeking1));
		assertInHand(tall);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
