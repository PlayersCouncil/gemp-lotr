package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dam", "101_65");
					put("foul", "2_58");
					put("watcher", "2_73");

					put("aragorn", "1_89");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SirannonDamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Sirannon Dam
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: Rivers and sites with no other terrain keywords gain <b>marsh</b>.
		* 	Companions are <b>defender +1</b>.
		* 	Skirmish: Remove 2 burdens to have Watcher in the Water replace a tentacle skirmishing an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dam");

		assertEquals("Sirannon Dam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SirannonDamMakesCompanionsDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var dam = scn.GetShadowCard("dam");
		scn.MoveCardsToSupportArea(dam);

		scn.StartGame();
		assertTrue(scn.HasKeyword(frodo, Keyword.DEFENDER));
	}

	@Test
	public void SirannonDamMakesSitesWithNoKeywordGainMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var dam = scn.GetShadowCard("dam");
		scn.MoveCardsToHand(dam);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		scn.ShadowPlayCard(dam);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
	}

	@Test
	public void SirannonDamCanRemove2BurdensToMakeWatcherReplaceTentacleInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionToTable(aragorn);

		var dam = scn.GetShadowCard("dam");
		var foul = scn.GetShadowCard("foul");
		var watcher = scn.GetShadowCard("watcher");
		scn.MoveCardsToSupportArea(dam);
		scn.MoveCardsToHand(foul, watcher);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(foul);
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowPlayCard(watcher);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, foul);
		scn.ShadowAssignToMinions(frodo, watcher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.IsCharSkirmishing(foul));

		assertFalse(scn.IsCharSkirmishing(watcher));
		assertTrue(scn.IsCharAssignedAgainst(watcher, frodo));
		//Because Aragorn is the active skirmish, there is no pending assignment for him
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, foul));
		assertFalse(scn.IsCharAssignedAgainst(aragorn, foul));
		assertTrue(scn.ShadowActionAvailable(dam));

		scn.ShadowUseCardAction(dam);
		assertEquals(1, scn.GetBurdens());

		assertFalse(scn.IsCharSkirmishing(foul));
		assertTrue(scn.IsCharSkirmishing(watcher));
		assertFalse(scn.IsCharAssignedAgainst(watcher, frodo));
		assertFalse(scn.IsCharSkirmishingAgainst(aragorn, foul));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, watcher));
	}
}
