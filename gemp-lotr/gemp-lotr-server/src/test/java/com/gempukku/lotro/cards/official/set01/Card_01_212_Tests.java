package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_212_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fear", "1_212");

					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] Nazgul, vitality 2; text on companions bearing [wraith] conditions only
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, vitality 3; the rest needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgul, vitality 2; the rest needs 2 wounds on the Ring-bearer

					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an [elven] Elf ally, home site 3; a fellowship ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man ally, home site 1; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Fear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Search</b>.<br><b>Maneuver:</b> Exert your Nazgûl to discard an ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fear");

		assertEquals("Fear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsANazgulOfEitherCultureToDiscardTheAllyChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var elrond = scn.GetFreepsCard("elrond");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond, barliman);
		scn.MoveCardsToHand(fear);

		scn.StartGame();
		assertInPlay(elrond, barliman, aragorn);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fear));
		scn.ShadowPlayCard(fear);

		// A Nazgul of either culture exerts; not the [wraith] Orc, who could.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));

		// Allies of two cultures, races and home sites; not a companion, nor the Ring-bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, barliman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(barliman);

		assertInDiscard(barliman, fear);
		assertInPlay(elrond, aragorn, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 2 with no wound: Lemenya can exert.
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowPlayAvailable(fear));

		scn.AddWoundsToChar(lemenya, 1);
		scn.ShadowRefreshDecision();

		// Vitality 2 with 1 wound: exhausted.  The [wraith] Orc beside him could exert, and an ally is there.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertFalse(scn.ShadowPlayAvailable(fear));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertInPlay(elrond);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoAllyInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// No ally to discard: the text has no requirement, so the event is offered, the Nazgul exerts and nothing
		// more happens.
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowPlayAvailable(fear));
		scn.ShadowPlayCard(fear);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInPlay(aragorn, frodo);
		assertInDiscard(fear);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
