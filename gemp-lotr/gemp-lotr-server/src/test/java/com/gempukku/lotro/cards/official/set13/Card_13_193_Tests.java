package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_193_Tests
{
	// An unordered Shadows-block path: the fellowship starts at Fangorn Glade and the Shadow player lays Isenwash
	// as the site it moves to, where its regroup phase is.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "5_83");           // Household Guard: [rohan] Man companion, vitality 3, valiant and nothing else
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability never used
					put("barliman", "1_70");        // Barliman Butterbur: [gandalf] Man ally, vitality 2; a fellowship ability never used
					put("troop", "1_177");          // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("troop2", "1_177");
					put("raiders", "1_158");        // Uruk-hai Raiding Party: [isengard] Uruk-hai minion, vitality 3, damage +1 only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site2", "13_193");         // Isenwash in place of Abandoned Mine Shaft
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void IsenwashStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Isenwash
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. <b>Plains</b>. At the start of the regroup phase, each Shadow player may exert a minion to exert a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site2");

		assertEquals("Isenwash", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseTheShadowPlayerMayExertAMinionToExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var raiders = scn.GetShadowCard("raiders");
		var isenwash = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(barliman);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(isenwash);
		assertEquals("Isenwash", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop, troop2, raiders);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(troop2, 2);
		assertEquals(2, scn.GetWoundsOn(troop2));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(isenwash));
		scn.ShadowAcceptOptionalTrigger();

		// The cost: a minion of either culture and race that can exert; not the exhausted one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop2, frodo, guard));
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));

		// The effect, chosen by the Shadow player: a companion who can exert, of either culture and race; not the
		// exhausted Aragorn, not the ally, not a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, barliman, raiders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(2, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedAtTheStartOfTheManeuverPhaseAndDecliningItAtRegroupExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		var isenwash = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(isenwash);
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(isenwash));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(isenwash));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheOnlyMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		var isenwash = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(isenwash);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 2);
		assertEquals(2, scn.GetWoundsOn(troop));

		scn.SkipToPhase(Phase.REGROUP);

		// Vitality 3 with 2 wounds: the Troop cannot exert, and the companions could.
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(isenwash));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedItIsOfferedAndTheMinionExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var isenwash = scn.GetShadowSite("site2");
		// Frodo, the only companion: vitality 4 with 3 wounds.
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(isenwash);
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(isenwash));
		scn.ShadowAcceptOptionalTrigger();

		// The lone minion pays unasked; no companion can exert.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnAtTheSameIsenwashTheFirstPlayerIsTheShadowPlayerOfferedIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo2 = scn.GetShadowRingBearer();
		var guard2 = scn.GetShadowCard("guard");
		var troop1 = scn.GetFreepsCard("troop");
		var isenwash = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(guard2);

		// Player 1's turn: player 2 lays his Isenwash, and with no minion in play it does nothing.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(isenwash);
		assertEquals("Isenwash", scn.GetCurrentSite().getBlueprint().getTitle());

		// Player 2's turn: his fellowship moves to the same Isenwash, and player 1 is the Shadow player.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		assertSame(isenwash, scn.GetCurrentSite());
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhaseInverted(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(isenwash));
		scn.FreepsAcceptOptionalTrigger();

		// Player 1's lone minion pays unasked; he chooses which of player 2's companions exerts.
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo2, guard2));
		scn.FreepsChooseCard(guard2);
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(frodo2));
		assertTrue(scn.ShadowDecisionAvailable("Play Regroup action or Pass"));
	}
}
