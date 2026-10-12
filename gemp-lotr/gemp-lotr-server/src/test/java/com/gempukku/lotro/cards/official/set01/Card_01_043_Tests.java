package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eyes", "1_43");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, cost 2
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally, cost 4; to play, spot Gandalf or an Elf
					put("lorien", "1_53");      // Lorien Elf: [elven] Elf companion, cost 1; to play, spot an Elf
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, cost 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4
					put("card1", "1_41");       // Elven Bow: an [elven] possession an Elf bears, cost 1
					put("card2", "1_42");       // Elven Cloak: an [elven] possession
					put("runner", "1_178");     // Goblin Runner: a Shadow minion
					put("fear", "1_278");       // Strength Born of Fear: a Shadow condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarseeingEyesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Far-seeing Eyes
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Each time you play an Elf, choose an opponent to discard a card from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eyes");

		assertEquals("Far-seeing Eyes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FarseeingEyesMakesShadowDiscardCardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var lorien = scn.GetFreepsCard("lorien");
		var orophin = scn.GetFreepsCard("orophin");
		// A card in the Free Peoples player's own hand that is never played.
		var ownCloak = scn.GetFreepsCard("card2");
		scn.MoveCardsToHand(eyes, arwen, elrond, lorien, orophin, ownCloak);

		// The Shadow player's hand: a minion, a condition and his copy of a Free Peoples possession.
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		var bow = scn.GetShadowCard("card1");
		scn.MoveCardsToHand(runner, fear, bow);

		scn.StartGame();

		// The condition itself is an [elven] card and no Elf: nobody discards for it.
		scn.FreepsPlayCard(eyes);
		assertInZone(Zone.SUPPORT, eyes);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An Elf companion: the opponent discards one card, and which is for him to say.
		scn.FreepsPlayCard(arwen);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, fear, bow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ownCloak));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(fear);
		assertInZone(Zone.DISCARD, fear);
		assertInZone(Zone.HAND, runner, bow);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		// The Free Peoples hand has lost the two cards played and nothing else.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An Elf ally: a second discard.
		scn.FreepsPlayCard(elrond);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, bow));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(bow);
		assertInZone(Zone.DISCARD, bow);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// His last card goes without a choice.
		scn.FreepsPlayCard(lorien);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With no card in his hand the next Elf is played and nobody is asked anything.
		scn.FreepsPlayCard(orophin);
		assertInZone(Zone.SUPPORT, orophin);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, ownCloak);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionWhoIsNoElfAndAnElvenPossessionAreNoElfPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("card1");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(eyes);
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(aragorn, bow);
		scn.MoveCardsToHand(runner, fear);

		scn.StartGame();

		assertEquals(2, scn.GetShadowHandCount());

		// A companion who is no Elf
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An [elven] card that is no Elf, played on an Elf
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, arwen);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
