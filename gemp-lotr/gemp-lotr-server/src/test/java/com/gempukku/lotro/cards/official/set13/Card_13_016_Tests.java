package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("song", "13_16");
					put("song2", "13_16");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; +3 only against a Nazgûl
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally; a fellowship ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text

					put("border", "1_32");      // Border Defenses: [elven] Skirmish event
					put("defiance", "1_37");    // Defiance: [elven] Skirmish event
					put("stand", "1_63");       // Stand Against Darkness: [elven] Maneuver event
					put("servant", "1_83");     // Servant of the Secret Fire: [gandalf] Skirmish event
					put("eyes", "1_43");        // Far-seeing Eyes: [elven] condition
					put("enmity", "1_29");      // Ancient Enmity: [elven] Skirmish event, laid only in the Shadow player's discard pile

					put("shake", "8_13");       // Shake Off the Shadow: [elven] support-area condition that holds [elven] tokens; abilities used only on purpose
					put("strands", "9_22");     // Strands of Elven Hair: [elven] support-area possession that holds [elven] tokens; a fellowship ability only
					put("axe", "4_52");         // My Axe Is Notched: [dwarven] support-area condition that holds [dwarven] tokens; reads Gimli only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void InsideaSongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Inside a Song
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert an Elf to take an [elven] skirmish event into hand from your discard pile and you may reinforce an [elven] token.
		*/

		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");

		assertEquals("Inside a Song", song.getBlueprint().getTitle());
		assertNull(song.getBlueprint().getSubtitle());
		assertFalse(song.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, song.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, song.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, song.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(song, Timeword.MANEUVER));
		assertEquals(0, song.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnElfToTakeAnElvenSkirmishEventIntoHandAndMayReinforceAnElvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var song2 = scn.GetFreepsCard("song2");
		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var knight = scn.GetFreepsCard("knight");
		var border = scn.GetFreepsCard("border");
		var defiance = scn.GetFreepsCard("defiance");
		var stand = scn.GetFreepsCard("stand");
		var servant = scn.GetFreepsCard("servant");
		var eyes = scn.GetFreepsCard("eyes");
		var shake = scn.GetFreepsCard("shake");
		var strands = scn.GetFreepsCard("strands");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(arwen, knight);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(shake, strands, axe);
		scn.MoveCardsToDiscard(border, defiance, stand, servant, eyes);
		scn.MoveCardsToHand(song, song2);
		assertInHand(song, song2);
		assertInDiscard(border, defiance, stand, servant, eyes);

		var enmity = scn.GetShadowCard("enmity");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(enmity);
		assertInDiscard(enmity);

		scn.StartGame();
		scn.AddTokensToCard(shake, 1);
		scn.AddTokensToCard(strands, 1);
		scn.AddTokensToCard(axe, 1);
		assertEquals(1, scn.GetCultureTokensOn(shake));
		assertEquals(1, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(axe));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsPlayAvailable(song));

		scn.FreepsPlayCard(song);
		// The cost: an Elf, companion or ally; not the [gondor] Man, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		scn.FreepsChooseCard(celeborn);
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(knight));

		// The [elven] Skirmish events of his own discard pile; not the [elven] Maneuver event, the [gandalf] Skirmish
		// event, the [elven] condition, nor the Shadow player's [elven] Skirmish event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(border, defiance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stand, servant, eyes, enmity));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(defiance);
		assertInHand(defiance);
		assertInDiscard(border, stand, servant, eyes);
		assertInDiscard(enmity);

		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseYes();
		// The cards with an [elven] token, a condition and a possession; not the [dwarven] condition with its own token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(shake, strands));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe));
		scn.FreepsChooseCard(strands);
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(shake));
		assertEquals(1, scn.GetCultureTokensOn(axe));
		assertInDiscard(song);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The second copy, paid by Arwen.  The reinforce is declined and no token moves.
		scn.ShadowPass();
		scn.FreepsPlayCard(song2);
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		// Border Defenses, the one [elven] Skirmish event left in the pile, is taken unasked.
		assertInHand(border);
		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseNo();
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(shake));
		assertEquals(1, scn.GetCultureTokensOn(axe));
		assertInDiscard(song2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoElvenSkirmishEventInTheDiscardPileTheElfExertsAndNothingIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var arwen = scn.GetFreepsCard("arwen");
		var stand = scn.GetFreepsCard("stand");
		var servant = scn.GetFreepsCard("servant");
		var shake = scn.GetFreepsCard("shake");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(shake);
		scn.MoveCardsToDiscard(stand, servant);
		scn.MoveCardsToHand(song);
		assertInHand(song);
		assertInDiscard(stand, servant);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddTokensToCard(shake, 1);
		assertEquals(1, scn.GetCultureTokensOn(shake));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(arwen));
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsPlayAvailable(song));

		scn.FreepsPlayCard(song);
		// Arwen, the one Elf, pays unasked.  A Maneuver event and a [gandalf] event are no [elven] Skirmish events:
		// nothing is taken.  The reinforce does not hang on the take, and is asked.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseYes();
		assertEquals(2, scn.GetCultureTokensOn(shake));
		assertInDiscard(stand, servant);
		assertInDiscard(song);
		// The event left the hand, and nothing came into it.
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoElvenTokenInPlayTheReinforceIsNotAskedAndTheEventIsStillTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var shake = scn.GetFreepsCard("shake");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(shake, axe);
		scn.MoveCardsToDiscard(defiance);
		scn.MoveCardsToHand(song);
		assertInHand(song);
		assertInDiscard(defiance);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// An [elven] card with no token, and a token of another culture: nothing an [elven] reinforce can take.
		scn.AddTokensToCard(axe, 1);
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(1, scn.GetCultureTokensOn(axe));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsPlayCard(song);
		// The lone Elf pays and the lone [elven] Skirmish event is taken, both unasked.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInHand(defiance);
		assertFalse(scn.FreepsDecisionAvailable("reinforce"));
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(1, scn.GetCultureTokensOn(axe));
		assertInDiscard(song);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileEveryElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCompanionsToTable(arwen, knight);
		scn.MoveCardsToDiscard(defiance);
		scn.MoveCardsToHand(song);
		assertInHand(song);
		assertInDiscard(defiance);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Vitality 3 with 2 wounds: Arwen, the one Elf, is exhausted; the Knight beside her could exert, but is no Elf.
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(song));
		assertInHand(song);
	}
}
