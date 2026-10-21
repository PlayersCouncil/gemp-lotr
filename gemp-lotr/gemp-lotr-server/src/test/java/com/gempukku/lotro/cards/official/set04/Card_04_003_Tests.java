package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anger", "4_3");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					// Saruman, Rabble-rouser: the one [dunland] minion who is no Man (a Wizard).  He may not be assigned
					// to a skirmish; each time the fellowship moves, the Free Peoples player exerts a companion for each
					// [dunland] Man spotted, so he is laid after the move.
					put("saruman", "4_33");
					// Easterling Axeman: [raider] Man, strength 6, vitality 2; strength +3 and fierce only while 2
					// burdens can be spotted, and there are none.
					put("axeman", "4_224");
					// Hides: a [dunland] possession in the support area, no minion.  Its response to a wound on a
					// [dunland] Man is never used.
					put("hides", "4_19");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AngerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Anger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot 3 [dunland] minions or a site you control to make a [dunland] Man <b>fierce</b> until the regroup phase.
		 */

		var scn = GetScenario();

		var anger = scn.GetFreepsCard("anger");

		assertEquals("Anger", anger.getBlueprint().getTitle());
		assertNull(anger.getBlueprint().getSubtitle());
		assertFalse(anger.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, anger.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, anger.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, anger.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(anger, Timeword.SKIRMISH));
		assertEquals(0, anger.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingThreeDunlandMinionsMakesADunlandManFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var anger = scn.GetShadowCard("anger");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(anger);

		scn.StartGame();
		scn.FreepsPass();
		// Three [dunland] minions, one of them no Man, and a minion of another culture.  Nobody controls a site.
		scn.MoveMinionsToTable(brigand, savage, saruman, axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(anger));
		scn.ShadowPlayCard(anger);

		// The [dunland] Men, in the skirmish and assigned to none, are offered; the [dunland] Wizard and the
		// [raider] Man are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, axeman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertInDiscard(anger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 8: the Brigand wins.  The Savage, fierce, is assigned in the fierce round.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// In the fierce skirmish, after the first was resolved, the Savage is still fierce.
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 11.  At the first decision of the regroup phase he is fierce no longer.
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
	}

	@Test
	public void ASiteTheShadowPlayerControlsLetsItBePlayedWithTwoDunlandMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var anger = scn.GetShadowCard("anger");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Player 1's second turn: each player has moved to site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		scn.MoveCardsToHand(anger);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, savage);
		scn.ShadowTakeControlOfSite();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		// Two [dunland] minions, and a site the Shadow player controls.
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(anger));
		scn.ShadowPlayCard(anger);
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		scn.ShadowChooseCard(brigand);

		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertInDiscard(anger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTwoDunlandMinionsBesideOneOfAnotherCultureADunlandPossessionAndASiteTheOtherPlayerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var anger = scn.GetShadowCard("anger");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var hides = scn.GetShadowCard("hides");
		scn.MoveCardsToSupportArea(hides);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(anger);
		assertInHand(anger);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, savage, axeman);
		// Site 1 is in the Free Peoples player's hands, not the Shadow player's.
		scn.FreepsTakeControlOfSite();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Two [dunland] minions, a [raider] minion and a [dunland] card that is no minion: one short of three, and
		// the site controlled is not his.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(anger));
		assertInHand(anger);
	}

	@Test
	public void WithNoDunlandManInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var anger = scn.GetShadowCard("anger");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(anger);
		scn.FreepsPass();
		// The [dunland] Wizard and a [raider] Man: neither is a [dunland] Man.  The site is the Shadow player's.
		scn.MoveMinionsToTable(saruman, axeman);
		scn.ShadowTakeControlOfSite();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, axeman);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(anger));
		scn.ShadowPlayCard(anger);

		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertInDiscard(anger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
