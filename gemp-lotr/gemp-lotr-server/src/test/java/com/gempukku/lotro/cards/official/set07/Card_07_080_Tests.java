package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anduril", "7_80");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndurilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Andúril, King's Blade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Aragorn.<br>If you cannot spot a threat, Aragorn is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("anduril");

		assertEquals("Andúril", card.getBlueprint().getTitle());
		assertEquals("King's Blade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void AndurilIsBorneByAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(anduril, aragorn);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(anduril));

		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(anduril));
		scn.FreepsPlayCard(anduril);
		assertEquals(Zone.ATTACHED, anduril.getZone());
		assertEquals(aragorn, anduril.getAttachedTo());
	}

	@Test
	public void AndurilMakesAragornDefenderIfNoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.AddThreats(1);

		assertEquals(1, scn.GetThreats());
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
	}
}
