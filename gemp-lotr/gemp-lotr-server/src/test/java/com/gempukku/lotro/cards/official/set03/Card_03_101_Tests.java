package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "3_101");
					put("warrior2", "3_101");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability used only on purpose
					// Sauron's Defenses: [sauron] condition in the support area; its text wants 3 companions in the dead pile
					put("defenses", "1_274");
					// You Bring Great Evil: [sauron] condition borne by a companion; its text works only at a sanctuary
					put("evil", "1_283");
					// Traitor's Voice: [isengard] condition in the support area; its text wants an exhausted Ring-bearer or 5 burdens
					put("voice", "1_142");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Maneuver:</b> Exert this minion and discard your [sauron] condition to make the Free Peoples player discard the top 2 cards from his or her draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Orc Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsHimAndDiscardsYourSauronConditionToDiscardTheTopTwoCardsOfTheFreePeoplesDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var defenses = scn.GetShadowCard("defenses");
		var evil = scn.GetShadowCard("evil");
		var voice = scn.GetShadowCard("voice");
		// The Free Peoples player's own Sauron's Defenses, left in his support area from a Shadow turn of his: not "your".
		var fpDefenses = scn.GetFreepsCard("defenses");
		var aragorn = scn.GetFreepsCard("aragorn");
		var top1 = scn.GetFreepsCard("warrior");
		var top2 = scn.GetFreepsCard("warrior2");
		var top3 = scn.GetFreepsCard("voice");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(defenses, voice, fpDefenses);
		scn.AttachCardsTo(aragorn, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.MoveCardsToTopOfDeck(top3, top2, top1);
		assertInZone(Zone.SUPPORT, defenses, voice, fpDefenses);
		assertAttachedTo(evil, aragorn);
		assertEquals(top1, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(top2, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(top3, scn.GetFromTopOfFreepsDeck(3));
		assertInZone(Zone.DECK, top1, top2, top3);
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertTrue(scn.ShadowActionAvailable(warrior));

		scn.ShadowUseCardAction(warrior);
		// His own [sauron] conditions, in the support area and on a companion; not the [isengard] one, nor the
		// other player's, nor a [sauron] card that is no condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(defenses, evil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(voice, fpDefenses, warrior2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(evil);

		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(warrior2));
		assertInDiscard(evil);
		assertInZone(Zone.SUPPORT, defenses, voice, fpDefenses);
		// The top two cards of the Free Peoples draw deck, and no more.
		assertInDiscard(top1, top2);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(top3, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(freepsDeck - 2, scn.GetFreepsDeckCount());
		// The Shadow player's own deck is untouched, and his discard pile has only the condition.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileHeIsExhaustedThoughTheOtherCopyCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var defenses = scn.GetShadowCard("defenses");
		scn.MoveCardsToSupportArea(defenses);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(warrior, 2);
		assertEquals(2, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(warrior2));
		assertInZone(Zone.SUPPORT, defenses);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));
		assertTrue(scn.ShadowActionAvailable(warrior2));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithoutASauronConditionOfHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var voice = scn.GetShadowCard("voice");
		var fpDefenses = scn.GetFreepsCard("defenses");
		scn.MoveCardsToSupportArea(voice, fpDefenses);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		assertInZone(Zone.SUPPORT, voice, fpDefenses);
		assertEquals(0, scn.GetWoundsOn(warrior));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// An [isengard] condition of his own, and a [sauron] condition that is not his.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyTheCostsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var defenses = scn.GetShadowCard("defenses");
		scn.MoveCardsToSupportArea(defenses);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.MoveCardsToFreepsDiscard("warrior", "warrior2", "aragorn", "defenses", "evil", "voice");
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SUPPORT, defenses);
		int shadowDeck = scn.GetShadowDeckCount();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertTrue(scn.ShadowActionAvailable(warrior));

		scn.ShadowUseCardAction(warrior);
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertInDiscard(defenses);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
