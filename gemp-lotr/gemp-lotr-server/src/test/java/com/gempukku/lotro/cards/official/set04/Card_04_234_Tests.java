package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_234_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flanking", "4_234");

					// Southron Explorer: [raider] Southron Man, strength 5, vitality 3; a regroup ability used only on purpose
					put("explorer", "4_250");
					put("explorer2", "4_250");   // a second copy: not unique
					// Bladetusk Rearguard: [raider] artifact whose maneuver ability (remove (5)) makes it a fierce
					// mounted Southron minion, strength 5, vitality 5, until the end of the turn: a Southron who is no Man
					put("rearguard", "103_43");
					// Easterling Infantry: [raider] Man who is no Southron; text only at 3 burdens
					put("infantry", "4_227");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlankingAttackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Flanking Attack
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver or Skirmish
		 * Game Text: <b>Maneuver</b> <i>or</i> <b>Skirmish:</b> Exert a Southron to add (3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flanking");

		assertEquals("Flanking Attack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InTheManeuverPhaseExertsASouthronOfEitherRaceToAddThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flanking = scn.GetShadowCard("flanking");
		var explorer = scn.GetShadowCard("explorer");
		var rearguard = scn.GetShadowCard("rearguard");
		var infantry = scn.GetShadowCard("infantry");
		scn.MoveCardsToHand(flanking);
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, infantry);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The Rearguard's own ability, (5) removed: a Southron minion now, and no Man.
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(rearguard));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertTrue(scn.ShadowPlayAvailable(flanking));
		scn.ShadowPlayCard(flanking);

		// A Southron Man and a Southron who is no Man; not the [raider] Man who is no Southron.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rearguard, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(infantry, scn.GetRingBearer()));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(rearguard);

		assertEquals(1, scn.GetWoundsOn(rearguard));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(infantry));
		// 0 + 3 added
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(flanking);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void InASkirmishItExertsASouthronToAddThreeAndInTheAssignmentPhaseItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flanking = scn.GetShadowCard("flanking");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var infantry = scn.GetShadowCard("infantry");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(flanking);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, explorer2, infantry);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The phase next door to the skirmish, with a Southron able to exert.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(flanking));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(frodo, explorer);
		// The other two are left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.ShadowPlayAvailable(flanking));
		scn.ShadowPlayCard(flanking);

		// Either Southron, in the skirmish or not; not the [raider] Man who is no Southron.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, explorer2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(infantry, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(explorer2);
		assertEquals(1, scn.GetWoundsOn(explorer2));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(infantry));
		assertEquals(pool + 3, scn.GetTwilight());
		assertInDiscard(flanking);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlySouthronIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flanking = scn.GetShadowCard("flanking");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		scn.MoveCardsToHand(flanking);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, infantry);
		// Vitality 3 with 2 wounds: exhausted; the Infantry beside him could exert, and is no Southron.
		scn.AddWoundsToChar(explorer, 2);
		assertEquals(2, scn.GetWoundsOn(explorer));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(flanking));

		scn.RemoveWoundsFromChar(explorer, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(flanking));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
