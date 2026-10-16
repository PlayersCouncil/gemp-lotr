package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_151_Tests
{

	// The Fellowship sites: site 2 is Ettenmoors, plains and no battleground.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ferocity", "4_151");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: companion, strength 3, vitality 4; +2 only with a weapon
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, damage +1 and nothing else
					// Isengard Retainer: [isengard] Orc, strength 8, vitality 3; a regroup ability used only on purpose
					put("retainer", "3_57");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers sites: site 4 is White Rocks, a battleground (its Shadow number counts mounted companions only).
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ferocity", "4_151");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FerocityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ferocity
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Uruk-hai strength +2 (or +3 if at a battleground).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ferocity");

		assertEquals("Ferocity", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AwayFromABattlegroundAnUrukHaiIsStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var ferocity = scn.GetShadowCard("ferocity");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var retainer = scn.GetShadowCard("retainer");
		scn.MoveCardsToHand(ferocity);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry, retainer);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		// A Skirmish event: not among the Shadow player's assignment actions.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(ferocity));
		scn.ShadowPass();

		// The Sentry is left unassigned.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{merry, retainer}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(ferocity));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(8, scn.GetStrength(retainer));

		scn.ShadowPlayCard(ferocity);
		// Uruk-hai of two cultures, the one skirmishing and one in no skirmish; an [isengard] Orc is not offered.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(retainer, aragorn, merry));
		scn.ShadowChooseCard(sentry);

		assertInZone(Zone.DISCARD, ferocity);
		assertEquals(10, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(retainer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still in force at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(sentry));

		// Aragorn, 8 against 5, wounds the Savage, who lives.
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(merry);

		// Gone in the next skirmish, which is another phase.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(sentry));
	}

	@Test
	public void AtABattlegroundTheUrukHaiIsStrengthPlus3InsteadOfPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var ferocity = scn.GetShadowCard("ferocity");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(ferocity);
		scn.SkipToAssignments();
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(savage));

		// The one Uruk-hai is chosen unasked: 5 + 3, not 5 + 2, and not 5 + 2 + 3.
		scn.ShadowPlayCard(ferocity);
		assertInZone(Zone.DISCARD, ferocity);
		assertEquals(8, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
