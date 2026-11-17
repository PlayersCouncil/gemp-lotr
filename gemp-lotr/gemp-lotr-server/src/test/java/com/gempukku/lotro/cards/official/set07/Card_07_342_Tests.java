package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_342_Tests
{

	// The King path, whose site 4 is Osgiliath Fallen.  Site 3 is The Dimholt (a fellowship ability used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7; a skirmish ability for a Hobbit, used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
					// Orc Chieftain: [sauron] Orc, strength 7, no damage bonus; its text answers a weapon played on it
					put("chieftain", "1_266");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OsgiliathFallenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Osgiliath Fallen
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 4
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: <b>Skirmish:</b> Spot your minion and remove 2 burdens to make that minion <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Osgiliath Fallen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void SkirmishActionRemovesTwoBurdensToMakeAMinionOfTheShadowPlayersDamagePlus1ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var osgiliath = scn.GetShadowSite(4);
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(3);
		// Three burdens, so that "remove 2" is told from "remove all".
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.FreepsPass();

		assertSame(osgiliath, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{boromir, chieftain});
		scn.FreepsResolveSkirmish(aragorn);

		// The Free Peoples player has burdens to remove, but no minion of his own to spot.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetBurdens());
		assertFalse(scn.FreepsActionAvailable(osgiliath));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(osgiliath));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(chieftain, Keyword.DAMAGE));

		scn.ShadowUseCardAction(osgiliath);
		// The minion skirmishing now and the one waiting in the other skirmish; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, boromir, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(chieftain);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetKeywordCount(chieftain, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's skirmish is resolved; the bonus was for that skirmish phase alone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetKeywordCount(chieftain, Keyword.DAMAGE));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyOneBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var osgiliath = scn.GetShadowSite(4);
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsPass();

		assertSame(osgiliath, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The cost is judged payable in full before it begins: one burden of two is not enough.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(osgiliath));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(osgiliath));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsOfferedWithOnlyAnOrcMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var osgiliath = scn.GetShadowSite(4);
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsPass();

		assertSame(osgiliath, scn.GetCurrentSite());
		// A [sauron] Orc alone, where the cost test spots an [isengard] Uruk-hai alone and the first test both.
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(osgiliath));
	}
}
