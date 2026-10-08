package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_136_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("power", "51_136");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc; a regroup ability only ever used on purpose
					put("goblins", "1_177");    // Goblin Patrol Troop: [moria] Orc, no text

					put("songs", "1_61");       // Songs of the Blessed Realm: Free Peoples condition in the support area; answers [elven] tales only
					put("eyes", "1_43");        // Far-seeing Eyes: Free Peoples condition in the support area; answers the play of an Elf only
					put("lord", "1_21");        // Lord of Moria: Free Peoples condition in the support area; each Dwarf is damage +1, and there are none
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf to bear the two below
					put("galad", "1_66");       // The Tale of Gil-galad: Free Peoples condition borne by an Elf; a skirmish ability
					put("bow", "1_41");         // Elven Bow: Free Peoples possession borne by an Elf; bearer is an archer

					put("armory", "1_157");     // Uruk-hai Armory: Shadow condition in the support area; touches only the archery total
					put("throw", "2_71");       // Throw Yourself in Next Time: Shadow condition on a Hobbit companion; bearer may be exerted only by Shadow cards
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansPowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Saruman's Power
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Spell</b>.
		 * Exert an [isengard] minion to discard a Free Peoples condition and then hinder all other conditions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("power");

		assertEquals("Saruman's Power", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnIsengardMinionToDiscardAFreePeoplesConditionAndHindersEveryOtherCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var goblins = scn.GetShadowCard("goblins");
		var armory = scn.GetShadowCard("armory");
		var throwIn = scn.GetShadowCard("throw");
		scn.MoveCardsToHand(power);
		scn.MoveCardsToSupportArea(armory);

		var frodo = scn.GetRingBearer();
		var songs = scn.GetFreepsCard("songs");
		var eyes = scn.GetFreepsCard("eyes");
		var lord = scn.GetFreepsCard("lord");
		var legolas = scn.GetFreepsCard("legolas");
		var galad = scn.GetFreepsCard("galad");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, galad, bow);
		scn.MoveCardsToSupportArea(songs, eyes, lord);

		scn.StartGame();
		scn.AttachCardsTo(frodo, throwIn);
		scn.MoveMinionsToTable(savage, servant, goblins);
		scn.HinderCard(lord);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(14, scn.GetTwilight());    // 10 set + 2 companions + 2 for Ettenmoors
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(goblins));
		assertTrue(scn.IsHindered(lord));
		assertFalse(scn.IsHindered(songs));
		assertFalse(scn.IsHindered(eyes));
		assertFalse(scn.IsHindered(galad));
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(throwIn));
		assertFalse(scn.IsHindered(bow));
		assertTrue(scn.ShadowPlayAvailable(power));

		scn.ShadowPlayCard(power);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(goblins));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(servant);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(goblins));

		// A hindered condition has no card type (CR 5.0 "hinder"), so Lord of Moria is not "a Free Peoples condition".
		assertTrue(scn.ShadowHasCardChoiceAvailable(songs, eyes, galad));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lord, armory, throwIn, bow));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(songs);

		assertInDiscard(songs);
		assertFalse(scn.IsHindered(songs));
		assertTrue(scn.IsHindered(eyes));
		assertTrue(scn.IsHindered(galad));
		assertTrue(scn.IsHindered(lord));
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(throwIn));
		assertFalse(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(legolas));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(savage));
		assertFalse(scn.IsHindered(servant));
		assertFalse(scn.IsHindered(goblins));

		assertEquals(12, scn.GetTwilight());    // 14 - 2 to play
		assertInDiscard(power);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesConditionToDiscardItStillHindersEveryCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		var throwIn = scn.GetShadowCard("throw");
		scn.MoveCardsToHand(power);
		scn.MoveCardsToSupportArea(armory);

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, bow);

		scn.StartGame();
		scn.AttachCardsTo(frodo, throwIn);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The cost can be paid, so the event is played; the discard finds nothing and the hinder is still done
		// (CR 4.2 "effect": as much as can be done is done).
		assertEquals(14, scn.GetTwilight());    // 10 set + 2 companions + 2 for Ettenmoors
		assertEquals(0, scn.GetWoundsOn(savage));
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(throwIn));
		assertFalse(scn.IsHindered(bow));
		assertTrue(scn.ShadowPlayAvailable(power));

		scn.ShadowPlayCard(power);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(throwIn));
		assertFalse(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(savage));
		assertEquals(12, scn.GetTwilight());    // 14 - 2 to play
		assertInDiscard(power);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var savage = scn.GetShadowCard("savage");
		var goblins = scn.GetShadowCard("goblins");
		scn.MoveCardsToHand(power);

		var songs = scn.GetFreepsCard("songs");
		scn.MoveCardsToSupportArea(songs);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, goblins);
		scn.AddWoundsToChar(savage, 2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Uruk Savage has vitality 3: with 2 wounds it cannot exert.  The [moria] minion beside it could.
		assertEquals(13, scn.GetTwilight());    // 10 set + 1 companion + 2 for Ettenmoors
		assertFalse(scn.ShadowPlayAvailable(power));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(goblins));
		assertFalse(scn.IsHindered(songs));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
