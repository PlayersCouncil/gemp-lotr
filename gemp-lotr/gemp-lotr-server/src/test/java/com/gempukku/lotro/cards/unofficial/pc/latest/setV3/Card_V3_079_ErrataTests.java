package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_079_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "103_79");
					put("runner", "1_178");
					put("guard", "1_7"); // STR 6, VIT 3
					put("gimli", "1_12"); // STR 6, VIT 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Toldea, Blessed with Brutality
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		* 	Each time this minion is assigned to a stronger companion, you may exhaust this minion to hinder that companion.
		* 	Each time this minion is assigned to a weaker companion, you may exert this minion to exert that companion or add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Ulaire Toldea", card.getBlueprint().getTitle());
		assertEquals("Blessed with Brutality", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToldeaAssignedToWeakerCompanionOffersChoiceOfExertOrThreat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		scn.MoveMinionsToTable(toldea);

		// Guard is STR 6, Toldea is STR 13. Guard is weaker.
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		scn.SkipToAssignments();

		// Assign guard to toldea. Guard (STR 6) is weaker than Toldea (STR 13).
		scn.FreepsAssignToMinions(guard, toldea);

		// The errata: when assigned to a weaker companion, may exert self to get a choice:
		// exert that companion OR add a threat.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Toldea should be exerted
		assertEquals(1, scn.GetWoundsOn(toldea));

		// Shadow should be presented with a choice: exert the companion or add a threat
		int threatsBefore = scn.GetThreats();
		// Choose to add a threat
		scn.ShadowChoose("Add a threat");
		assertEquals(threatsBefore + 1, scn.GetThreats());
		// Guard should not be exerted since we chose threat instead
		assertEquals(0, scn.GetWoundsOn(guard));
	}
}
