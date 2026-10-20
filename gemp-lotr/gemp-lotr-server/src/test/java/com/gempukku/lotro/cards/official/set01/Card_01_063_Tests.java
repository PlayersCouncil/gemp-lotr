package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_063_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stand", "1_63");

					put("lorien", "1_53");        // Lorien Elf: Elf companion, vitality 2, no text in play
					put("celeborn", "1_34");      // Celeborn, Lord of Lorien: Elf ally, vitality 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose

					put("warden", "1_259");       // Morgul Warden: [sauron] Orc; strength +1 for each other [sauron] Orc
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai; damage +1 is its whole text
					put("patrol", "1_177");       // Goblin Patrol Troop: [moria] Orc, no text
					put("seeking", "1_276");      // Seeking Its Master: [sauron] condition in the support area; a Shadow ability only
					put("evil", "1_283");         // You Bring Great Evil: [sauron] condition borne by a companion; nothing away from a sanctuary
					put("pinned", "1_192");       // Pinned Down: [moria] condition in the support area; nothing without a [moria] archer
					put("scimitar", "1_269");     // Orc Scimitar: [sauron] possession, bearer must be a [sauron] Orc; a skirmish ability only
					put("goblinblade", "1_180");  // Goblin Scimitar: [moria] possession, bearer must be a [moria] Orc; a when-played draw only
					put("ithil", "9_47");         // Ithil Stone: a [sauron] ARTIFACT in the support area; a Shadow-phase draw and ability only
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
		 * Game Text: <b>Maneuver:</b> Exert an Elf to discard a [sauron] minion, a [sauron] condition, or a [sauron] possession.
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
	public void ExertsAnElfToDiscardTheChosenSauronMinionConditionOrPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var warden = scn.GetShadowCard("warden");
		var guard = scn.GetShadowCard("guard");
		var patrol = scn.GetShadowCard("patrol");
		var seeking = scn.GetShadowCard("seeking");
		var evil = scn.GetShadowCard("evil");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");
		var goblinblade = scn.GetShadowCard("goblinblade");
		var ithil = scn.GetShadowCard("ithil");
		scn.MoveCardsToHand(stand);
		// An Elf companion, an Elf ally, and a companion who could exert and is no Elf.
		scn.MoveCompanionsToTable(lorien, aragorn);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(warden, guard, patrol);
		scn.AttachCardsTo(warden, scimitar);
		scn.AttachCardsTo(patrol, goblinblade);
		scn.AttachCardsTo(aragorn, evil);
		scn.MoveCardsToSupportArea(seeking, pinned);
		scn.SkipToPhase(Phase.MANEUVER);
		// Laid here, past the Shadow phase whose start its draw answers.
		scn.MoveCardsToSupportArea(ithil);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(stand));

		scn.FreepsPlayCard(stand);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorien, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(celeborn);
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// [sauron] minions of two races, a [sauron] condition in the support area and one on a companion, and a
		// [sauron] possession; not a [sauron] artifact, nor the [moria] minion, condition and possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(warden, guard, seeking, evil, scimitar));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ithil, patrol, pinned, goblinblade, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInDiscard(guard, stand);
		assertInZone(Zone.SHADOW_CHARACTERS, warden, patrol);
		assertAttachedTo(scimitar, warden);
		assertAttachedTo(goblinblade, patrol);
		assertAttachedTo(evil, aragorn);
		assertInZone(Zone.SUPPORT, seeking, pinned, ithil);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardsASauronConditionBorneByACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var lorien = scn.GetFreepsCard("lorien");
		var aragorn = scn.GetFreepsCard("aragorn");
		var warden = scn.GetShadowCard("warden");
		var evil = scn.GetShadowCard("evil");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToHand(stand);
		scn.MoveCompanionsToTable(lorien, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(warden);
		scn.AttachCardsTo(warden, scimitar);
		scn.AttachCardsTo(aragorn, evil);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(lorien));
		assertAttachedTo(evil, aragorn);

		scn.FreepsPlayCard(stand);
		// One Elf: he exerts unasked.
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertTrue(scn.FreepsHasCardChoiceAvailable(warden, evil, scimitar));
		scn.FreepsChooseCard(evil);

		assertInDiscard(evil, stand);
		assertInZone(Zone.SHADOW_CHARACTERS, warden);
		assertAttachedTo(scimitar, warden);
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

		assertEquals(1, scn.GetWoundsOn(lorien));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stand));

		scn.RemoveWoundsFromChar(lorien, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(stand));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
