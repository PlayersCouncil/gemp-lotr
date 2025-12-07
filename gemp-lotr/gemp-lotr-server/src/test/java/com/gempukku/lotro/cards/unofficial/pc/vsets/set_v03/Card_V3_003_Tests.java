package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mantle", "103_3");
					put("gandalf", "1_364");
					put("sleep", "1_84"); // Fellowship spell event
					put("narya", "3_34"); // Gandalf artifact
					put("aragorn", "1_89");
					put("legolas", "1_50");

					// Free Peoples card to be hindered and then restored
					put("fpcondition", "1_365"); // Need: any Free Peoples support area card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MantleoftheWhiteWizardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Mantle of the White Wizard
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you play a spell, you may hinder a [Gandalf] artifact to restore a Free Peoples card.
		* 	Each time the fellowship moves to or from a sanctuary, you may exert Gandalf to heal another companion. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mantle");

		assertEquals("Mantle of the White Wizard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MantleHindersArtifactToRestoreCardWhenSpellPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var mantle = scn.GetFreepsCard("mantle");
		var sleep = scn.GetFreepsCard("sleep");
		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.MoveCardsToSupportArea(mantle);
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		// Manually hinder frodo so we can restore it
		scn.HinderCard(frodo);
		scn.FreepsDeclineOptionalTrigger(); //Narya

		assertFalse(scn.IsHindered(narya));
		assertTrue(scn.IsHindered(frodo));

		// Play spell
		scn.FreepsPlayCard(sleep);

		// Should trigger - choose artifact to hinder
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.IsHindered(narya));
		scn.FreepsChooseCard(frodo);
		assertFalse(scn.IsHindered(frodo));
	}

	@Test
	public void MantleExertsGandalfToHealCompanionWhenMovingToSanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");

		scn.MoveCardsToSupportArea(mantle);
		scn.MoveCompanionsToTable(gandalf, aragorn, legolas);

		scn.StartGame();

		// Add wounds to companions
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(legolas, 1);

		// Skip to site 2
		scn.SkipToSite(2);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));

		// Move to site 3 (sanctuary)
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetCurrentSiteNumber());

		// Should trigger - exert Gandalf to heal companion
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Gandalf exerted
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Choose companion to heal (should have 2 choices: Aragorn or Legolas, not Gandalf)
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsCanChooseCharacter(aragorn));
		assertTrue(scn.FreepsCanChooseCharacter(legolas));
		assertFalse(scn.FreepsCanChooseCharacter(gandalf));

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void MantleExertsGandalfToHealCompanionWhenMovingFromSanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(mantle);
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 2);

		// Start at site 3 (sanctuary)
		scn.SkipToSite(3);

		scn.FreepsDeclineSanctuaryHealing();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Move to site 4 (moving FROM sanctuary)
		scn.FreepsPassCurrentPhaseAction();
		// Should trigger
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetCurrentSiteNumber());
	}

	@Test
	public void MantleDoesNotTriggerOnSanctuaryMovementWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(mantle);
		scn.MoveCompanionsToTable(aragorn);
		// No Gandalf

		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 2);

		// Skip to site 2
		scn.SkipToSite(2);

		// Move to site 3 (sanctuary)
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetCurrentSiteNumber());

		// Should NOT trigger - no Gandalf to exert
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(aragorn));
	}
}
