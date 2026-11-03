package com.gempukku.lotro.cards.official.set05;

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

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cover", "5_40");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man knight companion, no other text
					// Pippin, Trollslayer: a [gondor] Hobbit knight companion; his skirmish ability is never used
					put("pippin", "103_31");
					// Gondorian Merchant: a [gondor] Man ally of vitality 1; Armor of the White City makes him a knight,
					// and Narsil's vitality +1 lets him exert (its wound response never comes up)
					put("merchant", "7_97");
					put("whitearmor", "18_39");
					put("narsil", "9_34");
					// Ranger of Osgiliath: a [gondor] Man companion who is no knight; a regroup ability, never used
					put("ranger", "7_116");
					put("wall", "5_33");        // City Wall: a [gondor] fortification, plays to the support area
					put("citadel", "5_32");     // Citadel of the Stars: a unique [gondor] fortification
					put("tower", "5_39");       // Stone Tower: a unique [gondor] fortification
					put("tower2", "5_39");
					// Armory: a [rohan] fortification with no requirement to play; a maneuver ability, never used
					put("armory", "5_79");
					// Answering the Cries: a [gondor] condition that is no fortification, with no requirement to play
					put("cries", "3_37");
					put("first", "7_92");       // First Level: a [gondor] fortification, put in the Shadow player's pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TakeCoverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Take Cover
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert a knight to play a [gondor] fortification from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cover");

		assertEquals("Take Cover", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAKnightToPlayAGondorFortificationFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetFreepsCard("cover");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var whitearmor = scn.GetFreepsCard("whitearmor");
		var narsil = scn.GetFreepsCard("narsil");
		var ranger = scn.GetFreepsCard("ranger");
		var wall = scn.GetFreepsCard("wall");
		var citadel = scn.GetFreepsCard("citadel");
		var armory = scn.GetFreepsCard("armory");
		var cries = scn.GetFreepsCard("cries");
		var othersFirst = scn.GetShadowCard("first");
		scn.MoveCompanionsToTable(knight, pippin, ranger);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, whitearmor, narsil);
		scn.MoveCardsToHand(cover);
		scn.MoveCardsToDiscard(wall, citadel, armory, cries);
		scn.MoveCardsToDiscard(othersFirst);

		scn.StartGame();
		assertInHand(cover);
		assertInDiscard(wall, citadel, armory, cries, othersFirst);
		assertAttachedTo(whitearmor, merchant);
		assertAttachedTo(narsil, merchant);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(ranger));

		assertTrue(scn.FreepsPlayAvailable(cover));
		scn.FreepsPlayCard(cover);
		// Any knight: a Man companion, a Hobbit companion and an ally; not a [gondor] Man who is no knight.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(ranger));

		// Either [gondor] fortification in the player's own pile; not the [rohan] fortification, nor the [gondor]
		// condition that is no fortification, nor the [gondor] fortification in the other player's pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wall, citadel));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory, cries, othersFirst));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(wall);

		assertInZone(Zone.SUPPORT, wall);
		assertInDiscard(citadel, armory, cries, othersFirst);
		assertInDiscard(cover);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileTheOnlyKnightIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetFreepsCard("cover");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.MoveCardsToHand(cover);
		scn.MoveCardsToDiscard(wall);
		// Vitality 3 with 2 wounds: the knight cannot exert. The Ranger, who could, is no knight.
		scn.AddWoundsToChar(knight, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertInDiscard(wall);
		assertInHand(cover);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(cover));
	}

	@Test
	public void IsNotPlayableWithNoGondorFortificationInTheDiscardPileThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cover = scn.GetFreepsCard("cover");
		var knight = scn.GetFreepsCard("knight");
		var tower = scn.GetFreepsCard("tower");
		var tower2 = scn.GetFreepsCard("tower2");
		var armory = scn.GetFreepsCard("armory");
		var cries = scn.GetFreepsCard("cries");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(cover);
		// A Stone Tower is in play, so the unique one in the pile cannot be played; the other two cards could be
		// played, but are no [gondor] fortifications.
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToDiscard(tower2, armory, cries);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tower);
		assertInDiscard(tower2, armory, cries);
		assertInHand(cover);
		assertEquals(0, scn.GetWoundsOn(knight));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(cover));
	}
}
