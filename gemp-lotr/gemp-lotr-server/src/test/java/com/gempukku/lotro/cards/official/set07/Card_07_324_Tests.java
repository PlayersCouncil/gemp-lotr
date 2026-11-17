package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_324_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "7_324");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used

					// Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, site 4; no text.  Roaming at site 2.
					put("troop", "1_177");
					// Uruk Savage: an [isengard] Uruk-hai, vitality 3, site 5; damage +1 and nothing else.  Roaming at site 2.
					put("savage", "1_151");
					// Rough Man of the South: a [men] Man, vitality 3, site 2: not roaming at site 2.  Stronger in
					// regions 1 and 2, and nothing else.
					put("rough", "18_73");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Pippin, Wearer of Black and Silver
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Skirmish:</b> If Pippin is not assigned to a skirmish, return him to your hand to wound a
		 * 	roaming minion twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Wearer of Black and Silver", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void UnassignedPippinReturnsToHandToWoundARoamingMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var rough = scn.GetShadowCard("rough");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, rough);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// At site 2: the Troop (site 4) and the Savage (site 5) are roaming, the Rough Man (site 2) is not.
		// The Troop is in the skirmish being resolved; the Savage and the Rough Man are assigned to nothing.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(rough, Keyword.ROAMING));
		assertTrue(scn.IsCharSkirmishing(troop));
		assertFalse(scn.IsCharAssigned(savage));
		assertFalse(scn.IsCharAssigned(pippin));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		assertInZone(Zone.HAND, pippin);

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rough));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Two wounds on the one minion chosen.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(rough));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhilePippinIsAssignedToASkirmishNorInHisOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var rough = scn.GetShadowCard("rough");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, rough);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{pippin, savage});
		scn.ShadowDeclineAssignments();

		// Aragorn's skirmish: Pippin is assigned to a skirmish that is still to come.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssigned(pippin));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));
		scn.PassSkirmishActions();

		// Nor in his own skirmish.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishing(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
	}
}
