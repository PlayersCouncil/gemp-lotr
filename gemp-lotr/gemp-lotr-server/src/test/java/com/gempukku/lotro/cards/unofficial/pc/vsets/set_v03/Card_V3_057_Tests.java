package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_057_Tests
{

// ----------------------------------------
// SAVAGE HARPOON TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("harpoon1", "103_57");    // Savage Harpoon
					put("harpoon2", "103_57");    // Second copy
					put("helmsman1", "103_46");   // Corsair Helmsman
					put("helmsman2", "103_46");   // Second Corsair
					put("ships", "8_65");         // Ships of Great Draught - token source

					put("aragorn", "1_89");
					put("athelas", "1_94");       // FP possession (item) on Aragorn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SavageHarpoonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Savage Harpoon
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Strength: -3
		 * Game Text: Each time the fellowship moves, the Free Peoples player must hinder bearer or a Free Peoples item on bearer.
		* 	Maneuver: Exert 2 corsairs and remove 2 [raider] tokens from a possession to transfer this from your support area to an unbound companion. Limit 1 per bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("harpoon1");

		assertEquals("Savage Harpoon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-3, card.getBlueprint().getStrength());
	}



	@Test
	public void SavageHarpoonTransfersToUnboundCompanionAndAppliesStrengthPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harpoon = scn.GetShadowCard("harpoon1");
		var helmsman1 = scn.GetShadowCard("helmsman1");
		var helmsman2 = scn.GetShadowCard("helmsman2");
		var ships = scn.GetShadowCard("ships");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(harpoon, ships);
		scn.AddTokensToCard(ships, 5);
		scn.MoveMinionsToTable(helmsman1, helmsman2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		int aragornStrengthBefore = scn.GetStrength(aragorn);
		assertInZone(Zone.SUPPORT, harpoon);

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(harpoon));
		scn.ShadowUseCardAction(harpoon);

		assertAttachedTo(harpoon, aragorn);
		assertEquals(1, scn.GetWoundsOn(helmsman1));
		assertEquals(1, scn.GetWoundsOn(helmsman2));
		assertEquals(3, scn.GetCultureTokensOn(ships)); // 5 - 2 = 3
		assertEquals(aragornStrengthBefore - 3, scn.GetStrength(aragorn));
	}

	@Test
	public void SavageHarpoonMoveTriggerForcesFPToHinderBearerOrItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harpoon = scn.GetShadowCard("harpoon1");
		var helmsman1 = scn.GetShadowCard("helmsman1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveMinionsToTable(helmsman1);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, harpoon, athelas); // Harpoon already attached

		scn.StartGame();

		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(athelas));

		// Fellowship moves (start of game moved to site 1, now move to site 2)
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst(); //bree gate

		// FP must choose to hinder bearer or item
		scn.FreepsChoose("item");
		scn.FreepsChooseCard(athelas);

		assertTrue(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(aragorn));
	}

	@Test
	public void SavageHarpoonMoveTriggerCanHinderBearerInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harpoon = scn.GetShadowCard("harpoon1");
		var helmsman1 = scn.GetShadowCard("helmsman1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveMinionsToTable(helmsman1);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, harpoon, athelas);

		scn.StartGame();
		// Fellowship moves (start of game moved to site 1, now move to site 2)
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst(); //bree gate

		scn.FreepsChoose("Aragorn");

		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(athelas));
	}

	@Test
	public void SavageHarpoonLimitOnePerBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harpoon1 = scn.GetShadowCard("harpoon1");
		var harpoon2 = scn.GetShadowCard("harpoon2");
		var helmsman1 = scn.GetShadowCard("helmsman1");
		var helmsman2 = scn.GetShadowCard("helmsman2");
		var ships = scn.GetShadowCard("ships");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(harpoon1, harpoon2, ships);
		scn.AddTokensToCard(ships, 10);
		scn.MoveMinionsToTable(helmsman1, helmsman2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Transfer first harpoon to Aragorn
		scn.ShadowUseCardAction(harpoon1);

		assertAttachedTo(harpoon1, aragorn);

		// Heal Corsairs for second attempt
		scn.RemoveWoundsFromChar(helmsman1, 1);
		scn.RemoveWoundsFromChar(helmsman2, 1);

		scn.FreepsPassCurrentPhaseAction();

		// Second harpoon should not be able to target Aragorn (limit 1 per bearer)
		assertTrue(scn.ShadowActionAvailable(harpoon2));
		scn.ShadowUseCardAction(harpoon2);

		// Aragorn should not be a valid target, so it fizzles with no other options
		assertNotAttachedTo(harpoon2, aragorn);
	}
}
