package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("domain", "103_20");
					put("kingdead", "8_38"); // Gondor Wraith, vitality 4
					put("aragorn", "1_89");
					put("boromir", "1_97"); // Companion to kill for threat wounds

					put("slayer", "3_93"); // Morgul Slayer - wounds companion in Regroup
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DomainoftheDeadKingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Domain of the Dead King
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: When you play this, spot a [gondor] Wraith to remove a threat.
		* 	Each time a [gondor] Wraith takes a threat wound, wound a minion.
		* 	Response: If an exhausted [gondor] Wraith is about to take a wound, add a threat and hinder this artifact to hinder that Wraith.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("domain");

		assertEquals("Domain of the Dead King", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}


	@Test
	public void DomainRemovesThreatWhenPlayedWithGondorWraith() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var kingdead = scn.GetFreepsCard("kingdead");

		scn.MoveCompanionsToTable(kingdead);
		scn.MoveCardsToHand(domain);

		scn.StartGame();

		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());

		scn.FreepsPlayCard(domain);

		// Trigger should remove 1 threat
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, domain);
	}

	@Test
	public void DomainCanBePlayedWithoutGondorWraith() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var aragorn = scn.GetFreepsCard("aragorn");
		// No Gondor Wraith

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(domain);

		scn.StartGame();

		scn.AddThreats(2);

		// Should still be playable
		assertTrue(scn.FreepsPlayAvailable(domain));
		scn.FreepsPlayCard(domain);

		// Trigger fizzles, threats unchanged
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, domain);
	}

	@Test
	public void DomainWoundsMinionWhenGondorWraithTakesThreatWound() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var kingdead = scn.GetFreepsCard("kingdead");
		var boromir = scn.GetFreepsCard("boromir");
		var slayer = scn.GetShadowCard("slayer");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(kingdead, boromir);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();

		scn.AddThreats(1);
		// Pre-wound Boromir to 1 vitality (vitality 3, so 2 wounds)
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(1, scn.GetVitality(boromir));

		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer, runner);

		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(kingdead));

		scn.FreepsPass();

		// Use Slayer to kill Boromir
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(boromir);

		// Boromir dies, 1 threat becomes wound assigned to companion
		// Assign threat wound to King of the Dead
		scn.FreepsChooseCard(kingdead);

		// King takes threat wound
		assertEquals(1, scn.GetWoundsOn(kingdead));

		// Domain triggers - choose minion to wound
		scn.FreepsChooseCard(runner);
	}

	@Test
	public void DomainResponseHindersExhaustedWraithAboutToTakeWound() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var kingdead = scn.GetFreepsCard("kingdead");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(kingdead);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();

		// Exhaust King of the Dead (vitality 4, so 3 wounds)
		scn.AddWoundsToChar(kingdead, 3);
		assertEquals(1, scn.GetVitality(kingdead));

		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer);

		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsHindered(domain));
		assertFalse(scn.IsHindered(kingdead));

		scn.FreepsPass();

		// Use Slayer to wound King of the Dead
		scn.ShadowUseCardAction(slayer);
		// King auto-selected as only valid target

		// Response should be offered
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Domain hindered, threat added, King hindered
		assertTrue(scn.IsHindered(domain));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.IsHindered(kingdead));

		// King should not have taken the wound (hindered prevents it)
		assertEquals(3, scn.GetWoundsOn(kingdead));
	}

	@Test
	public void DomainResponseNotOfferedForNonExhaustedWraith() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var kingdead = scn.GetFreepsCard("kingdead");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(kingdead);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();

		// King is NOT exhausted (no wounds)
		assertEquals(0, scn.GetWoundsOn(kingdead));

		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer);

		scn.FreepsPass();

		// Use Slayer to wound King of the Dead
		scn.ShadowUseCardAction(slayer);

		// Response should NOT be offered (King is not exhausted)
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// King takes the wound normally
		assertEquals(1, scn.GetWoundsOn(kingdead));
	}

	@Test
	public void DomainResponseCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var kingdead = scn.GetFreepsCard("kingdead");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(kingdead);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();

		// Exhaust King of the Dead
		scn.AddWoundsToChar(kingdead, 3);

		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer);

		scn.FreepsPass();

		// Use Slayer to wound King of the Dead
		scn.ShadowUseCardAction(slayer);

		// Decline response
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Domain not hindered, no threat added, King not hindered
		assertFalse(scn.IsHindered(domain));
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsHindered(kingdead));

		// King dies from the wound (was at 1 vitality)
		assertInZone(Zone.DEAD, kingdead);
	}

	@Test
	public void DomainResponseNotOfferedWhenAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var domain = scn.GetFreepsCard("domain");
		var kingdead = scn.GetFreepsCard("kingdead");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(kingdead);
		scn.MoveCardsToSupportArea(domain);

		scn.StartGame();

		// Exhaust King, pre-hinder Domain
		scn.AddWoundsToChar(kingdead, 3);
		scn.HinderCard(domain);
		assertTrue(scn.IsHindered(domain));

		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer);

		scn.FreepsPass();

		// Use Slayer to wound King of the Dead
		scn.ShadowUseCardAction(slayer);

		// Response should NOT be offered (Domain already hindered, can't pay cost)
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}
}
