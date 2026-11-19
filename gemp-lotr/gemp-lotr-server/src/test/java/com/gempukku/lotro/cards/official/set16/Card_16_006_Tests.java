package com.gempukku.lotro.cards.official.set16;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_16_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("undead", "16_6");
					put("faces", "16_4");       // Dead Faces: [wraith] Wraith, cost 2; its trigger answers the Free Peoples player's assignment only
					put("corpses", "16_2");     // Candle Corpses: [wraith] Wraith of another title, cost 3; the same kind of trigger
					put("lights", "6_99");      // Corpse Lights: a Wraith who is not [wraith] ([sauron]), cost 3; a Skirmish ability only
					put("brute", "7_188");      // Morgul Brute: a [wraith] minion who is no Wraith (an Orc), cost 2; cheated in, so its play trigger never fires

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, resistance 6; a maneuver ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion, resistance 6; an archery ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UndeadofAngmarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 16
		 * Name: Undead of Angmar
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: <b>Enduring</b>. While you can spot another [wraith] Wraith, each wounded companion is resistance -2.<br><b>Shadow:</b> Exert Undead of Angmar twice to play a [wraith] Wraith from your draw deck, then reshuffle.
		*/

		var scn = GetScenario();

		var undead = scn.GetFreepsCard("undead");

		assertEquals("Undead of Angmar", undead.getBlueprint().getTitle());
		assertNull(undead.getBlueprint().getSubtitle());
		assertTrue(undead.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, undead.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, undead.getBlueprint().getCulture());
		assertEquals(CardType.MINION, undead.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, undead.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(undead, Keyword.ENDURING));
		assertEquals(4, undead.getBlueprint().getTwilightCost());
		assertEquals(9, undead.getBlueprint().getStrength());
		assertEquals(3, undead.getBlueprint().getVitality());
		assertEquals(2, undead.getBlueprint().getSiteNumber());
	}

	@Test
	public void OnceAnotherWraithWraithIsPlayedEachWoundedCompanionIsResistanceMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);

		var undead = scn.GetShadowCard("undead");
		var faces = scn.GetShadowCard("faces");
		var lights = scn.GetShadowCard("lights");
		var brute = scn.GetShadowCard("brute");
		scn.MoveCardsToHand(faces);

		scn.StartGame();
		scn.MoveMinionsToTable(undead, lights, brute);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertInHand(faces);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Corpse Lights is a Wraith of another culture and Morgul Brute a [wraith] minion who is no Wraith: neither is
		// "another [wraith] Wraith", and the Undead does not spot itself.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(legolas));

		scn.ShadowPlayCard(faces);
		assertInZone(Zone.SHADOW_CHARACTERS, faces);

		// Both wounded companions, of two cultures, lose 2; Legolas has no wound and keeps his 6.
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACandleCorpsesAsTheOtherWraithWraithAlsoMakesAWoundedCompanionResistanceMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var undead = scn.GetShadowCard("undead");
		var corpses = scn.GetShadowCard("corpses");
		scn.MoveCardsToHand(corpses);

		scn.StartGame();
		scn.MoveMinionsToTable(undead);
		scn.AddWoundsToChar(frodo, 1);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(corpses);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(10, scn.GetResistance(frodo));

		// Dead Faces is not the only card that is "another [wraith] Wraith".
		scn.ShadowPlayCard(corpses);
		assertInZone(Zone.SHADOW_CHARACTERS, corpses);
		assertEquals(8, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsTwiceToPlayAWraithWraithFromTheDrawDeckAndReshuffles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undead = scn.GetShadowCard("undead");
		var faces = scn.GetShadowCard("faces");
		var corpses = scn.GetShadowCard("corpses");
		var lights = scn.GetShadowCard("lights");
		var brute = scn.GetShadowCard("brute");

		scn.StartGame();
		scn.MoveMinionsToTable(undead);
		// Enough for any of the four: only the words keep Corpse Lights and the Brute out.
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, faces, corpses, lights, brute);

		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(undead));
		int shuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.ShadowActionAvailable(undead));
		scn.ShadowUseCardAction(undead);
		assertEquals(2, scn.GetWoundsOn(undead));

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(faces, corpses));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lights, brute));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(faces);

		assertInZone(Zone.SHADOW_CHARACTERS, faces);
		assertInZone(Zone.DECK, corpses, lights, brute);
		// One shuffle as the Dead Faces leaves the deck that was looked through, and the reshuffle the text asks for.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWraithWraithInTheDrawDeckTheExertionsArePaidForNothingAndTheDeckIsReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undead = scn.GetShadowCard("undead");
		var faces = scn.GetShadowCard("faces");
		var corpses = scn.GetShadowCard("corpses");
		var lights = scn.GetShadowCard("lights");
		var brute = scn.GetShadowCard("brute");
		scn.MoveCardsToDiscard(faces, corpses);

		scn.StartGame();
		scn.MoveMinionsToTable(undead);
		scn.SetTwilight(10);
		assertInDiscard(faces, corpses);
		assertInZone(Zone.DECK, lights, brute);

		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(undead));
		int shuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.ShadowActionAvailable(undead));
		scn.ShadowUseCardAction(undead);
		assertEquals(2, scn.GetWoundsOn(undead));

		// The deck is shown with nothing in it that can be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lights, false));
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.DECK, lights, brute);
		assertInDiscard(faces, corpses);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneWoundUndeadCannotExertTwiceAndTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undead = scn.GetShadowCard("undead");
		var faces = scn.GetShadowCard("faces");

		scn.StartGame();
		scn.MoveMinionsToTable(undead);
		scn.AddWoundsToChar(undead, 1);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetWoundsOn(undead));
		assertInZone(Zone.DECK, faces);

		scn.FreepsPass();

		// Vitality 3 with 1 wound: 2 left, and exerting twice wants 3.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(undead));
	}
}
