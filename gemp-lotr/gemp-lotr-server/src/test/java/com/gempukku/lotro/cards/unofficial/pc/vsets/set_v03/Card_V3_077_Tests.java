package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static org.junit.Assert.*;

public class Card_V3_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "103_77");
					put("witchking", "1_237");
					put("rider", "12_161");
					put("sword", "1_218");     // Nazgul Sword - possession
					put("rancor", "9_44");     // Ring of Rancor - artifact

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Nertea, Sanctified for Cruelty
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Fierce.
		* 	Each time you play a minion, you may exert it to play a possession on it from your discard pile (or an artifact if you exert this minion as well).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
		assertEquals("Sanctified for Cruelty", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}


// ======== BASIC POSSESSION FROM DISCARD ========

	@Test
	public void NerteaCanEquipPlayedMinionWithPossessionFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var sword = scn.GetShadowCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(nertea);
		scn.MoveCardsToHand(witchking);
		scn.MoveCardsToDiscard(sword);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertInDiscard(sword);

		// Play Witch-king
		scn.ShadowPlayCard(witchking);

		// Trigger available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Witch-king exerted (cost)
		assertEquals(1, scn.GetWoundsOn(witchking));

		// Sword auto-selected as only possession, attaches to Witch-king
		assertAttachedTo(sword, witchking);
	}

// ======== ARTIFACT OPTION ========

	@Test
	public void NerteaCanExertToUpgradeToArtifactWhenOnlyArtifactsAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var rancor = scn.GetShadowCard("rancor");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(nertea);
		scn.MoveCardsToHand(witchking);
		scn.MoveCardsToDiscard(rancor);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(nertea));
		assertEquals(0, scn.GetWoundsOn(witchking));

		scn.ShadowPlayCard(witchking);
		scn.ShadowAcceptOptionalTrigger();

		// Witch-king exerted
		assertEquals(1, scn.GetWoundsOn(witchking));

		// Choose artifact option (Nertëa exerts as additional cost)
		assertTrue(scn.ShadowDecisionAvailable("play an artifact from discard?"));
		scn.ShadowChooseYes();

		// Nertëa also exerted
		assertEquals(1, scn.GetWoundsOn(nertea));

		// Rancor attaches to Witch-king
		assertAttachedTo(rancor, witchking);
	}

	@Test
	public void NerteaCanPlayPossessionWhenBothPossessionAndArtifactAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var sword = scn.GetShadowCard("sword");
		var rancor = scn.GetShadowCard("rancor");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(nertea);
		scn.MoveCardsToHand(witchking);
		scn.MoveCardsToDiscard(sword, rancor);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);
		scn.ShadowAcceptOptionalTrigger();

		// Should have choice between possession and artifact
		assertTrue(scn.ShadowChoiceAvailable("possession"));
		assertTrue(scn.ShadowChoiceAvailable("artifact"));

		// Choose possession (no additional exert)
		scn.ShadowChoose("possession");

		assertEquals(0, scn.GetWoundsOn(nertea));  // Nertëa not exerted
		assertAttachedTo(sword, witchking);
		assertInDiscard(rancor);  // Artifact still in discard
	}

	@Test
	public void NerteaCanPlayArtifactWhenBothPossessionAndArtifactAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var sword = scn.GetShadowCard("sword");
		var rancor = scn.GetShadowCard("rancor");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(nertea);
		scn.MoveCardsToHand(witchking);
		scn.MoveCardsToDiscard(sword, rancor);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);
		scn.ShadowAcceptOptionalTrigger();

		// Should have choice between possession and artifact
		assertTrue(scn.ShadowChoiceAvailable("possession"));
		assertTrue(scn.ShadowChoiceAvailable("artifact"));

		// Choose possession (no additional exert)
		scn.ShadowChoose("artifact");

		assertEquals(1, scn.GetWoundsOn(nertea));
		assertAttachedTo(rancor, witchking);
		assertInDiscard(sword);  // Possession still in discard
	}

	@Test
	public void NerteaArtifactOptionNotAvailableWhenNerteaExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var sword = scn.GetShadowCard("sword");
		var rancor = scn.GetShadowCard("rancor");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(nertea);
		scn.MoveCardsToHand(witchking);
		scn.MoveCardsToDiscard(sword, rancor);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(nertea, 1);  // Vitality 2, exhausted

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);
		scn.ShadowAcceptOptionalTrigger();

		// Artifact option should not be available (can't exert Nertëa)
		// Should just auto-play possession
		assertAttachedTo(sword, witchking);
		assertInDiscard(rancor);
	}

// ======== TRIGGER AVAILABILITY ========

	@Test
	public void NerteaTriggerNotAvailableWithNoItemsInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(nertea);
		scn.MoveCardsToHand(witchking);
		// No items in discard
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);

		// No items in discard, trigger not available
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void NerteaTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var witchking = scn.GetShadowCard("witchking");
		var sword = scn.GetShadowCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(nertea);
		scn.MoveCardsToHand(witchking);
		scn.MoveCardsToDiscard(sword);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);

		scn.ShadowDeclineOptionalTrigger();

		// Nothing exerted, sword still in discard
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertInDiscard(sword);
	}

// ======== SELF-TRIGGER ========

	@Test
	public void NerteaCanTriggerOffHimselfBeingPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var sword = scn.GetShadowCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(sword);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// Play Nertëa himself
		scn.ShadowPlayCard(nertea);

		// Should be able to trigger off his own play
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Nertëa exerts himself (the played minion)
		assertEquals(1, scn.GetWoundsOn(nertea));

		// Sword attaches to Nertëa
		assertAttachedTo(sword, nertea);
	}
}
