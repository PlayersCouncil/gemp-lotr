package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_V3_018_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anduril", "103_18");
					put("aragorn", "1_89");
					put("eomer", "4_267"); // Man, not Gondor (Rohan)
					put("kingdead", "8_38"); // Gondor, not Man (Wraith)
					put("legolas", "1_50"); // Neither Gondor nor Man (Elven)
					put("gandalf", "1_364");

					put("uruk", "1_143");
					put("uruk2", "1_143");
					put("slayer", "3_93"); // Morgul Slayer - wounds companion in Regroup
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
	public void AndurilAttachesToAragornAndGivesDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(anduril);

		scn.StartGame();

		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		int aragornBaseStrength = scn.GetStrength(aragorn);

		// Should auto-attach to Aragorn (only valid target)
		scn.FreepsPlayCard(anduril);
		assertAttachedTo(anduril, aragorn);

		// Should give +3 strength and damage +1
		assertEquals(aragornBaseStrength + 3, scn.GetStrength(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DAMAGE));
	}

	@Test
	public void AndurilResponseHindersGondorCompanionWithoutAddingTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var kingdead = scn.GetFreepsCard("kingdead"); // Gondor, not Man
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn, kingdead);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Cheat slayer onto board in Regroup
		scn.MoveMinionsToTable(slayer);

		int twilightBefore = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsHindered(kingdead));

		scn.FreepsPass();

		// Use Slayer to wound King of the Dead
		assertTrue(scn.ShadowActionAvailable(slayer));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(kingdead);

		// Response should be offered
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// King of Dead should be hindered, 2 threats added
		assertTrue(scn.IsHindered(kingdead));
		assertEquals(2, scn.GetThreats());

		// No twilight added (Gondor companion)
		assertEquals(twilightBefore, scn.GetTwilight());

		// Wound should be prevented
		assertEquals(0, scn.GetWoundsOn(kingdead));
	}

	@Test
	public void AndurilResponseHindersManCompanionWithoutAddingTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer"); // Man, not Gondor
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn, eomer);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer);

		int twilightBefore = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsHindered(eomer));

		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(eomer);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Eomer should be hindered, 2 threats added
		assertTrue(scn.IsHindered(eomer));
		assertEquals(2, scn.GetThreats());

		// No twilight added (Man companion)
		assertEquals(twilightBefore, scn.GetTwilight());

		assertEquals(0, scn.GetWoundsOn(eomer));
	}

	@Test
	public void AndurilResponseHindersNonGondorNonManCompanionAndAddsTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas"); // Neither Gondor nor Man
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer);

		int twilightBefore = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsHindered(legolas));

		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(legolas);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Legolas should be hindered, 2 threats added
		assertTrue(scn.IsHindered(legolas));
		assertEquals(2, scn.GetThreats());

		// Twilight SHOULD be added (+2 for non-Gondor, non-Man)
		assertEquals(twilightBefore + 2, scn.GetTwilight());

		assertEquals(0, scn.GetWoundsOn(legolas));
	}

	@Test
	public void AndurilResponseNotOfferedWhenAragornWounded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);
		// Only Aragorn as unbound companion (Frodo is ring-bound)

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer);

		scn.FreepsPass();

		// Slayer can wound Aragorn
		scn.ShadowUseCardAction(slayer);
		// Aragorn should be auto-selected as only valid target for Slayer

		// Response should NOT be offered (Aragorn is excluded from trigger)
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Aragorn should take the wound
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void AndurilResponseCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer);

		assertEquals(0, scn.GetThreats());

		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(legolas);

		// Decline response
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Legolas should NOT be hindered, no threats added
		assertFalse(scn.IsHindered(legolas));
		assertEquals(0, scn.GetThreats());

		// Legolas should take the wound
		assertEquals(1, scn.GetWoundsOn(legolas));
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
