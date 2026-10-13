package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_96");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tongue", "4_96");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: spotted; his own signet is Frodo's
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Gandalf signet, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: Gandalf signet
					// Uruk-hai Raiding Party: strength 9, Damage +1 its whole text.  It beats Aragorn by one
					// and deals two wounds.
					put("raiders", "1_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KeepYourForkedTongueStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Keep Your Forked Tongue
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot Gandalf and 3 twilight tokens to prevent all wounds to a companion who has the Gandalf signet.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Keep Your Forked Tongue", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PreventsEveryWoundToACompanionWithTheGandalfSignetInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var tongue = scn.GetFreepsCard("tongue");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(gandalf, aragorn, gimli);
		scn.MoveCardsToHand(tongue);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		scn.FreepsResolveSkirmish(aragorn);

		// The move added 4 for the companions and 2 for Ettenmoors: more than the 3 to be spotted.
		assertEquals(6, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(raiders));
		assertTrue(scn.FreepsPlayAvailable(tongue));

		scn.FreepsPlayCard(tongue);
		// Gandalf himself and Frodo have the Frodo signet.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));

		scn.FreepsChooseCard(aragorn);
		// 1 for the event; the 3 tokens are spotted and stay.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.DISCARD, tongue);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.ShadowPass();
		scn.FreepsPass();
		// Aragorn loses 8 to 9 and Damage +1 makes it two wounds: still 1, where one wound prevented
		// would read 2 and none prevented 3.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
