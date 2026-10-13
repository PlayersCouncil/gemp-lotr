package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_063_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stand", "51_63");

					put("lorien", "1_53");        // Lorien Elf: Elf companion, vitality 2, no text in play
					put("celeborn", "1_34");      // Celeborn, Lord of Lorien: Elf ally, vitality 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose

					put("warden", "1_259");       // Morgul Warden: [sauron] Orc, vitality 3; strength +1 for each other [sauron] Orc
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2; damage +1 is its whole text
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("deadones", "6_100");     // Dead Ones: [sauron] Wraith, vitality 1; twilight and damage +1, and a cost to play (cheated in)
					put("patrol", "1_177");       // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("seeking", "1_276");      // Seeking Its Master: [sauron] condition in the support area; a Shadow ability only
					put("pinned", "1_192");       // Pinned Down: [moria] condition in the support area; nothing without a [moria] archer
					put("scimitar", "1_269");     // Orc Scimitar: [sauron] possession, bearer must be a [sauron] Orc; a skirmish ability only
					put("goblinblade", "1_180");  // Goblin Scimitar: [moria] possession, bearer must be a [moria] Orc; a when-played draw only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StandAgainstDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Stand Against Darkness
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert an Elf to wound a [sauron] minion twice or discard a [sauron] condition or possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stand");

		assertEquals("Stand Against Darkness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnElfToWoundTheChosenSauronMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var warden = scn.GetShadowCard("warden");
		var guard = scn.GetShadowCard("guard");
		var deadones = scn.GetShadowCard("deadones");
		var patrol = scn.GetShadowCard("patrol");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCardsToHand(stand);
		scn.MoveCompanionsToTable(lorien, aragorn);
		// An Elf companion, an Elf ally, and a companion who could exert and is no Elf.
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(warden, guard, deadones, patrol);
		// A [sauron] condition in play, so that the discard could be chosen too.
		scn.MoveCardsToSupportArea(seeking);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.FreepsPlayAvailable(stand));

		scn.FreepsPlayCard(stand);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorien, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(celeborn);
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsChoiceAvailable("Wound"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		scn.FreepsChooseOption("Wound");

		// The wound was open to choose because it can be done in full, so whom to wound is held to that too
		// (Q-0209): the [sauron] minions of 3 and of 2 vitality, and not the one of 1.
		assertTrue(scn.FreepsHasCardChoiceAvailable(warden, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deadones, patrol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(warden);

		assertEquals(2, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, deadones);
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertInZone(Zone.SUPPORT, seeking);
		assertInDiscard(stand);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOnlyASauronMinionToActOnItIsWoundedTwiceAndCanBeKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var lorien = scn.GetFreepsCard("lorien");
		var soldier = scn.GetShadowCard("soldier");
		var patrol = scn.GetShadowCard("patrol");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveCardsToHand(stand);
		scn.MoveCompanionsToTable(lorien);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, patrol);
		// A [moria] condition only: nothing the discard could take.
		scn.MoveCardsToSupportArea(pinned);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(lorien));

		scn.FreepsPlayCard(stand);
		// One Elf and one [sauron] minion: neither is asked for, and with no [sauron] condition or possession
		// the wound is the only thing the event can do in full, so no choice between the two is offered.
		assertFalse(scn.FreepsChoiceAvailable("Discard"));
		assertEquals(1, scn.GetWoundsOn(lorien));
		// Orc Soldier's vitality is 2: two wounds kill him, which an exertion could not have done.
		assertInDiscard(soldier);
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertInZone(Zone.SUPPORT, pinned);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardsTheChosenSauronConditionOrPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var lorien = scn.GetFreepsCard("lorien");
		var warden = scn.GetShadowCard("warden");
		var patrol = scn.GetShadowCard("patrol");
		var seeking = scn.GetShadowCard("seeking");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");
		var goblinblade = scn.GetShadowCard("goblinblade");
		scn.MoveCardsToHand(stand);
		scn.MoveCompanionsToTable(lorien);

		scn.StartGame();
		scn.MoveMinionsToTable(warden, patrol);
		scn.AttachCardsTo(warden, scimitar);
		scn.AttachCardsTo(patrol, goblinblade);
		scn.MoveCardsToSupportArea(seeking, pinned);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(warden));

		scn.FreepsPlayCard(stand);
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertTrue(scn.FreepsChoiceAvailable("Wound"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		scn.FreepsChooseOption("Discard");

		assertTrue(scn.FreepsHasCardChoiceAvailable(seeking, scimitar));
		// The [sauron] minion is not a condition or possession; the others are [moria].
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(warden, pinned, goblinblade, patrol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(scimitar);

		assertInDiscard(scimitar);
		assertInZone(Zone.SUPPORT, seeking, pinned);
		assertInZone(Zone.ATTACHED, goblinblade);
		assertEquals(0, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertInDiscard(stand);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoSauronMinionTheSauronConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var lorien = scn.GetFreepsCard("lorien");
		var patrol = scn.GetShadowCard("patrol");
		var seeking = scn.GetShadowCard("seeking");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveCardsToHand(stand);
		scn.MoveCompanionsToTable(lorien);

		scn.StartGame();
		// A [moria] minion only, so that there is a maneuver phase and nothing the wound could take.
		scn.MoveMinionsToTable(patrol);
		scn.MoveCardsToSupportArea(seeking, pinned);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(lorien));

		scn.FreepsPlayCard(stand);
		// The discard is the only thing the event can do in full: no choice between the two is offered, and
		// the one [sauron] condition is taken without asking.
		assertFalse(scn.FreepsChoiceAvailable("Wound"));
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertInDiscard(seeking);
		assertInZone(Zone.SUPPORT, pinned);
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var lorien = scn.GetFreepsCard("lorien");
		var aragorn = scn.GetFreepsCard("aragorn");
		var warden = scn.GetShadowCard("warden");
		scn.MoveCardsToHand(stand);
		scn.MoveCompanionsToTable(lorien, aragorn);

		scn.StartGame();
		// Vitality 2 with 1 wound: exhausted. Aragorn, who could exert, is no Elf.
		scn.AddWoundsToChar(lorien, 1);
		scn.MoveMinionsToTable(warden);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsPlayAvailable(stand));

		scn.RemoveWoundsFromChar(lorien, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(stand));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
