package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "10_56");
					put("warrior2", "10_56");   // a second copy: not unique, and "this minion" is the one used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("cloak", "1_42");       // Elven Cloak: a Free Peoples possession borne by a companion; minion archery total -1
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an ARTIFACT on a [gondor] Man; its response is used only on purpose
					put("toby", "1_305");       // Old Toby: a Free Peoples possession in the support area, borne by nobody
					put("banner", "1_263");     // Orc Banner: a [sauron] condition in the support area; acts only when a skirmish is lost
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; a play trigger only
					put("scimitar", "1_180");   // Goblin Scimitar: a Shadow possession on a [moria] Orc; a play trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cirith Ungol Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Shadow:</b> Spot a possession to make this minion <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Cirith Ungol Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityMakesThisMinionFierceUntilTheRegroupPhaseWhileAPossessionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn);
		// The one possession in play: the Free Peoples player's, borne by a companion.
		scn.AttachCardsTo(aragorn, cloak);
		assertAttachedTo(cloak, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(warrior2, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(warrior));

		scn.ShadowUseCardAction(warrior);
		assertTrue(scn.HasKeyword(warrior, Keyword.FIERCE));
		// The other copy is not made fierce.
		assertFalse(scn.HasKeyword(warrior2, Keyword.FIERCE));

		// Fierce already, it may be made fierce again: the offer stands (Q-0245).
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowPass();

		// Nobody is assigned, so the fierce assignments follow at once, the last before the regroup phase: this
		// Warrior is still fierce and is the one minion to be assigned.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(warrior, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(warrior));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(warrior2));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithOnlyAnArtifactAndAConditionAndIsWithAPossessionNobodyBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var toby = scn.GetFreepsCard("toby");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, narsil);
		scn.MoveCardsToSupportArea(banner);
		assertAttachedTo(narsil, aragorn);
		assertInZone(Zone.SUPPORT, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.FreepsPass();

		// An artifact, a condition and The One Ring: none of them a possession.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));

		// A Free Peoples possession in the support area, borne by nobody, and it is.
		scn.MoveCardsToSupportArea(toby);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(warrior));
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
	}

	@Test
	public void ShadowAbilityIsOfferedWhenTheOnlyPossessionIsBorneByAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, runner);
		scn.AttachCardsTo(runner, scimitar);
		assertAttachedTo(scimitar, runner);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);

		assertTrue(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
