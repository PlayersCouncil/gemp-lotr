package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "7_71");
					put("balrog", "12_79");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Sméagol, Always Helps
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: 
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ring-bound</b>. To play, add a burden.<br><b>Assignment:</b> Assign a minion to Sméagol and add 2 threats to exhaust that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Always Helps", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet()); 
	}

	@Test
	public void SmeagolAddsABurdenWhenPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(1, scn.GetBurdens());
		scn.FreepsPlayCard(smeagol);
		assertEquals(2, scn.GetBurdens());
	}

	@Test
	public void SmeagolAbilityCantBeUsedIfNoThreatRoom() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var balrog = scn.GetShadowCard("balrog");
		scn.MoveMinionsToTable(balrog);

		scn.StartGame();
		scn.AddThreats(2);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertFalse(scn.FreepsActionAvailable(smeagol));
	}

	@Test
	public void SmeagolAbilityAssignsMinionAndAdds2ThreatsToExhaustMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var balrog = scn.GetShadowCard("balrog");
		scn.MoveMinionsToTable(balrog);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetVitality(balrog));
		assertFalse(scn.IsCharAssigned(smeagol));
		assertFalse(scn.IsCharAssigned(balrog));

		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsCharAssigned(smeagol));
		assertTrue(scn.IsCharAssigned(balrog));
		assertEquals(1, scn.GetVitality(balrog));
	}
}
