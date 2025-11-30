package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anduril", "103_18");
					put("aragorn", "1_89");
					put("gandalf", "1_364");

					put("uruk", "1_143");
					put("uruk2", "1_143");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndurilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Anduril, Legend Remade
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: 3
		 * Game Text: Bearer must be Aragorn.  He is <b>damage +1</b>.
		* 	Response: If an unbound companion is about to take a wound (except Aragorn),
		* 	add 2 threats to hinder that companion.
		* 	Add (2) unless that companion was [gondor] or a Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("anduril");

		assertEquals("Andúril", card.getBlueprint().getTitle());
		assertEquals("Legend Remade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void AndurilPreventsTheWoundsFromBeingAppliedToHinderedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, anduril);

		var uruk1 = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");

		scn.MoveMinionsToTable(uruk1, uruk2);

		scn.StartGame();
		
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { gandalf, uruk1 },
				new PhysicalCardImpl[] { aragorn, uruk2 }
		);

		scn.FreepsResolveSkirmish(gandalf);
		scn.BothPass();

		assertFalse(scn.IsHindered(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetTwilight());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.IsHindered(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetThreats());
		assertEquals(7, scn.GetTwilight());

		scn.FreepsResolveSkirmish(aragorn);
		scn.BothPass();
		//Cannot be used to prevent wounds on aragorn
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.BothPass();
		scn.FreepsChooseToStay();

		//No shenanigans around timing; the wound wasn't applied even after restoration
		assertFalse(scn.IsHindered(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}
}
