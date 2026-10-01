package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_94_021_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("foes1", "1_105"); // Foes of Mordor: non-unique Free Peoples support-area condition, no play requirement
		put("foes2", "1_105"); // second copy, to test the uniqueness restriction
		put("ambition1", "1_133"); // Saruman's Ambition: non-unique Shadow support-area condition, proves side(freeps) scoping
		put("ambition2", "1_133"); // second copy
		put("gimli", "1_13"); // Gimli, Dwarf of Erebor: Dwarf on the table so Dwarf Guard can be played
		put("guard1", "1_7"); // Dwarf Guard: non-unique Free Peoples companion, proves the filter is condition-only
		put("guard2", "1_7"); // second copy
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_21", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_21"
		);
	}

	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_21
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Your Free Peoples conditions are unique.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_21", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void OwnerCannotPlayASecondCopyOfAFreepsCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var foes1 = scn.GetFreepsCard("foes1");
		var foes2 = scn.GetFreepsCard("foes2");

		scn.MoveCardsToSupportArea(foes1);
		scn.MoveCardsToHand(foes2);
		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(foes2));
	}

	@Test
	public void SecondCopyOfAFreepsConditionIsPlayableWithoutTheModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoModScenario();
		var foes1 = scn.GetFreepsCard("foes1");
		var foes2 = scn.GetFreepsCard("foes2");

		scn.MoveCardsToSupportArea(foes1);
		scn.MoveCardsToHand(foes2);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(foes2));
		scn.FreepsPlayCard(foes2);

		assertInZone(Zone.SUPPORT, foes1);
		assertInZone(Zone.SUPPORT, foes2);
	}

	@Test
	public void OwnersNonConditionsAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");

		scn.MoveCompanionsToTable(gimli, guard1);
		scn.MoveCardsToHand(guard2);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(guard2));
	}

	@Test
	public void NonOwnersFreepsConditionsAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// "your" scopes the modifier to the runner; the Shadow player's copy leaves the Freeps player alone
		var scn = GetShadowScenario();
		var foes1 = scn.GetFreepsCard("foes1");
		var foes2 = scn.GetFreepsCard("foes2");

		scn.MoveCardsToSupportArea(foes1);
		scn.MoveCardsToHand(foes2);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(foes2));
		scn.FreepsPlayCard(foes2);

		assertInZone(Zone.SUPPORT, foes1);
		assertInZone(Zone.SUPPORT, foes2);
	}

	@Test
	public void OwnersShadowConditionsAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// side(freeps) in the filter: the owner's own Shadow conditions stay non-unique
		var scn = GetShadowScenario();
		var ambition1 = scn.GetShadowCard("ambition1");
		var ambition2 = scn.GetShadowCard("ambition2");

		scn.MoveCardsToSupportArea(ambition1);
		scn.MoveCardsToHand(ambition2);
		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(ambition2));
		scn.ShadowPlayCard(ambition2);

		assertInZone(Zone.SUPPORT, ambition1);
		assertInZone(Zone.SUPPORT, ambition2);
	}
}
