package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_004_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: dying companion, bears the possession
		put("gimli", "1_13"); // Gimli: eligible alternate bearer
		put("cloak", "1_42"); // Elven Cloak: non-unique possession, attaches to any companion
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_4", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_4"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_4
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Each time your companion is killed, you may transfer each possession attached to that
		 * companion to another eligible bearer.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_4", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void OwnerMayTransferThePossessionBeforeTheCompanionDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		// pump the loop just once so the game stops at the offered optional trigger rather than
		// blindly declining everything in transit
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();
		// Only one possession is attached, so the "choose which possession" step auto-resolves and
		// the first decision presented is the destination bearer.
		scn.FreepsChooseCard(gimli);

		assertEquals(gimli, cloak.getAttachedTo());
		assertInZone(Zone.DEAD, aragorn);
	}

	@Test
	public void DecliningLeavesThePossessionToBeDiscardedWithTheCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, aragorn);
		assertNotEquals(gimli, cloak.getAttachedTo());
	}

	@Test
	public void NotOfferedToNonOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.FreepsPassCurrentPhaseAction();

		// The card's owner (Freeps) is offered the trigger; the Shadow player, who does not own this
		// copy of the metasite, is not - even though "your" would otherwise resolve against whichever
		// player is asked.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mod));
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsCompanionGetsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, cloak);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.FreepsPassCurrentPhaseAction();

		// "your,companion" combined with IsOwner: Shadow owns the mod, so Freeps' own companion
		// dying does not trigger it.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mod));
		assertInZone(Zone.DEAD, aragorn);
	}
}
