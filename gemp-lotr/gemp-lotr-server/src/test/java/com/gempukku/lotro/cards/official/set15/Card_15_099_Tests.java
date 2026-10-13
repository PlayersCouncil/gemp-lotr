package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_99");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chieftain", "15_99");
					put("goblin", "11_137");    // Scurrying Goblin: the [orc] minion to spot; its own offer comes only when it is played underground
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession, on a Dwarf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackLandChieftainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Black Land Chieftain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] minion.<br>When you play Black Land Chieftain, you may add a threat for each Free Peoples possession and each Free Peoples artifact you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Black Land Chieftain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  "A threat for each
	// Free Peoples possession and each Free Peoples artifact" is no threat where there is neither.
	@Test
	public void TheThreatsAreNotOfferedWithNoFreePeoplesPossessionOrArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var chieftain = scn.GetShadowCard("chieftain");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Two companions, so a threat could be added; The One Ring is neither a possession nor an artifact.
		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheThreatsAreOfferedWithAFreePeoplesPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		var chieftain = scn.GetShadowCard("chieftain");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BlackLandChieftainTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(5, scn.GetTwilight());
	}
}
